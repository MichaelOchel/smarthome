/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters;

/**
 * Represents a device state update for lights, shades and sensor data.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public interface DeviceStateUpdate {

    // Update types

    // light
    public final static String UPDATE_BRIGHTNESS = "brightness";
    public final static String UPDATE_ON_OFF = "OnOff";
    public final static String UPDATE_BRIGHTNESS_INCREASE = "brightnessIncrese";
    public final static String UPDATE_BRIGHTNESS_DECREASE = "brightnessDecrese";
    public final static String UPDATE_BRIGHTNESS_STOP = "brightnessStop";
    public final static String UPDATE_BRIGHTNESS_MOVE = "brightnessMove";

    // shades
    public final static String UPDATE_SLATPOSITION = "slatposition";
    public final static String UPDATE_SLAT_INCREASE = "slatIncrese";
    public final static String UPDATE_SLAT_DECREASE = "slatDecrese";
    public final static String UPDATE_OPEN_CLOSE = "OpenClose";
    public final static String UPDATE_SLAT_MOVE = "slatMove";
    public final static String UPDATE_SLAT_STOP = "slatStop";

    // sensor data
    public final static String UPDATE_ACTIVE_POWER = "activePower";
    public final static String UPDATE_OUTPUT_CURRENT = "outputCurrent";
    public final static String UPDATE_ELECTRIC_METER = "electricMeter";
    public final static String UPDATE_OUTPUT_VALUE = "outputValue";

    // scene
    /**
     * A scene call can have the value between 0 and 127.
     */
    public final static String UPDATE_CALL_SCENE = "callScene";
    public final static String UPDATE_UNDO_SCENE = "undoScene";
    public final static String UPDATE_SCENE_OUTPUT = "sceneOutput";
    public final static String UPDATE_SCENE_CONFIG = "sceneConfig";

    /**
     * Returns the state update value.
     *
     * NOTE: - For the OnOff-type is the value for off < 0 and for on > 0.
     * - For all Increase- and Decrease-types is the value the new output value.
     * - For SceneCall-type is the value between 0 and 127 a scene call.
     * - For all SceneUndo-types is the value the new output value.
     *
     * @return new state value
     */
    public int getValue();

    /**
     * Returns the state update type.
     *
     * @return state update type
     */
    public String getType();
}
