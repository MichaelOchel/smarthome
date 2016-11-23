/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.FunctionalColorGroupEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringUnitsEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.OutputModeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.SensorEnum;
import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * The {@link DsChannelTypeProvider} implements the {@link ChannelTypeProvider} generates all supported
 * {@link Channel}'s for digitalSTROM.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class DsChannelTypeProvider implements ChannelTypeProvider {

    private final List<String> SUPPORTED_CHANNEL_TYPES = Lists.newArrayList(
            DigitalSTROMBindingConstants.CHANNEL_ID_SCENE, DigitalSTROMBindingConstants.CHANNEL_ID_TOTAL_ACTIVE_POWER,
            DigitalSTROMBindingConstants.CHANNEL_ID_TOTAL_ELECTRIC_METER);

    /*
     * output channels dynamic?
     * dimmer / light, general, heating
     * switch / light, general, heating
     * 2/3 stage / light, general
     * scene
     * shade / shade
     * shade angle / shade
     * temperature controlled
     * wipe / general
     */
    private I18nProvider i18n = null;
    private Bundle bundle = null;

    // channelID building (effect group type + (nothing || item type || extended item type) e.g. lightSwitch, shade or
    // shadeAngle
    // channel effect group type
    public final static String LIGHT = "light"; // and tag
    public final static String SHADE = "shade"; // and tag
    public final static String HEATING = "heating"; // and tag
    public final static String GENERAL = "general";
    public final static String SCENE = "scene";
    // channel extended item type
    public final static String WIPE = "Wipe";
    public final static String ANGLE = "Angle";
    public final static String STAGE = "Stage"; // pre stageses e.g. 2+STAGE_SWITCH
    public final static String TEMPERATURE_CONTROLLED = "TemperatureControlled";

    // item types
    public final static String DIMMER = "Dimmer";
    public final static String SWITCH = "Switch";
    public final static String ROLLERSHUTTER = "Rollershutter";
    public final static String STRING = "String";
    public final static String NUMBER = "Number";

    public final static String TOTAL_PRE = "total_";

    // tags
    private final String GE = "GE";
    private final String GR = "GR";
    private final String BL = "BL";
    private final String SW = "SW";
    private final String DS = "DS";
    private final String JOKER = "JOKER";
    // private final String BLIDNS = "shade";

    // categories
    private final String CATEGORY_BLINDES = "Blinds";
    private final String CATEGORY_DIMMABLE_LIGHT = "DimmableLight";
    private final String CATEGORY_CARBONE_DIOXIDE = "CarbonDioxide";
    private final String CATEGORY_ENERGY = "Energy";
    private final String CATEGORY_HUMIDITY = "Humidity";
    private final String CATEGORY_LIGHT = "Light";
    private final String CATEGORY_PRESSURE = "Pressure";
    private final String CATEGORY_SOUND_VOLUME = "SoundVolume";
    private final String CATEGORY_TEMPERATURE = "Temperature";
    private final String CATEGORY_WIND = "Wind";
    private final String CATEGORY_RAIN = "Rain";

    // rollershutter?
    // private final String CATEGORY_MOVE_CONTROL = "MoveControl";

    protected void activate(ComponentContext componentContext) {
        this.bundle = componentContext.getBundleContext().getBundle();
        init();
    }

    protected void deactivate(ComponentContext componentContext) {
        this.bundle = null;
    }

    protected void setI18nProvider(I18nProvider i18n) {
        this.i18n = i18n;
    };

    protected void unsetI18nProvider(I18nProvider i18n) {
        this.i18n = null;
    };

    private String getText(String key, Locale locale) {
        return i18n != null ? i18n.getText(bundle, key, i18n.getText(bundle, key, key, Locale.ENGLISH), locale) : key;
    }

    public static String getOutputChannelTypeID(FunctionalColorGroupEnum functionalGroup, OutputModeEnum outputMode) {
        String channelPreID = GENERAL;
        if (functionalGroup.equals(FunctionalColorGroupEnum.YELLOW)) {
            channelPreID = LIGHT;
        }
        if (functionalGroup.equals(FunctionalColorGroupEnum.GREY)) {
            if (outputMode.equals(OutputModeEnum.POSITION_CON)) {
                return SHADE;
            }
            if (outputMode.equals(OutputModeEnum.POSITION_CON_US)) {
                return SHADE + "_" + ANGLE.toLowerCase();
            }
        }
        if (functionalGroup.equals(FunctionalColorGroupEnum.BLUE)) {
            channelPreID = HEATING;
            if (OutputModeEnum.outputModeIsTemperationControlled(outputMode)) {
                return channelPreID + "_" + TEMPERATURE_CONTROLLED.toLowerCase();
            }
        }
        if (OutputModeEnum.outputModeIsSwitch(outputMode)) {
            return channelPreID + "_" + SWITCH.toLowerCase();
        }
        if (OutputModeEnum.outputModeIsDimmable(outputMode)) {
            return channelPreID + "_" + DIMMER.toLowerCase();
        }
        if (!channelPreID.equals(HEATING)) {
            if (outputMode.equals(OutputModeEnum.COMBINED_2_STAGE_SWITCH)) {
                return channelPreID + "_2_" + STAGE.toLowerCase();
            }
            if (outputMode.equals(OutputModeEnum.COMBINED_3_STAGE_SWITCH)) {
                return channelPreID + "_3_" + STAGE.toLowerCase();
            }
        }
        return null;
    }

    private static List<String> SUPPORTED_OUTPUT_CHANNEL_TYPES = new ArrayList<>();

    public static boolean isOutputChannel(String channelTypeID) {
        return SUPPORTED_OUTPUT_CHANNEL_TYPES.contains(channelTypeID);
    }

    private void init() {
        String channelIDpre = GENERAL;
        for (short i = 0; i < 3; i++) {
            if (i == 1) {
                channelIDpre = LIGHT;
            }
            if (i == 2) {
                channelIDpre = HEATING;
                SUPPORTED_OUTPUT_CHANNEL_TYPES.add(channelIDpre + "_" + TEMPERATURE_CONTROLLED.toLowerCase());
            }
            SUPPORTED_OUTPUT_CHANNEL_TYPES.add(channelIDpre + "_" + SWITCH.toLowerCase());
            SUPPORTED_OUTPUT_CHANNEL_TYPES.add(channelIDpre + "_" + DIMMER.toLowerCase());
            if (i < 2) {
                SUPPORTED_OUTPUT_CHANNEL_TYPES.add(channelIDpre + "_2_" + STAGE.toLowerCase());
                SUPPORTED_OUTPUT_CHANNEL_TYPES.add(channelIDpre + "_3_" + STAGE.toLowerCase());
            }
        }
        channelIDpre = SHADE;
        SUPPORTED_OUTPUT_CHANNEL_TYPES.add(channelIDpre);
        SUPPORTED_OUTPUT_CHANNEL_TYPES.add(channelIDpre + "_" + ANGLE.toLowerCase());
    }

    /*
     * LIGHT+SWITCH;
     *
     */
    private StateDescription getSensorStateDescription(String shortcutUnit) {
        return shortcutUnit.equals(SensorEnum.ELECTRIC_METER.getUnitShortcut())
                ? new StateDescription(null, null, null, "%.3f " + shortcutUnit, true, null)
                : new StateDescription(null, null, null, "%d " + shortcutUnit, true, null);
    }

    private String getSensorCategory(SensorEnum sensorType) {
        switch (sensorType) {
            case ACTIVE_POWER:
            case ELECTRIC_METER:
            case OUTPUT_CURRENT:
            case OUTPUT_CURRENT_H:
            case POWER_CONSUMPTION:
                return CATEGORY_ENERGY;
            case AIR_PRESSURE:
                return CATEGORY_PRESSURE;
            case CARBONE_DIOXIDE:
                return CATEGORY_CARBONE_DIOXIDE;
            case PRECIPITATION:
                return CATEGORY_RAIN;
            case RELATIVE_HUMIDITY_INDOORS:
            case RELATIVE_HUMIDITY_OUTDOORS:
                return CATEGORY_HUMIDITY;
            case ROOM_TEMPERATION_CONTROL_VARIABLE:
                break;
            case ROOM_TEMPERATION_SET_POINT:
                break;
            case TEMPERATURE_INDOORS:
                break;
            case TEMPERATURE_OUTDOORS:
                return CATEGORY_TEMPERATURE;
            case WIND_DIRECTION:
            case WIND_SPEED:
                return CATEGORY_WIND;
            case SOUND_PRESSURE_LEVEL:
                return CATEGORY_SOUND_VOLUME;
            // missing category
            case BRIGHTNESS_INDOORS:
            case BRIGHTNESS_OUTDOORS:
                break;
            default:
                break;

        }
        return null;
    }

    private StateDescription getSensorStateDescription(SensorEnum sensorType) {
        // the digitalSTROM resolution for temperature in kelvin is not correct but sensor-events and cached values are
        // shown in °C so we will use this unit for temperature sensors
        String unitShortCut = sensorType.getUnitShortcut();
        if (unitShortCut.equals("%")) {
            unitShortCut = "%%";
        }
        if (sensorType.toString().contains("TEMPERATURE")) {
            unitShortCut = "°C";
        }
        return new StateDescription(null, null, null, sensorType.getPattern() + " " + unitShortCut, true, null);
    }

    private StateDescription getStageDescription(String channelID, Locale locale) {
        if (channelID.contains(STAGE.toLowerCase())) {
            List<StateOption> stateOptions = new ArrayList<StateOption>();
            // if (isLight) {
            if (channelID.contains(LIGHT)) {
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_OFF,
                        getText("OPTION_BOTH_LIGHTS_OFF", locale)));
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_ON,
                        getText("OPTION_BOTH_LIGHTS_ON", locale)));
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_FIRST_ON,
                        getText("OPTION_FIRST_LIGHT_ON", locale)));
                if (channelID.contains("3")) {
                    stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_SECOND_ON,
                            getText("OPTION_SECOND_LIGHT_ON", locale)));
                }
            } else {
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_OFF,
                        getText("OPTION_BOTH_RELAIS_OFF", locale)));
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_ON,
                        getText("OPTION_BOTH_RELAIS_ON", locale)));
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_FIRST_ON,
                        getText("OPTION_FIRST_RELAIS_ON", locale)));
                if (channelID.contains("3")) {
                    stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_SECOND_ON,
                            getText("OPTION_SECOND_RELAIS_ON", locale)));
                }
            }
            return new StateDescription(null, null, null, null, false, stateOptions);
        }
        return null;
    }

    private String getSesorDescription(SensorEnum sensorType, Locale locale) {
        // the digitalSTROM resolution for temperature in kelvin is not correct but sensor-events and cached values are
        // shown in °C so we will use this unit for temperature sensors
        return getDescText(sensorType.toString().toLowerCase() + "_label",
                locale);/*
                         * sensorType.toString().contains("TEMPERATURE")
                         * ? getText("sensor_desc_0", locale) + getText(sensorType.toString(), locale) + " "
                         * + getText("sensor_desc_1", locale) + getText(sensorType.toString(), locale) + " "
                         * + getText("sensor_desc_2", locale) + " " + getText("degrees_celsius", locale) + " (°C) "
                         * + getText("sensor_desc_3", locale)
                         * : getText("sensor_desc_0", locale) + getText(sensorType.toString(), locale) + " "
                         * + getText("sensor_desc_1", locale) + getText(sensorType.toString(), locale) + " "
                         * + getText("sensor_desc_2", locale) + " " + getText(sensorType.getUnit(), locale) + " ("
                         * + sensorType.getUnitShortcut() + ") " + getText("sensor_desc_3", locale);
                         */
    }

    private String getSensorText(SensorEnum sensorType, Locale locale) {
        return getText(sensorType.toString().toLowerCase() + "_desc", locale);
    }

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        List<ChannelType> channelTypeList = new LinkedList<ChannelType>();
        for (String channelTypeId : SUPPORTED_CHANNEL_TYPES) {
            channelTypeList.add(
                    getChannelType(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID, channelTypeId), locale));
        }
        for (String channelTypeId : SUPPORTED_OUTPUT_CHANNEL_TYPES) {
            channelTypeList.add(
                    getChannelType(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID, channelTypeId), locale));
        }
        for (SensorEnum sensorType : SensorEnum.values()) {
            channelTypeList.add(getChannelType(
                    new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID, sensorType.toString().toLowerCase()),
                    locale));
        }
        for (MeteringTypeEnum meteringType : MeteringTypeEnum.values()) {
            for (MeteringUnitsEnum meteringUnit : meteringType.getMeteringUnitList()) {
                channelTypeList.add(getChannelType(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID,
                        meteringType.toString() + "_" + meteringUnit.toString()), locale));
                channelTypeList.add(getChannelType(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID,
                        TOTAL_PRE + meteringType.toString() + "_" + meteringUnit.toString()), locale));
            }
        }
        return channelTypeList;
    }

    private String getLabelText(String channelID, Locale locale) {
        return getText(channelID + "_label", locale);
    }

    private String getDescText(String channelID, Locale locale) {
        return getText(channelID + "_desc", locale);
    }

    private String getCategory(String channelID) {
        if (channelID.contains(LIGHT)) {
            if (channelID.contains(DIMMER.toLowerCase())) {
                return CATEGORY_DIMMABLE_LIGHT;
            }
            return CATEGORY_LIGHT;
        }
        if (channelID.contains(SHADE)) {
            if (channelID.contains(ANGLE.toLowerCase())) {
                return CATEGORY_BLINDES;
            }
            return ROLLERSHUTTER;
        }
        return null;
    }

    private Set<String> getTags(String channelID, Locale locale) {
        if (channelID.contains(LIGHT)) {
            return Sets.newHashSet(getText(GE, locale), getText(DS, locale), getText(LIGHT, locale));
        }
        if (channelID.contains(GENERAL)) {
            return Sets.newHashSet(getText(SW, locale), getText(DS, locale), getText(JOKER, locale));
        }
        if (channelID.contains(SHADE)) {
            return Sets.newHashSet(getText(GR, locale), getText(DS, locale), getText("SHADE", locale));
        }
        if (channelID.contains(SCENE)) {
            return Sets.newHashSet(getText(SCENE, locale), getText(DS, locale));
        }
        if (channelID.contains(HEATING)) {
            return Sets.newHashSet(getText(BL, locale), getText(DS, locale), getText(HEATING, locale));
        }
        return null;
    }

    public static String getItemType(String channelID) {
        // TODO: STAGE_SWITCH zu Stage umbenennen?
        if (channelID != null) {
            if (channelID.contains(STAGE.toLowerCase()) || channelID.contains(TEMPERATURE_CONTROLLED.toLowerCase())) {
                return STRING;
            }
            if (channelID.contains(SWITCH.toLowerCase()) || channelID.contains(SCENE)
                    || channelID.contains(WIPE.toLowerCase())) {
                return SWITCH;
            }
            if (channelID.contains(DIMMER.toLowerCase()) || channelID.contains(ANGLE.toLowerCase())) {
                return DIMMER;
            }
            if (channelID.contains(SHADE)) {
                return ROLLERSHUTTER;
            }
        }
        return null;
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        if (channelTypeUID.getBindingId().equals(DigitalSTROMBindingConstants.BINDING_ID)) {
            try {
                SensorEnum sensorType = SensorEnum.valueOf(channelTypeUID.getId().toUpperCase());
                return new ChannelType(channelTypeUID, false, NUMBER, getSensorText(sensorType, locale),
                        getSesorDescription(sensorType, locale), getSensorCategory(sensorType),
                        Sets.newHashSet(getSensorText(sensorType, locale), getText("DS", locale)),
                        getSensorStateDescription(sensorType), null);
            } catch (IllegalArgumentException e) {
                String channelID = channelTypeUID.getId();
                if (SUPPORTED_OUTPUT_CHANNEL_TYPES.contains(channelID)) {
                    // TODO:WIPE? config standby?
                    return new ChannelType(channelTypeUID, false, getItemType(channelID),
                            getLabelText(channelID, locale), getDescText(channelID, locale), getCategory(channelID),
                            getTags(channelID, locale), getStageDescription(channelID, locale), null);
                }
                switch (channelID) {
                    // TODO: auch autmatisch? auf alle erweitern?
                    case DigitalSTROMBindingConstants.CHANNEL_ID_TOTAL_ACTIVE_POWER:
                        return new ChannelType(channelTypeUID, false, NUMBER,
                                getText("CHANNEL_TOTAL_ACTIVE_POWER_LABEL", locale),
                                getText("CHANNEL_TOTAL_ACTIVE_POWER_DESCRIPTION", locale), CATEGORY_ENERGY,
                                Sets.newHashSet(getText("ACTIVE_POWER", locale), getText("POWER_CONSUMPTION", locale),
                                        getText("DS", locale)),
                                getSensorStateDescription(SensorEnum.ACTIVE_POWER.getUnitShortcut()), null);
                    case DigitalSTROMBindingConstants.CHANNEL_ID_TOTAL_ELECTRIC_METER:
                        return new ChannelType(channelTypeUID, false, NUMBER,
                                getText("CHANNEL_TOTAL_ELECTRIC_METER_LABEL", locale),
                                getText("CHANNEL_TOTAL_ELECTRIC_METER_DESCRIPTION", locale), CATEGORY_ENERGY,
                                Sets.newHashSet(getText("ELECTRIC_METER", locale), getText("DS", locale)),
                                getSensorStateDescription(SensorEnum.ELECTRIC_METER.getUnitShortcut()), null);

                    case DigitalSTROMBindingConstants.CHANNEL_ID_SCENE:
                        return new ChannelType(channelTypeUID, false, SWITCH, getText("CHANNEL_SCENE_LABEL", locale),
                                getText("CHANNEL_SCENE_DESCRIPTION", locale), null,
                                Sets.newHashSet(getText("SCENE", locale), getText("DS", locale)), null, null);
                    default:
                        break;
                }
                try {
                    // check metering channel
                    String[] meteringChannelSplit = channelID.split("_");
                    if (meteringChannelSplit.length > 1) {
                        short offset = 0;
                        // if total_
                        if (meteringChannelSplit.length == 3) {
                            offset = 1;
                        }
                        // check through IllegalArgumentException, if channel is metering
                        MeteringTypeEnum meteringType = MeteringTypeEnum.valueOf(meteringChannelSplit[0 + offset]);
                        MeteringUnitsEnum unitType = MeteringUnitsEnum.valueOf(meteringChannelSplit[1 + offset]);

                        String pattern = "%.3f kWh";

                        if (MeteringTypeEnum.energy.equals(meteringType)) {
                            if (MeteringUnitsEnum.Ws.equals(unitType)) {
                                pattern = "%d " + unitType.toString();
                            }
                        } else {
                            pattern = "%d W";
                        }
                        return new ChannelType(channelTypeUID, false, NUMBER, getLabelText(channelID, locale),
                                getDescText(channelID, locale), CATEGORY_ENERGY,
                                Sets.newHashSet(getLabelText(channelID, locale), getText("DS", locale)),
                                new StateDescription(null, null, null, pattern, true, null), null);
                    }
                } catch (IllegalArgumentException e1) {
                    // ignore
                }

            }
        }
        return null;
    }

    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        return null;
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        return null;
    }
}
