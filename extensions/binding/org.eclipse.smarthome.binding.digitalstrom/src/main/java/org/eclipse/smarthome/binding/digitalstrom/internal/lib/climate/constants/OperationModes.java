package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.constants;

/**
 * The {@link OperationModes} contains all digitalSTROM heating operation states.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public enum OperationModes {

    OFF((short) 0, "Off"),
    COMFORT((short) 1, "Comfort"),
    ECONEMY((short) 2, "Econemy"),
    NOT_USED((short) 3, "NotUsed"),
    NIGHT((short) 4, "Night"),
    HOLLYDAY((short) 5, "Holliday"),
    COOLING((short) 6, "Cooling"),
    COOLING_OFF((short) 7, "CoolingOff");

    private final Short ID;
    private final String KEY;

    private final static OperationModes[] OPERATION_MODES = new OperationModes[OperationModes.values().length];

    static {
        for (OperationModes operationMode : OperationModes.values()) {
            OPERATION_MODES[operationMode.ID] = operationMode;
        }
    }

    public static OperationModes getOperationMode(short id) {
        try {
            return OPERATION_MODES[id];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private OperationModes(short id, String key) {
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
