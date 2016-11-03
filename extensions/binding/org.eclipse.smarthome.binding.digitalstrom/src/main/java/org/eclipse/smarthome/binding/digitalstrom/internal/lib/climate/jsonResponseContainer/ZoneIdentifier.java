package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer;

/**
 * The {@link ZoneIdentifier} can be implement to identify a zone.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface ZoneIdentifier {

    /**
     * Returns the zoneID of this zone.
     *
     * @return the zoneID
     */
    public Integer getZoneID();

    /**
     * Returns the zoneName of this zone.
     *
     * @return the zoneName
     */
    public String getZoneName();
}
