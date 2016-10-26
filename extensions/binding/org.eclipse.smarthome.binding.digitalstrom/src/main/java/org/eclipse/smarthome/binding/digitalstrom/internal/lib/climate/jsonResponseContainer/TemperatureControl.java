package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer;

/**
 * @author Michael
 *
 */
public abstract class TemperatureControl extends BaseZoneIdentifier {

    protected String controlDSUID;
    protected Short controlMode;
    protected Boolean isConfigured;

    /**
     * @return the controlDSUID
     */
    public String getControlDSUID() {
        return controlDSUID;
    }

    /**
     * @return the controlMode
     */
    public Short getControlMode() {
        return controlMode;
    }

    /**
     * @return the isConfigured
     */
    public Boolean getIsConfigured() {
        return isConfigured;
    }

}
