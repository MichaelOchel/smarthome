/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
    EXBACKUP((short) 2, "exbackup"),
    EMERGENCY((short) 3, "emergency");

    private final Short ID;
    private final String KEY;
    private final static ControlStates[] CONTROL_STATES = new ControlStates[ControlStates.values().length];

    static {
        for (ControlStates controlState : ControlStates.values()) {
            CONTROL_STATES[controlState.ID] = controlState;
        }
    }

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

    /**
     * Returns the {@link ControlStates} of the given control state id.
     *
     * @param id of the control state
     * @return control state
     */
    public static ControlStates getControlState(short id) {
        try {
            return CONTROL_STATES[id];
        } catch (IndexOutOfBoundsException e) {
            // throw new IllegalArgumentException("The id dosen not exist, id have to be between 0 and 4.");
            return null;
        }
    }
}
