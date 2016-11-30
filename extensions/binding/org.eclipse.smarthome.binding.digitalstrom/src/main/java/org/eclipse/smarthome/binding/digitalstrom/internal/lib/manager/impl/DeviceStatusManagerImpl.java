/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.config.Config;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.EventHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.EventListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.constants.EventNames;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.constants.EventResponseEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.types.EventItem;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.ConnectionListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.ManagerStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.SceneStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.TotalPowerConsumptionListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.stateEnums.ManagerStates;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.stateEnums.ManagerTypes;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.DeviceStatusManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.SceneManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.StructureManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.SceneReadingJobExecutor;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.SensorJobExecutor;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.SensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.impl.DeviceConsumptionSensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.impl.DeviceOutputValueSensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.impl.SceneConfigReadingJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.impl.SceneOutputValueReadingJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.DsAPI;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Circuit;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.CachedMeteringValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.DeviceBinarayInputEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringUnitsEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.SensorEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.DSID;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.DeviceStateUpdateImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.impl.DeviceImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.InternalScene;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.ApartmentSceneEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.SceneEnum;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link DeviceStatusManagerImpl} is the implementation of the the {@link DeviceStatusManager}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class DeviceStatusManagerImpl implements DeviceStatusManager, EventHandler {

    private Logger logger = LoggerFactory.getLogger(DeviceStatusManagerImpl.class);
    public static final List<String> SUPPORTED_EVENTS = Lists.newArrayList(EventNames.DEVICE_SENSOR_VALUE,
            EventNames.DEVICE_BINARY_INPUT_EVENT);

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(Config.THREADPOOL_NAME);
    private ScheduledFuture<?> pollingScheduler = null;

    public static final String GET_DETAILD_DEVICES = "/apartment/zones/zone0(*)/devices/*(*)/*(*)/*(*)";
    public static final String LAST_CALL_SCENE_QUERY = "/apartment/zones/*(*)/groups/*(*)/*(*)";

    private ConnectionManager connMan;
    private StructureManager strucMan;
    private SceneManager sceneMan;
    private DsAPI digitalSTROMClient;
    private Config config;

    private SensorJobExecutor sensorJobExecutor = null;
    private SceneReadingJobExecutor sceneJobExecutor = null;
    private EventListener eventListener = null;

    // private List<String> meters = null;
    private List<TrashDevice> trashDevices = new LinkedList<TrashDevice>();

    private long lastBinCheck = 0;
    private ManagerStates state = ManagerStates.STOPPED;

    private int tempConsumption = 0;
    // private int totalPowerConsumption = 0;
    private int tempEnergyMeter = 0;
    private int tempEnergyMeterWs = 0;
    // private int totalEnergyMeter = 0;

    private DeviceStatusListener deviceDiscovery = null;
    private TotalPowerConsumptionListener totalPowerConsumptionListener = null;
    private ManagerStatusListener statusListener = null;

    public DeviceStatusManagerImpl(Config config) {
        init(new ConnectionManagerImpl(config), null, null, null, null);
    }

    public DeviceStatusManagerImpl(String hostAddress, String user, String password, String appToken) {
        init(new ConnectionManagerImpl(hostAddress, user, password, false), null, null, null, null);
    }

    public DeviceStatusManagerImpl(ConnectionManager connMan, StructureManager strucMan, SceneManager sceneMan) {
        init(connMan, strucMan, sceneMan, null, null);
    }

    public DeviceStatusManagerImpl(ConnectionManager connMan, StructureManager strucMan, SceneManager sceneMan,
            ManagerStatusListener statusListener) {
        init(connMan, strucMan, sceneMan, statusListener, null);
    }

    public DeviceStatusManagerImpl(ConnectionManager connMan, StructureManager strucMan, SceneManager sceneMan,
            ManagerStatusListener statusListener, EventListener eventListener) {
        init(connMan, strucMan, sceneMan, statusListener, eventListener);
    }

    public DeviceStatusManagerImpl(ConnectionManager connMan) {
        init(connMan, null, null, null, null);
    }

    private void init(ConnectionManager connMan, StructureManager strucMan, SceneManager sceneMan,
            ManagerStatusListener statusListener, EventListener eventListener) {
        this.connMan = connMan;
        this.digitalSTROMClient = connMan.getDigitalSTROMAPI();
        this.config = connMan.getConfig();
        if (strucMan != null) {
            this.strucMan = strucMan;
        } else {
            this.strucMan = new StructureManagerImpl();
        }
        if (sceneMan != null) {
            this.sceneMan = sceneMan;
        } else {
            this.sceneMan = new SceneManagerImpl(connMan, strucMan, statusListener);
        }
        this.statusListener = statusListener;
        this.eventListener = eventListener;
    }

    private class PollingRunnable implements Runnable {
        private boolean devicesLoaded = false;
        private long nextSensorUpdate = 0;

        @Override
        public void run() {
            try {
                if (connMan.checkConnection()) {
                    if (!getManagerState().equals(ManagerStates.RUNNING)) {
                        logger.debug("Thread started");
                        if (devicesLoaded) {
                            stateChanged(ManagerStates.RUNNING);
                        } else {
                            stateChanged(ManagerStates.INITIALIZING);
                        }
                    }
                    HashMap<DSID, Device> tempDeviceMap;
                    if (strucMan.getDeviceMap() != null) {
                        tempDeviceMap = (HashMap<DSID, Device>) strucMan.getDeviceMap();
                    } else {
                        tempDeviceMap = new HashMap<DSID, Device>();
                    }

                    List<Device> currentDeviceList = getDetailedDevices();

                    // update the current total power consumption
                    if (/* totalPowerConsumptionListener != null && */nextSensorUpdate <= System.currentTimeMillis()) {
                        // TODO:
                        // check circuits
                        List<Circuit> circuits = digitalSTROMClient.getApartmentCircuits(connMan.getSessionToken());
                        for (Circuit circuit : circuits) {
                            if (strucMan.getCircuitByDSID(circuit.getDSID()) != null) {
                                if (!circuit.equals(strucMan.getCircuitByDSID(circuit.getDSID()))) {
                                    strucMan.updateCircuitConfig(circuit);
                                }
                            } else {
                                strucMan.addCircuit(circuit);
                                if (deviceDiscovery != null) {
                                    deviceDiscovery.onDeviceAdded(circuit);
                                }
                            }
                        }
                        getMeterData();
                        /*
                         * meters = digitalSTROMClient.getMeterList(connMan.getSessionToken());
                         * totalPowerConsumptionListener.onTotalPowerConsumptionChanged(getTotalPowerConsumption());
                         * totalPowerConsumptionListener.onEnergyMeterValueChanged(getTotalEnergyMeterValue());
                         */
                        nextSensorUpdate = System.currentTimeMillis() + config.getTotalPowerUpdateInterval();
                    }

                    while (!currentDeviceList.isEmpty()) {
                        Device currentDevice = currentDeviceList.remove(0);
                        DSID currentDeviceDSID = currentDevice.getDSID();
                        Device eshDevice = tempDeviceMap.remove(currentDeviceDSID);

                        if (eshDevice != null) {
                            checkDeviceConfig(currentDevice, eshDevice);

                            if (eshDevice.isPresent()) {
                                // check device state updates
                                while (!eshDevice.isDeviceUpToDate()) {
                                    DeviceStateUpdate deviceStateUpdate = eshDevice.getNextDeviceUpdateState();
                                    if (deviceStateUpdate != null) {
                                        switch (deviceStateUpdate.getType()) {
                                            case DeviceStateUpdate.UPDATE_BRIGHTNESS:
                                            case DeviceStateUpdate.UPDATE_SLAT_ANGLE_INCREASE:
                                            case DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE:
                                                filterCommand(deviceStateUpdate, eshDevice);
                                                break;
                                            case DeviceStateUpdate.UPDATE_SCENE_CONFIG:
                                            case DeviceStateUpdate.UPDATE_SCENE_OUTPUT:
                                                updateSceneData(eshDevice, deviceStateUpdate);
                                                break;
                                            case DeviceStateUpdate.UPDATE_OUTPUT_VALUE:
                                                if (deviceStateUpdate.getValueAsInteger() > -1) {
                                                    readOutputValue(eshDevice);
                                                } else {
                                                    removeSensorJob(eshDevice, deviceStateUpdate);
                                                }
                                                break;
                                            default:
                                                sendComandsToDSS(eshDevice, deviceStateUpdate);
                                        }
                                    }
                                }
                            }

                        } else {
                            logger.debug("Found new device!");
                            if (trashDevices.isEmpty()) {
                                currentDevice.setConfig(config);
                                strucMan.addDeviceToStructure(currentDevice);
                                logger.debug("trashDevices are empty, add Device with dSID "
                                        + currentDevice.getDSID().toString() + " to the deviceMap!");
                            } else {
                                logger.debug("Search device in trashDevices.");
                                TrashDevice foundTrashDevice = null;
                                for (TrashDevice trashDevice : trashDevices) {
                                    if (trashDevice != null) {
                                        if (trashDevice.getDevice().equals(currentDevice)) {
                                            foundTrashDevice = trashDevice;
                                            logger.debug(
                                                    "Found device in trashDevices, add TrashDevice with dSID {} to the StructureManager!",
                                                    currentDeviceDSID);
                                        }
                                    }
                                }
                                if (foundTrashDevice != null) {
                                    trashDevices.remove(foundTrashDevice);
                                    strucMan.addDeviceToStructure(foundTrashDevice.getDevice());
                                } else {
                                    strucMan.addDeviceToStructure(currentDevice);
                                    logger.debug(
                                            "Can't find device in trashDevices, add Device with dSID: {} to the StructureManager!",
                                            currentDeviceDSID);
                                }
                            }
                            if (deviceDiscovery != null) {
                                // only inform discover, if the device is with output or a sensor device
                                // TODO: quatsch, weg damit
                                // if (currentDevice.isDeviceWithOutput() || currentDevice.isSensorDevice()) {
                                deviceDiscovery.onDeviceAdded(currentDevice);
                                logger.debug("inform DeviceStatusListener: {} about removed device with dSID {}",
                                        DeviceStatusListener.DEVICE_DISCOVERY, currentDevice.getDSID().getValue());
                                // }
                            } else {
                                logger.debug(
                                        "The device discovery is not registrated, can't inform device discovery about found device.");
                            }
                        }
                    }

                    if (!devicesLoaded && strucMan.getDeviceMap() != null) {
                        if (!strucMan.getDeviceMap().values().isEmpty()) {
                            logger.debug("Devices loaded");
                            devicesLoaded = true;
                            setInizialStateWithLastCallScenes();
                            stateChanged(ManagerStates.RUNNING);
                        } else {
                            logger.info("No devices found");
                        }
                    }

                    // TODO:
                    // if (sceneMan.getManagerState().equals(ManagerStates.STOPPED)
                    // && !getManagerState().equals(ManagerStates.STOPPED)) {
                    if (!sceneMan.scenesGenerated() && devicesLoaded
                            && !sceneMan.getManagerState().equals(ManagerStates.GENERATING_SCENES)) {
                        logger.debug(sceneMan.getManagerState().toString());
                        // sceneMan.start();
                        sceneMan.generateScenes();
                    }

                    for (Device device : tempDeviceMap.values()) {
                        logger.debug("Found removed devices.");

                        trashDevices.add(new TrashDevice(device));
                        DeviceStatusListener listener = device.unregisterDeviceStatusListener();
                        if (listener != null) {
                            listener.onDeviceRemoved(null);
                        }
                        strucMan.deleteDevice(device);
                        logger.debug("Add device with dSID {} to trashDevices", device.getDSID().getValue());

                        if (deviceDiscovery != null) {
                            deviceDiscovery.onDeviceRemoved(device);
                            logger.debug("inform DeviceStatusListener: {} about removed device with dSID {}",
                                    DeviceStatusListener.DEVICE_DISCOVERY, device.getDSID().getValue());
                        } else {
                            logger.debug(
                                    "The device-Discovery is not registrated, can't inform device discovery about removed device.");
                        }
                    }

                    if (!trashDevices.isEmpty()
                            && (lastBinCheck + config.getBinCheckTime() < System.currentTimeMillis())) {
                        for (TrashDevice trashDevice : trashDevices) {
                            if (trashDevice.isTimeToDelete(Calendar.getInstance().get(Calendar.DAY_OF_YEAR))) {
                                logger.debug("Found trashDevice that have to delete!");
                                trashDevices.remove(trashDevice);
                                logger.debug("Delete trashDevice: " + trashDevice.getDevice().getDSID().getValue());
                            }
                        }
                        lastBinCheck = System.currentTimeMillis();
                    }
                }
            } catch (Exception e) {
                logger.error("An exception occurred: ", e);
            }
        }

        private List<Device> getDetailedDevices() {
            List<Device> deviceList = new LinkedList<Device>();
            JsonObject result = connMan.getDigitalSTROMAPI().query2(connMan.getSessionToken(), GET_DETAILD_DEVICES);
            if (result != null && result.isJsonObject()) {
                if (result.getAsJsonObject().get("zone0").isJsonObject()) {
                    result = result.getAsJsonObject().get("zone0").getAsJsonObject();
                    for (Entry<String, JsonElement> entry : result.entrySet()) {
                        if (!(entry.getKey().equals(JSONApiResponseKeysEnum.ZONE_ID.getKey())
                                && entry.getKey().equals(JSONApiResponseKeysEnum.NAME.getKey()))
                                && entry.getValue().isJsonObject()) {
                            deviceList.add(new DeviceImpl(entry.getValue().getAsJsonObject()));
                        }
                    }
                }
            }
            return deviceList;
        }

        private void filterCommand(DeviceStateUpdate deviceStateUpdate, Device device) {
            String stateUpdateType = deviceStateUpdate.getType();
            short newAngle = 0;
            if (stateUpdateType.equals(DeviceStateUpdate.UPDATE_SLAT_ANGLE_INCREASE)
                    || stateUpdateType.equals(DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE)) {
                newAngle = device.getAnglePosition();
            }
            DeviceStateUpdate nextDeviceStateUpdate = device.getNextDeviceUpdateState();
            while (nextDeviceStateUpdate != null && nextDeviceStateUpdate.getType().equals(stateUpdateType)) {
                switch (stateUpdateType) {
                    case DeviceStateUpdate.UPDATE_BRIGHTNESS:
                        deviceStateUpdate = nextDeviceStateUpdate;
                        nextDeviceStateUpdate = device.getNextDeviceUpdateState();
                        break;
                    case DeviceStateUpdate.UPDATE_SLAT_ANGLE_INCREASE:
                        if (deviceStateUpdate.getValueAsInteger() == 1) {
                            newAngle = (short) (newAngle + DeviceConstants.ANGLE_STEP_SLAT);
                        }
                        break;
                    case DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE:
                        if (deviceStateUpdate.getValueAsInteger() == 1) {
                            newAngle = (short) (newAngle - DeviceConstants.ANGLE_STEP_SLAT);
                        }
                        break;
                }
            }
            if (stateUpdateType.equals(DeviceStateUpdate.UPDATE_SLAT_ANGLE_INCREASE)
                    || stateUpdateType.equals(DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE)) {
                if (newAngle > device.getMaxSlatAngle()) {
                    newAngle = (short) device.getMaxSlatAngle();
                }
                if (newAngle < device.getMinSlatAngle()) {
                    newAngle = (short) device.getMinSlatAngle();
                }
                if (!(stateUpdateType.equals(DeviceStateUpdate.UPDATE_SLAT_ANGLE_INCREASE)
                        && checkAngleIsMinMax(device) == 1)
                        || !(stateUpdateType.equals(DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE)
                                && checkAngleIsMinMax(device) == 0)) {
                    deviceStateUpdate = new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_ANGLE, newAngle);
                }
            }
            sendComandsToDSS(device, deviceStateUpdate);
            if (nextDeviceStateUpdate != null) {
                if (deviceStateUpdate.getType() == DeviceStateUpdate.UPDATE_SCENE_CONFIG
                        || deviceStateUpdate.getType() == DeviceStateUpdate.UPDATE_SCENE_OUTPUT) {
                    updateSceneData(device, deviceStateUpdate);
                } else {
                    sendComandsToDSS(device, deviceStateUpdate);
                }
            }
        }

    };

    private void removeSensorJob(Device eshDevice, DeviceStateUpdate deviceStateUpdate) {
        switch (deviceStateUpdate.getType()) {
            case DeviceStateUpdate.UPDATE_SCENE_CONFIG:
                if (sceneJobExecutor != null) {
                    sceneJobExecutor.removeSensorJob(eshDevice,
                            SceneConfigReadingJob.getID(eshDevice, deviceStateUpdate.getSceneId()));
                }
                break;
            case DeviceStateUpdate.UPDATE_SCENE_OUTPUT:
                if (sceneJobExecutor != null) {
                    sceneJobExecutor.removeSensorJob(eshDevice,
                            SceneOutputValueReadingJob.getID(eshDevice, deviceStateUpdate.getSceneId()));
                }
                break;
            case DeviceStateUpdate.UPDATE_OUTPUT_VALUE:
                if (sensorJobExecutor != null) {
                    sensorJobExecutor.removeSensorJob(eshDevice, DeviceOutputValueSensorJob.getID(eshDevice));
                }
                break;
        }
        if (deviceStateUpdate.isSensorUpdateType()) {
            if (sensorJobExecutor != null) {
                logger.debug("remove SensorJob with ID: "
                        + DeviceConsumptionSensorJob.getID(eshDevice, deviceStateUpdate.getTypeAsSensorEnum()));
                sensorJobExecutor.removeSensorJob(eshDevice,
                        DeviceConsumptionSensorJob.getID(eshDevice, deviceStateUpdate.getTypeAsSensorEnum()));
            }
        }
    }

    @Override
    public ManagerTypes getManagerType() {
        return ManagerTypes.DEVICE_STATUS_MANAGER;
    }

    @Override
    public synchronized ManagerStates getManagerState() {
        return state;
    }

    private synchronized void stateChanged(ManagerStates state) {
        if (statusListener != null) {
            this.state = state;
            statusListener.onStatusChanged(ManagerTypes.DEVICE_STATUS_MANAGER, state);
        }
    }

    @Override
    public synchronized void start() {
        logger.debug("start DeviceStatusManager");
        if (pollingScheduler == null || pollingScheduler.isCancelled()) {
            pollingScheduler = scheduler.scheduleAtFixedRate(new PollingRunnable(), 0, config.getPollingFrequency(),
                    TimeUnit.MILLISECONDS);
            // sceneMan.start();
            logger.debug("start pollingScheduler");
        }
        // if (sceneMan.scenesGenerated()) {
        sceneMan.start();
        // }
        if (sceneJobExecutor != null) {
            this.sceneJobExecutor.startExecutor();
        }

        if (sensorJobExecutor != null) {
            this.sensorJobExecutor.startExecutor();
        }
        if (eventListener != null) {
            eventListener.addEventHandler(this);
        } else {
            eventListener = new EventListener(connMan, this);
            eventListener.start();
        }
    }

    @Override
    public synchronized void stop() {
        logger.debug("stop DeviceStatusManager");
        stateChanged(ManagerStates.STOPPED);
        if (sceneMan != null) {
            sceneMan.stop();
        }
        if (pollingScheduler != null && !pollingScheduler.isCancelled()) {
            pollingScheduler.cancel(true);
            pollingScheduler = null;
            logger.debug("stop pollingScheduler");
        }
        if (sceneJobExecutor != null) {
            this.sceneJobExecutor.shutdown();
        }
        if (sensorJobExecutor != null) {
            this.sensorJobExecutor.shutdown();
        }
        if (eventListener != null) {
            eventListener.removeEventHandler(this);
        }
    }

    /**
     * The {@link TrashDevice} saves not present {@link Device}'s, but at this point not deleted from the
     * digitalSTROM-System, temporary to get back the configuration of the {@link Device}'s faster.
     *
     * @author Michael Ochel - Initial contribution
     * @author Matthias Siegele - Initial contribution
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
            return this.timeStamp + config.getTrashDeviceDeleteTime() <= dayOfYear;
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
        // The device is not availability for the digitalSTROM-Server, it has not been deleted and are therefore set to
        // OFFLINE.
        // To delete an alternate algorithm is responsible.
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
        if (!newDevice.getBinaryInputs().equals(internalDevice.getBinaryInputs())) {
            internalDevice.setBinaryInputs(newDevice.getBinaryInputs());
        }
        strucMan.updateDevice(newDevice);
    }

    private long lastSceneCall = 0;
    private long sleepTime = 0;

    @Override
    public synchronized void sendSceneComandsToDSS(InternalScene scene, boolean call_undo) {
        if (scene != null) {
            if (lastSceneCall + 1000 > System.currentTimeMillis()) {
                sleepTime = System.currentTimeMillis() - lastSceneCall;
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    logger.error("An exception occurred", e);
                }
            }
            if (this.connMan.checkConnection()) {
                lastSceneCall = System.currentTimeMillis();
                boolean requestSuccsessfull = false;
                if (scene.getZoneID() == 0) {
                    if (call_undo) {
                        logger.debug(scene.getGroupID() + " " + scene.getSceneID() + " "
                                + ApartmentSceneEnum.getApartmentScene(scene.getSceneID()));
                        requestSuccsessfull = this.digitalSTROMClient.callApartmentScene(connMan.getSessionToken(),
                                scene.getGroupID(), null, ApartmentSceneEnum.getApartmentScene(scene.getSceneID()),
                                false);
                    } else {
                        requestSuccsessfull = this.digitalSTROMClient.undoApartmentScene(connMan.getSessionToken(),
                                scene.getGroupID(), null, ApartmentSceneEnum.getApartmentScene(scene.getSceneID()));
                    }
                } else {
                    if (call_undo) {
                        requestSuccsessfull = this.digitalSTROMClient.callZoneScene(connMan.getSessionToken(),
                                scene.getZoneID(), null, scene.getGroupID(), null,
                                SceneEnum.getScene(scene.getSceneID()), false);
                    } else {
                        requestSuccsessfull = this.digitalSTROMClient.undoZoneScene(connMan.getSessionToken(),
                                scene.getZoneID(), null, scene.getGroupID(), null,
                                SceneEnum.getScene(scene.getSceneID()));
                    }
                }

                logger.debug("Was the scene call succsessful?: " + requestSuccsessfull);
                if (requestSuccsessfull) {
                    this.sceneMan.addEcho(scene.getID());
                    if (call_undo) {
                        scene.activateScene();
                    } else {
                        scene.deactivateScene();
                    }
                }
            }
        }
    }

    @Override
    public synchronized void sendStopComandsToDSS(final Device device) {
        scheduler.execute(new Runnable() {

            @Override
            public void run() {
                if (connMan.checkConnection()) {
                }
                if (digitalSTROMClient.callDeviceScene(connMan.getSessionToken(), device.getDSID(), null,
                        SceneEnum.STOP, true)) {
                    sceneMan.addEcho(device.getDSID().getValue(), SceneEnum.STOP.getSceneNumber());
                    readOutputValue(device);
                }
            }
        });
    }

    private void readOutputValue(Device device) {
        short outputIndex = DeviceConstants.DEVICE_SENSOR_OUTPUT;
        if (device.isShade()) {
            outputIndex = DeviceConstants.DEVICE_SENSOR_SLAT_POSITION_OUTPUT;
        }

        int outputValue = this.digitalSTROMClient.getDeviceOutputValue(connMan.getSessionToken(), device.getDSID(),
                null, outputIndex);
        if (outputValue != -1) {
            if (!device.isShade()) {
                device.updateInternalDeviceState(
                        new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_BRIGHTNESS, outputValue));
            } else {
                device.updateInternalDeviceState(
                        new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION, outputValue));
                if (device.isBlind()) {
                    outputValue = this.digitalSTROMClient.getDeviceOutputValue(connMan.getSessionToken(),
                            device.getDSID(), null, DeviceConstants.DEVICE_SENSOR_SLAT_ANGLE_OUTPUT);
                    device.updateInternalDeviceState(
                            new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_ANGLE, outputValue));
                }
            }
        }
    }

    public synchronized void updateDevice(Device eshDevice) {
        logger.debug("Check device updates");
        // check device state updates
        while (!eshDevice.isDeviceUpToDate()) {
            DeviceStateUpdate deviceStateUpdate = eshDevice.getNextDeviceUpdateState();
            if (deviceStateUpdate != null) {
                if (deviceStateUpdate.getType() != DeviceStateUpdate.UPDATE_BRIGHTNESS) {
                    if (deviceStateUpdate.getType() == DeviceStateUpdate.UPDATE_SCENE_CONFIG
                            || deviceStateUpdate.getType() == DeviceStateUpdate.UPDATE_SCENE_OUTPUT) {
                        updateSceneData(eshDevice, deviceStateUpdate);
                    } else {
                        sendComandsToDSS(eshDevice, deviceStateUpdate);
                    }
                } else {
                    DeviceStateUpdate nextDeviceStateUpdate = eshDevice.getNextDeviceUpdateState();
                    while (nextDeviceStateUpdate != null
                            && nextDeviceStateUpdate.getType() == DeviceStateUpdate.UPDATE_BRIGHTNESS) {
                        deviceStateUpdate = nextDeviceStateUpdate;
                        nextDeviceStateUpdate = eshDevice.getNextDeviceUpdateState();
                    }
                    sendComandsToDSS(eshDevice, deviceStateUpdate);
                    if (nextDeviceStateUpdate != null) {
                        if (deviceStateUpdate.getType() == DeviceStateUpdate.UPDATE_SCENE_CONFIG
                                || deviceStateUpdate.getType() == DeviceStateUpdate.UPDATE_SCENE_OUTPUT) {
                            updateSceneData(eshDevice, deviceStateUpdate);
                        } else {
                            sendComandsToDSS(eshDevice, deviceStateUpdate);
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks the output value of a {@link Device} and return 0, if the output value or slat position is min and 1, if
     * the output value or slat position is max, otherwise it returns -1.
     *
     * @param device
     * @return 0 = output value is min, 1 device value is min, otherwise -1
     */
    private short checkIsAllreadyMinMax(Device device) {
        if (device.isShade()) {
            if (device.getSlatPosition() == device.getMaxSlatPosition()) {
                if (device.isBlind()) {
                    if (device.getAnglePosition() == device.getMaxSlatAngle()) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
                return 1;
            }
            if (device.getSlatPosition() == device.getMinSlatPosition()) {
                if (device.isBlind()) {
                    if (device.getAnglePosition() == device.getMinSlatAngle()) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
                return 0;
            }
        } else {
            if (device.getOutputValue() == device.getMaxOutputValue()) {
                return 1;
            }
            if (device.getOutputValue() == device.getMinOutputValue() || device.getOutputValue() <= 0) {
                return 0;
            }
        }
        return -1;
    }

    /**
     * Checks the angle value of a {@link Device} and return 0, if the angle value is min and 1, if the angle value is
     * max, otherwise it returns -1.
     *
     * @param device
     * @return 0 = angle value is min, 1 angle value is min, otherwise -1
     */
    private short checkAngleIsMinMax(Device device) {
        if (device.getAnglePosition() == device.getMaxSlatAngle()) {
            return 1;
        }
        if (device.getAnglePosition() == device.getMinSlatAngle()) {
            return 1;
        }
        return -1;
    }

    @Override
    public synchronized void sendComandsToDSS(Device device, DeviceStateUpdate deviceStateUpdate) {
        if (connMan.checkConnection()) {
            boolean requestSuccsessful = false;
            boolean commandHaveNoEffect = false;
            if (deviceStateUpdate != null) {
                if (deviceStateUpdate.isSensorUpdateType()) {
                    SensorEnum sensorType = deviceStateUpdate.getTypeAsSensorEnum();
                    if (deviceStateUpdate.getValueAsInteger() == 0) {
                        logger.debug("Device need " + sensorType + " SensorData update");
                        // TODO:
                        updateSensorData(new DeviceConsumptionSensorJob(device, sensorType),
                                device.getPowerSensorRefreshPriority(sensorType));
                        return;
                    } else if (deviceStateUpdate.getValueAsInteger() < 0) {
                        // TODO:
                        removeSensorJob(device, deviceStateUpdate);
                        return;
                    } else {
                        int consumption = this.digitalSTROMClient.getDeviceSensorValue(connMan.getSessionToken(),
                                device.getDSID(), null, device.getSensorIndex(sensorType));
                        if (consumption >= 0) {
                            device.setDeviceSensorDsValueBySensorJob(sensorType, consumption);
                            // device.updateInternalDeviceState(new DeviceStateUpdateImpl(sensorType, consumption));
                            requestSuccsessful = true;
                        }
                    }
                } else {
                    switch (deviceStateUpdate.getType()) {
                        case DeviceStateUpdate.UPDATE_BRIGHTNESS_DECREASE:
                        case DeviceStateUpdate.UPDATE_SLAT_DECREASE:
                            if (checkIsAllreadyMinMax(device) != 0) {
                                requestSuccsessful = digitalSTROMClient.decreaseValue(connMan.getSessionToken(),
                                        device.getDSID());
                                if (requestSuccsessful) {
                                    sceneMan.addEcho(device.getDSID().getValue(), SceneEnum.DECREMENT.getSceneNumber());
                                }
                            } else {
                                commandHaveNoEffect = true;
                            }
                            break;
                        case DeviceStateUpdate.UPDATE_BRIGHTNESS_INCREASE:
                        case DeviceStateUpdate.UPDATE_SLAT_INCREASE:
                            if (checkIsAllreadyMinMax(device) != 1) {
                                requestSuccsessful = digitalSTROMClient.increaseValue(connMan.getSessionToken(),
                                        device.getDSID());
                                if (requestSuccsessful) {
                                    sceneMan.addEcho(device.getDSID().getValue(), SceneEnum.INCREMENT.getSceneNumber());
                                }
                            } else {
                                commandHaveNoEffect = true;
                            }
                            break;
                        case DeviceStateUpdate.UPDATE_BRIGHTNESS:
                            if (device.getOutputValue() != deviceStateUpdate.getValueAsInteger()) {
                                requestSuccsessful = digitalSTROMClient.setDeviceValue(connMan.getSessionToken(),
                                        device.getDSID(), null, deviceStateUpdate.getValueAsInteger());
                            } else {
                                commandHaveNoEffect = true;
                            }
                            break;
                        case DeviceStateUpdate.UPDATE_OPEN_CLOSE:
                        case DeviceStateUpdate.UPDATE_ON_OFF:
                            if (deviceStateUpdate.getValueAsInteger() > 0) {
                                if (checkIsAllreadyMinMax(device) != 1) {
                                    requestSuccsessful = digitalSTROMClient.turnDeviceOn(connMan.getSessionToken(),
                                            device.getDSID(), null);
                                    if (requestSuccsessful) {
                                        sceneMan.addEcho(device.getDSID().getValue(),
                                                SceneEnum.MAXIMUM.getSceneNumber());
                                    }
                                } else {
                                    commandHaveNoEffect = true;
                                }
                            } else {
                                if (checkIsAllreadyMinMax(device) != 0) {
                                    requestSuccsessful = digitalSTROMClient.turnDeviceOff(connMan.getSessionToken(),
                                            device.getDSID(), null);
                                    if (requestSuccsessful) {
                                        sceneMan.addEcho(device.getDSID().getValue(),
                                                SceneEnum.MINIMUM.getSceneNumber());
                                    }
                                    // if (sensorJobExecutor != null) {
                                    // sensorJobExecutor.removeSensorJobs(device);
                                    // }
                                } else {
                                    commandHaveNoEffect = true;
                                }
                            }
                            break;
                        case DeviceStateUpdate.UPDATE_SLATPOSITION:
                            if (device.getSlatPosition() != deviceStateUpdate.getValueAsInteger()) {
                                requestSuccsessful = digitalSTROMClient.setDeviceOutputValue(connMan.getSessionToken(),
                                        device.getDSID(), null, DeviceConstants.DEVICE_SENSOR_SLAT_POSITION_OUTPUT,
                                        deviceStateUpdate.getValueAsInteger());
                            } else {
                                commandHaveNoEffect = true;
                            }
                            break;
                        case DeviceStateUpdate.UPDATE_SLAT_STOP:
                            this.sendStopComandsToDSS(device);
                            break;
                        case DeviceStateUpdate.UPDATE_SLAT_MOVE:
                            if (deviceStateUpdate.getValueAsInteger() > 0) {
                                requestSuccsessful = digitalSTROMClient.turnDeviceOn(connMan.getSessionToken(),
                                        device.getDSID(), null);
                                if (requestSuccsessful) {
                                    sceneMan.addEcho(device.getDSID().getValue(), SceneEnum.MAXIMUM.getSceneNumber());
                                }
                            } else {
                                requestSuccsessful = digitalSTROMClient.turnDeviceOff(connMan.getSessionToken(),
                                        device.getDSID(), null);
                                if (requestSuccsessful) {
                                    sceneMan.addEcho(device.getDSID().getValue(), SceneEnum.MINIMUM.getSceneNumber());
                                }
                                if (sensorJobExecutor != null) {
                                    sensorJobExecutor.removeSensorJobs(device);
                                }
                            }
                            break;
                        case DeviceStateUpdate.UPDATE_CALL_SCENE:
                            if (SceneEnum.getScene((short) deviceStateUpdate.getValue()) != null) {
                                requestSuccsessful = digitalSTROMClient.callDeviceScene(connMan.getSessionToken(),
                                        device.getDSID(), null,
                                        SceneEnum.getScene((short) deviceStateUpdate.getValue()), true);
                            }
                            break;
                        case DeviceStateUpdate.UPDATE_UNDO_SCENE:
                            if (SceneEnum.getScene((short) deviceStateUpdate.getValue()) != null) {
                                requestSuccsessful = digitalSTROMClient.undoDeviceScene(connMan.getSessionToken(),
                                        device.getDSID(), SceneEnum.getScene((short) deviceStateUpdate.getValue()));
                            }
                            break;
                        case DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE:
                            // By UPDATE_SLAT_ANGLE_DECREASE, UPDATE_SLAT_ANGLE_INCREASE with value unequal 1 which will
                            // handle in the pollingRunnable and UPDATE_OPEN_CLOSE_ANGLE the value will be set without
                            // checking, because it was triggered by setting the slat position.
                            requestSuccsessful = true;
                            break;
                        case DeviceStateUpdate.UPDATE_SLAT_ANGLE_INCREASE:
                            requestSuccsessful = true;
                            break;
                        case DeviceStateUpdate.UPDATE_OPEN_CLOSE_ANGLE:
                            requestSuccsessful = true;
                            break;
                        case DeviceStateUpdate.UPDATE_SLAT_ANGLE:
                            if (device.getAnglePosition() != deviceStateUpdate.getValueAsInteger()) {
                                requestSuccsessful = digitalSTROMClient.setDeviceOutputValue(connMan.getSessionToken(),
                                        device.getDSID(), null, DeviceConstants.DEVICE_SENSOR_SLAT_ANGLE_OUTPUT,
                                        deviceStateUpdate.getValueAsInteger());
                            } else {
                                commandHaveNoEffect = true;
                            }
                            break;
                        case DeviceStateUpdate.REFRESH_OUTPUT:
                            readOutputValue(device);
                            logger.debug("Inizalize output value reading for device with dSID {}.",
                                    device.getDSID().getValue());
                            return;
                        default:
                            return;
                    }
                }
                if (commandHaveNoEffect) {
                    logger.debug("Command {} for device with dSID {} not send to dSS, because it has no effect!",
                            deviceStateUpdate.getType(), device.getDSID().getValue());
                    return;
                }
                if (requestSuccsessful) {
                    logger.debug("Send {} command to dSS and updateInternalDeviceState for device with dSID {}.",
                            deviceStateUpdate.getType(), device.getDSID().getValue());
                    device.updateInternalDeviceState(deviceStateUpdate);
                } else {
                    logger.debug("Can't send {} command for device with dSID {} to dSS!", deviceStateUpdate.getType(),
                            device.getDSID().getValue());
                }
            }
        }
    }

    @Override
    public void updateSensorData(SensorJob sensorJob, String priority) {
        if (sensorJobExecutor == null) {
            sensorJobExecutor = new SensorJobExecutor(connMan);
            this.sensorJobExecutor.startExecutor();
        }
        if (sensorJob != null && priority != null) {
            switch (priority) {
                case Config.REFRESH_PRIORITY_HIGH:
                    sensorJobExecutor.addHighPriorityJob(sensorJob);
                    break;
                case Config.REFRESH_PRIORITY_MEDIUM:
                    sensorJobExecutor.addMediumPriorityJob(sensorJob);
                    break;
                case Config.REFRESH_PRIORITY_LOW:
                    sensorJobExecutor.addLowPriorityJob(sensorJob);
                    break;
                default:
                    try {
                        long prio = Long.parseLong(priority);
                        sensorJobExecutor.addPriorityJob(sensorJob, prio);
                    } catch (NumberFormatException e) {
                        System.err.println("Sensor data update priority do not exist! Please check the input!");
                        return;
                    }
            }
            /*
             * if (priority.contains(Config.REFRESH_PRIORITY_HIGH)) {
             * sensorJobExecutor.addHighPriorityJob(sensorJob);
             * } else if (priority.contains(Config.REFRESH_PRIORITY_MEDIUM)) {
             * sensorJobExecutor.addMediumPriorityJob(sensorJob);
             * } else if (priority.contains(Config.REFRESH_PRIORITY_LOW)) {
             * sensorJobExecutor.addLowPriorityJob(sensorJob);
             * } else {
             * System.err.println("Sensor data update priority do not exist! Please check the input!");
             * return;
             * }
             */
            logger.debug("Add new sensorJob {} with priority: {} to sensorJobExecuter", sensorJob.toString(), priority);
        }
    }

    @Override
    public void updateSceneData(Device device, DeviceStateUpdate deviceStateUpdate) {
        if (sceneJobExecutor == null) {
            sceneJobExecutor = new SceneReadingJobExecutor(connMan);
            this.sceneJobExecutor.startExecutor();
        }

        if (deviceStateUpdate != null) {
            if (deviceStateUpdate.getScenePriority() > -1) {
                if (deviceStateUpdate.getType().equals(DeviceStateUpdate.UPDATE_SCENE_OUTPUT)) {
                    sceneJobExecutor.addPriorityJob(
                            new SceneOutputValueReadingJob(device, deviceStateUpdate.getSceneId()),
                            deviceStateUpdate.getScenePriority().longValue());
                } else {
                    sceneJobExecutor.addPriorityJob(new SceneConfigReadingJob(device, deviceStateUpdate.getSceneId()),
                            deviceStateUpdate.getScenePriority().longValue());
                }
                if (deviceStateUpdate.getScenePriority() == 0) {
                    updateSensorData(new DeviceOutputValueSensorJob(device), "0");
                }
                logger.debug("Add new sceneReadingJob with priority: {} to SceneReadingJobExecuter",
                        deviceStateUpdate.getScenePriority());
            } else {
                removeSensorJob(device, deviceStateUpdate);
            }
        }
    }

    @Override
    public void registerDeviceListener(DeviceStatusListener deviceListener) {
        if (deviceListener != null) {
            String id = deviceListener.getDeviceStatusListenerID();
            if (id.equals(DeviceStatusListener.DEVICE_DISCOVERY)) {
                this.deviceDiscovery = deviceListener;
                logger.debug("register Device-Discovery ");
                for (Device device : strucMan.getDeviceMap().values()) {
                    deviceDiscovery.onDeviceAdded(device);
                }
            } else {
                Device intDevice = strucMan.getDeviceByDSID(deviceListener.getDeviceStatusListenerID());
                if (intDevice != null) {
                    logger.debug("register DeviceListener with id: {} to Device ", id);
                    intDevice.registerDeviceStatusListener(deviceListener);
                } else {
                    Circuit intCircuit = strucMan
                            .getCircuitByDSID(new DSID(deviceListener.getDeviceStatusListenerID()));
                    if (intCircuit != null) {
                        logger.debug("register DeviceListener with id: {} to Circuit ", id);
                        intCircuit.registerDeviceStatusListener(deviceListener);
                    } else {
                        deviceListener.onDeviceRemoved(null);
                    }
                }
            }
        }
    }

    @Override
    public void unregisterDeviceListener(DeviceStatusListener deviceListener) {
        if (deviceListener != null) {
            String id = deviceListener.getDeviceStatusListenerID();
            logger.debug("unregister DeviceListener with id: " + id);
            if (id.equals(DeviceStatusListener.DEVICE_DISCOVERY)) {
                this.deviceDiscovery = null;
            } else {
                Device intDevice = strucMan.getDeviceByDSID(deviceListener.getDeviceStatusListenerID());
                if (intDevice != null) {
                    intDevice.unregisterDeviceStatusListener();
                } else {
                    Circuit intCircuit = strucMan
                            .getCircuitByDSID(new DSID(deviceListener.getDeviceStatusListenerID()));
                    if (intCircuit != null) {
                        intCircuit.unregisterDeviceStatusListener();
                        if (deviceDiscovery != null) {
                            deviceListener.onDeviceAdded(intCircuit);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void removeDevice(String dSID) {
        Device intDevice = strucMan.getDeviceByDSID(dSID);
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
    public void registerStatusListener(ManagerStatusListener statusListener) {
        this.statusListener = statusListener;
        this.sceneMan.registerStatusListener(statusListener);
    }

    @Override
    public void unregisterStatusListener() {
        this.statusListener = null;
        this.sceneMan.unregisterStatusListener();
    }

    @Override
    public void registerConnectionListener(ConnectionListener connectionListener) {
        this.connMan.registerConnectionListener(connectionListener);
    }

    @Override
    public void unregisterConnectionListener() {
        this.connMan.unregisterConnectionListener();
    }

    public final static String ALL_METERS = ".meters(all)";

    @Override
    public int getTotalPowerConsumption() {
        List<CachedMeteringValue> cachedConsumptionMeteringValues = digitalSTROMClient
                .getLatest(connMan.getSessionToken(), MeteringTypeEnum.consumption, ALL_METERS, null);
        if (cachedConsumptionMeteringValues != null) {
            tempConsumption = 0;
            for (CachedMeteringValue value : cachedConsumptionMeteringValues) {
                tempConsumption += value.getValue();
                if (strucMan.getCircuitByDSID(value.getDsid()) != null) {
                    strucMan.getCircuitByDSID(value.getDsid()).addMeteringValue(value);
                }
            }
        }
        return tempConsumption;
    }

    private void getMeterData() {
        int val = getTotalPowerConsumption();
        if (totalPowerConsumptionListener != null) {
            totalPowerConsumptionListener.onTotalPowerConsumptionChanged(val);
        }
        val = getTotalEnergyMeterValue();
        if (totalPowerConsumptionListener != null) {
            totalPowerConsumptionListener.onEnergyMeterValueChanged(val);
        }
        val = getTotalEnergyMeterWsValue();
        if (totalPowerConsumptionListener != null) {
            totalPowerConsumptionListener.onEnergyMeterWsValueChanged(val);
        }
    }

    @Override
    public int getTotalEnergyMeterValue() {
        List<CachedMeteringValue> cachedEnergyMeteringValues = digitalSTROMClient.getLatest(connMan.getSessionToken(),
                MeteringTypeEnum.energy, ALL_METERS, MeteringUnitsEnum.Wh);
        if (cachedEnergyMeteringValues != null) {
            tempEnergyMeter = 0;
            for (CachedMeteringValue value : cachedEnergyMeteringValues) {
                tempEnergyMeter += value.getValue();
                if (strucMan.getCircuitByDSID(value.getDsid()) != null) {
                    strucMan.getCircuitByDSID(value.getDsid()).addMeteringValue(value);
                }
            }
        }
        return tempEnergyMeter;
    }

    @Override
    public int getTotalEnergyMeterWsValue() {
        List<CachedMeteringValue> cachedEnergyMeteringValues = digitalSTROMClient.getLatest(connMan.getSessionToken(),
                MeteringTypeEnum.energy, ALL_METERS, MeteringUnitsEnum.Ws);
        if (cachedEnergyMeteringValues != null) {
            tempEnergyMeterWs = 0;
            for (CachedMeteringValue value : cachedEnergyMeteringValues) {
                tempEnergyMeterWs += value.getValue();
                if (strucMan.getCircuitByDSID(value.getDsid()) != null) {
                    strucMan.getCircuitByDSID(value.getDsid()).addMeteringValue(value);
                }
            }
        }
        return tempEnergyMeterWs;
    }

    private void setInizialStateWithLastCallScenes() {
        if (sceneMan != null && connMan.checkConnection()) {
            JsonObject response = connMan.getDigitalSTROMAPI().query2(connMan.getSessionToken(), LAST_CALL_SCENE_QUERY);
            if (response.isJsonObject()) {
                for (Entry<String, JsonElement> entry : response.entrySet()) {
                    if (entry.getValue().isJsonObject()) {
                        JsonObject zone = entry.getValue().getAsJsonObject();
                        int zoneID = -1;
                        short groupID = -1;
                        short sceneID = -1;
                        if (zone.get(JSONApiResponseKeysEnum.ZONE_ID.getKey()) != null) {
                            zoneID = zone.get(JSONApiResponseKeysEnum.ZONE_ID.getKey()).getAsInt();
                        }
                        for (Entry<String, JsonElement> groupEntry : zone.entrySet()) {
                            if (groupEntry.getKey().startsWith("group") && groupEntry.getValue().isJsonObject()) {
                                JsonObject group = groupEntry.getValue().getAsJsonObject();
                                if (group.get(JSONApiResponseKeysEnum.DEVICES.getKey()) != null) {
                                    if (group.get(JSONApiResponseKeysEnum.GROUP.getKey()) != null) {
                                        groupID = group.get(JSONApiResponseKeysEnum.GROUP.getKey()).getAsShort();
                                    }
                                    if (group.get(JSONApiResponseKeysEnum.LAST_CALL_SCENE.getKey()) != null) {
                                        sceneID = group.get(JSONApiResponseKeysEnum.LAST_CALL_SCENE.getKey())
                                                .getAsShort();
                                    }
                                    if (zoneID > -1 && groupID > -1 && sceneID > -1) {
                                        logger.debug(
                                                "inizial state, call scene " + zoneID + "-" + groupID + "-" + sceneID);
                                        sceneMan.callInternalSceneWithoutDiscovery(zoneID, groupID, sceneID);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleEvent(EventItem eventItem) {
        if (EventNames.DEVICE_SENSOR_VALUE.equals(eventItem.getName())
                || EventNames.DEVICE_BINARY_INPUT_EVENT.equals(eventItem.getName())) {
            logger.debug("Detect " + eventItem.getName() + " eventItem=" + eventItem.toString());
            Device dev = getDeviceOfEvent(eventItem);
            if (dev != null) {
                if (EventNames.DEVICE_SENSOR_VALUE.equals(eventItem.getName())) {
                    dev.setDeviceSensorByEvent(eventItem);
                } else {
                    DeviceBinarayInputEnum binaryInputType = DeviceBinarayInputEnum.getdeviceBinarayInput(
                            Short.parseShort(eventItem.getProperties().get(EventResponseEnum.INPUT_TYPE)));
                    Short newState = Short.parseShort(eventItem.getProperties().get(EventResponseEnum.INPUT_STATE));
                    if (binaryInputType != null) {
                        dev.setBinaryInputState(binaryInputType, newState);
                    }
                }
            }
        }
    }

    private Device getDeviceOfEvent(EventItem eventItem) {
        if (eventItem.getSource().get(EventResponseEnum.DSID) != null) {
            String dSID = eventItem.getSource().get(EventResponseEnum.DSID);
            Device dev = strucMan.getDeviceByDSID(dSID);
            if (dev == null) {
                dev = strucMan.getDeviceByDSUID(dSID);
            }
            return dev;
        }
        return null;
    }

    @Override
    public List<String> getSupportetEvents() {
        return SUPPORTED_EVENTS;
    }

    @Override
    public boolean supportsEvent(String eventName) {
        return SUPPORTED_EVENTS.contains(eventName);
    }

    @Override
    public String getUID() {
        return getClass().getName();
    }

    @Override
    public void setEventListener(EventListener eventListener) {
        if (this.eventListener != null) {
            this.eventListener.removeEventHandler(this);
        }
        this.eventListener = eventListener;
    }

    @Override
    public void unsetEventListener(EventListener eventListener) {
        if (this.eventListener != null) {
            this.eventListener.removeEventHandler(this);
        }
        this.eventListener = null;
    }
}
