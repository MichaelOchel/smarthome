/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdate;
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

    public DeviceStateUpdateImpl(String updateType, Object value) {
        this.UPDATE_TYPE = updateType;
        this.VALUE = value;
    }

    public DeviceStateUpdateImpl(String updateType, Integer value) {
        this.UPDATE_TYPE = updateType;
        this.VALUE = value;
    }

    public DeviceStateUpdateImpl(SensorEnum updateSensorType, Integer value) {
        this.UPDATE_TYPE = DeviceStateUpdate.UPDATE_DEVICE_SENSOR + updateSensorType.getSensorType();
        this.VALUE = value;
    }

    public DeviceStateUpdateImpl(SensorEnum updateSensorType, Float value) {
        this.UPDATE_TYPE = DeviceStateUpdate.UPDATE_DEVICE_SENSOR + updateSensorType.getSensorType();
        this.VALUE = value;
    }

    public DeviceStateUpdateImpl(String updateType, Float value) {
        this.UPDATE_TYPE = updateType;
        this.VALUE = value;
    }

    public DeviceStateUpdateImpl(String updateType, String value) {
        this.UPDATE_TYPE = updateType;
        this.VALUE = value;
    }

    public DeviceStateUpdateImpl(String updateType, Short[] value) {
        this.UPDATE_TYPE = updateType;
        this.VALUE = value;
    }

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
        return (Integer) VALUE;
    }

    @Override
    public String getValueAsString() {
        return (String) VALUE;
    }

    @Override
    public Short[] getValueAsShortArray() {
        return (Short[]) VALUE;
    }

    @Override
    public Short getValueAsShort() {
        return (Short) VALUE;
    }

    @Override
    public Float getValueAsFloat() {
        return (Float) VALUE;
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
