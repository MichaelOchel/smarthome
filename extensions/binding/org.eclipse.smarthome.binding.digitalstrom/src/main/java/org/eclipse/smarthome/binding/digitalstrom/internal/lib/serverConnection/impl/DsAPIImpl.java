/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.DsAPI;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.HttpTransport;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONRequestConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleURLBuilder.SimpleRequestBuilder;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleURLBuilder.constants.Classes;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleURLBuilder.constants.Functions;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleURLBuilder.constants.Interfaces;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleURLBuilder.constants.ParameterTyps;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.Apartment;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.CachedMeteringValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DSID;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceConfig;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceParameterClassEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceSceneSpec;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.JSONCachedMeteringValueImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.JSONDeviceConfigImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.JSONDeviceSceneSpecImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.MeteringUnitsEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.SensorEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.SensorIndexEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.impl.JSONDeviceImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.impl.JSONApartmentImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.Scene;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.SceneEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link DsAPIImpl} is the implementation of the {@link DsAPI}.
 *
 * @author Alexander Betker
 * @author Alex Maier
 * @author Michael Ochel - implements new methods, API updates and change SimpleJSON to GSON and requests building with
 *         constants to SimpleRequestBuilder
 * @author Matthias Siegele - implements new methods, API updates and change SimpleJSON to GSON and requests building
 *         with constants to SimpleRequestBuilder
 */
public class DsAPIImpl implements DsAPI {

    private Logger logger = LoggerFactory.getLogger(DsAPIImpl.class);
    private HttpTransport transport = null;

    public DsAPIImpl(HttpTransport transport) {
        this.transport = transport;
    }

    public DsAPIImpl(String uri, int connectTimeout, int readTimeout) {
        this.transport = new HttpTransportImpl(uri, connectTimeout, readTimeout);
    }

    public DsAPIImpl(String uri, int connectTimeout, int readTimeout, boolean aceptAllCerts) {
        this.transport = new HttpTransportImpl(uri, connectTimeout, readTimeout, aceptAllCerts);
    }

    private String getParameterGroupIdString(Short groupID) {
        return groupID != null && groupID > -1 ? groupID.toString() : null;
    }

    @Override
    public boolean callApartmentScene(String token, Short groupID, String groupName, Scene sceneNumber, Boolean force) {
        if (sceneNumber != null && isValidApartmentSceneNumber(sceneNumber.getSceneNumber())) {
            try {
                String response = transport.execute(
                        SimpleRequestBuilder.buildNewRequest(Interfaces.JSON).addRequestClass(Classes.APARTMENT)
                                .addFunction(Functions.CALL_SCENE).addParameter(ParameterTyps.TOKEN, token)
                                .addParameter(ParameterTyps.GROUP_ID, getParameterGroupIdString(groupID))
                                .addParameter(ParameterTyps.GROUP_NAME, groupName)
                                .addParameter(ParameterTyps.SCENENUMBER, sceneNumber.toString())
                                .addParameter(ParameterTyps.FORCE, force.toString()).buildRequestString());
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
                        SimpleRequestBuilder.buildNewRequest(Interfaces.JSON).addRequestClass(Classes.APARTMENT)
                                .addFunction(Functions.UNDO_SCENE).addParameter(ParameterTyps.TOKEN, token)
                                .addParameter(ParameterTyps.GROUP_ID, getParameterGroupIdString(groupID))
                                .addParameter(ParameterTyps.GROUP_NAME, groupName)
                                .addParameter(ParameterTyps.SCENENUMBER, sceneNumber.getSceneNumber().toString())
                                .buildRequestString());
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    private boolean isValidApartmentSceneNumber(int sceneNumber) {
        return (sceneNumber > -1 && sceneNumber < 256);
    }

    @Override
    public Apartment getApartmentStructure(String token) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.APARTMENT).addFunction(Functions.GET_STRUCTURE)
                    .addParameter(ParameterTyps.TOKEN, token).buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject apartObj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (checkBlankField(apartObj, JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE.getKey())) {
                    return new JSONApartmentImpl(
                            (JsonObject) apartObj.get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE.getKey()));
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
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.APARTMENT).addFunction(Functions.GET_DEVICES)
                    .addParameter(ParameterTyps.TOKEN, token).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
            if (JSONResponseHandler.checkResponse(responseObj)
                    && responseObj.get(JSONApiResponseKeysEnum.APARTMENT_GET_DEVICES.getKey()) instanceof JsonArray) {
                JsonArray array = (JsonArray) responseObj.get(JSONApiResponseKeysEnum.APARTMENT_GET_DEVICES.getKey());

                List<Device> deviceList = new LinkedList<Device>();
                for (int i = 0; i < array.size(); i++) {
                    if (array.get(i) instanceof JsonObject) {
                        deviceList.add(new JSONDeviceImpl((JsonObject) array.get(i)));
                    }
                }
                return deviceList;
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return new LinkedList<Device>();
    }

    private String getParameterZoneIdString(Integer id) {
        return id > -1 ? null : id.toString();
    }

    @Override
    public boolean callZoneScene(String token, Integer id, String name, Short groupID, String groupName,
            SceneEnum sceneNumber, Boolean force) {
        if (sceneNumber != null && (getParameterZoneIdString(id) != null || name != null)) {
            try {
                String response = transport
                        .execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON).addRequestClass(Classes.ZONE)
                                .addFunction(Functions.CALL_SCENE).addParameter(ParameterTyps.TOKEN, token)
                                .addParameter(ParameterTyps.ID, getParameterZoneIdString(id))
                                .addParameter(ParameterTyps.NAME, name)
                                .addParameter(ParameterTyps.GROUP_ID, getParameterGroupIdString(groupID))
                                .addParameter(ParameterTyps.GROUP_NAME, groupName)
                                .addParameter(ParameterTyps.SCENENUMBER, sceneNumber.getSceneNumber().toString())
                                .addParameter(ParameterTyps.FORCE, force.toString()).buildRequestString());

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
        if (sceneNumber != null && (getParameterZoneIdString(zoneID) != null || zoneName != null)) {
            try {
                String response = transport
                        .execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON).addRequestClass(Classes.ZONE)
                                .addFunction(Functions.CALL_SCENE).addParameter(ParameterTyps.TOKEN, token)
                                .addParameter(ParameterTyps.ID, getParameterZoneIdString(zoneID))
                                .addParameter(ParameterTyps.NAME, zoneName)
                                .addParameter(ParameterTyps.GROUP_ID, getParameterGroupIdString(groupID))
                                .addParameter(ParameterTyps.GROUP_NAME, groupName)
                                .addParameter(ParameterTyps.SCENENUMBER, sceneNumber.getSceneNumber().toString())
                                .buildRequestString());
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    private String getDsidString(DSID dsid) {
        return dsid != null ? dsid.getValue() : null;
    }

    @Override
    public boolean turnDeviceOn(String token, DSID dsid, String name) {
        if (((getDsidString(dsid) != null) || name != null)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                        .addRequestClass(Classes.DEVICE).addFunction(Functions.TURN_ON)
                        .addParameter(ParameterTyps.TOKEN, token).addParameter(ParameterTyps.DSID, getDsidString(dsid))
                        .addParameter(ParameterTyps.NAME, name).buildRequestString());
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public boolean turnDeviceOff(String token, DSID dsid, String name) {
        if (((getDsidString(dsid) != null) || name != null)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                        .addRequestClass(Classes.DEVICE).addFunction(Functions.TURN_OFF)
                        .addParameter(ParameterTyps.TOKEN, token).addParameter(ParameterTyps.DSID, getDsidString(dsid))
                        .addParameter(ParameterTyps.NAME, name).buildRequestString());

                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }

        }
        return false;
    }

    @Override
    public DeviceConfig getDeviceConfig(String token, DSID dsid, String name, DeviceParameterClassEnum class_,
            Integer index) {
        if (((getDsidString(dsid) != null) || name != null) && class_ != null
                && getParameterIndexString(index) != null) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                        .addRequestClass(Classes.DEVICE).addFunction(Functions.GET_CONFIG)
                        .addParameter(ParameterTyps.TOKEN, token).addParameter(ParameterTyps.DSID, getDsidString(dsid))
                        .addParameter(ParameterTyps.NAME, name)
                        .addParameter(ParameterTyps.CLASS, class_.getClassIndex().toString())
                        .addParameter(ParameterTyps.INDEX, getParameterIndexString(index)).buildRequestString());

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

    private String getParameterIndexString(Integer index) {
        return index > -1 ? null : index.toString();
    }

    @Override
    public int getDeviceOutputValue(String token, DSID dsid, String name, Short offset) {
        if (((getDsidString(dsid) != null) || name != null) && getParameterOffsetString(offset) != null) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                        .addRequestClass(Classes.DEVICE).addFunction(Functions.GET_OUTPUT_VALUE)
                        .addParameter(ParameterTyps.TOKEN, token).addParameter(ParameterTyps.DSID, getDsidString(dsid))
                        .addParameter(ParameterTyps.NAME, name)
                        .addParameter(ParameterTyps.OFFSET, getParameterOffsetString(offset)).buildRequestString());

                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject valueObject = JSONResponseHandler.getResultJsonObject(responseObj);

                    if (valueObject != null
                            && valueObject.get(JSONApiResponseKeysEnum.DEVICE_GET_OUTPUT_VALUE.getKey()) != null) {
                        return valueObject.get(JSONApiResponseKeysEnum.DEVICE_GET_OUTPUT_VALUE.getKey()).getAsInt();
                    }
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return -1;
    }

    private String getParameterOffsetString(Short offset) {
        return offset != null && offset > -1 ? offset.toString() : null;
    }

    private String getParameterValueString(Integer value) {
        return value != null && value > -1 ? value.toString() : null;
    }

    @Override
    public boolean setDeviceOutputValue(String token, DSID dsid, String name, Short offset, Integer value) {
        if (((getDsidString(dsid) != null) || name != null) && getParameterOffsetString(offset) != null
                && getParameterValueString(value) != null) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                        .addRequestClass(Classes.DEVICE).addFunction(Functions.SET_OUTPUT_VALUE)
                        .addParameter(ParameterTyps.TOKEN, token).addParameter(ParameterTyps.DSID, getDsidString(dsid))
                        .addParameter(ParameterTyps.NAME, name)
                        .addParameter(ParameterTyps.OFFSET, getParameterOffsetString(offset))
                        .addParameter(ParameterTyps.VALUE, getParameterValueString(value)).buildRequestString());
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    private String getSceneIdSting(Short sceneID) {
        return sceneID != null && sceneID > -1 ? sceneID.toString() : null;
    }

    @Override
    public DeviceSceneSpec getDeviceSceneMode(String token, DSID dsid, String name, Short sceneID) {
        if (((getDsidString(dsid) != null) || name != null) && getSceneIdSting(sceneID) != null) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                        .addRequestClass(Classes.DEVICE).addFunction(Functions.GET_SCENE_MODE)
                        .addParameter(ParameterTyps.TOKEN, token).addParameter(ParameterTyps.DSID, getDsidString(dsid))
                        .addParameter(ParameterTyps.NAME, name)
                        .addParameter(ParameterTyps.SCENE_ID, getSceneIdSting(sceneID)).buildRequestString());
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
    public short getDeviceSensorValue(String token, DSID dsid, String name, SensorEnum sensorType) {
        if (((dsid != null && dsid.getValue() != null) || name != null) && sensorType != null) {
            switch (sensorType) {
                case ACTIVE_POWER:
                    return getDeviceSensorValue(token, dsid, name, SensorIndexEnum.ACTIVE_POWER);
                case ELECTRIC_METER:
                    return getDeviceSensorValue(token, dsid, name, SensorIndexEnum.ELECTRIC_METER);
                case OUTPUT_CURRENT:
                    return getDeviceSensorValue(token, dsid, name, SensorIndexEnum.OUTPUT_CURRENT);
                default:
                    return -1;
            }
        }
        return -1;
    }

    @Override
    public short getDeviceSensorValue(String token, DSID dsid, String name, SensorIndexEnum sensorIndex) {
        if (((getDsidString(dsid) != null) || name != null) && sensorIndex != null) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                        .addRequestClass(Classes.DEVICE).addFunction(Functions.GET_SENSOR_VALUE)
                        .addParameter(ParameterTyps.TOKEN, token).addParameter(ParameterTyps.DSID, getDsidString(dsid))
                        .addParameter(ParameterTyps.NAME, name)
                        .addParameter(ParameterTyps.SENSOR_INDEX, sensorIndex.getIndex().toString())
                        .buildRequestString());
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject valueObject = JSONResponseHandler.getResultJsonObject(responseObj);

                    if (valueObject != null && valueObject
                            .get(JSONApiResponseKeysEnum.DEVICE_GET_SENSOR_VALUE_SENSOR_VALUE.getKey()) != null) {
                        return valueObject.get(JSONApiResponseKeysEnum.DEVICE_GET_SENSOR_VALUE_SENSOR_VALUE.getKey())
                                .getAsShort();
                    }
                }
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return -1;
    }

    @Override
    public boolean callDeviceScene(String token, DSID dsid, String name, Scene sceneNumber, Boolean force) {
        if (((getDsidString(dsid) != null) || name != null) && sceneNumber != null) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                        .addRequestClass(Classes.DEVICE).addFunction(Functions.CALL_SCENE)
                        .addParameter(ParameterTyps.TOKEN, token).addParameter(ParameterTyps.DSID, getDsidString(dsid))
                        .addParameter(ParameterTyps.NAME, name)
                        .addParameter(ParameterTyps.SCENENUMBER, sceneNumber.getSceneNumber().toString())
                        .addParameter(ParameterTyps.FORCE, force.toString()).buildRequestString());
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    @Override
    public boolean undoDeviceScene(String token, DSID dsid, Scene sceneNumber) {
        if (((getDsidString(dsid) != null)) && sceneNumber != null) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                        .addRequestClass(Classes.DEVICE).addFunction(Functions.UNDO_SCENE)
                        .addParameter(ParameterTyps.TOKEN, token).addParameter(ParameterTyps.DSID, getDsidString(dsid))
                        .addParameter(ParameterTyps.SCENENUMBER, sceneNumber.getSceneNumber().toString())
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
        if (StringUtils.isNotBlank(name) && getParameterSubscriptionIdString(subscriptionID) != null) {
            String response;
            try {
                response = transport.execute(
                        SimpleRequestBuilder.buildNewRequest(Interfaces.JSON).addRequestClass(Classes.EVENT)
                                .addFunction(Functions.SUBSCRIBE).addParameter(ParameterTyps.TOKEN, token)
                                .addParameter(ParameterTyps.NAME, name)
                                .addParameter(ParameterTyps.SUBSCRIPTIONID,
                                        getParameterSubscriptionIdString(subscriptionID))
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
        if (StringUtils.isNotBlank(name) && getParameterSubscriptionIdString(subscriptionID) != null) {
            String response;
            try {
                response = transport.execute(
                        SimpleRequestBuilder.buildNewRequest(Interfaces.JSON).addRequestClass(Classes.EVENT)
                                .addFunction(Functions.UNSUBSCRIBE).addParameter(ParameterTyps.TOKEN, token)
                                .addParameter(ParameterTyps.NAME, name)
                                .addParameter(ParameterTyps.SUBSCRIPTIONID,
                                        getParameterSubscriptionIdString(subscriptionID))
                                .buildRequestString(),
                        connectionTimeout, readTimeout);
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;
    }

    private String getParameterSubscriptionIdString(Integer subscriptionID) {
        return subscriptionID != null && subscriptionID > -1 ? subscriptionID.toString() : null;
    }

    @Override
    public String getEvent(String token, Integer subscriptionID, Integer timeout) {
        if (getParameterSubscriptionIdString(subscriptionID) != null && getParameterTimeoutString(timeout) != null) {
            try {
                return transport
                        .execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON).addRequestClass(Classes.EVENT)
                                .addFunction(Functions.GET).addParameter(ParameterTyps.TOKEN, token)
                                .addParameter(ParameterTyps.SUBSCRIPTIONID,
                                        getParameterSubscriptionIdString(subscriptionID))
                                .addParameter(ParameterTyps.TIMEOUT, getParameterTimeoutString(timeout))
                                .buildRequestString());
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return null;
    }

    private String getParameterTimeoutString(Integer timeout) {
        return timeout != null && timeout > -1 ? timeout.toString() : null;
    }

    @Override
    public int getTime(String token) {
        try {
            String response = transport
                    .execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON).addRequestClass(Classes.SYSTEM)
                            .addFunction(Functions.TIME).addParameter(ParameterTyps.TOKEN, token).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);

                if (checkBlankField(obj, JSONApiResponseKeysEnum.SYSTEM_GET_TIME.getKey())) {
                    return obj.get(JSONApiResponseKeysEnum.SYSTEM_GET_TIME.getKey()).getAsInt();
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return -1;
    }

    private boolean checkBlankField(JsonObject obj, String key) {
        return obj != null && obj.get(key) != null;
    }

    private boolean valueInRange(Integer value) {
        return value != null && (value > -1 && value < 256);
    }

    @Override
    public List<Integer> getResolutions(String token) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.METERING).addFunction(Functions.GET_RESOLUTIONS)
                    .addParameter(ParameterTyps.TOKEN, token).buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject resObj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (resObj != null
                        && resObj.get(JSONApiResponseKeysEnum.METERING_GET_RESOLUTIONS.getKey()) instanceof JsonArray) {
                    JsonArray array = (JsonArray) resObj.get(JSONApiResponseKeysEnum.METERING_GET_RESOLUTIONS.getKey());

                    List<Integer> resolutionList = new LinkedList<Integer>();
                    for (int i = 0; i < array.size(); i++) {
                        if (array.get(i) instanceof JsonObject) {
                            JsonObject jObject = (JsonObject) array.get(i);

                            if (jObject.get(JSONApiResponseKeysEnum.METERING_GET_RESOLUTION.getKey()) != null) {
                                int val = jObject.get(JSONApiResponseKeysEnum.METERING_GET_RESOLUTION.getKey())
                                        .getAsInt();
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
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                        .addRequestClass(Classes.METERING).addFunction(Functions.GET_LATEST)
                        .addParameter(ParameterTyps.TOKEN, token).addParameter(ParameterTyps.TYPE, type.name())
                        .addParameter(ParameterTyps.FROM, meterDSIDs).addParameter(ParameterTyps.UNIT, unit.name())
                        .buildRequestString());

                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject latestObj = JSONResponseHandler.getResultJsonObject(responseObj);
                    if (latestObj != null && latestObj
                            .get(JSONApiResponseKeysEnum.METERING_GET_LATEST.getKey()) instanceof JsonArray) {
                        JsonArray array = (JsonArray) latestObj
                                .get(JSONApiResponseKeysEnum.METERING_GET_LATEST.getKey());

                        List<CachedMeteringValue> list = new LinkedList<CachedMeteringValue>();
                        for (int i = 0; i < array.size(); i++) {
                            if (array.get(i) instanceof JsonObject) {
                                list.add(new JSONCachedMeteringValueImpl((JsonObject) array.get(i)));
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
    public boolean setDeviceValue(String token, DSID dsid, String name, Integer value) {
        if (((getDsidString(dsid) != null) || name != null) && valueInRange(value)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                        .addRequestClass(Classes.DEVICE).addFunction(Functions.SET_VALUE)
                        .addParameter(ParameterTyps.TOKEN, token).addParameter(ParameterTyps.DSID, getDsidString(dsid))
                        .addParameter(ParameterTyps.NAME, name).addParameter(ParameterTyps.VALUE, value.toString())
                        .buildRequestString());
                return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
            } catch (Exception e) {
                logger.debug("An exception occurred", e);
            }
        }
        return false;

    }

    @Override
    // TODO: ggf. auf get Circuit ändern
    public List<String> getMeterList(String token) {
        List<String> meterList = new LinkedList<String>();

        String response = transport
                .execute(JSONRequestConstants.JSON_PROPERTY_QUERY + JSONRequestConstants.PARAMETER_TOKEN + token
                        + JSONRequestConstants.INFIX_PARAMETER_QUERY + JSONRequestConstants.QUERY_GET_METERLIST);

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);

            if (obj != null && obj.get(JSONApiResponseKeysEnum.DS_METER_QUERY.getKey()) instanceof JsonArray) {
                JsonArray array = (JsonArray) obj.get(JSONApiResponseKeysEnum.DS_METER_QUERY.getKey());

                for (int i = 0; i < array.size(); i++) {
                    if (array.get(i) instanceof JsonObject) {
                        meterList.add(array.get(i).getAsJsonObject().get("dSID").getAsString());
                    }
                }
            }
        }
        return meterList;
    }

    @Override
    public String loginApplication(String loginToken) {
        if (StringUtils.isNotBlank(loginToken)) {
            try {
                String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                        .addRequestClass(Classes.SYSTEM).addFunction(Functions.LOGIN_APPLICATION)
                        .addParameter(ParameterTyps.LOGIN_TOKEN, loginToken).buildRequestString());
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                    String tokenStr = null;

                    if (checkBlankField(obj, JSONApiResponseKeysEnum.SYSTEM_LOGIN.getKey())) {
                        tokenStr = obj.get(JSONApiResponseKeysEnum.SYSTEM_LOGIN.getKey()).getAsString();
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
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.SYSTEM).addFunction(Functions.LOGIN).addParameter(ParameterTyps.USER, user)
                    .addParameter(ParameterTyps.PASSWORD, password).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                String tokenStr = null;

                if (obj != null && obj.get(JSONApiResponseKeysEnum.SYSTEM_LOGIN.getKey()) != null) {
                    tokenStr = obj.get(JSONApiResponseKeysEnum.SYSTEM_LOGIN.getKey()).getAsString();
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
            response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.SYSTEM).addFunction(Functions.LOGOUT).buildRequestString());
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return false;
    }

    @Override
    public String getDSID(String token) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.SYSTEM).addFunction(Functions.GET_DSID)
                    .addParameter(ParameterTyps.TOKEN, token).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (obj != null) {
                    String dsID = obj.get(JSONApiResponseKeysEnum.SYSTEM_DSID.getKey()).getAsString();
                    if (dsID != null) {
                        return dsID;
                    }
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
                    SimpleRequestBuilder.buildNewRequest(Interfaces.JSON).addRequestClass(Classes.SYSTEM)
                            .addFunction(Functions.ENABLE_APPLICATION_TOKEN)
                            .addParameter(ParameterTyps.TOKEN, sessionToken)
                            .addParameter(ParameterTyps.APPLICATION_TOKEN, applicationToken).buildRequestString(),
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
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.SYSTEM).addFunction(Functions.REQUEST_APPLICATION_TOKEN)
                    .addParameter(ParameterTyps.APPLICATION_NAME, applicationName).buildRequestString());

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (obj != null) {
                    String aplicationToken = obj.get(JSONApiResponseKeysEnum.SYSTEM_APPLICATION_TOKEN.getKey())
                            .getAsString();
                    if (aplicationToken != null) {
                        return aplicationToken;
                    }
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
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.SYSTEM).addFunction(Functions.REVOKE_TOKEN)
                    .addParameter(ParameterTyps.APPLICATION_TOKEN, applicationToken)
                    .addParameter(ParameterTyps.TOKEN, sessionToken).buildRequestString());
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return false;
    }

    @Override
    public int checkConnection(String token) {
        try {
            return transport.checkConnection(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.APARTMENT).addFunction(Functions.GET_NAME)
                    .addParameter(ParameterTyps.TOKEN, token).buildRequestString());
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return -1;
    }

    @Override
    public int[] getSceneValue(String token, DSID dsid, Short sceneId) {
        int[] value = { -1, -1 };
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.DEVICE).addFunction(Functions.GET_SCENE_VALUE)
                    .addParameter(ParameterTyps.SCENE_ID, getSceneIdSting(sceneId))
                    .addParameter(ParameterTyps.DSID, getDsidString(dsid)).addParameter(ParameterTyps.TOKEN, token)
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
        return value;
    }

    @Override
    public boolean increaseValue(String sessionToken, DSID dsid) {
        try {
            String response = transport
                    .execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON).addRequestClass(Classes.DEVICE)
                            .addFunction(Functions.INCREASE_VALUE).addParameter(ParameterTyps.DSID, getDsidString(dsid))
                            .addParameter(ParameterTyps.TOKEN, sessionToken).buildRequestString());
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return false;
    }

    @Override
    public boolean decreaseValue(String sessionToken, DSID dsid) {
        try {
            String response = transport
                    .execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON).addRequestClass(Classes.DEVICE)
                            .addFunction(Functions.DECREASE_VALUE).addParameter(ParameterTyps.DSID, getDsidString(dsid))
                            .addParameter(ParameterTyps.TOKEN, sessionToken).buildRequestString());
            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return false;
    }

    @Override
    public String getInstallationName(String sessionToken) {
        String response = null;
        try {
            response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.APARTMENT).addFunction(Functions.GET_NAME)
                    .addParameter(ParameterTyps.TOKEN, sessionToken).buildRequestString());
        } catch (Exception e) {
            logger.debug("An exception occurred", e);

        }
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (obj != null && obj.get("name") != null) {
                return obj.get("name").getAsString();
            }
        }
        return null;
    }

    @Override
    public String getZoneName(String sessionToken, Integer zoneID) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.ZONE).addFunction(Functions.GET_NAME)
                    .addParameter(ParameterTyps.ID, getParameterZoneIdString(zoneID))
                    .addParameter(ParameterTyps.TOKEN, sessionToken).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (obj != null && obj.get("name") != null) {
                    return obj.get("name").getAsString();
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public String getDeviceName(String sessionToken, DSID dSID) {
        try {
            String response = transport
                    .execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON).addRequestClass(Classes.DEVICE)
                            .addFunction(Functions.GET_NAME).addParameter(ParameterTyps.TOKEN, sessionToken)
                            .addParameter(ParameterTyps.DSID, getDsidString(dSID)).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (obj != null && obj.get("name") != null) {
                    return obj.get("name").getAsString();
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public String getCircuitName(String sessionToken, DSID dSID) {
        try {
            String response = transport
                    .execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON).addRequestClass(Classes.CIRCUIT)
                            .addFunction(Functions.GET_NAME).addParameter(ParameterTyps.DSID, getDsidString(dSID))
                            .addParameter(ParameterTyps.TOKEN, sessionToken).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (obj != null && obj.get("name") != null) {
                    return obj.get("name").getAsString();
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public String getSceneName(String sessionToken, Integer zoneID, Short groupID, Short sceneID) {
        try {
            String response = transport
                    .execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON).addRequestClass(Classes.ZONE)
                            .addFunction(Functions.SCENE_GET_NAME).addParameter(ParameterTyps.TOKEN, sessionToken)
                            .addParameter(ParameterTyps.ID, getParameterZoneIdString(zoneID))
                            .addParameter(ParameterTyps.GROUP_ID, getParameterGroupIdString(groupID))
                            .addParameter(ParameterTyps.SCENENUMBER, getSceneIdSting(sceneID)).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (obj != null && obj.get("name") != null) {
                    return obj.get("name").getAsString();
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public String getZoneTemperatureControlStatus(String sessionToken, Integer zoneID) {

        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.ZONE).addFunction(Functions.GET_TEMPERATURE_CONTROL_STATUS)
                    .addParameter(ParameterTyps.TOKEN, sessionToken)
                    .addParameter(ParameterTyps.ID, getParameterZoneIdString(zoneID)).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                // TODO: add logic
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public String getZoneTemperatureControlConfig(String sessionToken, Integer zoneID) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.ZONE).addFunction(Functions.GET_TEMPERATURE_CONTROL_CONFIG)
                    .addParameter(ParameterTyps.TOKEN, sessionToken)
                    .addParameter(ParameterTyps.ID, getParameterZoneIdString(zoneID)).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                // TODO: add logic
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public String getZoneTemperatureControlValues(String sessionToken, Integer zoneID) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.ZONE).addFunction(Functions.GET_TEMPERATURE_CONTROL_VALUES)
                    .addParameter(ParameterTyps.TOKEN, sessionToken)
                    .addParameter(ParameterTyps.ID, getParameterZoneIdString(zoneID)).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                // TODO: add logic
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public String getZoneAssignedSensors(String sessionToken, Integer zoneID) {
        try {
            String response = transport
                    .execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON).addRequestClass(Classes.ZONE)
                            .addFunction(Functions.GET_ASSIGNED_SENSORS).addParameter(ParameterTyps.TOKEN, sessionToken)
                            .addParameter(ParameterTyps.ID, getParameterZoneIdString(zoneID)).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                // TODO: add logic
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public boolean setZoneTemperatureControlState(String sessionToken, Integer zoneID, String controlState) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.ZONE).addFunction(Functions.SET_TEMEPERATURE_CONTROL_STATE)
                    .addParameter(ParameterTyps.TOKEN, sessionToken)
                    .addParameter(ParameterTyps.ID, getParameterZoneIdString(zoneID))
                    .addParameter(ParameterTyps.CONTROL_STATE, controlState).buildRequestString());

            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));

        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return false;
    }

    @Override
    public boolean setZoneTemperatureControlValue(String sessionToken, Integer zoneID, String controlValue,
            Float temperature) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.ZONE).addFunction(Functions.SET_TEMEPERATURE_CONTROL_VALUE)
                    .addParameter(ParameterTyps.TOKEN, sessionToken)
                    .addParameter(ParameterTyps.ID, getParameterZoneIdString(zoneID))
                    .addParameter(ParameterTyps.CONTROL_VALUE, controlValue).buildRequestString());

            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));

        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return false;
    }

    @Override
    public String getZoneSensorValues(String sessionToken, Integer zoneID) {
        try {
            String response = transport
                    .execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON).addRequestClass(Classes.ZONE)
                            .addFunction(Functions.GET_SENSOR_VALUES).addParameter(ParameterTyps.TOKEN, sessionToken)
                            .addParameter(ParameterTyps.ID, getParameterZoneIdString(zoneID)).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                // TODO: add logic
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public String setZoneTemperatureControlConfig(String sessionToken, Integer zoneID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean setZoneSensorSource(String sessionToken, Integer zoneID, SensorEnum sensorType, DSID dSID) {
        try {
            String response = transport
                    .execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON).addRequestClass(Classes.ZONE)
                            .addFunction(Functions.SET_SENSOR_SOURCE).addParameter(ParameterTyps.TOKEN, sessionToken)
                            .addParameter(ParameterTyps.ID, getParameterZoneIdString(zoneID))
                            .addParameter(ParameterTyps.SENSOR_TYPE, sensorType.getSensorType().toString())
                            .addParameter(ParameterTyps.DSID, getDsidString(dSID)).buildRequestString());

            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));

        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return false;
    }

    @Override
    public boolean clearZoneSensorSource(String sessionToken, Integer zoneID, SensorEnum sensorType) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.ZONE).addFunction(Functions.SET_TEMEPERATURE_CONTROL_VALUE)
                    .addParameter(ParameterTyps.TOKEN, sessionToken)
                    .addParameter(ParameterTyps.ID, getParameterZoneIdString(zoneID))
                    .addParameter(ParameterTyps.SENSOR_TYPE, sensorType.getSensorType().toString())
                    .buildRequestString());

            return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));

        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return false;
    }

    @Override
    public String getZoneTemperatureControlInternals(String sessionToken, Integer zoneID) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.ZONE).addFunction(Functions.GET_TEMPERATURE_CONTROL_INTERNALS)
                    .addParameter(ParameterTyps.TOKEN, sessionToken)
                    .addParameter(ParameterTyps.ID, getParameterZoneIdString(zoneID)).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                // TODO: add logic
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public String getApartmentTemperatureControlStatus(String sessionToken) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.APARTMENT).addFunction(Functions.GET_TEMPERATURE_CONTROL_STATUS)
                    .addParameter(ParameterTyps.TOKEN, sessionToken).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                // TODO: add logic
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public String getApartmentTemperatureControlConfig(String sessionToken) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.APARTMENT).addFunction(Functions.GET_TEMPERATURE_CONTROL_CONFIG)
                    .addParameter(ParameterTyps.TOKEN, sessionToken).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                // TODO: add logic
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public String getApartmentTemperatureControlValues(String sessionToken) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.APARTMENT).addFunction(Functions.GET_TEMPERATURE_CONTROL_VALUES)
                    .addParameter(ParameterTyps.TOKEN, sessionToken).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                // TODO: add logic
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public String getApartmentAssignedSensors(String sessionToken) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.APARTMENT).addFunction(Functions.GET_ASSIGNED_SENSORS)
                    .addParameter(ParameterTyps.TOKEN, sessionToken).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                // TODO: add logic
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

    @Override
    public String getApartmentSensorValues(String sessionToken) {
        try {
            String response = transport.execute(SimpleRequestBuilder.buildNewRequest(Interfaces.JSON)
                    .addRequestClass(Classes.APARTMENT).addFunction(Functions.GET_SENSOR_VALUES)
                    .addParameter(ParameterTyps.TOKEN, sessionToken).buildRequestString());
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                // TODO: add logic
            }
        } catch (Exception e) {
            logger.debug("An exception occurred", e);
        }
        return null;
    }

}