/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.providers;

import static org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants.BINDING_ID;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.DeviceBinarayInputEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.FunctionalColorGroupEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringUnitsEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.OutputModeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.SensorEnum;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;

import com.google.common.collect.Sets;

/**
 * The {@link DsChannelTypeProvider} implements the {@link ChannelTypeProvider} generates all supported
 * {@link Channel}'s for digitalSTROM.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class DsChannelTypeProvider extends BaseDsI18n implements ChannelTypeProvider {

    // channelID building (effect group type + (nothing || SEPERATOR + item type || SEPERATOR + extended item type) e.g.
    // light_switch, shade or shade_angle
    // channel effect group type
    public final static String LIGHT = "light"; // and tag
    public final static String SHADE = "shade"; // and tag
    public final static String HEATING = "heating"; // and tag
    public final static String GENERAL = "general";
    public final static String SCENE = "scene";
    // channel extended item type
    public final static String WIPE = "wipe";
    public final static String ANGLE = "angle";
    public final static String STAGE = "stage"; // pre stageses e.g. 2+STAGE_SWITCH
    public final static String TEMPERATURE_CONTROLLED = "temperature_controlled";

    // item types
    public final static String DIMMER = "Dimmer";
    public final static String SWITCH = "Switch";
    public final static String ROLLERSHUTTER = "Rollershutter";
    public final static String STRING = "String";
    public final static String NUMBER = "Number";

    public final static String TOTAL_PRE = "total";
    public static final String BINARY_INPUT_PRE = "binary_input";
    public static final String OPTION = "opt";

    // tags
    private final String GE = "GE";
    private final String GR = "GR";
    private final String BL = "BL";
    private final String SW = "SW";
    private final String DS = "DS";
    private final String JOKER = "JOKER";

    // categories
    private final String CATEGORY_BLINDES = "Blinds";
    private final String CATEGORY_DIMMABLE_LIGHT = "DimmableLight";
    private final String CATEGORY_CARBONE_DIOXIDE = "CarbonDioxide";
    private final String CATEGORY_ENERGY = "Energy";
    private final String CATEGORY_HUMIDITY = "Humidity";
    private final String CATEGORY_BRIGHTNESS = "Brightness";
    private final String CATEGORY_LIGHT = "Light";
    private final String CATEGORY_PRESSURE = "Pressure";
    private final String CATEGORY_SOUND_VOLUME = "SoundVolume";
    private final String CATEGORY_TEMPERATURE = "Temperature";
    private final String CATEGORY_WIND = "Wind";
    private final String CATEGORY_RAIN = "Rain";
    private final String CATEGORY_BATTERY = "Battery";
    private final String CATEGORY_DOOR = "Door";
    private final String CATEGORY_WINDOW = "Window";
    private final String CATEGORY_GARAGE_DOOR = "GarageDoor";
    private final String CATEGORY_SMOKE = "Smoke";
    private final String CATEGORY_ALARM = "Alarm";
    private final String CATEGORY_MOTION = "Motion";

    /**
     * Returns the output channel type id as {@link String} for the given {@link FunctionalColorGroupEnum} and
     * {@link OutputModeEnum} or null, if no channel type exists for the given {@link FunctionalColorGroupEnum} and
     * {@link OutputModeEnum}.
     *
     * @param functionalGroup of the {@link Device}
     * @param outputMode of the {@link Device}
     * @return the output channel type id or null
     */
    public static String getOutputChannelTypeID(FunctionalColorGroupEnum functionalGroup, OutputModeEnum outputMode) {
        if (functionalGroup != null && outputMode != null) {
            String channelPreID = GENERAL;
            if (functionalGroup.equals(FunctionalColorGroupEnum.YELLOW)) {
                channelPreID = LIGHT;
            }
            if (functionalGroup.equals(FunctionalColorGroupEnum.GREY)) {
                if (outputMode.equals(OutputModeEnum.POSITION_CON)) {
                    return buildIdentifier(SHADE);
                }
                if (outputMode.equals(OutputModeEnum.POSITION_CON_US)) {
                    return buildIdentifier(SHADE, ANGLE);
                }
            }
            if (functionalGroup.equals(FunctionalColorGroupEnum.BLUE)) {
                channelPreID = HEATING;
                if (OutputModeEnum.outputModeIsTemperationControlled(outputMode)) {
                    return buildIdentifier(channelPreID, TEMPERATURE_CONTROLLED);
                }
            }
            if (OutputModeEnum.outputModeIsSwitch(outputMode)) {
                return buildIdentifier(channelPreID, SWITCH);
            }
            if (OutputModeEnum.outputModeIsDimmable(outputMode)) {
                return buildIdentifier(channelPreID, DIMMER);
            }
            if (!channelPreID.equals(HEATING)) {
                if (outputMode.equals(OutputModeEnum.COMBINED_2_STAGE_SWITCH)) {
                    return buildIdentifier(channelPreID, "2", STAGE);
                }
                if (outputMode.equals(OutputModeEnum.COMBINED_3_STAGE_SWITCH)) {
                    return buildIdentifier(channelPreID, "3", STAGE);
                }
            }
        }
        return null;
    }

    public static String getMeteringChannelID(MeteringTypeEnum type, MeteringUnitsEnum unit, boolean isTotal) {
        if (isTotal) {
            return buildIdentifier(TOTAL_PRE, type, unit);
        } else {
            return buildIdentifier(type, unit);
        }
    }

    public static MeteringTypeEnum getMeteringType(String channelID) {
        // check metering channel
        String[] meteringChannelSplit = channelID.split(SEPERATOR);
        if (meteringChannelSplit.length > 1) {
            short offset = 0;
            // if total_
            if (meteringChannelSplit.length == 3) {
                offset = 1;
            }
            try {
                // check through IllegalArgumentException, if channel is metering
                return MeteringTypeEnum.valueOf(meteringChannelSplit[0 + offset].toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    private static List<String> supportedOutputChannelTypes = new ArrayList<>();

    /**
     * Returns true, if the given channel type id is a output channel.
     *
     * @param channelTypeID to check
     * @return true, if channel type id is output channel
     */
    public static boolean isOutputChannel(String channelTypeID) {
        return supportedOutputChannelTypes.contains(channelTypeID);
    }

    @Override
    protected void init() {
        String channelIDpre = GENERAL;
        for (short i = 0; i < 3; i++) {
            if (i == 1) {
                channelIDpre = LIGHT;
            }
            if (i == 2) {
                channelIDpre = HEATING;
                supportedOutputChannelTypes.add(buildIdentifier(channelIDpre, TEMPERATURE_CONTROLLED));
            }
            supportedOutputChannelTypes.add(buildIdentifier(channelIDpre, SWITCH));
            supportedOutputChannelTypes.add(buildIdentifier(channelIDpre, DIMMER));
            if (i < 2) {
                supportedOutputChannelTypes.add(buildIdentifier(channelIDpre, "2", STAGE));
                supportedOutputChannelTypes.add(buildIdentifier(channelIDpre, "3", STAGE));
            }
        }
        channelIDpre = SHADE;
        supportedOutputChannelTypes.add(channelIDpre);
        supportedOutputChannelTypes.add(buildIdentifier(channelIDpre, ANGLE));
        supportedOutputChannelTypes.add(SCENE);
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
            case CARBON_DIOXIDE:
                return CATEGORY_CARBONE_DIOXIDE;
            case PRECIPITATION:
                return CATEGORY_RAIN;
            case RELATIVE_HUMIDITY_INDOORS:
            case RELATIVE_HUMIDITY_OUTDOORS:
                return CATEGORY_HUMIDITY;
            case ROOM_TEMPERATURE_CONTROL_VARIABLE:
                break;
            case ROOM_TEMPERATURE_SET_POINT:
                break;
            case TEMPERATURE_INDOORS:
            case TEMPERATURE_OUTDOORS:
                return CATEGORY_TEMPERATURE;
            case WIND_DIRECTION:
            case WIND_SPEED:
                return CATEGORY_WIND;
            case SOUND_PRESSURE_LEVEL:
                return CATEGORY_SOUND_VOLUME;
            case BRIGHTNESS_INDOORS:
            case BRIGHTNESS_OUTDOORS:
                return CATEGORY_BRIGHTNESS;
            default:
                break;

        }
        return null;
    }

    private String getBinaryInputCategory(DeviceBinarayInputEnum binaryInputType) {
        switch (binaryInputType) {
            case BATTERY_STATUS_IS_LOW:
                return CATEGORY_BATTERY;
            case SUN_RADIATION:
            case SUN_PROTECTION:
            case TWILIGHT:
            case BRIGHTNESS:
                return CATEGORY_BRIGHTNESS;
            case HEATING_OPERATION_ON_OFF:
            case CHANGE_OVER_HEATING_COOLING:
            case TEMPERATION_BELOW_LIMIT:
                return CATEGORY_TEMPERATURE;
            case DOOR_IS_OPEN:
                return CATEGORY_DOOR;
            case GARAGE_DOOR_IS_OPEN:
                return CATEGORY_GARAGE_DOOR;
            case PRESENCE:
            case PRESENCE_IN_DARKNESS:
            case MOTION:
            case MOTION_IN_DARKNESS:
                return CATEGORY_MOTION;
            case RAIN:
                return CATEGORY_RAIN;
            case SMOKE:
                return CATEGORY_SMOKE;
            case WINDOW_IS_OPEN:
            case WINDOW_IS_TILTED:
                return CATEGORY_WINDOW;
            case WIND_STRENGHT_ABOVE_LIMIT:
                return CATEGORY_WIND;
            case FROST:
                return CATEGORY_ALARM;
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

    private String getStageChannelOption(String type, String option) {
        return buildIdentifier(type, STAGE, OPTION, option);
    }

    private StateDescription getStageDescription(String channelID, Locale locale) {
        if (channelID.contains(STAGE.toLowerCase())) {
            List<StateOption> stateOptions = new ArrayList<StateOption>();
            if (channelID.contains(LIGHT)) {
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_OFF, getText(
                        getStageChannelOption(LIGHT, DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_OFF), locale)));
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_ON, getText(
                        getStageChannelOption(LIGHT, DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_ON), locale)));
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_FIRST_ON, getText(
                        getStageChannelOption(LIGHT, DigitalSTROMBindingConstants.OPTION_COMBINED_FIRST_ON), locale)));
                if (channelID.contains("3")) {
                    stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_SECOND_ON, getText(
                            getStageChannelOption(LIGHT, DigitalSTROMBindingConstants.OPTION_COMBINED_SECOND_ON),
                            locale)));
                }
            } else {
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_OFF,
                        getText(getStageChannelOption(GENERAL, DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_OFF),
                                locale)));
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_ON, getText(
                        getStageChannelOption(GENERAL, DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_ON), locale)));
                stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_FIRST_ON,
                        getText(getStageChannelOption(GENERAL, DigitalSTROMBindingConstants.OPTION_COMBINED_FIRST_ON),
                                locale)));
                if (channelID.contains("3")) {
                    stateOptions.add(new StateOption(DigitalSTROMBindingConstants.OPTION_COMBINED_SECOND_ON, getText(
                            getStageChannelOption(GENERAL, DigitalSTROMBindingConstants.OPTION_COMBINED_SECOND_ON),
                            locale)));
                }
            }
            return new StateDescription(null, null, null, null, false, stateOptions);
        }
        if (channelID.contains(TEMPERATURE_CONTROLLED)) {
            return new StateDescription(new BigDecimal(0), new BigDecimal(50), new BigDecimal(0.1), "%.1f °C", false,
                    null);
        }
        return null;
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
        if (channelID.contains(TEMPERATURE_CONTROLLED)) {
            return CATEGORY_TEMPERATURE;
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

    private Set<String> getSimpleTags(String channelID, Locale locale) {
        return Sets.newHashSet(getText(channelID, locale), getText(channelID, locale));
    }

    /**
     * Returns the supported item type for the given channel type id or null, if the channel type does not exist.
     *
     * @param channelTypeID of the channel
     * @return item type or null
     */
    public static String getItemType(String channelTypeID) {
        if (channelTypeID != null) {
            if (stringContains(channelTypeID, STAGE)) {
                return STRING;
            }
            if (stringContains(channelTypeID, SWITCH) || stringContains(channelTypeID, SCENE)
                    || stringContains(channelTypeID, WIPE) || stringContains(channelTypeID, BINARY_INPUT_PRE)) {
                return SWITCH;
            }
            if (stringContains(channelTypeID, DIMMER) || stringContains(channelTypeID, ANGLE)) {
                return DIMMER;
            }
            if (stringContains(channelTypeID, TEMPERATURE_CONTROLLED)) {
                return NUMBER;
            }
            if (channelTypeID.contains(SHADE)) {
                return ROLLERSHUTTER;
            }
        }
        return null;
    }

    private static boolean stringContains(String string, String compare) {
        return string.toLowerCase().contains(compare.toLowerCase());
    }

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        List<ChannelType> channelTypeList = new LinkedList<ChannelType>();
        for (String channelTypeId : supportedOutputChannelTypes) {
            channelTypeList.add(
                    getChannelType(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID, channelTypeId), locale));
        }
        for (SensorEnum sensorType : SensorEnum.values()) {
            channelTypeList.add(getChannelType(
                    new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID, buildIdentifier(sensorType)), locale));
        }

        for (MeteringTypeEnum meteringType : MeteringTypeEnum.values()) {
            channelTypeList.add(getChannelType(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID,
                    buildIdentifier(meteringType, MeteringUnitsEnum.WH)), locale));
            channelTypeList.add(getChannelType(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID,
                    buildIdentifier(TOTAL_PRE, meteringType, MeteringUnitsEnum.WH)), locale));
        }

        for (DeviceBinarayInputEnum binaryInput : DeviceBinarayInputEnum.values()) {
            channelTypeList.add(getChannelType(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID,
                    buildIdentifier(BINARY_INPUT_PRE, binaryInput)), locale));
        }

        return channelTypeList;
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        if (channelTypeUID.getBindingId().equals(DigitalSTROMBindingConstants.BINDING_ID)) {
            String channelID = channelTypeUID.getId();
            try {
                SensorEnum sensorType = SensorEnum.valueOf(channelTypeUID.getId().toUpperCase());
                return new ChannelType(channelTypeUID, false, NUMBER, getLabelText(channelID, locale),
                        getDescText(channelID, locale), getSensorCategory(sensorType), getSimpleTags(channelID, locale),
                        getSensorStateDescription(sensorType), null);
            } catch (IllegalArgumentException e) {
                if (supportedOutputChannelTypes.contains(channelID)) {
                    return new ChannelType(channelTypeUID, false, getItemType(channelID),
                            getLabelText(channelID, locale), getDescText(channelID, locale), getCategory(channelID),
                            getTags(channelID, locale), getStageDescription(channelID, locale), null);
                }
                MeteringTypeEnum meteringType = getMeteringType(channelID);
                if (meteringType != null) {
                    String pattern = "%.3f kWh";

                    if (MeteringTypeEnum.CONSUMPTION.equals(meteringType)) {
                        pattern = "%d W";
                    }
                    return new ChannelType(channelTypeUID, false, NUMBER, getLabelText(channelID, locale),
                            getDescText(channelID, locale), CATEGORY_ENERGY,
                            Sets.newHashSet(getLabelText(channelID, locale), getText(DS, locale)),
                            new StateDescription(null, null, null, pattern, true, null), null);
                }
                try {
                    DeviceBinarayInputEnum binarayInputType = DeviceBinarayInputEnum
                            .valueOf(channelTypeUID.getId().replaceAll(BINARY_INPUT_PRE + SEPERATOR, "").toUpperCase());
                    return new ChannelType(channelTypeUID, false, getItemType(channelID),
                            getLabelText(channelID, locale), getDescText(channelID, locale),
                            getBinaryInputCategory(binarayInputType), getSimpleTags(channelTypeUID.getId(), locale),
                            new StateDescription(null, null, null, null, true, null), null);
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

    /**
     * Returns the {@link ChannelGroupTypeUID} for the given {@link SensorEnum}.
     *
     * @param sensorType (must not be null)
     * @return the channel type uid
     */
    public static ChannelTypeUID getSensorChannelUID(SensorEnum sensorType) {
        return new ChannelTypeUID(BINDING_ID, buildIdentifier(sensorType));
    }

    /**
     * Returns the {@link ChannelGroupTypeUID} for the given {@link DeviceBinarayInputEnum}.
     *
     * @param binaryInputType (must not be null)
     * @return the channel type uid
     */
    public static ChannelTypeUID getBinaryInputChannelUID(DeviceBinarayInputEnum binaryInputType) {
        return new ChannelTypeUID(BINDING_ID, buildIdentifier(BINARY_INPUT_PRE, binaryInputType));
    }
}
