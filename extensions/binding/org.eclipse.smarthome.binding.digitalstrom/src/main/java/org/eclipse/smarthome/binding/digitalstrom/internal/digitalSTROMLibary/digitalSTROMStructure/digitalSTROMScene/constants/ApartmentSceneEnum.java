/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.constants;

import java.util.HashMap;

/**
 * The {@link ApartmentSceneEnum} lists all group independent scenes from digitalSTROM which are callable at the
 * digitalSTROM web interface.
 *
 * @author Alexander Betker
 * @see digitalSTROM wiki on http://redmine.digitalstrom.org/projects/dss/wiki/Scene_table
 *
 * @author Michael Ochel - add new scenes and deleted scenes which are show as zone scenes in the dss-web-interface
 * @author Mathias Siegele - add new scenes and deleted scenes which are show as zone scenes in the dss-web-interface
 * @see http://developer.digitalstrom.org/Architecture/ds-basics.pdf , Table 35: Group independent activities and scene
 *      command values, page 47 and dss-web-interface
 */
public enum ApartmentSceneEnum implements Scene {

    // DEEP_OFF (68),
    ENERGY_OVERLOAD(66),
    // STANDBY (67),
    ZONE_ACTIVE(75),
    ALARM_SIGNAL(74),
    AUTO_STANDBY(64),
    ABSENT(72),
    PRESENT(71),
    // SLEEPING (69),
    // WAKEUP (70),
    DOOR_BELL(73),
    PANIC(65),
    FIRE(76),
    ALARM_1(74),
    ALARM_2(83),
    ALARM_3(84),
    ALARM_4(85),
    WIND(86),
    NO_WIND(87),
    RAIN(88),
    NO_RAIN(89),
    HAIL(90),
    NO_HAIL(91);

    private final int sceneNumber;

    static final HashMap<Integer, ApartmentSceneEnum> apartmentScenes = new HashMap<Integer, ApartmentSceneEnum>();

    static {
        for (ApartmentSceneEnum as : ApartmentSceneEnum.values()) {
            apartmentScenes.put(as.getSceneNumber(), as);
        }
    }

    private ApartmentSceneEnum(int sceneNumber) {
        this.sceneNumber = sceneNumber;
    }

    /**
     * Returns the apartment scene from the given scene number.
     *
     * @param sceneNumber
     * @return apartment scene
     */
    public static ApartmentSceneEnum getApartmentScene(int sceneNumber) {
        return apartmentScenes.get(sceneNumber);
    }

    @Override
    public int getSceneNumber() {
        return this.sceneNumber;
    }

}
