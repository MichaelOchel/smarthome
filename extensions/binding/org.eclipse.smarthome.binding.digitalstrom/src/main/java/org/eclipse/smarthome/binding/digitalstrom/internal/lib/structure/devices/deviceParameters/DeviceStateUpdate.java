/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.SensorJobExecutor;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.SensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.DeviceBinarayInputEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.SensorEnum;

/**
 * Represents a device state update for lights, joker, shades and sensor data.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface DeviceStateUpdate {

    // Update types
    // TODO: UPDATE löschen, ggf. UPDATE_BRIGHTNESS zu output oder main_output ändern, u.U. UPDATE_SLATPOSITION
    // ebenfalls hierdurch ersetzen
    // light
    public final static String UPDATE_BRIGHTNESS = "brightness";
    public final static String UPDATE_ON_OFF = "OnOff";
    public final static String UPDATE_BRIGHTNESS_INCREASE = "brightnessIncrese";
    public final static String UPDATE_BRIGHTNESS_DECREASE = "brightnessDecrese";
    public final static String UPDATE_BRIGHTNESS_STOP = "brightnessStop";
    public final static String UPDATE_BRIGHTNESS_MOVE = "brightnessMove";

    // shades
    public final static String UPDATE_SLATPOSITION = "slatposition";
    public static final String UPDATE_SLAT_ANGLE = "slatAngle";
    public final static String UPDATE_SLAT_INCREASE = "slatIncrese";
    public final static String UPDATE_SLAT_DECREASE = "slatDecrese";
    public static final String UPDATE_SLAT_ANGLE_INCREASE = "slatAngleIncrese";
    public static final String UPDATE_SLAT_ANGLE_DECREASE = "slatAngleDecrese";
    public final static String UPDATE_OPEN_CLOSE = "openClose";
    public static final String UPDATE_OPEN_CLOSE_ANGLE = "openCloseAngle";
    public final static String UPDATE_SLAT_MOVE = "slatMove";
    public final static String UPDATE_SLAT_STOP = "slatStop";

    // sensor data
    public final static String UPDATE_OUTPUT_VALUE = "outputValue";
    public final static String UPDATE_DEVICE_SENSOR = "deviceSensor-";

    // metering data
    public final static String UPDATE_CIRCUIT_METER = "circuitMeter";

    // binary inputs
    public static final String BINARY_INPUT = "binaryInput-";

    // scene
    /** A scene call can have the value between 0 and 127. */
    public final static String UPDATE_CALL_SCENE = "callScene";
    public final static String UPDATE_UNDO_SCENE = "undoScene";
    public final static String UPDATE_SCENE_OUTPUT = "sceneOutput";
    public final static String UPDATE_SCENE_CONFIG = "sceneConfig";

    // general
    /** command to refresh the output value of an device. */
    public static final String REFRESH_OUTPUT = "refreshOutput";

    // standard values
    public static final int ON_VALUE = 1;
    public static final int OFF_VALUE = -1;

    /**
     * Returns the state update value.
     * <p>
     * <b>NOTE:</b>
     * <ul>
     * <li>For all OnOff-types the value for off is < 0 and for on > 0.</li>
     * <li>For all Increase- and Decrease-types the value is the new output value.</li>
     * <li>For SceneCall-type the value is between 0 and 127.</li>
     * <li>For all SceneUndo-types the value is the new output value.</li>
     * <li>For all SensorUpdate-types will read the sensor data directly, if the value is 0, otherwise a
     * {@link SensorJob} will be added to the {@link SensorJobExecutor}.</li>
     * </ul>
     * </p>
     *
     * @return new state value
     */
    public Object getValue();

    /**
     * Returns the value as {@link Integer}.
     *
     * @return integer value
     * @see #getValue()
     * @throws {@link ClassCastException}, if the value is not in a cast or parsable format.
     */
    public Integer getValueAsInteger();

    /**
     * Returns the value as {@link String}.
     *
     * @return string value
     * @see #getValue()
     */
    public String getValueAsString();

    /**
     * Returns the value as {@link Short}.
     *
     * @return short value
     * @see #getValue()
     * @throws {@link ClassCastException}, if the value is not in a cast or parsable format.
     */
    public Short getValueAsShort();

    /**
     * Returns the value as {@link Float}.
     *
     * @return float value
     * @see #getValue()
     * @throws {@link ClassCastException}, if the value is not in a cast or parsable format.
     */
    public Float getValueAsFloat();

    /**
     * Returns the value as {@link Short[]}.
     *
     * @return short[] value
     * @see #getValue()
     * @throws {@link ClassCastException}, if the value is not in a cast or parsable format.
     */
    public Short[] getValueAsShortArray();

    /**
     * Returns the state update type.
     *
     * @return state update type
     */
    public String getType();

    /**
     * Returns the update type as {@link SensorEnum} or null, if the type is not a {@link #UPDATE_DEVICE_SENSOR} type.
     *
     * @return type as {@link SensorEnum} or null
     */
    public SensorEnum getTypeAsSensorEnum();

    /**
     * Returns true, if this {@link DeviceStateUpdate} is a {@link #UPDATE_DEVICE_SENSOR} type, otherwise false.
     *
     * @return true, if it is a sensor type
     */
    public boolean isSensorUpdateType();

    /**
     * Returns the scene id of this {@link DeviceStateUpdate}, if this {@link DeviceStateUpdate} is a scene update type,
     * otherwise it will be returned -1.
     *
     * @return the scene id or -1
     */
    public Short getSceneId();

    /**
     * Returns the scene configuration or output reading priority, if this {@link DeviceStateUpdate} is a
     * {@link #UPDATE_SCENE_CONFIG} or {@link #UPDATE_SCENE_OUTPUT} type.
     *
     * @return scene reading priority
     */
    public Short getScenePriority();

    /**
     * Returns true, if this {@link DeviceStateUpdate} is a {@link #UPDATE_SCENE_CONFIG} or {@link #UPDATE_SCENE_OUTPUT}
     * type, otherwise false.
     *
     * @return true, if it is a scene reading type
     */
    public boolean isSceneUpdateType();

    /**
     * Returns the update type as {@link DeviceBinarayInputEnum} or null, if the type is not a {@link #BINARY_INPUT}
     * type.
     *
     * @return type as {@link DeviceBinarayInputEnum} or null
     */
    public DeviceBinarayInputEnum getTypeAsDeviceBinarayInputEnum();

    /**
     * Returns true, if this {@link DeviceStateUpdate} is a {@link #BINARY_INPUT} type, otherwise false.
     *
     * @return true, if it is a binary input type
     */
    public boolean isBinarayInputType();
}
