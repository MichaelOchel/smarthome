/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.DeviceBinarayInputEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.SensorEnum;

/**
 * The {@link DeviceStateUpdateImpl} is the implementation of the {@link DeviceStateUpdate}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class DeviceStateUpdateImpl implements DeviceStateUpdate {

    private final String UPDATE_TYPE;
    private final Object VALUE;

    /**
     * Creates a new {@link DeviceStateUpdateImpl} with the given updateType and a value as {@link Object}.
     *
     * @param updateType must not be null
     * @param value must not be null
     */
    public DeviceStateUpdateImpl(String updateType, Object value) {
        this.UPDATE_TYPE = updateType;
        this.VALUE = value;
    }

    /**
     * Creates a new {@link DeviceStateUpdateImpl} with the given updateType and a value as {@link Integer}.
     *
     * @param updateType must not be null
     * @param value must not be null
     */
    public DeviceStateUpdateImpl(String updateType, Integer value) {
        this.UPDATE_TYPE = updateType;
        this.VALUE = value;
    }

    /**
     * Creates a new {@link DeviceStateUpdateImpl} through the given {@link DeviceBinaryInput} and value as
     * {@link Short}. The updateType as {@link String} will be automatically create.
     *
     * @param updateDeviceBinary must not be null
     * @param value must not be null
     */
    public DeviceStateUpdateImpl(DeviceBinarayInputEnum updateDeviceBinary, Short value) {
        this.UPDATE_TYPE = DeviceStateUpdate.BINARY_INPUT + updateDeviceBinary.getBinaryInputType();
        this.VALUE = value;
    }

    /**
     * Creates a new {@link DeviceStateUpdateImpl} through the given {@link SensorEnum} and value as
     * {@link Integer}. The updateType as {@link String} will be automatically create.
     *
     * @param updateSensorType must not be null
     * @param value must not be null
     */
    public DeviceStateUpdateImpl(SensorEnum updateSensorType, Integer value) {
        this.UPDATE_TYPE = DeviceStateUpdate.UPDATE_DEVICE_SENSOR + updateSensorType.getSensorType();
        this.VALUE = value;
    }

    /**
     * Creates a new {@link DeviceStateUpdateImpl} through the given {@link SensorEnum} and value as
     * {@link Float}. The updateType as {@link String} will be automatically create.
     *
     * @param updateSensorType must not be null
     * @param value must not be null
     */
    public DeviceStateUpdateImpl(SensorEnum updateSensorType, Float value) {
        this.UPDATE_TYPE = DeviceStateUpdate.UPDATE_DEVICE_SENSOR + updateSensorType.getSensorType();
        this.VALUE = value;
    }

    /**
     * Creates a new {@link DeviceStateUpdateImpl} with the given updateType and a value as {@link Float}.
     *
     * @param updateType must not be null
     * @param value must not be null
     */
    public DeviceStateUpdateImpl(String updateType, Float value) {
        this.UPDATE_TYPE = updateType;
        this.VALUE = value;
    }

    /**
     * Creates a new {@link DeviceStateUpdateImpl} with the given updateType and a value as {@link String}.
     *
     * @param updateType must not be null
     * @param value must not be null
     */
    public DeviceStateUpdateImpl(String updateType, String value) {
        this.UPDATE_TYPE = updateType;
        this.VALUE = value;
    }

    /**
     * Creates a new {@link DeviceStateUpdateImpl} with the given updateType and a value as {@link Short}-array, e.g.
     * for
     * scene updates.
     *
     * @param updateType must not be null
     * @param value must not be null
     */
    public DeviceStateUpdateImpl(String updateType, Short[] value) {
        this.UPDATE_TYPE = updateType;
        this.VALUE = value;
    }

    /**
     * Creates a new {@link DeviceStateUpdateImpl} with the given updateType and a value as {@link Short}.
     *
     * @param updateType must not be null
     * @param value must not be null
     */
    public DeviceStateUpdateImpl(String updateType, Short value) {
        this.UPDATE_TYPE = updateType;
        this.VALUE = value;
    }

    @Override
    public Object getValue() {
        return VALUE;
    }

    @Override
    public String getType() {
        return UPDATE_TYPE;
    }

    @Override
    public Integer getValueAsInteger() {
        try {
            if (VALUE instanceof Integer) {
                return (Integer) VALUE;
            }
            if (VALUE instanceof Float) {
                return ((Float) VALUE).intValue();
            }
            if (VALUE instanceof Short) {
                return ((Short) VALUE).intValue();
            }
            if (VALUE instanceof String) {
                return Integer.parseInt((String) VALUE);
            }
        } catch (Exception e) {
            throw new ClassCastException();
        }
        throw new ClassCastException();
    }

    @Override
    public String getValueAsString() {
        if (VALUE instanceof Integer) {
            return ((Integer) VALUE).toString();
        }
        if (VALUE instanceof Float) {
            return ((Float) VALUE).toString();
        }
        if (VALUE instanceof Short) {
            return ((Short) VALUE).toString();
        }
        if (VALUE instanceof String) {
            return (String) VALUE;
        }
        throw new ClassCastException();
    }

    @Override
    public Short[] getValueAsShortArray() {
        return (Short[]) VALUE;
    }

    @Override
    public Short getValueAsShort() {
        try {
            if (VALUE instanceof Integer) {
                return ((Integer) VALUE).shortValue();
            }
            if (VALUE instanceof Float) {
                return ((Float) VALUE).shortValue();
            }
            if (VALUE instanceof Short) {
                return (Short) VALUE;
            }
            if (VALUE instanceof String) {
                return Short.parseShort((String) VALUE);
            }
        } catch (Exception e) {
            throw new ClassCastException();
        }
        throw new ClassCastException();
    }

    @Override
    public Float getValueAsFloat() {
        try {
            if (VALUE instanceof Integer) {
                return ((Integer) VALUE).floatValue();
            }
            if (VALUE instanceof Float) {
                return (Float) VALUE;
            }
            if (VALUE instanceof Short) {
                return ((Short) VALUE).floatValue();
            }
            if (VALUE instanceof String) {
                return Float.parseFloat((String) VALUE);
            }
        } catch (Exception e) {
            throw new ClassCastException();
        }
        throw new ClassCastException();
    }

    @Override
    public SensorEnum getTypeAsSensorEnum() {
        return SensorEnum.getSensor(Short.parseShort(UPDATE_TYPE.split("-")[1]));
    }

    @Override
    public boolean isSensorUpdateType() {
        return UPDATE_TYPE.startsWith(UPDATE_DEVICE_SENSOR);
    }

    @Override
    public DeviceBinarayInputEnum getTypeAsDeviceBinarayInputEnum() {
        return DeviceBinarayInputEnum.getdeviceBinarayInput(Short.parseShort(UPDATE_TYPE.split("-")[1]));
    }

    @Override
    public boolean isBinarayInputType() {
        return UPDATE_TYPE.startsWith(BINARY_INPUT);
    }

    @Override
    public Short getSceneId() {
        if (isSceneUpdateType()) {
            return ((Short[]) VALUE)[0];
        }
        return -1;
    }

    @Override
    public Short getScenePriority() {
        if (isSceneUpdateType()) {
            return ((Short[]) VALUE)[1];
        }
        return -1;
    }

    @Override
    public boolean isSceneUpdateType() {
        return UPDATE_TYPE.equals(UPDATE_SCENE_CONFIG) || UPDATE_TYPE.equals(UPDATE_SCENE_OUTPUT);
    }
}
