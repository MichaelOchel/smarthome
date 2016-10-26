package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.constants;

public enum ControlModes {

    OFF((short) 0, "off"),
    PID_CONTROL((short) 1, "pid-control"),
    ZONE_FOLLOWER((short) 2, "zone-follower"),
    FIXED_VALUE((short) 3, "fixed-value"),
    MANUAL((short) 4, "manual");

    private final Short ID;
    private final String KEY;

    private ControlModes(short id, String key) {
        this.ID = id;
        this.KEY = key;
    }

    /**
     * Returns the key of the operation mode.
     *
     * @return key
     */
    public String getKey() {
        return KEY;
    }

    /**
     * Returns the ID of the operation mode.
     *
     * @return ID
     */
    public Short getID() {
        return ID;
    }
}
