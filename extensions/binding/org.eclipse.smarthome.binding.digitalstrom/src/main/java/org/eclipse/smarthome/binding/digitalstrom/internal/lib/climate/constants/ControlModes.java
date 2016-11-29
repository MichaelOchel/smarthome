package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.constants;

/**
 * The {@link ControlModes} contains all digitalSTROM heating control modes.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public enum ControlModes {

    OFF((short) 0, "off"),
    PID_CONTROL((short) 1, "pid-control"),
    ZONE_FOLLOWER((short) 2, "zone-follower"),
    FIXED_VALUE((short) 3, "fixed-value"),
    MANUAL((short) 4, "manual");

    private final Short ID;
    private final String KEY;

    private final static ControlModes[] CONTROL_MODES = new ControlModes[5];

    static {
        CONTROL_MODES[0] = OFF;
        CONTROL_MODES[1] = PID_CONTROL;
        CONTROL_MODES[2] = ZONE_FOLLOWER;
        CONTROL_MODES[3] = FIXED_VALUE;
        CONTROL_MODES[4] = MANUAL;
    }

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

    public static ControlModes getControlMode(short id) {
        try {
            return CONTROL_MODES[id];
        } catch (IndexOutOfBoundsException e) {
            // throw new IllegalArgumentException("The id dosen not exist, id have to be between 0 and 4.");
            return null;
        }
    }
}
