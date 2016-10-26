package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.TemperatureControl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonObject;

public class TemperatureControlConfig extends TemperatureControl {

    private Integer referenceZone;
    private Float ctrlOffset;
    private Float manualValue;
    private Float emergencyValue;
    private Float ctrlKp;
    private Float ctrlTs;
    private Float ctrlTi;
    private Float ctrlKd;
    private Float ctrlImin;
    private Float ctrlImax;
    private Float ctrlYmin;
    private Float ctrlYmax;
    private Boolean ctrlAntiWindUp;
    private Boolean ctrlKeepFloorWarm;

    public TemperatureControlConfig(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.ID.getKey()) != null) {
            this.zoneID = jObject.get(JSONApiResponseKeysEnum.ID.getKey()).getAsInt();
        }
        if (jObject.get(JSONApiResponseKeysEnum.NAME.getKey()) != null) {
            this.zoneName = jObject.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
        }
        init(jObject);
    }

    public TemperatureControlConfig(JsonObject jObject, Integer zoneID, String zoneName) {
        this.zoneID = zoneID;
        this.zoneName = zoneName;
        init(jObject);
    }

    private void init(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.IS_CONFIGURED.getKey()) != null) {
            this.isConfigured = jObject.get(JSONApiResponseKeysEnum.IS_CONFIGURED.getKey()).getAsBoolean();
        }
        if (isConfigured) {
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_MODE.getKey()) != null) {
                this.controlMode = jObject.get(JSONApiResponseKeysEnum.CONTROL_MODE.getKey()).getAsShort();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_DSUID.getKey()) != null) {
                this.controlDSUID = jObject.get(JSONApiResponseKeysEnum.CONTROL_DSUID.getKey()).getAsString();
            }
            if (controlMode == 1) {
                if (jObject.get(JSONApiResponseKeysEnum.EMERGENCY_VALUE.getKey()) != null) {
                    this.emergencyValue = jObject.get(JSONApiResponseKeysEnum.EMERGENCY_VALUE.getKey()).getAsFloat();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_KP.getKey()) != null) {
                    this.ctrlKp = jObject.get(JSONApiResponseKeysEnum.CTRL_KP.getKey()).getAsFloat();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_TS.getKey()) != null) {
                    this.ctrlTs = jObject.get(JSONApiResponseKeysEnum.CTRL_TS.getKey()).getAsFloat();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_TI.getKey()) != null) {
                    this.ctrlTi = jObject.get(JSONApiResponseKeysEnum.CTRL_TI.getKey()).getAsFloat();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_KD.getKey()) != null) {
                    this.ctrlKd = jObject.get(JSONApiResponseKeysEnum.CTRL_KD.getKey()).getAsFloat();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_MIN.getKey()) != null) {
                    this.ctrlImin = jObject.get(JSONApiResponseKeysEnum.CTRL_MIN.getKey()).getAsFloat();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_MAX.getKey()) != null) {
                    this.ctrlImax = jObject.get(JSONApiResponseKeysEnum.CTRL_MAX.getKey()).getAsFloat();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_Y_MIN.getKey()) != null) {
                    this.ctrlYmin = jObject.get(JSONApiResponseKeysEnum.CTRL_Y_MIN.getKey()).getAsFloat();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_Y_MAX.getKey()) != null) {
                    this.ctrlYmax = jObject.get(JSONApiResponseKeysEnum.CTRL_Y_MAX.getKey()).getAsFloat();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_KEEP_FLOOR_WARM.getKey()) != null) {
                    this.ctrlKeepFloorWarm = jObject.get(JSONApiResponseKeysEnum.CTRL_KEEP_FLOOR_WARM.getKey())
                            .getAsBoolean();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_ANTI_WIND_UP.getKey()) != null) {
                    this.ctrlAntiWindUp = jObject.get(JSONApiResponseKeysEnum.CTRL_ANTI_WIND_UP.getKey())
                            .getAsBoolean();
                }
            }
            if (controlMode == 2) {
                if (jObject.get(JSONApiResponseKeysEnum.REFERENCE_ZONE.getKey()) != null) {
                    this.referenceZone = jObject.get(JSONApiResponseKeysEnum.REFERENCE_ZONE.getKey()).getAsInt();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_OFFSET.getKey()) != null) {
                    this.ctrlOffset = jObject.get(JSONApiResponseKeysEnum.CTRL_OFFSET.getKey()).getAsFloat();
                }
            }
        }

    }

    /**
     * @return the referenceZone
     */
    public Integer getReferenceZone() {
        return referenceZone;
    }

    /**
     * @return the ctrlOffset
     */
    public Float getCtrlOffset() {
        return ctrlOffset;
    }

    /**
     * @return the manualValue
     */
    public Float getManualValue() {
        return manualValue;
    }

    /**
     * @return the emergencyValue
     */
    public Float getEmergencyValue() {
        return emergencyValue;
    }

    /**
     * @return the ctrlKp
     */
    public Float getCtrlKp() {
        return ctrlKp;
    }

    /**
     * @return the ctrlTs
     */
    public Float getCtrlTs() {
        return ctrlTs;
    }

    /**
     * @return the ctrlTi
     */
    public Float getCtrlTi() {
        return ctrlTi;
    }

    /**
     * @return the ctrlKd
     */
    public Float getCtrlKd() {
        return ctrlKd;
    }

    /**
     * @return the ctrlImin
     */
    public Float getCtrlImin() {
        return ctrlImin;
    }

    /**
     * @return the ctrlImax
     */
    public Float getCtrlImax() {
        return ctrlImax;
    }

    /**
     * @return the ctrlYmin
     */
    public Float getCtrlYmin() {
        return ctrlYmin;
    }

    /**
     * @return the ctrlYmax
     */
    public Float getCtrlYmax() {
        return ctrlYmax;
    }

    /**
     * @return the ctrlAntiWindUp
     */
    public Boolean getCtrlAntiWindUp() {
        return ctrlAntiWindUp;
    }

    /**
     * @return the ctrlKeepFloorWarm
     */
    public Boolean getCtrlKeepFloorWarm() {
        return ctrlKeepFloorWarm;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TemperatureControlConfig [referenceZone=" + referenceZone + ", ctrlOffset=" + ctrlOffset
                + ", manualValue=" + manualValue + ", emergencyValue=" + emergencyValue + ", ctrlKp=" + ctrlKp
                + ", ctrlTs=" + ctrlTs + ", ctrlTi=" + ctrlTi + ", ctrlKd=" + ctrlKd + ", ctrlImin=" + ctrlImin
                + ", ctrlImax=" + ctrlImax + ", ctrlYmin=" + ctrlYmin + ", ctrlYmax=" + ctrlYmax + ", ctrlAntiWindUp="
                + ctrlAntiWindUp + ", ctrlKeepFloorWarm=" + ctrlKeepFloorWarm + "]";
    }
}
