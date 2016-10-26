package org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.BaseSensorValues;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.AssignedSensors;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlConfig;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlStatus;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlValues;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.impl.ConnectionManagerImpl;

public class TestApi {

    public static void main(String[] args) {
        String host = "fsqdss1.inf.fh-koeln.de";
        String host1 = "testrack2.aizo.com:58080";
        String user = "dssadmin";
        String pw = "5Kp1_i1B";

        ConnectionManager connMan = new ConnectionManagerImpl(host1, user, user, false);
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
