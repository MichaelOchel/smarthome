/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.handler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Circuit;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.CachedMeteringValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.ChangeableDeviceConfigEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringUnitsEnum;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CircuitHandler extends BaseThingHandler implements DeviceStatusListener {

    private Logger logger = LoggerFactory.getLogger(CircuitHandler.class);

    // will be filled by DsDeviceThingTypeProvider
    public static Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>();

    private String dSID = null;
    private Circuit circuit;

    private BridgeHandler dssBridgeHandler;

    public CircuitHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing CircuitHandler.");
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
        circuit = null;
    }

    @Override
    public void handleRemoval() {
        if (getDssBridgeHandler() != null) {
            this.dssBridgeHandler.childThingRemoved(dSID);
        }
        updateStatus(ThingStatus.REMOVED);
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
    public void thingUpdated(Thing thing) {
        this.thing = thing;
        if (circuit == null) {
            initialize();
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            if (dSID != null) {
                if (getDssBridgeHandler() != null && circuit == null) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "waiting for listener registration");
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
        logger.debug("Set status to {}", getThing().getStatusInfo());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeviceStateChanged(DeviceStateUpdate deviceStateUpdate) {
        if (deviceStateUpdate != null && DeviceStateUpdate.UPDATE_CIRCUIT_METER.equals(deviceStateUpdate.getType())) {
            if (deviceStateUpdate.getValue() instanceof CachedMeteringValue) {
                CachedMeteringValue cachedVal = (CachedMeteringValue) deviceStateUpdate.getValue();
                logger.debug(cachedVal.getMeteringType() + " " + cachedVal.getMeteringUnit() + " = "
                        + (cachedVal.getMeteringType().equals(MeteringTypeEnum.energy)
                                && (cachedVal.getMeteringUnit() == null
                                        || cachedVal.getMeteringUnit().equals(MeteringUnitsEnum.Wh))));
                if (cachedVal.getMeteringType().equals(MeteringTypeEnum.energy) && (cachedVal.getMeteringUnit() == null
                        || cachedVal.getMeteringUnit().equals(MeteringUnitsEnum.Wh))) {
                    updateState(getChannelID(cachedVal), new DecimalType(cachedVal.getValue() * 0.001));
                } else {
                    updateState(getChannelID(cachedVal), new DecimalType(cachedVal.getValue()));
                }
            }
        }

    }

    @Override
    public void onDeviceRemoved(Object device) {
        if (device instanceof Circuit) {
            this.circuit = null;
            if (this.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                if (device != null && !((Device) circuit).isPresent()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "Circuit is not present in the digitalSTROM-System.");
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "Circuit is not avaible in the digitalSTROM-System.");
                }

            }
            logger.debug("Set status to {}", getThing().getStatus());
        }
    }

    @Override
    public void onDeviceAdded(Object device) {
        if (device instanceof Circuit) {
            this.circuit = (Circuit) device;
            if (this.circuit.isPresent()) {
                ThingStatusInfo statusInfo = this.dssBridgeHandler.getThing().getStatusInfo();
                updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());
                logger.debug("Set status to {}", getThing().getStatus());

                checkDeviceInfoProperties(this.circuit);

                // load first channel values
                onDeviceStateInitial(this.circuit);

            } else {
                onDeviceRemoved(device);
            }
        }
    }

    private void checkDeviceInfoProperties(Circuit device) {
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
        // TODO:füllen
        if (propertiesChanged) {
            super.updateProperties(properties);
            propertiesChanged = false;
        }
    }

    private void onDeviceStateInitial(Circuit circuit) {
        if (circuit != null) {
            for (CachedMeteringValue cachedMeterValue : circuit.getAllCachedMeteringValues()) {
                if (cachedMeterValue != null) {
                    String channelID = getChannelID(cachedMeterValue);
                    if (isLinked(channelID)) {
                        channelLinked(new ChannelUID(getThing().getUID(), channelID));
                    }
                }
            }
        }
    }

    private String getChannelID(CachedMeteringValue cachedMeterValue) {
        String channelID = cachedMeterValue.getMeteringType().toString();
        if (cachedMeterValue.getMeteringUnit() != null) {
            channelID = channelID + "_" + cachedMeterValue.getMeteringUnit().toString();
        } else {
            channelID = channelID + "_" + MeteringUnitsEnum.Wh.toString();
        }
        return channelID;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (circuit != null) {
            try {
                String[] meteringChannelSplit = channelUID.getId().split("_");
                if (meteringChannelSplit.length > 1) {
                    MeteringTypeEnum meteringType = MeteringTypeEnum.valueOf(meteringChannelSplit[0]);
                    MeteringUnitsEnum unitType = MeteringUnitsEnum.valueOf(meteringChannelSplit[1]);
                    double val = circuit.getMeteringValue(meteringType, unitType);
                    if (val > -1) {
                        if (meteringType.equals(MeteringTypeEnum.energy)
                                && (unitType == null || unitType.equals(MeteringUnitsEnum.Wh))) {
                            updateState(channelUID, new DecimalType(val * 0.001));
                        } else {
                            updateState(channelUID, new DecimalType(val));
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                // ignore
            }

        }
    }

    @Override
    public void onDeviceConfigChanged(ChangeableDeviceConfigEnum whatConfig) {
        // nothing to do, will be registered again
    }

    @Override
    public void onSceneConfigAdded(short sceneID) {
        // nothing to do
    }

    @Override
    public String getDeviceStatusListenerID() {
        return this.dSID;
    }

}
