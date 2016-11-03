package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonObject;

public abstract class BaseZoneIdentifier implements ZoneIdentifier {

    protected Integer zoneID;
    protected String zoneName;

    public BaseZoneIdentifier(Integer zoneID, String zoneName) {
        this.zoneID = zoneID;
        this.zoneName = zoneName;
    }

    public BaseZoneIdentifier(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.ID.getKey()) != null) {
            this.zoneID = jObject.get(JSONApiResponseKeysEnum.ID.getKey()).getAsInt();
        }
        if (jObject.get(JSONApiResponseKeysEnum.NAME.getKey()) != null) {
            this.zoneName = jObject.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
        }
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
