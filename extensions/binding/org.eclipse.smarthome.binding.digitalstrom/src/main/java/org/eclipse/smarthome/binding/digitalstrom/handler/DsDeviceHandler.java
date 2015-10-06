/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.handler;

import static org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.ChangeableDeviceConfigEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceSceneSpec;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.FunctionalColorGroupEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.JSONDeviceSceneSpecImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.OutputModeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.providers.DigitalSTROMThingTypeProvider;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link DsDeviceHandler} is responsible to handling the configuration, load supported channels of an
 * digitalSTROM device and handling commands which are send to one of the channels. <br>
 * <br>
 * For that it uses the {@link DssBridgeHandler} to execute the actual command and implements the
 * {@link DeviceStatusListener}
 * to get informed by changes from the accompanying {@link Device}.
 * <br>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class DsDeviceHandler extends BaseThingHandler implements DeviceStatusListener {

    private Logger logger = LoggerFactory.getLogger(DsDeviceHandler.class);

    /**
     * The {@link DigitalSTROMThingTypeProvider} add the supported thing types
     */
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet();// THING_TYPE_GE_KM200,THING_TYPE_GE_KL200

    private String dSID = null;

    private Device device;

    private DssBridgeHandler dssBridgeHandler;

    private Command lastComand = null;

    private String currentChannel = null;

    public DsDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing DigitalSTROM Device handler.");
        String configdSID = getConfig().get(DigitalSTROMBindingConstants.DEVICE_DSID).toString();

        if (!configdSID.isEmpty()) {
            dSID = configdSID;
            if (this.dssBridgeHandler == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR, "Bridge is missig");
            } else {
                bridgeHandlerInitialized(dssBridgeHandler, this.getBridge());
            }

        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "dSID is missig");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposes. Unregistering listener.");
        if (dSID != null) {
            if (dssBridgeHandler != null) {
                dssBridgeHandler.unregisterDeviceStatusListener(this);
            }
            device = null;
        }
    }

    @Override
    public void handleRemoval() {
        if (getDssBridgeHandler() != null) {
            this.dssBridgeHandler.childThingRemoved(dSID);
        }
        updateStatus(ThingStatus.REMOVED);
    }

    @Override
    public void thingUpdated(Thing thing) {
        // TODO: perhaps our own sequence, because we not really need to dispose() and initialize() the thing after an
        // update
        // dispose();
        this.thing = thing;
        if (device == null) {
            initialize();
        } else {
            // loadSensorChannels(thing.getConfiguration());
            onDeviceAdded(device);
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParmeters) {
        // can be overridden by subclasses
        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParmeter : configurationParmeters.entrySet()) {
            configuration.put(configurationParmeter.getKey(), configurationParmeter.getValue());
        }

        // load sensor priorities into the device and load sensor channels of the thing
        // loadSensorChannels(configuration);
        updateConfiguration(configuration);
        // initialize();
    }

    @Override
    protected void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        if (dSID != null) {
            if (thingHandler instanceof DssBridgeHandler) {
                this.dssBridgeHandler = (DssBridgeHandler) thingHandler;

                if (device == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "waiting for listener registartion");
                    logger.debug("Set status on {}", getThing().getStatus());
                    // note: this call implicitly registers our handler as a device-listener on the bridge
                    this.dssBridgeHandler.registerDeviceStatusListener(this);
                } else {
                    updateStatus(ThingStatus.ONLINE);
                }

            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        DssBridgeHandler dssBridgeHandler = getDssBridgeHandler();
        if (dssBridgeHandler == null) {
            logger.warn("DigitalSTROM bridge handler not found. Cannot handle command without bridge.");
            return;
        }

        if (device == null) {
            logger.debug("DigitalSTROM device not known on bridge. Cannot handle command.");
            return;
        }

        if (!device.isRollershutter()) {
            if (this.channelIsOutputChannel(channelUID.getId())) {
                if (command instanceof PercentType) {
                    device.setOutputValue(
                            (short) fromPercentToValue(((PercentType) command).intValue(), device.getMaxOutputValue()));
                } else if (command instanceof OnOffType) {
                    if (OnOffType.ON.equals(command)) {
                        device.setIsOn(true);
                    } else {
                        device.setIsOn(false);
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    if (IncreaseDecreaseType.INCREASE.equals(command)) {
                        device.increase();
                    } else {
                        device.decrease();
                    }
                } else if (command instanceof StringType)
                    device.setOutputValue(Short.parseShort(((StringType) command).toString()));
            } else {
                logger.warn("Command send to an unknown channel id: " + channelUID);
            }
        } else {
            if (channelUID.getId().equals(DigitalSTROMBindingConstants.CHANNEL_SHADE)) {
                if (command instanceof PercentType) {
                    device.setSlatPosition(
                            fromPercentToValue(((PercentType) command).intValue(), device.getMaxOutputValue()));
                    this.lastComand = command;
                } else if (command instanceof StopMoveType) {
                    if (StopMoveType.MOVE.equals(command)) {
                        handleCommand(channelUID, this.lastComand);
                    } else {
                        dssBridgeHandler.stopOutputValue(device);
                    }
                } else if (command instanceof UpDownType) {
                    if (UpDownType.UP.equals(command)) {
                        device.setIsOpen(true);
                        this.lastComand = command;
                    } else {
                        device.setIsOpen(false);
                        this.lastComand = command;
                    }
                }
            } else {
                logger.warn("Command send to an unknown channel id: " + channelUID);
            }
        }
    }

    private int fromPercentToValue(int percent, int max) {
        if (percent < 0 || percent == 0) {
            return 0;
        }
        if (max < 0 || max == 0) {
            return 0;
        }
        return (int) (max * ((float) percent / 100));
    }

    private synchronized DssBridgeHandler getDssBridgeHandler() {
        if (this.dssBridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.debug("cant find Bridge");
                return null;
            }
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof DssBridgeHandler) {
                this.dssBridgeHandler = (DssBridgeHandler) handler;
                this.dssBridgeHandler.registerDeviceStatusListener(this);
            } else {
                return null;
            }
        }
        return this.dssBridgeHandler;
    }

    @Override
    public synchronized void onDeviceStateChanged(DeviceStateUpdate deviceStateUpdate) {
        if (device != null) {
            if (deviceStateUpdate != null) {
                logger.debug("Update ESH State");
                if (!device.isRollershutter()) {
                    switch (deviceStateUpdate.getType()) {
                        case DeviceStateUpdate.UPDATE_BRIGHTNESS:
                            switch (currentChannel) {
                                case CHANNEL_COMBINED_2_STAGE_SWITCH:
                                case CHANNEL_COMBINED_3_STAGE_SWITCH:
                                case CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH:
                                case CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH:
                                    updateState(new ChannelUID(getThing().getUID(), currentChannel),
                                            new DecimalType(deviceStateUpdate.getValue()));
                                    break;
                                case CHANNEL_BRIGHTNESS:
                                case CHANNEL_GENERAL_DIMM:
                                    if (deviceStateUpdate.getValue() > 0) {
                                        updateState(new ChannelUID(getThing().getUID(), currentChannel),
                                                new PercentType(fromValueToPercent(deviceStateUpdate.getValue(),
                                                        device.getMaxOutputValue())));
                                    } else {
                                        updateState(new ChannelUID(getThing().getUID(), currentChannel), OnOffType.OFF);
                                    }
                                    break;

                            }
                            break;
                        case DeviceStateUpdate.UPDATE_ON_OFF:
                            if (deviceStateUpdate.getValue() > 0) {
                                updateState(new ChannelUID(getThing().getUID(), currentChannel), OnOffType.ON);
                            } else {
                                updateState(new ChannelUID(getThing().getUID(), currentChannel), OnOffType.OFF);
                            }
                            break;
                        case DeviceStateUpdate.UPDATE_ELECTRIC_METER:
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ELECTRIC_METER),
                                    new DecimalType(deviceStateUpdate.getValue() * 0.01));
                            break;
                        case DeviceStateUpdate.UPDATE_OUTPUT_CURRENT:
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_OUTPUT_CURRENT),
                                    new DecimalType(deviceStateUpdate.getValue()));
                            break;
                        case DeviceStateUpdate.UPDATE_ACTIVE_POWER:
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ACTIVE_POWER),
                                    new DecimalType(deviceStateUpdate.getValue()));
                            break;
                        default:
                            return;

                    }
                } else {
                    switch (deviceStateUpdate.getType()) {
                        case DeviceStateUpdate.UPDATE_SLATPOSITION:
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_SHADE), new PercentType(
                                    fromValueToPercent(deviceStateUpdate.getValue(), device.getMaxOutputValue())));
                            break;
                        case DeviceStateUpdate.UPDATE_OPEN_CLOSE:
                            if (deviceStateUpdate.getValue() > 0) {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_SHADE), UpDownType.UP);
                            } else {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_SHADE), UpDownType.DOWN);
                            }
                            break;
                        default:
                            return;
                    }
                }
            }
        }
    }

    private int fromValueToPercent(int value, int max) {
        if (value < 0 || value == 0) {
            return 0;
        }
        if (max < 0 || max == 0) {
            return 0;
        }
        return (int) (value * ((float) 100 / max));
    }

    @Override
    public synchronized void onDeviceRemoved(Device device) {
        this.device = null;
        updateStatus(ThingStatus.OFFLINE);
        logger.debug("Set status on {}", getThing().getStatus());
    }

    @Override
    public synchronized void onDeviceAdded(Device device) {
        if (device != null && device.isPresent()) {
            this.device = device;
            ThingStatusInfo statusInfo = this.dssBridgeHandler.getThing().getStatusInfo();
            updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());
            logger.debug("Set status on {}", getThing().getStatus());

            // load sensor priorities into the device and load sensor channels of the thing
            if (!device.isRollershutter()) {
                loadSensorChannels(getThing().getConfiguration());
            }

            // check and load output channel of the thing
            checkOutputChannel();

            // load first channel values
            onDeviceStateInitial(device);

            // load scene configurations persistently into the thing
            for (Short i : device.getSavedScenes()) {
                onSceneConfigAdded(i);
            }

            saveConfigSceneSpecificationIntoDevice(device);
            logger.debug("Load saved scene specification into device");
        } else {
            onDeviceRemoved(device);
        }

    }

    private void loadSensorChannels(Configuration config) {
        if (device != null && device.isPresent()) {
            // load sensor priorities into the device

            boolean configChanged = false;

            logger.debug("Add sensor priorities to device");
            String activePowerPrio = DigitalSTROMBindingConstants.REFRESH_PRIORITY_NEVER;
            if (config.get(DigitalSTROMBindingConstants.ACTIVE_POWER_REFRESH_PRIORITY) != null) {
                activePowerPrio = config.get(DigitalSTROMBindingConstants.ACTIVE_POWER_REFRESH_PRIORITY).toString();
            } else {
                config.put(DigitalSTROMBindingConstants.ACTIVE_POWER_REFRESH_PRIORITY,
                        DigitalSTROMBindingConstants.REFRESH_PRIORITY_NEVER);
                configChanged = true;
            }

            String outputCurrentPrio = DigitalSTROMBindingConstants.REFRESH_PRIORITY_NEVER;
            if (config.get(DigitalSTROMBindingConstants.OUTPUT_CURRENT_REFRESH_PRIORITY) != null) {
                outputCurrentPrio = config.get(DigitalSTROMBindingConstants.OUTPUT_CURRENT_REFRESH_PRIORITY).toString();
            } else {
                config.put(DigitalSTROMBindingConstants.OUTPUT_CURRENT_REFRESH_PRIORITY,
                        DigitalSTROMBindingConstants.REFRESH_PRIORITY_NEVER);
                configChanged = true;
            }

            String electricMeterPrio = DigitalSTROMBindingConstants.REFRESH_PRIORITY_NEVER;
            if (config.get(DigitalSTROMBindingConstants.ELECTRIC_METER_REFRESH_PRIORITY) != null) {
                electricMeterPrio = config.get(DigitalSTROMBindingConstants.ELECTRIC_METER_REFRESH_PRIORITY).toString();
            } else {
                config.put(DigitalSTROMBindingConstants.ELECTRIC_METER_REFRESH_PRIORITY,
                        DigitalSTROMBindingConstants.REFRESH_PRIORITY_NEVER);
                configChanged = true;
            }
            if (configChanged) {
                super.updateConfiguration(config);
                configChanged = false;
            }
            logger.debug(activePowerPrio + ", " + outputCurrentPrio + ", " + electricMeterPrio);

            device.setSensorDataRefreshPriority(activePowerPrio, electricMeterPrio, outputCurrentPrio);

            // check and load sensor channels of the thing
            checkSensorChannel(activePowerPrio, outputCurrentPrio, electricMeterPrio);
        }
    }

    private boolean isActivePowerChannelLoaded = true;
    private boolean isOutputCurrentChannelLoaded = true;
    private boolean isElectricMeterChannelLoaded = true;

    private void checkSensorChannel(String activePowerPrio, String outputCurrentPrio, String electricMeterPrio) {
        List<Channel> channelList = new LinkedList<Channel>(this.getThing().getChannels());

        boolean channelListChanged = false;

        if (!activePowerPrio.equals(REFRESH_PRIORITY_NEVER) && !isActivePowerChannelLoaded) {
            Channel channel = ChannelBuilder
                    .create(new ChannelUID(this.getThing().getUID(), CHANNEL_ACTIVE_POWER), "Number").build();
            channelList.add(channel);
            isActivePowerChannelLoaded = true;
            channelListChanged = true;
        }
        if (!outputCurrentPrio.equals(REFRESH_PRIORITY_NEVER) && !isOutputCurrentChannelLoaded) {
            Channel channel = ChannelBuilder
                    .create(new ChannelUID(this.getThing().getUID(), CHANNEL_OUTPUT_CURRENT), "Number").build();
            channelList.add(channel);
            isOutputCurrentChannelLoaded = true;
            channelListChanged = true;
        }
        if (!electricMeterPrio.equals(REFRESH_PRIORITY_NEVER) && !isElectricMeterChannelLoaded) {
            Channel channel = ChannelBuilder
                    .create(new ChannelUID(this.getThing().getUID(), CHANNEL_ELECTRIC_METER), "Number").build();
            channelList.add(channel);
            isElectricMeterChannelLoaded = true;
            channelListChanged = true;
        }

        // if sensor channels are loaded delete these channels
        if (!channelList.isEmpty()) {
            Iterator<Channel> channelInter = channelList.iterator();
            while (channelInter.hasNext()) {
                Channel channel = channelInter.next();
                switch (channel.getUID().getId()) {
                    case CHANNEL_ACTIVE_POWER:
                        if (activePowerPrio.equals(REFRESH_PRIORITY_NEVER)) {
                            logger.debug("!!!!!!!!!!!!!!");
                            channelInter.remove();
                            isActivePowerChannelLoaded = false;
                            channelListChanged = true;
                        }
                        break;
                    case CHANNEL_OUTPUT_CURRENT:
                        if (outputCurrentPrio.equals(REFRESH_PRIORITY_NEVER)) {
                            channelInter.remove();
                            isOutputCurrentChannelLoaded = false;
                            channelListChanged = true;
                        }
                        break;
                    case CHANNEL_ELECTRIC_METER:
                        if (electricMeterPrio.equals(REFRESH_PRIORITY_NEVER)) {
                            channelInter.remove();
                            isElectricMeterChannelLoaded = false;
                            channelListChanged = true;
                        }
                        break;
                }
            }
        }

        if (channelListChanged) {
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withChannels(channelList);
            updateThing(thingBuilder.build());
        }
    }

    private void checkOutputChannel() {
        if (device == null) {
            logger.debug("Can not load a channel without an device!");
            return;
        }
        // if the device have no output channel or it is disabled delete all output channels
        // TODO: or delete the thing?
        if (!device.isDeviceWithOutput()) {
            loadOutputChannel(null, null);
        }

        if (device.getFunctionalColorGroup().equals(FunctionalColorGroupEnum.BLACK)) {
            if (device.isDimmable() && (currentChannel == null || currentChannel != CHANNEL_GENERAL_DIMM)) {
                loadOutputChannel(CHANNEL_GENERAL_DIMM, "Dimmer");
            } else
                if (device.isRollershutter() && (currentChannel != null || currentChannel != CHANNEL_GENERAL_SHADE)) {
                loadOutputChannel(CHANNEL_GENERAL_SHADE, "Rollershutter");
            } else if (device.isSwitch() && (currentChannel != null || currentChannel != CHANNEL_GENERAL_SWITCH)) {
                loadOutputChannel(CHANNEL_GENERAL_SWITCH, "Switch");
            } else if (device.getOutputMode().equals(OutputModeEnum.COMBINED_2_STAGE_SWITCH)
                    && (currentChannel != null || currentChannel != CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH)) {
                loadOutputChannel(CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH, "String");
            } else if (device.getOutputMode().equals(OutputModeEnum.COMBINED_3_STAGE_SWITCH)
                    && (currentChannel != null || currentChannel != CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH)) {
                loadOutputChannel(CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH, "String");
            }
        } else {
            if (device.isDimmable() && (currentChannel == null || currentChannel != CHANNEL_BRIGHTNESS)) {
                loadOutputChannel(CHANNEL_BRIGHTNESS, "Dimmer");
            } else if (device.isRollershutter() && (currentChannel != null || currentChannel != CHANNEL_SHADE)) {
                loadOutputChannel(CHANNEL_SHADE, "Rollershutter");
            } else if (device.isSwitch() && (currentChannel != null || currentChannel != CHANNEL_LIGHT_SWITCH)) {
                loadOutputChannel(CHANNEL_LIGHT_SWITCH, "Switch");
            } else if (device.getOutputMode().equals(OutputModeEnum.COMBINED_2_STAGE_SWITCH)
                    && (currentChannel != null || currentChannel != CHANNEL_COMBINED_2_STAGE_SWITCH)) {
                loadOutputChannel(CHANNEL_COMBINED_2_STAGE_SWITCH, "String");
            } else if (device.getOutputMode().equals(OutputModeEnum.COMBINED_3_STAGE_SWITCH)
                    && (currentChannel != null || currentChannel != CHANNEL_COMBINED_3_STAGE_SWITCH)) {
                loadOutputChannel(CHANNEL_COMBINED_3_STAGE_SWITCH, "String");
            }
        }
    }

    private void loadOutputChannel(String channelId, String item) {
        currentChannel = channelId;

        List<Channel> channelList = new LinkedList<Channel>(this.getThing().getChannels());
        boolean channelIsAlreadyLoaded = false;
        boolean channelListChanged = false;

        if (!channelList.isEmpty()) {
            Iterator<Channel> channelInter = channelList.iterator();
            while (channelInter.hasNext()) {
                Channel eshChannel = channelInter.next();
                if (channelIsOutputChannel(eshChannel.getUID().getId())) {
                    if (!eshChannel.getUID().getId().equals(currentChannel)) {
                        channelInter.remove();
                        channelListChanged = true;
                    } else {
                        channelIsAlreadyLoaded = true;
                    }
                }
            }
        }

        if (!channelIsAlreadyLoaded && currentChannel != null) {
            Channel channel = ChannelBuilder.create(new ChannelUID(this.getThing().getUID(), channelId), item).build();
            channelList.add(channel);
            channelListChanged = true;
        }

        if (channelListChanged)

        {
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withChannels(channelList);
            updateThing(thingBuilder.build());
            logger.debug("load channel: {} with item: {}", channelId, item);
        }

    }

    private boolean channelIsOutputChannel(String id) {
        switch (id) {
            case CHANNEL_GENERAL_DIMM:
            case CHANNEL_GENERAL_SWITCH:
            case CHANNEL_GENERAL_SHADE:
            case CHANNEL_BRIGHTNESS:
            case CHANNEL_LIGHT_SWITCH:
            case CHANNEL_SHADE:
            case CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH:
            case CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH:
            case CHANNEL_COMBINED_2_STAGE_SWITCH:
            case CHANNEL_COMBINED_3_STAGE_SWITCH:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (device != null) {
            switch (channelUID.getId()) {
                case CHANNEL_GENERAL_DIMM:
                    if (device.isOn()) {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_GENERAL_DIMM), new PercentType(
                                fromValueToPercent(device.getOutputValue(), device.getMaxOutputValue())));
                        logger.debug("initial channel update brightness = "
                                + fromValueToPercent(device.getOutputValue(), device.getMaxOutputValue()));
                    } else {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_GENERAL_DIMM), new PercentType(0));
                    }
                    break;
                case CHANNEL_GENERAL_SWITCH:
                    if (device.isOn()) {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_GENERAL_SWITCH), OnOffType.ON);
                    } else {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_GENERAL_SWITCH), OnOffType.OFF);
                    }
                    break;
                case CHANNEL_GENERAL_SHADE:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_GENERAL_SHADE),
                            new PercentType(fromValueToPercent(device.getSlatPosition(), device.getMaxSlatPosition())));
                    break;
                case CHANNEL_BRIGHTNESS:
                    if (device.isOn()) {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_BRIGHTNESS), new PercentType(
                                fromValueToPercent(device.getOutputValue(), device.getMaxOutputValue())));
                        logger.debug("initial channel update brightness = "
                                + fromValueToPercent(device.getOutputValue(), device.getMaxOutputValue()));
                    } else {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_BRIGHTNESS), new PercentType(0));
                    }
                case CHANNEL_LIGHT_SWITCH:
                    if (device.isOn()) {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_LIGHT_SWITCH), OnOffType.ON);
                    } else {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_LIGHT_SWITCH), OnOffType.OFF);
                    }
                    break;
                case CHANNEL_SHADE:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_SHADE),
                            new PercentType(fromValueToPercent(device.getSlatPosition(), device.getMaxSlatPosition())));
                    break;
                case CHANNEL_ELECTRIC_METER:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_ELECTRIC_METER),
                            new DecimalType(device.getElectricMeter() * 0.01));
                    break;
                case CHANNEL_OUTPUT_CURRENT:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_OUTPUT_CURRENT),
                            new DecimalType(device.getOutputCurrent()));
                    break;
                case CHANNEL_ACTIVE_POWER:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_ACTIVE_POWER),
                            new DecimalType(device.getActivePower()));
                    break;
                case CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH),
                            new DecimalType(device.getOutputValue()));
                    break;
                case CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH),
                            new DecimalType(device.getMaxOutputValue()));
                    break;
                case CHANNEL_COMBINED_2_STAGE_SWITCH:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_COMBINED_2_STAGE_SWITCH),
                            new DecimalType(device.getOutputValue()));
                    break;
                case CHANNEL_COMBINED_3_STAGE_SWITCH:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_COMBINED_3_STAGE_SWITCH),
                            new DecimalType(device.getOutputValue()));
                    break;
                default:
                    return;
            }
        }
    }

    private void onDeviceStateInitial(Device device) {
        if (device != null) {
            if (currentChannel != null) {
                if (isLinked(currentChannel)) {
                    channelLinked(new ChannelUID(getThing().getUID(), currentChannel));
                }
            }
            if (!device.isRollershutter()) {
                if (isActivePowerChannelLoaded) {
                    if (getThing().getChannel(CHANNEL_ACTIVE_POWER) != null && isLinked(CHANNEL_ACTIVE_POWER)) {
                        channelLinked(new ChannelUID(getThing().getUID(), CHANNEL_ACTIVE_POWER));
                    }
                }
                if (isOutputCurrentChannelLoaded) {
                    if (getThing().getChannel(CHANNEL_OUTPUT_CURRENT) != null && isLinked(CHANNEL_OUTPUT_CURRENT)) {
                        channelLinked(new ChannelUID(getThing().getUID(), CHANNEL_OUTPUT_CURRENT));
                    }
                }
                if (isElectricMeterChannelLoaded) {
                    if (getThing().getChannel(CHANNEL_ELECTRIC_METER) != null && isLinked(CHANNEL_ELECTRIC_METER)) {
                        channelLinked(new ChannelUID(getThing().getUID(), CHANNEL_ELECTRIC_METER));
                    }
                }
            }
        }
    }

    @Override
    public synchronized void onSceneConfigAdded(short sceneId) {
        if (device != null) {
            String saveScene = "";
            DeviceSceneSpec sceneSpec = device.getSceneConfig(sceneId);
            if (sceneSpec != null) {
                saveScene = sceneSpec.toString();
            }

            int sceneValue = device.getSceneOutputValue(sceneId);
            if (sceneValue != -1) {
                saveScene = saveScene + ", sceneValue: " + sceneValue;
            }
            String key = DigitalSTROMBindingConstants.DEVICE_SCENE + sceneId;
            if (!saveScene.isEmpty()) {
                logger.debug("Save scene configuration: [{}] to thing with UID {}", saveScene,
                        this.getThing().getUID());
                if (this.getThing().getProperties().get(key) != null) {
                    this.getThing().setProperty(key, saveScene);
                } else {
                    Map<String, String> properties = editProperties();
                    properties.put(key, saveScene);
                    updateProperties(properties);
                }
            }
        }
    }

    @SuppressWarnings("null")
    private void saveConfigSceneSpecificationIntoDevice(Device device) {
        if (device != null) {
            /*
             * get persistently saved DeviceSceneSpec from Thing and save it in the Device, have to call after the
             * device is added (onDeviceAdded()) to ThingHandler
             */
            Map<String, String> propertries = this.getThing().getProperties();
            String sceneSave;
            for (short i = 0; i < 128; i++) {
                sceneSave = propertries.get(DigitalSTROMBindingConstants.DEVICE_SCENE + i);
                if (sceneSave != null && !sceneSave.isEmpty()) {
                    logger.debug("Find saved scene configuration for scene id " + i);
                    String[] sceneParm = sceneSave.replace(" ", "").split(",");
                    JSONDeviceSceneSpecImpl sceneSpecNew = null;
                    for (int j = 0; j < sceneParm.length; j++) {
                        System.out.println(sceneParm[j]);
                        String[] sceneParmSplit = sceneParm[j].split(":");
                        switch (sceneParmSplit[0]) {
                            case "Scene":
                                sceneSpecNew = new JSONDeviceSceneSpecImpl(sceneParmSplit[1]);
                                break;
                            case "dontcare":
                                sceneSpecNew.setDontcare(Boolean.parseBoolean(sceneParmSplit[1]));
                                break;
                            case "localPrio":
                                sceneSpecNew.setLocalPrio(Boolean.parseBoolean(sceneParmSplit[1]));
                                break;
                            case "specialMode":
                                sceneSpecNew.setSpecialMode(Boolean.parseBoolean(sceneParmSplit[1]));
                                break;
                            case "sceneValue":
                                logger.debug("Saved sceneValue {} for scene id {} into device with dsid {}",
                                        sceneParmSplit[1], i, device.getDSID().getValue());
                                ;
                                device.setSceneOutputValue(i, Integer.parseInt(sceneParmSplit[1]));
                                break;
                        }
                    }
                    if (sceneSpecNew != null) {
                        logger.debug("Saved sceneConfig: [{}] for scene id {} into device with dsid {}",
                                sceneSpecNew.toString(), i, device.getDSID().getValue());
                        ;
                        device.addSceneConfig(i, sceneSpecNew);
                    }
                }

            }
        }

    }

    @Override
    public void onDeviceConfigChanged(ChangeableDeviceConfigEnum whichConfig) {
        Configuration config = editConfiguration();
        switch (whichConfig) {
            case DEVICE_NAME:
                config.put(DEVICE_NAME, device.getName());
                break;
            case METER_DSID:
                config.put(DEVICE_METER_ID, device.getMeterDSID().getValue());
                break;
            case ZONE_ID:
                config.put(DEVICE_ZONE_ID, device.getZoneId());
                break;
            case GROUPS:
                config.put(DEVICE_GROUPS, device.getGroups().toString());
                break;
            case FUNCTIONAL_GROUP:
                config.put(DEVICE_FUNCTIONAL_COLOR_GROUP, device.getFunctionalColorGroup().toString());
                checkOutputChannel();
                break;
            case OUTPUT_MODE:
                config.put(DEVICE_OUTPUT_MODE, device.getOutputMode().toString());
                checkOutputChannel();
                break;
        }
        super.updateConfiguration(config);

    }

    @Override
    public String getID() {
        return this.dSID;
    }

}
