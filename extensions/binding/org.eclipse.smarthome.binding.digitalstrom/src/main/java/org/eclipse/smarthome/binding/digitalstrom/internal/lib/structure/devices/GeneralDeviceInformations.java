package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.DSID;

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
     * @param name
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
     * @param deviceStatuslistener
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

    public Boolean isValide();

    public void setIsValide(boolean isPresent);

    public String getDisplayID();

    DeviceStatusListener getDeviceStatusListener();
}
