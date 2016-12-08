package org.eclipse.smarthome.binding.digitalstrom.handler;

import static org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants.BINDING_ID;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.constants.ControlModes;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.constants.ControlStates;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlStatus;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.TemperatureControlStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.TemperatureSensorTransreciver;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.FunctionalColorGroupEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.OutputModeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.providers.DsChannelTypeProvider;
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

public class ZoneTemperatureControlHandler extends BaseThingHandler implements TemperatureControlStatusListener {

    public static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets
            .newHashSet(DigitalSTROMBindingConstants.THING_TYPE_ZONE_TEMERATURE_CONTROL);

    private Logger logger = LoggerFactory.getLogger(ZoneTemperatureControlHandler.class);

    private TemperatureSensorTransreciver temperatureSensorTransreciver = null;

    private BridgeHandler dssBridgeHandler;
    private Integer zoneID = null;
    private String currentChannelID = null;
    private Float currentValue = 0f;
    // über channel config?
    private Float step = 1f;

    public ZoneTemperatureControlHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing DeviceHandler.");
        // if (StringUtils.isNotBlank((String) getConfig().get(DigitalSTROMBindingConstants.DEVICE_DSID))) {
        try {
            // TODO: String
            zoneID = ((Integer) getConfig().get(DigitalSTROMBindingConstants.ZONE_ID));
        } catch (ClassCastException e) {
            if (e.getMessage().startsWith("java.math.BigDecimal")) {
                zoneID = ((BigDecimal) getConfig().get(DigitalSTROMBindingConstants.ZONE_ID)).intValue();
            }
        }
        if (getBridge() != null) {
            bridgeStatusChanged(getBridge().getStatusInfo());
        } else {
            // Set status to OFFLINE if no bridge is available e.g. because the bridge has been removed and the
            // Thing was reinitialized.
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Bridge is missing!");
        }
        // } else {
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "zoneID is missing");
        // }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed... unregister DeviceStatusListener");
        if (zoneID != null) {
            if (dssBridgeHandler != null) {
                dssBridgeHandler.unregisterTemperatureControlStatusListener(this);
            }
            temperatureSensorTransreciver = null;
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            if (zoneID != null) {
                if (getDssBridgeHandler() != null && temperatureSensorTransreciver == null) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "waiting for listener registration");
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
        if (temperatureSensorTransreciver == null && zoneID != null) {
            // logger.warn(
            // "Device not known on StructureManager or DeviceStatusListener is not registerd. Cannot handle command.");
            return;
        }
        if (channelUID.getId().equals(currentChannelID)) {
            if (command instanceof PercentType || command instanceof DecimalType) {
                sendCommandAndUpdateChannel(((DecimalType) command).floatValue());
            } else if (command instanceof OnOffType) {
                if (OnOffType.ON.equals(command)) {
                    if (isTemperature()) {
                        sendCommandAndUpdateChannel(TemperatureSensorTransreciver.MAX_TEMP);
                    } else {
                        sendCommandAndUpdateChannel(TemperatureSensorTransreciver.MAX_CONTROLL_VALUE);
                    }
                } else {
                    if (isTemperature()) {
                        sendCommandAndUpdateChannel(0f);
                    } else {
                        sendCommandAndUpdateChannel(TemperatureSensorTransreciver.MIN_CONTROLL_VALUE);
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
            if (temperatureSensorTransreciver.pushTargetTemperature(zoneID, newValue)) {
                currentValue = newValue;
                updateState(currentChannelID, new DecimalType(newValue));
            }
        } else {
            if (temperatureSensorTransreciver.pushControlValue(zoneID, newValue)) {
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
                dssBridgeHandler.registerTemperatureControlStatusListener(this);
            } else {
                return null;
            }
        } else if (temperatureSensorTransreciver == null) {
            dssBridgeHandler.registerTemperatureControlStatusListener(this);
        }
        return dssBridgeHandler;
    }

    @Override
    public synchronized void configChanged(TemperatureControlStatus tempControlStatus) {
        ControlModes controlMode = ControlModes.getControlMode(tempControlStatus.getControlMode());
        ControlStates controlState = ControlStates.getControlState(tempControlStatus.getControlState());
        if (controlMode != null && controlState != null) {
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
        logger.debug("load channel: {} with item: Dimmer", currentChannelID);
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
    public void registerTemperatureSensorTransreciver(TemperatureSensorTransreciver temperatureSensorTransreciver) {
        updateStatus(ThingStatus.ONLINE);
        this.temperatureSensorTransreciver = temperatureSensorTransreciver;
    }

    @Override
    public Integer getID() {
        return zoneID;
    }

}
