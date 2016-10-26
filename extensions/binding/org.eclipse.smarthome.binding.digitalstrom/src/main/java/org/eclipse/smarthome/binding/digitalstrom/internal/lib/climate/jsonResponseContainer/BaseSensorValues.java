package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.dataTypes.CachedSensorValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.SensorEnum;

import com.google.gson.JsonObject;

public abstract class BaseSensorValues {

    private List<CachedSensorValue> sensorValues = null;

    protected void addSensorValue(JsonObject jObject, boolean outdoor) {
        if (jObject.get(JSONApiResponseKeysEnum.TEMPERATION_VALUE.getKey()) != null) {
            SensorEnum sensorType = SensorEnum.TEMPERATURE_INDOORS;
            if (outdoor) {
                sensorType = SensorEnum.TEMPERATURE_OUTDOORS;
            }
            addSensorValue(new CachedSensorValue(sensorType,
                    jObject.get(JSONApiResponseKeysEnum.TEMPERATION_VALUE.getKey()).getAsFloat(),
                    jObject.get(JSONApiResponseKeysEnum.TEMPERATION_VALUE_TIME.getKey()).getAsString()));
        }
        if (jObject.get(JSONApiResponseKeysEnum.HUMIDITY_VALUE.getKey()) != null) {
            SensorEnum sensorType = SensorEnum.RELATIVE_HUMIDITY_INDOORS;
            if (outdoor) {
                sensorType = SensorEnum.RELATIVE_HUMIDITY_OUTDOORS;
            }
            addSensorValue(new CachedSensorValue(sensorType,
                    jObject.get(JSONApiResponseKeysEnum.HUMIDITY_VALUE.getKey()).getAsFloat(),
                    jObject.get(JSONApiResponseKeysEnum.HUMIDITY_VALUE_TIME.getKey()).getAsString()));
        }
        if (jObject.get(JSONApiResponseKeysEnum.CO2_CONCENTRATION_VALUE.getKey()) != null) {
            addSensorValue(new CachedSensorValue(SensorEnum.CARBONE_DIOXIDE,
                    jObject.get(JSONApiResponseKeysEnum.CO2_CONCENTRATION_VALUE.getKey()).getAsFloat(),
                    jObject.get(JSONApiResponseKeysEnum.CO2_CONCENTRATION_VALUE_TIME.getKey()).getAsString()));
        }
        if (jObject.get(JSONApiResponseKeysEnum.BRIGHTNESS_VALUE.getKey()) != null) {
            SensorEnum sensorType = SensorEnum.BRIGHTNESS_INDOORS;
            if (outdoor) {
                sensorType = SensorEnum.BRIGHTNESS_OUTDOORS;
            }
            addSensorValue(new CachedSensorValue(sensorType,
                    jObject.get(JSONApiResponseKeysEnum.BRIGHTNESS_VALUE.getKey()).getAsFloat(),
                    jObject.get(JSONApiResponseKeysEnum.BRIGHTNESS_VALUE_TIME.getKey()).getAsString()));
        }
    }

    private void addSensorValue(CachedSensorValue cachedSensorValue) {
        if (sensorValues == null) {
            sensorValues = new LinkedList<CachedSensorValue>();
            sensorValues.add(cachedSensorValue);
        } else {
            sensorValues.add(cachedSensorValue);
        }
    }

    public List<SensorEnum> getAvailableSensorTypes() {
        List<SensorEnum> sensorTypes = new LinkedList<SensorEnum>();
        if (sensorValues != null) {
            for (CachedSensorValue cSensorValue : sensorValues) {
                sensorTypes.add(cSensorValue.getSensorType());
            }
        }
        return sensorTypes;
    }

    public List<CachedSensorValue> getCachedSensorValues() {
        return sensorValues;
    }

    public CachedSensorValue getCachedSensorValue(SensorEnum sensorType) {
        if (sensorType != null && sensorValues != null) {
            for (CachedSensorValue cSensorValue : sensorValues) {
                if (cSensorValue.getSensorType().equals(sensorType)) {
                    return cSensorValue;
                }
            }
        }
        return null;
    }

    public boolean existSensorValue(SensorEnum sensorType) {
        return getCachedSensorValue(sensorType) != null;
    }

    public boolean existSensorValues() {
        return sensorValues != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SensorValues [sensorValues=" + sensorValues + "]";
    }
}
