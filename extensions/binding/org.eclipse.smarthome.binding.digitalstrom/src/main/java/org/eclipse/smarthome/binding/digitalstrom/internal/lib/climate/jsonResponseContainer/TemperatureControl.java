package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonObject;

/**
 * @author Michael
 *
 */
public abstract class TemperatureControl extends BaseZoneIdentifier {

    protected String controlDSUID;
    protected Short controlMode;
    protected Boolean isConfigured;

    public TemperatureControl(JsonObject jObject, Integer zoneID, String zoneName) {
        super(zoneID, zoneName);
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
        }
    }

    public TemperatureControl(JsonObject jObject) {
        super(jObject);
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
        }
    }

    /**
     * Returns the dSUID of the control sensor for heating of the zone.
     *
     * @return the controlDSUID
     */
    public String getControlDSUID() {
        return controlDSUID;
    }

    /**
     * Returns controlMode for heating of the zone.
     *
     * @return the controlMode
     */
    public Short getControlMode() {
        return controlMode;
    }

    /**
     * Returns true, if heating for this zone is configured, otherwise false.
     *
     * @return the isConfigured
     */
    public Boolean getIsConfigured() {
        return isConfigured;
    }

}
