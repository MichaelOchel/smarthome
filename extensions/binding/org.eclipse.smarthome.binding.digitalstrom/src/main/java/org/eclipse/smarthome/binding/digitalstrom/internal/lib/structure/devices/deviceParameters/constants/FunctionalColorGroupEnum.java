/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * The {@link FunctionalColorGroupEnum} contains all digitalSTROM functional color groups.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 * @see <a href="http://developer.digitalstrom.org/Architecture/ds-basics.pdf">ds-basics.pdf,
 *      "Table 1: digitalSTROM functional groups and their colors", page 9 [04.09.2015]</a>
 */
public enum FunctionalColorGroupEnum {
    /*
     * | Number | Name | Color | Function |
     * --------------------------------------------------------------------------------------
     * | 1 | Lights | Yellow | Room lights |
     * | 2 | Blinds | Gray | Blinds or shades outside |
     * | 12 | Curtains | Gray | Curtains and blinds inside |
     * | 3 | Heating | Blue | Heating |
     * | 9 | Cooling | Blue | Cooling |
     * | 10 | Ventilation | Blue | Ventilation |
     * | 11 | Window | Blue | Window |
     * | 48 | Temperature Control | Blue | Single room temperature control |
     * | 4 | Audio | Cyan | Playing music or radio |
     * | 5 | Video | Magenta | TV, Video |
     * | 8 | Joker | Black | Configurable behaviour |
     * | n/a | Single Device | White | Various, individual per device |
     * | n/a | Security | Red | Security related functions, Alarms |
     * | n/a | Access | Green | Access related functions, door bell |
     *
     */
    YELLOW(Lists.newArrayList((short) 1)),
    GREY(Lists.newArrayList((short) 2, (short) 12)),
    BLUE(Lists.newArrayList((short) 3, (short) 9, (short) 10, (short) 11, (short) 48)),
    CYAN(Lists.newArrayList((short) 4)),
    MAGENTA(Lists.newArrayList((short) 5)),
    BLACK(Lists.newArrayList((short) 8)),
    WHITE(Lists.newArrayList((short) -1)),
    RED(Lists.newArrayList((short) -2)),
    GREEN(Lists.newArrayList((short) -3));

    private final List<Short> colorGroup;

    static final HashMap<Short, FunctionalColorGroupEnum> colorGroups = new HashMap<Short, FunctionalColorGroupEnum>();

    static {
        for (FunctionalColorGroupEnum colorGroup : FunctionalColorGroupEnum.values()) {
            for (Short colorGroupID : colorGroup.getFunctionalColorGroup()) {
                colorGroups.put(colorGroupID, colorGroup);
            }
        }
    }

    /**
     * Returns true, if contains the given functional color group id in digitalSTROM exits, otherwise false.
     *
     * @param functionalColorGroupID to contains
     * @return true, if contains
     */
    public static boolean containsColorGroup(Short functionalColorGroupID) {
        return colorGroups.keySet().contains(functionalColorGroupID);
    }

    /**
     * Returns the {@link FunctionalColorGroupEnum} of the given color id.
     *
     * @param functionalColorGroupID of the {@link FunctionalColorGroupEnum}
     * @return {@link FunctionalColorGroupEnum} of the id
     */
    public static FunctionalColorGroupEnum getColorGroup(Short functionalColorGroupID) {
        return colorGroups.get(functionalColorGroupID);
    }

    private FunctionalColorGroupEnum(List<Short> functionalColorGroupID) {
        this.colorGroup = Lists.newArrayList(functionalColorGroupID);
    }

    /**
     * Returns the functional color group id's as {@link List} of this {@link FunctionalColorGroupEnum}.
     *
     * @return functional color group id's
     */
    public List<Short> getFunctionalColorGroup() {
        return colorGroup;
    }

}
