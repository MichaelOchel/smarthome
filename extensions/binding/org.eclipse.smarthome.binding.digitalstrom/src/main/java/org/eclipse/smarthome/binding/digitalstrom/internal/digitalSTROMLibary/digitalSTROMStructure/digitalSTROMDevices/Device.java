/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices;

import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DSID;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceSceneSpec;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.FunctionalColorGroupEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.OutputModeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.InternalScene;

/**
 * The {@link Device} represents a DigitalSTROM internal stored device.
 *
 * @author Alexander Betker - Initial contribution
 * @since 1.3.0
 * @author Michael Ochel - add methods for ESH, new functionalities and JavaDoc
 * @author Mathias Siegele - add methods for ESH, new functionalities and JavaDoc
 */
public interface Device {

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
     * Returns the id of the DS-Meter in which the device is registered.
     *
     * @return meterDSID
     */
    public DSID getMeterDSID();

    /**
     * Sets the id of the DS-Meter in which the device is registered.
     *
     * @param meterDSID
     */

    public void setMeterDSID(String meterDSID);

    /**
     * Returns the hardware info of this device.
     * You can see all available hardware info here
     * {@link http://www.digitalstrom.com/Partner/Support/Techn-Dokumentation/}
     *
     * @return hardware info
     */
    public String getHWinfo();

    /**
     * Returns the user defined name of this device.
     *
     * @return name of this device
     */
    public String getName();

    /**
     * Sets the name of this device;
     *
     * @param new name for this device
     */
    public void setName(String name);

    /**
     * Returns the zone id in which this device is.
     *
     * @return zone id
     */
    public int getZoneId();

    /**
     * Sets the zone id to the given zone id of this device is.
     *
     * @parm zone id
     */
    public void setZoneId(int zoneID);

    /**
     * This device is available in his zone or not.
     * Every 24h the dSM (meter) checks, if the devices are
     * plugged in
     *
     * @return true, if device is available otherwise false
     */
    public boolean isPresent();

    /**
     * Set this device is available in his zone or not.
     *
     * @param isPresent (true = available | false = not available)
     */
    public void setIsPresent(boolean isPresent);

    /**
     * Returns true if this device is on otherwise false.
     *
     * @return is on (true = on | false = off)
     */
    public boolean isOn();

    /**
     * Set this device on if the flag is true or off if it is false.
     *
     * @param flag (true = on | false = off)
     */
    public void setIsOn(boolean flag);

    /**
     * Returns true if this shade device is open otherwise false.
     *
     * @return is on (true = open | false = closed)
     */
    public boolean isOpen();

    /**
     * Set this shade device open if the flag is true or closed if it is false.
     *
     * @param flag (true = open | false = closed)
     */
    public void setIsOpen(boolean flag);

    /**
     * Return true if this device is dimmable, otherwise false.
     *
     * @return is dimmable (true = yes | false = no)
     */
    public boolean isDimmable();

    /**
     * Returns true if this device is a shade device (grey), otherwise false.
     *
     * @return is shade (true = yes | false = no)
     */
    public boolean isRollershutter();

    /**
     * Returns true if the device output mode isn't disabled.
     *
     * @return have output mode (true = yes | false = no)
     */
    public boolean isDeviceWithOutput();

    /**
     * Returns the current functional color group of this device.
     * For more informations please have a look at {@link FunctionalColorGroup}.
     *
     * @return current functional color group
     */
    public FunctionalColorGroupEnum getFunctionalColorGroup();

    /**
     * Sets the functional color group of this device.
     *
     * @param fuctionalColorGroup
     */
    public void setFunctionalColorGroup(FunctionalColorGroupEnum fuctionalColorGroup);

    /**
     * Returns the current output mode of this device.
     * Some devices are able to have different output modes e.g. the device GE-KM200 is able to
     * be in dimm mode, switch mode or disabled.
     * For more informations please have a look at {@link OutputModeEnum}.
     *
     * @return the current output mode of this device
     */
    public OutputModeEnum getOutputMode();

    /**
     * Increase the output value of this device.
     */
    public void increase();

    /**
     * Decrease the output value of this device.
     */
    public void decrease();

    /**
     * Returns the current slat position of this device.
     *
     * @return current slat position
     */
    public int getSlatPosition();

    /**
     * Sets the slat position of this device to the given slat position.
     *
     * @return slat position
     */
    public void setSlatPosition(int slatPosition);

    /**
     * Returns the maximal slat position value of this device.
     *
     * @return maximal slat position value
     */
    public int getMaxSlatPosition();

    /**
     * Returns the minimal slat position value of this device.
     *
     * @return minimal slat position value
     */
    public int getMinSlatPosition();

    /**
     * Returns the current output value of this device.
     * This can be the slat position or the brightness of this device.
     *
     * @return current output value
     */
    public short getOutputValue();

    /**
     * Set the output value of this device to a given value.
     *
     * @param outputValue
     */
    public void setOutputValue(short outputValue);

    /**
     * Returns the maximal output value of this device.
     *
     * @return maximal output value
     */
    public short getMaxOutputValue();

    /**
     * Returns the last recorded power consumption in watt of this device.
     *
     * @return current power consumption in watt
     */
    public int getActivePower();

    /**
     * Set the current power consumption in watt to the given power consumption.
     *
     * @param powerConsumption in watt
     */
    public void setActivePower(int powerConsumption);

    /**
     * Returns the energy meter value in watt per hour of this device.
     *
     * @return energy meter value in watt per hour
     */
    public int getOutputCurrent();

    /**
     * Set the last recorded energy meter value in watt per hour of this device.
     *
     * @param energy meter value in watt per hour
     */
    public void setOutputCurrent(int value);

    /**
     * Returns the last recorded electric meter value in ampere of this device.
     *
     * @return electric meter value in amoere
     */
    public int getElectricMeter();

    /**
     * Sets the last recorded electric meter value in ampere of this device.
     *
     * @param electric meter value in mA
     */
    public void setElectricMeter(int electricMeterValue);

    /**
     * Returns a list with group id's in which the device is part of.
     *
     * @return List of group id's
     */
    public List<Short> getGroups();

    public void addGroup(Short groupID);

    public void setGroups(List<Short> newGroupList);

    /**
     * Returns the scene output value of this device of the given scene id
     * or -1 if this scene id isn't read yet.
     *
     * @return scene output value or -1
     */
    public int getSceneOutputValue(short sceneId);

    /**
     * Sets the scene output value of this device for the given scene id and scene output value.
     *
     * @param sceneId
     * @param sceneOutputValue
     */
    public void setSceneOutputValue(short sceneId, int sceneOutputValue);

    /**
     * This configuration is very important. The devices can
     * be configured to not react to some commands (scene calls).
     * So you can't imply that a device automatically turns on (by default yes,
     * but if someone configured his own scenes, then maybe not) after a
     * scene call. This method returns true or false, if the configuration
     * for this sceneID already has been read
     *
     * @param sceneId the sceneID
     * @return true if this device has the configuration for this specific scene
     */
    public boolean containsSceneConfig(short sceneId);

    /**
     * Add the config for this scene. The config has the configuration
     * for the specific sceneID.
     *
     * @param sceneId scene call id
     * @param sceneSpec config for this sceneID
     */
    public void addSceneConfig(short sceneId, DeviceSceneSpec sceneSpec);

    /**
     * Get the config for this scene. The config has the configuration
     * for the specific sceneID.
     *
     * @param sceneId scene call id
     * @return sceneSpec config for this sceneID
     */
    public DeviceSceneSpec getSceneConfig(short sceneId);

    /**
     * Should the device react on this scene call or not .
     *
     * @param sceneId scene call id
     * @return true, if this device should react on this sceneID
     */
    public boolean doIgnoreScene(short sceneId);

    // follow methods added by Michael Ochel and Matthias Siegele

    /**
     * Returns true if the power consumption is up to date or false if it has to be updated.
     *
     * @return is up to date (true = yes | false = no)
     */
    public boolean isActivePowerUpToDate();

    /**
     * Returns true if the electric meter is up to date or false if it has to be updated.
     *
     * @return is up to date (true = yes | false = no)
     */
    public boolean isElectricMeterUpToDate();

    /**
     * Returns true if the energy meter is up to date or false if it has to be updated.
     *
     * @return is up to date (true = yes | false = no)
     */
    public boolean isOutputCurrentUpToDate();

    /**
     * Returns true if all sensor data are up to date or false if some have to be updated.
     *
     * @return is up to date (true = yes | false = no)
     */
    public boolean isSensorDataUpToDate();

    /**
     * Sets the priority to refresh the data of the sensors to the given priorities.
     * They can be never, low, medium or high.
     *
     * @param powerConsumptionRefreshPriority
     * @param electricMeterRefreshPriority
     * @param energyMeterRefreshPriority
     */
    public void setSensorDataRefreshPriority(String powerConsumptionRefreshPriority,
            String electricMeterRefreshPriority, String energyMeterRefreshPriority);

    /**
     * Returns the priority of the power consumption refresh.
     *
     * @return power consumption refresh priority
     */

    public String getActivePowerRefreshPriority();

    /**
     * Returns the priority of the electric meter refresh.
     *
     * @return electric meter refresh priority
     */
    public String getElectricMeterRefreshPriority();

    /**
     * Returns the priority of the energy meter refresh.
     *
     * @return energy meter refresh priority
     */
    public String getOutputCurrentRefreshPriority();

    /**
     * Returns true if the device is up to date.
     *
     * @return DigitalSTROM-Device is up to date (true = yes | false = no)
     */
    public boolean isDeviceUpToDate();

    /**
     * Returns the next {@linkDeviceStateUpdate} to update the DigitalSTROM-Device on the DigitalSTROM-Server.
     *
     * @return DeviceStateUpdate for DigitalSTROM-Device
     */
    public DeviceStateUpdate getNextDeviceUpdateState();

    /**
     * Update the internal stored device object.
     *
     * @param deviceStateUpdate
     */
    public void updateInternalDeviceState(DeviceStateUpdate deviceStateUpdate);

    /**
     * Call the given {@link InternalScene} on this {@link Device} and updates it.
     *
     * @param scene
     */
    public void callInternalScene(InternalScene scene);

    /**
     * Undo the given {@link InternalScene} on this {@link Device} and updates it.
     */
    public void undoInternalScene();

    /**
     * Initial a call scene for the given scene number.
     *
     * @param sceneNumber
     */
    public void callScene(Short sceneNumber);

    /**
     * Returns the current active {@link InternalScene} otherwise null.
     *
     * @return active {@link InternalScene} or null
     */
    public InternalScene getAcitiveScene();

    /**
     * Undo the active scene if a scene is active.
     */
    public void undoScene();

    /**
     * Checks the scene configuration for the given scene number and initial a scene configuration reading with the
     * given priority if no scene configuration exists.
     *
     * @param sceneNumber
     * @param prio
     */
    public void checkSceneConfig(Short sceneNumber, int prio);

    /**
     * Register a {@link DeviceStatusListener} to this {@link Device}.
     *
     * @param listener
     */
    public void registerDeviceStateListener(DeviceStatusListener listener);

    /**
     * Unregister the {@link DeviceStatusListener} to this {@link Device} if it exists.
     */
    public void unregisterDeviceStateListener();

    /**
     * Returns true if a {@link DeviceStatusListener} is registered to this {@link Device} otherwise false.
     *
     * @return return true if a lister is registerd otherwise false
     */
    public boolean isListenerRegisterd();

    /**
     * Sets the given output mode as new output mode of this {@link Device}.
     *
     * @param newOutputMode
     */
    public void setOutputMode(OutputModeEnum newOutputMode);

    /**
     * Returns a {@link List} of all saved scene configurations.
     *
     * @return
     */
    public List<Short> getSavedScenes();

    /**
     * Initial a internal device update as call scene for the given scene number.
     *
     * @param sceneNumber
     */
    public void internalCallScene(Short sceneNumber);

    /**
     * Initial a internal device update as undo scene.
     */
    public void internalUndoScene();

    /**
     * Returns true if this {@link Device} is a device with a switch output mode.
     *
     * @return true if it is a switch otherwise false
     */
    public boolean isSwitch();

}
