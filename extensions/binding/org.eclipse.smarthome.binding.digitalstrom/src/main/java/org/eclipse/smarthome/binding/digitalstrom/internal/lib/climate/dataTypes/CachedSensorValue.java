package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.dataTypes;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.SensorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CachedSensorValue} holds a read sensor value. For that the {@link CachedSensorValue} includes the sensor
 * type, sensor value and a the timestamp.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class CachedSensorValue {

    private Logger logger = LoggerFactory.getLogger(CachedSensorValue.class);

    private final SensorEnum SENSOR_TYPE;
    private final Float SENSOR_VALUE;
    private final String TIMESTAMP;

    /**
     * Create a new {@link CachedSensorValue}.
     *
     * @param sensorType must not be null
     * @param sensorValue must not be null
     * @param timestamp must not be null
     */
    public CachedSensorValue(SensorEnum sensorType, Float sensorValue, String timestamp) {
        SENSOR_TYPE = sensorType;
        SENSOR_VALUE = sensorValue;
        TIMESTAMP = timestamp;
    }

    /**
     * Returns the sensor type as {@link SensorEnum}.
     *
     * @return the sensorType
     */
    public SensorEnum getSensorType() {
        return SENSOR_TYPE;
    }

    /**
     * Returns the sensor value.
     *
     * @return the sensorValue
     */
    public Float getSensorValue() {
        return SENSOR_VALUE;
    }

    /**
     * Returns the timestamp as {@link String}.
     *
     * @return the timestamp
     */
    public String getTimestamp() {
        return TIMESTAMP;
    }

    /**
     * Returns the time stamp as {@link Date}.
     *
     * @return the timeStamp
     */
    public Date getTimestampAsDate() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SS");
        try {
            return formatter.parse(TIMESTAMP);
        } catch (ParseException e) {
            logger.error("A ParseException occurred by parsing date string: " + TIMESTAMP, e);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CachedSensorValue [SENSOR_TYPE=" + SENSOR_TYPE + ", SENSOR_VALUE=" + SENSOR_VALUE + ", TIMESTAMP="
                + TIMESTAMP + "]";
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof CachedSensorValue) {
            return this.toString().equals(obj.toString());
        }
        return false;
    }
}
