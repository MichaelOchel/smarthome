/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection;

import java.util.HashMap;
import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.BaseSensorValues;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.AssignedSensors;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.SensorValues;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlConfig;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlInternals;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlStatus;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlValues;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.Apartment;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Circuit;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.CachedMeteringValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceConfig;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceSceneSpec;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.DeviceParameterClassEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringUnitsEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.SensorEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.DSID;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.Scene;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.SceneEnum;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * digitalSTROM-API based on dSS-Version higher then 1.14.5
 *
 * @author Alexander Betker
 * @see http://developer.digitalstrom.org/download/dss/dss-1.14.5-doc/dss-1.14.5-json_api.html
 *
 * @author Michael Ochel - add missing java-doc, update digitalSTROM-JSON-API as far as possible to the pfd version from
 *         June 19, 2014 and add checkConnection method
 * @author Matthias Siegele - add missing java-doc, update digitalSTROM-JSON-API as far as possible to the pfd version
 *         from
 *         June 19, 2014 and add checkConnection method
 * @see http://developer.digitalstrom.org/Architecture/v1.1/dss-json.pdf
 */
public interface DsAPI {

    /**
     * Calls the scene sceneNumber on all devices of the apartment. If groupID
     * or groupName are specified. Only devices contained in this group will be
     * addressed.
     *
     * @param groupID not required
     * @param groupName not required
     * @param sceneNumber required
     * @param force not required
     * @return true, if successful
     */
    public boolean callApartmentScene(String sessionToken, Short groupID, String groupName, Scene sceneNumber,
            Boolean force);

    /**
     * Returns all zones
     *
     * @return Apartment
     */
    public Apartment getApartmentStructure(String sessionToken);

    /**
     * Returns the list of devices in the apartment. If unassigned is true,
     * only devices that are not assigned to a zone will be returned.
     *
     * @param unassigned not required
     * @return List of devices
     */
    public List<Device> getApartmentDevices(String sessionToken);

    /**
     * Returns an array containing all digitalSTROM-Meters of the apartment.
     *
     * @param sessionToken
     * @return
     */
    public List<Circuit> getApartmentCircuits(String sessionToken);

    /**
     * Returns a list of dSID's of all meters(dSMs)
     *
     * @return String-List with dSID's
     */
    public List<String> getMeterList(String sessionToken);

    /**
     * Calls the sceneNumber on all devices in the given zone. If groupID or groupName
     * are specified only devices contained in this group will be addressed.
     *
     * @param zoneID needs either id or name
     * @param zoneName needs either id or name
     * @param groupID not required
     * @param groupName not required
     * @param sceneNumber required (only a zone/user scene is possible -> sceneNumber 0..63 )
     * @param force not required
     * @return true on success
     */
    public boolean callZoneScene(String sessionToken, Integer zoneID, String zoneName, Short groupID, String groupName,
            SceneEnum sceneNumber, Boolean force);

    /**
     * Turns the device on. This will call the scene "max" on the device.
     *
     * @param dSIDneeds either dSIDid or name
     * @param deviceName needs either dSIDid or name
     * @return true, if successful
     */
    public boolean turnDeviceOn(String sessionToken, DSID dSID, String deviceName);

    /**
     * Turns the device off. This will call the scene "min" on the device.
     *
     * @param dSID needs either dSID or name
     * @param deviceName needs either dSID or name
     * @return true, if successful
     */
    public boolean turnDeviceOff(String sessionToken, DSID dSID, String deviceName);

    /**
     * Sets the output value of device.
     *
     * @param dSID needs either dSID or name
     * @param deviceName needs either dSID or name
     * @param value required (0 - 255)
     * @return true, if successful
     */
    public boolean setDeviceValue(String sessionToken, DSID dSID, String deviceName, Integer value);

    /**
     * Gets the value of configuration class at offset index.
     *
     * @param dSID needs either dSID or name
     * @param deviceName needs either dSID or name
     * @param clazz required
     * @param index required
     * @return config with values
     */
    public DeviceConfig getDeviceConfig(String sessionToken, DSID dSID, String deviceName,
            DeviceParameterClassEnum clazz, Integer index);

    /**
     * Gets the device output value from parameter at the given offset.
     * The available parameters and offsets depend on the features of the
     * hardware components.
     *
     * @param dSID needs either dSID or name
     * @param deviceName needs either dSID or name
     * @param offset required (known offset f.e. 0)
     * @return
     */
    public int getDeviceOutputValue(String sessionToken, DSID dSID, String deviceName, Short offset);

    /**
     * Sets the device output value at the given offset. The available
     * parameters and offsets depend on the features of the hardware components.
     *
     * @param dSIDneeds either dSID or name
     * @param deviceName needs either dSID or name
     * @param offset required
     * @param value required (0 - 65535)
     * @return true, if successful
     */
    public boolean setDeviceOutputValue(String sessionToken, DSID dSID, String deviceName, Short offset, Integer value);

    /**
     * Gets the device configuration for a specific scene command.
     *
     * @param dSID needs either dSID or name
     * @param deviceName needs either dSID or name
     * @param sceneID required (0 .. 255)
     * @return scene configuration
     */
    public DeviceSceneSpec getDeviceSceneMode(String sessionTokens, DSID dSID, String deviceName, Short sceneID);

    /**
     * Requests the sensor value for a given index.
     *
     * @param dSID needs either dSID or name
     * @param deviceName needs either dSID or name
     * @param sensorIndex required
     * @return sensor value
     */
    public short getDeviceSensorValue(String sessionToken, DSID dSID, String deviceName, Short sensorIndex);

    /**
     * Calls scene sceneNumber on the device.
     *
     * @param dSID needs either dSID or name
     * @param deviceName needs either dSID or name
     * @param sceneNumber required
     * @param force not required
     * @return true, if successful
     */
    public boolean callDeviceScene(String sessionToken, DSID dSID, String deviceName, Scene sceneNumber, Boolean force);

    /**
     * Subscribes to an event given by the name. The subscriptionID is a unique id
     * that is defined by the subscriber. It is possible to subscribe to several events,
     * using the same subscription id, this allows to retrieve a grouped output of the
     * events (i.e. get output of all subscribed by the given id).
     *
     * @param eventName required
     * @param subscriptionID required
     * @return true on success
     */
    public boolean subscribeEvent(String sessionToken, String eventName, Integer subscriptionID, int connectionTimeout,
            int readTimeout);

    /**
     * Unsubscribes from an event given by the name. The subscriptionID is a unique
     * id that was used in the subscribe call.
     *
     * @param eventName required
     * @param subscriptionID required
     * @return true on success
     */
    public boolean unsubscribeEvent(String sessionToken, String eventName, Integer subscriptionID,
            int connectionTimeout, int readTimeout);

    /**
     * Gets event information and output. The subscriptionID is a unique id
     * that was used in the subscribe call. All events, subscribed with the
     * given id will be handled by this call. A timeout, in case no events
     * are taken place, can be specified (in ms). By default the timeout
     * is disabled: 0 (zero), if no events occur the call will block.
     *
     * @param subscriptionID required
     * @param timeout optional
     * @return Event-String
     */
    public String getEvent(String sessionToken, Integer subscriptionID, Integer timeout);

    /**
     * Returns the dSS time in UTC seconds since epoch.
     *
     * @return time
     */
    public int getTime(String sessionToken);

    /**
     * Creates a new session using the registered application token
     *
     * @param applicationToken required
     * @return sessionToken
     */
    public String loginApplication(String applicationToken);

    /**
     * Creates a new session
     *
     * @param user required
     * @param password required
     */
    public String login(String user, String password);

    /**
     * Destroys the session and signs out the user
     */
    public boolean logout();

    /**
     * Returns the dSID of the digitalSTROM Server.
     *
     * @param sessionToken required
     * @return dsID
     */
    public String getDSID(String sessionToken);

    /**
     * Returns a token for passwordless login. The token will need to be approved
     * by a user first, the caller must not be logged in.
     *
     * @param applicationName required
     * @return applicationToken
     */
    public String requestAppplicationToken(String applicationName);

    /**
     * Revokes an application token, caller must be logged in.
     *
     * @param applicationToken
     */
    public boolean revokeToken(String applicationToken, String sessionToken);

    /**
     * Enables an application token, caller must be logged in.
     *
     * @param applicationToken required
     */
    public boolean enableApplicationToken(String applicationToken, String sessionToken);

    /**
     * Returns all resolutions stored on this dSS
     *
     * @return List of resolutions
     */
    public List<Integer> getResolutions(String sessionToken);

    /**
     * Returns cached energy meter value or cached power consumption
     * value in watt (W). The type parameter defines what should
     * be returned, valid types, 'energyDelta' are 'energy' and
     * 'consumption' you can also see at {@link MeteringTypeEnum}. 'energy' and 'energyDelta' are available in two
     * units: 'Wh' (default) and 'Ws' you can also see at {@link MeteringUnitsEnum}. The meterDSIDs parameter follows
     * the
     * set-syntax, currently it supports: .meters(dsid1,dsid2,...) and .meters(all)
     *
     * @param type required
     * @param meterDSIDs required
     * @param unit optional
     * @return cached metering values
     */
    public List<CachedMeteringValue> getLatest(String sessionToken, MeteringTypeEnum type, String meterDSIDs,
            MeteringUnitsEnum unit);

    /**
     * Returns cached energy meter value or cached power consumption
     * value in watt (W). The type parameter defines what should
     * be returned, valid types, 'energyDelta' are 'energy' and
     * 'consumption' you can also see at {@link MeteringTypeEnum}. 'energy' and 'energyDelta' are available in two
     * units: 'Wh' (default) and 'Ws' you can also see at {@link MeteringUnitsEnum}. <br>
     * The meterDSIDs parameter you can directly pass a {@link List} of the digitalSTROM-Meter dSID's as {@link String}.
     *
     * @param type required
     * @param meterDSIDs required
     * @param unit optional
     * @return cached metering values
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public List<CachedMeteringValue> getLatest(String sessionToken, MeteringTypeEnum type, List<String> meterDSIDs,
            MeteringUnitsEnum unit);

    /**
     * Checks the connection and returns the HTTP-Status-Code.
     *
     * @param sessionToken required
     * @return HTTP-Status-Code
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public int checkConnection(String sessionToken);

    /**
     * Returns the configured scene output value for the given sceneId of the digitalSTROM-Device with the given dSID.
     * <br>
     * At array position 0 is the output value and at position 1 the angle value, if the device is a blind.
     *
     * @param sessionToken required
     * @param dSID required
     * @param sceneId required
     * @return scene value at array position 0 and angle at position 1
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public int[] getSceneValue(String sessionToken, DSID dSID, Short sceneId);

    /**
     * Calls the INC scene on the digitalSTROM-Device with the given dSID and returns true if the request was success.
     *
     * @param sessionToken required
     * @param dSID required
     * @return success true otherwise false
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public boolean increaseValue(String sessionToken, DSID dSID);

    /**
     * Calls the DEC scene on the digitalSTROM-Device with the given dSID and returns true if the request was
     * successful.
     *
     * @param sessionToken required
     * @param dSID required
     * @return success true otherwise false
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public boolean decreaseValue(String sessionToken, DSID dSID);

    /**
     * Undos the given sceneNumer of the digitalSTROM-Device with the given dSID and returns true if the request was
     * successful.
     *
     * @param sessionToken required
     * @param dSID required
     * @param sceneNumber required
     * @return success true otherwise false
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public boolean undoDeviceScene(String sessionToken, DSID dsid, Scene sceneNumber);

    /**
     * Undo the given sceneNumer on the digitalSTROM apartment-group with the given groupID or groupName and returns
     * true
     * if the request was successful.
     *
     * @param sessionToken required
     * @param groupID needs either groupID or groupName
     * @param groupName needs either groupID or groupName
     * @param sceneNumber required
     * @return success true otherwise false
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public boolean undoApartmentScene(String sessionToken, Short groupID, String groupName, Scene sceneNumber);

    /**
     * Undo the given sceneNumer on the digitalSTROM zone-group with the given zoneID or zoneName and groupID or
     * groupName and returns true if the request was successful.
     *
     * @param sessionToken
     * @param zoneID needs either zoneID or zoneName
     * @param zoneName needs either zoneID or zoneName
     * @param groupID needs either groupID or groupName
     * @param groupName needs either groupID or groupName
     * @param sceneNumber required
     * @return success true otherwise false
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public boolean undoZoneScene(String sessionToken, Integer zoneID, String zoneName, Short groupID, String groupName,
            SceneEnum sceneNumber);

    /**
     * Returns the digitalSTROM-device sensor value for the digitalSTROM-device with the given dSID or deviceName and
     * the given sensorType. If the sensorType is supports from the device and the request was successful it returns
     * the sensor value otherwise -1.
     *
     * @param sessionToken required
     * @param dSID needs either dSID or deviceName
     * @param name
     * @param sensortype required
     * @return success sensor value otherwise -1
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    // public short getDeviceSensorValue(String sessionToken, DSID dSID, String deviceName, SensorEnum sensorType);

    /**
     * Returns user defined name of the digitalSTROM installation.
     *
     * @param sessionToken required
     * @return name of the digitalSTROM installation
     */
    public String getInstallationName(String sessionToken);

    /**
     * Returns user defined name of the zone from the given zone id.
     *
     * @param sessionToken required
     * @param zoneID required
     * @return name of the given zone id
     */
    public String getZoneName(String sessionToken, Integer zoneID);

    /**
     * Returns user defined name of the device from the given dSID
     *
     * @param sessionToken required
     * @param dSID required
     * @return name of the given device dSID
     */
    public String getDeviceName(String sessionToken, DSID dSID);

    /**
     * Returns user defined name of the circuit from the given dSID.
     *
     * @param sessionToken required
     * @param dSID required
     * @return name of the given circuit dSID
     */
    public String getCircuitName(String sessionToken, DSID dSID);

    /**
     * Returns user defined name of the scene from the given zoneID, groupID and sceneID.
     *
     * @param sessionToken required
     * @param zoneID (0 is broadcast)
     * @param groupID (0 is broadcast)
     * @param sceneID (between 0 and 127)
     * @return name of the scene otherwise null
     */
    public String getSceneName(String sessionToken, Integer zoneID, Short groupID, Short sceneID);

    /**
     * Returns the temperature control status to the given zone.
     *
     * @param sessionToken required
     * @param zoneID
     * @return temperature control status to the given zone
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public TemperatureControlStatus getZoneTemperatureControlStatus(String sessionToken, Integer zoneID,
            String zoneName);

    /**
     * Returns the temperature control configuration of the given zone. It's like the temperature control status added
     * by the following control values.
     *
     * CtrlKp = Control proportional factor
     * CtrlTs = Control sampling time
     * CtrlTi = Control integrator time constant
     * CtrlKd = Control differential factor
     * CtrlImin = Control minimum integrator value
     * CtrlImax = Control maximum integrator value
     * CtrlYmin = Control minimum control value
     * CtrlYmax = Control maximum control value
     * CtrlAntiWindUp = Control integrator anti wind up: 0=inactive, 1=active
     * CtrlKeepFloorWarm = Control mode with higher priority on comfort: 0=inactive, 1=active
     *
     * @param sessionToken required
     * @param zoneID required
     * @return temperature control status with configuration parameters
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public TemperatureControlConfig getZoneTemperatureControlConfig(String sessionToken, Integer zoneID,
            String zoneName);

    /**
     * Returns the temperature control values to their control modes of the given zone.
     * There are following control modes:
     *
     * <li>0 Off</li>
     * <li>1 Comfort</li>
     * <li>2 Economy</li>
     * <li>3 Not Used</li>
     * <li>4 Night</li>
     * <li>5 Holiday</li>
     * <li>6 Cooling</li>
     * <li>7 CollingOff</li>
     *
     *
     * @param sessionToken required
     * @param zoneID required
     * @return temperature control values of control modes
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public TemperatureControlValues getZoneTemperatureControlValues(String sessionToken, Integer zoneID,
            String zoneName);

    /**
     * Set the configuration of the zone temperature control.
     *
     * @param sessionToken (required)
     * @param zoneID (required alternative zoneName)
     * @param zoneName (required alternative zoneID)
     * @param controlDSUID dSUID of the meter or service that runs the control algorithm for this zone (optional)
     * @param ControlMode Control mode, can be one of: 0=off; 1=pid-control; 2=zone-follower; 3=fixed-value; 4=manual
     *            (Optional)
     * @param ReferenceZone Zone number of the reference zone (Optional for ControlMode 2)
     * @param ctrlOffset Control value offset (Optional for ControlMode 2)
     * @param emergencyValue Fixed control value in case of malfunction (Optional for ControlMode 1)
     * @param manualValue Control value for manual mode (Optional for ControlMode 1)
     * @param ctrlKp Control proportional factor (Optional for ControlMode 1)
     * @param ctrlTs Control sampling time (Optional for ControlMode 1)
     * @param ctrlTi Control integrator time constant (Optional for ControlMode 1)
     * @param ctrlKd Control differential factor (Optional for ControlMode 1)
     * @param ctrlImin Control minimum integrator value (Optional for ControlMode 1)
     * @param ctrlImax Control maximum integrator value (Optional for ControlMode 1)
     * @param ctrlYmin Control minimum control value (Optional for ControlMode 1)
     * @param ctrlYmay Control maximum control value (Optional for ControlMode 1)
     * @param ctrlAntiWindUp Control integrator anti wind up (Optional for ControlMode 1)
     * @param ctrlKeepFloorWarm Control mode with higher priority on comfort (Optional for ControlMode 1)
     * @return
     */
    public boolean setZoneTemperatureControlConfig(String sessionToken, Integer zoneID, String zoneName,
            String controlDSUID, Short controlMode, Integer referenceZone, Float ctrlOffset, Float emergencyValue,
            Float manualValue, Float ctrlKp, Float ctrlTs, Float ctrlTi, Float ctrlKd, Float ctrlImin, Float ctrlImax,
            Float ctrlYmin, Float ctrlYmax, Boolean ctrlAntiWindUp, Boolean ctrlKeepFloorWarm);

    /**
     * Returns the assigned Sensor dSUID of a zone.
     *
     *
     * @param sessionToken required
     * @param zoneID required
     * @return assigned Sensor dSUID of the given zone.
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public AssignedSensors getZoneAssignedSensors(String sessionToken, Integer zoneID, String zoneName);

    /**
     * Sets the temperature control state of a given zone.<br>
     * Control states: 0=internal; 1=external; 2=exbackup; 3=emergency
     *
     * @param sessionToken required
     * @param zoneID required
     * @return success true otherwise false
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public boolean setZoneTemperatureControlState(String sessionToken, Integer zoneID, String zoneName,
            String controlState);

    /**
     * Sets the wished temperature for a controlValue
     *
     * @param sessionToken
     * @param zoneID
     * @param controlValue
     * @param temperature
     * @return success true otherwise false
     */
    public boolean setZoneTemperatureControlValue(String sessionToken, Integer zoneID, String zoneName,
            String controlValue, Float temperature);
    // TODO: add einzelwert oder Liste

    /**
     * Returns the value of a Sensor of the given zone.
     *
     *
     * @param sessionToken required
     * @param zoneID required
     * @return value of a Sensor of the given zone
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public SensorValues getZoneSensorValues(String sessionToken, Integer zoneID, String zoneName);

    /**
     * Set the source of a sensor in a zone to a given device source address.
     *
     * @param sessionToken
     * @param sensorType
     * @param dSID
     * @return success true otherwise false
     */
    public boolean setZoneSensorSource(String sessionToken, Integer zoneID, String zoneName, SensorEnum sensorType,
            DSID dSID);

    /**
     * Remove all assignments for a particular sensor type in a zone.
     *
     * @param sessionToken
     * @param sensorType
     * @param zoneID
     * @return success true otherwise false
     *
     */
    public boolean clearZoneSensorSource(String sessionToken, Integer zoneID, String zoneName, SensorEnum sensorType);

    /**
     * Returns internal status information of the temperature control of a zone.
     *
     * @param sessionToken
     * @param zoneID
     * @return internal status information of the temperature control of a zone
     */
    public TemperatureControlInternals getZoneTemperatureControlInternals(String sessionToken, Integer zoneID,
            String zoneName);

    /**
     * Returns the temperature control status of all zones.
     *
     * @param sessionToken required
     * @return temperature control status of all zones
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public HashMap<Integer, TemperatureControlStatus> getApartmentTemperatureControlStatus(String sessionToken);

    /**
     * Returns the temperature control status of all zones.
     *
     * @param sessionToken required
     * @return temperature control status of all zones
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public HashMap<Integer, TemperatureControlConfig> getApartmentTemperatureControlConfig(String sessionToken);

    /**
     * Returns the temperature control status of all zones.
     *
     * @param sessionToken required
     * @return temperature control status of all zones
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public HashMap<Integer, TemperatureControlValues> getApartmentTemperatureControlValues(String sessionToken);

    /**
     * Returns the assigned Sensor dSUID of all zones.
     *
     *
     * @param sessionToken required
     * @return assigned Sensor dSUID of all zones.
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public HashMap<Integer, AssignedSensors> getApartmentAssignedSensors(String sessionToken);

    /**
     * Returns the value of a Sensor of all zones.
     *
     * @param sessionToken required
     * @return value of a Sensor of all zones
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public HashMap<Integer, BaseSensorValues> getApartmentSensorValues(String sessionToken);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Returns a part of the tree specified by query. All queries start from the root.
     * The properties to be included have to be put in parentheses. A query to get
     * all device from zone4 would look like this: ’/apartment/zones/zone4/*(ZoneID,name)’.
     * More complex combinations (see example below) are also possible.
     *
     * @param token required
     * @param query required
     * @return response as {@link JsonObject}
     */
    public JsonObject query(String token, String query);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Differs from query(1) only in the format of the the returned json struct.<br>
     * <br>
     * <i>Folder selects the nodes to descend, Property declares which attributes
     * we are extracting from the current node. If no properties are declared for a
     * folder, nothing is extracted, and the node will not show up in the resulting
     * json structure.</i>
     *
     * @param token required
     * @param query required
     * @return response as {@link JsonObject}
     */
    public JsonObject query2(String token, String query);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Set the output value of a group of devices in a zone to a given value. <br>
     * <br>
     * <b>Notice:</b> Setting output values directly bypasses the group state machine
     * and is not recommended.<br>
     * <br>
     * If the group parameters are omitted the command is sent as broadcast
     * to all devices in the selected zone. <br>
     * <br>
     * <b>Notice:</b> Setting output values without a group identification is strongly
     * unrecommended.<br>
     *
     * @param sessionToken required
     * @param zoneID or zoneName are required
     * @param zoneName or zoneID are required
     * @param groupID optional
     * @param groupName optional
     * @param value required
     * @return true, if request was successful, otherwise false
     */
    public boolean setZoneOutputValue(String sessionToken, Integer zoneID, String zoneName, Short groupID,
            String groupName, Integer value);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Executes the ”blink” function on a group of devices in a zone for identification
     * purposes.
     *
     * @param sessionToken required
     * @param zoneID or zoneName are required
     * @param zoneName or zoneID are required
     * @param groupID optional
     * @param groupName optional
     * @return true, if request was successful, otherwise false
     */
    public boolean zoneBlink(String sessionToken, Integer zoneID, String zoneName, Short groupID, String groupName);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Send a sensor value to a group of devices in a zone.<br>
     * If the group parameter is omitted the command is sent as broadcast to
     * all devices in the selected zone. The reference for the sensor type definitions
     * can be found in the ds-basics document.
     *
     * @param sessionToken required
     * @param zoneID or zoneName are required
     * @param zoneName or zoneID are required
     * @param groupID optional
     * @param sourceDSUID optional
     * @param sensorValue required
     * @param sensorType required
     * @return true, if request was successful, otherwise false
     */
    public boolean pushZoneSensorValue(String sessionToken, Integer zoneID, String zoneName, Short groupID,
            String sourceDSUID, Float sensorValue, SensorEnum sensorType);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Returns the string value of the property, this call will fail if the property is
     * not of type ’string’.
     *
     * @param token required
     * @param path to property required
     * @return string value of the property
     */
    public String propertyTreeGetString(String token, String path);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Sets the string value of the property, this call will fail if the property is not
     * of type ’string’.
     *
     * @param token required
     * @param path to property required
     * @return {@link JsonArray} of child nodes
     */
    public JsonArray propertyTreeGetChildren(String token, String path);

}
