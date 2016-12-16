package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.dataTypes;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.SensorEnum;

/**
 * The {@link AssignSensorType} assigns a sensor type of a zone to the dSUID of the sensor-device.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class AssignSensorType {

    private final SensorEnum SENSOR_TYPE;
    private final String DSUID;

    /**
     * Create a new {@link AssignSensorType}.
     *
     * @param sensorType
     * @param dSUID
     */
    public AssignSensorType(SensorEnum sensorType, String dSUID) {
        SENSOR_TYPE = sensorType;
        DSUID = dSUID;
    }

    /**
     * Returns the sensor type as {@link SensorEnum}.
     *
     * @return the sensor type
     */
    public SensorEnum getSensorType() {
        return SENSOR_TYPE;
    }

    /**
     * Returns the dSUID of the assign sensor-device.
     *
     * @return the dSUID
     */
    public String getDSUID() {
        return DSUID;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AssignSensorType [SENSOR_TYPE=" + SENSOR_TYPE + ", dSUID=" + DSUID + "]";
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AssignSensorType) {
            return this.toString().equals(obj.toString());
        }
        return false;
    }

}
