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
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.DeviceBinarayInputEnum;
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
    public final static String WIPE = "wipe";
    public final static String ANGLE = "angle";
    public final static String STAGE = "stage"; // pre stageses e.g. 2+STAGE_SWITCH
    // benötigt?
    public final static String TEMPERATURE_CONTROLLED = "temperature_controled";

    // item types
    public final static String DIMMER = "Dimmer";
    public final static String SWITCH = "Switch";
    public final static String ROLLERSHUTTER = "Rollershutter";
    public final static String STRING = "String";
    public final static String NUMBER = "Number";

    public final static String TOTAL_PRE = "total_";
    public static final String BINARY_INPUT_PRE = "binary_input_";

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

    /**
     * Returns the output channel type id as {@link String} for the given {@link FunctionalColorGroupEnum} and
     * {@link OutputModeEnum} or null, if no channel type exists for the given {@link FunctionalColorGroupEnum} and
     * {@link OutputModeEnum}.
     *
     * @param functionalGroup
     * @param outputMode
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
        }
        return null;
    }

    private static List<String> supportedOutputChannelTypes = new ArrayList<>();

    /**
     * Returns true, if the given channel type id is a output channel.
     *
     * @param channelTypeID
     * @return true, if channel type id is output channel
     */
    public static boolean isOutputChannel(String channelTypeID) {
        return supportedOutputChannelTypes.contains(channelTypeID);
    }

    private void init() {
        String channelIDpre = GENERAL;
        for (short i = 0; i < 3; i++) {
            if (i == 1) {
                channelIDpre = LIGHT;
            }
            if (i == 2) {
                channelIDpre = HEATING;
                supportedOutputChannelTypes.add(channelIDpre + "_" + TEMPERATURE_CONTROLLED.toLowerCase());
            }
            supportedOutputChannelTypes.add(channelIDpre + "_" + SWITCH.toLowerCase());
            supportedOutputChannelTypes.add(channelIDpre + "_" + DIMMER.toLowerCase());
            if (i < 2) {
                supportedOutputChannelTypes.add(channelIDpre + "_2_" + STAGE.toLowerCase());
                supportedOutputChannelTypes.add(channelIDpre + "_3_" + STAGE.toLowerCase());
            }
        }
        channelIDpre = SHADE;
        supportedOutputChannelTypes.add(channelIDpre);
        supportedOutputChannelTypes.add(channelIDpre + "_" + ANGLE.toLowerCase());
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
            case ROOM_TEMPERATION_CONTROL_VARIABLE:
                break;
            case ROOM_TEMPERATION_SET_POINT:
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

    private StateDescription getStageDescription(String channelID, Locale locale) {
        if (channelID.contains(STAGE.toLowerCase())) {
            List<StateOption> stateOptions = new ArrayList<StateOption>();
            if (channelID.contains(LIGHT)) {
                // TODO: DigitalSTROMBindingConstants.OPTION_COMBINED_BOTH_OFF hierhin? und "OPTION_BOTH_LIGHTS_OFF" als
                // const? ... besser channelID + "_opt_"+val?
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
        if (channelID.contains(TEMPERATURE_CONTROLLED)) {
            return new StateDescription(new BigDecimal(0), new BigDecimal(50), new BigDecimal(0.1), "%.1f °C", false,
                    null);
        }
        return null;
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
                    new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID, sensorType.toString().toLowerCase()),
                    locale));
        }
        for (MeteringTypeEnum meteringType : MeteringTypeEnum.values()) {
            // TODO: UNIT weg lassen
            for (MeteringUnitsEnum meteringUnit : meteringType.getMeteringUnitList()) {
                channelTypeList.add(getChannelType(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID,
                        meteringType.toString() + "_" + meteringUnit.toString()), locale));
                channelTypeList.add(getChannelType(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID,
                        TOTAL_PRE + meteringType.toString() + "_" + meteringUnit.toString()), locale));
            }
        }
        for (DeviceBinarayInputEnum binaryInput : DeviceBinarayInputEnum.values()) {
            channelTypeList.add(getChannelType(new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID,
                    BINARY_INPUT_PRE + binaryInput.toString().toLowerCase()), locale));
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
        return Sets.newHashSet(getText(channelID, locale), getText("SHADE", locale));
    }

    /**
     * Returns the supported item type for the given channel type id or null, if the channel type does not exist.
     *
     * @param channelTypeID
     * @return item type or null
     */
    public static String getItemType(String channelTypeID) {
        if (channelTypeID != null) {
            if (channelTypeID.contains(STAGE.toLowerCase())) {
                return STRING;
            }
            if (channelTypeID.contains(SWITCH.toLowerCase()) || channelTypeID.contains(SCENE)
                    || channelTypeID.contains(WIPE.toLowerCase()) || channelTypeID.contains(BINARY_INPUT_PRE)) {
                return SWITCH;
            }
            if (channelTypeID.contains(DIMMER.toLowerCase()) || channelTypeID.contains(ANGLE.toLowerCase())) {
                return DIMMER;
            }
            if (channelTypeID.contains(TEMPERATURE_CONTROLLED.toLowerCase())) {
                return NUMBER;
            }
            if (channelTypeID.contains(SHADE)) {
                return ROLLERSHUTTER;
            }
        }
        return null;
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
                    try {
                        DeviceBinarayInputEnum binarayInputType = DeviceBinarayInputEnum
                                .valueOf(channelTypeUID.getId().replaceAll(BINARY_INPUT_PRE, "").toUpperCase());
                        return new ChannelType(channelTypeUID, false, getItemType(channelID),
                                getLabelText(channelID, locale), getDescText(channelID, locale),
                                getBinaryInputCategory(binarayInputType), getSimpleTags(channelTypeUID.getId(), locale),
                                new StateDescription(null, null, null, null, true, null), null);
                    } catch (IllegalArgumentException e2) {
                        // ignore
                    }
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
        return new ChannelTypeUID(BINDING_ID, sensorType.toString().toLowerCase());
    }

    /**
     * Returns the {@link ChannelGroupTypeUID} for the given {@link DeviceBinarayInputEnum}.
     * 
     * @param binaryInputType (must not be null)
     * @return the channel type uid
     */
    public static ChannelTypeUID getBinaryInputChannelUID(DeviceBinarayInputEnum binaryInputType) {
        return new ChannelTypeUID(BINDING_ID, BINARY_INPUT_PRE + binaryInputType.toString().toLowerCase());
    }
}
