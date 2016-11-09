package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.impl;

import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.AbstractGeneralDeviceInformations;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Circuit;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.CachedMeteringValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.MeteringUnitsEnum;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

public class CircuitImpl extends AbstractGeneralDeviceInformations implements Circuit {

    // TODO: complete circuit interface and this class

    // Config
    private Integer hwVersion = null;
    private String hwVersionString = null;
    private String swVersion = null;
    private Integer armSwVersion = null;
    private Integer dspSwVersion = null;
    private Integer apiVersion = null;
    private String hwName = null;
    private Integer busMemberType = null;
    private Boolean hasDevices = null;
    private Boolean hasMetering = null;
    private String vdcConfigURL = null;
    private String vdcModelUID = null;
    private String vdcHardwareGuid = null;
    private String vdcHardwareModelGuid = null;
    private String vdcVendorGuid = null;
    private String vdcOemGuid = null;
    private Boolean ignoreActionsFromNewDevices = null;

    // Metering
    CachedMeteringValue consumption = null;
    CachedMeteringValue energyWh = null;
    CachedMeteringValue energyWs = null;
    CachedMeteringValue energyDeltaWh = null;
    CachedMeteringValue energyDeltaWs = null;

    public CircuitImpl(JsonObject jObject) {
        super(jObject);
        if (jObject.get(JSONApiResponseKeysEnum.HW_VERSION.getKey()) != null) {
            hwVersion = jObject.get(JSONApiResponseKeysEnum.HW_VERSION.getKey()).getAsInt();
        }
        if (jObject.get(JSONApiResponseKeysEnum.HW_VERSION_STRING.getKey()) != null) {
            hwVersionString = jObject.get(JSONApiResponseKeysEnum.HW_VERSION_STRING.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.SW_VERSION.getKey()) != null) {
            swVersion = jObject.get(JSONApiResponseKeysEnum.SW_VERSION.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.ARM_SW_VERSION.getKey()) != null) {
            armSwVersion = jObject.get(JSONApiResponseKeysEnum.ARM_SW_VERSION.getKey()).getAsInt();
        }
        if (jObject.get(JSONApiResponseKeysEnum.DSP_SW_VERSION.getKey()) != null) {
            dspSwVersion = jObject.get(JSONApiResponseKeysEnum.DSP_SW_VERSION.getKey()).getAsInt();
        }
        if (jObject.get(JSONApiResponseKeysEnum.API_VERSION.getKey()) != null) {
            apiVersion = jObject.get(JSONApiResponseKeysEnum.API_VERSION.getKey()).getAsInt();
        }
        if (jObject.get(JSONApiResponseKeysEnum.HW_NAME.getKey()) != null) {
            hwName = jObject.get(JSONApiResponseKeysEnum.HW_NAME.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.BUS_MEMBER_TYPE.getKey()) != null) {
            busMemberType = jObject.get(JSONApiResponseKeysEnum.BUS_MEMBER_TYPE.getKey()).getAsInt();
        }
        if (jObject.get(JSONApiResponseKeysEnum.HAS_DEVICES.getKey()) != null) {
            hasDevices = jObject.get(JSONApiResponseKeysEnum.HAS_DEVICES.getKey()).getAsBoolean();
        }
        if (jObject.get(JSONApiResponseKeysEnum.HAS_METERING.getKey()) != null) {
            hasMetering = jObject.get(JSONApiResponseKeysEnum.HAS_METERING.getKey()).getAsBoolean();
        }
        if (jObject.get(JSONApiResponseKeysEnum.VDC_CONFIG_URL.getKey()) != null) {
            vdcConfigURL = jObject.get(JSONApiResponseKeysEnum.VDC_CONFIG_URL.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.VDC_MODEL_UID.getKey()) != null) {
            vdcModelUID = jObject.get(JSONApiResponseKeysEnum.VDC_MODEL_UID.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.VDC_HARDWARE_GUID.getKey()) != null) {
            vdcHardwareGuid = jObject.get(JSONApiResponseKeysEnum.VDC_HARDWARE_GUID.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.VDC_HARDWARE_MODEL_GUID.getKey()) != null) {
            vdcHardwareModelGuid = jObject.get(JSONApiResponseKeysEnum.VDC_HARDWARE_MODEL_GUID.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.VDC_VENDOR_GUID.getKey()) != null) {
            vdcVendorGuid = jObject.get(JSONApiResponseKeysEnum.VDC_VENDOR_GUID.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.VDC_OEM_GUID.getKey()) != null) {
            vdcOemGuid = jObject.get(JSONApiResponseKeysEnum.VDC_OEM_GUID.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.IGNORE_ACTIONS_FROM_NEW_DEVICES.getKey()) != null) {
            ignoreActionsFromNewDevices = jObject.get(JSONApiResponseKeysEnum.IGNORE_ACTIONS_FROM_NEW_DEVICES.getKey())
                    .getAsBoolean();
        }
    }

    @Override
    public Integer getHwVersion() {
        return hwVersion;
    }

    @Override
    public void setHwVersion(Integer hwVersion) {
        this.hwVersion = hwVersion;
    }

    @Override
    public String getHwVersionString() {
        return hwVersionString;
    }

    @Override
    public void setHwVersionString(String hwVersionString) {
        this.hwVersionString = hwVersionString;
    }

    @Override
    public String getSwVersion() {
        return swVersion;
    }

    @Override
    public void setSwVersion(String swVersion) {
        this.swVersion = swVersion;
    }

    @Override
    public Integer getArmSwVersion() {
        return armSwVersion;
    }

    @Override
    public void setArmSwVersion(Integer armSwVersion) {
        this.armSwVersion = armSwVersion;
    }

    @Override
    public Integer getDspSwVersion() {
        return dspSwVersion;
    }

    @Override
    public void setDspSwVersion(Integer dspSwVersion) {
        this.dspSwVersion = dspSwVersion;
    }

    @Override
    public Integer getApiVersion() {
        return apiVersion;
    }

    @Override
    public void setApiVersion(Integer apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Override
    public String getHwName() {
        return hwName;
    }

    @Override
    public void setHwName(String hwName) {
        this.hwName = hwName;
    }

    @Override
    public Integer getBusMemberType() {
        return busMemberType;
    }

    @Override
    public void setBusMemberType(Integer busMemberType) {
        this.busMemberType = busMemberType;
    }

    @Override
    public Boolean getHasDevices() {
        return hasDevices;
    }

    @Override
    public void setHasDevices(Boolean hasDevices) {
        this.hasDevices = hasDevices;
    }

    @Override
    public Boolean getHasMetering() {
        return hasMetering;
    }

    @Override
    public void setHasMetering(Boolean hasMetering) {
        this.hasMetering = hasMetering;
    }

    @Override
    public String getVdcConfigURL() {
        return vdcConfigURL;
    }

    @Override
    public void setVdcConfigURL(String vdcConfigURL) {
        this.vdcConfigURL = vdcConfigURL;
    }

    @Override
    public String getVdcModelUID() {
        return vdcModelUID;
    }

    @Override
    public void setVdcModelUID(String vdcModelUID) {
        this.vdcModelUID = vdcModelUID;
    }

    @Override
    public String getVdcHardwareGuid() {
        return vdcHardwareGuid;
    }

    @Override
    public void setVdcHardwareGuid(String vdcHardwareGuid) {
        this.vdcHardwareGuid = vdcHardwareGuid;
    }

    @Override
    public String getVdcHardwareModelGuid() {
        return vdcHardwareModelGuid;
    }

    @Override
    public void setVdcHardwareModelGuid(String vdcHardwareModelGuid) {
        this.vdcHardwareModelGuid = vdcHardwareModelGuid;
    }

    @Override
    public String getVdcVendorGuid() {
        return vdcVendorGuid;
    }

    @Override
    public void setVdcVendorGuid(String vdcVendorGuid) {
        this.vdcVendorGuid = vdcVendorGuid;
    }

    @Override
    public String getVdcOemGuid() {
        return vdcOemGuid;
    }

    @Override
    public void setVdcOemGuid(String vdcOemGuid) {
        this.vdcOemGuid = vdcOemGuid;
    }

    @Override
    public Boolean getIgnoreActionsFromNewDevices() {
        return ignoreActionsFromNewDevices;
    }

    @Override
    public void setIgnoreActionsFromNewDevices(Boolean ignoreActionsFromNewDevices) {
        this.ignoreActionsFromNewDevices = ignoreActionsFromNewDevices;
    }

    @Override
    public void addMeteringValue(CachedMeteringValue cachedMeteringValue) {
        if (cachedMeteringValue != null) {
            switch (cachedMeteringValue.getMeteringType()) {
                case consumption:
                    if (checkNewer(this.consumption, cachedMeteringValue)) {
                        this.consumption = cachedMeteringValue;
                    }
                    break;
                case energy:
                    if (cachedMeteringValue.getMeteringUnit() == null
                            || cachedMeteringValue.getMeteringUnit().equals(MeteringUnitsEnum.Wh)) {
                        if (checkNewer(this.energyWh, cachedMeteringValue)) {
                            this.energyWh = cachedMeteringValue;
                        }
                    } else {
                        if (checkNewer(this.energyWs, cachedMeteringValue)) {
                            this.energyWs = cachedMeteringValue;
                        }
                    }
                    break;
                case energyDelta:
                    if (cachedMeteringValue.getMeteringUnit() == null
                            || cachedMeteringValue.getMeteringUnit().equals(MeteringUnitsEnum.Wh)) {
                        if (checkNewer(this.energyDeltaWh, cachedMeteringValue)) {
                            this.energyDeltaWh = cachedMeteringValue;
                        }
                    } else {
                        if (checkNewer(this.energyDeltaWs, cachedMeteringValue)) {
                            this.energyDeltaWs = cachedMeteringValue;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private boolean checkNewer(CachedMeteringValue oldCachedMeteringValue, CachedMeteringValue newCachedMeteringValue) {
        return oldCachedMeteringValue != null
                && oldCachedMeteringValue.getDateAsDate().before(newCachedMeteringValue.getDateAsDate());
    }

    @Override
    public double getMeteringValue(MeteringTypeEnum meteringType, MeteringUnitsEnum meteringUnit) {
        switch (meteringType) {
            case consumption:
                return getValue(this.consumption);
            case energy:
                if (meteringUnit.equals(MeteringUnitsEnum.Wh)) {
                    return getValue(this.energyWh);
                } else {
                    return getValue(this.energyWs);
                }
            case energyDelta:
                if (meteringUnit.equals(MeteringUnitsEnum.Wh)) {
                    return getValue(this.energyDeltaWh);
                } else {
                    return getValue(this.energyDeltaWs);
                }
            default:
                break;
        }
        return -1;
    }

    private double getValue(CachedMeteringValue cachedMeteringValue) {
        return cachedMeteringValue != null ? cachedMeteringValue.getValue() : -1;
    }

    @Override
    public List<CachedMeteringValue> getAllCachedMeteringValues() {
        return Lists.newArrayList(consumption, energyDeltaWh, energyDeltaWs, energyWh, energyWs);
    }
}
