/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters;

import java.util.HashMap;

/**
 *
 * @author Michael Ochel
 * @author Matthias Siegele
 * @since 1.3.0
 * @version digitalSTROM-API 1.14.5
 */
public enum SensorEnum {
    /*
     * | Sensor Type | Description | Unit | Min | 12 Bit Max | 12 Bit Resolution |
     * -----------------------------------------------------------------------------------------------------------------
     * -------------------------------------------------------------
     * | 4 | Active power | Watts (W) | 0 | 4095 | 1 |
     * | 5 | Output current | Ampere (mA) | 0 | 4095 | 1 |
     * | 6 | Electric meter | Kilowatt hours (kWh) | 0 | 40,95 | 0,01 |
     * | 9 | Temperature indoors | Kelvin (K) | 230 | 332,375 | 0,25 |
     * | 10 | Temperature outdoors | Kelvin (K) | 230 | 332,375 | 0,25 |
     * | 11 | Brightness indoors | Lux (Lx) | 1 | 131446,795 | logarithmic: lx = 10 * (x/800), x = 800 * log(lx) |
     * | 12 | Brightness outdoors | Lux (Lx) | 1 | 131446,795 | logarithmic: lx = 10 * (x/800), x = 800 * log(lx) |
     * | 13 | Relative humidity indoors | Percent (%) | 0 | 102,375 | 0,025 |
     * | 14 | Relative humidity outdoors | Percent (%) | 0 | 102,375 | 0,025 |
     * | 15 | Air pressure | Pascal (hPa) | 200 | 1223,75 | 0,25 |
     * | 18 | Wind speed | Meters per second (m/s) | 0 | 102,375 | 0,025 |
     * | 19 | Wind direction | degrees | 0 | 511,875 | 0,54 |
     * | 20 | Precipitation | Millimeter per square meter (mm/m2) | 0 | 102,375 | 0,025 |
     * | 21 | Carbon Dioxide | Parts per million (ppm) | 1 | 131446,795 | logarithmic: ppm = 10 * (x/800), x = 800 * log
     * (ppm) |
     * | 25 | Sound pressure level | Decibel (dB) | 0 | 255,938 | 0,25/4 |
     * | 50 | Room temperature set point | Kelvin (K) | 230 | 332,375 | 0,025 |
     * | 51 | Room temperature control variable | Percent (%) | 0 | 102,375 | 0,025 |
     * | 64 | Output current (H) | Ampere (mA) | 0 | 16380 | 4 |
     * | 65 | Power consumption | Volt-Ampere (VA) | 0 | 4095 | 1 |
     */
    ACTIVE_POWER(4, "Watts", "W"),
    OUTPUT_CURRENT(5, "Ampere", "mA"),
    ELECTRIC_METER(6, "Kilowatt hours)", "kWh"),
    TEMPERATURE_INDOORS(9, "Kelvin", "K"),
    TEMPERATURE_OUTDOORS(10, "Kelvin", "K"),
    BRIGHTNESS_INDOORS(11, "Lux", "Lx"),
    BRIGHTNESS_OUTDOORS(12, "Lux", "Lx"),
    RELATIVE_HUMIDITY_INDOORS(13, "Percent", "%"),
    RELATIVE_HUMIDITY_OUTDOORS(14, "Percent", "%"),
    AIR_PRESSURE(15, "Pascal", "hPa"),
    WIND_SPEED(18, "Meters per second", "m/s"),
    PRECIPITATION(20, "Millimeter per square meter", "mm/m2"),
    CARBONE_DIOXIDE(21, "Parts per million", "ppm"),
    SOUND_PRESSURE_LEVEL(25, "Decibel", "dB"),
    ROOM_TEMPERATION_SET_POINT(50, "Kelvin", "K"),
    ROOM_TEMPERATION_CONTROL_VARIABLE(51, "Kelvin", "K"),
    OUTPUT_CURRENT_H(64, "Ampere", "mA"),
    POWER_CONSUMPTION(65, "Volt-Ampere", "VA");

    private final int sensorType;
    private final String unit;
    private final String unitShortcut;

    static final HashMap<Integer, SensorEnum> sensorEnums = new HashMap<Integer, SensorEnum>();

    static {
        for (SensorEnum sensor : SensorEnum.values()) {
            sensorEnums.put(sensor.getSensorType(), sensor);
        }
    }

    public static boolean containsSensor(Integer index) {
        return sensorEnums.keySet().contains(index);
    }

    public static SensorEnum getSensor(Integer index) {
        return sensorEnums.get(index);
    }

    SensorEnum(int sensorType, String unit, String unitShortcut) {
        this.sensorType = sensorType;
        this.unit = unit;
        this.unitShortcut = unitShortcut;
    }

    public int getSensorType() {
        return this.sensorType;
    }

    public String getUnit() {
        return this.unit;
    }

    public String getUnitShortcut() {
        return this.unitShortcut;
    }

}
