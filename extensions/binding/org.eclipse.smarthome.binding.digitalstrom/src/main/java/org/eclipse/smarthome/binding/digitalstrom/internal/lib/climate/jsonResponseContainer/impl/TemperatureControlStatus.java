package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.TemperatureControl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonObject;

public class TemperatureControlStatus extends TemperatureControl {

    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

    private Short controlState;
    private Short operationMode;
    private Float temperature;
    private String temperatureTime;
    private Float nominalValue;
    private String nominalValueTime;
    private Float controlValue;
    private String controlValueTime;

    public TemperatureControlStatus(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.ID.getKey()) != null) {
            this.zoneID = jObject.get(JSONApiResponseKeysEnum.ID.getKey()).getAsInt();
        }
        if (jObject.get(JSONApiResponseKeysEnum.NAME.getKey()) != null) {
            this.zoneName = jObject.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
        }
        init(jObject);
    }

    public TemperatureControlStatus(JsonObject jObject, Integer zoneID, String zoneName) {
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
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_STATE.getKey()) != null) {
                this.controlState = jObject.get(JSONApiResponseKeysEnum.CONTROL_STATE.getKey()).getAsShort();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_DSUID.getKey()) != null) {
                this.controlDSUID = jObject.get(JSONApiResponseKeysEnum.CONTROL_DSUID.getKey()).getAsString();
            }
            if (jObject.get(JSONApiResponseKeysEnum.OPERATION_MODE.getKey()) != null) {
                this.operationMode = jObject.get(JSONApiResponseKeysEnum.OPERATION_MODE.getKey()).getAsShort();
            }
            if (jObject.get(JSONApiResponseKeysEnum.TEMPERATION_VALUE.getKey()) != null) {
                this.temperature = jObject.get(JSONApiResponseKeysEnum.TEMPERATION_VALUE.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.NOMINAL_VALUE.getKey()) != null) {
                this.nominalValue = jObject.get(JSONApiResponseKeysEnum.NOMINAL_VALUE.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_VALUE.getKey()) != null) {
                this.controlValue = jObject.get(JSONApiResponseKeysEnum.CONTROL_VALUE.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.TEMPERATION_VALUE_TIME.getKey()) != null) {
                this.temperatureTime = jObject.get(JSONApiResponseKeysEnum.TEMPERATION_VALUE_TIME.getKey())
                        .getAsString();
            }
            if (jObject.get(JSONApiResponseKeysEnum.NOMINAL_VALUE_TIME.getKey()) != null) {
                this.nominalValueTime = jObject.get(JSONApiResponseKeysEnum.NOMINAL_VALUE_TIME.getKey()).getAsString();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_VALUE_TIME.getKey()) != null) {
                this.controlValueTime = jObject.get(JSONApiResponseKeysEnum.CONTROL_VALUE_TIME.getKey()).getAsString();
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
     * @return the operationMode
     */
    public Short getOperationMode() {
        return operationMode;
    }

    /**
     * @return the temperature
     */
    public Float getTemperature() {
        return temperature;
    }

    /**
     * @return the temperatureTime
     * @throws ParseException
     */
    public Date getTemperatureTimeAsDate() throws ParseException {
        return formatter.parse(temperatureTime);
    }

    /**
     * @return the temperatureTime
     * @throws ParseException
     */
    public String getTemperatureTimeAsString() {
        return temperatureTime;
    }

    /**
     * @return the nominalValue
     */
    public Float getNominalValue() {
        return nominalValue;
    }

    /**
     * @return the nominalValueTime
     * @throws ParseException
     */
    public Date getNominalValueTimeAsDate() throws ParseException {
        return formatter.parse(nominalValueTime);
    }

    /**
     * @return the nominalValueTime
     */
    public String getNominalValueTimeAsString() {
        return nominalValueTime;
    }

    /**
     * @return the controlValue
     */
    public Float getControlValue() {
        return controlValue;
    }

    /**
     * @return the controlValueTime
     * @throws ParseException
     */
    public Date getControlValueTimeAsDate() throws ParseException {
        return formatter.parse(controlValueTime);
    }

    /**
     * @return the controlValueTime
     */
    public String getControlValueTimeAsString() {
        return controlValueTime;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TemperatureControlStatus [controlState=" + controlState + ", operationMode=" + operationMode
                + ", temperature=" + temperature + ", temperatureTime=" + temperatureTime + ", nominalValue="
                + nominalValue + ", nominalValueTime=" + nominalValueTime + ", controlValue=" + controlValue
                + ", controlValueTime=" + controlValueTime + "]";
    }
}
