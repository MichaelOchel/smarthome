/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMConfiguration.DigitalSTROMConfig;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.DigitalSTROMConnectionListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.DigitalSTROMManagerStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.SceneStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.TotalPowerConsumptionListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.StateEnums.ManagerStates;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.StateEnums.ManagerTypes;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMDeviceStatusManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMSceneManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMStructureManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.SceneSensorJobExecutor;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.SensorJobExecutor;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.sensorJob.SensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.sensorJob.impl.DeviceConsumptionSensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.sensorJob.impl.DeviceOutputValueSensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.sensorJob.impl.SceneConfigSensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.sensorJob.impl.SceneOutputValueSensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMServerConnection.DigitalSTROMAPI;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.CachedMeteringValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DSID;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceStateUpdateImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.MeteringUnitsEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.OutputModeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.SensorEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.InternalScene;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.constants.ApartmentSceneEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.constants.SceneEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DigitalSTROMDeviceStatusManagerImpl} is the implementation of the the
 * {@link DigitalSTROMDeviceStatusManager}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class DigitalSTROMDeviceStatusManagerImpl implements DigitalSTROMDeviceStatusManager {

    private Logger logger = LoggerFactory.getLogger(DigitalSTROMDeviceStatusManagerImpl.class);

    private DigitalSTROMConnectionManager connMan;
    private DigitalSTROMStructureManager strucMan;
    private DigitalSTROMSceneManager sceneMan;
    private DigitalSTROMAPI digitalSTROMClient;

    private SensorJobExecutor sensorJobExecuter = null;
    private SceneSensorJobExecutor sceneJobExecuter = null;

    private static final int POLLING_FREQUENCY = DigitalSTROMConfig.POLLING_FREQUENCY; // in seconds
    private int BIN_CHECK_TIME = DigitalSTROMConfig.BIN_CHECK_TIME; // in milliseconds

    private Thread schedduler2 = null;

    /**** States ****/
    private long lastBinCheck = 0;
    ManagerStates state = ManagerStates.stopped;

    private List<TrashDevice> trashDevices = new LinkedList<TrashDevice>();

    private DeviceStatusListener deviceDiscovery = null;
    private TotalPowerConsumptionListener totalPowerConsumptionListener = null;
    private DigitalSTROMManagerStatusListener statusListener = null;
    private int tempConsumtion = 0;
    private int totalPowerConsumption = 0;
    private int tempEnergyMeter = 0;
    private int totalEnergyMeter = 0;

    public DigitalSTROMDeviceStatusManagerImpl(String host, String user, String password, String appToken) {
        this.connMan = new DigitalSTROMConnectionManagerImpl(host, user, password, false);
        this.digitalSTROMClient = connMan.getDigitalSTROMAPI();
        this.strucMan = new DigitalSTROMStructureManagerImpl();
        this.sceneMan = new DigitalSTROMSceneManagerImpl(connMan, strucMan);
    }

    public DigitalSTROMDeviceStatusManagerImpl(DigitalSTROMConnectionManager connMan,
            DigitalSTROMStructureManager strucMan, DigitalSTROMSceneManager sceneMan) {
        this.connMan = connMan;
        this.digitalSTROMClient = connMan.getDigitalSTROMAPI();
        this.strucMan = strucMan;
        this.sceneMan = sceneMan;
    }

    public DigitalSTROMDeviceStatusManagerImpl(DigitalSTROMConnectionManager connMan) {
        this.connMan = connMan;
        this.digitalSTROMClient = connMan.getDigitalSTROMAPI();
        this.strucMan = new DigitalSTROMStructureManagerImpl();
        this.sceneMan = new DigitalSTROMSceneManagerImpl(connMan, strucMan);
    }

    private boolean shutdown = true;
    private PollingRunnable pollingRunnable = null;

    private class PollingRunnable implements Runnable {
        private boolean devicesLoaded = false;

        @Override
        public void run() {
            if (!devicesLoaded) {
                stateChanged(ManagerStates.running);
            } else {
                stateChanged(ManagerStates.initialasing);
            }
            while (!shutdown) {
                if (connMan.checkConnection()) {
                    logger.debug("start");
                    HashMap<DSID, Device> tempDeviceMap;
                    if (strucMan.getDeviceMap() != null) {
                        tempDeviceMap = new HashMap<DSID, Device>(strucMan.getDeviceMap());
                    } else {
                        tempDeviceMap = new HashMap<DSID, Device>();
                    }

                    List<Device> currentDeviceList = digitalSTROMClient.getApartmentDevices(connMan.getSessionToken(),
                            false);

                    // update the current total power consumption

                    if (totalPowerConsumptionListener != null) {
                        List<String> meters = digitalSTROMClient.getMeterList(connMan.getSessionToken());
                        List<CachedMeteringValue> cachedConsumptionMeteringValues = digitalSTROMClient.getLatest(
                                connMan.getSessionToken(), MeteringTypeEnum.consumption, meters, MeteringUnitsEnum.W);
                        List<CachedMeteringValue> cachedEnergyMeteringValues = digitalSTROMClient.getLatest(
                                connMan.getSessionToken(), MeteringTypeEnum.energy, meters, MeteringUnitsEnum.Wh);
                        if (cachedConsumptionMeteringValues != null) {
                            tempConsumtion = 0;
                            for (CachedMeteringValue value : cachedConsumptionMeteringValues) {

                                tempConsumtion += value.getValue();

                            }
                            if (tempConsumtion != totalPowerConsumption) {
                                totalPowerConsumption = tempConsumtion;
                                totalPowerConsumptionListener.onTotalPowerConsumptionChanged(totalPowerConsumption);
                            }
                        }

                        if (cachedEnergyMeteringValues != null) {
                            tempEnergyMeter = 0;
                            for (CachedMeteringValue value : cachedEnergyMeteringValues) {

                                tempEnergyMeter += value.getValue();

                            }
                            if (tempEnergyMeter != totalEnergyMeter) {
                                totalEnergyMeter = tempEnergyMeter;
                                totalPowerConsumptionListener.onEnergyMeterValueChanged(totalEnergyMeter);
                            }
                        }
                    }

                    while (!currentDeviceList.isEmpty()) {
                        Device currentDevice = currentDeviceList.remove(0);
                        DSID currentDeviceDSID = currentDevice.getDSID();
                        Device eshDevice = tempDeviceMap.remove(currentDeviceDSID);

                        if (eshDevice != null) {

                            checkDeviceConfig(currentDevice, eshDevice);

                            if (eshDevice.isPresent()) {
                                if (eshDevice.isListenerRegisterd()) {
                                    logger.debug("Check device updates");
                                    // check device state updates
                                    while (!eshDevice.isDeviceUpToDate()) {
                                        DeviceStateUpdate deviceStateUpdate = eshDevice.getNextDeviceUpdateState();
                                        if (deviceStateUpdate != null) {

                                            if (deviceStateUpdate.getType() != DeviceStateUpdate.UPDATE_BRIGHTNESS) {
                                                if (deviceStateUpdate.getType() == DeviceStateUpdate.UPDATE_SCENE_CONFIG
                                                        || deviceStateUpdate
                                                                .getType() == DeviceStateUpdate.UPDATE_SCENE_OUTPUT) {
                                                    updateSceneData(eshDevice, deviceStateUpdate);
                                                } else {
                                                    sendComandsToDSS(eshDevice, deviceStateUpdate);
                                                }
                                            } else {
                                                DeviceStateUpdate nextDeviceStateUpdate = eshDevice
                                                        .getNextDeviceUpdateState();
                                                while (nextDeviceStateUpdate != null && nextDeviceStateUpdate
                                                        .getType() == DeviceStateUpdate.UPDATE_BRIGHTNESS) {
                                                    deviceStateUpdate = nextDeviceStateUpdate;
                                                    nextDeviceStateUpdate = eshDevice.getNextDeviceUpdateState();
                                                }
                                                sendComandsToDSS(eshDevice, deviceStateUpdate);
                                                if (nextDeviceStateUpdate != null) {
                                                    sendComandsToDSS(eshDevice, nextDeviceStateUpdate);
                                                }
                                            }
                                        }
                                    }

                                    // check if device need sensor data update
                                    if (!eshDevice.isSensorDataUpToDate()) {
                                        logger.debug("Device need SensorData update");

                                        if (!eshDevice.isActivePowerUpToDate()) {
                                            logger.debug("Device need active power SensorData update");
                                            updateSensorData(
                                                    new DeviceConsumptionSensorJob(eshDevice, SensorEnum.ACTIVE_POWER),
                                                    eshDevice.getActivePowerRefreshPriority());
                                        }

                                        if (!eshDevice.isOutputCurrentUpToDate()) {
                                            logger.debug("Device need ouput current SensorData update");
                                            updateSensorData(
                                                    new DeviceConsumptionSensorJob(eshDevice,
                                                            SensorEnum.OUTPUT_CURRENT),
                                                    eshDevice.getOutputCurrentRefreshPriority());
                                        }

                                        if (!eshDevice.isElectricMeterUpToDate()) {
                                            logger.debug("Device need electric meter SensorData update");
                                            updateSensorData(
                                                    new DeviceConsumptionSensorJob(eshDevice,
                                                            SensorEnum.ELECTRIC_METER),
                                                    eshDevice.getElectricMeterRefreshPriority());
                                        }
                                    }
                                } else {
                                    while (!eshDevice.isDeviceUpToDate()) {
                                        DeviceStateUpdate deviceStateUpdate = eshDevice.getNextDeviceUpdateState();
                                        if (deviceStateUpdate != null) {
                                            if (deviceStateUpdate.getType() == DeviceStateUpdate.UPDATE_SCENE_CONFIG
                                                    || deviceStateUpdate
                                                            .getType() == DeviceStateUpdate.UPDATE_SCENE_OUTPUT) {
                                                updateSceneData(eshDevice, deviceStateUpdate);
                                            }
                                        }
                                    }
                                }
                            }

                        } else {

                            logger.debug("Found new Device!");

                            if (trashDevices.isEmpty()) {
                                strucMan.addDeviceToStructure(currentDevice);
                                logger.debug("trashDevices are empty, add Device with dSID "
                                        + currentDevice.getDSID().toString() + "to the deviceMap!");
                            } else {
                                logger.debug("Search device in trashDevices.");

                                boolean found = false;
                                for (TrashDevice trashDevice : trashDevices) {
                                    if (trashDevice != null) {
                                        if (trashDevice.getDevice().equals(currentDevice)) {
                                            Device device = trashDevice.getDevice();
                                            trashDevices.remove(trashDevice);
                                            strucMan.addDeviceToStructure(device);
                                            found = true;
                                            logger.debug(
                                                    "Found device in trashDevices, add TrashDevice to the deviceMap!");
                                        }
                                    }
                                }

                                if (!found) {
                                    strucMan.addDeviceToStructure(currentDevice);
                                    logger.debug(
                                            "Can't find device in trashDevices, add Device with dSUID: {} to the deviceMap!",
                                            currentDeviceDSID);
                                }
                            }

                            // TODO: may move discovery in DigitalSTROMStructureManagerImpl.addDevice
                            if (deviceDiscovery != null) {
                                if (currentDevice.isDeviceWithOutput()) {
                                    deviceDiscovery.onDeviceAdded(currentDevice);
                                    logger.debug("inform DeviceStatusListener: \""
                                            + DeviceStatusListener.DEVICE_DESCOVERY + "\" about Device with DSID: \""
                                            + currentDevice.getDSUID() + "\" added.");
                                }

                            } else {
                                logger.debug(
                                        "The digitalSTROM-Device-Discovery is disabled, can't inform device descovery about found device.");
                            }
                        }
                    }

                    if (!devicesLoaded) {
                        logger.debug("Devices loaded");
                        devicesLoaded = true;
                        stateChanged(ManagerStates.running);
                    }

                    if (!sceneMan.scenesGenerated()) {
                        logger.debug("generateScenes");
                        sceneMan.generateScenes();
                    }

                    for (Device device : tempDeviceMap.values())

                    {
                        logger.debug("Found removed Devices.");

                        trashDevices.add(new TrashDevice(device));
                        strucMan.deleteDevice(device);
                        logger.debug("Add Device: " + device.getDSID().getValue() + " to trashDevices");

                        if (deviceDiscovery != null) {
                            deviceDiscovery.onDeviceRemoved(device);
                            logger.debug("inform DeviceStatusListener: " + DeviceStatusListener.DEVICE_DESCOVERY
                                    + " about Device: " + device.getDSUID() + " removed.");
                        } else {
                            logger.debug(
                                    "The digitalSTROM-Device-Discovery is disabled, can't inform device descovery about removed device.");
                        }
                    }

                    if (!trashDevices.isEmpty() && (lastBinCheck + BIN_CHECK_TIME < System.currentTimeMillis()))

                    {
                        for (TrashDevice trashDevice : trashDevices) {
                            if (trashDevice.isTimeToDelete(Calendar.getInstance().get(Calendar.DAY_OF_YEAR))) {
                                logger.debug("Found trashDevice that have to deleate!");
                                trashDevices.remove(trashDevice);
                                logger.debug("Delete trashDevice: " + trashDevice.getDevice().getDSID().getValue());
                            }
                        }
                        lastBinCheck = System.currentTimeMillis();
                    }
                }

                try {
                    synchronized (this) {
                        wait(POLLING_FREQUENCY);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            stateChanged(ManagerStates.stopped);
        }
    };

    @Override
    public ManagerTypes getManagerType() {
        return ManagerTypes.deviceStatusManager;
    }

    @Override
    public ManagerStates getManagerState() {
        return state;
    }

    private synchronized void stateChanged(ManagerStates state) {
        if (statusListener != null) {
            this.state = state;
            statusListener.onStatusChanged(ManagerTypes.deviceStatusManager, state);
        }
    }

    @Override
    public synchronized void start() {
        logger.debug("start Thread");
        if (shutdown) {
            shutdown = false;
            if (schedduler2 == null || !schedduler2.isAlive()) {
                this.pollingRunnable = new PollingRunnable();
                schedduler2 = new Thread(pollingRunnable);
                schedduler2.start();
            }
            sceneMan.start();
        }
        if (sceneJobExecuter != null) {
            this.sceneJobExecuter.startExecuter();
        }

        if (sensorJobExecuter != null) {
            this.sensorJobExecuter.startExecuter();
        }
    }

    @Override
    public synchronized void stop() {
        shutdown = true;
        sceneMan.stop();
        schedduler2 = null;
        if (sceneJobExecuter != null) {
            this.sceneJobExecuter.shutdown();
        }

        if (sensorJobExecuter != null) {
            this.sensorJobExecuter.shutdown();
        }
    }

    /**
     * The {@link TrashDevice} saves not present {@link Device}'s, but at this point not deleted from the
     * digitalSTROM-System, temporary to get back the configuration of the {@link Device}'s faster.
     *
     * @author Michael Ochel - Initial contribution
     * @author Matthias Siegele - Initial contribution
     *
     */
    private class TrashDevice {
        private Device device;
        private int timeStamp;

        /**
         * Creates a new {@link TrashDevice}.
         *
         * @param device
         */
        public TrashDevice(Device device) {
            this.device = device;
            this.timeStamp = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        }

        /**
         * Returns the saved {@link Device}.
         *
         * @return device
         */
        public Device getDevice() {
            return device;
        }

        /**
         * Returns true if the time for the {@link TrashDevice} is over and it can be deleted.
         *
         * @param dayOfYear
         * @return true = time to delete | false = not time to delete
         */
        public boolean isTimeToDelete(int dayOfYear) {
            return this.timeStamp + DigitalSTROMConfig.TRASH_DEVICE_DELEATE_TIME <= dayOfYear;
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof TrashDevice
                    ? this.device.getDSID().equals(((TrashDevice) object).getDevice().getDSID()) : false;
        }

    }

    private void checkDeviceConfig(Device newDevice, Device internalDevice) {
        if (newDevice == null || internalDevice == null) {
            return;
        }
        // check device availability has changed and inform the deviceStatusListener about the change.
        // NOTE:
        // The device is not availability for the DigitalSTROM-Server, it has not been deleted and are therefore set to
        // OFFLINE.
        // To delete an alternate algorithm is responsible .
        if (newDevice.isPresent() != internalDevice.isPresent()) {
            internalDevice.setIsPresent(newDevice.isPresent());
        }
        if (newDevice.getMeterDSID() != null && !newDevice.getMeterDSID().equals(internalDevice.getMeterDSID())) {
            internalDevice.setMeterDSID(newDevice.getMeterDSID().getValue());
        }
        if (newDevice.getFunctionalColorGroup() != null
                && !newDevice.getFunctionalColorGroup().equals(internalDevice.getFunctionalColorGroup())) {
            internalDevice.setFunctionalColorGroup(newDevice.getFunctionalColorGroup());
        }

        if (newDevice.getName() != null && !newDevice.getName().equals(internalDevice.getName())) {
            internalDevice.setName(newDevice.getName());
        }

        if (newDevice.getOutputMode() != null && !newDevice.getOutputMode().equals(internalDevice.getOutputMode())) {
            internalDevice.setOutputMode(newDevice.getOutputMode());
        }
        strucMan.updateDevice(newDevice);
    }

    private long lastSceneCall = 0;
    private long sleepTime = 0;

    @Override
    public synchronized void sendSceneComandsToDSS(InternalScene scene, boolean call_undo) {
        if (lastSceneCall + 1000 > System.currentTimeMillis()) {
            sleepTime = System.currentTimeMillis() - lastSceneCall;
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (this.connMan.checkConnection()) {
            lastSceneCall = System.currentTimeMillis();
            boolean requestSucsessfull = false;
            if (scene.getZoneID() == 0) {
                if (call_undo) {
                    requestSucsessfull = this.digitalSTROMClient.callApartmentScene(connMan.getSessionToken(),
                            scene.getGroupID(), null, ApartmentSceneEnum.getApartmentScene(scene.getSceneID()), false);
                } else {
                    requestSucsessfull = this.digitalSTROMClient.undoApartmentScene(connMan.getSessionToken(),
                            scene.getGroupID(), null, ApartmentSceneEnum.getApartmentScene(scene.getSceneID()));
                }
            } else {
                if (call_undo) {
                    requestSucsessfull = this.digitalSTROMClient.callZoneScene(connMan.getSessionToken(),
                            scene.getZoneID(), null, scene.getGroupID(), null, SceneEnum.getScene(scene.getSceneID()),
                            false);
                } else {
                    requestSucsessfull = this.digitalSTROMClient.undoZoneScene(connMan.getSessionToken(),
                            scene.getZoneID(), null, scene.getGroupID(), null, SceneEnum.getScene(scene.getSceneID()));
                }
            }

            logger.debug("scenecall sucsess?: " + requestSucsessfull);
            if (requestSucsessfull) {
                this.sceneMan.addEcho(scene.getID());
                if (call_undo) {
                    scene.activateScene();
                } else {
                    scene.deactivateScene();
                }
            }
        }
    }

    @Override
    public synchronized void sendStopComandsToDSS(Device device) {
        if (connMan.checkConnection()) {
            if (this.digitalSTROMClient.callDeviceScene(connMan.getSessionToken(), device.getDSID(), null,
                    SceneEnum.STOP, true)) {
                short outputIndex = DeviceConstants.DEVICE_SENSOR_OUTPUT;
                // TODO: right?
                if (device.getOutputMode().equals(OutputModeEnum.POSITION_CON_US)) {
                    outputIndex = DeviceConstants.DEVICE_SENSOR_SLAT_OUTPUT;
                }

                int outputValue = this.digitalSTROMClient.getDeviceOutputValue(connMan.getSessionToken(),
                        device.getDSID(), null, outputIndex);

                if (outputValue != -1) {
                    if (!device.isRollershutter()) {
                        device.updateInternalDeviceState(
                                new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_BRIGHTNESS, outputValue));
                    } else {
                        device.updateInternalDeviceState(
                                new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION, outputValue));
                    }
                }
            }
        }
    }

    @Override
    public synchronized void sendComandsToDSS(Device device) {
        while (!device.isDeviceUpToDate()) {
            DeviceStateUpdate deviceStateUpdate = device.getNextDeviceUpdateState();
            if (deviceStateUpdate.getType() != DeviceStateUpdate.UPDATE_BRIGHTNESS) {
                sendComandsToDSS(device, deviceStateUpdate);
            } else {
                DeviceStateUpdate nextDeviceStateUpdate = device.getNextDeviceUpdateState();
                while (nextDeviceStateUpdate != null
                        && nextDeviceStateUpdate.getType() == DeviceStateUpdate.UPDATE_BRIGHTNESS) {
                    deviceStateUpdate = nextDeviceStateUpdate;
                    nextDeviceStateUpdate = device.getNextDeviceUpdateState();
                }
                sendComandsToDSS(device, deviceStateUpdate);
                if (nextDeviceStateUpdate != null) {
                    sendComandsToDSS(device, nextDeviceStateUpdate);
                }
            }
        }
    }

    private synchronized void sendComandsToDSS(Device device, DeviceStateUpdate deviceStateUpdate) {

        if (connMan.checkConnection()) {
            boolean requestSucsessfull = false;

            if (deviceStateUpdate != null) {
                switch (deviceStateUpdate.getType()) {
                    case DeviceStateUpdate.UPDATE_BRIGHTNESS_DECREASE:
                    case DeviceStateUpdate.UPDATE_SLAT_DECREASE:
                        requestSucsessfull = digitalSTROMClient.decreaseValue(connMan.getSessionToken(),
                                device.getDSID());
                        if (requestSucsessfull) {
                            sceneMan.addEcho(device.getDSID().getValue(), (short) SceneEnum.DECREMENT.getSceneNumber());
                        }
                        break;
                    case DeviceStateUpdate.UPDATE_BRIGHTNESS_INCREASE:
                    case DeviceStateUpdate.UPDATE_SLAT_INCREASE:
                        requestSucsessfull = digitalSTROMClient.increaseValue(connMan.getSessionToken(),
                                device.getDSID());
                        if (requestSucsessfull) {
                            sceneMan.addEcho(device.getDSID().getValue(), (short) SceneEnum.INCREMENT.getSceneNumber());
                        }
                        break;
                    case DeviceStateUpdate.UPDATE_BRIGHTNESS:
                        requestSucsessfull = digitalSTROMClient.setDeviceValue(connMan.getSessionToken(),
                                device.getDSID(), null, deviceStateUpdate.getValue());
                        break;
                    case DeviceStateUpdate.UPDATE_OPEN_CLOSE:
                    case DeviceStateUpdate.UPDATE_ON_OFF:
                        if (deviceStateUpdate.getValue() > 0) {
                            requestSucsessfull = digitalSTROMClient.turnDeviceOn(connMan.getSessionToken(),
                                    device.getDSID(), null);
                            if (requestSucsessfull) {
                                sceneMan.addEcho(device.getDSID().getValue(),
                                        (short) SceneEnum.MAXIMUM.getSceneNumber());
                            }
                        } else {
                            requestSucsessfull = digitalSTROMClient.turnDeviceOff(connMan.getSessionToken(),
                                    device.getDSID(), null);
                            if (requestSucsessfull) {
                                sceneMan.addEcho(device.getDSID().getValue(),
                                        (short) SceneEnum.MINIMUM.getSceneNumber());
                            }
                            if (sensorJobExecuter != null) {
                                sensorJobExecuter.removeSensorJobs(device);
                            }
                        }
                        break;
                    case DeviceStateUpdate.UPDATE_SLATPOSITION:
                        requestSucsessfull = digitalSTROMClient.setDeviceValue(connMan.getSessionToken(),
                                device.getDSID(), null, deviceStateUpdate.getValue());
                        break;
                    case DeviceStateUpdate.UPDATE_SLAT_STOP:
                        this.sendStopComandsToDSS(device);
                        break;
                    case DeviceStateUpdate.UPDATE_SLAT_MOVE:
                        if (deviceStateUpdate.getValue() > 0) {
                            requestSucsessfull = digitalSTROMClient.turnDeviceOn(connMan.getSessionToken(),
                                    device.getDSID(), null);
                            if (requestSucsessfull) {
                                sceneMan.addEcho(device.getDSID().getValue(),
                                        (short) SceneEnum.MAXIMUM.getSceneNumber());
                            }
                        } else {
                            requestSucsessfull = digitalSTROMClient.turnDeviceOff(connMan.getSessionToken(),
                                    device.getDSID(), null);
                            if (requestSucsessfull) {
                                sceneMan.addEcho(device.getDSID().getValue(),
                                        (short) SceneEnum.MINIMUM.getSceneNumber());
                            }
                            if (sensorJobExecuter != null) {
                                sensorJobExecuter.removeSensorJobs(device);
                            }
                        }
                        break;
                    case DeviceStateUpdate.UPDATE_CALL_SCENE:
                        if (SceneEnum.getScene(deviceStateUpdate.getValue()) != null) {
                            requestSucsessfull = digitalSTROMClient.callDeviceScene(connMan.getSessionToken(),
                                    device.getDSID(), null, SceneEnum.getScene(deviceStateUpdate.getValue()), true);
                        }
                        break;
                    case DeviceStateUpdate.UPDATE_UNDO_SCENE:
                        if (SceneEnum.getScene(deviceStateUpdate.getValue()) != null) {
                            requestSucsessfull = digitalSTROMClient.undoDeviceScene(connMan.getSessionToken(),
                                    device.getDSID(), SceneEnum.getScene(deviceStateUpdate.getValue()));
                        }
                        break;

                    default:
                        return;
                }

                if (requestSucsessfull) {
                    logger.debug("Send {} command to DSS and updateInternalDeviceState", deviceStateUpdate.getType());
                    device.updateInternalDeviceState(deviceStateUpdate);
                } else {
                    logger.debug("Can't send {} command to DSS!", deviceStateUpdate.getType());
                }
            }
        }
    }

    @Override
    public void updateSensorData(SensorJob sensorJob, String priority) {
        if (sensorJobExecuter == null) {
            sensorJobExecuter = new SensorJobExecutor(connMan);
            this.sensorJobExecuter.startExecuter();
        }
        if (sensorJob != null && priority != null) {
            if (priority.contains(DigitalSTROMConfig.REFRESH_PRIORITY_HIGH)) {
                sensorJobExecuter.addHighPriorityJob(sensorJob);
            } else if (priority.contains(DigitalSTROMConfig.REFRESH_PRIORITY_MEDIUM)) {
                sensorJobExecuter.addMediumPriorityJob(sensorJob);
            } else if (priority.contains(DigitalSTROMConfig.REFRESH_PRIORITY_LOW)) {
                sensorJobExecuter.addLowPriorityJob(sensorJob);
            } else {
                System.err.println("Sensor data update priority do not exist! Please check the input!");
                return;
            }
            logger.debug("Add new sensorJob with priority: {} to sensorJobExecuter", priority);

        }
    }

    @Override
    public void updateSceneData(Device device, DeviceStateUpdate deviceStateUpdate) {
        if (sceneJobExecuter == null) {
            sceneJobExecuter = new SceneSensorJobExecutor(connMan);
            this.sceneJobExecuter.startExecuter();
        }

        if (deviceStateUpdate != null) {
            if (deviceStateUpdate.getValue() < 1000) {
                if (deviceStateUpdate.getType().equals(DeviceStateUpdate.UPDATE_SCENE_OUTPUT)) {
                    sceneJobExecuter.addHighPriorityJob(
                            new SceneOutputValueSensorJob(device, (short) deviceStateUpdate.getValue()));
                    updateSensorData(new DeviceOutputValueSensorJob(device), DigitalSTROMConfig.REFRESH_PRIORITY_HIGH);
                } else {
                    sceneJobExecuter
                            .addHighPriorityJob(new SceneConfigSensorJob(device, (short) deviceStateUpdate.getValue()));
                }
            } else if (deviceStateUpdate.getValue() < 2000) {
                if (deviceStateUpdate.getType().equals(DeviceStateUpdate.UPDATE_SCENE_OUTPUT)) {
                    sceneJobExecuter.addMediumPriorityJob(
                            new SceneOutputValueSensorJob(device, (short) (deviceStateUpdate.getValue() - 1000)));
                } else {
                    sceneJobExecuter.addMediumPriorityJob(
                            new SceneConfigSensorJob(device, (short) (deviceStateUpdate.getValue() - 1000)));
                }
            } else if (deviceStateUpdate.getValue() >= 2000 && deviceStateUpdate.getValue() < 3000) {
                if (deviceStateUpdate.getType().equals(DeviceStateUpdate.UPDATE_SCENE_OUTPUT)) {
                    sceneJobExecuter.addLowPriorityJob(
                            new SceneOutputValueSensorJob(device, (short) (deviceStateUpdate.getValue() - 2000)));
                } else {
                    sceneJobExecuter.addLowPriorityJob(
                            new SceneConfigSensorJob(device, (short) (deviceStateUpdate.getValue() - 2000)));
                }
            } else {
                System.err.println("Sensor data update priority do not exist! Please check the input!");
                return;
            }
            logger.debug("Add new sceneSenesorJob with priority: {} to SceneSensorJobExecuter",
                    new Integer(deviceStateUpdate.getValue()).toString().charAt(0));

        }
    }

    @Override
    public void registerDeviceListener(DeviceStatusListener deviceListener) {
        if (deviceListener != null) {
            String id = deviceListener.getID();
            logger.debug("register DeviceListener with id: " + id);
            if (id.equals(DeviceStatusListener.DEVICE_DESCOVERY)) {
                this.deviceDiscovery = deviceListener;
                if (getManagerState().equals(ManagerStates.running)) {
                    for (Device device : strucMan.getDeviceMap().values()) {
                        deviceDiscovery.onDeviceAdded(device);
                    }
                }
                logger.debug("register device descovery");
            } else {
                Device intDevice = strucMan.getDeviceByDSID(deviceListener.getID());
                if (intDevice != null) {
                    intDevice.registerDeviceStateListener(deviceListener);
                } else {
                    // Fehlermeldung
                }
            }
        } else {
            // Fehlermeldung
        }

    }

    @Override
    public void unregisterDeviceListener(DeviceStatusListener deviceListener) {
        if (deviceListener != null) {
            String id = deviceListener.getID();
            if (id.equals(DeviceStatusListener.DEVICE_DESCOVERY)) {
                this.deviceDiscovery = null;
            } else {
                Device intDevice = strucMan.getDeviceByDSID(deviceListener.getID());
                if (intDevice != null) {
                    intDevice.unregisterDeviceStateListener();
                } else {
                    // Fehlermeldung
                }
            }
        } else {
            // Fehlermeldung
        }
    }

    @Override
    public void removeDevice(String dSID) {
        Device intDevice = strucMan.getDeviceByDSID(dSID);
        // TODO: maybe another method removeDevice(String dSID): Device intDevice in strucMan
        if (intDevice != null) {
            strucMan.deleteDevice(intDevice);
            trashDevices.add(new TrashDevice(intDevice));
        }
    }

    @Override
    public void registerTotalPowerConsumptionListener(TotalPowerConsumptionListener totalPowerConsumptionListener) {
        this.totalPowerConsumptionListener = totalPowerConsumptionListener;
    }

    @Override
    public void unregisterTotalPowerConsumptionListener() {
        this.totalPowerConsumptionListener = null;
    }

    @Override
    public void registerSceneListener(SceneStatusListener sceneListener) {
        this.sceneMan.registerSceneListener(sceneListener);
    }

    @Override
    public void unregisterSceneListener(SceneStatusListener sceneListener) {
        this.sceneMan.unregisterSceneListener(sceneListener);
    }

    @Override
    public void registerStatusListener(DigitalSTROMManagerStatusListener statusListener) {
        this.statusListener = statusListener;
        this.sceneMan.registerStatusListener(statusListener);
    }

    @Override
    public void unregisterStatusListener() {
        this.statusListener = null;
        this.sceneMan.unregisterStatusListener();
    }

    @Override
    public void reisterDigitalSTROMConnectionListener(DigitalSTROMConnectionListener connectionListener) {
        this.connMan.registerConnectionListener(connectionListener);
    }

    @Override
    public void unreisterDigitalSTROMConnectionListener() {
        this.connMan.unregisterConnectionListener();
    }
}
