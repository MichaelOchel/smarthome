package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.dataTypes;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.SensorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedSensorValue {

    private Logger logger = LoggerFactory.getLogger(CachedSensorValue.class);

    private final SensorEnum SENSOR_TYPE;
    private final Float SENSOR_VALUE;
    private final String TIMESTAMP;

    public CachedSensorValue(SensorEnum sensorType, Float sensorValue, String timeStamp) {
        SENSOR_TYPE = sensorType;
        SENSOR_VALUE = sensorValue;
        TIMESTAMP = timeStamp;
    }

    /**
     * @return the sensorType
     */
    public SensorEnum getSensorType() {
        return SENSOR_TYPE;
    }

    /**
     * @return the sensorValue
     */
    public Float getSensorValue() {
        return SENSOR_VALUE;
    }

    /**
     * @return the timeStamp
     */
    public String getTimeStamp() {
        return TIMESTAMP;
    }

    /**
     * @return the timeStamp
     */
    public Date getTimeStampAsDate() {
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
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((SENSOR_TYPE == null) ? 0 : SENSOR_TYPE.hashCode());
        result = prime * result + ((SENSOR_VALUE == null) ? 0 : SENSOR_VALUE.hashCode());
        result = prime * result + ((TIMESTAMP == null) ? 0 : TIMESTAMP.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CachedSensorValue other = (CachedSensorValue) obj;
        if (SENSOR_TYPE == null) {
            if (other.SENSOR_TYPE != null) {
                return false;
            }
        } else if (!SENSOR_TYPE.equals(other.SENSOR_TYPE)) {
            return false;
        }
        if (SENSOR_VALUE == null) {
            if (other.SENSOR_VALUE != null) {
                return false;
            }
        } else if (!SENSOR_VALUE.equals(other.SENSOR_VALUE)) {
            return false;
        }
        if (TIMESTAMP == null) {
            if (other.TIMESTAMP != null) {
                return false;
            }
        } else if (!TIMESTAMP.equals(other.TIMESTAMP)) {
            return false;
        }
        return true;
    }
}
