/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.handler;

import static org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants.*;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceSceneSpec;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.ChangeableDeviceConfigEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.FunctionalColorGroupEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.OutputModeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.SensorEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.DeviceStateUpdateImpl;
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
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link DeviceHandler} is responsible for handling the configuration, load supported channels of a
 * digitalSTROM device and handling commands, which are sent to one of the channels. <br>
 * <br>
 * For that it uses the {@link BridgeHandler} and the {@link DeviceStateUpdate} mechanism of the {@link Device} to
 * execute the actual command and implements the {@link DeviceStatusListener} to get informed about changes from the
 * accompanying {@link Device}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class DeviceHandler extends BaseThingHandler implements DeviceStatusListener {

    private Logger logger = LoggerFactory.getLogger(DeviceHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(
            DigitalSTROMBindingConstants.THING_TYPE_GE_DEVICE, DigitalSTROMBindingConstants.THING_TYPE_SW_DEVICE,
            DigitalSTROMBindingConstants.THING_TYPE_GR_DEVICE);

    private String dSID = null;

    private Device device;

    private BridgeHandler dssBridgeHandler;

    private Command lastComand = null;

    private String currentChannel = null;

    private boolean isActivePowerChannelLoaded = false;
    private boolean isOutputCurrentChannelLoaded = false;
    private boolean isElectricMeterChannelLoaded = false;

    public DeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing DeviceHandler.");
        if (StringUtils.isNotBlank((String) getConfig().get(DigitalSTROMBindingConstants.DEVICE_DSID))) {
            dSID = getConfig().get(DigitalSTROMBindingConstants.DEVICE_DSID).toString();
            if (getBridge() != null) {
                bridgeStatusChanged(getBridge().getStatusInfo());
            } else {
                // Set status to OFFLINE if no bridge is available e.g. because the bridge has been removed and the
                // Thing was reinitialized.
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Bridge is missing!");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "dSID is missing");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed... unregister DeviceStatusListener");
        if (dSID != null) {
            if (dssBridgeHandler != null) {
                dssBridgeHandler.unregisterDeviceStatusListener(this);
            }
        }
        if (device != null) {
            device.setSensorDataRefreshPriority(REFRESH_PRIORITY_NEVER, REFRESH_PRIORITY_NEVER, REFRESH_PRIORITY_NEVER);
        }
        device = null;
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
        this.thing = thing;
        if (device == null) {
            initialize();
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParmeters) {
        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParmeter : configurationParmeters.entrySet()) {
            configuration.put(configurationParmeter.getKey(), configurationParmeter.getValue());
        }
        updateConfiguration(configuration);
        // check device info, load sensor priorities into the device and load sensor channels of the thing
        loadSensorChannels();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            if (dSID != null) {
                if (getDssBridgeHandler() != null && device == null) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "waiting for listener registration");
                    logger.debug("Set status to {}", getThing().getStatusInfo());
                    dssBridgeHandler.registerDeviceStatusListener(this);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No dSID is set!");
            }
        }
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.OFFLINE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.REMOVED)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Bridge has been removed.");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        BridgeHandler dssBridgeHandler = getDssBridgeHandler();
        if (dssBridgeHandler == null) {
            logger.warn("BridgeHandler not found. Cannot handle command without bridge.");
            return;
        }

        if (device == null) {
            logger.warn(
                    "Device not known on StructureManager or DeviceStatusListener is not registerd. Cannot handle command.");
            return;
        }

        if (command instanceof RefreshType) {
            try {
                SensorEnum sensorType = SensorEnum.valueOf(channelUID.getId());
                dssBridgeHandler.sendComandsToDSS(device, new DeviceStateUpdateImpl(sensorType, 1));
            } catch (IllegalArgumentException e) {
                dssBridgeHandler.sendComandsToDSS(device,
                        new DeviceStateUpdateImpl(DeviceStateUpdate.REFRESH_OUTPUT, 0));
            }
            /*
             * switch (channelUID.getId()) {
             * case CHANNEL_ID_ACTIVE_POWER:
             * dssBridgeHandler.sendComandsToDSS(device, new DeviceStateUpdateImpl(SensorEnum.ACTIVE_POWER, 1));
             * break;
             * case CHANNEL_ID_OUTPUT_CURRENT:
             * dssBridgeHandler.sendComandsToDSS(device, new DeviceStateUpdateImpl(SensorEnum.OUTPUT_CURRENT, 1));
             * break;
             * case CHANNEL_ID_ELECTRIC_METER:
             * dssBridgeHandler.sendComandsToDSS(device, new DeviceStateUpdateImpl(SensorEnum.ELECTRIC_METER, 1));
             * break;
             * default:
             * dssBridgeHandler.sendComandsToDSS(device,
             * new DeviceStateUpdateImpl(DeviceStateUpdate.REFRESH_OUTPUT, 0));
             * break;
             * }
             */
        } else if (!device.isShade()) {
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
                } else if (command instanceof StringType) {
                    device.setOutputValue(Short.parseShort(((StringType) command).toString()));
                }
            } else {
                logger.warn("Command sent to an unknown channel id: " + channelUID);
            }
        } else {
            if (channelUID.getId().equals(CHANNEL_ID_SHADE_ANGLE)) {
                if (command instanceof PercentType) {
                    device.setAnglePosition(
                            (short) fromPercentToValue(((PercentType) command).intValue(), device.getMaxSlatAngle()));
                } else if (command instanceof OnOffType) {
                    if (OnOffType.ON.equals(command)) {
                        device.setAnglePosition(device.getMaxSlatAngle());
                    } else {
                        device.setAnglePosition(device.getMinSlatAngle());
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    if (IncreaseDecreaseType.INCREASE.equals(command)) {
                        device.increaseSlatAngle();
                    } else {
                        device.decreaseSlatAngle();
                    }
                }
            } else if (channelUID.getId().equals(DigitalSTROMBindingConstants.CHANNEL_ID_SHADE)) {
                if (command instanceof PercentType) {
                    int percent = ((PercentType) command).intValue();
                    if (!device.getHWinfo().equals("GR-KL200")) {
                        percent = 100 - percent;
                    }
                    device.setSlatPosition(fromPercentToValue(percent, device.getMaxSlatPosition()));
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
                logger.warn("Command sent to an unknown channel id: " + channelUID);
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

    private synchronized BridgeHandler getDssBridgeHandler() {
        if (this.dssBridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.debug("Bride cannot be found");
                return null;
            }
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof BridgeHandler) {
                dssBridgeHandler = (BridgeHandler) handler;
                dssBridgeHandler.registerDeviceStatusListener(this);
            } else {
                return null;
            }
        }
        return dssBridgeHandler;
    }

    @Override
    public synchronized void onDeviceStateChanged(DeviceStateUpdate deviceStateUpdate) {
        if (device != null) {
            if (deviceStateUpdate != null) {
                logger.debug("Update ESH-State");
                if (deviceStateUpdate.isSensorUpdateType()) {
                    updateState(new ChannelUID(getThing().getUID(), deviceStateUpdate.getTypeAsSensorEnum().toString()),
                            new DecimalType(deviceStateUpdate.getValueAsFloat()));
                    return;
                    /*
                     * switch (deviceStateUpdate.getTypeAsSensorEnum()) {
                     * case ELECTRIC_METER:
                     * updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_ELECTRIC_METER),
                     * new DecimalType(deviceStateUpdate.getValueAsFloat()));
                     * return;
                     * case OUTPUT_CURRENT:
                     * updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_OUTPUT_CURRENT),
                     * new DecimalType(deviceStateUpdate.getValueAsFloat().intValue()));
                     * return;
                     * case ACTIVE_POWER:
                     * updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_ACTIVE_POWER),
                     * new DecimalType(deviceStateUpdate.getValueAsFloat().intValue()));
                     * return;
                     * default:
                     * return;
                     * }
                     */
                }
                if (!device.isShade()) {
                    switch (deviceStateUpdate.getType()) {
                        case DeviceStateUpdate.UPDATE_BRIGHTNESS_DECREASE:
                        case DeviceStateUpdate.UPDATE_BRIGHTNESS_INCREASE:
                        case DeviceStateUpdate.UPDATE_BRIGHTNESS:
                            switch (currentChannel) {
                                case CHANNEL_ID_COMBINED_2_STAGE_SWITCH:
                                case CHANNEL_ID_GENERAL_COMBINED_2_STAGE_SWITCH:
                                    updateState(new ChannelUID(getThing().getUID(), currentChannel),
                                            new StringType(convertStageValue((short) 2, device.getOutputValue())));
                                    break;
                                case CHANNEL_ID_COMBINED_3_STAGE_SWITCH:
                                case CHANNEL_ID_GENERAL_COMBINED_3_STAGE_SWITCH:
                                    updateState(new ChannelUID(getThing().getUID(), currentChannel),
                                            new StringType(convertStageValue((short) 3, device.getOutputValue())));
                                    break;
                                case CHANNEL_ID_BRIGHTNESS:
                                case CHANNEL_ID_GENERAL_DIMM:
                                    if (deviceStateUpdate.getValueAsInteger() > 0) {
                                        updateState(new ChannelUID(getThing().getUID(), currentChannel),
                                                new PercentType(
                                                        fromValueToPercent(deviceStateUpdate.getValueAsInteger(),
                                                                device.getMaxOutputValue())));
                                    } else {
                                        updateState(new ChannelUID(getThing().getUID(), currentChannel), OnOffType.OFF);
                                    }
                                    break;
                            }
                            break;
                        case DeviceStateUpdate.UPDATE_ON_OFF:
                            switch (currentChannel) {
                                case CHANNEL_ID_COMBINED_2_STAGE_SWITCH:
                                case CHANNEL_ID_GENERAL_COMBINED_2_STAGE_SWITCH:
                                    updateState(new ChannelUID(getThing().getUID(), currentChannel),
                                            new StringType(convertStageValue((short) 2, device.getOutputValue())));
                                    break;
                                case CHANNEL_ID_COMBINED_3_STAGE_SWITCH:
                                case CHANNEL_ID_GENERAL_COMBINED_3_STAGE_SWITCH:
                                    updateState(new ChannelUID(getThing().getUID(), currentChannel),
                                            new StringType(convertStageValue((short) 3, device.getOutputValue())));
                                    break;
                                default:
                                    if (deviceStateUpdate.getValueAsInteger() > 0) {
                                        updateState(new ChannelUID(getThing().getUID(), currentChannel), OnOffType.ON);
                                    } else {
                                        updateState(new ChannelUID(getThing().getUID(), currentChannel), OnOffType.OFF);
                                    }
                            }
                            break;
                        default:
                            return;
                    }
                } else {
                    int percent = 0;
                    switch (deviceStateUpdate.getType()) {
                        case DeviceStateUpdate.UPDATE_SLAT_DECREASE:
                        case DeviceStateUpdate.UPDATE_SLAT_INCREASE:
                        case DeviceStateUpdate.UPDATE_SLATPOSITION:
                            percent = fromValueToPercent(deviceStateUpdate.getValueAsInteger(),
                                    device.getMaxSlatPosition());
                            break;
                        case DeviceStateUpdate.UPDATE_OPEN_CLOSE:
                            if (deviceStateUpdate.getValueAsInteger() > 0) {
                                percent = 100;
                            }
                            break;
                        case DeviceStateUpdate.UPDATE_OPEN_CLOSE_ANGLE:
                            if (device.isBlind()) {
                                if (deviceStateUpdate.getValueAsInteger() > 0) {
                                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_SHADE_ANGLE),
                                            PercentType.HUNDRED);
                                } else {
                                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_SHADE_ANGLE),
                                            PercentType.ZERO);
                                }
                            }
                            return;
                        case DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE:
                        case DeviceStateUpdate.UPDATE_SLAT_ANGLE_INCREASE:
                        case DeviceStateUpdate.UPDATE_SLAT_ANGLE:
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_SHADE_ANGLE),
                                    new PercentType(fromValueToPercent(deviceStateUpdate.getValueAsInteger(),
                                            device.getMaxSlatAngle())));
                            return;
                        default:
                            return;
                    }
                    if (!device.getHWinfo().equals("GR-KL210")) {
                        percent = 100 - percent;
                    }
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_SHADE), new PercentType(percent));
                }
            }
        }
    }

    private int fromValueToPercent(int value, int max) {
        if (value <= 0 || max <= 0) {
            return 0;
        }
        return new BigDecimal(value * ((float) 100 / max)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    @Override
    public synchronized void onDeviceRemoved(Object device) {
        if (device instanceof Device) {
            this.device = null;
            if (this.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                if (device != null && !((Device) device).isPresent()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "Device is not present in the digitalSTROM-System.");
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "Device is not avaible in the digitalSTROM-System.");
                }

            }
            logger.debug("Set status to {}", getThing().getStatus());
        }
    }

    @Override
    public synchronized void onDeviceAdded(Object device) {
        if (device instanceof Device) {
            this.device = (Device) device;
            if (this.device.isPresent()) {
                logger.debug("properties: " + getThing().getProperties().toString());
                ThingStatusInfo statusInfo = this.dssBridgeHandler.getThing().getStatusInfo();
                updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());
                logger.debug("Set status to {}", getThing().getStatus());

                checkDeviceInfoProperties(this.device);
                // load sensor priorities into the device and load sensor channels of the thing
                if (!this.device.isShade()) {
                    loadSensorChannels();
                    // check and load output channel of the thing
                    checkOutputChannel();
                } else if (this.device.isBlind()) {
                    // load channel for set the angle of jalousie devices
                    loadOutputChannel(CHANNEL_TYPE_SHADE_ANGLE, "Dimmer");
                }

                // load first channel values
                onDeviceStateInitial(this.device);

                // load scene configurations persistently into the thing
                for (Short i : this.device.getSavedScenes()) {
                    onSceneConfigAdded(i);
                }
                logger.debug("Load saved scene specification into device");
                this.device.saveConfigSceneSpecificationIntoDevice(getThing().getProperties());

            } else {
                onDeviceRemoved(device);
            }
        }
    }

    /**
     * Updates device info properties.
     *
     * @param device (must not be null)
     */
    private void checkDeviceInfoProperties(Device device) {
        boolean propertiesChanged = false;
        Map<String, String> properties = editProperties();
        // check device info
        if (device.getName() != null) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_NAME, device.getName());
            propertiesChanged = true;
        }
        if (device.getDSUID() != null) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_UID, device.getDSUID());
            propertiesChanged = true;
        }
        if (device.getHWinfo() != null) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_HW_INFO, device.getHWinfo());
            propertiesChanged = true;
        }
        if (device.getZoneId() != -1) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_ZONE_ID, device.getZoneId() + "");
            propertiesChanged = true;
        }
        if (device.getGroups() != null) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_GROUPS, device.getGroups().toString());
            propertiesChanged = true;
        }
        if (device.getOutputMode() != null) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_OUTPUT_MODE, device.getOutputMode().toString());
            propertiesChanged = true;
        }
        if (device.getFunctionalColorGroup() != null) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_FUNCTIONAL_COLOR_GROUP,
                    device.getFunctionalColorGroup().toString());
            propertiesChanged = true;
        }
        if (device.getMeterDSID() != null) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_METER_ID, device.getMeterDSID().toString());
            propertiesChanged = true;
        }
        if (propertiesChanged) {
            super.updateProperties(properties);
            propertiesChanged = false;
        }
    }

    private void loadSensorChannels() {
        if (device != null && device.isPresent()) {
            // load sensor priorities into the device
            boolean configChanged = false;
            Configuration config = getThing().getConfiguration();
            logger.debug("Add sensor priorities to the device");

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

            device.setSensorDataRefreshPriority(activePowerPrio, electricMeterPrio, outputCurrentPrio);
            logger.debug("add sensor priorities: active power = " + activePowerPrio + ", output current = "
                    + outputCurrentPrio + ", electric meter = " + electricMeterPrio + " to device with id "
                    + device.getDSID());

            // check and load sensor channels of the thing
            checkSensorChannel(activePowerPrio, outputCurrentPrio, electricMeterPrio);
        }
    }

    private void checkSensorChannel(String activePowerPrio, String outputCurrentPrio, String electricMeterPrio) {
        List<Channel> channelList = new LinkedList<Channel>(this.getThing().getChannels());

        boolean channelListChanged = false;

        // if sensor channels with priority never are loaded delete these channels
        if (!channelList.isEmpty()) {
            Iterator<Channel> channelInter = channelList.iterator();
            while (channelInter.hasNext()) {
                Channel channel = channelInter.next();
                try {
                    SensorEnum sensorType = SensorEnum.valueOf(channel.getUID().getId());
                    switch (sensorType) {
                        case ACTIVE_POWER:
                            if (activePowerPrio.equals(REFRESH_PRIORITY_NEVER)) {
                                logger.debug("remove active power sensor channel");
                                channelInter.remove();
                                isActivePowerChannelLoaded = false;
                                channelListChanged = true;
                            } else {
                                isActivePowerChannelLoaded = true;
                            }
                            break;
                        case OUTPUT_CURRENT:
                            if (outputCurrentPrio.equals(REFRESH_PRIORITY_NEVER)) {
                                logger.debug("remove output current sensor channel");
                                channelInter.remove();
                                isOutputCurrentChannelLoaded = false;
                                channelListChanged = true;
                            } else {
                                isOutputCurrentChannelLoaded = true;
                            }
                            break;
                        case ELECTRIC_METER:
                            if (electricMeterPrio.equals(REFRESH_PRIORITY_NEVER)) {
                                logger.debug("remove eclectric meter sensor channel");
                                channelInter.remove();
                                isElectricMeterChannelLoaded = false;
                                channelListChanged = true;
                            } else {
                                isElectricMeterChannelLoaded = true;
                            }
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    // ignore
                }
                /*
                 * switch (channel.getUID().getId()) {
                 * case CHANNEL_ID_ACTIVE_POWER:
                 * if (activePowerPrio.equals(REFRESH_PRIORITY_NEVER)) {
                 * logger.debug("remove active power sensor channel");
                 * channelInter.remove();
                 * isActivePowerChannelLoaded = false;
                 * channelListChanged = true;
                 * } else {
                 * isActivePowerChannelLoaded = true;
                 * }
                 * break;
                 * case CHANNEL_ID_OUTPUT_CURRENT:
                 * if (outputCurrentPrio.equals(REFRESH_PRIORITY_NEVER)) {
                 * logger.debug("remove output current sensor channel");
                 * channelInter.remove();
                 * isOutputCurrentChannelLoaded = false;
                 * channelListChanged = true;
                 * } else {
                 * isOutputCurrentChannelLoaded = true;
                 * }
                 * break;
                 * case CHANNEL_ID_ELECTRIC_METER:
                 * if (electricMeterPrio.equals(REFRESH_PRIORITY_NEVER)) {
                 * logger.debug("remove eclectric meter sensor channel");
                 * channelInter.remove();
                 * isElectricMeterChannelLoaded = false;
                 * channelListChanged = true;
                 * } else {
                 * isElectricMeterChannelLoaded = true;
                 * }
                 * break;
                 * }
                 */
            }
        }
        // if sensor channels with priority unequal never are not loaded these channels will be loaded now
        if (!activePowerPrio.equals(REFRESH_PRIORITY_NEVER) && !isActivePowerChannelLoaded) {
            logger.debug("create active power sensor channel");
            /*
             * Channel channel = ChannelBuilder
             * .create(new ChannelUID(this.getThing().getUID(), SensorEnum.ACTIVE_POWER.toString()), "Number")
             * .withType(getSensorChannelType(SensorEnum.ACTIVE_POWER.toString())).build();
             * channelList.add(getSensorChannel(SensorEnum.ACTIVE_POWER));
             */
            channelList.add(getSensorChannel(SensorEnum.ACTIVE_POWER));
            isActivePowerChannelLoaded = true;
            channelListChanged = true;
        }
        if (!outputCurrentPrio.equals(REFRESH_PRIORITY_NEVER) && !isOutputCurrentChannelLoaded) {
            logger.debug("create output current sensor channel");
            /*
             * Channel channel = ChannelBuilder
             * .create(new ChannelUID(this.getThing().getUID(), SensorEnum.OUTPUT_CURRENT.toString()), "Number")
             * .withType(getSensorChannelType(SensorEnum.OUTPUT_CURRENT)).build();
             */
            channelList.add(getSensorChannel(SensorEnum.OUTPUT_CURRENT));
            isOutputCurrentChannelLoaded = true;
            channelListChanged = true;
        }
        if (!electricMeterPrio.equals(REFRESH_PRIORITY_NEVER) && !isElectricMeterChannelLoaded) {
            logger.debug("create eclectric meter sensor channel");
            /*
             * Channel channel = ChannelBuilder
             * .create(new ChannelUID(this.getThing().getUID(), SensorEnum.ELECTRIC_METER.toString()), "Number")
             * .withType(getSensorChannelType(SensorEnum.ELECTRIC_METER)).build();
             */
            channelList.add(getSensorChannel(SensorEnum.ELECTRIC_METER));
            isElectricMeterChannelLoaded = true;
            channelListChanged = true;
        }

        if (channelListChanged) {
            logger.debug("load new channel list");
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withChannels(channelList);
            updateThing(thingBuilder.build());
        }
    }

    private Channel getSensorChannel(SensorEnum sensorType) {
        return ChannelBuilder.create(new ChannelUID(this.getThing().getUID(), sensorType.toString()), "Number")
                .withType(new ChannelTypeUID(BINDING_ID, sensorType.toString())).build();
    }

    private void checkOutputChannel() {
        if (device == null) {
            logger.debug("Can not load a channel without a device!");
            return;
        }
        // if the device have no output channel or it is disabled all output channels will be deleted
        if (!device.isDeviceWithOutput()) {
            loadOutputChannel(null, null);
        }

        if (device.getFunctionalColorGroup().equals(FunctionalColorGroupEnum.YELLOW)) {
            if (device.isDimmable() && (currentChannel == null || currentChannel != CHANNEL_ID_BRIGHTNESS)) {
                loadOutputChannel(CHANNEL_TYPE_BRIGHTNESS, "Dimmer");
            } else if (device.isSwitch() && (currentChannel != null || currentChannel != CHANNEL_ID_LIGHT_SWITCH)) {
                loadOutputChannel(CHANNEL_TYPE_LIGHT_SWITCH, "Switch");
            } else if (device.getOutputMode().equals(OutputModeEnum.COMBINED_2_STAGE_SWITCH)
                    && (currentChannel != null || currentChannel != CHANNEL_ID_COMBINED_2_STAGE_SWITCH)) {
                loadOutputChannel(CHANNEL_TYPE_COMBINED_2_STAGE_SWITCH, "String");
            } else if (device.getOutputMode().equals(OutputModeEnum.COMBINED_3_STAGE_SWITCH)
                    && (currentChannel != null || currentChannel != CHANNEL_ID_COMBINED_3_STAGE_SWITCH)) {
                loadOutputChannel(CHANNEL_TYPE_COMBINED_3_STAGE_SWITCH, "String");
            }
        } else {
            if (device.isDimmable() && (currentChannel == null || currentChannel != CHANNEL_ID_GENERAL_DIMM)) {
                loadOutputChannel(CHANNEL_TYPE_GENERAL_DIMM, "Dimmer");
            } else if (device.isSwitch() && (currentChannel != null || currentChannel != CHANNEL_ID_GENERAL_SWITCH)) {
                loadOutputChannel(CHANNEL_TYPE_GENERAL_SWITCH, "Switch");
            } else if (device.getOutputMode().equals(OutputModeEnum.COMBINED_2_STAGE_SWITCH)
                    && (currentChannel != null || currentChannel != CHANNEL_ID_GENERAL_COMBINED_2_STAGE_SWITCH)) {
                loadOutputChannel(CHANNEL_TYPE_GENERAL_COMBINED_2_STAGE_SWITCH, "String");
            } else if (device.getOutputMode().equals(OutputModeEnum.COMBINED_3_STAGE_SWITCH)
                    && (currentChannel != null || currentChannel != CHANNEL_ID_GENERAL_COMBINED_3_STAGE_SWITCH)) {
                loadOutputChannel(CHANNEL_TYPE_GENERAL_COMBINED_3_STAGE_SWITCH, "String");
            } else {
                loadOutputChannel(null, null);
            }
        }
    }

    private void loadOutputChannel(ChannelTypeUID channelTypeUID, String acceptedItemType) {
        currentChannel = channelTypeUID.getId();

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
            Channel channel = ChannelBuilder
                    .create(new ChannelUID(this.getThing().getUID(), channelTypeUID.getId()), acceptedItemType)
                    .withType(channelTypeUID).build();
            channelList.add(channel);
            channelListChanged = true;
        }

        if (channelListChanged) {
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withChannels(channelList);
            updateThing(thingBuilder.build());
            logger.debug("load channel: {} with item: {}", channelTypeUID.getAsString(), acceptedItemType);
        }

    }

    private boolean channelIsOutputChannel(String id) {
        switch (id) {
            case CHANNEL_ID_GENERAL_DIMM:
            case CHANNEL_ID_GENERAL_SWITCH:
            case CHANNEL_ID_BRIGHTNESS:
            case CHANNEL_ID_LIGHT_SWITCH:
            case CHANNEL_ID_SHADE_ANGLE:
            case CHANNEL_ID_GENERAL_COMBINED_2_STAGE_SWITCH:
            case CHANNEL_ID_GENERAL_COMBINED_3_STAGE_SWITCH:
            case CHANNEL_ID_COMBINED_2_STAGE_SWITCH:
            case CHANNEL_ID_COMBINED_3_STAGE_SWITCH:
                return true;
            default:
                return false;
        }
    }

    private ChannelUID getSensorChannelUID(SensorEnum sensorType) {
        return new ChannelUID(getThing().getUID(), sensorType.toString());
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (device != null) {
            try {
                SensorEnum sensorType = SensorEnum.valueOf(channelUID.getId());
                Float val = device.getFloatSensorValue(sensorType);
                if (val != null) {
                    updateState(getSensorChannelUID(sensorType), new DecimalType(val));
                }
            } catch (IllegalArgumentException e) {
                switch (channelUID.getId()) {
                    case CHANNEL_ID_GENERAL_DIMM:
                        if (device.isOn()) {
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_GENERAL_DIMM), new PercentType(
                                    fromValueToPercent(device.getOutputValue(), device.getMaxOutputValue())));
                        } else {
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_GENERAL_DIMM),
                                    new PercentType(0));
                        }
                        break;
                    case CHANNEL_ID_GENERAL_SWITCH:
                        if (device.isOn()) {
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_GENERAL_SWITCH), OnOffType.ON);
                        } else {
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_GENERAL_SWITCH), OnOffType.OFF);
                        }
                        break;
                    case CHANNEL_ID_BRIGHTNESS:
                        if (device.isOn()) {
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_BRIGHTNESS), new PercentType(
                                    fromValueToPercent(device.getOutputValue(), device.getMaxOutputValue())));
                        } else {
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_BRIGHTNESS), new PercentType(0));
                        }
                    case CHANNEL_ID_LIGHT_SWITCH:
                        if (device.isOn()) {
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_LIGHT_SWITCH), OnOffType.ON);
                        } else {
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_LIGHT_SWITCH), OnOffType.OFF);
                        }
                        break;
                    case CHANNEL_ID_SHADE:
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_SHADE), new PercentType(
                                fromValueToPercent(device.getSlatPosition(), device.getMaxSlatPosition())));
                        break;
                    case CHANNEL_ID_SHADE_ANGLE:
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_SHADE_ANGLE), new PercentType(
                                fromValueToPercent(device.getAnglePosition(), device.getMaxSlatAngle())));
                        break;
                    /*
                     * case CHANNEL_ID_ELECTRIC_METER:
                     * Float val = device.getFloatSensorValue(SensorEnum.ELECTRIC_METER);
                     * if (val != null) {
                     * updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_ELECTRIC_METER),
                     * new DecimalType(val));
                     * }
                     * break;
                     * case CHANNEL_ID_OUTPUT_CURRENT:
                     * val = device.getFloatSensorValue(SensorEnum.OUTPUT_CURRENT);
                     * if (val != null) {
                     * updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_OUTPUT_CURRENT),
                     * new DecimalType(val.intValue()));
                     * }
                     * break;
                     * case CHANNEL_ID_ACTIVE_POWER:
                     * val = device.getFloatSensorValue(SensorEnum.ACTIVE_POWER);
                     * if (val != null) {
                     * updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_ACTIVE_POWER),
                     * new DecimalType(val.intValue()));
                     * }
                     * break;
                     */
                    case CHANNEL_ID_GENERAL_COMBINED_2_STAGE_SWITCH:
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_GENERAL_COMBINED_2_STAGE_SWITCH),
                                new StringType(convertStageValue((short) 2, device.getOutputValue())));
                        break;
                    case CHANNEL_ID_GENERAL_COMBINED_3_STAGE_SWITCH:
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_GENERAL_COMBINED_3_STAGE_SWITCH),
                                new StringType(convertStageValue((short) 3, device.getOutputValue())));
                        break;
                    case CHANNEL_ID_COMBINED_2_STAGE_SWITCH:
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_COMBINED_2_STAGE_SWITCH),
                                new StringType(convertStageValue((short) 2, device.getOutputValue())));
                        break;
                    case CHANNEL_ID_COMBINED_3_STAGE_SWITCH:
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_COMBINED_3_STAGE_SWITCH),
                                new StringType(convertStageValue((short) 3, device.getOutputValue())));
                        break;
                    default:
                        return;
                }
            }
        }
    }

    private String convertStageValue(short stage, short value) {
        switch (stage) {
            case 2:
                if (value < 85) {
                    return "0";
                } else if (value >= 85 && value < 170) {
                    return "90";
                } else if (value >= 170 && value <= 255) {
                    return "200";
                }
            case 3:
                if (value < 64) {
                    return "0";
                } else if (value >= 64 && value < 128) {
                    return "90";
                } else if (value >= 128 && value < 192) {
                    return "130";
                } else if (value >= 192 && value <= 255) {
                    return "200";
                }
        }
        return null;
    }

    private void onDeviceStateInitial(Device device) {
        if (device != null) {
            if (currentChannel != null) {
                if (isLinked(currentChannel)) {
                    channelLinked(new ChannelUID(getThing().getUID(), currentChannel));
                }
            }
            if (!device.isShade()) {
                if (isActivePowerChannelLoaded) {
                    // if (getThing().getChannel(CHANNEL_ID_ACTIVE_POWER) != null && isLinked(CHANNEL_ID_ACTIVE_POWER))
                    // {
                    // channelLinked(new ChannelUID(getThing().getUID(), CHANNEL_ID_ACTIVE_POWER));
                    // }
                    if (getThing().getChannel(SensorEnum.ACTIVE_POWER.toString()) != null
                            && isLinked(SensorEnum.ACTIVE_POWER.toString())) {
                        channelLinked(getSensorChannelUID(SensorEnum.ACTIVE_POWER));
                    }
                }
                if (isOutputCurrentChannelLoaded) {
                    // if (getThing().getChannel(CHANNEL_ID_OUTPUT_CURRENT) != null
                    // && isLinked(CHANNEL_ID_OUTPUT_CURRENT)) {
                    // channelLinked(new ChannelUID(getThing().getUID(), CHANNEL_ID_OUTPUT_CURRENT));
                    // }
                    if (getThing().getChannel(SensorEnum.OUTPUT_CURRENT.toString()) != null
                            && isLinked(SensorEnum.OUTPUT_CURRENT.toString())) {
                        channelLinked(getSensorChannelUID(SensorEnum.OUTPUT_CURRENT));
                    }
                }
                if (isElectricMeterChannelLoaded) {
                    // if (getThing().getChannel(CHANNEL_ID_ELECTRIC_METER) != null
                    // && isLinked(CHANNEL_ID_ELECTRIC_METER)) {
                    // channelLinked(new ChannelUID(getThing().getUID(), CHANNEL_ID_ELECTRIC_METER));
                    // }
                    if (getThing().getChannel(SensorEnum.ELECTRIC_METER.toString()) != null
                            && isLinked(SensorEnum.ELECTRIC_METER.toString())) {
                        channelLinked(getSensorChannelUID(SensorEnum.ELECTRIC_METER));
                    }
                }
            } else {
                if (isLinked(CHANNEL_ID_SHADE)) {
                    channelLinked(new ChannelUID(getThing().getUID(), CHANNEL_ID_SHADE));
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

            Integer[] sceneValue = device.getSceneOutputValue(sceneId);
            if (sceneValue[0] != -1) {
                saveScene = saveScene + ", sceneValue: " + sceneValue[0];
            }
            if (sceneValue[1] != -1) {
                saveScene = saveScene + ", sceneAngle: " + sceneValue[1];
            }
            String key = DigitalSTROMBindingConstants.DEVICE_SCENE + sceneId;
            if (!saveScene.isEmpty()) {
                logger.debug("Save scene configuration: [{}] to thing with UID {}", saveScene, getThing().getUID());
                super.updateProperty(key, saveScene);
                // persist the new property
                super.updateThing(getThing());
            }
        }

    }

    @Override
    public void onDeviceConfigChanged(ChangeableDeviceConfigEnum whichConfig) {
        switch (whichConfig) {
            case DEVICE_NAME:
                super.updateProperty(DEVICE_NAME, device.getName());
                break;
            case METER_DSID:
                super.updateProperty(DEVICE_METER_ID, device.getMeterDSID().getValue());
                break;
            case ZONE_ID:
                super.updateProperty(DEVICE_ZONE_ID, device.getZoneId() + "");
                break;
            case GROUPS:
                super.updateProperty(DEVICE_GROUPS, device.getGroups().toString());
                break;
            case FUNCTIONAL_GROUP:
                super.updateProperty(DEVICE_FUNCTIONAL_COLOR_GROUP, device.getFunctionalColorGroup().toString());
                checkOutputChannel();
                break;
            case OUTPUT_MODE:
                super.updateProperty(DEVICE_OUTPUT_MODE, device.getOutputMode().toString());
                checkOutputChannel();
                break;
        }
    }

    @Override
    public String getDeviceStatusListenerID() {
        return this.dSID;
    }
}