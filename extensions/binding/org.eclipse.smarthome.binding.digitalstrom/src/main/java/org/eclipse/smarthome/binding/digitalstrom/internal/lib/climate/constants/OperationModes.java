/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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

    /**
     * Returns the {@link OperationModes} of the given operation mode id.
     *
     * @param id of the operation mode
     * @return operation mode
     */
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