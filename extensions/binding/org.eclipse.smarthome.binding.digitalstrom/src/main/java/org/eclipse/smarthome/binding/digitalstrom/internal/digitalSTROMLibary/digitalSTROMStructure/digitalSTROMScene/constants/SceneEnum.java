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
 * The {@link SceneEnum} lists all available scenes in digitalSTROM.
 *
 * @author Alexander Betker
 * @since 1.3.0
 * @version digitalSTROM-API 1.14.5
 *
 * @author Michael Ochel - add EVENT_NAME and missing java-doc
 * @author Mathias Siegele - add EVENT_NAME and missing java-doc
 *
 * @see http://developer.digitalstrom.org/Architecture/ds-basics.pdf appendix B, page 44
 */
public enum SceneEnum implements Scene {

    /* Area scene commands */
    AREA_1_OFF(1), // Set output value to Preset Area 1 Off (Default: Off)
    AREA_1_ON(6), // Set output value to Preset Area 1 On (Default: On)
    AREA_1_INCREMENT(43), // Initial command to increment output value
    AREA_1_DECREMENT(42), // Initial command to decrement output value
    AREA_1_STOP(52), // Stop output value change at current position
    AREA_STEPPING_CONTINUE(10), // Next step to increment or decrement

    AREA_2_OFF(2), // Set output value to Area 2 Off (Default: Off)
    AREA_2_ON(7), // Set output value to Area 2 On (Default: On)
    AREA_2_INCREMENT(45), // Initial command to increment output value
    AREA_2_DECREMENT(44), // Initial command to decrement output value
    AREA_2_STOP(53), // Stop output value change at current position

    AREA_3_OFF(3), // Set output value to Area 3 Off (Default: Off)
    AREA_3_ON(8), // Set output value to Area 3 On (Default: On)
    AREA_3_INCREMENT(47), // Initial command to increment output value
    AREA_3_DECREMENT(46), // Initial command to decrement output value
    AREA_3_STOP(54), // Stop output value change at current position

    AREA_4_OFF(4), // Set output value to Area 4 Off (Default: Off)
    AREA_4_ON(9), // Set output value to Area 4 On (Default: On)
    AREA_4_INCREMENT(49), // Initial command to increment output value
    AREA_4_DECREMENT(48), // Initial command to decrement output value
    AREA_4_STOP(55), // Stop output value change at current position

    /* local pushbutton scene commands */
    DEVICE_ON(51), // Local on
    DEVICE_OFF(50), // Local off
    DEVICE_STOP(15), // Stop output value change at current position

    /* special scene commands */
    MINIMUM(13), // Minimum output value
    MAXIMUM(14), // Maximum output value
    STOP(15), // Stop output value change at current position
    AUTO_OFF(40), // slowly fade down to off

    /* stepping scene commands */
    INCREMENT(11), // Increment output value
    DECREMENT(12), // Decrement output value

    /* presets */
    PRESET_0(0), // Set output value to Preset 0 (Default: Off)
    PRESET_1(5), // Set output value to Preset 1 (Default: On)
    PRESET_2(17), // Set output value to Preset 2
    PRESET_3(18), // Set output value to Preset 3
    PRESET_4(19), // Set output value to Preset 4

    PRESET_10(32), // Set output value to Preset 10 (Default: Off)
    PRESET_11(33), // Set output value to Preset 11 (Default: On)
    PRESET_12(20), // Set output value to Preset 12
    PRESET_13(21), // Set output value to Preset 13
    PRESET_14(22), // Set output value to Preset 14

    PRESET_20(34), // Set output value to Preset 20 (Default: Off)
    PRESET_21(35), // Set output value to Preset 21 (Default: On)
    PRESET_22(23), // Set output value to Preset 22
    PRESET_23(24), // Set output value to Preset 23
    PRESET_24(25), // Set output value to Preset 24

    PRESET_30(36), // Set output value to Preset 30 (Default: Off)
    PRESET_31(37), // Set output value to Preset 31 (Default: On)
    PRESET_32(26), // Set output value to Preset 32
    PRESET_33(27), // Set output value to Preset 33
    PRESET_34(28), // Set output value to Preset 34

    PRESET_40(38), // Set output value to Preset 40 (Default: Off)
    PRESET_41(39), // Set output value to Preset 41 (Default: On)
    PRESET_42(29), // Set output value to Preset 42
    PRESET_43(30), // Set output value to Preset 43
    PRESET_44(31), // Set output value to Preset 44

    /* Temperature control scenes */
    /*
     * OFF (0),
     * CONFORT (1),
     * ECONEMY (2),
     * NOT_USED (3),
     * NIGHT (4),
     * HOLLYDAY (5),
     */

    /* group independent scene commands */
    DEEP_OFF(68),
    ENERGY_OVERLOAD(66),
    STANDBY(67),
    ZONE_ACTIVE(75),
    ALARM_SIGNAL(74),
    AUTO_STANDBY(64),
    ABSENT(72),
    PRESENT(71),
    SLEEPING(69),
    WAKEUP(70),
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

    static final HashMap<Integer, SceneEnum> digitalstromScenes = new HashMap<Integer, SceneEnum>();

    static {
        for (SceneEnum zs : SceneEnum.values()) {
            digitalstromScenes.put(zs.getSceneNumber(), zs);
        }
    }

    private SceneEnum(int sceneNumber) {
        this.sceneNumber = sceneNumber;
    }

    /**
     * Returns the {@link SceneEnum} for the given scene number.
     *
     * @param sceneNumber
     * @return SceneEnum
     */
    public static SceneEnum getScene(int sceneNumber) {
        return digitalstromScenes.get(sceneNumber);
    }

    /**
     * Returns true if the given scene number contains in digitalSTROM scenes otherwise false.
     *
     * @param sceneNumber
     * @return true if contains otherwise false
     */
    public static boolean containsScene(Integer sceneNumber) {
        return digitalstromScenes.keySet().contains(sceneNumber);
    }

    @Override
    public int getSceneNumber() {
        return this.sceneNumber;
    }

}
