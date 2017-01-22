/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.handler;

import static org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants.BINDING_ID;

import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.TemperatureControlSensorTransmitter;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.constants.ControlModes;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.constants.ControlStates;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlStatus;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.TemperatureControlStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.StructureManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.FunctionalColorGroupEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.OutputModeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.providers.DsChannelTypeProvider;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * The {@link ZoneTemperatureControlHandler} is responsible for handling the configuration, to load the supported
 * channel of a
 * digitalSTROM zone, which has a temperature control configured, and handling commands, which are sent to the channel.
 * <br>
 * <br>
 * For that it uses the {@link BridgeHandler} to register itself as {@link TemperatureControlStatusListener} at the
 * {@link TemperatureControlManager} to get informed by status changes. Through the registration as
 * {@link TemperatureControlStatusListener} a {@link TemperatureControlSensorTransmitter} will be registered to this
 * {@link ZoneTemperatureControlHandler}, which is needed to set the temperature or the control value of a zone.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class ZoneTemperatureControlHandler extends BaseThingHandler implements TemperatureControlStatusListener {

    /**
     * Contains all supported thing types of this handler
     */
    public static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets
            .newHashSet(DigitalSTROMBindingConstants.THING_TYPE_ZONE_TEMERATURE_CONTROL);

    private Logger logger = LoggerFactory.getLogger(ZoneTemperatureControlHandler.class);

    private TemperatureControlSensorTransmitter temperatureSensorTransmitter = null;

    private BridgeHandler dssBridgeHandler;
    private Integer zoneID = null;
    private String currentChannelID = null;
    private Float currentValue = 0f;
    private Float step = 1f;

    /**
     * Creates a new {@link ZoneTemperatureControlHandler}.
     *
     * @param thing
     */
    public ZoneTemperatureControlHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing DeviceHandler.");
        if (getConfig().get(DigitalSTROMBindingConstants.ZONE_ID) != null) {
            if (getBridge() != null) {
                bridgeStatusChanged(getBridge().getStatusInfo());
            } else {
                // Set status to OFFLINE if no bridge is available e.g. because the bridge has been removed and the
                // Thing was reinitialized.
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Bridge is missing!");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "zoneID is missing");
        }
    }

    public static int getZoneID(Configuration config, BridgeHandler bridge) {
        if (config == null || config.get(DigitalSTROMBindingConstants.ZONE_ID) == null) {
            return -2;
        }
        if (bridge == null) {
            return -3;
        }
        String configZoneID = config.get(DigitalSTROMBindingConstants.ZONE_ID).toString();
        int zoneID;
        StructureManager strucMan = bridge.getStructureManager();
        if (strucMan != null) {
            try {
                zoneID = Integer.parseInt(configZoneID);// SceneHandler.fixNumber(configZoneID));
                if (!strucMan.checkZoneID(zoneID)) {
                    zoneID = -1;
                }
            } catch (NumberFormatException e) {
                zoneID = strucMan.getZoneId(configZoneID);
            }
            return zoneID;
        }
        return -1;
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed... unregister DeviceStatusListener");
        if (zoneID != null) {
            if (dssBridgeHandler != null) {
                dssBridgeHandler.unregisterTemperatureControlStatusListener(this);
            }
            temperatureSensorTransmitter = null;
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            int tempZoneID = getZoneID(getConfig(), getDssBridgeHandler());
            if (tempZoneID == -1) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Configured zone '" + getConfig().get(DigitalSTROMBindingConstants.ZONE_ID)
                                + "' does not exist, please check the configuration.");
            } else {
                this.zoneID = tempZoneID;
            }
            if (zoneID != null) {
                if (getDssBridgeHandler() != null && temperatureSensorTransmitter == null) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "waiting for listener registration");
                } else {
                    updateStatus(ThingStatus.ONLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No zoneID is set!");
            }
        }
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.OFFLINE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.REMOVED)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Bridge has been removed.");
        }
        logger.debug("Set status to {}", getThing().getStatusInfo());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        BridgeHandler dssBridgeHandler = getDssBridgeHandler();
        if (dssBridgeHandler == null) {
            logger.warn("BridgeHandler not found. Cannot handle command without bridge.");
            return;
        }
        if (temperatureSensorTransmitter == null && zoneID != null) {
            logger.warn(
                    "Device not known on TemperationControlManager or temperatureSensorTransreciver is not registerd. Cannot handle command.");
            return;
        }
        if (channelUID.getId().equals(currentChannelID)) {
            if (command instanceof PercentType || command instanceof DecimalType) {
                sendCommandAndUpdateChannel(((DecimalType) command).floatValue());
            } else if (command instanceof OnOffType) {
                if (OnOffType.ON.equals(command)) {
                    if (isTemperature()) {
                        sendCommandAndUpdateChannel(TemperatureControlSensorTransmitter.MAX_TEMP);
                    } else {
                        sendCommandAndUpdateChannel(TemperatureControlSensorTransmitter.MAX_CONTROLL_VALUE);
                    }
                } else {
                    if (isTemperature()) {
                        sendCommandAndUpdateChannel(0f);
                    } else {
                        sendCommandAndUpdateChannel(TemperatureControlSensorTransmitter.MIN_CONTROLL_VALUE);
                    }
                }
            } else if (command instanceof IncreaseDecreaseType) {
                if (IncreaseDecreaseType.INCREASE.equals(command)) {
                    sendCommandAndUpdateChannel(currentValue + step);
                } else {
                    sendCommandAndUpdateChannel(currentValue - step);
                }
            }
        } else {
            logger.warn("Command sent to an unknown channel id: " + channelUID);
        }

    }

    private boolean isTemperature() {
        return currentChannelID.contains(DsChannelTypeProvider.TEMPERATURE_CONTROLLED);
    }

    private void sendCommandAndUpdateChannel(Float newValue) {
        if (isTemperature()) {
            if (temperatureSensorTransmitter.pushTargetTemperature(zoneID, newValue)) {
                currentValue = newValue;
                updateState(currentChannelID, new DecimalType(newValue));
            }
        } else {
            if (temperatureSensorTransmitter.pushControlValue(zoneID, newValue)) {
                currentValue = newValue;
                updateState(currentChannelID, new PercentType(newValue.intValue()));
            }
        }
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
            } else {
                return null;
            }
        }
        if (temperatureSensorTransmitter == null && zoneID != null) {
            dssBridgeHandler.registerTemperatureControlStatusListener(this);
        }
        return dssBridgeHandler;
    }

    @Override
    public synchronized void configChanged(TemperatureControlStatus tempControlStatus) {
        ControlModes controlMode = ControlModes.getControlMode(tempControlStatus.getControlMode());
        ControlStates controlState = ControlStates.getControlState(tempControlStatus.getControlState());
        if (controlMode != null && controlState != null) {
            logger.debug("config changed: " + tempControlStatus.toString());
            if (controlMode.equals(ControlModes.PID_CONTROL)
                    && (currentChannelID == null
                            || !currentChannelID.contains(DsChannelTypeProvider.TEMPERATURE_CONTROLLED))
                    && !controlState.equals(ControlStates.EMERGENCY)) {
                currentChannelID = DsChannelTypeProvider.getOutputChannelTypeID(FunctionalColorGroupEnum.BLUE,
                        OutputModeEnum.TEMPRETURE_PWM);
                loadChannel();
                currentValue = tempControlStatus.getNominalValue();
                updateState(currentChannelID, new DecimalType(currentValue.doubleValue()));
            } else {
                currentChannelID = DsChannelTypeProvider.getOutputChannelTypeID(FunctionalColorGroupEnum.BLUE,
                        OutputModeEnum.HEATING_PWM);
                loadChannel();
                currentValue = tempControlStatus.getControlValue();
                updateState(currentChannelID, new PercentType(fixPercent(currentValue.intValue())));
                if (controlState.equals(ControlStates.EMERGENCY)) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "The communication with temperation sensor fails. Temperature control state emergency (temperature control though the control value) is active.");
                }
            }
            // TODO: in case control-mode zone-follower it is maybe useful to add the followed zone-id, but this info is
            // not in the control-status
            Map<String, String> properties = editProperties();
            properties.put("controlDSUID", tempControlStatus.getControlDSUID());
            properties.put("controlMode", controlMode.getKey());
            properties.put("controlState", controlState.getKey());
            updateProperties(properties);
        }
    }

    private synchronized void loadChannel() {
        Channel channel = ChannelBuilder
                .create(new ChannelUID(this.getThing().getUID(), currentChannelID),
                        DsChannelTypeProvider.getItemType(currentChannelID))
                .withType(new ChannelTypeUID(BINDING_ID, currentChannelID)).build();
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(Lists.newArrayList(channel));
        updateThing(thingBuilder.build());
        logger.debug("load channel: {} with item: {]", currentChannelID,
                DsChannelTypeProvider.getItemType(currentChannelID));
    }

    @Override
    public synchronized void onTargetTemperatureChanged(Float newValue) {
        if (isTemperature()) {
            updateState(currentChannelID, new DecimalType(newValue));
        }
    }

    @Override
    public synchronized void onControlValueChanged(Integer newValue) {
        if (!isTemperature()) {
            updateState(currentChannelID, new PercentType(fixPercent(newValue)));
        }
    }

    private int fixPercent(int value) {
        return value < 0 ? 0 : value > 100 ? 100 : value;
    }

    @Override
    public void onTemperatureControlIsNotConfigured() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "digitalSTROM temperature control is for this zone not configured in.");
    }

    @Override
    public void registerTemperatureSensorTransmitter(
            TemperatureControlSensorTransmitter temperatureSensorTransreciver) {
        updateStatus(ThingStatus.ONLINE);
        this.temperatureSensorTransmitter = temperatureSensorTransreciver;
    }

    @Override
    public Integer getTemperationControlStatusListenrID() {
        return zoneID;
    }

}
