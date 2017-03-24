/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.DSID;

/**
 * The {@link GeneralDeviceInformations} interface contains all informations of digitalSTROM devices, which are
 * identical for all device types. It also contains the methods to implement the mechanism of the
 * {@link DeviceStatusListener}.
 *
 * @author Michael Ochel - initial contributer
 * @author Matthias Siegele - initial contributer
 */
public interface GeneralDeviceInformations {

    /**
     * Returns the user defined name of this device.
     *
     * @return name of this device
     */
    public String getName();

    /**
     * Sets the name of this device;
     *
     * @param name to set
     */
    public void setName(String name);

    /**
     * Returns the dSID of this device.
     *
     * @return {@link DSID} dSID
     */
    public DSID getDSID();

    /**
     * Returns the dSUID of this device.
     *
     * @return dSID
     */
    public String getDSUID();

    /**
     * This device is available in his zone or not.
     * Every 24h the dSM (meter) checks, if the devices are
     * plugged in
     *
     * @return true, if device is available otherwise false
     */
    public Boolean isPresent();

    /**
     * Sets this device is available in his zone or not.
     *
     * @param isPresent (true = available | false = not available)
     */
    public void setIsPresent(boolean isPresent);

    /**
     * Register a {@link DeviceStatusListener} to this {@link Device}.
     *
     * @param deviceStatuslistener to register
     */
    public void registerDeviceStatusListener(DeviceStatusListener deviceStatuslistener);

    /**
     * Unregister the {@link DeviceStatusListener} to this {@link Device} if it exists.
     *
     * @return the unregistered {@link DeviceStatusListener} or null if no one was registered
     */
    public DeviceStatusListener unregisterDeviceStatusListener();

    /**
     * Returns true, if a {@link DeviceStatusListener} is registered to this {@link Device}, otherwise false.
     *
     * @return return true, if a lister is registered, otherwise false
     */
    public boolean isListenerRegisterd();

    /**
     * Returns true, if this device is valid, otherwise false.
     *
     * @return true, if valid
     */
    public Boolean isValid();

    /**
     * Sets the valid state.
     *
     * @param isValid the new valid state
     */
    public void setIsValid(boolean isValid);

    /**
     * Returns the in the digitalSTROM web interface displayed dSID.
     *
     * @return displayed dSID
     */
    public String getDisplayID();

    /**
     * Returns the registered {@link DeviceStatusListener} or null, if no {@link DeviceStatusListener} is registered
     *
     * @return the registered {@link DeviceStatusListener} or null
     */
    public DeviceStatusListener getDeviceStatusListener();
}
