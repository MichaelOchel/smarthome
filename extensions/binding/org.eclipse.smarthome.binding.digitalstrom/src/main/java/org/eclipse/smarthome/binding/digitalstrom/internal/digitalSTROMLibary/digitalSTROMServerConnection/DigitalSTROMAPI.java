/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMServerConnection;

import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.Apartment;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.CachedMeteringValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DSID;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceConfig;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceParameterClassEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceSceneSpec;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.MeteringUnitsEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.SensorEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.SensorIndexEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.constants.Scene;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.constants.SceneEnum;

/**
 * digitalSTROM-API based on dSS-Version higher then 1.14.5
 *
 * @author Alexander Betker
 * @see http://developer.digitalstrom.org/download/dss/dss-1.14.5-doc/dss-1.14.5-json_api.html
 * @since 1.3.0
 *
 * @author Michael Ochel - add missing java-doc, update digitalSTROM-JSON-API as far as possible to the pfd version from
 *         June 19, 2014 and add checkConnection method
 * @author Matthias Siegele - add missing java-doc, update digitalSTROM-JSON-API as far as possible to the pfd version
 *         from
 *         June 19, 2014 and add checkConnection method
 * @see http://developer.digitalstrom.org/Architecture/v1.1/dss-json.pdf
 */
public interface DigitalSTROMAPI {

    /**
     * Calls the scene sceneNumber on all devices of the apartment. If groupID
     * or groupName are specified, only devices contained in this group will be
     * addressed
     *
     * @param groupID this parameter is optional (not required)
     * @param groupName this parameter is optional (not required)
     * @param sceneNumber required
     * @param force this parameter is optional (not required)
     * @return true on success
     */
    public boolean callApartmentScene(String token, int groupID, String groupName, Scene sceneNumber, boolean force);

    /**
     * Returns all zones
     *
     * @return DigitalSTROMApartment which has a list of all zones
     */
    public Apartment getApartmentStructure(String token);

    /**
     * Returns the list of devices in the apartment. If unassigned is true,
     * only devices that are not assigned to a zone get returned
     *
     * @param unassigned this parameter is optional (not required)
     * @return List of DigitalSTROMDevices
     */
    public List<Device> getApartmentDevices(String token, boolean unassigned);

    /**
     * Returns a list of dsids of all meters(dSMs)
     *
     * @return String-List with dsids
     */
    public List<String> getMeterList(String token);

    /**
     * Sets the scene sceneNumber on all devices in the zone. If groupID or groupName
     * are specified, only devices contained in this group will be addressed
     *
     * @param id needs either id or name
     * @param name needs either id or name
     * @param groupID this parameter is optional (not required)
     * @param groupName this parameter is optional (not required)
     * @param sceneNumber required (only a zone/user scene is possible -> sceneNumber 0..63 )
     * @param force this parameter is optional (not required)
     * @return true on success
     */
    public boolean callZoneScene(String token, int id, String name, int groupID, String groupName,
            SceneEnum sceneNumber, boolean force);

    /**
     * Turns on the device. This will call SceneMax on the device
     *
     * @param dsid needs either dsid id or name
     * @param name needs either dsid id or name
     * @return true on success
     */
    public boolean turnDeviceOn(String token, DSID dsid, String name);

    /**
     * Turns off the device. This will call SceneMin on the device
     *
     * @param dsid needs either dsid id or name
     * @param name needs either dsid id or name
     * @return true on success
     */
    public boolean turnDeviceOff(String token, DSID dsid, String name);

    /**
     * Set the output value of device
     *
     * @param dsid needs either dsid id or name
     * @param name needs either dsid id or name
     * @param value required (0 - 255)
     * @return true on success
     */
    public boolean setDeviceValue(String token, DSID dsid, String name, int value);

    /**
     * Gets the value of config class at offset index
     *
     * @param dsid needs either dsid id or name
     * @param name needs either dsid id or name
     * @param class_ required
     * @param index required
     * @return config with values
     */
    public DeviceConfig getDeviceConfig(String token, DSID dsid, String name, DeviceParameterClassEnum class_,
            int index);

    /**
     * Gets the device output value from parameter at the given offset.
     * The available parameters and offsets depend on the features of the
     * hardware components
     *
     * @param dsid needs either dsid id or name
     * @param name needs either dsid id or name
     * @param offset required (known offset f.e. 0)
     * @return
     */
    public int getDeviceOutputValue(String token, DSID dsid, String name, int offset);

    /**
     * Sets the device output value at the given offset. The available
     * parameters and offsets depend on the features of the hardware components
     *
     * @param dsid needs either dsid id or name
     * @param name needs either dsid id or name
     * @param offset required
     * @param value required (0 - 65535)
     * @return true on success
     */
    public boolean setDeviceOutputValue(String token, DSID dsid, String name, int offset, int value);

    /**
     * Gets the device configuration for a specific scene command
     *
     * @param dsid needs either dsid id or name
     * @param name needs either dsid id or name
     * @param sceneID required (0 .. 255)
     * @return
     */
    public DeviceSceneSpec getDeviceSceneMode(String token, DSID dsid, String name, short sceneID);

    /**
     * Request the sensor value of a given index
     *
     * @param dsid needs either dsid id or name
     * @param name needs either dsid id or name
     * @param sensorIndex required
     * @return
     */
    public short getDeviceSensorValue(String token, DSID dsid, String name, SensorIndexEnum sensorIndex);

    /**
     * Calls scene sceneNumber on the device
     *
     * @param dsid needs either dsid id or name
     * @param name needs either dsid id or name
     * @param sceneNumber required
     * @param force this parameter is optional (not required)
     * @return true on success
     */
    public boolean callDeviceScene(String token, DSID dsid, String name, Scene sceneNumber, boolean force);

    /**
     * Subscribes to an event given by the name. The subscriptionID is a unique id
     * that is defined by the subscriber. It is possible to subscribe to several events,
     * using the same subscription id, this allows to retrieve a grouped output of the
     * events (i.e. get output of all subscribed by the given id)
     *
     * @param name required
     * @param subscriptionID required
     * @return true on success
     */
    public boolean subscribeEvent(String token, String name, int subscriptionID, int connectionTimeout,
            int readTimeout);

    /**
     * Unsubscribes from an event given by the name. The subscriptionID is a unique
     * id that was used in the subscribe call
     *
     * @param name required
     * @param subscriptionID required
     * @return true on success
     */
    public boolean unsubscribeEvent(String token, String name, int subscriptionID, int connectionTimeout,
            int readTimeout);

    /**
     * Get event information and output. The subscriptionID is a unique id
     * that was used in the subscribe call. All events, subscribed with the
     * given id will be handled by this call. A timout, in case no events
     * are taken place, can be specified (in MS). By default the timeout
     * is disabled: 0 (zero), if no events occur the call will block.
     *
     * @param subscriptionID required
     * @param timeout optional
     * @return Event-String
     */
    public String getEvent(String token, int subscriptionID, int timeout);

    /**
     * Returns the dSS time in UTC seconds since epoch
     *
     * @return
     */
    public int getTime(String token);

    /**
     * Creates a new session using the registered application token
     *
     * @param loginToken required
     */
    public String loginApplication(String loginToken);

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
     * @param token required
     * @return dsID
     */
    public String getDSID(String token);

    /**
     * Returns a token for paswordless login. The token will need to be approved
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
    public List<Integer> getResolutions(String token);

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
    public List<CachedMeteringValue> getLatest(String token, MeteringTypeEnum type, String meterDSIDs,
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
    public List<CachedMeteringValue> getLatest(String token, MeteringTypeEnum type, List<String> meterDSIDs,
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
     *
     * @param sessionToken required
     * @param dsid required
     * @param sceneId required
     * @return scene value
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public int getSceneValue(String sessionToken, DSID dSID, short sceneId);

    /**
     * Call the INC scene on the digitalSTROM-Device with the given dSID and returns true if the request was success.
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
     * Call the DEC scene on the digitalSTROM-Device with the given dSID and returns true if the request was successful.
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
     * Undo the given sceneNumer of the digitalSTROM-Device with the given dSID and returns true if the request was
     * successful.
     *
     * @param sessionToken required
     * @param dsid required
     * @param sceneNumber required
     * @return success true otherwise false
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    boolean undoDeviceScene(String sessionToken, DSID dsid, Scene sceneNumber);

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
    boolean undoApartmentScene(String sessionToken, int groupID, String groupName, Scene sceneNumber);

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
    boolean undoZoneScene(String sessionToken, int zoneID, String zoneName, int groupID, String groupName,
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
    public short getDeviceSensorValue(String sessionToken, DSID dSID, String deviceName, SensorEnum sensortype);

}
