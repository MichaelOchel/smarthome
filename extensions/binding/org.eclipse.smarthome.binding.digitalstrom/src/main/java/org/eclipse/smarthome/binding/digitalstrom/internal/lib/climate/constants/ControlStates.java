package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.constants;

/**
 * The {@link ControlStates} contains all digitalSTROM heating control states.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public enum ControlStates {

    INTERNAL((short) 0, "internal"),
    EXTERNAL((short) 1, "external"),
    EXBACHUP((short) 2, "exbackup"),
    EMERGENCY((short) 3, "emergency");

    private final Short ID;
    private final String KEY;

    private ControlStates(short id, String key) {
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
