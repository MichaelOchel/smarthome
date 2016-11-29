package org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlStatus;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.EventHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.EventListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.constants.EventNames;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.constants.EventResponseEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.types.EventItem;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.SystemStateChangeListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.TemperatureControlStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.TemperatureSensorTransreciver;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.DsAPI;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.FuncNameAndColorGroupEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.SensorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class TemperatureControlManager implements EventHandler, TemperatureSensorTransreciver {

    public List<String> SUPPORTED_EVENTS = Lists.newArrayList(EventNames.HEATING_CONTROL_OPERATION_MODE);

    private Logger logger = LoggerFactory.getLogger(TemperatureControlManager.class);

    private ConnectionManager connectionMananager;
    private DsAPI dSapi;
    private EventListener eventListener = null;
    private boolean isConfigured = false;

    private HashMap<Integer, TemperatureControlStatusListener> zoneTemperationControlListenerMap = null;
    private HashMap<Integer, TemperatureControlStatus> temperationControlStatus = null;
    private TemperatureControlStatusListener discovery = null;
    private SystemStateChangeListener systemStateChangeListener = null;

    public static final String STATE_NAME_HEATING_WATER_SYSTEM = "heating_water_system";
    public static final String STATE_HEATING_WATER_SYSTEM_OFF = "off"; // val=0
    public static final String STATE_HEATING_WATER_SYSTEM_HOT_WATER = "hot water"; // val=1
    public static final String STATE_HEATING_WATER_SYSTEM_COLD_WATER = "cold water"; // val=2

    public static final String GET_HEATING_WATER_SYSTEM_STATE_PATH = "/usr/states/heating_water_system/state";
    public static final String GET_HEATING_HEATING_CONTROLLER_CHILDREN_PATH = "/scripts/heating-controller/";

    public static final String SET_OPERATION_MODE = "setOperationMode";
    public static final String EVALUATE_REAL_ACTIVE_MODE = "evaluateRealActiveMode";

    private String currentHeatingWaterSystemStage = null;

    private List<String> echoBox = Collections.synchronizedList(new LinkedList<String>());

    public TemperatureControlManager(ConnectionManager connectionMananager, EventListener eventListener,
            TemperatureControlStatusListener discovery) {
        this(connectionMananager, eventListener, discovery, null);
    }

    public TemperatureControlManager(ConnectionManager connectionMananager, EventListener eventListener,
            TemperatureControlStatusListener discovery, SystemStateChangeListener systemStateChangeListener) {
        this.connectionMananager = connectionMananager;
        this.dSapi = connectionMananager.getDigitalSTROMAPI();
        this.systemStateChangeListener = systemStateChangeListener;
        this.discovery = discovery;
        this.eventListener = eventListener;
        checkZones();
        if (eventListener != null) {
            if (isConfigured) {
                SUPPORTED_EVENTS.add(EventNames.ZONE_SENSOR_VALUE);
                if (systemStateChangeListener != null) {
                    SUPPORTED_EVENTS.add(EventNames.STATE_CHANGED);
                }
            }
            eventListener.addEventHandler(this);
        }
    }

    public void checkZones() {
        if (connectionMananager.checkConnection()) {
            // TODO: dS-API getApartmentTemperatureControlStatus zu Liste ändern?
            HashMap<Integer, TemperatureControlStatus> temperationControlStatus = dSapi
                    .getApartmentTemperatureControlStatus(connectionMananager.getSessionToken());
            if (!temperationControlStatus.isEmpty()) {
                for (TemperatureControlStatus tempConStat : temperationControlStatus.values()) {
                    /*
                     * if (tempConStat.getIsConfigured()) {
                     * isConfigured = true;
                     * // TODO: heatingWaterSystemStateAbfragen
                     * if (discovery != null) {
                     * discovery.configChanged(tempConStat);
                     * } else {
                     * break;
                     * }
                     * }
                     */
                    addTemperatureControlStatus(tempConStat);
                }
                if (isConfigured && systemStateChangeListener != null) {
                    currentHeatingWaterSystemStage = dSapi.propertyTreeGetString(connectionMananager.getSessionToken(),
                            GET_HEATING_WATER_SYSTEM_STATE_PATH);
                }
                // this.temperationControlStatus = temperationControlStatus;
            }
        }
    }

    // TODO: static isHeatingControllerInstallated
    public static boolean isHeatingControllerInstallated(ConnectionManager connectionManager) {
        if (connectionManager.checkConnection()) {
            return connectionManager.getDigitalSTROMAPI().propertyTreeGetChildren(connectionManager.getSessionToken(),
                    GET_HEATING_HEATING_CONTROLLER_CHILDREN_PATH) != null;
        }
        return false;
    }

    public Collection<TemperatureControlStatus> getTemperatureControlStatusFromAllZones() {
        return this.temperationControlStatus.values();
    }

    public void registerTemperatureControlStatusListener(
            TemperatureControlStatusListener temperatureControlStatusListener) {
        if (temperatureControlStatusListener != null) {
            if (temperatureControlStatusListener.getID().equals(TemperatureControlStatusListener.DISCOVERY)) {
                logger.debug("discovery is registered");
                this.discovery = temperatureControlStatusListener;
                if (temperationControlStatus != null) {
                    for (TemperatureControlStatus tempConStat : temperationControlStatus.values()) {
                        discovery.configChanged(tempConStat);
                    }
                }
            } else {
                if (zoneTemperationControlListenerMap == null) {
                    zoneTemperationControlListenerMap = new HashMap<Integer, TemperatureControlStatusListener>();
                }
                TemperatureControlStatus tempConStat = checkAndGetTemperatureControlStatus(
                        temperatureControlStatusListener.getID());
                if (tempConStat != null) {
                    logger.debug("register listener with id " + temperatureControlStatusListener.getID());
                    zoneTemperationControlListenerMap.put(temperatureControlStatusListener.getID(),
                            temperatureControlStatusListener);
                    temperatureControlStatusListener.registerTemperatureSensorTransreciver(this);
                    temperatureControlStatusListener.configChanged(tempConStat);
                } else {
                    temperatureControlStatusListener.onTemperatureControlIsNotConfigured();
                }
            }
        }
    }

    public void unregisterTemperatureControlStatusListener(
            TemperatureControlStatusListener temperatureControlStatusListener) {
        if (temperatureControlStatusListener != null) {
            if (temperatureControlStatusListener.getID().equals(TemperatureControlStatusListener.DISCOVERY)) {
                this.discovery = null;
                return;
            }
            if (temperatureControlStatusListener != null) {
                temperatureControlStatusListener = zoneTemperationControlListenerMap
                        .remove(temperatureControlStatusListener.getID());
                if (discovery != null && temperatureControlStatusListener != null) {
                    discovery.configChanged(temperationControlStatus.get(temperatureControlStatusListener.getID()));
                }
            }
        }
    }

    public TemperatureControlStatus checkAndGetTemperatureControlStatus(Integer zoneID) {
        TemperatureControlStatus tempConStat = this.temperationControlStatus.get(zoneID);
        if (tempConStat.getIsConfigured()) {
            return tempConStat;
        }
        return null;
    }

    private boolean isEcho(Integer zoneID, SensorEnum sensorType, Float value) {
        return echoBox.remove(zoneID + "-" + sensorType.getSensorType() + "-" + value);
    }

    private void addEcho(Integer zoneID, SensorEnum sensorType, Float value) {
        echoBox.add(zoneID + "-" + sensorType.getSensorType() + "-" + value);
    }

    @Override
    public void handleEvent(EventItem eventItem) {
        logger.debug("detect event: {}", eventItem.toString());
        try {
            if (eventItem.getName().equals(EventNames.ZONE_SENSOR_VALUE)) {
                if (SensorEnum.ROOM_TEMPERATION_SET_POINT.getSensorType().toString()
                        .equals(eventItem.getProperties().get(EventResponseEnum.SENSOR_TYPE))) {
                    Integer zoneID = Integer.parseInt(eventItem.getSource().get(EventResponseEnum.ZONEID));
                    if (zoneTemperationControlListenerMap.get(zoneID) != null) {
                        Float newValue = Float
                                .parseFloat(eventItem.getProperties().get(EventResponseEnum.SENSOR_VALUE_FLOAT));
                        if (!isEcho(zoneID, SensorEnum.ROOM_TEMPERATION_CONTROL_VARIABLE, newValue)) {
                            zoneTemperationControlListenerMap.get(zoneID).onTargetTemperatureChanged(newValue);
                        }
                    }
                }
                if (SensorEnum.ROOM_TEMPERATION_CONTROL_VARIABLE.getSensorType().toString()
                        .equals(eventItem.getProperties().get(EventResponseEnum.SENSOR_TYPE))) {
                    Integer zoneID = Integer.parseInt(eventItem.getSource().get(EventResponseEnum.ZONEID));
                    if (zoneTemperationControlListenerMap.get(zoneID) != null) {
                        Float newValue = Float
                                .parseFloat(eventItem.getProperties().get(EventResponseEnum.SENSOR_VALUE_FLOAT));
                        if (!isEcho(zoneID, SensorEnum.ROOM_TEMPERATION_CONTROL_VARIABLE, newValue)) {
                            zoneTemperationControlListenerMap.get(zoneID).onControlValueChanged(newValue.intValue());
                        }
                    }
                }
            }

            if (eventItem.getName().equals(EventNames.HEATING_CONTROL_OPERATION_MODE)) {
                if (EVALUATE_REAL_ACTIVE_MODE.equals(eventItem.getProperties().get(EventResponseEnum.ACTIONS))) {
                    if (connectionMananager.checkConnection()) {
                        Integer zoneID = Integer.parseInt(eventItem.getProperties().get(EventResponseEnum.ZONEID));
                        TemperatureControlStatus temperationControlStatus = dSapi
                                .getZoneTemperatureControlStatus(connectionMananager.getSessionToken(), zoneID, null);
                        if (temperationControlStatus != null) {
                            addTemperatureControlStatus(temperationControlStatus);
                        }
                    }
                }
            }

            if (eventItem.getName().equals(EventNames.STATE_CHANGED)) {
                if (STATE_NAME_HEATING_WATER_SYSTEM
                        .equals(eventItem.getProperties().get(EventResponseEnum.STATE_NAME))) {
                    currentHeatingWaterSystemStage = eventItem.getProperties().get(EventResponseEnum.STATE);
                    logger.debug("heating water system state changed to " + currentHeatingWaterSystemStage);
                    if (systemStateChangeListener != null) {
                        systemStateChangeListener.onSystemStateChanged(STATE_NAME_HEATING_WATER_SYSTEM,
                                currentHeatingWaterSystemStage);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Exception: ", e);
        }
    }

    private void addTemperatureControlStatus(TemperatureControlStatus temperationControlStatus) {
        if (temperationControlStatus.getIsConfigured()) {
            if (this.temperationControlStatus == null) {
                this.temperationControlStatus = new HashMap<Integer, TemperatureControlStatus>();
            }
            if (this.temperationControlStatus.get(temperationControlStatus.getZoneID()) == null && discovery != null) {
                discovery.configChanged(temperationControlStatus);
                if (!isConfigured) {
                    isConfigured = true;
                }
            }
            this.temperationControlStatus.put(temperationControlStatus.getZoneID(), temperationControlStatus);
            if (zoneTemperationControlListenerMap != null
                    && zoneTemperationControlListenerMap.get(temperationControlStatus.getZoneID()) != null) {
                zoneTemperationControlListenerMap.get(temperationControlStatus.getZoneID())
                        .configChanged(temperationControlStatus);
            }
        }
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
        return getClass().getSimpleName();
    }

    @Override
    public void setEventListener(EventListener eventListener) {
        eventListener.addEventHandler(this);
    }

    @Override
    public void unsetEventListener(EventListener eventListener) {
        eventListener.removeEventHandler(this);
    }

    @Override
    public boolean pushTargetTemperature(Integer zoneID, Float newValue) {
        if (checkAndGetTemperatureControlStatus(zoneID) != null) {
            if (connectionMananager.checkConnection()) {
                if (dSapi.pushZoneSensorValue(connectionMananager.getSessionToken(), zoneID, null, (short) 0, null,
                        newValue, SensorEnum.ROOM_TEMPERATION_SET_POINT)) {
                    addEcho(zoneID, SensorEnum.ROOM_TEMPERATION_SET_POINT, newValue);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean pushControlValue(Integer zoneID, Float newValue) {
        if (checkAndGetTemperatureControlStatus(zoneID) != null) {
            if (dSapi.pushZoneSensorValue(connectionMananager.getSessionToken(), zoneID, null,
                    FuncNameAndColorGroupEnum.TEMPERATION_CONTROL.getFunctionalColorGroup(), null, newValue,
                    SensorEnum.ROOM_TEMPERATION_CONTROL_VARIABLE)) {
                addEcho(zoneID, SensorEnum.ROOM_TEMPERATION_CONTROL_VARIABLE, newValue);
                return true;
            }
        }
        return false;
    }

    public boolean isConfigured() {
        return isConfigured;
    }

    public String getHeatingWaterSystemState() {
        return currentHeatingWaterSystemStage;
    }

    public void registerSystemStateChangeListener(SystemStateChangeListener systemStateChangeListener) {
        if (eventListener != null) {
            SUPPORTED_EVENTS.add(EventNames.STATE_CHANGED);
            eventListener.addSubscribe(EventNames.STATE_CHANGED);
        }
        this.systemStateChangeListener = systemStateChangeListener;
    }

    public void unregisterSystemStateChangeListener() {
        this.systemStateChangeListener = null;
    }
}
