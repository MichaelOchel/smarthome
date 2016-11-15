package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices;

import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.CachedMeteringValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringUnitsEnum;

public interface Circuit extends GeneralDeviceInformations {

    public Integer getHwVersion();

    public void setHwVersion(Integer hwVersion);

    public String getHwVersionString();

    public void setHwVersionString(String hwVersionString);

    public String getSwVersion();

    public void setSwVersion(String swVersion);

    public Integer getArmSwVersion();

    public void setArmSwVersion(Integer armSwVersion);

    public Integer getDspSwVersion();

    public void setDspSwVersion(Integer dspSwVersion);

    public Integer getApiVersion();

    public void setApiVersion(Integer apiVersion);

    public String getHwName();

    public void setHwName(String hwName);

    public Integer getBusMemberType();

    public void setBusMemberType(Integer busMemberType);

    public Boolean getHasDevices();

    public void setHasDevices(Boolean hasDevices);

    public Boolean getHasMetering();

    public void setHasMetering(Boolean hasMetering);

    public String getVdcConfigURL();

    public void setVdcConfigURL(String vdcConfigURL);

    public String getVdcModelUID();

    public void setVdcModelUID(String vdcModelUID);

    public String getVdcHardwareGuid();

    public void setVdcHardwareGuid(String vdcHardwareGuid);

    public String getVdcHardwareModelGuid();

    public void setVdcHardwareModelGuid(String vdcHardwareModelGuid);

    public String getVdcVendorGuid();

    public void setVdcVendorGuid(String vdcVendorGuid);

    public String getVdcOemGuid();

    public void setVdcOemGuid(String vdcOemGuid);

    public Boolean getIgnoreActionsFromNewDevices();

    public void setIgnoreActionsFromNewDevices(Boolean ignoreActionsFromNewDevices);

    void addMeteringValue(CachedMeteringValue cachedMeteringValue);

    double getMeteringValue(MeteringTypeEnum meteringType, MeteringUnitsEnum meteringUnit);

    List<CachedMeteringValue> getAllCachedMeteringValues();

}
