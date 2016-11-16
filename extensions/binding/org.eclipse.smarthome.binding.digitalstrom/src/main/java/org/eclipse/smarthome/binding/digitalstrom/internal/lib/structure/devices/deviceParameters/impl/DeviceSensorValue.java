package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.constants.EventResponseEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.SensorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class DeviceSensorValue {

    private Logger logger = LoggerFactory.getLogger(DeviceSensorValue.class);

    private SensorEnum sensorType = null;
    private Short sensorIndex = null;

    private Float floatValue = null;
    private Integer dsValue = null;

    private Date timestamp = null;
    private boolean valid = false;

    public DeviceSensorValue(JsonObject sensorValue) {
        if (sensorValue.get(JSONApiResponseKeysEnum.TYPE.getKey()) != null) {
            sensorType = SensorEnum.getSensor(sensorValue.get(JSONApiResponseKeysEnum.TYPE.getKey()).getAsShort());
        }
        if (sensorValue.get(JSONApiResponseKeysEnum.INDEX.getKey()) != null) {
            sensorIndex = sensorValue.get(JSONApiResponseKeysEnum.INDEX.getKey()).getAsShort();
        }
        if (sensorValue.get(JSONApiResponseKeysEnum.VALID.getKey()) != null) {
            valid = sensorValue.get(JSONApiResponseKeysEnum.VALID.getKey()).getAsBoolean();
        }
        if (sensorValue.get(JSONApiResponseKeysEnum.VALUE.getKey()) != null) {
            floatValue = sensorValue.get(JSONApiResponseKeysEnum.VALUE.getKey()).getAsFloat();
        }
        if (sensorValue.get(JSONApiResponseKeysEnum.VALUE_DS.getKey()) != null) {
            dsValue = sensorValue.get(JSONApiResponseKeysEnum.VALUE_DS.getKey()).getAsInt();
        }
        if (sensorValue.get(JSONApiResponseKeysEnum.TIMESTAMP.getKey()) != null) {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            try {
                timestamp = formatter.parse(sensorValue.get(JSONApiResponseKeysEnum.TIMESTAMP.getKey()).getAsString());
            } catch (ParseException e) {
                logger.error("A ParseException occurred by parsing date string: "
                        + sensorValue.get(JSONApiResponseKeysEnum.TIMESTAMP.getKey()).getAsString(), e);
            }
        }
    }

    public DeviceSensorValue(Map<EventResponseEnum, String> eventPropertie) {
        if (eventPropertie.get(EventResponseEnum.SENSOR_VALUE_FLOAT) != null) {
            floatValue = Float.parseFloat(eventPropertie.get(EventResponseEnum.SENSOR_VALUE_FLOAT));
        }
        if (eventPropertie.get(EventResponseEnum.SENSOR_TYPE) != null) {
            sensorType = SensorEnum.getSensor(Short.parseShort(eventPropertie.get(EventResponseEnum.SENSOR_TYPE)));
        }
        if (eventPropertie.get(EventResponseEnum.SENSOR_VALUE) != null) {
            dsValue = Integer.parseInt(eventPropertie.get(EventResponseEnum.SENSOR_VALUE));
        }
        if (eventPropertie.get(EventResponseEnum.SENSOR_INDEX) != null) {
            sensorIndex = Short.parseShort(eventPropertie.get(EventResponseEnum.SENSOR_INDEX));
        }
        timestamp = Date.from(Instant.ofEpochMilli(System.currentTimeMillis()));
        valid = true;
    }

    public DeviceSensorValue(SensorEnum sensorType, Short sensorIndex) {
        this.sensorType = sensorType;
        this.sensorIndex = sensorIndex;
    }

    /**
     * @return the floatValue
     */
    public Float getFloatValue() {
        return floatValue;
    }

    /**
     * @param floatValue the floatValue to set
     */
    public boolean setFloatValue(Float floatValue) {
        if (floatValue > -1) {
            this.floatValue = floatValue;
            if (sensorType.getResolution() != 800) {
                this.dsValue = (int) (floatValue / sensorType.getResolution());
            } else {
                this.dsValue = (int) (800 * Math.log10(floatValue));
            }
            timestamp = Date.from(Instant.ofEpochMilli(System.currentTimeMillis()));
            this.valid = true;
            return true;
        }
        return false;
    }

    /**
     * @return the dsValue
     */
    public Integer getDsValue() {
        return dsValue;
    }

    /**
     * @param dsValue the dsValue to set
     */
    public boolean setDsValue(Integer dsValue) {
        if (dsValue > -1) {
            this.dsValue = dsValue;
            if (sensorType.getResolution() != 800) {
                this.floatValue = dsValue * sensorType.getResolution();
            } else {
                this.floatValue = 10 * (dsValue / sensorType.getResolution());
            }
            timestamp = Date.from(Instant.ofEpochMilli(System.currentTimeMillis()));
            this.valid = true;
            return true;
        }
        return false;
    }

    public boolean setValues(Float floatValue, Integer dSvalue) {
        if (dsValue > -1 && floatValue > -1) {
            this.floatValue = floatValue;
            this.dsValue = dSvalue;
            timestamp = Date.from(Instant.ofEpochMilli(System.currentTimeMillis()));
            this.valid = true;
            return true;
        }
        return false;
    }

    /**
     * @return the sensorType
     */
    public SensorEnum getSensorType() {
        return sensorType;
    }

    /**
     * @return the sensorIndex
     */
    public Short getSensorIndex() {
        return sensorIndex;
    }

    /**
     * @return the timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @return the valid
     */
    public boolean getValid() {
        return valid;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DeviceSensorValue [sensorType=" + sensorType + ", sensorIndex=" + sensorIndex + ", floatValue="
                + floatValue + ", dsValue=" + dsValue + ", timestamp=" + timestamp + ", valid=" + valid + "]";
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
        result = prime * result + ((sensorType == null) ? 0 : sensorType.hashCode());
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
        if (obj.getClass() != getClass()) {
            // if (obj instanceof SensorEnum) {
            // return sensorType.equals(obj);
            // }
            return false;
        }
        DeviceSensorValue other = (DeviceSensorValue) obj;
        if (sensorType != other.sensorType) {
            return false;
        }
        return true;
    }

}
