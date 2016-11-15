package org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.BaseSensorValues;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.AssignedSensors;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlConfig;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlStatus;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlValues;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.EventHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.EventListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.constants.EventNames;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.types.EventItem;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.impl.ConnectionManagerImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.SensorEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.DeviceSensorValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.JSONDeviceSceneSpecImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.impl.DeviceImpl;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TestApi {

    public static void main(String[] args) {
        String host = "fsqdss1.inf.fh-koeln.de";
        String host1 = "testrack2.aizo.com:58080";
        String user = "dssadmin";
        String pw = "5Kp1_i1B";

        final String JSON_API_HEATING = "APIheating";
        final String EVENT_LISTENER = "eventListener";
        final String PARSE_TEST = "parseTest";
        final String DEVICE_QUERY2 = "devQ2";
        String testType = DEVICE_QUERY2;

        ConnectionManager connMan = new ConnectionManagerImpl(host, user, pw, false);

        List<SensorEnum> deviceTypes = new ArrayList<SensorEnum>();
        deviceTypes.add(SensorEnum.ACTIVE_POWER);
        deviceTypes.add(SensorEnum.AIR_PRESSURE);
        int index = deviceTypes.indexOf(SensorEnum.ACTIVE_POWER);
        if (index < 0) {
            deviceTypes.add(SensorEnum.ACTIVE_POWER);
        }
        System.out.println(deviceTypes.toString());

        List<DeviceSensorValue> deviceSensorValues = new ArrayList<DeviceSensorValue>();
        deviceSensorValues.add(new DeviceSensorValue(SensorEnum.ACTIVE_POWER, (short) 3));
        deviceSensorValues.add(new DeviceSensorValue(SensorEnum.OUTPUT_CURRENT, (short) 4));
        deviceSensorValues.add(new DeviceSensorValue(SensorEnum.ELECTRIC_METER, (short) 5));
        DeviceSensorValue devSenVal = new DeviceSensorValue(SensorEnum.ELECTRIC_METER, (short) 3);
        devSenVal.setDsValue(14);
        index = deviceSensorValues.indexOf(devSenVal);
        if (index < 0) {
            deviceSensorValues.add(devSenVal);
        } else {
            deviceSensorValues.get(index).setDsValue(devSenVal.getDsValue());
        }
        System.out.println(deviceSensorValues.toString());
        devSenVal = new DeviceSensorValue(SensorEnum.ELECTRIC_METER, (short) 3);
        devSenVal.setDsValue(18);
        index = deviceSensorValues.indexOf(devSenVal);
        if (index < 0) {
            deviceSensorValues.add(devSenVal);
        } else {
            deviceSensorValues.get(index).setDsValue(devSenVal.getDsValue());
        }
        System.out.println(deviceSensorValues.toString());
        for (DeviceSensorValue sensorVal : deviceSensorValues) {
            if (sensorVal.getSensorType().equals(SensorEnum.ELECTRIC_METER)) {
                System.out.println(sensorVal.getSensorIndex());
                System.out.println(sensorVal.equals(SensorEnum.ELECTRIC_METER));
            }
        }

        index = deviceSensorValues.indexOf(SensorEnum.ACTIVE_POWER);
        if (index > -1) {
            System.out.println(deviceSensorValues.get(index).getSensorIndex());
        }
        Object obj = new String[] { "a", "b" };
        System.out.println(obj.toString());

        if (testType.equals(DEVICE_QUERY2)) {
            final String GET_DETAILD_DEVICES = "/apartment/zones/zone0(*)/devices/*(*)/*(*)/*(*)";
            if (connMan.checkConnection()) {
                JsonObject result = connMan.getDigitalSTROMAPI().query2(connMan.getSessionToken(), GET_DETAILD_DEVICES);
                System.out.println(result.toString());
                if (result.isJsonObject()) {
                    if (result.getAsJsonObject().get("zone0").isJsonObject()) {
                        result = result.getAsJsonObject().get("zone0").getAsJsonObject();
                        for (Entry<String, JsonElement> entry : result.entrySet()) {
                            if (!(entry.getKey().equals(JSONApiResponseKeysEnum.ZONE_ID.getKey())
                                    && entry.getKey().equals(JSONApiResponseKeysEnum.NAME.getKey()))
                                    && entry.getValue().isJsonObject()) {
                                Device dev = new DeviceImpl(entry.getValue().getAsJsonObject());
                                System.out.println(dev.toString());
                            }
                        }
                    }
                }
            }
            System.out.println(Date.from(Instant.ofEpochMilli(System.currentTimeMillis())));
        }

        if (testType.equals(PARSE_TEST)) {

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

            }
            EventListener eventListener = new EventListener(connMan,
                    new DummyEventHandler(Lists.newArrayList(EventNames.CALL_SCENE, EventNames.UNDO_SCENE)));
            eventListener.start();
        }
        if (testType.equals(JSON_API_HEATING)) {
            DsAPI api = connMan.getDigitalSTROMAPI();
            HttpTransport transport = connMan.getHttpTransport();
            if (connMan.checkConnection()) {
                System.out.println("APARTMENT:");
                System.out.println("getTemperatureControlStatus");
                List<TemperatureControlStatus> tempContStatList = new LinkedList<TemperatureControlStatus>(
                        api.getApartmentTemperatureControlStatus(connMan.getSessionToken()).values());
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

            }
        }

    }

}
