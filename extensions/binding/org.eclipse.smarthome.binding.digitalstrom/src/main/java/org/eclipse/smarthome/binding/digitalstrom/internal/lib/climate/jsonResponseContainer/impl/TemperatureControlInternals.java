package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.TemperatureControl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonObject;

public class TemperatureControlInternals extends TemperatureControl {

    private Short controlState;
    private Float ctrlTRecent;
    private Float ctrlTReference;
    private Float ctrlTError;
    private Float ctrlTErrorPrev;
    private Float ctrlIntegral;
    private Float ctrlYp;
    private Float ctrlYi;
    private Float ctrlYd;
    private Float ctrlY;
    private Short ctrlAntiWindUp;

    public TemperatureControlInternals(JsonObject jObject, Integer zoneID, String zoneName) {
        this.zoneID = zoneID;
        this.zoneName = zoneName;
        if (jObject.get(JSONApiResponseKeysEnum.IS_CONFIGURED.getKey()) != null) {
            this.isConfigured = jObject.get(JSONApiResponseKeysEnum.IS_CONFIGURED.getKey()).getAsBoolean();
        }
        if (isConfigured) {
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_MODE.getKey()) != null) {
                this.controlMode = jObject.get(JSONApiResponseKeysEnum.CONTROL_MODE.getKey()).getAsShort();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_STATE.getKey()) != null) {
                this.controlState = jObject.get(JSONApiResponseKeysEnum.CONTROL_STATE.getKey()).getAsShort();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_DSUID.getKey()) != null) {
                this.controlDSUID = jObject.get(JSONApiResponseKeysEnum.CONTROL_DSUID.getKey()).getAsString();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_T_RECENT.getKey()) != null) {
                this.ctrlTRecent = jObject.get(JSONApiResponseKeysEnum.CTRL_T_RECENT.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_T_REFERENCE.getKey()) != null) {
                this.ctrlTReference = jObject.get(JSONApiResponseKeysEnum.CTRL_T_REFERENCE.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_T_ERROR.getKey()) != null) {
                this.ctrlTError = jObject.get(JSONApiResponseKeysEnum.CTRL_T_ERROR.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_T_ERROR_PREV.getKey()) != null) {
                this.ctrlTErrorPrev = jObject.get(JSONApiResponseKeysEnum.CTRL_T_ERROR_PREV.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_INTEGRAL.getKey()) != null) {
                this.ctrlIntegral = jObject.get(JSONApiResponseKeysEnum.CTRL_INTEGRAL.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_YP.getKey()) != null) {
                this.ctrlY = jObject.get(JSONApiResponseKeysEnum.CTRL_YP.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_YI.getKey()) != null) {
                this.ctrlYi = jObject.get(JSONApiResponseKeysEnum.CTRL_YI.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_YD.getKey()) != null) {
                this.ctrlYd = jObject.get(JSONApiResponseKeysEnum.CTRL_YD.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_Y.getKey()) != null) {
                this.ctrlY = jObject.get(JSONApiResponseKeysEnum.CTRL_Y.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_ANTI_WIND_UP.getKey()) != null) {
                this.ctrlAntiWindUp = jObject.get(JSONApiResponseKeysEnum.CTRL_ANTI_WIND_UP.getKey()).getAsShort();
            }
        }
    }

    /**
     * @return the controlState
     */
    public Short getControlState() {
        return controlState;
    }

    /**
     * @return the ctrlTRecent
     */
    public Float getCtrlTRecent() {
        return ctrlTRecent;
    }

    /**
     * @return the ctrlTReference
     */
    public Float getCtrlTReference() {
        return ctrlTReference;
    }

    /**
     * @return the ctrlTError
     */
    public Float getCtrlTError() {
        return ctrlTError;
    }

    /**
     * @return the ctrlTErrorPrev
     */
    public Float getCtrlTErrorPrev() {
        return ctrlTErrorPrev;
    }

    /**
     * @return the ctrlIntegral
     */
    public Float getCtrlIntegral() {
        return ctrlIntegral;
    }

    /**
     * @return the ctrlYp
     */
    public Float getCtrlYp() {
        return ctrlYp;
    }

    /**
     * @return the ctrlYi
     */
    public Float getCtrlYi() {
        return ctrlYi;
    }

    /**
     * @return the ctrlYd
     */
    public Float getCtrlYd() {
        return ctrlYd;
    }

    /**
     * @return the ctrlY
     */
    public Float getCtrlY() {
        return ctrlY;
    }

    /**
     * @return the ctrlAntiWindUp
     */
    public Short getCtrlAntiWindUp() {
        return ctrlAntiWindUp;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TemperatureControlInternals [controlState=" + controlState + ", ctrlTRecent=" + ctrlTRecent
                + ", ctrlTReference=" + ctrlTReference + ", ctrlTError=" + ctrlTError + ", ctrlTErrorPrev="
                + ctrlTErrorPrev + ", ctrlIntegral=" + ctrlIntegral + ", ctrlYp=" + ctrlYp + ", ctrlYi=" + ctrlYi
                + ", ctrlYd=" + ctrlYd + ", ctrlY=" + ctrlY + ", ctrlAntiWindUp=" + ctrlAntiWindUp + "]";
    }

}
