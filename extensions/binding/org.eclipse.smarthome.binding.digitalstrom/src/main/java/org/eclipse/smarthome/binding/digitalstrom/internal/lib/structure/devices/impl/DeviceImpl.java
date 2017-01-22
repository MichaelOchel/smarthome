/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.config.Config;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.types.EventItem;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.AbstractGeneralDeviceInformations;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceSceneSpec;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.ChangeableDeviceConfigEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.DeviceBinarayInputEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.FuncNameAndColorGroupEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.FunctionalColorGroupEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.OutputModeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.SensorEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.DSID;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.DeviceBinaryInput;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.DeviceSensorValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.DeviceStateUpdateImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.JSONDeviceSceneSpecImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.InternalScene;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.SceneEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link DeviceImpl} is the implementation of the {@link Device}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class DeviceImpl extends AbstractGeneralDeviceInformations implements Device {

    private static final Logger logger = LoggerFactory.getLogger(DeviceImpl.class);

    private Config config;

    private DSID meterDSID = null;
    private int zoneId = 0;
    private List<Short> groupList = new LinkedList<Short>();

    private FunctionalColorGroupEnum functionalGroup = null;
    private FuncNameAndColorGroupEnum functionalName = null;
    private String hwInfo;

    private OutputModeEnum outputMode = null;

    private boolean isOn = false;
    private boolean isOpen = true;
    private short outputValue = 0;
    private short maxOutputValue = DeviceConstants.DEFAULT_MAX_OUTPUTVALUE;
    private short minOutputValue = 0;

    private short slatAngle = 0;
    private short maxSlatAngle = DeviceConstants.MAX_SLAT_ANGLE;
    private short minSlatAngle = DeviceConstants.MIN_SLAT_ANGLE;

    private int slatPosition = 0;
    private int maxSlatPosition = DeviceConstants.MAX_ROLLERSHUTTER;
    private int minSlatPosition = DeviceConstants.MIN_ROLLERSHUTTER;

    private List<DeviceSensorValue> deviceSensorValues = Collections
            .synchronizedList(new ArrayList<DeviceSensorValue>());
    private List<DeviceBinaryInput> deviceBinaryInputs = Collections
            .synchronizedList(new ArrayList<DeviceBinaryInput>());
    private List<SensorEnum> devicePowerSensorTypes = new ArrayList<SensorEnum>();
    private List<SensorEnum> deviceClimateSensorTypes = new ArrayList<SensorEnum>();

    // for scenes
    private short activeSceneNumber = -1;
    private InternalScene activeScene = null;
    private InternalScene lastScene = null;
    private int outputValueBeforeSceneCall = 0;
    private short slatAngleBeforeSceneCall = 0;
    private boolean lastCallWasUndo = false;

    private Map<Short, DeviceSceneSpec> sceneConfigMap = Collections
            .synchronizedMap(new HashMap<Short, DeviceSceneSpec>());
    private Map<Short, Integer[]> sceneOutputMap = Collections.synchronizedMap(new HashMap<Short, Integer[]>());

    // saves outstanding commands
    private List<DeviceStateUpdate> deviceStateUpdates = Collections
            .synchronizedList(new LinkedList<DeviceStateUpdate>());

    /*
     * Saves the refresh priorities and reading initialized flag of power sensors as an matrix.
     * The first array fields are 0 = active power, 1 = output current, 2 = electric meter, 3 = power consumption and in
     * each field is a
     * string array with the fields 0 = refresh priority 1 = reading initial flag (true = reading is initialized,
     * otherwise false)
     */
    private Object[] powerSensorRefresh = new Object[] { new String[] { Config.REFRESH_PRIORITY_NEVER, "false" },
            new String[] { Config.REFRESH_PRIORITY_NEVER, "false" },
            new String[] { Config.REFRESH_PRIORITY_NEVER, "false" },
            new String[] { Config.REFRESH_PRIORITY_NEVER, "false" } };
    /*
     * Cache the last power sensor value to get power sensor value directly
     * the key is the output value and the value is an Integer array for the sensor values (0 = active power, 1 =
     * output current, 2 = power consumption, 3 = output current high)
     */
    private Map<Short, Integer[]> cachedSensorPowerValues = Collections
            .synchronizedMap(new HashMap<Short, Integer[]>());

    // Preparing for the advance device property setting "Turn 'switched' output off if value below:", but the
    // configuration currently not work in digitalSTROM, because of that the value is fix 1.
    private int switchPercentOff = 1;

    /**
     * Creates a new {@link DeviceImpl} from the given DigitalSTROM-Device {@link JsonObject}.
     *
     * @param deviceJsonObject
     */
    public DeviceImpl(JsonObject deviceJsonObject) {
        super(deviceJsonObject);
        if (deviceJsonObject.get(JSONApiResponseKeysEnum.METER_DSID.getKey()) != null) {
            this.meterDSID = new DSID(deviceJsonObject.get(JSONApiResponseKeysEnum.METER_DSID.getKey()).getAsString());
        } else if (deviceJsonObject.get(JSONApiResponseKeysEnum.DS_METER_DSID.getKey()) != null) {
            this.meterDSID = new DSID(
                    deviceJsonObject.get(JSONApiResponseKeysEnum.DS_METER_DSID.getKey()).getAsString());
        }
        if (deviceJsonObject.get(JSONApiResponseKeysEnum.HW_INFO.getKey()) != null) {
            this.hwInfo = deviceJsonObject.get(JSONApiResponseKeysEnum.HW_INFO.getKey()).getAsString();
        } else if (deviceJsonObject.get(JSONApiResponseKeysEnum.HW_INFO_UPPER_HW.getKey()) != null) {
            this.hwInfo = deviceJsonObject.get(JSONApiResponseKeysEnum.HW_INFO_UPPER_HW.getKey()).getAsString();
        }
        if (deviceJsonObject.get(JSONApiResponseKeysEnum.ON.getKey()) != null) {
            if (!isShade()) {
                this.isOn = deviceJsonObject.get(JSONApiResponseKeysEnum.ON.getKey()).getAsBoolean();
            } else {
                this.isOpen = deviceJsonObject.get(JSONApiResponseKeysEnum.ON.getKey()).getAsBoolean();
            }
        }
        if (deviceJsonObject.get(JSONApiResponseKeysEnum.ZONE_ID.getKey()) != null) {
            zoneId = deviceJsonObject.get(JSONApiResponseKeysEnum.ZONE_ID.getKey()).getAsInt();
        } else if (deviceJsonObject.get(JSONApiResponseKeysEnum.ZONE_ID_Lower_Z.getKey()) != null) {
            zoneId = deviceJsonObject.get(JSONApiResponseKeysEnum.ZONE_ID_Lower_Z.getKey()).getAsInt();
        }
        if (deviceJsonObject.get(JSONApiResponseKeysEnum.GROUPS.getKey()).isJsonArray()) {
            JsonArray array = deviceJsonObject.get(JSONApiResponseKeysEnum.GROUPS.getKey()).getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i) != null) {
                    initAddGroup(array.get(i).getAsShort());
                }
            }
        } else if (deviceJsonObject.get(JSONApiResponseKeysEnum.GROUPS.getKey()).isJsonObject()) {
            for (Entry<String, JsonElement> entry : deviceJsonObject.get(JSONApiResponseKeysEnum.GROUPS.getKey())
                    .getAsJsonObject().entrySet()) {
                initAddGroup(entry.getValue().getAsJsonObject().get(JSONApiResponseKeysEnum.ID.getKey()).getAsShort());
            }
        }
        if (deviceJsonObject.get(JSONApiResponseKeysEnum.OUTPUT_MODE.getKey()) != null) {
            int tmp = deviceJsonObject.get(JSONApiResponseKeysEnum.OUTPUT_MODE.getKey()).getAsInt();
            if (tmp != -1) {
                if (OutputModeEnum.containsMode(tmp)) {
                    outputMode = OutputModeEnum.getMode(tmp);
                }
            }
        }
        if (deviceJsonObject.get(JSONApiResponseKeysEnum.SENSOR_INPUTS.getKey()) != null
                && deviceJsonObject.get(JSONApiResponseKeysEnum.SENSOR_INPUTS.getKey()).isJsonObject()) {
            JsonObject jObj = deviceJsonObject.get(JSONApiResponseKeysEnum.SENSOR_INPUTS.getKey()).getAsJsonObject();
            for (Entry<String, JsonElement> entry : jObj.entrySet()) {
                if (entry.getValue().isJsonObject()) {
                    JsonObject sensorType = entry.getValue().getAsJsonObject();
                    if (sensorType.get(JSONApiResponseKeysEnum.TYPE.getKey()) != null) {
                        if (SensorEnum
                                .containsSensor(sensorType.get(JSONApiResponseKeysEnum.TYPE.getKey()).getAsShort())) {
                            setDeviceSensorValue(new DeviceSensorValue(entry.getValue().getAsJsonObject()));
                        }
                    }
                }
            }
        }
        if (deviceJsonObject.get(JSONApiResponseKeysEnum.BINARY_INPUTS.getKey()) != null
                && deviceJsonObject.get(JSONApiResponseKeysEnum.BINARY_INPUTS.getKey()).isJsonObject()) {
            JsonObject jObj = deviceJsonObject.get(JSONApiResponseKeysEnum.BINARY_INPUTS.getKey()).getAsJsonObject();
            for (Entry<String, JsonElement> entry : jObj.entrySet()) {
                if (entry.getValue().isJsonObject()) {
                    JsonObject binaryInput = entry.getValue().getAsJsonObject();
                    deviceBinaryInputs.add(new DeviceBinaryInput(binaryInput));
                }
            }
        }
        init();
    }

    private void initAddGroup(Short groupID) {
        if (groupID != -1) {
            this.groupList.add(groupID);
            if (FuncNameAndColorGroupEnum.containsColorGroup(groupID)) {
                if (this.functionalName == null
                        || !FuncNameAndColorGroupEnum.getMode(groupID).equals(FuncNameAndColorGroupEnum.JOKER)) {
                    this.functionalName = FuncNameAndColorGroupEnum.getMode(groupID);
                    this.functionalGroup = functionalName.getFunctionalColor();
                }
            }
        }
    }

    private void init() {
        if (groupList.contains((short) 1)) {
            maxOutputValue = DeviceConstants.MAX_OUTPUT_VALUE_LIGHT;
            if (this.isDimmable()) {
                minOutputValue = DeviceConstants.MIN_DIM_VALUE;
            }
        } else {
            maxOutputValue = DeviceConstants.DEFAULT_MAX_OUTPUTVALUE;
            minOutputValue = 0;
        }
        if (isOn) {
            outputValue = DeviceConstants.DEFAULT_MAX_OUTPUTVALUE;
        }
    }

    @Override
    public synchronized DSID getMeterDSID() {
        return this.meterDSID;
    }

    @Override
    public synchronized void setMeterDSID(String meterDSID) {
        this.meterDSID = new DSID(meterDSID);
        informListenerAboutConfigChange(ChangeableDeviceConfigEnum.METER_DSID);
    }

    @Override
    public String getHWinfo() {
        return hwInfo;
    }

    @Override
    public List<Short> getGroups() {
        return new LinkedList<Short>(groupList);
    }

    @Override
    public void addGroup(Short groupID) {
        if (!this.groupList.contains(groupID)) {
            this.groupList.add(groupID);
        }
        informListenerAboutConfigChange(ChangeableDeviceConfigEnum.GROUPS);
    }

    @Override
    public void setGroups(List<Short> newGroupList) {
        if (newGroupList != null) {
            this.groupList = newGroupList;
        }
        informListenerAboutConfigChange(ChangeableDeviceConfigEnum.GROUPS);
    }

    @Override
    public synchronized int getZoneId() {
        return zoneId;
    }

    @Override
    public synchronized void setZoneId(int zoneID) {
        this.zoneId = zoneID;
        informListenerAboutConfigChange(ChangeableDeviceConfigEnum.ZONE_ID);
    }

    @Override
    public synchronized boolean isOn() {
        return isOn;
    }

    @Override
    public synchronized void setIsOn(boolean flag) {
        if (flag) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.ON_OFF, 1));
        } else {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.ON_OFF, -1));
        }
    }

    @Override
    public synchronized boolean isOpen() {
        return this.isOpen;
    }

    @Override
    public synchronized void setIsOpen(boolean flag) {
        if (flag) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.OPEN_CLOSE, 1));
            if (isBlind()) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.OPEN_CLOSE_ANGLE, 1));
            }
        } else {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.OPEN_CLOSE, -1));
            if (isBlind()) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.OPEN_CLOSE_ANGLE, -1));
            }
        }
    }

    @Override
    public synchronized void setOutputValue(short value) {
        if (!isShade()) {
            if (value <= 0) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.ON_OFF, -1));

            } else if (value > maxOutputValue) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.ON_OFF, 1));
            } else {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.OUTPUT, value));
            }
        }
    }

    @Override
    public synchronized boolean isDimmable() {
        return OutputModeEnum.outputModeIsDimmable(outputMode);
    }

    @Override
    public synchronized boolean isSwitch() {
        return OutputModeEnum.outputModeIsSwitch(outputMode);
    }

    @Override
    public synchronized boolean isDeviceWithOutput() {
        return this.outputMode != null && !this.outputMode.equals(OutputModeEnum.DISABLED);
    }

    @Override
    public boolean isSensorDevice() {
        return !isDeviceWithOutput() && !deviceClimateSensorTypes.isEmpty();
    }

    @Override
    public boolean isHeatingDevice() {
        return functionalName.equals(FuncNameAndColorGroupEnum.HEATING);
    }

    @Override
    public boolean isTemperatureControlledDevice() {
        return functionalName.equals(FuncNameAndColorGroupEnum.TEMPERATION_CONTROL);
    }

    @Override
    public boolean isShade() {
        return OutputModeEnum.outputModeIsShade(outputMode);
    }

    @Override
    public boolean isBlind() {
        return outputMode.equals(OutputModeEnum.POSITION_CON_US);
    }

    @Override
    public synchronized FunctionalColorGroupEnum getFunctionalColorGroup() {
        return this.functionalGroup;
    }

    @Override
    public synchronized void setFunctionalColorGroup(FunctionalColorGroupEnum fuctionalColorGroup) {
        this.functionalGroup = fuctionalColorGroup;
        informListenerAboutConfigChange(ChangeableDeviceConfigEnum.FUNCTIONAL_GROUP);
    }

    @Override
    public OutputModeEnum getOutputMode() {
        return outputMode;
    }

    @Override
    public synchronized void setOutputMode(OutputModeEnum newOutputMode) {
        this.outputMode = newOutputMode;
        informListenerAboutConfigChange(ChangeableDeviceConfigEnum.OUTPUT_MODE);
    }

    @Override
    public synchronized void increase() {
        if (isDimmable()) {
            deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.OUTPUT_INCREASE, 0));
        }
        if (isShade()) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_INCREASE, 0));
            if (isBlind()) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_ANGLE_DECREASE, 0));
            }
        }
    }

    @Override
    public synchronized void decrease() {
        if (isDimmable()) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.OUTPUT_DECREASE, 0));
        }
        if (isShade()) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_DECREASE, 0));
            if (isBlind()) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_ANGLE_DECREASE, 0));
            }
        }
    }

    @Override
    public synchronized void increaseSlatAngle() {
        if (isBlind()) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_ANGLE_DECREASE, 1));
        }
    }

    @Override
    public synchronized void decreaseSlatAngle() {
        if (isBlind()) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_ANGLE_DECREASE, 1));
        }
    }

    @Override
    public synchronized short getOutputValue() {
        return outputValue;
    }

    @Override
    public short getMaxOutputValue() {
        return maxOutputValue;
    }

    @Override
    public short getMinOutputValue() {
        return minOutputValue;
    }

    @Override
    public synchronized int getSlatPosition() {
        return slatPosition;
    }

    @Override
    public synchronized short getAnglePosition() {
        return slatAngle;
    }

    @Override
    public synchronized void setAnglePosition(int angle) {
        if (angle == slatAngle) {
            return;
        }
        if (angle < minSlatAngle) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_ANGLE, minSlatAngle));
        } else if (angle > this.maxSlatPosition) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_ANGLE, maxSlatAngle));
        } else {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_ANGLE, angle));
        }
    }

    @Override
    public synchronized void setSlatPosition(int position) {
        if (position == this.slatPosition) {
            return;
        }
        if (position < minSlatPosition) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.SLATPOSITION, minSlatPosition));
        } else if (position > this.maxSlatPosition) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.SLATPOSITION, maxSlatPosition));
        } else {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.SLATPOSITION, position));
        }
    }

    private short getDimmStep() {
        if (isDimmable()) {
            return DeviceConstants.DIM_STEP_LIGHT;
        } else if (isShade()) {
            return DeviceConstants.MOVE_STEP_ROLLERSHUTTER;
        } else {
            return DeviceConstants.DEFAULT_MOVE_STEP;
        }
    }

    @Override
    public int getMaxSlatPosition() {
        return maxSlatPosition;
    }

    @Override
    public int getMinSlatPosition() {
        return minSlatPosition;
    }

    @Override
    public int getMaxSlatAngle() {
        return maxSlatAngle;
    }

    @Override
    public int getMinSlatAngle() {
        return minSlatAngle;
    }

    /* Begin-Scenes */

    @Override
    public synchronized void callInternalScene(InternalScene scene) {
        if (lastCallWasUndo) {
            lastScene = null;
            if (activeScene != null) {
                activeScene.deactivateSceneByDevice();
            }
            activeScene = null;
        }
        internalCallScene(scene.getSceneID());
        activeScene = scene;
        lastCallWasUndo = false;
    }

    @Override
    public void checkSceneConfig(Short sceneNumber, short prio) {
        if (isDeviceWithOutput()) {
            if (!containsSceneConfig(sceneNumber)) {
                deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SCENE_CONFIG,
                        new Short[] { sceneNumber, prio }));

            }
            if (sceneOutputMap.get(sceneNumber) == null) {
                deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SCENE_OUTPUT,
                        new Short[] { sceneNumber, prio }));
            }
        }
    }

    @Override
    public synchronized void undoInternalScene(InternalScene scene) {
        logger.debug("undo Scene {} dSID {}", scene.getSceneID(), dsid.getValue());
        if (activeScene != null && activeScene.equals(scene)) {
            if (lastCallWasUndo) {
                lastScene = null;
                return;
            }
            if (this.lastScene != null && !lastScene.equals(activeScene)) {
                activeScene = lastScene;
                lastScene = null;
                activeScene.activateSceneByDevice();
            } else {
                internalUndoScene();
                logger.debug("internalUndo Scene dSID " + dsid.getValue());
                this.activeScene = null;
            }
            internalUndoScene();
            lastCallWasUndo = true;
        }
    }

    @Override
    public synchronized void callScene(Short sceneNumber) {
        this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_CALL_SCENE, sceneNumber));
    }

    @Override
    public synchronized void internalCallScene(Short sceneNumber) {
        logger.debug("call Scene id {} dSID {}", sceneNumber, dsid.getValue());
        if (isDeviceWithOutput()) {
            activeSceneNumber = sceneNumber;
            informLastSceneAboutSceneCall(sceneNumber);
            if (!isShade()) {
                outputValueBeforeSceneCall = this.outputValue;
            } else {
                outputValueBeforeSceneCall = this.slatPosition;
                if (isBlind()) {
                    slatAngleBeforeSceneCall = this.slatAngle;
                }
            }
            if (!checkSceneNumber(sceneNumber)) {
                if (containsSceneConfig(sceneNumber)) {
                    if (doIgnoreScene(sceneNumber)) {
                        return;
                    }
                } else {
                    this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SCENE_CONFIG,
                            new Short[] { sceneNumber, 0 }));
                }
                if (sceneOutputMap.get(sceneNumber) != null) {
                    if (!isShade()) {
                        updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.OUTPUT,
                                sceneOutputMap.get(sceneNumber)[0]));
                    } else {
                        updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.SLATPOSITION,
                                sceneOutputMap.get(sceneNumber)[0]));
                        if (isBlind()) {
                            updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_ANGLE,
                                    sceneOutputMap.get(sceneNumber)[1]));
                        }
                    }
                } else {
                    this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SCENE_OUTPUT,
                            new Short[] { sceneNumber, 0 }));
                }
            }

        }
    }

    private boolean checkSceneNumber(Short sceneNumber) {
        if (SceneEnum.containsScene(sceneNumber)) {
            if (this.outputMode.equals(OutputModeEnum.POWERSAVE)) {
                switch (SceneEnum.getScene(sceneNumber)) {
                    case ABSENT:
                    case DEEP_OFF:
                    case SLEEPING:
                        this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.ON_OFF, -1));
                        return true;
                    case AREA_1_OFF:
                    case AREA_2_OFF:
                    case AREA_3_OFF:
                    case AREA_4_OFF:
                    case PRESET_0:
                    case PRESET_10:
                    case PRESET_20:
                    case PRESET_30:
                    case PRESET_40:
                        return true;
                    default:
                        break;
                }
            }
            if (this.outputMode.equals(OutputModeEnum.WIPE)) {
                switch (SceneEnum.getScene(sceneNumber)) {
                    case STANDBY:
                    case AUTO_STANDBY:
                    case AREA_1_OFF:
                    case AREA_2_OFF:
                    case AREA_3_OFF:
                    case AREA_4_OFF:
                    case PRESET_0:
                    case PRESET_10:
                    case PRESET_20:
                    case PRESET_30:
                    case PRESET_40:
                        this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.ON_OFF, -1));
                        return true;
                    default:
                        break;
                }
            }
            switch (SceneEnum.getScene(sceneNumber)) {
                // on scenes
                case DEVICE_ON:
                case MAXIMUM:
                    if (!isShade()) {
                        this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.ON_OFF, 1));
                    } else {
                        this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.OPEN_CLOSE, 1));
                        if (isBlind()) {
                            this.updateInternalDeviceState(
                                    new DeviceStateUpdateImpl(DeviceStateUpdate.OPEN_CLOSE_ANGLE, 1));
                        }
                    }
                    return true;
                // off scenes
                case MINIMUM:
                case DEVICE_OFF:
                case AUTO_OFF:
                    if (!isShade()) {
                        this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.ON_OFF, -1));
                    } else {
                        this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.OPEN_CLOSE, -1));
                        if (isBlind()) {
                            this.updateInternalDeviceState(
                                    new DeviceStateUpdateImpl(DeviceStateUpdate.OPEN_CLOSE_ANGLE, -1));
                        }
                    }
                    return true;
                // increase scenes
                case INCREMENT:
                case AREA_1_INCREMENT:
                case AREA_2_INCREMENT:
                case AREA_3_INCREMENT:
                case AREA_4_INCREMENT:
                    if (isDimmable()) {
                        if (outputValue == maxOutputValue) {
                            return true;
                        }
                        this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.OUTPUT_INCREASE, 0));
                    }
                    if (isShade()) {
                        if (slatPosition == maxSlatPosition) {
                            return true;
                        }
                        this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_INCREASE, 0));
                        if (isBlind()) {
                            if (slatAngle == maxSlatAngle) {
                                return true;
                            }
                            updateInternalDeviceState(
                                    new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_ANGLE_INCREASE, 0));
                        }
                    }
                    return true;
                // decrease scenes
                case DECREMENT:
                case AREA_1_DECREMENT:
                case AREA_2_DECREMENT:
                case AREA_3_DECREMENT:
                case AREA_4_DECREMENT:
                    if (isDimmable()) {
                        if (outputValue == minOutputValue) {
                            return true;
                        }
                        updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.OUTPUT_DECREASE, 0));
                    }
                    if (isShade()) {
                        if (slatPosition == minSlatPosition) {
                            return true;
                        }
                        updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_DECREASE, 0));
                        if (isBlind()) {
                            if (slatAngle == minSlatAngle) {
                                return true;
                            }
                            this.updateInternalDeviceState(
                                    new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_ANGLE_INCREASE, 0));
                        }
                    }
                    return true;
                // Stop scenes
                case AREA_1_STOP:
                case AREA_2_STOP:
                case AREA_3_STOP:
                case AREA_4_STOP:
                case DEVICE_STOP:
                case STOP:
                    this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OUTPUT_VALUE, 0));
                    return true;
                // Area Stepping continue scenes
                case AREA_STEPPING_CONTINUE:
                    // TODO: we don't know what will be happened when this scene was called. Some one know it?
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    private Integer[] getStandartSceneOutput(short sceneNumber) {
        if (SceneEnum.getScene(sceneNumber) != null) {
            switch (SceneEnum.getScene(sceneNumber)) {
                case DEVICE_ON:
                case MAXIMUM:
                    if (!isShade()) {
                        return new Integer[] { (int) maxOutputValue, -1 };
                    } else {
                        if (isBlind()) {
                            return new Integer[] { (int) maxSlatPosition, (int) maxSlatAngle };
                        } else {
                            return new Integer[] { (int) maxSlatPosition, -1 };
                        }
                    }
                    // off scenes
                case MINIMUM:
                case DEVICE_OFF:
                case AUTO_OFF:
                    if (!isShade()) {
                        return new Integer[] { (int) 0, -1 };
                    } else {
                        if (isBlind()) {
                            return new Integer[] { (int) 0, 0 };
                        } else {
                            return new Integer[] { (int) 0, -1 };
                        }
                    }
                default:
                    break;
            }
        }
        return null;
    }

    private void informLastSceneAboutSceneCall(short sceneNumber) {
        if (this.activeScene != null && this.activeScene.getSceneID() != sceneNumber) {
            this.activeScene.deactivateSceneByDevice();
            this.lastScene = this.activeScene;
            this.activeScene = null;
        }
    }

    @Override
    public synchronized void undoScene() {
        this.deviceStateUpdates
                .add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_UNDO_SCENE, this.activeSceneNumber));
    }

    @Override
    public synchronized void internalUndoScene() {
        if (!isShade()) {
            updateInternalDeviceState(
                    new DeviceStateUpdateImpl(DeviceStateUpdate.OUTPUT, this.outputValueBeforeSceneCall));
        } else {
            updateInternalDeviceState(
                    new DeviceStateUpdateImpl(DeviceStateUpdate.SLATPOSITION, this.outputValueBeforeSceneCall));
            if (isBlind()) {
                updateInternalDeviceState(
                        new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_ANGLE, this.slatAngleBeforeSceneCall));
            }
        }

        if (activeSceneNumber != -1) {
            activeSceneNumber = -1;
        }
    }

    @Override
    public InternalScene getAcitiveScene() {
        return this.activeScene;
    }

    @Override
    public Integer[] getSceneOutputValue(short sceneId) {
        synchronized (sceneOutputMap) {
            if (sceneOutputMap.containsKey(sceneId)) {
                return sceneOutputMap.get(sceneId);
            }
        }
        return new Integer[] { -1, -1 };
    }

    @Override
    public void setSceneOutputValue(short sceneId, int value) {
        internalSetSceneOutputValue(sceneId, value, -1);
        if (listener != null) {
            listener.onSceneConfigAdded(sceneId);
        }
    }

    @Override
    public void setSceneOutputValue(short sceneId, int value, int angle) {
        internalSetSceneOutputValue(sceneId, value, angle);
        if (listener != null) {
            listener.onSceneConfigAdded(sceneId);
        }
    }

    private void internalSetSceneOutputValue(short sceneId, int value, int angle) {
        synchronized (sceneOutputMap) {
            sceneOutputMap.put(sceneId, new Integer[] { value, angle });
        }
        if (activeSceneNumber == sceneId) {
            internalCallScene(sceneId);
        }
    }

    @Override
    public List<Short> getSavedScenes() {
        Set<Short> bothKeySet = new HashSet<Short>(sceneOutputMap.keySet());
        bothKeySet.addAll(sceneConfigMap.keySet());
        return new LinkedList<Short>(bothKeySet);
    }

    @Override
    public void addSceneConfig(short sceneId, DeviceSceneSpec sceneSpec) {
        if (sceneSpec != null) {
            synchronized (sceneConfigMap) {
                sceneConfigMap.put(sceneId, sceneSpec);
                if (listener != null) {
                    listener.onSceneConfigAdded(sceneId);
                }
            }
        }
    }

    @Override
    public DeviceSceneSpec getSceneConfig(short sceneId) {
        synchronized (sceneConfigMap) {
            return sceneConfigMap.get(sceneId);
        }
    }

    @Override
    public boolean doIgnoreScene(short sceneId) {
        synchronized (sceneConfigMap) {
            if (this.sceneConfigMap.containsKey(sceneId)) {
                return this.sceneConfigMap.get(sceneId).isDontCare();
            }
        }
        return false;
    }

    @Override
    public boolean containsSceneConfig(short sceneId) {
        synchronized (sceneConfigMap) {
            return sceneConfigMap.containsKey(sceneId);
        }
    }

    /* End-Scenes */

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Device) {
            Device device = (Device) obj;
            return device.getDSID().equals(this.getDSID());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getDSID().hashCode();
    }

    @Override
    public boolean isPowerSensorUpToDate(SensorEnum powerSensorType) {
        if (powerSensorType != null && SensorEnum.isPowerSensor(powerSensorType)) {
            boolean isUpToDate = true;
            if (powerSensorType.equals(SensorEnum.ACTIVE_POWER)) {
                isUpToDate = (outputMode != null && outputMode.equals(OutputModeEnum.WIPE) && !isOn)
                        || (isOn && !isShade()) && !checkPowerSensorRefreshPriorityNever(powerSensorType)
                                ? checkSensorRefreshTime(powerSensorType) : true;
            }
            if (powerSensorType.equals(SensorEnum.ELECTRIC_METER)) {
                isUpToDate = (isOn || getDeviceSensorValue(powerSensorType).getDsValue() == 0) && !isShade()
                        && !checkPowerSensorRefreshPriorityNever(powerSensorType)
                                ? checkSensorRefreshTime(powerSensorType) : true;
            }
            isUpToDate = isOn && !isShade() && !checkPowerSensorRefreshPriorityNever(powerSensorType)
                    ? checkSensorRefreshTime(powerSensorType) : true;
            if (!isUpToDate) {
                if (!getSensorDataReadingInitialized(powerSensorType)) {
                    deviceStateUpdates.add(new DeviceStateUpdateImpl(powerSensorType, 0));
                    setSensorDataReadingInitialized(powerSensorType, true);
                }
                return false;
            }
            return true;
        }
        throw new IllegalArgumentException("powerSensorType is null or not a power sensor type.");
    }

    private boolean checkSensorRefreshTime(SensorEnum sensorType) {
        if (sensorType != null) {
            DeviceSensorValue devSenVal = getDeviceSensorValue(sensorType);
            if (devSenVal.getValid()) {
                if (devSenVal != null) {
                    int refresh = Config.DEFAULT_SENSORDATA_REFRESH_INTERVAL;
                    if (config != null) {
                        refresh = config.getSensordataRefreshInterval();
                    }
                    return (devSenVal.getTimestamp().getTime() + refresh) > System.currentTimeMillis();
                }
            }
        }
        return false;
    }

    @Override
    public boolean isSensorDataUpToDate() {
        boolean isUpToDate = true;
        for (SensorEnum sensorType : devicePowerSensorTypes) {
            isUpToDate = isPowerSensorUpToDate(sensorType);
        }
        return isUpToDate;
    }

    @Override
    public void setSensorDataRefreshPriority(String activePowerRefreshPriority, String electricMeterRefreshPriority,
            String outputCurrentRefreshPriority) {
        if (checkPriority(activePowerRefreshPriority)) {
            ((String[]) powerSensorRefresh[0])[0] = activePowerRefreshPriority;
        }
        if (checkPriority(outputCurrentRefreshPriority)) {
            ((String[]) powerSensorRefresh[1])[0] = outputCurrentRefreshPriority;
        }
        if (checkPriority(electricMeterRefreshPriority)) {
            ((String[]) powerSensorRefresh[2])[0] = electricMeterRefreshPriority;
        }
    }

    @Override
    public void setSensorDataRefreshPriority(SensorEnum powerSensorType, String refreshPriority) {
        if (checkPriority(refreshPriority)) {
            String[] powerSensorRefresh = getPowerSensorRefresh(powerSensorType);
            if (powerSensorRefresh != null) {
                powerSensorRefresh[0] = refreshPriority;
            }
        }
    }

    @Override
    public String getPowerSensorRefreshPriority(SensorEnum powerSensorType) {
        if (powerSensorType.equals(SensorEnum.ACTIVE_POWER) && outputMode.equals(OutputModeEnum.WIPE) && !isOn) {
            return Config.REFRESH_PRIORITY_LOW;
        }
        String[] powerSensorRefresh = getPowerSensorRefresh(powerSensorType);
        if (powerSensorRefresh != null) {
            return powerSensorRefresh[0];
        }
        return null;
    }

    @Override
    public boolean checkPowerSensorRefreshPriorityNever(SensorEnum powerSensorType) {
        if (getPowerSensorRefreshPriority(powerSensorType) != null) {
            return getPowerSensorRefreshPriority(powerSensorType).equals(Config.REFRESH_PRIORITY_NEVER);
        }
        return true;
    }

    private void setAllSensorDataRefreshPrioritiesToNever() {
        for (short i = 0; i < powerSensorRefresh.length; i++) {
            ((String[]) powerSensorRefresh[i])[0] = Config.REFRESH_PRIORITY_NEVER;
        }
    }

    private void setSensorDataReadingInitialized(SensorEnum powerSensorType, Boolean flag) {
        String[] powerSensorRefresh = getPowerSensorRefresh(powerSensorType);
        if (powerSensorRefresh != null) {
            powerSensorRefresh[1] = flag.toString();
        }
    }

    private boolean getSensorDataReadingInitialized(SensorEnum powerSensorType) {
        String[] powerSensorRefresh = getPowerSensorRefresh(powerSensorType);
        if (powerSensorRefresh != null) {
            return Boolean.valueOf(powerSensorRefresh[1]);
        }
        return false;
    }

    private String[] getPowerSensorRefresh(SensorEnum powerSensorType) {
        // if (SensorEnum.isPowerSensor(powerSensorType)) {
        switch (powerSensorType) {
            case ACTIVE_POWER:
                return (String[]) powerSensorRefresh[0];
            case OUTPUT_CURRENT:
                return (String[]) powerSensorRefresh[1];
            case ELECTRIC_METER:
                return (String[]) powerSensorRefresh[2];
            case POWER_CONSUMPTION:
                return (String[]) powerSensorRefresh[3];
            default:
                return null;
        }
        // }
    }

    private boolean checkPriority(String priority) {
        switch (priority) {
            case Config.REFRESH_PRIORITY_HIGH:
            case Config.REFRESH_PRIORITY_MEDIUM:
            case Config.REFRESH_PRIORITY_LOW:
            case Config.REFRESH_PRIORITY_NEVER:
                return true;
            default:
                logger.error("Sensor data update priority do not exist! Please check the input!");
                return false;
        }
    }

    @Override
    public boolean isDeviceUpToDate() {
        isSensorDataUpToDate();
        return this.deviceStateUpdates.isEmpty();
    }

    @Override
    public DeviceStateUpdate getNextDeviceUpdateState() {
        return !this.deviceStateUpdates.isEmpty() ? this.deviceStateUpdates.remove(0) : null;
    }

    private int internalSetOutputValue(int value) {
        if (isShade()) {
            slatPosition = value;
            if (slatPosition <= 0) {
                slatPosition = 0;
                isOpen = false;
            } else {
                isOpen = true;
            }
            return slatPosition;
        } else {
            outputValue = (short) value;
            if (outputValue <= 0) {
                internalSetOff();
            } else {
                if (isSwitch()) {
                    if (outputValue < switchPercentOff) {
                        internalSetOff();
                        isOn = false;
                    } else {
                        isOn = true;
                        setCachedMeterData();
                    }
                } else {
                    isOn = true;
                    setCachedMeterData();
                }
            }
            return outputValue;
        }
    }

    private void internalSetOff() {
        this.isOn = false;
        logger.debug("internal set OFF ");
        if (!checkPowerSensorRefreshPriorityNever(SensorEnum.ACTIVE_POWER)) {
            if (getSensorDataReadingInitialized(SensorEnum.ACTIVE_POWER)) {
                deviceStateUpdates.add(new DeviceStateUpdateImpl(SensorEnum.ACTIVE_POWER, -1));
                logger.debug("internal set sensor to 0");
            }
            setDsSensorValue(SensorEnum.ACTIVE_POWER, 0);
        }
        if (!checkPowerSensorRefreshPriorityNever(SensorEnum.OUTPUT_CURRENT)) {
            if (getSensorDataReadingInitialized(SensorEnum.OUTPUT_CURRENT)) {
                deviceStateUpdates.add(new DeviceStateUpdateImpl(SensorEnum.OUTPUT_CURRENT, -1));
            }
            setDsSensorValue(SensorEnum.OUTPUT_CURRENT, 0);
        }
        if (!checkPowerSensorRefreshPriorityNever(SensorEnum.POWER_CONSUMPTION)) {
            if (getSensorDataReadingInitialized(SensorEnum.POWER_CONSUMPTION)) {
                deviceStateUpdates.add(new DeviceStateUpdateImpl(SensorEnum.POWER_CONSUMPTION, -1));
            }
            setDsSensorValue(SensorEnum.POWER_CONSUMPTION, 0);
        }
    }

    private short internalSetAngleValue(int value) {
        if (value < 0) {
            slatAngle = 0;
        }
        if (value > maxSlatAngle) {
            slatAngle = maxSlatAngle;
        } else {
            slatAngle = (short) value;
        }
        return slatAngle;
    }

    // Device sensors
    @Override
    public List<SensorEnum> getSensorTypes() {
        List<SensorEnum> list = Lists.newArrayList(devicePowerSensorTypes);
        list.addAll(deviceClimateSensorTypes);
        return list;
    }

    @Override
    public List<SensorEnum> getPowerSensorTypes() {
        return devicePowerSensorTypes;
    }

    @Override
    public List<SensorEnum> getClimateSensorTypes() {
        return deviceClimateSensorTypes;
    }

    @Override
    public List<DeviceSensorValue> getDeviceSensorValues() {
        return deviceSensorValues;
    }

    @Override
    public boolean supportsSensorType(SensorEnum sensorType) {
        if (sensorType != null) {
            return getSensorTypes().contains(sensorType);
        }
        return false;
    }

    @Override
    public void setDeviceSensorValue(DeviceSensorValue deviceSensorValue) {
        if (deviceSensorValue != null) {
            int index = deviceSensorValues.indexOf(deviceSensorValue);
            if (index < 0) {
                deviceSensorValues.add(deviceSensorValue);
                if (SensorEnum.isPowerSensor(deviceSensorValue.getSensorType())) {
                    devicePowerSensorTypes.add(deviceSensorValue.getSensorType());
                } else {
                    deviceClimateSensorTypes.add(deviceSensorValue.getSensorType());
                }
            } else {
                if (deviceSensorValue.getTimestamp().after(deviceSensorValues.get(index).getTimestamp())) {
                    logger.debug("set deviceSeneorValue, new deviceSensorValue is: " + deviceSensorValue.toString());
                    deviceSensorValues.set(index, deviceSensorValue);
                    checkSensorValueSet(deviceSensorValue, true);
                }
            }
        }
    }

    @Override
    public void setDeviceSensorByEvent(EventItem event) {
        DeviceSensorValue devSenVal = new DeviceSensorValue(event.getProperties());
        if (devSenVal != null) {
            SensorEnum sensorType = devSenVal.getSensorType();
            if (!isEchoSensor(sensorType)) {
                logger.debug("Event is no echo, set values {} for sensorType {}", devSenVal, devSenVal.getSensorType());
                if (SensorEnum.isPowerSensor(sensorType) && getSensorDataReadingInitialized(sensorType)) {
                    logger.debug(
                            "SensorJob was initialized, remove sensorjob for sensorType: " + devSenVal.getSensorType());
                    deviceStateUpdates.add(new DeviceStateUpdateImpl(sensorType, -1));
                }
                setDeviceSensorValue(devSenVal);
            } else {
                logger.debug("Event is echo remove sensorType " + devSenVal.getSensorType() + " from echoBox");
                sensorEchoBox.remove(devSenVal.getSensorType());
            }
        }
    }

    private boolean isEchoSensor(SensorEnum sensorType) {
        return sensorEchoBox != null ? sensorEchoBox.contains(sensorType) : false;
    }

    private List<SensorEnum> sensorEchoBox = Collections.synchronizedList(new LinkedList<SensorEnum>());

    @Override
    public void setDeviceSensorDsValueBySensorJob(SensorEnum sensorType, Integer value) {
        logger.debug("sensorJob for device {} is executet", dsid.getValue());
        if (isSensorEchoBoxEnabled()) {
            // temperature resolution is not correct, so waiting for device sensor-event
            if (!sensorType.toString().contains("TEMPERATURE")) {
                logger.debug("echoBox is enabled, add sensorType {} to echoBox", sensorType);
                sensorEchoBox.add(sensorType);
            } else {
                logger.debug("echoBox is enabled, ignoring temperation update and wait for sensor Event");
                return;
            }
        }
        setDsSensorValue(sensorType, value);
    }

    @Override
    public void enableSensorEchoBox() {
        if (sensorEchoBox == null) {
            sensorEchoBox = Collections.synchronizedList(new LinkedList<SensorEnum>());
        }
    }

    @Override
    public void disableSensorEchoBox() {
        sensorEchoBox = null;
    }

    @Override
    public boolean isSensorEchoBoxEnabled() {
        return sensorEchoBox != null;
    }

    @Override
    public DeviceSensorValue getDeviceSensorValue(SensorEnum sensorType) {
        if (sensorType != null) {
            for (DeviceSensorValue devSenVal : deviceSensorValues) {
                if (devSenVal.getSensorType().equals(sensorType)) {
                    return devSenVal;
                }
            }
        }
        return null;
    }

    @Override
    public DeviceSensorValue getDeviceSensorValue(Short sensorIndex) {
        if (sensorIndex != null) {
            for (DeviceSensorValue devSenVal : deviceSensorValues) {
                if (devSenVal.getSensorIndex().equals(sensorIndex)) {
                    return devSenVal;
                }
            }
        }
        return null;
    }

    @Override
    public Short getSensorIndex(SensorEnum sensorType) {
        if (sensorType != null) {
            DeviceSensorValue devSenVal = getDeviceSensorValue(sensorType);
            return devSenVal != null /* && devSenVal.getValid() */ ? devSenVal.getSensorIndex() : null;
        }
        return null;
    }

    @Override
    public SensorEnum getSensorType(Short sensorIndex) {
        if (sensorIndex != null) {
            DeviceSensorValue devSenVal = getDeviceSensorValue(sensorIndex);
            return devSenVal != null /* && devSenVal.getValid() */ ? devSenVal.getSensorType() : null;
        }
        return null;
    }

    @Override
    public Integer getDsSensorValue(SensorEnum sensorType) {
        return getDsSensorValue((Object) sensorType);
    }

    @Override
    public Integer getDsSensorValue(Short sensorIndex) {
        return getDsSensorValue((Object) sensorIndex);
    }

    @Override
    public Float getFloatSensorValue(Short sensorIndex) {
        return getFloatSensorValue((Object) sensorIndex);
    }

    @Override
    public Float getFloatSensorValue(SensorEnum sensorType) {
        return getFloatSensorValue((Object) sensorType);
    }

    @Override
    public boolean setFloatSensorValue(SensorEnum sensorType, Float floatSensorValue) {
        return checkAndSetSensorValue(sensorType, null, floatSensorValue);
    }

    @Override
    public boolean setFloatSensorValue(Short sensorIndex, Float floatSensorValue) {
        return checkAndSetSensorValue(sensorIndex, null, floatSensorValue);
    }

    @Override
    public boolean setDsSensorValue(Short sensorIndex, Integer dSSensorValue) {
        return checkAndSetSensorValue(sensorIndex, dSSensorValue, null);
    }

    @Override
    public boolean setDsSensorValue(SensorEnum sensorType, Integer dSSensorValue) {
        return checkAndSetSensorValue(sensorType, dSSensorValue, null);
    }

    @Override
    public boolean setDsSensorValue(Short sensorIndex, Integer dSSensorValue, Float floatSensorValue) {
        return checkAndSetSensorValue(sensorIndex, dSSensorValue, floatSensorValue);
    }

    @Override
    public boolean setDsSensorValue(SensorEnum sensorType, Integer dSSensorValue, Float floatSensorValue) {
        return checkAndSetSensorValue(sensorType, dSSensorValue, floatSensorValue);
    }

    @Override
    public boolean hasSensors() {
        return hasClimateSensors() || hasPowerSensors();
    }

    @Override
    public boolean hasClimateSensors() {
        return !deviceClimateSensorTypes.isEmpty();
    }

    @Override
    public boolean hasPowerSensors() {
        return !devicePowerSensorTypes.isEmpty();
    }

    // Sensor get/set helper methods
    private DeviceSensorValue getDeviceSensorValueForGet(Object obj) {
        return checkHighOutputCurrent(getDeviceSensorValueForSet(obj));
    }

    private DeviceSensorValue getDeviceSensorValueForSet(Object obj) {
        if (obj instanceof Short) {
            return getDeviceSensorValue((Short) obj);
        } else {
            return getDeviceSensorValue((SensorEnum) obj);
        }
    }

    private Integer getDsSensorValue(Object obj) {
        if (obj != null) {
            DeviceSensorValue devSenVal = checkPowerSensor(getDeviceSensorValueForGet(obj));
            return devSenVal != null && devSenVal.getValid() ? devSenVal.getDsValue() : null;
        }
        return null;
    }

    private Float getFloatSensorValue(Object obj) {
        if (obj != null) {
            DeviceSensorValue devSenVal = checkPowerSensor(getDeviceSensorValueForGet(obj));
            return devSenVal != null && devSenVal.getValid() ? devSenVal.getFloatValue() : null;
        }
        return null;
    }

    private DeviceSensorValue checkPowerSensor(DeviceSensorValue devSenVal) {
        if (devSenVal != null && SensorEnum.isPowerSensor(devSenVal.getSensorType())) {
            if (!devSenVal.getSensorType().equals(SensorEnum.ELECTRIC_METER)
                    && !(SensorEnum.isPowerSensor(devSenVal.getSensorType()) && isOn)) {
                devSenVal.setDsValue(0);
            }
        }
        return devSenVal;
    }

    /**
     * Checks output current sensor to return automatically high output current sensor, if the sensor exists.
     *
     * @param devSenVal
     * @return output current high DeviceSensorValue or the given DeviceSensorValue
     */
    private DeviceSensorValue checkHighOutputCurrent(DeviceSensorValue devSenVal) {
        if (devSenVal != null && devSenVal.getSensorType().equals(SensorEnum.OUTPUT_CURRENT)
                && devSenVal.getDsValue() == SensorEnum.OUTPUT_CURRENT.getMax().intValue()
                && devicePowerSensorTypes.contains(SensorEnum.OUTPUT_CURRENT_H)) {
            return getDeviceSensorValue(SensorEnum.OUTPUT_CURRENT_H);
        }
        return devSenVal;
    }

    private boolean checkAndSetSensorValue(Object obj, Integer dsValue, Float floatValue) {
        boolean isSet = false;
        if (obj != null) {
            DeviceSensorValue devSenVal = getDeviceSensorValueForSet(obj);
            if (devSenVal != null) {
                if (dsValue != null && floatValue != null) {
                    isSet = devSenVal.setValues(floatValue, dsValue);
                } else if (dsValue != null) {
                    isSet = devSenVal.setDsValue(dsValue);
                } else if (floatValue != null) {
                    isSet = devSenVal.setFloatValue(floatValue);
                }
                logger.debug("check devSenVal {} isSet={}", devSenVal.toString(), isSet);
                checkSensorValueSet(devSenVal, isSet);
            }
        }
        return isSet;
    }

    private void checkSensorValueSet(DeviceSensorValue devSenVal, boolean isSet) {
        if (devSenVal != null) {
            if (isSet) {
                if (outputMode.equals(OutputModeEnum.WIPE) && !isOn
                        && devSenVal.getSensorType().equals(SensorEnum.ACTIVE_POWER)) {
                    int standby = Config.DEFAULT_STANDBY_ACTIVE_POWER;
                    if (config != null) {
                        standby = config.getStandbyActivePower();
                    }
                    if (devSenVal.getDsValue() > standby) {
                        this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.ON_OFF, 1));
                    }
                }
                if (SensorEnum.isPowerSensor(devSenVal.getSensorType())) {
                    addPowerSensorCache(devSenVal);
                }
                informListenerAboutStateUpdate(
                        new DeviceStateUpdateImpl(devSenVal.getSensorType(), devSenVal.getFloatValue()));
            }
            setSensorDataReadingInitialized(devSenVal.getSensorType(), false);
        }
    }

    private void addPowerSensorCache(DeviceSensorValue newDevSenVal) {
        Integer[] cachedPowerValues = cachedSensorPowerValues.get(outputValue);
        if (cachedPowerValues == null) {
            cachedPowerValues = new Integer[4];
        }
        switch (newDevSenVal.getSensorType()) {
            case ACTIVE_POWER:
                cachedPowerValues[0] = newDevSenVal.getDsValue();
                break;
            case OUTPUT_CURRENT:
                cachedPowerValues[1] = newDevSenVal.getDsValue();
                break;
            case OUTPUT_CURRENT_H:
                cachedPowerValues[3] = newDevSenVal.getDsValue();
                break;
            case POWER_CONSUMPTION:
                cachedPowerValues[2] = newDevSenVal.getDsValue();
                break;
            default:
                return;
        }
        this.cachedSensorPowerValues.put(outputValue, cachedPowerValues);
    }

    @Override
    public synchronized void updateInternalDeviceState(DeviceStateUpdate deviceStateUpdate) {
        if (deviceStateUpdate != null) {
            logger.debug("internal set outputvalue");
            switch (deviceStateUpdate.getType()) {
                case DeviceStateUpdate.OUTPUT_DECREASE:
                    deviceStateUpdate = new DeviceStateUpdateImpl(DeviceStateUpdate.OUTPUT_DECREASE,
                            internalSetOutputValue(outputValue - getDimmStep()));
                    break;
                case DeviceStateUpdate.OUTPUT_INCREASE:
                    deviceStateUpdate = new DeviceStateUpdateImpl(DeviceStateUpdate.OUTPUT_INCREASE,
                            internalSetOutputValue(outputValue + getDimmStep()));
                    break;
                case DeviceStateUpdate.OUTPUT:
                    internalSetOutputValue(deviceStateUpdate.getValueAsInteger());
                    break;
                case DeviceStateUpdate.ON_OFF:
                    if (deviceStateUpdate.getValueAsInteger() < 0) {
                        internalSetOutputValue(0);
                    } else {
                        internalSetOutputValue(maxOutputValue);
                    }
                    break;
                case DeviceStateUpdate.OPEN_CLOSE:
                    if (deviceStateUpdate.getValueAsInteger() < 0) {
                        internalSetOutputValue(0);
                    } else {
                        internalSetOutputValue(maxSlatPosition);
                    }
                    break;
                case DeviceStateUpdate.OPEN_CLOSE_ANGLE:
                    if (deviceStateUpdate.getValueAsInteger() < 0) {
                        internalSetAngleValue(0);
                    } else {
                        internalSetAngleValue(maxSlatAngle);
                    }
                    break;
                case DeviceStateUpdate.SLAT_DECREASE:
                    deviceStateUpdate = new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_DECREASE,
                            internalSetOutputValue(slatPosition - getDimmStep()));
                    break;
                case DeviceStateUpdate.SLAT_INCREASE:
                    deviceStateUpdate = new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_INCREASE,
                            internalSetOutputValue(slatPosition + getDimmStep()));
                case DeviceStateUpdate.SLATPOSITION:
                    internalSetOutputValue(deviceStateUpdate.getValueAsInteger());
                    break;
                case DeviceStateUpdate.SLAT_ANGLE_DECREASE:
                    deviceStateUpdate = new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_ANGLE_DECREASE,
                            internalSetAngleValue(slatAngle - DeviceConstants.ANGLE_STEP_SLAT));
                    break;
                case DeviceStateUpdate.SLAT_ANGLE_INCREASE:
                    deviceStateUpdate = new DeviceStateUpdateImpl(DeviceStateUpdate.SLAT_ANGLE_INCREASE,
                            internalSetAngleValue(slatAngle + DeviceConstants.ANGLE_STEP_SLAT));
                    break;
                case DeviceStateUpdate.SLAT_ANGLE:
                    internalSetAngleValue(deviceStateUpdate.getValueAsInteger());
                    break;
                case DeviceStateUpdate.UPDATE_CALL_SCENE:
                    this.internalCallScene(deviceStateUpdate.getValueAsShort());
                    return;
                case DeviceStateUpdate.UPDATE_UNDO_SCENE:
                    this.internalUndoScene();
                    return;
                default:
                    if (deviceStateUpdate.isSensorUpdateType()) {
                        SensorEnum sensorType = deviceStateUpdate.getTypeAsSensorEnum();
                        setFloatSensorValue(sensorType, deviceStateUpdate.getValueAsFloat());
                    }
                    return;
            }
        }
        if (activeScene != null) {
            Integer[] sceneOutput = getStandartSceneOutput(activeScene.getSceneID());
            if (sceneOutput == null) {
                sceneOutput = sceneOutputMap.get(activeScene.getSceneID());
            }
            if (sceneOutput != null) {
                boolean outputChanged = false;
                if (isShade()) {
                    if (isBlind() && sceneOutput[1] != slatAngle) {
                        logger.debug("Scene output angle: {} setted output value {}", sceneOutput[1], slatAngle);
                        outputChanged = true;
                    }
                    if (sceneOutput[0] != slatPosition) {
                        logger.debug("Scene output value: {} setted output value {}", sceneOutput[0], slatPosition);
                        outputChanged = true;
                    }
                } else {
                    if (sceneOutput[0] != outputValue) {
                        logger.debug("Scene output value: {} setted output value {}", sceneOutput[0], outputValue);
                        outputChanged = true;
                    }
                }
                if (outputChanged) {
                    logger.debug("Device output from Device with dSID {} changed deactivate scene {}", dsid.getValue(),
                            activeScene.getID());
                    activeScene.deviceSceneChanged((short) -1);
                    lastScene = null;
                    activeScene = null;
                }

            } else {
                lastScene = null;
            }
        }
        informListenerAboutStateUpdate(deviceStateUpdate);
    }

    @Override
    public DeviceStatusListener unregisterDeviceStatusListener() {
        setAllSensorDataRefreshPrioritiesToNever();
        return super.unregisterDeviceStatusListener();
    }

    private void setCachedMeterData() {
        logger.debug("load cached sensor data device with dsid {}", dsid.getValue());
        Integer[] cachedSensorData = this.cachedSensorPowerValues.get(this.getOutputValue());
        if (cachedSensorData != null) {
            if (cachedSensorData[0] != null && !checkPowerSensorRefreshPriorityNever(SensorEnum.ACTIVE_POWER)) {
                informListenerAboutStateUpdate(
                        new DeviceStateUpdateImpl(SensorEnum.ACTIVE_POWER, (float) cachedSensorData[0]));

            }
            if (cachedSensorData[1] != null && !checkPowerSensorRefreshPriorityNever(SensorEnum.OUTPUT_CURRENT)) {
                if (cachedSensorData[1] == SensorEnum.OUTPUT_CURRENT.getMax().intValue()
                        && devicePowerSensorTypes.contains(SensorEnum.OUTPUT_CURRENT_H)) {
                    informListenerAboutStateUpdate(
                            new DeviceStateUpdateImpl(SensorEnum.OUTPUT_CURRENT, (float) cachedSensorData[3]));
                } else {
                    informListenerAboutStateUpdate(
                            new DeviceStateUpdateImpl(SensorEnum.OUTPUT_CURRENT, (float) cachedSensorData[1]));
                }
            }
            if (cachedSensorData[2] != null && !checkPowerSensorRefreshPriorityNever(SensorEnum.POWER_CONSUMPTION)) {
                informListenerAboutStateUpdate(
                        new DeviceStateUpdateImpl(SensorEnum.ACTIVE_POWER, (float) cachedSensorData[2]));
            }
        }
    }

    /**
     * if an {@link DeviceStatusListener} is registered inform him about the new state otherwise do nothing.
     *
     * @param deviceStateUpdate
     */
    private void informListenerAboutStateUpdate(DeviceStateUpdate deviceStateUpdate) {
        if (listener != null) {
            if (isSwitch() && deviceStateUpdate.getType().equals(DeviceStateUpdate.OUTPUT)) {
                if (deviceStateUpdate.getValueAsInteger() >= switchPercentOff) {
                    deviceStateUpdate = new DeviceStateUpdateImpl(DeviceStateUpdate.ON_OFF, DeviceStateUpdate.ON_VALUE);
                } else {
                    deviceStateUpdate = new DeviceStateUpdateImpl(DeviceStateUpdate.ON_OFF,
                            DeviceStateUpdate.OFF_VALUE);
                }
            }
            logger.debug("Inform listener about device state changed: type: " + deviceStateUpdate.getType()
                    + ", value: " + deviceStateUpdate.getValue());
            listener.onDeviceStateChanged(deviceStateUpdate);
        }
    }

    private void informListenerAboutConfigChange(ChangeableDeviceConfigEnum changedConfig) {
        if (listener != null) {
            listener.onDeviceConfigChanged(changedConfig);
            logger.debug("Inform listener about device config {} changed" + changedConfig.toString());
        }
    }

    @SuppressWarnings("null")
    @Override
    public void saveConfigSceneSpecificationIntoDevice(Map<String, String> propertries) {
        if (propertries != null) {
            String sceneSave;
            for (String key : propertries.keySet()) {
                if (key.startsWith(DigitalSTROMBindingConstants.DEVICE_SCENE)) {
                    try {
                        short sceneID = Short.parseShort((String) key
                                .subSequence(DigitalSTROMBindingConstants.DEVICE_SCENE.length(), key.length()));
                        sceneSave = propertries.get(key);
                        if (StringUtils.isNotBlank(sceneSave)) {
                            logger.debug("Find saved scene configuration for device with dSID {} and sceneID {}", dsid,
                                    key);
                            String[] sceneParm = sceneSave.replace(" ", "").split(",");
                            JSONDeviceSceneSpecImpl sceneSpecNew = null;
                            int sceneValue = -1;
                            int sceneAngle = -1;
                            for (int j = 0; j < sceneParm.length; j++) {
                                String[] sceneParmSplit = sceneParm[j].split(":");
                                switch (sceneParmSplit[0]) {
                                    case "Scene":
                                        sceneSpecNew = new JSONDeviceSceneSpecImpl(sceneParmSplit[1]);
                                        break;
                                    case "dontcare":
                                        sceneSpecNew.setDontcare(Boolean.parseBoolean(sceneParmSplit[1]));
                                        break;
                                    case "localPrio":
                                        sceneSpecNew.setLocalPrio(Boolean.parseBoolean(sceneParmSplit[1]));
                                        break;
                                    case "specialMode":
                                        sceneSpecNew.setSpecialMode(Boolean.parseBoolean(sceneParmSplit[1]));
                                        break;
                                    case "sceneValue":
                                        sceneValue = Integer.parseInt(sceneParmSplit[1]);
                                        break;
                                    case "sceneAngle":
                                        sceneAngle = Integer.parseInt(sceneParmSplit[1]);
                                        break;
                                }
                            }
                            if (sceneValue > -1) {
                                logger.debug(
                                        "Saved sceneValue {}, sceneAngle {} for scene id {} into device with dsid {}",
                                        sceneValue, sceneAngle, sceneID, getDSID().getValue());
                                internalSetSceneOutputValue(sceneID, sceneValue, sceneAngle);
                                deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SCENE_OUTPUT,
                                        new Short[] { sceneID, (short) -1 }));
                            }
                            if (sceneSpecNew != null) {
                                logger.debug("Saved sceneConfig: [{}] for scene id {} into device with dsid {}",
                                        sceneSpecNew.toString(), sceneID, getDSID().getValue());
                                synchronized (sceneConfigMap) {
                                    sceneConfigMap.put(sceneID, sceneSpecNew);
                                }
                                deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SCENE_CONFIG,
                                        new Short[] { sceneID, (short) -1 }));
                            }
                        }
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }
        }
    }

    @SuppressWarnings("null")
    @Override
    public void saveConfigSceneSpecificationIntoDevice(String propertries) {
        String[] scenes = propertries.split("\n");
        for (int i = 0; i < scenes.length; i++) {
            logger.debug("Find saved scene configuration for device with dSID {} and sceneID {}", dsid, i);
            String[] sceneIdToConfig = scenes[i].replaceAll(" ", "").split("=");
            String[] sceneParm = sceneIdToConfig[1].split(",");
            JSONDeviceSceneSpecImpl sceneSpecNew = null;
            int sceneValue = -1;
            int sceneAngle = -1;
            for (int j = 0; j < sceneParm.length; j++) {
                String[] sceneParmSplit = sceneParm[j].split(":");
                switch (sceneParmSplit[0]) {
                    case "Scene":
                        sceneSpecNew = new JSONDeviceSceneSpecImpl(sceneParmSplit[1]);
                        break;
                    case "dontcare":
                        sceneSpecNew.setDontcare(Boolean.parseBoolean(sceneParmSplit[1]));
                        break;
                    case "localPrio":
                        sceneSpecNew.setLocalPrio(Boolean.parseBoolean(sceneParmSplit[1]));
                        break;
                    case "specialMode":
                        sceneSpecNew.setSpecialMode(Boolean.parseBoolean(sceneParmSplit[1]));
                        break;
                    case "sceneValue":
                        sceneValue = Integer.parseInt(sceneParmSplit[1]);
                        break;
                    case "sceneAngle":
                        sceneAngle = Integer.parseInt(sceneParmSplit[1]);
                        break;
                }
            }
            if (sceneValue > -1) {
                logger.debug("Saved sceneValue {}, sceneAngle {} for scene id {} into device with dsid {}", sceneValue,
                        sceneAngle, i, getDSID().getValue());
                synchronized (sceneOutputMap) {
                    sceneOutputMap.put(sceneSpecNew.getScene().getSceneNumber(),
                            new Integer[] { sceneValue, sceneAngle });
                }
            }
            if (sceneSpecNew != null) {
                logger.debug("Saved sceneConfig: [{}] for scene id {} into device with dsid {}",
                        sceneSpecNew.toString(), i, getDSID().getValue());
                synchronized (sceneConfigMap) {
                    sceneConfigMap.put(sceneSpecNew.getScene().getSceneNumber(), sceneSpecNew);
                }
            }
        }

    }

    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

    private String powerSensorRefreshToString() {
        String powSenRef = "";
        for (int i = 0; i < powerSensorRefresh.length; i++) {
            powSenRef = powSenRef + " [" + i + "]=Prio: " + ((String[]) powerSensorRefresh[i])[0] + ", Initialized: "
                    + ((String[]) powerSensorRefresh[i])[1] + " ";
        }
        return powSenRef;
    }

    @Override
    public boolean isBinaryInputDevice() {
        return !deviceBinaryInputs.isEmpty();
    }

    @Override
    public List<DeviceBinaryInput> getBinaryInputs() {
        return deviceBinaryInputs;
    }

    @Override
    public DeviceBinaryInput getBinaryInput(DeviceBinarayInputEnum binaryInputType) {
        if (binaryInputType != null) {
            for (DeviceBinaryInput binInput : deviceBinaryInputs) {
                if (binaryInputType.getBinaryInputType().equals(binInput.getInputType())) {
                    return binInput;
                }
            }
        }
        return null;
    }

    @Override
    public Short getBinaryInputState(DeviceBinarayInputEnum binaryInputType) {
        DeviceBinaryInput devBinInput = getBinaryInput(binaryInputType);
        if (devBinInput != null) {
            return devBinInput.getState();
        }
        return null;
    }

    @Override
    public boolean setBinaryInputState(DeviceBinarayInputEnum binaryInputType, Short newState) {
        DeviceBinaryInput devBinInput = getBinaryInput(binaryInputType);
        if (devBinInput != null) {
            devBinInput.setState(newState);
            informListenerAboutStateUpdate(new DeviceStateUpdateImpl(binaryInputType, newState));
            return true;
        }
        return false;
    }

    @Override
    public void setBinaryInputs(List<DeviceBinaryInput> newBinaryInputs) {
        this.deviceBinaryInputs.clear();
        this.deviceBinaryInputs.addAll(newBinaryInputs);
        informListenerAboutConfigChange(ChangeableDeviceConfigEnum.BINARAY_INPUTS);
    }

    @Override
    public String toString() {
        return "DeviceImpl [meterDSID=" + meterDSID + ", zoneId=" + zoneId + ", groupList=" + groupList
                + ", functionalGroup=" + functionalGroup + ", functionalName=" + functionalName + ", hwInfo=" + hwInfo
                + ", getName()=" + getName() + ", getDSID()=" + getDSID() + ", getDSUID()=" + getDSUID()
                + ", isPresent()=" + isPresent() + ", isValide()=" + isValid() + ", getDisplayID()=" + getDisplayID()
                + ", outputMode=" + outputMode + ", getSensorTypes()=" + getSensorTypes() + ", getDeviceSensorValues()="
                + getDeviceSensorValues() + ", powerSensorRefresh=" + powerSensorRefreshToString() + "]";
    }
}