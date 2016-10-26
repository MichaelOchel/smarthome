/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants;

/**
 * The {@link JSONApiResponseKeysEnum} contains all digitalSTROM-JSON response keys.
 *
 * @author Alexander Betker
 * @version digitalSTROM-API 1.14.5
 */
public enum JSONApiResponseKeysEnum {

    OK("ok"),
    MESSAGE("message"),

    RESULT("result"),

    NAME("name"),
    ID("id"),

    CONSUMPTION("consumption"),
    APARTMENT("apartment"),
    ZONES("zones"),
    IS_PRESENT("isPresent"),
    DEVICES("devices"),
    GROUPS("groups"),

    CIRCUITS("circuits"),

    REACHABLE_SCENES("reachableScenes"),

    IS_ON("isOn"),
    HAS_TAG("hasTag"),
    TAGS("tags"),
    CLASS("class"),
    INDEX("index"),
    VALUE("value"),
    SCENE_ID("sceneID"),
    DONT_CARE("dontCare"),
    LOCAL_PRIO("localPrio"),
    SPECIAL_MODE("specialMode"),
    FLASH_MODE("flashMode"),
    LEDCON_INDEX("ledconIndex"),
    DIM_TIME_INDEX("dimtimeIndex"),
    UP("up"),
    DOWN("down"),

    COLOR_SELECT("colorSelect"),
    MODE_SELECT("modeSelect"),
    DIM_MODE("dimMode"),
    RGB_MODE("rgbMode"),
    GROUP_COLOR_MODE("groupColorMode"),

    SENSOR_VALUE("sensorValue"),
    TYPE_TYPE("sensorType"),
    SENSOR_INDEX("sensorIndex"),

    EVENT_INDEX("eventIndex"),
    EVENT_NAME("eventName"),
    IS_SCENE_DEVICE("isSceneDevice"),
    TEST("test"),
    ACTION("action"),
    HYSTERSIS("hysteresis"),
    VALIDITY("validity"),

    // IDs
    DSID("dSID"),
    DSUID("dSUID"),
    DSID_LOWER_CASE("dsid"),
    METER_DSID("meterDSID"),
    ZONE_ID("ZoneID"),
    ZONE_ID_UPPER_Z("zoneID"),

    FUNCTION_ID("functionID"),
    PRODUCT_REVISION("productRevision"),
    PRODUCT_ID("productID"),
    HW_INFO("hwInfo"),
    ON("on"),
    OUTPUT_MODE("outputMode"),
    BUTTON_ID("buttonID"),

    // DeviceSpec
    REVISION_ID("revisionID"),

    EVENTS("events"),
    PROPERTIES("properties"),
    SOURCE("source"),

    DS_METERS("dSMeters"),

    PRESENT("present"),

    POWER_CONSUMPTION("powerConsumption"),
    ENERGY_METER_VALUE("energyMeterValue"),
    ENERGY_METER_VALUE_WS("energyMeterValueWs"),

    // Group
    METER_VALUE("meterValue"),
    TYPE("type"),

    VERSION("version"),
    TIME("time"),
    TOKEN("token"),
    APPLICATION_TOKEN("applicationToken"),

    SELF("self"),

    GROUP_ID("groupID"),

    RESOLUTIONS("resolutions"),
    RESOLUTION("resolution"),
    SERIES("series"),
    METER_ID("meterID"),
    UNIT("unit"),
    VALUES("values"),

    DATE("date"),

    IS_CONFIGURED("IsConfigured"),
    CONTROL_MODE("ControlMode"),
    CONTROL_STATE("ControlState"),
    CONTROL_DSUID("ControlDSUID"),
    OPERATION_MODE("OperationMode"),
    TEMPERATURE_VALUE("TemperatureValue"),
    NOMINAL_VALUE("NominalValue"),
    CONTROL_VALUE("ControlValue"),
    TEMPERATURE_VALUE_TIME("TemperatureValueTime"),
    NOMINAL_VALUE_TIME("NominalValueTime"),
    CONTROL_VALUE_TIME("ControlValueTime"),
    CTRL_T_RECENT("CtrlTRecent"),
    CTRL_T_REFERENCE("CtrlTReference"),
    CTRL_T_ERROR("CtrlTError"),
    CTRL_T_ERROR_PREV("CtrlTErrorPrev"),
    CTRL_INTEGRAL("CtrlIntegral"),
    CTRL_YP("CtrlYp"),
    CTRL_YI("CtrlYi"),
    CTRL_YD("CtrlYd"),
    CTRL_Y("CtrlY"),
    CTRL_ANTI_WIND_UP("CtrlAntiWindUp"),
    REFERENCE_ZONE("ReferenceZone"),
    CTRL_OFFSET("CtrlOffset"),
    EMERGENCY_VALUE("EmergencyValue"),
    CTRL_KP("CtrlKp"),
    CTRL_TS("CtrlTs"),
    CTRL_TI("CtrlTi"),
    CTRL_KD("CtrlKd"),
    CTRL_MIN("CtrlImin"),
    CTRL_MAX("CtrlImax"),
    CTRL_Y_MIN("CtrlYmin"),
    CTRL_Y_MAX("CtrlYmax"),
    CTRL_KEEP_FLOOR_WARM("CtrlKeepFloorWarm"),
    SENSOR_TYPE("sensorType"),
    DSUID_LOWER_CASE("dsuid"),
    TEMPERATION_VALUE("TemperatureValue"),
    TEMPERATION_VALUE_TIME("TemperatureValueTime"),
    HUMIDITY_VALUE("HumidityValue"),
    HUMIDITY_VALUE_TIME("HumidityValueTime"),
    BRIGHTNESS_VALUE("BrightnessValue"),
    BRIGHTNESS_VALUE_TIME("BrightnessValueTime"),
    CO2_CONCENTRATION_VALUE("CO2ConcentrationValue"),
    CO2_CONCENTRATION_VALUE_TIME("CO2ConcentrationValueTime"),
    SENSORS("sensors"),
    WEATHER_ICON_ID("WeatherIconId"),
    WEATHER_CONDITION_ID("WeatherConditionId"),
    WEATHER_SERVICE_ID("WeatherServiceId"),
    WEATHER_SERVICE_TIME("WeatherServiceTime");

    private final String key;

    private JSONApiResponseKeysEnum(String key) {
        this.key = key;
    }

    /**
     * Returns the key.
     *
     * @return key
     */
    public String getKey() {
        return key;
    }

}
