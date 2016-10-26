package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.constants.OperationModes;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.BaseZoneIdentifier;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonObject;

public class TemperatureControlValues extends BaseZoneIdentifier {

    private HashMap<OperationModes, Float> temperatureControlValues;
    private String controlDSUID;
    private Boolean isConfigured;

    public TemperatureControlValues(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.ID.getKey()) != null) {
            this.zoneID = jObject.get(JSONApiResponseKeysEnum.ID.getKey()).getAsInt();
        }
        if (jObject.get(JSONApiResponseKeysEnum.NAME.getKey()) != null) {
            this.zoneName = jObject.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
        }
        init(jObject);
    }

    public TemperatureControlValues(JsonObject jObject, Integer zoneID, String zoneName) {
        this.zoneID = zoneID;
        this.zoneName = zoneName;
        init(jObject);
    }

    private void init(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.IS_CONFIGURED.getKey()) != null) {
            this.isConfigured = jObject.get(JSONApiResponseKeysEnum.IS_CONFIGURED.getKey()).getAsBoolean();
        }
        if (isConfigured) {
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_DSUID.getKey()) != null) {
                this.controlDSUID = jObject.get(JSONApiResponseKeysEnum.CONTROL_DSUID.getKey()).getAsString();
            }
            temperatureControlValues = new HashMap<OperationModes, Float>(OperationModes.values().length);
            for (OperationModes opMode : OperationModes.values()) {
                if (jObject.get(opMode.getKey()) != null) {
                    temperatureControlValues.put(opMode, jObject.get(opMode.getKey()).getAsFloat());
                }
            }
        }
    }

    /**
     * @return the controlDSUID
     */
    public String getControlDSUID() {
        return controlDSUID;
    }

    /**
     * @return the isConfigured
     */
    public Boolean getIsConfigured() {
        return isConfigured;
    }

    /**
     *
     * @param operationMode
     * @return
     */
    public Float getTemperation(OperationModes operationMode) {
        return temperatureControlValues.get(operationMode);
    }

    /**
     *
     * @return
     */
    public Set<OperationModes> getOperationModes() {
        return temperatureControlValues.keySet();
    }

    /**
     *
     * @return
     */
    public HashMap<OperationModes, Float> getTemperatureControlValues() {
        return temperatureControlValues;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TemperatureControlValues [temperatureControlValues=" + temperatureControlValues + ", controlDSUID="
                + controlDSUID + ", isConfigured=" + isConfigured + "]";
    }
}
