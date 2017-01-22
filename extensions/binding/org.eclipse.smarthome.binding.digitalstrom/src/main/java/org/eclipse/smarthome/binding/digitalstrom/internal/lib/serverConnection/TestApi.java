package org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.BaseSensorValues;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.AssignedSensors;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlConfig;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlStatus;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlValues;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.config.Config;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.EventHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.EventListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.constants.EventNames;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.types.Event;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.types.EventItem;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.types.JSONEventImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.ConnectionListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.impl.ConnectionManagerImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.impl.JSONResponseHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleDSRequestBuilder.SimpleRequestBuilder;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleDSRequestBuilder.constants.ClassKeys;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleDSRequestBuilder.constants.FunctionKeys;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleDSRequestBuilder.constants.InterfaceKeys;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleDSRequestBuilder.constants.ParameterKeys;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Circuit;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.CachedMeteringValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.ChangeableDeviceConfigEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringUnitsEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.JSONDeviceSceneSpecImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.impl.DeviceImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.SceneDiscovery;
import org.eclipse.smarthome.binding.digitalstrom.internal.providers.DsChannelTypeProvider;
import org.eclipse.smarthome.core.library.types.DecimalType;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TestApi {

    private static class DummyConnectionListener implements ConnectionListener {

        @Override
        public void onConnectionStateChange(String newConnectionState) {
            System.out.println(newConnectionState);
        }

        @Override
        public void onConnectionStateChange(String newConnectionState, String reason) {
            System.out.println(newConnectionState + ", " + reason);
        }

    }

    private static class DummyListener implements DeviceStatusListener {

        private final String DSID;

        public DummyListener(String dsid) {
            this.DSID = dsid;
        }

        @Override
        public void onDeviceStateChanged(DeviceStateUpdate deviceStateUpdate) {
            System.out.println("State changed");
            if (deviceStateUpdate != null
                    && DeviceStateUpdate.UPDATE_CIRCUIT_METER.equals(deviceStateUpdate.getType())) {
                if (deviceStateUpdate.getValue() instanceof CachedMeteringValue) {
                    CachedMeteringValue cachedVal = (CachedMeteringValue) deviceStateUpdate.getValue();
                    System.out.println(cachedVal.getMeteringType() + " " + cachedVal.getMeteringUnit() + " = "
                            + (cachedVal.getMeteringType().equals(MeteringTypeEnum.ENERGY)
                                    && (cachedVal.getMeteringUnit() == null
                                            || cachedVal.getMeteringUnit().equals(MeteringUnitsEnum.WH))));
                    if (cachedVal.getMeteringType().equals(MeteringTypeEnum.ENERGY)
                            && (cachedVal.getMeteringUnit() == null
                                    || cachedVal.getMeteringUnit().equals(MeteringUnitsEnum.WH))) {
                        System.out.println("new Value = " + new DecimalType(cachedVal.getValue() * 0.001));
                    }
                    System.out.println("new Value = " + new DecimalType(cachedVal.getValue()));
                }
            }
        }

        @Override
        public void onDeviceRemoved(Object device) {
            System.out.println(device + " removed");
        }

        @Override
        public void onDeviceAdded(Object device) {
            System.out.println(device + " added");
        }

        @Override
        public void onDeviceConfigChanged(ChangeableDeviceConfigEnum whatConfig) {

        }

        @Override
        public void onSceneConfigAdded(short sceneID) {

        }

        @Override
        public String getDeviceStatusListenerID() {
            return DSID;
        }

    }

    public static String buildChannelID(String... parts) {
        return StringUtils.join(parts, DsChannelTypeProvider.SEPERATOR);
    }

    public static void main(String[] args) {
        String host = "fsqdss1.inf.fh-koeln.de";
        String host1 = "testrack2.aizo.com:58080";
        String user = "dssadmin";
        String pw = "5Kp1_i1B";

        final String JSON_API_HEATING = "APIheating";
        final String EVENT_LISTENER = "eventListener";
        final String PARSE_TEST = "parseTest";
        final String DEVICE_QUERY2 = "devQ2";
        String testType = "";

        final ConnectionManager connMan = new ConnectionManagerImpl(host1, user, user, true);
        connMan.registerConnectionListener(new DummyConnectionListener());
        final DsAPI dSAPI = connMan.getDigitalSTROMAPI();
        System.out.println(
                connMan.getDigitalSTROMAPI().query(connMan.getSessionToken(), SceneDiscovery.NAMEND_SCENE_QUERY));
        System.out.println(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.SYSTEM)
                .addFunction(FunctionKeys.GET_DSID).addParameter(ParameterKeys.TOKEN, null).buildRequestString());
        System.out.println(dSAPI.getDSID(null));
        // connMan.checkConnection();
        String request = SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.SYSTEM)
                .addFunction(FunctionKeys.LOGIN_APPLICATION).addParameter("test", "test").buildRequestString();

        String functionName = request.substring(request.indexOf("/", (request.indexOf("/", 1) + 1)) + 1);
        int functionEnd;
        if ((functionEnd = functionName.indexOf("?")) > -1) {
            functionName = functionName.substring(0, functionEnd);
        }
        functionName = StringUtils.substringAfterLast(StringUtils.substringBefore(null, "?"), "/");
        System.out.println(StringUtils.replaceOnce(request,
                StringUtils.substringBefore(StringUtils.substringAfter(request, "token="), "&"), "dssadmin2"));
        System.out.println(dSAPI.getInstallationName("123"));

        String channelID = DsChannelTypeProvider.getMeteringChannelID(MeteringTypeEnum.ENERGY, MeteringUnitsEnum.WH,
                false);
        System.out.println(channelID);
        System.out.println(DsChannelTypeProvider.getMeteringType(channelID));
        List<CachedMeteringValue> meteringVale = dSAPI.getLatest(connMan.getSessionToken(),
                MeteringTypeEnum.CONSUMPTION, DsAPI.ALL_METERS, null);
        for (Circuit circuit : dSAPI.getApartmentCircuits(null)) {
            for (CachedMeteringValue meterval : meteringVale) {
                if (meterval.getDsid().equals(circuit.getDSID())) {
                    circuit.addMeteringValue(meterval);
                    System.out.println(circuit.getAllCachedMeteringValues().toString());
                }
            }
        }
        /*
         * String channelIDpre = DsChannelTypeProvider.GENERAL;
         * List<String> supportedOutputChannelTypes = new LinkedList<String>();
         * for (short i = 0; i < 3; i++) {
         * if (i == 1) {
         * channelIDpre = DsChannelTypeProvider.LIGHT;
         * }
         * if (i == 2) {
         * channelIDpre = DsChannelTypeProvider.HEATING;
         * supportedOutputChannelTypes.add(DsChannelTypeProvider.buildIdentifier(channelIDpre,
         * DsChannelTypeProvider.TEMPERATURE_CONTROLLED));
         * }
         * supportedOutputChannelTypes
         * .add(DsChannelTypeProvider.buildIdentifier(channelIDpre, DsChannelTypeProvider.SWITCH));
         * supportedOutputChannelTypes
         * .add(DsChannelTypeProvider.buildIdentifier(channelIDpre, DsChannelTypeProvider.DIMMER));
         * if (i < 2) {
         * supportedOutputChannelTypes
         * .add(DsChannelTypeProvider.buildIdentifier(channelIDpre, "2", DsChannelTypeProvider.STAGE));
         * supportedOutputChannelTypes
         * .add(DsChannelTypeProvider.buildIdentifier(channelIDpre, "3", DsChannelTypeProvider.STAGE));
         * }
         * }
         * channelIDpre = DsChannelTypeProvider.SHADE;
         * supportedOutputChannelTypes.add(channelIDpre);
         * supportedOutputChannelTypes
         * .add(DsChannelTypeProvider.buildIdentifier(channelIDpre, DsChannelTypeProvider.ANGLE));
         * supportedOutputChannelTypes.add(DsChannelTypeProvider.SCENE);
         *
         * System.out.println(supportedOutputChannelTypes.toString());
         */
        // StringUtils.join([DsChannelTypeProvider.SHADE, DsChannelTypeProvider.ANGLE]),
        // DsChannelTypeProvider.SEPERATOR));

        /*
         * List<ChannelTypeUID> channelTypeList = new LinkedList<ChannelTypeUID>();
         * for (String channelTypeId : supportedOutputChannelTypes) {
         * channelTypeList.add(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID, channelTypeId));
         * }
         * for (SensorEnum sensorType : SensorEnum.values()) {
         * channelTypeList.add(
         * new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID, sensorType.toString().toLowerCase()));
         * }
         * for (MeteringTypeEnum meteringType : MeteringTypeEnum.values()) {
         * channelTypeList.add(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID,
         * DsChannelTypeProvider.buildIdentifier(meteringType.toString(), MeteringUnitsEnum.WH.toString())));
         * channelTypeList.add(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID,
         * DsChannelTypeProvider.buildIdentifier(DsChannelTypeProvider.TOTAL_PRE, meteringType.toString(),
         * MeteringUnitsEnum.WH.toString())));
         * }
         * for (DeviceBinarayInputEnum binaryInput : DeviceBinarayInputEnum.values()) {
         * channelTypeList.add(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID, DsChannelTypeProvider
         * .buildIdentifier(DsChannelTypeProvider.BINARY_INPUT_PRE, binaryInput.toString().toLowerCase())));
         * }
         * System.out.println(channelTypeList.toString());
         */

        // connMan.checkConnection();
        new Thread(new Runnable() {

            @Override
            public void run() {
                int counter = 0;
                long sleepTime = 175000;
                while (true) {
                    // if (counter++ > 0) {
                    // connMan.getConfig().setUserName("dssadmin");
                    // }
                    System.out.println("request was sucsessfull? " + (dSAPI.getInstallationName(null)) + " " + counter);
                    // new SecureRandom().nextInt(65000);
                    System.out.println("sleep for: " + sleepTime / 1000 + " seconds");

                    try {
                        Thread.sleep(sleepTime);
                        sleepTime += 1000;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }

        });// .start();

        /*
         * String path =
         * "F:/PraxisProjekt/smarthome-master/git/smarthome/extensions/binding/org.eclipse.smarthome.binding.digitalstrom/ESH-INF/i18n/digitalstrom_en.properties";
         * File en = new File(path);
         * Map<String, String> i18nMap = new HashMap<String, String>();
         * if (en.exists()) {
         * try {
         * InputStream certInputStream = new FileInputStream(en);
         * InputStreamReader reader = new InputStreamReader(certInputStream);
         * BufferedReader br = new BufferedReader(reader);
         *
         * String currentLine;
         * while ((currentLine = br.readLine()) != null) {
         * if (!currentLine.startsWith("#")) {
         * String[] currentLineSnipp = currentLine.split("=");
         * if (currentLineSnipp.length == 2) {
         * i18nMap.put(currentLineSnipp[0].replaceAll(" ", ""), currentLineSnipp[1]);
         * }
         * // System.out.println(currentLine);
         * }
         * }
         * br.close();
         * System.out.println(i18nMap.toString());
         * } catch (FileNotFoundException e) {
         * System.err.println("Can't find a certificate file at the path: " + path + "\nPlease check the path!");
         * } catch (IOException e) {
         * System.err.println("An IOException occurred: " + e.getMessage());
         * }
         * }
         * String channelID = null;
         * for (short i = 1; i < 21; i++) {
         * channelID = DsChannelTypeProvider.getBinaryInputChannelUID(DeviceBinarayInputEnum.getdeviceBinarayInput(i))
         * .getId();
         * System.out.println("| " + channelID + " | Switch | " + i18nMap.get(channelID + "_desc") + " | SW |");
         * }
         * System.out.println();
         * for (SensorEnum sensor : SensorEnum.values()) {
         * channelID = DsChannelTypeProvider.getSensorChannelUID(sensor).getId();
         * System.out.println("| " + channelID + " | Number | " + i18nMap.get(channelID + "_desc") + " | --- |");
         * }
         */
        // System.out.println(connMan.getDigitalSTROMAPI().getLatest(connMan.getSessionToken(),
        // MeteringTypeEnum.energyDelta,
        // ALL_METERS, MeteringUnitsEnum.Wh));
        // System.out.println(connMan.getDigitalSTROMAPI().getLatest(connMan.getSessionToken(),
        // MeteringTypeEnum.energyDelta,
        // ALL_METERS, MeteringUnitsEnum.Ws));

        /*
         * System.out.println(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.
         * APARTMENT)
         * .addFunction(FunctionKeys.GET_CIRCUITS).addParameter(ParameterKeys.TOKEN, connMan.getSessionToken())
         * .buildRequestString());
         * System.out.println(connMan.getHttpTransport()
         * .execute(SimpleRequestBuilder.buildNewRequest(InterfaceKeys.JSON).addRequestClass(ClassKeys.APARTMENT)
         * .addFunction(FunctionKeys.GET_CIRCUITS)
         * .addParameter(ParameterKeys.TOKEN, connMan.getSessionToken()).buildRequestString()));
         * System.out.println(connMan.getDigitalSTROMAPI().getApartmentCircuits(connMan.getSessionToken()));
         * final String LAST_CALL_SCENE_QUERY = "/apartment/zones/*(*)/groups/*(*)/*(*)";
         *
         * List<String> SUPPORTED_OUTPUT_CHANNEL_TYPES = new ArrayList<>();
         *
         * String channelIDpre = DsChannelTypeProvider.GENERAL;
         * for (short i = 0; i < 3; i++) {
         * if (i == 1) {
         * channelIDpre = DsChannelTypeProvider.LIGHT;
         * }
         * if (i == 2) {
         * channelIDpre = DsChannelTypeProvider.HEATING;
         * SUPPORTED_OUTPUT_CHANNEL_TYPES.add(channelIDpre + DsChannelTypeProvider.TEMPERATURE_CONTROLLED);
         * }
         * SUPPORTED_OUTPUT_CHANNEL_TYPES.add(channelIDpre + DsChannelTypeProvider.SWITCH);
         * SUPPORTED_OUTPUT_CHANNEL_TYPES.add(channelIDpre + DsChannelTypeProvider.DIMMER);
         * if (i < 2) {
         * SUPPORTED_OUTPUT_CHANNEL_TYPES.add(channelIDpre + 2 + DsChannelTypeProvider.STAGE);
         * SUPPORTED_OUTPUT_CHANNEL_TYPES.add(channelIDpre + 3 + DsChannelTypeProvider.STAGE);
         * }
         * }
         * channelIDpre = DsChannelTypeProvider.SHADE;
         * SUPPORTED_OUTPUT_CHANNEL_TYPES.add(channelIDpre);
         * SUPPORTED_OUTPUT_CHANNEL_TYPES.add(channelIDpre + DsChannelTypeProvider.ANGLE);
         * for (String channelID : SUPPORTED_OUTPUT_CHANNEL_TYPES) {
         * System.out.println(channelID);
         * }
         *
         * System.out.println("");
         *
         * FunctionalColorGroupEnum functionalGroup = FunctionalColorGroupEnum.YELLOW;
         * OutputModeEnum outputMode = OutputModeEnum.COMBINED_3_STAGE_SWITCH;
         *
         * String channelPreID = DsChannelTypeProvider.GENERAL;
         * System.out.println(FunctionalColorGroupEnum.YELLOW.equals(null));
         * if (FunctionalColorGroupEnum.YELLOW.equals(functionalGroup)) {
         * channelPreID = DsChannelTypeProvider.LIGHT;
         * System.out.println(channelPreID);
         * }
         * System.out.println(channelPreID);
         * if (functionalGroup.equals(FunctionalColorGroupEnum.GREY)) {
         * if (outputMode.equals(OutputModeEnum.POSITION_CON)) {
         * System.out.println(DsChannelTypeProvider.SHADE);
         * }
         * if (outputMode.equals(OutputModeEnum.POSITION_CON_US)) {
         * System.out.println(DsChannelTypeProvider.SHADE + DsChannelTypeProvider.ANGLE);
         * }
         * }
         * if (functionalGroup.equals(FunctionalColorGroupEnum.BLUE)) {
         * channelPreID = DsChannelTypeProvider.HEATING;
         * if (OutputModeEnum.outputModeIsTemperationControlled(outputMode)) {
         * System.out.println(channelPreID + DsChannelTypeProvider.TEMPERATURE_CONTROLLED);
         * }
         * }
         * if (OutputModeEnum.outputModeIsSwitch(outputMode)) {
         * System.out.println(channelPreID + DsChannelTypeProvider.SWITCH);
         * }
         * if (OutputModeEnum.outputModeIsDimmable(outputMode)) {
         * System.out.println(channelPreID + DsChannelTypeProvider.DIMMER);
         * }
         * System.out.println(!channelPreID.equals(DsChannelTypeProvider.HEATING));
         * if (!channelPreID.equals(DsChannelTypeProvider.HEATING)) {
         * if (outputMode.equals(OutputModeEnum.COMBINED_2_STAGE_SWITCH)) {
         * System.out.println(channelPreID + 2 + DsChannelTypeProvider.STAGE);
         * }
         * if (outputMode.equals(OutputModeEnum.COMBINED_2_STAGE_SWITCH)) {
         * System.out.println(channelPreID + 3 + DsChannelTypeProvider.STAGE);
         * }
         * }
         *
         * System.out.println(DsChannelTypeProvider.getOutputChannelTypeID(functionalGroup, outputMode));
         */
        /*
         * if (connMan.checkConnection()) {
         * JsonObject response = connMan.getDigitalSTROMAPI().query2(connMan.getSessionToken(), LAST_CALL_SCENE_QUERY);
         * System.out.println(response.toString());
         * if (response.isJsonObject()) {
         * for (Entry<String, JsonElement> entry : response.entrySet()) {
         * if (entry.getValue().isJsonObject()) {
         * JsonObject zone = entry.getValue().getAsJsonObject();
         * // System.out.println(entry.getValue().toString());
         * int zoneID = -1;
         * short groupID = -1;
         * short sceneID = -1;
         * if (zone.get(JSONApiResponseKeysEnum.ZONE_ID.getKey()) != null) {
         * zoneID = zone.get(JSONApiResponseKeysEnum.ZONE_ID.getKey()).getAsInt();
         * }
         * for (Entry<String, JsonElement> groupEntry : zone.entrySet()) {
         * if (groupEntry.getKey().startsWith("group") && groupEntry.getValue().isJsonObject()) {
         * JsonObject group = groupEntry.getValue().getAsJsonObject();
         * if (group.get(JSONApiResponseKeysEnum.DEVICES.getKey()) != null) {
         * // System.out.println(group.toString());
         * if (group.get(JSONApiResponseKeysEnum.GROUP.getKey()) != null) {
         * groupID = group.get(JSONApiResponseKeysEnum.GROUP.getKey()).getAsShort();
         * }
         * if (group.get(JSONApiResponseKeysEnum.LAST_CALL_SCENE.getKey()) != null) {
         * sceneID = group.get(JSONApiResponseKeysEnum.LAST_CALL_SCENE.getKey())
         * .getAsShort();
         * }
         * if (zoneID > -1 && groupID > -1 && sceneID > -1) {
         * System.out.println(zoneID + "-" + groupID + "-" + sceneID);
         * }
         * }
         * }
         * }
         * }
         * }
         * }
         * }
         */
        /*
         * List<SensorEnum> deviceTypes = new ArrayList<SensorEnum>();
         * deviceTypes.add(SensorEnum.ACTIVE_POWER);
         * deviceTypes.add(SensorEnum.AIR_PRESSURE);
         * int index = deviceTypes.indexOf(SensorEnum.ACTIVE_POWER);
         * if (index < 0) {
         * deviceTypes.add(SensorEnum.ACTIVE_POWER);
         * }
         * System.out.println(deviceTypes.toString());
         *
         * List<DeviceSensorValue> deviceSensorValues = new ArrayList<DeviceSensorValue>();
         * deviceSensorValues.add(new DeviceSensorValue(SensorEnum.ACTIVE_POWER, (short) 3));
         * deviceSensorValues.add(new DeviceSensorValue(SensorEnum.OUTPUT_CURRENT, (short) 4));
         * deviceSensorValues.add(new DeviceSensorValue(SensorEnum.ELECTRIC_METER, (short) 5));
         * DeviceSensorValue devSenVal = new DeviceSensorValue(SensorEnum.ELECTRIC_METER, (short) 3);
         * devSenVal.setDsValue(14);
         * index = deviceSensorValues.indexOf(devSenVal);
         * if (index < 0) {
         * deviceSensorValues.add(devSenVal);
         * } else {
         * deviceSensorValues.get(index).setDsValue(devSenVal.getDsValue());
         * }
         * System.out.println(deviceSensorValues.toString());
         * devSenVal = new DeviceSensorValue(SensorEnum.ELECTRIC_METER, (short) 3);
         * devSenVal.setDsValue(18);
         * index = deviceSensorValues.indexOf(devSenVal);
         * if (index < 0) {
         * deviceSensorValues.add(devSenVal);
         * } else {
         * deviceSensorValues.get(index).setDsValue(devSenVal.getDsValue());
         * }
         * System.out.println(deviceSensorValues.toString());
         * for (DeviceSensorValue sensorVal : deviceSensorValues) {
         * if (sensorVal.getSensorType().equals(SensorEnum.ELECTRIC_METER)) {
         * System.out.println(sensorVal.getSensorIndex());
         * System.out.println(sensorVal.equals(SensorEnum.ELECTRIC_METER));
         * }
         * }
         *
         * index = deviceSensorValues.indexOf(SensorEnum.ACTIVE_POWER);
         * if (index > -1) {
         * System.out.println(deviceSensorValues.get(index).getSensorIndex());
         * }
         * Object obj = new String[] { "a", "b" };
         * System.out.println(obj.toString());
         */
        if (testType.equals(DEVICE_QUERY2)) {
            final String GET_DETAILD_DEVICES = "/apartment/zones/zone0(*)/devices/*(*)/*(*)/*(*)";
            // if (connMan.checkConnection()) {
            JsonObject result = connMan.getDigitalSTROMAPI().query2(connMan.getSessionToken(), GET_DETAILD_DEVICES);
            // System.out.println(result.toString());
            if (result.isJsonObject()) {
                if (result.getAsJsonObject().get("zone0").isJsonObject()) {
                    result = result.getAsJsonObject().get("zone0").getAsJsonObject();
                    for (Entry<String, JsonElement> entry : result.entrySet()) {
                        if (!(entry.getKey().equals(JSONApiResponseKeysEnum.ZONE_ID.getKey())
                                && entry.getKey().equals(JSONApiResponseKeysEnum.NAME.getKey()))
                                && entry.getValue().isJsonObject()) {
                            Device device = new DeviceImpl(entry.getValue().getAsJsonObject());
                            // device.setSensorDataRefreshPriority(Config.REFRESH_PRIORITY_HIGH,
                            // Config.REFRESH_PRIORITY_LOW, Config.REFRESH_PRIORITY_MEDIUM);
                            /*
                             * if ((device.isSensorDevice()
                             * && DigitalSTROMBindingConstants.THING_TYPE_ID_DS_I_SENSE_200_DEVICE
                             * .equals(device.getHWinfo()))
                             * || (DigitalSTROMBindingConstants.THING_TYPE_ID_DS_I_SENSE_200_DEVICE
                             * .equals(device.getHWinfo().substring(0, 2))
                             * && device.isDeviceWithOutput() && device.isPresent())) {
                             */
                            System.out.println(device.toString());
                            // }
                        }
                    }
                }
            }
            // }

        }

        if (testType.equals(PARSE_TEST))

        {

            HashMap<String, String> propertries = new HashMap<String, String>();
            propertries.put(DigitalSTROMBindingConstants.DEVICE_SCENE + "1", "test1");
            propertries.put(DigitalSTROMBindingConstants.DEVICE_SCENE + "2", "test2");
            propertries.put(DigitalSTROMBindingConstants.DEVICE_SCENE + "3", "test3");
            propertries.put("4", "test4");
            propertries.put("5", "test5");
            for (String key : propertries.keySet()) {
                if (key.startsWith(DigitalSTROMBindingConstants.DEVICE_SCENE)) {
                    System.out.println(Short.parseShort((String) key
                            .subSequence(DigitalSTROMBindingConstants.DEVICE_SCENE.length(), key.length())));
                }
            }
            final String LINE_SEPERATOR = System.getProperty("line.separator");
            String parseTest = "10 = Scene: PRESET_4, dontcare: false, localPrio: false, specialMode: false, flashMode: false, sceneValue: 0\n"
                    + "5 = , sceneValue: 0" + "\n"
                    + "8 = Scene: PRESET_4, dontcare: false, localPrio: false, specialMode: false, flashMode: false, sceneValue: 0\n";
            System.out.println(parseTest);
            // String[] scenes1 = parseTest.split("\n");
            String newSceneConfigurations = "";
            if (parseTest.contains("5 = ")) {
                int startIndex = parseTest.indexOf("5 = ");
                int endIndex = parseTest.indexOf("\n", startIndex) + 1;
                System.out.println(startIndex + ", " + endIndex);
                String firstPart = (String) parseTest.subSequence(0, startIndex);
                System.out.println("firstPart: " + firstPart);
                String secondPart = (String) parseTest.subSequence(endIndex, parseTest.length());
                System.out.println("secondPart: " + secondPart);
                newSceneConfigurations = firstPart
                        + "5 = Scene: PRESET_0, dontcare: false, localPrio: false, specialMode: false, flashMode: false, sceneValue: 0"
                        + "\n" + secondPart;
                System.out.println("old Config: \n" + parseTest + "\nnew Config\n" + newSceneConfigurations);
            }
            /*
             * for (int i = 0; i < scenes1.length; i++) {
             * System.out.println("Scene gesplittet:\n" + scenes1[i].replaceAll(" ", "").split("=")[1]);
             * String[] sceneIdToConfig = scenes1[i].replaceAll(" ", "").split("=");
             * if (sceneIdToConfig[0].equals("5")) {
             * String newSceneConfig =
             * "Scene: PRESET_0, dontcare: false, localPrio: false, specialMode: false, flashMode: false"
             * + sceneIdToConfig[1];
             * for (int j = 0; j < scenes1.length; j++) {
             * if (scenes1[j].startsWith("5")) {
             * newSceneConfigurations = "5 = " + newSceneConfigurations + newSceneConfig + "\n";
             * } else {
             * newSceneConfigurations = newSceneConfigurations + scenes1[j] + "\n";
             * }
             * }
             * }
             * }
             */
            System.out.println("new SceneConfig:\n" + newSceneConfigurations);
            String[] scenes = newSceneConfigurations.split("\n");
            for (int i = 0; i < scenes.length; i++) {
                System.out.println("Scene gesplittet:\n" + scenes[i].replaceAll(" ", "").split("=")[1]);
                String[] sceneIdToConfig = scenes[i].replaceAll(" ", "").split("=");
                String[] sceneParm = sceneIdToConfig[1].replace(" ", "").split(",");
                JSONDeviceSceneSpecImpl sceneSpecNew = null;
                int sceneValue = -1;
                int sceneAngle = -1;
                for (int j = 0; j < sceneParm.length; j++) {
                    String[] sceneParmSplit = sceneParm[j].split(":");
                    System.out.println(sceneParm[j]);
                    switch (sceneParmSplit[0]) {
                        case "Scene":
                            sceneSpecNew = new JSONDeviceSceneSpecImpl(sceneParmSplit[1]);
                            break;
                        case "dontcare":
                            System.out.println(Boolean.parseBoolean(sceneParmSplit[1]));
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
                System.out.println("Scene geparste:\n" + sceneSpecNew.toString() + ", sceneValue: " + sceneValue);
            }
        }

        if (testType.equals(EVENT_LISTENER)) {
            class DummyEventHandler implements EventHandler {
                private List<String> supportetEvents = null;

                public DummyEventHandler(List<String> supportetEvents) {
                    this.supportetEvents = supportetEvents;
                }

                @Override
                public void handleEvent(EventItem eventItem) {
                    System.out.println("handling event: " + eventItem.toString());
                }

                @Override
                public List<String> getSupportetEvents() {
                    return supportetEvents;
                }

                @Override
                public boolean supportsEvent(String eventName) {
                    return supportetEvents.contains(eventName);
                }

                @Override
                public String getUID() {
                    return this.getClass().getSimpleName() + "-" + supportetEvents.toString();
                }

                @Override
                public void setEventListener(EventListener eventListener) {

                }

                @Override
                public void unsetEventListener(EventListener eventListener) {

                }

            }
            EventListener eventListener = new EventListener(connMan, new DummyEventHandler(
                    Lists.newArrayList(EventNames.CALL_SCENE, EventNames.UNDO_SCENE, EventNames.DEVICE_SENSOR_VALUE)));
            eventListener.start();

            new Thread(new Runnable() {
                boolean subscribed = false;
                private List<String> subscribedEvents = Lists.newArrayList("deviceBinaryInputEvent");

                @Override
                public void run() {
                    while (true) {
                        // if (connMan.checkConnection()) {

                        if (subscribed) {
                            String response = connMan.getDigitalSTROMAPI().getEvent(connMan.getSessionToken(), 12, 500);
                            System.out.println(response);
                            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                            if (JSONResponseHandler.checkResponse(responseObj)) {
                                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                                if (obj != null && obj.get(JSONApiResponseKeysEnum.EVENTS.getKey()).isJsonArray()) {
                                    JsonArray array = obj.get(JSONApiResponseKeysEnum.EVENTS.getKey()).getAsJsonArray();
                                    try {
                                        if (array.size() > 0) {
                                            Event event = new JSONEventImpl(array);
                                            for (EventItem item : event.getEventItems()) {
                                                // for (EventHandler handler : eventHandlers) {
                                                // if (handler.supportsEvent(item.getName())) {
                                                System.out.println(
                                                        ("inform handler with id {} about event {}" + item.toString()));
                                                // Integer zoneID = Integer
                                                // .parseInt(item.getProperties().get(EventResponseEnum.ZONEID));
                                                // TemperatureControlStatus temperationControlStatus = connMan
                                                // .getDigitalSTROMAPI().getZoneTemperatureControlStatus(
                                                // connMan.getSessionToken(), zoneID, null);
                                                // System.out.println(
                                                // "readout new temperationControlStatus, new
                                                // temperationControlStatus
                                                // is: "
                                                // + temperationControlStatus.toString());
                                                // handler.handleEvent(item);
                                                // }
                                                // }
                                            }
                                        }
                                    } catch (Exception e) {
                                        System.out.printf("An Exception occurred", e);
                                    }
                                }
                            } else {
                                String errorStr = null;
                                if (responseObj != null
                                        && responseObj.get(JSONApiResponseKeysEnum.MESSAGE.getKey()) != null) {
                                    errorStr = responseObj.get(JSONApiResponseKeysEnum.MESSAGE.getKey()).getAsString();
                                }
                                if (errorStr != null) {
                                    // unsubscribe();
                                    // subscribe();
                                } else if (errorStr != null) {
                                    // pollingScheduler.cancel(true);
                                    System.out.println("Unknown error message at event response: " + errorStr);
                                }
                            }
                        } else {
                            // if (connMan.checkConnection()) {
                            connMan.getDigitalSTROMAPI().unsubscribeEvent(connMan.getSessionToken(), null, 12,
                                    Config.DEFAULT_CONNECTION_TIMEOUT, Config.DEFAULT_READ_TIMEOUT);
                            for (String eventName : this.subscribedEvents) {
                                subscribed = connMan.getDigitalSTROMAPI().subscribeEvent(connMan.getSessionToken(),
                                        eventName, 12, Config.DEFAULT_CONNECTION_TIMEOUT, Config.DEFAULT_READ_TIMEOUT);
                                System.out.println(eventName + " subscribe sucsess? " + subscribed);
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            // }
                        }
                        // } else {
                        // System.out.println("no connection");
                        // }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }).start();
        }
        if (testType.equals(JSON_API_HEATING)) {
            DsAPI api = connMan.getDigitalSTROMAPI();
            HttpTransport transport = connMan.getHttpTransport();
            // if (connMan.checkConnection()) {
            System.out.println("APARTMENT:");
            System.out.println("getTemperatureControlStatus");
            List<TemperatureControlStatus> tempContStatList = new LinkedList<TemperatureControlStatus>(
                    api.getApartmentTemperatureControlStatus(connMan.getSessionToken()));
            for (TemperatureControlStatus tempContStat : tempContStatList) {
                if (tempContStat.getIsConfigured()) {
                    System.out.println(tempContStat);
                }
            }
            System.out.println("getTemperatureControlConfiguration");
            List<TemperatureControlConfig> tempContConfList = new LinkedList<TemperatureControlConfig>(
                    api.getApartmentTemperatureControlConfig(connMan.getSessionToken()).values());
            for (TemperatureControlConfig tempContConf : tempContConfList) {
                if (tempContConf.getIsConfigured()) {
                    System.out.println(tempContConf);
                }
            }
            System.out.println("getTemperatureControlValues");
            List<TemperatureControlValues> tempContValList = new LinkedList<TemperatureControlValues>(
                    api.getApartmentTemperatureControlValues(connMan.getSessionToken()).values());
            for (TemperatureControlValues tempContVal : tempContValList) {
                if (tempContVal.getIsConfigured()) {
                    System.out.println(tempContVal);
                }
            }
            System.out.println("getAssignedSensors");
            List<AssignedSensors> assignedSensorList = new LinkedList<AssignedSensors>(
                    api.getApartmentAssignedSensors(connMan.getSessionToken()).values());
            // System.out.println(assignedSensorList);
            for (AssignedSensors assignedSensor : assignedSensorList) {
                if (assignedSensor.existsAssignedSensors()) {
                    System.out.println(assignedSensor);
                }
            }
            System.out.println("getSensorValues");
            List<BaseSensorValues> sensorValuesList = new LinkedList<BaseSensorValues>(
                    api.getApartmentSensorValues(connMan.getSessionToken()).values());
            for (BaseSensorValues sensorValues : sensorValuesList) {
                if (sensorValues.existSensorValues()) {
                    System.out.println(sensorValues);
                    System.out.println("Classname: " + sensorValues.getClass().getSimpleName());

                }
            }

            System.out.println("\nZONE:");
            Integer zoneID = 25105;

            System.out.println("getTemperatureControlStatus");
            System.out.println(api.getZoneTemperatureControlStatus(connMan.getSessionToken(), zoneID, null));

            System.out.println("getTemperatureControlConfiguration");
            System.out.println(api.getZoneTemperatureControlConfig(connMan.getSessionToken(), zoneID, null));

            System.out.println("getTemperatureControlValues");
            System.out.println(api.getZoneTemperatureControlValues(connMan.getSessionToken(), zoneID, null));

            System.out.println("getTemperatureControlInternals");
            System.out.println(api.getZoneTemperatureControlInternals(connMan.getSessionToken(), zoneID, null));

            System.out.println("getAssignedSensors");
            System.out.println(api.getZoneAssignedSensors(connMan.getSessionToken(), zoneID, null));

            System.out.println("getSensorValues");
            System.out.println(api.getZoneSensorValues(connMan.getSessionToken(), zoneID, null));

            // }
        }

    }

}
