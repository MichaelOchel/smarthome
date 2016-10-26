package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl;

import java.util.Iterator;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.BaseSensorValues;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.ZoneIdentifier;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SensorValues extends BaseSensorValues implements ZoneIdentifier {

    private Integer zoneID;
    private String zoneName;

    public SensorValues(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.ID.getKey()) != null) {
            this.zoneID = jObject.get(JSONApiResponseKeysEnum.ID.getKey()).getAsInt();
        }
        if (jObject.get(JSONApiResponseKeysEnum.NAME.getKey()) != null) {
            this.zoneName = jObject.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
        }
        init(jObject);
    }

    public SensorValues(JsonObject jObject, Integer zoneID, String zoneName) {
        this.zoneID = zoneID;
        this.zoneName = zoneName;
        init(jObject);
    }

    private void init(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.VALUES.getKey()).isJsonArray()) {
            JsonArray jArray = jObject.get(JSONApiResponseKeysEnum.VALUES.getKey()).getAsJsonArray();
            if (jArray.size() != 0) {
                Iterator<JsonElement> iter = jArray.iterator();
                while (iter.hasNext()) {
                    JsonObject cachedSensorValue = iter.next().getAsJsonObject();
                    super.addSensorValue(cachedSensorValue, false);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SensorValues [zoneID=" + zoneID + ", zoneName=" + zoneName + ", " + super.toString() + "]";
    }

    @Override
    public Integer getZoneID() {
        return zoneID;
    }

    @Override
    public String getZoneName() {
        return zoneName;
    }

}
