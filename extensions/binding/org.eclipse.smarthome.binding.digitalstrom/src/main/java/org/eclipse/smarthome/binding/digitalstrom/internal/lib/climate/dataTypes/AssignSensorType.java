package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.dataTypes;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.SensorEnum;

public class AssignSensorType {

    private final SensorEnum SENSOR_TYPE;
    private final String DSUID;

    /**
     * @param sensorType
     * @param dSUID
     */
    public AssignSensorType(SensorEnum sensorType, String dSUID) {
        SENSOR_TYPE = sensorType;
        DSUID = dSUID;
    }

    /**
     * @return the sENSOR_TYPE
     */
    public SensorEnum getSensorType() {
        return SENSOR_TYPE;
    }

    /**
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

}
