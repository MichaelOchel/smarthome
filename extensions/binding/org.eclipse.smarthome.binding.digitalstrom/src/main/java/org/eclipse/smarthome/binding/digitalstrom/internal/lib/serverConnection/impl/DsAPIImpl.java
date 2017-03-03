/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.BaseSensorValues;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.AssignedSensors;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.SensorValues;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlConfig;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlInternals;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlStatus;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlValues;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.WeatherSensorData;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.DsAPI;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.HttpTransport;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleDSRequestBuilder.SimpleRequestBuilder;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleDSRequestBuilder.constants.ClassKeys;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleDSRequestBuilder.constants.FunctionKeys;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleDSRequestBuilder.constants.InterfaceKeys;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleDSRequestBuilder.constants.ParameterKeys;
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
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.JSONCachedMeteringValueImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.JSONDeviceConfigImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.JSONDeviceSceneSpecImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.impl.CircuitImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.impl.DeviceImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.impl.JSONApartmentImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.Scene;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.SceneEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link DsAPIImpl} is the implementation of the {@link DsAPI}.
 *
 * @author Alexander Betker - initial contributer
 * @author Alex Maier - initial contributer
 * @author Michael Ochel - implements new methods, API updates and change SimpleJSON to GSON, add helper methods and
 *         requests building with constants to {@link SimpleRequestBuilder}
 * @author Matthias Siegele - implements new methods, API updates and change SimpleJSON to GSON, add helper methods and
 *         requests building with constants to {@link SimpleRequestBuilder}
 */
public class DsAPIImpl implements DsAPI {

    private Logger logger = LoggerFactory.getLogger(DsAPIImpl.class);
    private HttpTransport transport = null;

    private final String QUERY_GET_METERLIST = "/apartment/dSMeters/*(dSID)";

    /**
     * Contains methods where no login is required.
     */
    public static final List<String> METHODS_MUST_NOT_BE_LOGGED_IN = Lists.newArrayList("login",
            "requestApplicationToken", "version", "time", "getDSID", "logout", "loginApplication");

    /**
     * Create a new {@link DsAPIImpl} with the given {@link HttpTransport}.
     *
     * @param transport for connection, must not be null
     */
    public DsAPIImpl(HttpTransport transport) {
        this.transport = transport;
    }

    /**
     * Creates a new {@link DsAPIImpl} with creating a new {@link HttpTransport}, parameters see
     * {@link HttpTransportImpl#HttpTransportImpl(String, int, int)}.
     *
     * @param uri of the digitalSTROM-Server, must not be null
     * @param connectTimeout to set
     * @param readTimeout to set
     */
    public DsAPIImpl(String uri, int connectTimeout, int readTimeout) {
        this.transport = new HttpTransportImpl(uri, connectTimeout, readTimeout);
    }

    /**
     * Creates a new {@link DsAPIImpl} with creating a new {@link HttpTransport}, parameters see
     * {@link HttpTransportImpl#HttpTransportImpl(String, int, int, boolean)}.
     *
     * @param uri of the digitalSTROM-Server, must not be null
     * @param connectTimeout to set
     * @param readTimeout to set
     * @param aceptAllCerts yes/no (true/false)
     */
    public DsAPIImpl(String uri, int connectTimeout, int readTimeout, boolean aceptAllCerts) {
        this.transport = new HttpTransportImpl(uri, connectTimeout, readTimeout, aceptAllCerts);
    }

    private String convertShortToString(Short arg) {
        return arg != null && arg > -1 ? arg.toString() : null;
    }

    private String convertIntegerToString(Integer arg) {
        return arg != null && arg > -1 ? arg.toString() : null;
    }

    private String convertFloatToString(Float arg) {
        return arg != null ? arg.toString() : null;
    }

    private String getDsidString(DSID dsid) {
        return dsid != null ? dsid.getValue() : null;
    }

    private String objectToString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    private boolean isValidApartmentSceneNumber(int sceneNumber) {
        return (sceneNumber > -1 && sceneNumber < 256);
    }

    private boolean checkBlankField(JsonObject obj, String key) {
        return obj != null && obj.get(key) != null;
    }

    private boolean checkRequiredZone(Integer zoneID, String zoneName) {
        return zoneID != null && zoneID > -1 || StringUtils.isNotBlank(zoneName);
    }

    private boolean checkRequiredDevice(DSID dsid, String dSUID, String name) {
        return StringUtils.isNotBlank(getDsidString(dsid)) || StringUtils.isNotBlank(name)
                || StringUtils.isNotBlank(dSUID);
    }

    @Override
    public boolean callApartmentScene(String token, Short groupID, String groupName, Scene sceneNumber, Boolean force) {
        if (sceneNumber != null && isValidApartmentSceneNumber(sceneNumber.getSceneNumber())) {
            try {
                String response = transport.execute(
                        SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.APARTMENT)
                                .addFunction(FunctionKeys.CALL_SCENE).addParameter(ParameterKeys.TOKEN, token)
                                .addParameter(ParameterKeys.GROUP_ID, convertShortToString(groupID))
                                .addParameter(ParameterKeys.GROUP_NAME, groupName)
                                .addParameter(ParameterKeys.SCENENUMBER, sceneNumber.toString())
                                .addParameter(ParameterKeys.FORCE, force.toString()).buildRequestString());
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public boolean undoApartmentScene(String token, Short groupID, String groupName, Scene sceneNumber) {
        if (sceneNumber != null && isValidApartmentSceneNumber(sceneNumber.getSceneNumber())) {
            try {
                String response = transport.execute(
                        SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.APARTMENT)
                                .addFunction(FunctionKeys.UNDO_SCENE).addParameter(ParameterKeys.TOKEN, token)
                                .addParameter(ParameterKeys.GROUP_ID, convertShortToString(groupID))
                                .addParameter(ParameterKeys.GROUP_NAME, groupName)
                                .addParameter(ParameterKeys.SCENENUMBER, sceneNumber.getSceneNumber().toString())
                                .buildRequestString());
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public Apartment getApartmentStructure(String token) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.APARTMENT).addFunction(FunctionKeys.GET_STRUCTURE)
                    .addParameter(ParameterKeys.TOKEN, token).buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject apartObj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (checkBlankField(apartObj, JSONApiResponseKeysEnum.APARTMENT.getKey())) {
                    return new JSONApartmentImpl((JsonObject) apartObj.get(JSONApiResponseKeysEnum.APARTMENT.getKey()));
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public List<Device> getApartmentDevices(String token) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.APARTMENT).addFunction(FunctionKeys.GET_DEVICES)
                    .addParameter(ParameterKeys.TOKEN, token).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
            if (JSONResponseHandler.checkResponse(responseObj)
                    && responseObj.get(JSONApiResponseKeysEnum.RESULT.getKey()) instanceof JsonArray) {
                JsonArray array = (JsonArray) responseObj.get(JSONApiResponseKeysEnum.RESULT.getKey());

                List<Device> deviceList = new LinkedList<Device>();
                for (int i = 0; i < array.size(); i++) {
                    if (array.get(i) instanceof JsonObject) {
                        deviceList.add(new DeviceImpl((JsonObject) array.get(i)));
                    }
                }
                return deviceList;
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return new LinkedList<Device>();
    }

    @Override
    public List<Circuit> getApartmentCircuits(String sessionToken) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.APARTMENT).addFunction(FunctionKeys.GET_CIRCUITS)
                    .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
            if (JSONResponseHandler.checkResponse(responseObj)) {
                responseObj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (responseObj.get(JSONApiResponseKeysEnum.CIRCUITS.getKey()).isJsonArray()) {
                    JsonArray array = responseObj.get(JSONApiResponseKeysEnum.CIRCUITS.getKey()).getAsJsonArray();

                    List<Circuit> circuitList = new LinkedList<Circuit>();
                    for (int i = 0; i < array.size(); i++) {
                        if (array.get(i).isJsonObject()) {
                            circuitList.add(new CircuitImpl(array.get(i).getAsJsonObject()));
                        }
                    }
                    return circuitList;
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return new LinkedList<Circuit>();
    }

    @Override
    public boolean callZoneScene(String token, Integer zoneID, String zoneName, Short groupID, String groupName,
            SceneEnum sceneNumber, Boolean force) {
        if (checkRequiredZone(zoneID, zoneName)) {
            try {
                String response = transport.execute(
                        SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.ZONE)
                                .addFunction(FunctionKeys.CALL_SCENE).addParameter(ParameterKeys.TOKEN, token)
                                .addParameter(ParameterKeys.ID, convertIntegerToString(zoneID))
                                .addParameter(ParameterKeys.NAME, zoneName)
                                .addParameter(ParameterKeys.GROUP_ID, convertShortToString(groupID))
                                .addParameter(ParameterKeys.GROUP_NAME, groupName)
                                .addParameter(ParameterKeys.SCENENUMBER, sceneNumber.getSceneNumber().toString())
                                .addParameter(ParameterKeys.FORCE, force.toString()).buildRequestString());

                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public boolean undoZoneScene(String token, Integer zoneID, String zoneName, Short groupID, String groupName,
            SceneEnum sceneNumber) {
        if (checkRequiredZone(zoneID, zoneName)) {
            try {
                String response = transport.execute(
                        SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.ZONE)
                                .addFunction(FunctionKeys.UNDO_SCENE).addParameter(ParameterKeys.TOKEN, token)
                                .addParameter(ParameterKeys.ID, convertIntegerToString(zoneID))
                                .addParameter(ParameterKeys.NAME, zoneName)
                                .addParameter(ParameterKeys.GROUP_ID, convertShortToString(groupID))
                                .addParameter(ParameterKeys.GROUP_NAME, groupName)
                                .addParameter(ParameterKeys.SCENENUMBER, sceneNumber.getSceneNumber().toString())
                                .buildRequestString());
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public boolean turnDeviceOn(String token, DSID dsid, String dSUID, String name) {
        if (checkRequiredDevice(dsid, dSUID, name)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.TURN_ON)
                        .addParameter(ParameterKeys.TOKEN, token).addParameter(ParameterKeys.DSID, getDsidString(dsid))
                        .addParameter(ParameterKeys.NAME, name).addParameter(ParameterKeys.DSUID, dSUID)
                        .buildRequestString());
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public boolean turnDeviceOff(String token, DSID dsid, String dSUID, String name) {
        if (checkRequiredDevice(dsid, dSUID, name)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.TURN_OFF)
                        .addParameter(ParameterKeys.TOKEN, token).addParameter(ParameterKeys.DSID, getDsidString(dsid))
                        .addParameter(ParameterKeys.DSUID, dSUID).addParameter(ParameterKeys.NAME, name)
                        .buildRequestString());

                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }

        }
        return false;
    }

    @Override
    public DeviceConfig getDeviceConfig(String token, DSID dSID, String dSUID, String name,
            DeviceParameterClassEnum class_, Integer index) {
        if (checkRequiredDevice(dSID, dSUID, name) && class_ != null && convertIntegerToString(index) != null) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.GET_CONFIG)
                        .addParameter(ParameterKeys.TOKEN, token).addParameter(ParameterKeys.DSID, getDsidString(dSID))
                        .addParameter(ParameterKeys.DSUID, dSUID).addParameter(ParameterKeys.NAME, name)
                        .addParameter(ParameterKeys.CLASS, class_.getClassIndex().toString())
                        .addParameter(ParameterKeys.INDEX, convertIntegerToString(index)).buildRequestString());

                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject configObject = JSONResponseHandler.getResultJsonObject(responseObj);

                    if (configObject != null) {
                        return new JSONDeviceConfigImpl(configObject);
                    }
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return null;
    }

    @Override
    public int getDeviceOutputValue(String token, DSID dSID, String dSUID, String name, Short offset) {
        if (checkRequiredDevice(dSID, dSUID, name) && convertShortToString(offset) != null) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.GET_OUTPUT_VALUE)
                        .addParameter(ParameterKeys.TOKEN, token).addParameter(ParameterKeys.DSID, getDsidString(dSID))
                        .addParameter(ParameterKeys.DSUID, dSUID).addParameter(ParameterKeys.NAME, name)
                        .addParameter(ParameterKeys.OFFSET, convertShortToString(offset)).buildRequestString());

                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject valueObject = JSONResponseHandler.getResultJsonObject(responseObj);

                    if (checkBlankField(valueObject, JSONApiResponseKeysEnum.VALUE.getKey())) {
                        return valueObject.get(JSONApiResponseKeysEnum.VALUE.getKey()).getAsInt();
                    }
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return -1;
    }

    @Override
    public boolean setDeviceOutputValue(String token, DSID dSID, String dSUID, String name, Short offset,
            Integer value) {
        if (checkRequiredDevice(dSID, dSUID, name) && convertShortToString(offset) != null
                && convertIntegerToString(value) != null) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.SET_OUTPUT_VALUE)
                        .addParameter(ParameterKeys.TOKEN, token).addParameter(ParameterKeys.DSID, getDsidString(dSID))
                        .addParameter(ParameterKeys.DSUID, dSUID).addParameter(ParameterKeys.NAME, name)
                        .addParameter(ParameterKeys.OFFSET, convertShortToString(offset))
                        .addParameter(ParameterKeys.VALUE, convertIntegerToString(value)).buildRequestString());
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public DeviceSceneSpec getDeviceSceneMode(String token, DSID dSID, String dSUID, String name, Short sceneID) {
        if (checkRequiredDevice(dSID, dSUID, name) && convertShortToString(sceneID) != null) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.GET_SCENE_MODE)
                        .addParameter(ParameterKeys.TOKEN, token).addParameter(ParameterKeys.DSID, getDsidString(dSID))
                        .addParameter(ParameterKeys.DSUID, dSUID).addParameter(ParameterKeys.NAME, name)
                        .addParameter(ParameterKeys.SCENE_ID, convertShortToString(sceneID)).buildRequestString());
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject sceneSpec = JSONResponseHandler.getResultJsonObject(responseObj);

                    if (sceneSpec != null) {
                        return new JSONDeviceSceneSpecImpl(sceneSpec);
                    }
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return null;
    }

    @Override
    public short getDeviceSensorValue(String token, DSID dSID, String dSUID, String name, Short sensorIndex) {
        if (checkRequiredDevice(dSID, dSUID, dSUID) && sensorIndex != null) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.GET_SENSOR_VALUE)
                        .addParameter(ParameterKeys.TOKEN, token).addParameter(ParameterKeys.DSID, getDsidString(dSID))
                        .addParameter(ParameterKeys.DSUID, dSUID).addParameter(ParameterKeys.NAME, name)
                        .addParameter(ParameterKeys.SENSOR_INDEX, convertShortToString(sensorIndex))
                        .buildRequestString());
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject valueObject = JSONResponseHandler.getResultJsonObject(responseObj);

                    if (checkBlankField(valueObject, JSONApiResponseKeysEnum.SENSOR_VALUE.getKey())) {
                        return valueObject.get(JSONApiResponseKeysEnum.SENSOR_VALUE.getKey()).getAsShort();
                    }
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return -1;
    }

    @Override
    public boolean callDeviceScene(String token, DSID dSID, String dSUID, String name, Scene sceneNumber,
            Boolean force) {
        if (checkRequiredDevice(dSID, dSUID, name) && sceneNumber != null) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.CALL_SCENE)
                        .addParameter(ParameterKeys.TOKEN, token).addParameter(ParameterKeys.DSID, getDsidString(dSID))
                        .addParameter(ParameterKeys.DSUID, dSUID).addParameter(ParameterKeys.NAME, name)
                        .addParameter(ParameterKeys.SCENENUMBER, sceneNumber.getSceneNumber().toString())
                        .addParameter(ParameterKeys.FORCE, force.toString()).buildRequestString());
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public boolean undoDeviceScene(String token, DSID dSID, String dSUID, String name, Scene sceneNumber) {
        if (checkRequiredDevice(dSID, dSUID, name) && sceneNumber != null) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.UNDO_SCENE)
                        .addParameter(ParameterKeys.TOKEN, token).addParameter(ParameterKeys.DSID, getDsidString(dSID))
                        .addParameter(ParameterKeys.DSUID, dSUID).addParameter(ParameterKeys.NAME, name)
                        .addParameter(ParameterKeys.SCENENUMBER, sceneNumber.getSceneNumber().toString())
                        .buildRequestString());
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public boolean subscribeEvent(String token, String name, Integer subscriptionID, int connectionTimeout,
            int readTimeout) {
        if (StringUtils.isNotBlank(name) && convertIntegerToString(subscriptionID) != null) {
            String response;
            try {
                response = transport
                        .execute(
                                SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                                        .addRequestClass(ClassKeys.EVENT).addFunction(FunctionKeys.SUBSCRIBE)
                                        .addParameter(ParameterKeys.TOKEN, token).addParameter(ParameterKeys.NAME, name)
                                        .addParameter(ParameterKeys.SUBSCRIPTIONID,
                                                convertIntegerToString(subscriptionID))
                                        .buildRequestString(),
                                connectionTimeout, readTimeout);
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public boolean unsubscribeEvent(String token, String name, Integer subscriptionID, int connectionTimeout,
            int readTimeout) {
        if (StringUtils.isNotBlank(name) && convertIntegerToString(subscriptionID) != null) {
            String response;
            try {
                response = transport
                        .execute(
                                SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                                        .addRequestClass(ClassKeys.EVENT).addFunction(FunctionKeys.UNSUBSCRIBE)
                                        .addParameter(ParameterKeys.TOKEN, token).addParameter(ParameterKeys.NAME, name)
                                        .addParameter(ParameterKeys.SUBSCRIPTIONID,
                                                convertIntegerToString(subscriptionID))
                                        .buildRequestString(),
                                connectionTimeout, readTimeout);
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public String getEvent(String token, Integer subscriptionID, Integer timeout) {
        if (convertIntegerToString(subscriptionID) != null) {
            try {
                return transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.EVENT).addFunction(FunctionKeys.GET)
                        .addParameter(ParameterKeys.TOKEN, token)
                        .addParameter(ParameterKeys.SUBSCRIPTIONID, convertIntegerToString(subscriptionID))
                        .addParameter(ParameterKeys.TIMEOUT, convertIntegerToString(timeout)).buildRequestString());
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return null;
    }

    @Override
    public int getTime(String token) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.SYSTEM).addFunction(FunctionKeys.TIME)
                    .addParameter(ParameterKeys.TOKEN, token).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);

                if (checkBlankField(obj, JSONApiResponseKeysEnum.TIME.getKey())) {
                    return obj.get(JSONApiResponseKeysEnum.TIME.getKey()).getAsInt();
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return -1;
    }

    @Override
    public List<Integer> getResolutions(String token) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.METERING).addFunction(FunctionKeys.GET_RESOLUTIONS)
                    .addParameter(ParameterKeys.TOKEN, token).buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject resObj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (resObj != null && resObj.get(JSONApiResponseKeysEnum.RESOLUTIONS.getKey()) instanceof JsonArray) {
                    JsonArray array = (JsonArray) resObj.get(JSONApiResponseKeysEnum.RESOLUTIONS.getKey());

                    List<Integer> resolutionList = new LinkedList<Integer>();
                    for (int i = 0; i < array.size(); i++) {
                        if (array.get(i) instanceof JsonObject) {
                            JsonObject jObject = (JsonObject) array.get(i);

                            if (jObject.get(JSONApiResponseKeysEnum.RESOLUTION.getKey()) != null) {
                                int val = jObject.get(JSONApiResponseKeysEnum.RESOLUTION.getKey()).getAsInt();
                                if (val != -1) {
                                    resolutionList.add(val);
                                }
                            }
                        }
                    }
                    return resolutionList;
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public List<CachedMeteringValue> getLatest(String token, MeteringTypeEnum type, List<String> meterDSIDs,
            MeteringUnitsEnum unit) {
        if (meterDSIDs != null) {
            String jsonMeterList = ".meters(";
            for (int i = 0; i < meterDSIDs.size(); i++) {
                if (!meterDSIDs.get(i).isEmpty()) {
                    jsonMeterList += meterDSIDs.get(i);
                    if (i < meterDSIDs.size() - 1 && !meterDSIDs.get(i + 1).isEmpty()) {
                        jsonMeterList += ",";
                    } else {
                        break;
                    }
                }
            }
            jsonMeterList += ")";
            return getLatest(token, type, jsonMeterList, unit);
        }
        return null;
    }

    @Override
    public List<CachedMeteringValue> getLatest(String token, MeteringTypeEnum type, String meterDSIDs,
            MeteringUnitsEnum unit) {
        if (type != null && meterDSIDs != null) {
            String unitstring = null;
            if (unit != null) {
                unitstring = unit.UNIT;
            }
            try {
                String response = transport.execute(
                        SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.METERING)
                                .addFunction(FunctionKeys.GET_LATEST).addParameter(ParameterKeys.TOKEN, token)
                                .addParameter(ParameterKeys.TYPE, objectToString(type).toLowerCase())
                                .addParameter(ParameterKeys.FROM, meterDSIDs)
                                .addParameter(ParameterKeys.UNIT, unitstring).buildRequestString());

                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject latestObj = JSONResponseHandler.getResultJsonObject(responseObj);
                    if (latestObj != null
                            && latestObj.get(JSONApiResponseKeysEnum.VALUES.getKey()) instanceof JsonArray) {
                        JsonArray array = (JsonArray) latestObj.get(JSONApiResponseKeysEnum.VALUES.getKey());

                        List<CachedMeteringValue> list = new LinkedList<CachedMeteringValue>();
                        for (int i = 0; i < array.size(); i++) {
                            if (array.get(i) instanceof JsonObject) {
                                list.add(new JSONCachedMeteringValueImpl((JsonObject) array.get(i), type, unit));
                            }
                        }
                        return list;
                    }
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return null;
    }

    @Override
    public boolean setDeviceValue(String token, DSID dSID, String dSUID, String name, Integer value) {
        if (checkRequiredDevice(dSID, dSUID, name)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.SET_VALUE)
                        .addParameter(ParameterKeys.TOKEN, token).addParameter(ParameterKeys.DSID, getDsidString(dSID))
                        .addParameter(ParameterKeys.DSUID, dSUID).addParameter(ParameterKeys.NAME, name)
                        .addParameter(ParameterKeys.VALUE, value.toString()).buildRequestString());
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;

    }

    @Override
    public List<String> getMeterList(String token) {
        List<String> meterList = new LinkedList<String>();
        JsonObject responseObj = query(token, QUERY_GET_METERLIST);
        if (responseObj != null && responseObj.get(JSONApiResponseKeysEnum.DS_METERS.getKey()).isJsonArray()) {
            JsonArray array = responseObj.get(JSONApiResponseKeysEnum.DS_METERS.getKey()).getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i) instanceof JsonObject) {
                    meterList.add(array.get(i).getAsJsonObject().get("dSID").getAsString());
                }
            }
        }
        return meterList;
    }

    @Override
    public String loginApplication(String loginToken) {
        if (StringUtils.isNotBlank(loginToken)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.SYSTEM).addFunction(FunctionKeys.LOGIN_APPLICATION)
                        .addParameter(ParameterKeys.LOGIN_TOKEN, loginToken).buildRequestString());
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                    String tokenStr = null;

                    if (checkBlankField(obj, JSONApiResponseKeysEnum.TOKEN.getKey())) {
                        tokenStr = obj.get(JSONApiResponseKeysEnum.TOKEN.getKey()).getAsString();
                    }
                    if (tokenStr != null) {
                        return tokenStr;
                    }
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }

        }
        return null;
    }

    @Override
    public String login(String user, String password) {
        try {
            String response = transport
                    .execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.SYSTEM)
                            .addFunction(FunctionKeys.LOGIN).addParameter(ParameterKeys.USER, user)
                            .addParameter(ParameterKeys.PASSWORD, password).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                String tokenStr = null;

                if (checkBlankField(obj, JSONApiResponseKeysEnum.TOKEN.getKey())) {
                    tokenStr = obj.get(JSONApiResponseKeysEnum.TOKEN.getKey()).getAsString();
                }
                if (tokenStr != null) {
                    return tokenStr;
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public boolean logout() {
        String response;
        try {
            response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.SYSTEM).addFunction(FunctionKeys.LOGOUT).buildRequestString());
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return false;
    }

    @Override
    public Map<String, String> getDSID(String token) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.SYSTEM).addFunction(FunctionKeys.GET_DSID)
                    .addParameter(ParameterKeys.TOKEN, token).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (obj != null) {
                    Map<String, String> dsidMap = new HashMap<String, String>(obj.entrySet().size());
                    for (Entry<String, JsonElement> entry : obj.entrySet()) {
                        dsidMap.put(entry.getKey(), entry.getValue().getAsString());
                    }
                    return dsidMap;
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public boolean enableApplicationToken(String applicationToken, String sessionToken) {
        try {
            String response = transport.execute(
                    SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.SYSTEM)
                            .addFunction(FunctionKeys.ENABLE_APPLICATION_TOKEN)
                            .addParameter(ParameterKeys.TOKEN, sessionToken)
                            .addParameter(ParameterKeys.APPLICATION_TOKEN, applicationToken).buildRequestString(),
                    60000, 60000);
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return false;
    }

    @Override
    public String requestAppplicationToken(String applicationName) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.SYSTEM).addFunction(FunctionKeys.REQUEST_APPLICATION_TOKEN)
                    .addParameter(ParameterKeys.APPLICATION_NAME, applicationName).buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (obj != null) {
                    return obj.get(JSONApiResponseKeysEnum.APPLICATION_TOKEN.getKey()).getAsString();
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public boolean revokeToken(String applicationToken, String sessionToken) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.SYSTEM).addFunction(FunctionKeys.REVOKE_TOKEN)
                    .addParameter(ParameterKeys.APPLICATION_TOKEN, applicationToken)
                    .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return false;
    }

    @Override
    public int checkConnection(String token) {
        try {
            return transport.checkConnection(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.APARTMENT).addFunction(FunctionKeys.GET_NAME)
                    .addParameter(ParameterKeys.TOKEN, token).buildRequestString());
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return -1;
    }

    @Override
    public int[] getSceneValue(String token, DSID dSID, String dSUID, String name, Short sceneId) {
        int[] value = { -1, -1 };
        if (checkRequiredDevice(dSID, dSUID, name)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.GET_SCENE_VALUE)
                        .addParameter(ParameterKeys.SCENE_ID, convertShortToString(sceneId))
                        .addParameter(ParameterKeys.DSID, getDsidString(dSID)).addParameter(ParameterKeys.DSUID, dSUID)
                        .addParameter(ParameterKeys.NAME, name).addParameter(ParameterKeys.TOKEN, token)
                        .buildRequestString(), 4000, 20000);
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                    if (obj != null && obj.get("value") != null) {
                        value[0] = obj.get("value").getAsInt();
                        if (obj.get("angle") != null) {
                            value[1] = obj.get("angle").getAsInt();
                        }
                        return value;
                    }
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return value;
    }

    @Override
    public boolean increaseValue(String sessionToken, DSID dSID, String dSUID, String name) {
        if (checkRequiredDevice(dSID, dSUID, name)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.INCREASE_VALUE)
                        .addParameter(ParameterKeys.DSID, getDsidString(dSID)).addParameter(ParameterKeys.DSUID, dSUID)
                        .addParameter(ParameterKeys.NAME, name).addParameter(ParameterKeys.TOKEN, sessionToken)
                        .buildRequestString());
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public boolean decreaseValue(String sessionToken, DSID dSID, String dSUID, String name) {
        if (checkRequiredDevice(dSID, dSUID, name)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.DEVICE).addFunction(FunctionKeys.DECREASE_VALUE)
                        .addParameter(ParameterKeys.DSID, getDsidString(dSID)).addParameter(ParameterKeys.DSUID, dSUID)
                        .addParameter(ParameterKeys.NAME, name).addParameter(ParameterKeys.TOKEN, sessionToken)
                        .buildRequestString());
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public String getInstallationName(String sessionToken) {
        String response = null;
        try {
            response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.APARTMENT).addFunction(FunctionKeys.GET_NAME)
                    .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());
        } catch (Exception e) {
            logger.debug("An exception occurred", e);

        }
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (checkBlankField(obj, JSONApiResponseKeysEnum.NAME.getKey())) {
                return obj.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
            }
        }
        return null;
    }

    @Override
    public String getZoneName(String sessionToken, Integer zoneID) {
        if (checkRequiredZone(zoneID, null)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.GET_NAME)
                        .addParameter(ParameterKeys.ID, convertIntegerToString(zoneID))
                        .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                    if (checkBlankField(obj, JSONApiResponseKeysEnum.NAME.getKey())) {
                        return obj.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
                    }
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return null;
    }

    @Override
    public String getDeviceName(String sessionToken, DSID dSID, String dSUID) {
        if (checkRequiredDevice(dSID, dSUID, null)) {
            try {
                String response = transport.execute(
                        SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.DEVICE)
                                .addFunction(FunctionKeys.GET_NAME).addParameter(ParameterKeys.TOKEN, sessionToken)
                                .addParameter(ParameterKeys.DSID, getDsidString(dSID))
                                .addParameter(ParameterKeys.DSUID, dSUID).buildRequestString());
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                    if (checkBlankField(obj, JSONApiResponseKeysEnum.NAME.getKey())) {
                        return obj.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
                    }
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return null;
    }

    @Override
    public String getCircuitName(String sessionToken, DSID dSID) {
        try {
            String response = transport
                    .execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.CIRCUIT)
                            .addFunction(FunctionKeys.GET_NAME).addParameter(ParameterKeys.DSID, getDsidString(dSID))
                            .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (checkBlankField(obj, JSONApiResponseKeysEnum.NAME.getKey())) {
                    return obj.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public String getSceneName(String sessionToken, Integer zoneID, String zoneName, Short groupID, Short sceneID) {
        if (checkRequiredZone(zoneID, zoneName)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.SCENE_GET_NAME)
                        .addParameter(ParameterKeys.TOKEN, sessionToken)
                        .addParameter(ParameterKeys.ID, convertIntegerToString(zoneID))
                        .addParameter(ParameterKeys.NAME, zoneName)
                        .addParameter(ParameterKeys.GROUP_ID, convertShortToString(groupID))
                        .addParameter(ParameterKeys.SCENENUMBER, convertShortToString(sceneID)).buildRequestString());
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                    if (checkBlankField(obj, JSONApiResponseKeysEnum.NAME.getKey())) {
                        return obj.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
                    }
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return null;
    }

    @Override
    public TemperatureControlStatus getZoneTemperatureControlStatus(String sessionToken, Integer zoneID,
            String zoneName) {
        if (checkRequiredZone(zoneID, zoneName)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.GET_TEMPERATURE_CONTROL_STATUS)
                        .addParameter(ParameterKeys.TOKEN, sessionToken)
                        .addParameter(ParameterKeys.ID, convertIntegerToString(zoneID))
                        .addParameter(ParameterKeys.NAME, zoneName).buildRequestString());
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                    return new TemperatureControlStatus(obj, zoneID, zoneName);
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return null;
    }

    @Override
    public TemperatureControlConfig getZoneTemperatureControlConfig(String sessionToken, Integer zoneID,
            String zoneName) {
        if (checkRequiredZone(zoneID, zoneName)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.GET_TEMPERATURE_CONTROL_CONFIG)
                        .addParameter(ParameterKeys.TOKEN, sessionToken)
                        .addParameter(ParameterKeys.ID, convertIntegerToString(zoneID))
                        .addParameter(ParameterKeys.NAME, zoneName).buildRequestString());
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                    return new TemperatureControlConfig(obj, zoneID, zoneName);
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return null;
    }

    @Override
    public TemperatureControlValues getZoneTemperatureControlValues(String sessionToken, Integer zoneID,
            String zoneName) {
        if (checkRequiredZone(zoneID, zoneName)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.GET_TEMPERATURE_CONTROL_VALUES)
                        .addParameter(ParameterKeys.TOKEN, sessionToken)
                        .addParameter(ParameterKeys.ID, convertIntegerToString(zoneID))
                        .addParameter(ParameterKeys.NAME, zoneName).buildRequestString());
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                    return new TemperatureControlValues(obj, zoneID, zoneName);
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return null;
    }

    @Override
    public AssignedSensors getZoneAssignedSensors(String sessionToken, Integer zoneID, String zoneName) {
        if (checkRequiredZone(zoneID, zoneName)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.GET_ASSIGNED_SENSORS)
                        .addParameter(ParameterKeys.TOKEN, sessionToken)
                        .addParameter(ParameterKeys.ID, convertIntegerToString(zoneID))
                        .addParameter(ParameterKeys.NAME, zoneName).buildRequestString());
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                    return new AssignedSensors(obj, zoneID, zoneName);
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return null;
    }

    @Override
    public boolean setZoneTemperatureControlState(String sessionToken, Integer zoneID, String controlState,
            String zoneName) {
        if (checkRequiredZone(zoneID, zoneName)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.SET_TEMEPERATURE_CONTROL_STATE)
                        .addParameter(ParameterKeys.TOKEN, sessionToken)
                        .addParameter(ParameterKeys.ID, convertIntegerToString(zoneID))
                        .addParameter(ParameterKeys.NAME, zoneName)
                        .addParameter(ParameterKeys.CONTROL_STATE, controlState).buildRequestString());

                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));

            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public boolean setZoneTemperatureControlValues(String sessionToken, Integer zoneID, String zoneName,
            List<Object[]> controlValues) {
        if (checkRequiredZone(zoneID, zoneName)) {
            try {
                if (checkRequiredZone(zoneID, zoneName)) {
                    if (controlValues != null) {
                        SimpleRequestBuilder builder = SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                                .addRequestClass(ClassKeys.ZONE)
                                .addFunction(FunctionKeys.SET_TEMEPERATURE_CONTROL_VALUE)
                                .addParameter(ParameterKeys.TOKEN, sessionToken)
                                .addParameter(ParameterKeys.ID, convertIntegerToString(zoneID))
                                .addParameter(ParameterKeys.NAME, zoneName);
                        for (Object[] objAry : controlValues) {
                            if (objAry.length == 2 && objAry[0] instanceof String && objAry[1] instanceof Integer) {
                                builder.addParameter((String) objAry[0], convertIntegerToString((Integer) objAry[1]));
                            } else {
                                builder.buildRequestString();
                                throw new IllegalArgumentException(
                                        "The first field of the object array have to be a String and the second have to be a Integer.");
                            }
                        }
                        String response = transport.execute(builder.buildRequestString());

                        return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
                    }
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public SensorValues getZoneSensorValues(String sessionToken, Integer zoneID, String zoneName) {
        if (checkRequiredZone(zoneID, zoneName)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.GET_SENSOR_VALUES)
                        .addParameter(ParameterKeys.TOKEN, sessionToken)
                        .addParameter(ParameterKeys.ID, convertIntegerToString(zoneID))
                        .addParameter(ParameterKeys.NAME, zoneName).buildRequestString());
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                    return new SensorValues(obj, zoneID, zoneName);
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return null;
    }

    @Override
    public boolean setZoneTemperatureControlConfig(String sessionToken, Integer zoneID, String zoneName,
            String controlDSUID, Short controlMode, Integer referenceZone, Float ctrlOffset, Float emergencyValue,
            Float manualValue, Float ctrlKp, Float ctrlTs, Float ctrlTi, Float ctrlKd, Float ctrlImin, Float ctrlImax,
            Float ctrlYmin, Float ctrlYmax, Boolean ctrlAntiWindUp, Boolean ctrlKeepFloorWarm) {
        if (checkRequiredZone(zoneID, zoneName)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.SET_TEMPERATION_CONTROL_CONFIG)
                        .addParameter(ParameterKeys.TOKEN, sessionToken)
                        .addParameter(ParameterKeys.ID, convertIntegerToString(zoneID))
                        .addParameter(ParameterKeys.NAME, zoneName)
                        .addParameter(ParameterKeys.CONTROL_MODE, objectToString(controlMode))
                        .addParameter(ParameterKeys.CONTROL_DSUID, controlDSUID)
                        .addParameter(ParameterKeys.REFERENCE_ZONE, objectToString(referenceZone))
                        .addParameter(ParameterKeys.CTRL_OFFSET, objectToString(ctrlOffset))
                        .addParameter(ParameterKeys.EMERGENCY_VALUE, objectToString(emergencyValue))
                        .addParameter(ParameterKeys.MANUAL_VALUE, objectToString(manualValue))
                        .addParameter(ParameterKeys.CTRL_KP, objectToString(ctrlKp))
                        .addParameter(ParameterKeys.CTRL_TS, objectToString(ctrlTs))
                        .addParameter(ParameterKeys.CTRL_TI, objectToString(ctrlTi))
                        .addParameter(ParameterKeys.CTRL_KD, objectToString(ctrlKd))
                        .addParameter(ParameterKeys.CTRL_I_MIN, objectToString(ctrlImin))
                        .addParameter(ParameterKeys.CTRL_I_MAX, objectToString(ctrlImax))
                        .addParameter(ParameterKeys.CTRL_Y_MIN, objectToString(ctrlYmin))
                        .addParameter(ParameterKeys.CTRL_Y_MAX, objectToString(ctrlYmax))
                        .addParameter(ParameterKeys.CTRL_ANTI_WIND_UP, objectToString(ctrlAntiWindUp))
                        .addParameter(ParameterKeys.CTRL_KEEP_FLOOR_WARM, objectToString(ctrlKeepFloorWarm))
                        .buildRequestString());

                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));

            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public boolean setZoneSensorSource(String sessionToken, Integer zoneID, String zoneName, SensorEnum sensorType,
            DSID dSID) {
        if (checkRequiredZone(zoneID, zoneName)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.SET_SENSOR_SOURCE)
                        .addParameter(ParameterKeys.TOKEN, sessionToken)
                        .addParameter(ParameterKeys.ID, convertIntegerToString(zoneID))
                        .addParameter(ParameterKeys.NAME, zoneName)
                        .addParameter(ParameterKeys.SENSOR_TYPE, sensorType.getSensorType().toString())
                        .addParameter(ParameterKeys.DSID, getDsidString(dSID)).buildRequestString());

                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));

            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public boolean clearZoneSensorSource(String sessionToken, Integer zoneID, String zoneName, SensorEnum sensorType) {
        if (checkRequiredZone(zoneID, zoneName)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.SET_TEMEPERATURE_CONTROL_VALUE)
                        .addParameter(ParameterKeys.TOKEN, sessionToken)
                        .addParameter(ParameterKeys.ID, convertIntegerToString(zoneID))
                        .addParameter(ParameterKeys.NAME, zoneName)
                        .addParameter(ParameterKeys.SENSOR_TYPE, sensorType.getSensorType().toString())
                        .buildRequestString());

                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));

            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public TemperatureControlInternals getZoneTemperatureControlInternals(String sessionToken, Integer zoneID,
            String zoneName) {
        if (checkRequiredZone(zoneID, zoneName)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.GET_TEMPERATURE_CONTROL_INTERNALS)
                        .addParameter(ParameterKeys.TOKEN, sessionToken).addParameter(ParameterKeys.NAME, zoneName)
                        .addParameter(ParameterKeys.ID, convertIntegerToString(zoneID)).buildRequestString());
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                    return new TemperatureControlInternals(obj, zoneID, zoneName);
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return null;
    }

    @Override
    public boolean setZoneOutputValue(String sessionToken, Integer zoneID, String zoneName, Short groupID,
            String groupName, Integer value) {
        if (value != null && checkRequiredZone(zoneID, zoneName)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.SET_OUTPUT_VALUE)
                        .addParameter(ParameterKeys.TOKEN, sessionToken).addParameter(ParameterKeys.NAME, zoneName)
                        .addParameter(ParameterKeys.ID, convertIntegerToString(zoneID))
                        .addParameter(ParameterKeys.GROUP_ID, convertShortToString(groupID))
                        .addParameter(ParameterKeys.GROUP_NAME, groupName)
                        .addParameter(ParameterKeys.VALUE, convertIntegerToString(value)).buildRequestString());
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    return true;
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public boolean zoneBlink(String sessionToken, Integer zoneID, String zoneName, Short groupID, String groupName) {
        if (checkRequiredZone(zoneID, zoneName)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.BLINK)
                        .addParameter(ParameterKeys.TOKEN, sessionToken).addParameter(ParameterKeys.NAME, zoneName)
                        .addParameter(ParameterKeys.ID, convertIntegerToString(zoneID))
                        .addParameter(ParameterKeys.GROUP_ID, convertShortToString(groupID))
                        .addParameter(ParameterKeys.GROUP_NAME, groupName).buildRequestString());
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    return true;
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public boolean pushZoneSensorValue(String sessionToken, Integer zoneID, String zoneName, Short groupID,
            String sourceDSUID, Float sensorValue, SensorEnum sensorType) {
        if (checkRequiredZone(zoneID, zoneName) && sensorType != null && sensorValue != null) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                        .addRequestClass(ClassKeys.ZONE).addFunction(FunctionKeys.PUSH_SENSOR_VALUE)
                        .addParameter(ParameterKeys.TOKEN, sessionToken).addParameter(ParameterKeys.NAME, zoneName)
                        .addParameter(ParameterKeys.ID, convertIntegerToString(zoneID))
                        .addParameter(ParameterKeys.GROUP_ID, convertShortToString(groupID))
                        .addParameter(ParameterKeys.SOURCE_DSUID, sourceDSUID)
                        .addParameter(ParameterKeys.SENSOR_VALUE, convertFloatToString(sensorValue))
                        .addParameter(ParameterKeys.SENSOR_TYPE, convertShortToString(sensorType.getSensorType()))
                        .buildRequestString());
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public List<TemperatureControlStatus> getApartmentTemperatureControlStatus(String sessionToken) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.APARTMENT).addFunction(FunctionKeys.GET_TEMPERATURE_CONTROL_STATUS)
                    .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).isJsonArray()) {
                    JsonArray jArray = obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).getAsJsonArray();
                    if (jArray.size() != 0) {
                        List<TemperatureControlStatus> list = new ArrayList<TemperatureControlStatus>(jArray.size());
                        Iterator<JsonElement> iter = jArray.iterator();
                        while (iter.hasNext()) {
                            TemperatureControlStatus tContStat = new TemperatureControlStatus(
                                    iter.next().getAsJsonObject());
                            if (tContStat != null) {
                                list.add(tContStat);
                            }
                        }
                        return list;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public HashMap<Integer, TemperatureControlConfig> getApartmentTemperatureControlConfig(String sessionToken) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.APARTMENT).addFunction(FunctionKeys.GET_TEMPERATURE_CONTROL_CONFIG)
                    .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).isJsonArray()) {
                    JsonArray jArray = obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).getAsJsonArray();
                    if (jArray.size() != 0) {
                        HashMap<Integer, TemperatureControlConfig> map = new HashMap<Integer, TemperatureControlConfig>(
                                jArray.size());
                        Iterator<JsonElement> iter = jArray.iterator();
                        while (iter.hasNext()) {
                            TemperatureControlConfig tContConf = new TemperatureControlConfig(
                                    iter.next().getAsJsonObject());
                            if (tContConf != null) {
                                map.put(tContConf.getZoneID(), tContConf);
                            }
                        }
                        return map;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public HashMap<Integer, TemperatureControlValues> getApartmentTemperatureControlValues(String sessionToken) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.APARTMENT).addFunction(FunctionKeys.GET_TEMPERATURE_CONTROL_VALUES)
                    .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).isJsonArray()) {
                    JsonArray jArray = obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).getAsJsonArray();
                    if (jArray.size() != 0) {
                        HashMap<Integer, TemperatureControlValues> map = new HashMap<Integer, TemperatureControlValues>(
                                jArray.size());
                        Iterator<JsonElement> iter = jArray.iterator();
                        while (iter.hasNext()) {
                            TemperatureControlValues tContVal = new TemperatureControlValues(
                                    iter.next().getAsJsonObject());
                            if (tContVal != null) {
                                map.put(tContVal.getZoneID(), tContVal);
                            }
                        }
                        return map;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public HashMap<Integer, AssignedSensors> getApartmentAssignedSensors(String sessionToken) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.APARTMENT).addFunction(FunctionKeys.GET_ASSIGNED_SENSORS)
                    .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).isJsonArray()) {
                    JsonArray jArray = obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).getAsJsonArray();
                    if (jArray.size() != 0) {
                        HashMap<Integer, AssignedSensors> map = new HashMap<Integer, AssignedSensors>(jArray.size());
                        Iterator<JsonElement> iter = jArray.iterator();
                        while (iter.hasNext()) {
                            AssignedSensors assignedSensors = new AssignedSensors(iter.next().getAsJsonObject());
                            if (assignedSensors != null) {
                                map.put(assignedSensors.getZoneID(), assignedSensors);
                            }
                        }
                        return map;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public HashMap<Integer, BaseSensorValues> getApartmentSensorValues(String sessionToken) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.APARTMENT).addFunction(FunctionKeys.GET_SENSOR_VALUES)
                    .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).isJsonArray()) {
                    JsonArray jArray = obj.get(JSONApiResponseKeysEnum.ZONES.getKey()).getAsJsonArray();
                    WeatherSensorData weather = new WeatherSensorData(obj);
                    if (jArray.size() != 0) {
                        HashMap<Integer, BaseSensorValues> map = new HashMap<Integer, BaseSensorValues>(
                                jArray.size() + 1);
                        Iterator<JsonElement> iter = jArray.iterator();
                        while (iter.hasNext()) {
                            SensorValues sensorValues = new SensorValues(iter.next().getAsJsonObject());
                            if (sensorValues != null) {
                                map.put(sensorValues.getZoneID(), sensorValues);
                            }
                        }
                        map.put(0, weather);
                        return map;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public JsonObject query(String sessionToken, String query) {
        try {
            String response = transport.execute(
                    SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.PROPERTY_TREE)
                            .addFunction(FunctionKeys.QUERY).addParameter(ParameterKeys.QUERY, query)
                            .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
            if (JSONResponseHandler.checkResponse(responseObj)) {
                return JSONResponseHandler.getResultJsonObject(responseObj);
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public JsonObject query2(String sessionToken, String query) {
        try {
            String response = transport.execute(
                    SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.PROPERTY_TREE)
                            .addFunction(FunctionKeys.QUERY2).addParameter(ParameterKeys.QUERY, query)
                            .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
            if (JSONResponseHandler.checkResponse(responseObj)) {
                return JSONResponseHandler.getResultJsonObject(responseObj);
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public String propertyTreeGetString(String sessionToken, String path) {
        try {
            String response = transport.execute(
                    SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.PROPERTY_TREE)
                            .addFunction(FunctionKeys.GET_STRING).addParameter(ParameterKeys.PATH, path)
                            .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
            if (JSONResponseHandler.checkResponse(responseObj)) {
                responseObj = JSONResponseHandler.getResultJsonObject(responseObj);
                return responseObj.get(JSONApiResponseKeysEnum.VALUE.getKey()).getAsString();
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public Boolean propertyTreeSetString(String token, String path, String value) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.PROPERTY_TREE).addFunction(FunctionKeys.SET_STRING)
                    .addParameter(ParameterKeys.PATH, path).addParameter(ParameterKeys.TOKEN, token)
                    .addParameter(ParameterKeys.VALUE, value).buildRequestString());

            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public JsonArray propertyTreeGetChildren(String sessionToken, String path) {
        try {
            String response = transport.execute(
                    SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.PROPERTY_TREE)
                            .addFunction(FunctionKeys.GET_CHILDREN).addParameter(ParameterKeys.PATH, path)
                            .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
            if (JSONResponseHandler.checkResponse(responseObj)) {
                return responseObj.get(JSONApiResponseKeysEnum.RESULT.getKey()).getAsJsonArray();
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public Integer propertyTreeGetInteger(String sessionToken, String path) {
        try {
            String response = transport.execute(
                    SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.PROPERTY_TREE)
                            .addFunction(FunctionKeys.GET_INTEGER).addParameter(ParameterKeys.PATH, path)
                            .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
            if (JSONResponseHandler.checkResponse(responseObj)) {
                responseObj = JSONResponseHandler.getResultJsonObject(responseObj);
                return responseObj.get(JSONApiResponseKeysEnum.VALUE.getKey()).getAsInt();
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public Boolean propertyTreeSetInteger(String sessionToken, String path, Integer value) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.PROPERTY_TREE).addFunction(FunctionKeys.SET_INTEGER)
                    .addParameter(ParameterKeys.PATH, path).addParameter(ParameterKeys.TOKEN, sessionToken)
                    .addParameter(ParameterKeys.VALUE, convertIntegerToString(value)).buildRequestString());

            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public Boolean propertyTreeGetBoolean(String sessionToken, String path) {
        try {
            String response = transport.execute(
                    SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.PROPERTY_TREE)
                            .addFunction(FunctionKeys.GET_BOOLEAN).addParameter(ParameterKeys.PATH, path)
                            .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
            if (JSONResponseHandler.checkResponse(responseObj)) {
                responseObj = JSONResponseHandler.getResultJsonObject(responseObj);
                return responseObj.get(JSONApiResponseKeysEnum.VALUE.getKey()).getAsBoolean();
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public Boolean propertyTreeSetBoolean(String sessionToken, String path, Boolean value) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.PROPERTY_TREE).addFunction(FunctionKeys.SET_BOOLEAN)
                    .addParameter(ParameterKeys.PATH, path).addParameter(ParameterKeys.TOKEN, sessionToken)
                    .addParameter(ParameterKeys.VALUE, objectToString(value)).buildRequestString());

            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public String propertyTreeGetType(String sessionToken, String path) {
        try {
            String response = transport.execute(
                    SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.PROPERTY_TREE)
                            .addFunction(FunctionKeys.GET_TYPE).addParameter(ParameterKeys.PATH, path)
                            .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
            if (JSONResponseHandler.checkResponse(responseObj)) {
                responseObj = JSONResponseHandler.getResultJsonObject(responseObj);
                return responseObj.get(JSONApiResponseKeysEnum.VALUE.getKey()).getAsString();
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public Map<String, Boolean> propertyTreeGetFlages(String sessionToken, String path) {
        try {
            String response = transport.execute(
                    SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.PROPERTY_TREE)
                            .addFunction(FunctionKeys.GET_FLAGS).addParameter(ParameterKeys.PATH, path)
                            .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
            if (JSONResponseHandler.checkResponse(responseObj)) {
                responseObj = JSONResponseHandler.getResultJsonObject(responseObj);
                Set<Entry<String, JsonElement>> flagEntries = responseObj.entrySet();
                Map<String, Boolean> flags = new HashMap<String, Boolean>(flagEntries.size());
                for (Entry<String, JsonElement> flag : flagEntries) {
                    flags.put(flag.getKey(), flag.getValue().getAsBoolean());
                }
                return flags;
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public Boolean propertyTreeSetFlag(String sessionToken, String path, String flag, Boolean value) {
        try {
            String response = transport.execute(
                    SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.PROPERTY_TREE)
                            .addFunction(FunctionKeys.SET_FLAG).addParameter(ParameterKeys.PATH, path)
                            .addParameter(ParameterKeys.TOKEN, sessionToken).addParameter(ParameterKeys.FLAG, flag)
                            .addParameter(ParameterKeys.VALUE, objectToString(value)).buildRequestString());

            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public Boolean propertyTreeRemove(String sessionToken, String path) {
        try {
            String response = transport.execute(
                    SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.PROPERTY_TREE)
                            .addFunction(FunctionKeys.REMOVE).addParameter(ParameterKeys.PATH, path)
                            .addParameter(ParameterKeys.TOKEN, sessionToken).buildRequestString());

            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public Map<String, String> getSystemVersion() {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON)
                    .addRequestClass(ClassKeys.SYSTEM).addFunction(FunctionKeys.VERSION).buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
            if (JSONResponseHandler.checkResponse(responseObj)) {
                Set<Entry<String, JsonElement>> entries = JSONResponseHandler.getResultJsonObject(responseObj)
                        .entrySet();
                Map<String, String> versions = new HashMap<String, String>(entries.size());
                for (Entry<String, JsonElement> entry : entries) {
                    versions.put(entry.getKey(), entry.getValue().getAsString());
                }
                return versions;
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }
}