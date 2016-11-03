package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.constants.OperationModes;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.BaseZoneIdentifier;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.TemperatureControl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonObject;

/**
 * The {@link TemperatureControlValues} acts as container for the digitalSTROM json-method
 * <i>getTemperatureControlValues</i>. So the {@link TemperatureControlValues} contains all temperature
 * control values information of a zone.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class TemperatureControlValues extends BaseZoneIdentifier {

    private HashMap<OperationModes, Float> temperatureControlValues;
    private String controlDSUID;
    private Boolean isConfigured;

    /**
     * Creates a new {@link TemperatureControlValues} through the {@link JsonObject} which will be returned by an
     * apartment call.
     *
     * @param jObject must not be null
     */
    public TemperatureControlValues(JsonObject jObject) {
        super(jObject);
        init(jObject);
    }

    /**
     * Creates a new {@link TemperatureControlValues} through the {@link JsonObject} which will be returned by an zone
     * call.<br>
     * Because of zone calls does not include a zoneID or zoneName in the json response, the zoneID and zoneName have to
     * be handed over the constructor.
     *
     * @param jObject must not be null
     * @param zoneID must not be null
     * @param zoneName can be null
     */
    public TemperatureControlValues(JsonObject jObject, Integer zoneID, String zoneName) {
        super(zoneID, zoneName);
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
     * @see TemperatureControl#getControlDSUID()
     * @return the controlDSUID
     */
    public String getControlDSUID() {
        return controlDSUID;
    }

    /**
     * @see TemperatureControl#getIsConfigured()
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
