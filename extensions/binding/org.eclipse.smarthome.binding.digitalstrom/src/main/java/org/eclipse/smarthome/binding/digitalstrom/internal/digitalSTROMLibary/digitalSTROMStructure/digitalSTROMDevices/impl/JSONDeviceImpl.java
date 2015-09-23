/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMConfiguration.DigitalSTROMConfig;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMServerConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.ChangeableDeviceConfigEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DSID;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceSceneSpec;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceStateUpdateImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.FunctionalColorGroupEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.OutputModeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.InternalScene;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JSONDeviceImpl} is the implementation of the {@link Device}.
 *
 * @author Alexander Betker
 * @author Alex Maier
 * @since 1.3.0
 */
public class JSONDeviceImpl implements Device {

    private static final Logger logger = LoggerFactory.getLogger(JSONDeviceImpl.class);

    private DSID dsid = null;

    private DSID meterDSID = null;

    private String dSUID = null;

    private String hwInfo;

    private String name = null;

    private int zoneId = 0;

    private boolean isPresent = false;

    private boolean isOn = false;

    private boolean isOpen = true;

    private OutputModeEnum outputMode = null;

    private short outputValue = 0;

    private short maxOutputValue = DeviceConstants.DEFAULT_MAX_OUTPUTVALUE;

    private short minOutputValue = 0;

    FunctionalColorGroupEnum functionalGroup = null;

    private int slatPosition = 0;

    private int maxSlatPosition = DeviceConstants.MAX_SLAT_POSITION;

    private int minSlatPosition = DeviceConstants.MIN_SLAT_POSITION;

    private int activePower = 0;

    private int outputCurrent = 0;

    private int electricMeter = 0;

    private List<Short> groupList = new LinkedList<Short>();

    // private List<DeviceListener> deviceListenerList = Collections.synchronizedList(new LinkedList<DeviceListener>());

    /*
     * Cache the last MeterValues to get MeterData directly
     * the key is the output value and the value is an Integer array for the meter data (0 = powerConsumption, 1 =
     * electricMeter, 2 =EnergyMeter)
     */
    private Map<Short, Integer[]> cachedSensorMeterData = Collections.synchronizedMap(new HashMap<Short, Integer[]>());

    private Map<Short, DeviceSceneSpec> sceneConfigMap = Collections
            .synchronizedMap(new HashMap<Short, DeviceSceneSpec>());

    private Map<Short, Integer> sceneOutputMap = Collections.synchronizedMap(new HashMap<Short, Integer>());

    /**
     * Creates a new {@link JSONDeviceImpl} from the given DigitalSTROM-Device {@link JSONObject}.
     *
     * @param group json object
     */
    public JSONDeviceImpl(JSONObject object) {

        if (object.get(JSONApiResponseKeysEnum.DEVICE_NAME.getKey()) != null) {
            this.name = object.get(JSONApiResponseKeysEnum.DEVICE_NAME.getKey()).toString();
        }

        if (object.get(JSONApiResponseKeysEnum.DEVICE_ID.getKey()) != null) {
            this.dsid = new DSID(object.get(JSONApiResponseKeysEnum.DEVICE_ID.getKey()).toString());
        } else if (object.get(JSONApiResponseKeysEnum.DEVICE_ID_QUERY.getKey()) != null) {
            this.dsid = new DSID(object.get(JSONApiResponseKeysEnum.DEVICE_ID_QUERY.getKey()).toString());
        }

        if (object.get(JSONApiResponseKeysEnum.DEVICE_METER_ID.getKey()) != null) {
            this.meterDSID = new DSID(object.get(JSONApiResponseKeysEnum.DEVICE_METER_ID.getKey()).toString());
        }

        if (object.get(JSONApiResponseKeysEnum.DEVICE_DSUID.getKey()) != null) {
            this.dSUID = object.get(JSONApiResponseKeysEnum.DEVICE_DSUID.getKey()).toString();
        }

        if (object.get(JSONApiResponseKeysEnum.DEVICE_ID.getKey()) != null) {
            this.hwInfo = object.get(JSONApiResponseKeysEnum.DEVICE_HW_INFO.getKey()).toString();
        }

        if (object.get(JSONApiResponseKeysEnum.DEVICE_ON.getKey()) != null) {
            this.isOn = object.get(JSONApiResponseKeysEnum.DEVICE_ON.getKey()).toString().equals("true");
        }

        if (object.get(JSONApiResponseKeysEnum.DEVICE_IS_PRESENT.getKey()) != null) {
            this.isPresent = object.get(JSONApiResponseKeysEnum.DEVICE_IS_PRESENT.getKey()).toString().equals("true");
        } else if (object.get(JSONApiResponseKeysEnum.DEVICE_IS_PRESENT_QUERY.getKey()) != null) {
            this.isPresent = object.get(JSONApiResponseKeysEnum.DEVICE_IS_PRESENT_QUERY.getKey()).toString()
                    .equals("true");
        }

        String zoneStr = null;
        if (object.get(JSONApiResponseKeysEnum.DEVICE_ZONE_ID.getKey()) != null) {
            zoneStr = object.get(JSONApiResponseKeysEnum.DEVICE_ZONE_ID.getKey()).toString();
        } else if (object.get(JSONApiResponseKeysEnum.DEVICE_ZONE_ID_QUERY.getKey()) != null) {
            zoneStr = object.get(JSONApiResponseKeysEnum.DEVICE_ZONE_ID_QUERY.getKey()).toString();
        }

        if (zoneStr != null) {
            try {
                this.zoneId = Integer.parseInt(zoneStr);
            } catch (java.lang.NumberFormatException e) {
                logger.error("NumberFormatException by parsing zoneId: " + zoneStr);
            }
        }

        if (object.get(JSONApiResponseKeysEnum.DEVICE_GROUPS.getKey()) instanceof JSONArray) {
            JSONArray array = (JSONArray) object.get(JSONApiResponseKeysEnum.DEVICE_GROUPS.getKey());

            for (int i = 0; i < array.size(); i++) {
                if (array.get(i) != null) {
                    String value = array.get(i).toString();
                    short tmp = -1;
                    try {
                        tmp = Short.parseShort(value);
                    } catch (java.lang.NumberFormatException e) {
                        logger.error("NumberFormatException by parsing groups: " + value);
                    }

                    if (tmp != -1) {
                        this.groupList.add(tmp);
                        if (FunctionalColorGroupEnum.containsColorGroup((int) tmp)) {
                            this.functionalGroup = FunctionalColorGroupEnum.getMode((int) tmp);
                        }
                    }
                }
            }
        }

        if (object.get(JSONApiResponseKeysEnum.DEVICE_OUTPUT_MODE.getKey()) != null) {
            int tmp = -1;
            try {
                tmp = Integer.parseInt(object.get(JSONApiResponseKeysEnum.DEVICE_OUTPUT_MODE.getKey()).toString());
            } catch (java.lang.NumberFormatException e) {
                logger.error("NumberFormatException by parsing outputmode: "
                        + object.get(JSONApiResponseKeysEnum.DEVICE_OUTPUT_MODE.getKey()).toString());
            }

            if (tmp != -1) {
                if (OutputModeEnum.containsMode(tmp)) {
                    outputMode = OutputModeEnum.getMode(tmp);
                }
            }

        }

        init();

    }

    private void init() {
        if (groupList.contains((short) 1)) {
            maxOutputValue = DeviceConstants.MAX_OUTPUT_VALUE_LIGHT;
            if (this.isDimmable()) {
                minOutputValue = DeviceConstants.MIN_DIMM_VALUE;
            }
        } else {
            maxOutputValue = DeviceConstants.DEFAULT_MAX_OUTPUTVALUE;
            minOutputValue = 0;
        }

        if (isOn)
            outputValue = DeviceConstants.DEFAULT_MAX_OUTPUTVALUE;
    }

    @Override
    public DSID getDSID() {
        return dsid;
    }

    @Override
    public String getDSUID() {
        return this.dSUID;
    }

    @Override
    public synchronized DSID getMeterDSID() {
        return this.meterDSID;
    }

    @Override
    public synchronized void setMeterDSID(String meterDSID) {
        this.meterDSID = new DSID(meterDSID);
        if (listener != null) {
            listener.onDeviceConfigChanged(ChangeableDeviceConfigEnum.METER_DSID);
        }
    }

    @Override
    public String getHWinfo() {
        return hwInfo;
    }

    @Override
    public synchronized String getName() {
        return this.name;
    }

    @Override
    public synchronized void setName(String name) {
        this.name = name;
        if (listener != null) {
            listener.onDeviceConfigChanged(ChangeableDeviceConfigEnum.DEVICE_NAME);
        }
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
        if (listener != null) {
            listener.onDeviceConfigChanged(ChangeableDeviceConfigEnum.GROUPS);
        }
    }

    @Override
    public void setGroups(List<Short> newGroupList) {
        if (newGroupList != null) {
            this.groupList = newGroupList;
        }
        if (listener != null) {
            listener.onDeviceConfigChanged(ChangeableDeviceConfigEnum.GROUPS);
        }
    }

    @Override
    public synchronized int getZoneId() {
        return zoneId;
    }

    @Override
    public synchronized void setZoneId(int zoneID) {
        this.zoneId = zoneID;
        if (listener != null) {
            listener.onDeviceConfigChanged(ChangeableDeviceConfigEnum.ZONE_ID);
        }
    }

    @Override
    public synchronized boolean isPresent() {
        return isPresent;
    }

    @Override
    public synchronized void setIsPresent(boolean isPresent) {
        this.isPresent = isPresent;
        if (listener != null) {
            if (!isPresent) {
                listener.onDeviceRemoved(this);
            } else {
                listener.onDeviceAdded(this);
            }

        }
    }

    @Override
    public synchronized boolean isOn() {
        return isOn;
    }

    @Override
    public synchronized void setIsOn(boolean flag) {
        if (flag) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, 1));
        } else {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, -1));
        }
    }

    @Override
    public synchronized boolean isOpen() {
        return this.isOpen;
    }

    @Override
    public synchronized void setIsOpen(boolean flag) {
        if (flag) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OPEN_CLOSE, 1));
        } else {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OPEN_CLOSE, -1));
        }
    }

    @Override
    public synchronized void setOutputValue(short value) {
        if (!isRollershutter()) {
            if (value <= 0) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, -1));

            } else if (value > maxOutputValue) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, 1));
            } else {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_BRIGHTNESS, value));
            }
        } else {
            if (value <= 0) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, -1));
            } else if (value > maxOutputValue) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, 1));
            } else {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION, value));
            }
        }

        this.outputValueBeforeSceneCall = outputValue;
        informLastSceneAboutSceneCall((short) -1);
        this.activeScene = null;
    }

    @Override
    public synchronized boolean isDimmable() {
        if (outputMode == null) {
            return false;
        }
        switch (this.outputMode) {
            case RMS_DIMMER:
            case RMS_DIMMER_CC:
            case PC_DIMMER:
            case PC_DIMMER_CC:
            case RPC_DIMMER:
            case RPC_DIMMER_CC:
                return true;
            default:
                return false;
        }
    }

    @Override
    public synchronized boolean isDeviceWithOutput() {
        return this.outputMode != null && !this.outputMode.equals(OutputModeEnum.DISABLED);
    }

    @Override
    public synchronized FunctionalColorGroupEnum getFunctionalColorGroup() {
        return this.functionalGroup;
    }

    @Override
    public synchronized void setFunctionalColorGroup(FunctionalColorGroupEnum fuctionalColorGroup) {
        this.functionalGroup = fuctionalColorGroup;
        if (listener != null) {
            listener.onDeviceConfigChanged(ChangeableDeviceConfigEnum.FUNCTIONAL_GROUP);
        }
    }

    @Override
    public OutputModeEnum getOutputMode() {
        return outputMode;
    }

    @Override
    public synchronized void setOutputMode(OutputModeEnum newOutputMode) {
        this.outputMode = newOutputMode;
        if (listener != null) {
            listener.onDeviceConfigChanged(ChangeableDeviceConfigEnum.OUTPUT_MODE);
        }
    }

    @Override
    public synchronized void increase() {
        if (isDimmable()) {

            if (outputValue == maxOutputValue) {
                return;
            }
            if ((outputValue + getDimmStep()) > maxOutputValue) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, 1));
            } else {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_BRIGHTNESS_INCREASE,
                        outputValue + getDimmStep()));
                // outputValue += getDimmStep();
            }
            // setIsOn(true);
            // notifyDeviceListener(this.dsid.getValue());
        }

        if (isRollershutter()) {
            if (slatPosition == maxSlatPosition) {
                return;
            }
            if ((slatPosition + getDimmStep()) > slatPosition) {
                this.deviceStateUpdates
                        .add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION, maxSlatPosition));
                // outputValue = maxOutputValue;
            } else {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_INCREASE,
                        slatPosition + getDimmStep()));
                // outputValue += getDimmStep();
            }
        }
    }

    @Override
    public synchronized void decrease() {
        if (isDimmable()) {
            if (outputValue == minOutputValue) {
                /*
                 * if (outputValue == 0) {
                 * setIsOn(false);
                 * }
                 */
                return;
            }

            if ((outputValue - getDimmStep()) <= minOutputValue) {

                if (isOn) {
                    this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, -1));
                }
            } else {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_BRIGHTNESS_DECREASE,
                        outputValue - getDimmStep()));
            }
        }

        if (isRollershutter()) {
            if (slatPosition == minSlatPosition) {
                return;
            }
            if ((slatPosition + getDimmStep()) < slatPosition) {
                this.deviceStateUpdates
                        .add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION, minSlatPosition));
            } else {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_DECREASE,
                        slatPosition - getDimmStep()));
            }
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
    public boolean isRollershutter() {
        if (outputMode == null) {
            return false;
        }
        return outputMode.equals(OutputModeEnum.POSITION_CON) || outputMode.equals(OutputModeEnum.POSITION_CON_US);
    }

    @Override
    public synchronized int getSlatPosition() {
        return slatPosition;
    }

    @Override
    public synchronized void setSlatPosition(int position) {
        if (position == this.slatPosition) {
            return;
        }

        if (position < minSlatPosition) {
            this.deviceStateUpdates
                    .add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION, minSlatPosition));
            // slatPosition = minSlatPosition;
        } else if (position > this.maxSlatPosition) {
            this.deviceStateUpdates
                    .add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION, maxSlatPosition));
            // slatPosition = this.maxSlatPosition;
        } else {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION, position));
            // this.slatPosition = position;
        }
        // notifyDeviceListener(this.dsid.getValue());
    }

    @Override
    public synchronized int getActivePower() {
        return activePower;
    }

    @Override
    public synchronized void setActivePower(int activePower) {
        lastActivePowerUpdate = System.currentTimeMillis();

        if (activePower == this.activePower) {
            return;
        }

        if (activePower < 0) {
            addEshThingStateUpdate(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ACTIVE_POWER, 0));
            this.activePower = 0;
        } else {
            addEshThingStateUpdate(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ACTIVE_POWER, activePower));
            this.activePower = activePower;
            this.addActivePowerToMeterCache(this.getOutputValue(), activePower);
        }
    }

    @Override
    public synchronized int getOutputCurrent() {
        return outputCurrent;
    }

    @Override
    public synchronized void setOutputCurrent(int outputCurrent) {
        lastOutputCurrentUpdate = System.currentTimeMillis();

        if (outputCurrent == this.outputCurrent) {
            return;
        }

        if (outputCurrent < 0) {
            addEshThingStateUpdate(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OUTPUT_CURRENT, 0));
            this.outputCurrent = 0;
        } else {
            addEshThingStateUpdate(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OUTPUT_CURRENT, outputCurrent));
            this.addEnergyMeterToMeterCache(this.getOutputValue(), outputCurrent);
            this.outputCurrent = outputCurrent;
        }
    }

    @Override
    public synchronized int getElectricMeter() {
        return electricMeter;
    }

    @Override
    public synchronized void setElectricMeter(int electricMeter) {
        lastElectricMeterUpdate = System.currentTimeMillis();

        if (electricMeter == this.electricMeter) {
            return;
        }

        if (electricMeter < 0) {
            addEshThingStateUpdate(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ELECTRIC_METER, 0));
            this.electricMeter = 0;
        } else {
            addEshThingStateUpdate(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ELECTRIC_METER, electricMeter));
            this.addElectricMeterToMeterCache(this.getOutputValue(), electricMeter);
            this.electricMeter = electricMeter;
        }
    }

    private void addActivePowerToMeterCache(short outputValue, int activePower) {
        Integer[] cachedMeterData = cachedSensorMeterData.get(outputValue);
        if (cachedMeterData == null) {
            cachedMeterData = new Integer[3];
        }

        cachedMeterData[0] = activePower;

        this.cachedSensorMeterData.put(outputValue, cachedMeterData);
    }

    private void addElectricMeterToMeterCache(short outputValue, int electricMeter) {
        Integer[] cachedMeterData = cachedSensorMeterData.get(outputValue);
        if (cachedMeterData == null) {
            cachedMeterData = new Integer[3];
        }

        cachedMeterData[1] = electricMeter;

        this.cachedSensorMeterData.put(outputValue, cachedMeterData);
    }

    private void addEnergyMeterToMeterCache(short outputValue, int energyMeter) {
        Integer[] cachedMeterData = cachedSensorMeterData.get(outputValue);
        if (cachedMeterData == null) {
            cachedMeterData = new Integer[3];
        }

        cachedMeterData[2] = energyMeter;

        this.cachedSensorMeterData.put(outputValue, cachedMeterData);
    }

    private short getDimmStep() {
        if (isDimmable()) {
            return DeviceConstants.DIMM_STEP_LIGHT;
        } else if (isRollershutter()) {
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

    /* Begin-Scenes */

    private InternalScene activeScene = null;
    private int outputValueBeforeSceneCall = 0;

    @Override
    public synchronized void callNamedScene(InternalScene scene) {
        // boolean haveStoredOutput =
        // logger.debug("!!!!!!!!!!!!!!!!!!!!CALL NAMED SCENE CALL!!!!!!!!!!!!!!!!!!!!!");
        internalCallScene(scene.getSceneID());
        this.activeScene = scene;
        // return haveStoredOutput;
    }

    // weis net
    @Override
    public void checkSceneConfig(Short sceneNumber, int prio) {
        if (isDeviceWithOutput()) {
            if (!containsSceneConfig(sceneNumber)) {
                this.deviceStateUpdates
                        .add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SCENE_CONFIG, prio + sceneNumber));

            }
            if (sceneOutputMap.get(sceneNumber) == null) {
                this.deviceStateUpdates
                        .add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SCENE_OUTPUT, prio + sceneNumber));
            }
        }
    }

    @Override
    public synchronized void undoNamedScene() {
        internalUndoScene();
        this.activeScene = null;
    }

    @Override
    public synchronized void callScene(Short sceneNumber) {
        this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_CALL_SCENE, sceneNumber));
    }

    short activeSceneNumber = -1;

    private synchronized void internalCallScene(Short sceneNumber) {
        // logger.debug("!!!!!!!!!!!!!!!!!!!!CALL INTERNAL SCENE CALL1!!!!!!!!!!!!!!!!!!!!!");
        if (isDeviceWithOutput()) {
            if (containsSceneConfig(sceneNumber)) {
                if (doIgnoreScene(sceneNumber)) {
                    return;
                }
            } else {
                this.deviceStateUpdates
                        .add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SCENE_CONFIG, sceneNumber));
            }

            // logger.debug("!!!!!!!!!!!!!!!!!!!!CALL INTERNAL SCENE CALL2!!!!!!!!!!!!!!!!!!!!!");
            if (checkSceneNumber(sceneNumber)) {
                return;
            }

            // boolean flag = false;
            if (sceneOutputMap.get(sceneNumber) != null) {
                if (!isRollershutter()) {
                    this.outputValueBeforeSceneCall = this.outputValue;
                    this.outputValue = sceneOutputMap.get(sceneNumber).shortValue();
                    addEshThingStateUpdate(
                            new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_BRIGHTNESS, this.outputValue));
                } else {
                    this.outputValueBeforeSceneCall = this.slatPosition;
                    this.slatPosition = sceneOutputMap.get(sceneNumber);
                    addEshThingStateUpdate(
                            new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION, this.slatPosition));
                }
                // flag = true;
            } else {
                this.deviceStateUpdates
                        .add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SCENE_OUTPUT, sceneNumber));
            }
            activeSceneNumber = sceneNumber;
            informLastSceneAboutSceneCall(sceneNumber);

            // return flag;
        }
    }

    private boolean checkSceneNumber(Short sceneNumber) {
        switch (sceneNumber) {
            // on scenes
            case 51:
            case 14:
                if (isDimmable()) {
                    this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, 1));
                }

                if (isRollershutter()) {
                    this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OPEN_CLOSE, 1));
                }
                return true;
            // off scenes
            case 13:
            case 50:
                if (isDimmable()) {
                    this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, -1));
                }
                if (isRollershutter()) {
                    this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OPEN_CLOSE, -1));
                }
                return true;
            // increase scenes
            case 11:
            case 43:
            case 45:
            case 47:
            case 49:
                if (isDimmable()) {
                    if (outputValue == maxOutputValue) {
                        return true;
                    }
                    if ((outputValue + getDimmStep()) > maxOutputValue) {
                        this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, 1));
                    } else {
                        this.updateInternalDeviceState(new DeviceStateUpdateImpl(
                                DeviceStateUpdate.UPDATE_BRIGHTNESS_INCREASE, outputValue + getDimmStep()));
                    }
                }

                if (isRollershutter()) {
                    if (slatPosition == maxSlatPosition) {
                        return true;
                    }
                    if ((slatPosition + getDimmStep()) > slatPosition) {
                        this.updateInternalDeviceState(
                                new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION, maxSlatPosition));
                    } else {
                        this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_INCREASE,
                                slatPosition + getDimmStep()));
                    }
                }
                return true;
            // decrease scenes
            case 12:
            case 42:
            case 44:
            case 46:
            case 48:
                if (isDimmable()) {
                    if (outputValue == minOutputValue) {
                        return true;
                    }
                    if ((outputValue - getDimmStep()) <= minOutputValue) {
                        if (isOn) {
                            this.updateInternalDeviceState(
                                    new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, -1));
                        }
                    } else {
                        this.updateInternalDeviceState(new DeviceStateUpdateImpl(
                                DeviceStateUpdate.UPDATE_BRIGHTNESS_DECREASE, outputValue - getDimmStep()));
                    }
                }

                if (isRollershutter()) {
                    if (slatPosition == minSlatPosition) {
                        return true;
                    }
                    if ((slatPosition + getDimmStep()) < slatPosition) {
                        this.updateInternalDeviceState(
                                new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION, minSlatPosition));
                    } else {
                        this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_DECREASE,
                                slatPosition - getDimmStep()));
                    }
                }

                return true;
            // Stop scenes
            case 52:
            case 53:
            case 54:
            case 55:
            case 15:
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OUTPUT_VALUE, 0));
                return true;
            // Area Stepping continue scenes
            case 10:
                // TODO: gute Frage was passiert hier?
                return true;

            // Auto-Off:
            case 40:
                // TOTO: checken ob das stimmt.
                if (isDimmable()) {
                    this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, -1));
                }
                if (isRollershutter()) {
                    this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OPEN_CLOSE, -1));
                }
                return true;

            default:
                return false;
        }
    }

    private void informLastSceneAboutSceneCall(short sceneNumber) {
        if (this.activeScene != null && this.activeScene.getSceneID() != sceneNumber) {
            this.activeScene.deviceSceneChanged(sceneNumber);
            this.activeScene = null;
        }
    }

    @Override
    public synchronized void undoScene() {
        this.deviceStateUpdates
                .add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_UNDO_SCENE, this.activeSceneNumber));
    }

    private synchronized void internalUndoScene() {
        if (!isRollershutter()) {
            this.outputValue = (short) this.outputValueBeforeSceneCall;
            addEshThingStateUpdate(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_BRIGHTNESS, this.outputValue));
        } else {
            this.slatPosition = this.outputValueBeforeSceneCall;
            addEshThingStateUpdate(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION, this.slatPosition));
        }

        if (this.activeScene != null) {
            informLastSceneAboutSceneCall((short) -1);
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
    public int getSceneOutputValue(short sceneId) {
        synchronized (sceneOutputMap) {
            if (sceneOutputMap.containsKey(sceneId)) {
                return sceneOutputMap.get(sceneId);
            }
        }
        return -1;
    }

    @Override
    public void setSceneOutputValue(short sceneId, int value) {
        synchronized (sceneOutputMap) {
            sceneOutputMap.put(sceneId, value);
            if (listener != null) {
                listener.onSceneConfigAdded(sceneId);
            }
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

    /**** End-Scenes ****/

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

    // for ESH

    // private List<DeviceStateUpdate> eshThingStateUpdates = Collections.synchronizedList(new
    // LinkedList<DeviceStateUpdate>());//new LinkedList<DeviceStateUpdate>();
    private List<DeviceStateUpdate> deviceStateUpdates = Collections
            .synchronizedList(new LinkedList<DeviceStateUpdate>());// new LinkedList<DeviceStateUpdate>();

    // private boolean isAddToESH = false;

    // save the last update time of the sensor data
    private long lastElectricMeterUpdate = System.currentTimeMillis();
    private long lastOutputCurrentUpdate = System.currentTimeMillis();
    private long lastActivePowerUpdate = System.currentTimeMillis();

    // sensor data refresh priorities
    private String activePowerRefreshPriority = DigitalSTROMConfig.REFRESH_PRIORITY_NEVER;
    private String electricMeterRefreshPriority = DigitalSTROMConfig.REFRESH_PRIORITY_NEVER;
    private String outputCurrentRefreshPriority = DigitalSTROMConfig.REFRESH_PRIORITY_NEVER;

    @Override
    public boolean isActivePowerUpToDate() {
        return isOn && !isRollershutter()
                && !this.activePowerRefreshPriority.contains(DigitalSTROMConfig.REFRESH_PRIORITY_NEVER)
                        ? (this.lastActivePowerUpdate + DigitalSTROMConfig.SENSORDATA_REFRESH_INTERVAL) > System
                                .currentTimeMillis()
                        : true;
    }

    @Override
    public boolean isElectricMeterUpToDate() {
        return isOn && !isRollershutter()
                && !this.electricMeterRefreshPriority.contains(DigitalSTROMConfig.REFRESH_PRIORITY_NEVER)
                        ? (this.lastElectricMeterUpdate + DigitalSTROMConfig.SENSORDATA_REFRESH_INTERVAL) > System
                                .currentTimeMillis()
                        : true;
    }

    @Override
    public boolean isOutputCurrentUpToDate() {
        return isOn && !isRollershutter()
                && !this.outputCurrentRefreshPriority.contains(DigitalSTROMConfig.REFRESH_PRIORITY_NEVER)
                        ? (this.lastOutputCurrentUpdate + DigitalSTROMConfig.SENSORDATA_REFRESH_INTERVAL) > System
                                .currentTimeMillis()
                        : true;
    }

    // 1. Unterscheidung zwischen nötigen und nich nötigen Sensordatenabfragen?
    // z.B. Lampe macht sinn, Rollershutter nicht, weil nur beim runter bzw. hochfahren Strom verbraucht wird
    // 2. Wenn der Refreshinterval vom Nutzer bestimmt wird Zeit übergeben (eigene var, wegen Übergabe des Devices),
    // da diese der Bridge-config entnommen werden muss
    @Override
    public boolean isSensorDataUpToDate() {
        return isOn && !isRollershutter() ? // Überprüfen ob es noch weitere gibt, bei denen es keinen Sinn macht
                                            // Sensordaten zu erfassen
        isActivePowerUpToDate() && isElectricMeterUpToDate() && isOutputCurrentUpToDate() : true;
    }

    @Override
    public void setSensorDataRefreshPriority(String activePowerRefreshPriority, String electricMeterRefreshPriority,
            String outputCurrentRefreshPriority) {
        if (checkPriority(activePowerRefreshPriority) != null) {
            this.activePowerRefreshPriority = activePowerRefreshPriority;
        }
        if (checkPriority(electricMeterRefreshPriority) != null) {
            this.electricMeterRefreshPriority = electricMeterRefreshPriority;
        }
        if (checkPriority(outputCurrentRefreshPriority) != null) {
            this.outputCurrentRefreshPriority = outputCurrentRefreshPriority;
        }

    }

    private String checkPriority(String priority) {
        switch (priority) {
            case DigitalSTROMConfig.REFRESH_PRIORITY_HIGH:
                break;
            case DigitalSTROMConfig.REFRESH_PRIORITY_MEDIUM:
                break;
            case DigitalSTROMConfig.REFRESH_PRIORITY_LOW:
                break;
            case DigitalSTROMConfig.REFRESH_PRIORITY_NEVER:
                return null;
            default:
                logger.error("Sensor data update priority do not exist! Please check the input!");
                return null;
        }
        return priority;
    }

    @Override
    public String getActivePowerRefreshPriority() {
        return this.activePowerRefreshPriority;
    }

    @Override
    public String getElectricMeterRefreshPriority() {
        return this.electricMeterRefreshPriority;
    }

    @Override
    public String getOutputCurrentRefreshPriority() {
        return this.outputCurrentRefreshPriority;
    }

    @Override
    public boolean isDeviceUpToDate() {
        return this.deviceStateUpdates.isEmpty();
    }

    @Override
    public DeviceStateUpdate getNextDeviceUpdateState() {
        return !this.deviceStateUpdates.isEmpty() ? this.deviceStateUpdates.remove(0) : null;
    }

    @Override
    public synchronized void updateInternalDeviceState(DeviceStateUpdate deviceStateUpdate) {
        if (deviceStateUpdate != null) {
            switch (deviceStateUpdate.getType()) {
                case DeviceStateUpdate.UPDATE_BRIGHTNESS_DECREASE:
                case DeviceStateUpdate.UPDATE_BRIGHTNESS_INCREASE:
                case DeviceStateUpdate.UPDATE_BRIGHTNESS:
                    this.outputValue = (short) deviceStateUpdate.getValue();
                    if (this.outputValue <= 0) {
                        this.isOn = false;
                        setActivePower(0);
                        setOutputCurrent(0);
                        setElectricMeter(0);
                    } else {
                        this.isOn = true;
                        setCachedMeterData();
                    }
                    break;
                case DeviceStateUpdate.UPDATE_ON_OFF:
                    if (deviceStateUpdate.getValue() < 0) {
                        this.outputValue = 0;
                        this.isOn = false;
                        setActivePower(0);
                        setOutputCurrent(0);
                        setElectricMeter(0);
                    } else {
                        this.outputValue = this.maxOutputValue;
                        this.isOn = true;
                        setCachedMeterData();
                    }
                    break;
                case DeviceStateUpdate.UPDATE_OPEN_CLOSE:
                    if (deviceStateUpdate.getValue() < 0) {
                        this.slatPosition = 0;
                        this.isOpen = false;
                    } else {
                        this.outputValue = this.maxOutputValue;
                        this.isOpen = true;
                    }
                    break;
                case DeviceStateUpdate.UPDATE_SLAT_DECREASE:
                case DeviceStateUpdate.UPDATE_SLAT_INCREASE:
                case DeviceStateUpdate.UPDATE_SLATPOSITION:
                    this.slatPosition = deviceStateUpdate.getValue();
                    break;
                case DeviceStateUpdate.UPDATE_ELECTRIC_METER:
                    setElectricMeter(deviceStateUpdate.getValue());
                    return;
                case DeviceStateUpdate.UPDATE_OUTPUT_CURRENT:
                    setOutputCurrent(deviceStateUpdate.getValue());
                    return;
                case DeviceStateUpdate.UPDATE_ACTIVE_POWER:
                    setActivePower(deviceStateUpdate.getValue());
                    return;
                case DeviceStateUpdate.UPDATE_CALL_SCENE:
                    this.internalCallScene((short) deviceStateUpdate.getValue());
                    return;
                case DeviceStateUpdate.UPDATE_UNDO_SCENE:
                    this.internalUndoScene();
                    return;
                default:
                    return;
            }

            addEshThingStateUpdate(deviceStateUpdate);

        }
    }

    private DeviceStatusListener listener = null;

    @Override
    public void registerDeviceStateListener(DeviceStatusListener listener) {
        if (listener != null) {
            this.listener = listener;
            listener.onDeviceAdded(this);
        }
    }

    @Override
    public void unregisterDeviceStateListener() {
        this.listener = null;
        // listener.onDeviceRemoved(this);
    }

    @Override
    public boolean isListenerRegisterd() {
        return (listener != null);
    }

    private void setCachedMeterData() {
        logger.debug("load cached sensor data");
        Integer[] cachedSensorData = this.cachedSensorMeterData.get(this.getOutputValue());
        if (cachedSensorData != null) {
            if (cachedSensorData[0] != null
                    && !this.activePowerRefreshPriority.contains(DigitalSTROMConfig.REFRESH_PRIORITY_NEVER)) {
                this.activePower = cachedSensorData[0];
                addEshThingStateUpdate(
                        new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ACTIVE_POWER, cachedSensorData[0]));

            }
            if (cachedSensorData[1] != null
                    && !this.electricMeterRefreshPriority.contains(DigitalSTROMConfig.REFRESH_PRIORITY_NEVER)) {
                this.electricMeter = cachedSensorData[1];
                addEshThingStateUpdate(
                        new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ELECTRIC_METER, cachedSensorData[1]));

            }
            if (cachedSensorData[2] != null
                    && !this.outputCurrentRefreshPriority.contains(DigitalSTROMConfig.REFRESH_PRIORITY_NEVER)) {
                this.outputCurrent = cachedSensorData[2];
                addEshThingStateUpdate(
                        new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OUTPUT_CURRENT, cachedSensorData[2]));

            }
        }
    }

    /**
     * if the device is added to ESH we save ever device state update to change it in ESH
     * if the device isn't added ESH we only save the current device state
     */
    private void addEshThingStateUpdate(DeviceStateUpdate deviceStateUpdate) {
        if (listener != null) {
            logger.debug("Inform listener about device state changed: type: " + deviceStateUpdate.getType()
                    + ", value: " + deviceStateUpdate.getValue());
            listener.onDeviceStateChanged(deviceStateUpdate);
        }
    }

}