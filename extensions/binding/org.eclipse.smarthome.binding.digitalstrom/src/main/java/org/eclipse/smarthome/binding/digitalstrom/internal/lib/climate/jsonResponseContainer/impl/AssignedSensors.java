package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.dataTypes.AssignSensorType;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.BaseZoneIdentifier;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.SensorEnum;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AssignedSensors extends BaseZoneIdentifier {

    private List<AssignSensorType> sensors = null;

    public AssignedSensors(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.ID.getKey()) != null) {
            this.zoneID = jObject.get(JSONApiResponseKeysEnum.ID.getKey()).getAsInt();
        }
        if (jObject.get(JSONApiResponseKeysEnum.NAME.getKey()) != null) {
            this.zoneName = jObject.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
        }
        init(jObject);
    }

    public AssignedSensors(JsonObject jObject, Integer zoneID, String zoneName) {
        this.zoneID = zoneID;
        this.zoneName = zoneName;
        init(jObject);
    }

    private void init(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.SENSORS.getKey()) != null
                && jObject.get(JSONApiResponseKeysEnum.SENSORS.getKey()).isJsonArray()) {
            JsonArray jArray = jObject.get(JSONApiResponseKeysEnum.SENSORS.getKey()).getAsJsonArray();
            if (jArray.size() != 0) {
                sensors = new LinkedList<AssignSensorType>();
                Iterator<JsonElement> iter = jArray.iterator();
                while (iter.hasNext()) {
                    JsonObject assignedSensor = iter.next().getAsJsonObject();
                    Short sensorType = null;
                    String meterDSUID = null;
                    if (assignedSensor.get(JSONApiResponseKeysEnum.SENSOR_TYPE.getKey()) != null) {
                        sensorType = assignedSensor.get(JSONApiResponseKeysEnum.SENSOR_TYPE.getKey()).getAsShort();
                    }
                    if (assignedSensor.get(JSONApiResponseKeysEnum.DSUID_LOWER_CASE.getKey()) != null) {
                        meterDSUID = assignedSensor.get(JSONApiResponseKeysEnum.DSUID_LOWER_CASE.getKey())
                                .getAsString();
                    }
                    sensors.add(new AssignSensorType(SensorEnum.getSensor(sensorType), meterDSUID));
                }
            }
        }
    }

    /**
     *
     * @return
     */
    public List<SensorEnum> getAssignedZoneSensorTypes() {
        List<SensorEnum> sensorTypes = new LinkedList<SensorEnum>();
        if (sensors != null) {
            for (AssignSensorType aSensorValue : sensors) {
                sensorTypes.add(aSensorValue.getSensorType());
            }
        }
        return sensorTypes;
    }

    /**
     *
     * @return
     */
    public List<AssignSensorType> getAssignedSensorTypes() {
        return sensors;
    }

    /**
     *
     * @param sensorType
     * @return
     */
    public AssignSensorType getAssignedSensorType(SensorEnum sensorType) {
        if (sensorType != null && sensors != null) {
            for (AssignSensorType aSensorValue : sensors) {
                if (aSensorValue.getSensorType().equals(sensorType)) {
                    return aSensorValue;
                }
            }
        }
        return null;
    }

    /**
     *
     * @param sensorType
     * @return
     */
    public boolean existSensorType(SensorEnum sensorType) {
        return getAssignedSensorType(sensorType) != null;
    }

    /**
     *
     * @return
     */
    public boolean existsAssignedSensors() {
        return sensors != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AssignedSensors [sensors=" + sensors + "]";
    }

}
