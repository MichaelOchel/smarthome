/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMDeviceStatusManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.ChangeableDeviceConfigEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceStateUpdate;

/**
 * The {@link DeviceStatusListener} is notified when a {@link Device} status has changed, a scene configuration is added
 * to a {@link Device} or a device has been removed or added.
 *
 * <p>
 * By implementation with the id {@link #DEVICE_DESCOVERY} this listener is a device discovery and will be informed by
 * the {@link DigitalSTROMDeviceStatusManager} if a new device would be found or is removed from the
 * digitalSTROM-System if it is registered on the{@link DigitalSTROMDeviceStatusManager}.
 * </p>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public interface DeviceStatusListener {

    /**
     * Id of the device discovery listener.
     */
    public final static String DEVICE_DESCOVERY = "DeviceDiscovey";

    /**
     * This method is called whenever the state of the given device has changed and passes the new device state as an
     * {@link DeviceStateUpdate} object.
     *
     * @param deviceStateUpdate
     *
     */
    public void onDeviceStateChanged(DeviceStateUpdate deviceStateUpdate);

    /**
     * This method is called whenever a device is removed.
     *
     * @param device
     *
     */
    public void onDeviceRemoved(Device device);

    /**
     * This method is called whenever a device is added.
     *
     * @param device
     *
     */
    public void onDeviceAdded(Device device);

    /**
     * This method is called whenever a configuration of an {@link Device} has changed.
     * For which configuration are able please have a look at {@link ChangeableDeviceConfigEnum}.
     *
     * @param whichConfig
     *
     */
    public void onDeviceConfigChanged(ChangeableDeviceConfigEnum whichConfig);

    /**
     * This method is called whenever a scene configuration is added to a device
     *
     * @param sceneId
     */
    public void onSceneConfigAdded(short sceneId);

    /**
     * Return the id of this {@link DeviceStatusListener}.
     *
     * @return id
     */
    public String getID();

}
