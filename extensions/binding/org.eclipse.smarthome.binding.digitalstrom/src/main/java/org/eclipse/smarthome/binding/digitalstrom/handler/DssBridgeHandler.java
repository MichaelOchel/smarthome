/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.handler;

import static org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
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
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.impl.DigitalSTROMConnectionManagerImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.impl.DigitalSTROMDeviceStatusManagerImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.impl.DigitalSTROMSceneManagerImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.impl.DigitalSTROMStructureManagerImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.InternalScene;
import org.eclipse.smarthome.binding.digitalstrom.internal.providers.DigitalSTROMThingTypeProvider;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DssBridgeHandler} is the handler for a digitalSTROM-Server and connects it to
 * the framework.<br>
 * All {@link DsDeviceHandler}s and {@link DsSceneHandler} use the {@link DssBridgeHandler} to execute the actual
 * commands.
 * The {@link DssBridgeHandler} also informs all other digitalSTROM handler about status changes from the outside.
 *
 * @author Michael Ochel - Initial contribution
 * @author Mathias Siegele - Initial contribution
 */
public class DssBridgeHandler extends BaseBridgeHandler
        implements DigitalSTROMConnectionListener, TotalPowerConsumptionListener, DigitalSTROMManagerStatusListener {

    private Logger logger = LoggerFactory.getLogger(DssBridgeHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_DSS_BRIDGE);

    /* DS-Manager */
    private DigitalSTROMConnectionManager connMan;
    private DigitalSTROMStructureManager structMan;
    private DigitalSTROMSceneManager sceneMan;
    private DigitalSTROMDeviceStatusManager devStatMan;

    private List<SceneStatusListener> sceneListener;
    private List<DeviceStatusListener> devListener;
    private DigitalSTROMThingTypeProvider thingTypeProvider = null;

    public DssBridgeHandler(Bridge bridge) {
        super(bridge);
        // this.connMan = connectionManager;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing DigitalSTROM Bridge handler.");
        Configuration configuration = this.getConfig();

        if (configuration.get(HOST) != null && !configuration.get(HOST).toString().isEmpty()) {
            // get Configurations
            if (configuration.get(DigitalSTROMBindingConstants.SENSOR_DATA_UPDATE_INTERVALL) != null
                    && !configuration.get(DigitalSTROMBindingConstants.SENSOR_DATA_UPDATE_INTERVALL).toString().trim()
                            .replace(" ", "").isEmpty()) {

                DigitalSTROMConfig.SENSORDATA_REFRESH_INTERVAL = Integer.parseInt(
                        configuration.get(DigitalSTROMBindingConstants.SENSOR_DATA_UPDATE_INTERVALL).toString()
                                + "000");
            }
            if (configuration.get(DigitalSTROMBindingConstants.DEFAULT_TRASH_DEVICE_DELEATE_TIME_KEY) != null
                    && !configuration.get(DigitalSTROMBindingConstants.DEFAULT_TRASH_DEVICE_DELEATE_TIME_KEY).toString()
                            .trim().replace(" ", "").isEmpty()) {

                DigitalSTROMConfig.TRASH_DEVICE_DELEATE_TIME = Integer.parseInt(configuration
                        .get(DigitalSTROMBindingConstants.DEFAULT_TRASH_DEVICE_DELEATE_TIME_KEY).toString());
            }
            if (configuration.get(DigitalSTROMBindingConstants.TRUST_CERT_PATH_KEY) != null
                    && !configuration.get(DigitalSTROMBindingConstants.TRUST_CERT_PATH_KEY).toString().trim()
                            .replace(" ", "").isEmpty()) {

                DigitalSTROMConfig.TRUST_CERT_PATH = configuration.get(DigitalSTROMBindingConstants.TRUST_CERT_PATH_KEY)
                        .toString();
            }

            logger.debug("Initializing DigitalSTROM Manager.");
            String[] loginConfig = getLoginConfig(configuration);
            if (connMan == null) {

                this.connMan = new DigitalSTROMConnectionManagerImpl(loginConfig[0], loginConfig[1], loginConfig[2],
                        loginConfig[3], true, this);
            } else {
                connMan.updateConfig(loginConfig[0], loginConfig[1], loginConfig[2], loginConfig[3]);
                connMan.registerConnectionListener(this);
            }

            if (this.structMan == null) {
                this.structMan = new DigitalSTROMStructureManagerImpl();
            }
            if (this.sceneMan == null) {
                this.sceneMan = new DigitalSTROMSceneManagerImpl(this.connMan, this.structMan);
            }
            if (this.devStatMan == null) {
                this.devStatMan = new DigitalSTROMDeviceStatusManagerImpl(this.connMan, this.structMan, this.sceneMan);
            }
            structMan.generateZoneGroupNames(connMan);

            devStatMan.registerStatusListener(this);
            devStatMan.registerTotalPowerConsumptionListener(this);

            if (this.thingTypeProvider != null) {
                this.thingTypeProvider.registerConnectionManagerHandler(connMan);
            }

            // TODO: vorallem wegen der Discovery
            if (this.devListener != null) {
                for (DeviceStatusListener listener : this.devListener) {
                    this.registerDeviceStatusListener(listener);
                }
                this.devListener = null;
            }

            if (this.sceneListener != null) {
                for (SceneStatusListener listener : this.sceneListener) {
                    this.registerSceneStatusListener(listener);
                }
                this.sceneListener = null;
            }

            if (connMan.checkConnection()) {
                // if (!devStatMan.getManagerState().equals(ManagerStates.running)) {
                this.devStatMan.start();
                // }
                this.onStatusChanged(devStatMan.getManagerType(), devStatMan.getManagerState());

                // setStatus(ThingStatus.ONLINE);
            } else {
                // TODO: offline
            }
            if (connMan.getApplicationToken() != null) {
                configuration.remove(USER_NAME);
                configuration.remove(PASSWORD);
                logger.debug(connMan.getApplicationToken());
                configuration.put(APPLICATION_TOKEN, connMan.getApplicationToken());
                this.updateConfiguration(configuration);
            }

        } else {
            logger.warn("Cannot connect to DigitalSTROMSever. Host address is not set.");
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The connection to the digitalSTROM-Server can't established, because the host is missing. Please set the host.");
        }

    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");

        devStatMan.unregisterTotalPowerConsumptionListener();
        devStatMan.unregisterStatusListener();
        connMan.unregisterConnectionListener();

        this.devStatMan.stop();

        if (this.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // nothing to do
    }

    @Override
    public void handleRemoval() {
        if (connMan == null) {
            Configuration configuration = this.getConfig();
            String[] loginConfig = getLoginConfig(configuration);
            this.connMan = new DigitalSTROMConnectionManagerImpl(loginConfig[0], loginConfig[1], loginConfig[2],
                    loginConfig[3], this);
        }
        // logger.debug(connMan.getApplicationToken());

        // this.devStatMan.stop();
        if (connMan.removeApplicationToken()) {
            logger.debug("Application-Token deleated");
            updateStatus(ThingStatus.REMOVED);

        }
        // this.connMan = null;
    }

    private String[] getLoginConfig(Configuration configuration) {
        String[] loginConfig = new String[4];
        if (configuration.get(HOST) != null) {
            loginConfig[0] = configuration.get(HOST).toString();
        }
        if (configuration.get(USER_NAME) != null) {
            loginConfig[1] = configuration.get(USER_NAME).toString();
        }
        if (configuration.get(PASSWORD) != null) {
            loginConfig[2] = configuration.get(PASSWORD).toString();
        }
        if (configuration.get(APPLICATION_TOKEN) != null) {
            loginConfig[3] = configuration.get(APPLICATION_TOKEN).toString();
        }

        return loginConfig;
    }

    /* methods to store DeviceStatusListener */

    /**
     * Registers a new {@link DeviceStatusListener} on the {@link DsBridgeHandler}.
     *
     * @param deviceStatusListener
     */
    public synchronized void registerDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        if (this.devStatMan != null) {
            if (deviceStatusListener == null) {
                throw new NullPointerException("It's not allowed to pass a null DeviceStatusListener.");
            }

            if (deviceStatusListener.getID() != null) {
                devStatMan.registerDeviceListener(deviceStatusListener);
            } else {
                throw new NullPointerException("It's not allowed to pass a null ID.");
            }
        } else {
            devListener = new LinkedList<DeviceStatusListener>();
            devListener.add(deviceStatusListener);
        }

    }

    public synchronized void registerThingTypeProvider(DigitalSTROMThingTypeProvider thingTypeProvider) {
        this.thingTypeProvider = thingTypeProvider;
    }

    /**
     * Unregisters a new {@link DeviceStatusListener} on the {@link DsBridgeHandler}.
     *
     * @param devicetatusListener
     */
    public void unregisterDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        if (this.devStatMan != null) {
            if (deviceStatusListener.getID() != null) {
                this.devStatMan.unregisterDeviceListener(deviceStatusListener);
            } else {
                throw new NullPointerException("It's not allowed to pass a null ID.");
            }
        }
    }

    /**
     * Registers a new {@link DeviceStatusListener} on the {@link DsBridgeHandler}.
     *
     * @param deviceStatusListener
     */
    public synchronized void registerSceneStatusListener(SceneStatusListener sceneStatusListener) {
        if (this.sceneMan != null && !generatingScenes) {
            if (sceneStatusListener == null) {
                throw new NullPointerException("It's not allowed to pass a null DeviceStatusListener.");
            }

            if (sceneStatusListener.getID() != null) {
                this.sceneMan.registerSceneListener(sceneStatusListener);
            } else {
                throw new NullPointerException("It's not allowed to pass a null ID.");
            }
        } else {
            if (sceneListener == null) {
                sceneListener = new LinkedList<SceneStatusListener>();
            }
            sceneListener.add(sceneStatusListener);
        }

    }

    List<SceneStatusListener> unregisterSceneStatusListeners = null;

    /**
     * Unregisters a new {@link DeviceStatusListener} on the {@link DsBridgeHandler}.
     *
     * @param devicetatusListener
     */
    public void unregisterSceneStatusListener(SceneStatusListener sceneStatusListener) {
        if (this.sceneMan != null && !generatingScenes) {
            if (sceneStatusListener.getID() != null) {
                this.sceneMan.unregisterSceneListener(sceneStatusListener);
            } else {
                throw new NullPointerException("It's not allowed to pass a null ID.");
            }
        } else {
            if (unregisterSceneStatusListeners == null) {
                unregisterSceneStatusListeners = new LinkedList<SceneStatusListener>();
            }
            unregisterSceneStatusListeners.add(sceneStatusListener);
        }
    }

    public void childThingRemoved(String id) {
        if (id.split("-").length == 3) {
            InternalScene scene = sceneMan.getInternalScene(id);
            if (scene != null) {
                sceneMan.removeInternalScene(id);
                sceneMan.addInternalScene(scene);
            }
        } else {
            devStatMan.removeDevice(id);
        }
    }

    public void stopOutputValue(Device device) {
        this.devStatMan.sendStopComandsToDSS(device);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case CHANNEL_TOTAL_ACTIVE_POWER:
                updateChannelState(CHANNEL_TOTAL_ACTIVE_POWER, totalPowerConsumption);
                break;
            case CHANNEL_TOTAL_ELECTRIC_METER:
                updateChannelState(CHANNEL_TOTAL_ELECTRIC_METER, totalEnergyMeterValue * 0.01);
        }
    }

    private int totalPowerConsumption = 0;
    private int totalEnergyMeterValue = 0;

    @Override
    public void onTotalPowerConsumptionChanged(int newPowerConsumption) {
        totalPowerConsumption = newPowerConsumption;
        updateChannelState(CHANNEL_TOTAL_ACTIVE_POWER, totalPowerConsumption);
    }

    @Override
    public void onEnergyMeterValueChanged(int newEnergyMeterValue) {
        logger.debug("energy meter updated to " + newEnergyMeterValue);
        totalEnergyMeterValue = newEnergyMeterValue;
        updateChannelState(CHANNEL_TOTAL_ELECTRIC_METER, totalEnergyMeterValue * 0.01);
    }

    private void updateChannelState(String channelID, double value) {
        if (getThing().getChannel(channelID) != null) {
            updateState(new ChannelUID(getThing().getUID(), channelID), new DecimalType(value));
        }
    }

    @Override
    public void onConnectionStateChange(String newConnectionState) {
        switch (newConnectionState) {
            case CONNECTION_LOST:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "The connection to the digitalSTROM-Server can't established");
                devStatMan.stop();
                break;
            case CONNECTION_RESUMED:
                setStatus(ThingStatus.ONLINE);
                devStatMan.start();
                sceneMan.start();
                break;
            case APPLICATION_TOKEN_GENERATED:

                Configuration config = this.getConfig();
                if (config != null) {
                    config.remove(USER_NAME);
                    config.remove(PASSWORD);
                    logger.debug(connMan.getApplicationToken());
                    config.put(APPLICATION_TOKEN, connMan.getApplicationToken());
                    this.updateConfiguration(config);
                }

            default:
                // TODO: Fehlermeldung
        }
    }

    private void setStatus(ThingStatus status) {
        updateStatus(ThingStatus.ONLINE);
        // if (getBridge() != null) {
        for (Thing thing : getThing().getThings()) {
            if (!thing.getStatus().equals(ThingStatus.ONLINE)) {
                ThingHandler handler = thing.getHandler();
                if (handler != null) {
                    handler.initialize();
                }
            }
        }
        // }
    }

    @Override
    public void onConnectionStateChange(String newConnectionState, String reason) {
        if (newConnectionState.equals(NOT_AUTHENTICATED) || newConnectionState.equals(CONNECTION_LOST)) {
            switch (reason) {
                case WRONG_APP_TOKEN:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "User defined Applicationtoken is wrong.");
                    break;
                case WRONG_USER_OR_PASSWORD:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "The set username or password is wrong.");
                    break;
                case NO_USER_PASSWORD:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "no username or password is set to genarate Appicationtoken.");
                    break;
                case CONNECTON_TIMEOUT:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Connection lost because connection timeout to Server.");
                    break;
                case HOST_NOT_FOUND:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Server not found! Please check this points:\n" + " - DigitalSTROM-Server turned on?\n"
                                    + " - hostadress correct?\n" + " - ethernet cable connection established?");
                    break;
                case INVALIDE_URL:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalide URL is set.");
                    break;
                default:
                    // TODO: Fehlermeldung
            }
        }

    }

    public List<Device> getDevices() {
        return this.structMan.getDeviceMap() != null ? new LinkedList<Device>(this.structMan.getDeviceMap().values())
                : null;
    }

    public DigitalSTROMStructureManager getStructureManager() {
        return this.structMan;
    }

    public void sendSceneComandToDSS(InternalScene scene, boolean call_undo) {
        if (devStatMan != null) {
            devStatMan.sendSceneComandsToDSS(scene, call_undo);
        }
    }

    public List<InternalScene> getScenes() {
        return sceneMan != null ? sceneMan.getScenes() : new LinkedList<InternalScene>();
    }

    public DigitalSTROMConnectionManager getConnectionManager() {
        return this.connMan;
    }

    private boolean generatingScenes = false;

    public boolean isGeneratingScenes() {
        return generatingScenes;
    }

    @Override
    public void onStatusChanged(ManagerTypes managerType, ManagerStates state) {
        logger.debug("!!!!!!" + managerType.toString() + " " + state.toString() + "!!!!!!!");
        if (managerType.equals(ManagerTypes.deviceStatusManager)) {
            switch (state) {
                case initialasing:

                    this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "Building digitalSTOM model.");
                    break;
                case running:
                    if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                        setStatus(ThingStatus.ONLINE);
                    }
                    break;
                case stopped:
                    this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Status-Manager is stopped.");
                    break;
                default:
                    break;
            }
        }
        if (managerType.equals(ManagerTypes.sceneManager)) {
            switch (state) {
                case generatingScenes:
                    generatingScenes = true;
                    break;
                case scenesGenerated:
                    if (unregisterSceneStatusListeners != null) {
                        for (SceneStatusListener sceneListener : this.unregisterSceneStatusListeners) {
                            sceneMan.registerSceneListener(sceneListener);
                        }
                    }
                    if (sceneListener != null) {
                        for (SceneStatusListener sceneListener : this.sceneListener) {
                            sceneMan.registerSceneListener(sceneListener);
                        }
                    }

                    generatingScenes = false;
                    break;
                default:
                    break;
            }
        }

    }

}
