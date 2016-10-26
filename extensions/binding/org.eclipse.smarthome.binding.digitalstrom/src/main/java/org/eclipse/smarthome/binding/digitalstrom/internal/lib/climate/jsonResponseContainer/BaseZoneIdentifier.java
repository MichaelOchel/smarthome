package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer;

public abstract class BaseZoneIdentifier implements ZoneIdentifier {

    protected Integer zoneID;
    protected String zoneName;

    @Override
    public Integer getZoneID() {
        return zoneID;
    }

    @Override
    public String getZoneName() {
        return zoneName;
    }
}
