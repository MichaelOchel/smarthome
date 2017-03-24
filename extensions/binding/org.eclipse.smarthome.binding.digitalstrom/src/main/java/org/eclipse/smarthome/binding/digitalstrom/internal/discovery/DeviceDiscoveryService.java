/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.discovery;

import static org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants.BINDING_ID;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.handler.BridgeHandler;
import org.eclipse.smarthome.binding.digitalstrom.handler.DeviceHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Circuit;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.GeneralDeviceInformations;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.OutputModeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.providers.DsDeviceThingTypeProvider;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link DeviceDiscoveryService} discovers all digitalSTROM-Devices, of one supported device-color-type. The
 * device-color-type has to be given to the {@link #DeviceDiscoveryService(BridgeHandler, ThingTypeUID)} as
 * {@link ThingTypeUID}. The supported {@link ThingTypeUID} can be found at {@link DeviceHandler#SUPPORTED_THING_TYPES}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class DeviceDiscoveryService extends AbstractDiscoveryService {

    private final static Logger logger = LoggerFactory.getLogger(DeviceDiscoveryService.class);

    private final BridgeHandler bridgeHandler;
    private final String DEVICE_TYPE;
    private final ThingUID BRIDGE_UID;

    /**
     * Creates a new {@link DeviceDiscoveryService} for the given supported {@link ThingTypeUID}.
     *
     * @param bridgeHandler (must not be null)
     * @param supportedThingType (must not be null)
     * @throws IllegalArgumentException see {@link AbstractDiscoveryService#AbstractDiscoveryService(int)}
     */
    public DeviceDiscoveryService(BridgeHandler bridgeHandler, ThingTypeUID supportedThingType)
            throws IllegalArgumentException {
        super(Sets.newHashSet(supportedThingType), 10, true);
        this.DEVICE_TYPE = supportedThingType.getId();
        this.bridgeHandler = bridgeHandler;
        BRIDGE_UID = bridgeHandler.getThing().getUID();
    }

    /**
     * Deactivates the {@link DeviceDiscoveryService} and removes the {@link DiscoveryResult}s.
     */
    @Override
    public void deactivate() {
        logger.debug("deactivate discovery service for device type " + DEVICE_TYPE + " thing types are: "
                + super.getSupportedThingTypes().toString());
        removeOlderResults(new Date().getTime());
    }

    @Override
    protected void startScan() {
        if (bridgeHandler != null) {
            if (!DsDeviceThingTypeProvider.SupportedThingTypes.circuit.toString().equals(DEVICE_TYPE)) {
                List<Device> devices = bridgeHandler.getDevices();
                if (devices != null) {
                    for (Device device : devices) {
                        onDeviceAddedInternal(device);
                    }
                }
            } else {
                List<Circuit> circuits = bridgeHandler.getCircuits();
                if (circuits != null) {
                    for (Circuit circuit : circuits) {
                        onDeviceAddedInternal(circuit);
                    }
                }
            }
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    private void onDeviceAddedInternal(GeneralDeviceInformations device) {
        boolean isSupported = false;
        if (device instanceof Device) {
            Device tempDevice = (Device) device;
            if ((tempDevice.isSensorDevice() && DEVICE_TYPE.equals(tempDevice.getHWinfo().replaceAll("-", "")))
                    || (DEVICE_TYPE.equals(tempDevice.getHWinfo().substring(0, 2))
                            && (tempDevice.isDeviceWithOutput() || tempDevice.isBinaryInputDevice())
                            && tempDevice.isPresent())) {
                isSupported = true;
            }
        } else if (device instanceof Circuit
                && DsDeviceThingTypeProvider.SupportedThingTypes.circuit.toString().equals(DEVICE_TYPE)) {
            isSupported = true;
        }
        if (isSupported) {
            ThingUID thingUID = getThingUID(device);
            if (thingUID != null) {
                Map<String, Object> properties = new HashMap<>(1);
                properties.put(DigitalSTROMBindingConstants.DEVICE_DSID, device.getDSID().getValue());
                String deviceName = null;
                if (StringUtils.isNotBlank(device.getName())) {
                    deviceName = device.getName();
                } else {
                    // if no name is set, the dSID will be used as name
                    deviceName = device.getDSID().getValue();
                }
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withBridge(BRIDGE_UID).withLabel(deviceName).build();

                thingDiscovered(discoveryResult);
            } else {
                if (device instanceof Device) {
                    logger.debug("Discovered unsupported device hardware type '{}' with uid {}",
                            ((Device) device).getHWinfo(), device.getDSUID());
                }
            }
        } else {
            if (device instanceof Device) {
                logger.debug(
                        "Discovered device with disabled or no output mode. Device was not added to inbox. "
                                + "Device information: hardware info: {}, dSUID: {}, device-name: {}, output value: {}",
                        ((Device) device).getHWinfo(), device.getDSUID(), device.getName(),
                        ((Device) device).getOutputMode());
            }
        }
    }

    private ThingUID getThingUID(GeneralDeviceInformations device) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingTypeUID thingTypeUID = null;
        if (device instanceof Device) {
            Device tempDevice = (Device) device;
            thingTypeUID = new ThingTypeUID(BINDING_ID, tempDevice.getHWinfo().substring(0, 2));
            if (tempDevice.isSensorDevice() && DEVICE_TYPE.equals(tempDevice.getHWinfo().replaceAll("-", ""))) {
                thingTypeUID = new ThingTypeUID(BINDING_ID, DEVICE_TYPE);
            }
        } else {
            thingTypeUID = new ThingTypeUID(BINDING_ID,
                    DsDeviceThingTypeProvider.SupportedThingTypes.circuit.toString());
        }
        if (getSupportedThingTypes().contains(thingTypeUID)) {
            String thingDeviceId = device.getDSID().toString();
            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, thingDeviceId);
            return thingUID;
        } else {
            return null;
        }
    }

    /**
     * Removes the {@link Thing} of the given {@link Device}.
     *
     * @param device (must not be null)
     */
    public void onDeviceRemoved(GeneralDeviceInformations device) {
        ThingUID thingUID = getThingUID(device);

        if (thingUID != null) {
            thingRemoved(thingUID);
        }
    }

    /**
     * Creates a {@link DiscoveryResult} for the given {@link Device}, if the {@link Device} is supported and the
     * {@link Device#getOutputMode()} is unequal {@link OutputModeEnum#DISABLED}.
     *
     * @param device (must not be null)
     */
    public void onDeviceAdded(GeneralDeviceInformations device) {
        if (super.isBackgroundDiscoveryEnabled()) {
            onDeviceAddedInternal(device);
        }
    }

    /**
     * Returns the ID of this {@link DeviceDiscoveryService}.
     *
     * @return id of the service
     */
    public String getID() {
        return DEVICE_TYPE;
    }
}
