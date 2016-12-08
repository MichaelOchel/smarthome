/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices;

import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.CachedMeteringValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringUnitsEnum;

/**
 * The {@link Circuit} represents a circuit of the digitalStrom system. For that all information will be able to get and
 * set through the same named getter- and setter-methods. To get informed about status and configuration changes a
 * {@link DeviceStatusListener} can be registered. For that and to get the general device informations like the dSID the
 * {@link Circuit} implements the {@link GeneralDeviceInformations} interface.
 *
 * @author Michael Ochel - initial contributer
 * @author Matthias Siegele - initial contributer
 */
public interface Circuit extends GeneralDeviceInformations {

    /**
     * Returns the hardware version of this {@link Circuit} as {@link Integer}.
     *
     * @return hardware version
     */
    public Integer getHwVersion();

    /**
     * Sets the hardware version of this {@link Circuit} as {@link Integer}.
     *
     * @param hwVersion the new hardware version as {@link Integer}
     */
    public void setHwVersion(Integer hwVersion);

    /**
     * Returns the hardware version of this {@link Circuit} as {@link String}.
     *
     * @return hardware version
     */
    public String getHwVersionString();

    /**
     * Sets the hardware version of this {@link Circuit} as {@link String}.
     *
     * @param hwVersionString the new hardware version as {@link Integer}
     */
    public void setHwVersionString(String hwVersionString);

    /**
     * Returns the software version of this {@link Circuit} as {@link String}.
     *
     * @return the software version
     */
    public String getSwVersion();

    /**
     * Sets the software version of this {@link Circuit} as {@link String}.
     *
     * @param swVersion the new software version
     */
    public void setSwVersion(String swVersion);

    /**
     * Returns the arm software version of this {@link Circuit} as {@link Integer}.
     *
     * @return the arm software version
     */
    public Integer getArmSwVersion();

    /**
     * Sets the arm software version of this {@link Circuit} as {@link Integer}.
     *
     * @param armSwVersion the new arm software version
     */
    public void setArmSwVersion(Integer armSwVersion);

    /**
     * Returns the dsp software version of this {@link Circuit} as {@link Integer}.
     *
     * @return the dsp softwaree version
     */
    public Integer getDspSwVersion();

    /**
     * Sets the dsp software version of this {@link Circuit} as {@link Integer}.
     *
     * @param dspSwVersion the new dsp software version
     */
    public void setDspSwVersion(Integer dspSwVersion);

    /**
     * Returns the api version of this {@link Circuit} as {@link Integer}.
     *
     * @return the api version as {@link Integer}
     */
    public Integer getApiVersion();

    /**
     * Setss the api version of this {@link Circuit} as {@link Integer}.
     *
     * @param apiVersion the new api version
     */
    public void setApiVersion(Integer apiVersion);

    /**
     * Returns the hardware name of this {@link Circuit}.
     *
     * @return the hardware name
     */
    public String getHwName();

    /**
     * Sets the hardware name of this {@link Circuit}.
     *
     * @param hwName the new hardware name
     */
    public void setHwName(String hwName);

    /**
     * Returns the bus member type of this {@link Circuit} as {@link Integer}.
     *
     * @return the bus member type
     */
    public Integer getBusMemberType();

    /**
     * Sets the bus member type of this {@link Circuit} as {@link Integer}.
     *
     * @param busMemberType the new bus member type
     */
    public void setBusMemberType(Integer busMemberType);

    /**
     * Returns true, if this {@link Circuit} has connected {@link Device}'s, otherwise false.
     *
     * @return true, if {@link Device}'s are connected
     */
    public Boolean getHasDevices();

    /**
     * Sets the connected devices flag.
     * 
     * @param hasDevices the new connected devices flag
     */
    public void setHasDevices(Boolean hasDevices);

    /**
     * Returns true, if this {@link Circuit} is valid to metering power data, otherwise false.
     * 
     * @return true, if is valid to metering power data
     */
    public Boolean getHasMetering();

    /**
     * Sets the flag hasMetering.
     * 
     * @param hasMetering the new hasMetering flag.
     */
    public void setHasMetering(Boolean hasMetering);

    /**
     * Returns the vdc configuration URL of this {@link Circuit} as {@link String}.
     * 
     * @return the vdc configuration URL
     */
    public String getVdcConfigURL();

    /**
     * Sets the vdc configuration URL of this {@link Circuit} as {@link String}.
     * 
     * @param vdcConfigURL the new vdc configuration URL
     */
    public void setVdcConfigURL(String vdcConfigURL);

    /**
     * Returns the vdc mode UID of this {@link Circuit} as {@link String}.
     * 
     * @return the vdc mode UID
     */
    public String getVdcModelUID();

    /**
     * Sets the vdc mode UID of this {@link Circuit} as {@link String}.
     * 
     * @param vdcModelUID the new vdc mode UID
     */
    public void setVdcModelUID(String vdcModelUID);

    /**
     * Returns the vdc hardware GUID of this {@link Circuit} as {@link String}.
     * 
     * @return the vdc hardware GUID
     */
    public String getVdcHardwareGuid();

    /**
     * Sets the vdc hardware GUID of this {@link Circuit} as {@link String}.
     * 
     * @param vdcHardwareGuid the new vdc hardware GUID
     */
    public void setVdcHardwareGuid(String vdcHardwareGuid);

    /**
     * Returns the vdc hardware model GUID of this {@link Circuit} as {@link String}.
     * 
     * @return the vdc hardware mode GUID
     */
    public String getVdcHardwareModelGuid();

    /**
     * Sets the vdc hardware model GUID of this {@link Circuit} as {@link String}.
     * 
     * @param vdcHardwareModelGuid the new vdc model GUID
     */
    public void setVdcHardwareModelGuid(String vdcHardwareModelGuid);

    /**
     * Returns the vdc vendor GUID of this {@link Circuit} as {@link String}.
     * 
     * @return the vdc vendor GUID
     */
    public String getVdcVendorGuid();

    /**
     * Sets the vdc vendor GUID of this {@link Circuit} as {@link String}.
     * 
     * @param vdcVendorGuid the new vdc vendor GUID
     */
    public void setVdcVendorGuid(String vdcVendorGuid);

    /**
     * Returns the vdc oem GUID of this {@link Circuit} as {@link String}.
     * 
     * @return the vdc oem GUID
     */
    public String getVdcOemGuid();

    /**
     * Sets the vdc oem GUID of this {@link Circuit} as {@link String}.
     * 
     * @param vdcOemGuid the new vdc oem GUID
     */
    public void setVdcOemGuid(String vdcOemGuid);

    /**
     * Returns true, if actions from new {@link Device}'s will be ignored by this {@link Circuit}, otherwise false.
     * 
     * @return true, if actions form new device will be ignored
     */
    public Boolean getIgnoreActionsFromNewDevices();

    /**
     * Sets the flag for ignore actions from new {@link Device}'s.
     * 
     * @param ignoreActionsFromNewDevices the new ignore actions from new devices flag
     */
    public void setIgnoreActionsFromNewDevices(Boolean ignoreActionsFromNewDevices);

    /**
     * Adds a new {@link CachedMeteringValue} or update the existing, if the new one is newer.
     * 
     * @param cachedMeteringValue the new {@link CachedMeteringValue}
     */
    public void addMeteringValue(CachedMeteringValue cachedMeteringValue);

    /**
     * Returns the value of the given {@link CachedMeteringValue} through the {@link MeteringTypeEnum} and
     * {@link MeteringUnitsEnum}.
     * 
     * @param meteringType (must not be null)
     * @param meteringUnit (can be null, default is {@link MeteringUnitsEnum#Wh})
     * @return the metering value or -1, if the metering value dose not exist
     */
    public double getMeteringValue(MeteringTypeEnum meteringType, MeteringUnitsEnum meteringUnit);

    /**
     * Returns the {@link List} of all {@link CachedMeteringValue}'s.
     * 
     * @return list of all {@link CachedMeteringValue}
     */
    public List<CachedMeteringValue> getAllCachedMeteringValues();

}
