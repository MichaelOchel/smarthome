package org.eclipse.smarthome.binding.digitalstrom.internal.providers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.SensorEnum;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class DigitalSTROMChannelTypeProvider implements ChannelTypeProvider {

    /* ChannelTypes */
    // items
    private final String DIMMER = "Dimmer";
    private final String SWITCH = "Switch";
    private final String SHADE = "Rollershutter";
    private final String STRING = "String";
    private final String NUMBER = "Number";

    /* English */
    // Constants label + description
    private final String CHANNEL_BRIGHTNESS_LABEL_EN = "brightness";
    private final String CHANNEL_BRIGHTNESS_DESCRIPTION_EN = "The brightness channel allows to dimm a light device.";

    private final String CHANNEL_LIGHT_SWITCH_LABEL_EN = "light switch";
    private final String CHANNEL_LIGHT_SWITCH_DESCRIPTION_EN = "The light switch channel allows to turn a light device on or off.";

    private final String CHANNEL_COMBINED_2_STAGE_SWITCH_LABEL_EN = "2 stage light switch";
    private final String CHANNEL_COMBINED_2_STAGE_SWITCH_DESCRIPTION_EN = "The 2 stage light switch channel allows to turn both light devices on or off or switch only 1 of the both light device on or off.";

    private final String CHANNEL_COMBINED_3_STAGE_SWITCH_LABEL_EN = "3 stage light switch";
    private final String CHANNEL_COMBINED_3_STAGE_SWITCH_DESCRIPTION_EN = "The 3 stage light switch channel allows to turn both light devices on or off or switch both light devices separated from each other on or off.";

    private final String CHANNEL_SHADE_LABEL_EN = "shade control";
    private final String CHANNEL_SHADE_DESCRIPTION_EN = "The shade control channel allows to control shade device e.g. a rollershutter or awnings.";

    private final String CHANNEL_GENERAL_DIMM_LABEL_EN = "device power control";
    private final String CHANNEL_GENERAL_DIMM_DESCRIPTION_EN = "The device power control channel allows to control the power of a device e.g. a ceiling fan.";

    private final String CHANNEL_GENERAL_SWITCH_LABEL_EN = "device switch";
    private final String CHANNEL_GENERAL_SWITCH_DESCRIPTION_EN = "The device switch channel allows to turn a device on or off e.g. a HIFI-System.";

    private final String CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH_LABEL_EN = "2 stage device switch";
    private final String CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH_DESCRIPTION_EN = "The 2 stage device switch channel allows to turn both relais of the ds-device on or off or switch only 1 of the both relais on or off.";

    private final String CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH_LABEL_EN = "3 stage device switch";
    private final String CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH_DESCRIPTION_EN = "The 3 stage device device channel allows to turn both relais of the ds-device on or off or switch both relais of the ds-device separated from each other on or off.";

    private final String CHANNEL_ACTIVE_POWER_LABEL_EN = "active power";
    private final String CHANNEL_ACTIVE_POWER_DESCRIPTION_EN = "The active power channel indicates the current active power in "
            + getUnitString(SensorEnum.ACTIVE_POWER) + " of the device.";

    private final String CHANNEL_OUTPUT_CURRENT_LABEL_EN = "output current";
    private final String CHANNEL_OUTPUT_CURRENT_DESCRIPTION_EN = "The output current channel indicates the current output current in "
            + getUnitString(SensorEnum.OUTPUT_CURRENT) + " of the device.";

    private final String CHANNEL_ELECTRIC_METER_LABEL_EN = "electric meter";
    private final String CHANNEL_ELECTRIC_METER_DESCRIPTION_EN = "The electric meter channel indicates the current electric meter value in "
            + getUnitString(SensorEnum.ELECTRIC_METER) + " of the device.";

    private final String CHANNEL_SCENE_LABEL_EN = "Scene";
    private final String CHANNEL_SCENE_DESCRIPTION_EN = "The scene channel allows to call or undo a scene from DigitalSTROM.";

    // Tags
    private final String DS = "digitalSTROM";
    private final String YELLOW = "yellow";
    private final String BLACK = "black";
    private final String JOKER = "joker";
    private final String GRAY = "gray";
    private final String LIGHT = "light";
    private final String SHADE_TAG = "shade";
    private final String ACTIVE_POWER = "active power";
    private final String ELECTRIC_METER = "electric meter";
    private final String OUTPUT_CURRENT = "output current";
    private final String UMR = "umr";
    private final String SCENE = "scene";

    // Channel definitions
    private final ChannelType CHANNEL_TYPE_BRIGHTNESS_EN = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_BRIGHTNESS)), false, DIMMER,
            CHANNEL_BRIGHTNESS_LABEL_EN, CHANNEL_BRIGHTNESS_DESCRIPTION_EN, "dimmableLight",
            Sets.newHashSet(YELLOW, DS, LIGHT), null, null);

    private final ChannelType CHANNEL_TYPE_LIGHT_SWITCH_EN = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_LIGHT_SWITCH)), false, SWITCH,
            CHANNEL_LIGHT_SWITCH_LABEL_EN, CHANNEL_LIGHT_SWITCH_DESCRIPTION_EN, "light",
            Sets.newHashSet(YELLOW, DS, LIGHT), null, null);

    /*
     * private final ChannelType CHANNEL_TYPE_PLUG_ADAPTER_EN = new ChannelType("plugAdapter",
     * new ChannelType(new ChannelTypeUID("digitalstrom:plugAdapter"), false, "Switch", "Plug adapter",
     * "The plug adapter channel allows to switch the plug adapter on or off.", "Light", null, null, null),
     * null, "Plug adapter", "The plug adapter channel allows to switch the plug adapter on or off.");
     */
    // TODO: Category Joker? Plug Adapter löschen!

    private final ChannelType CHANNEL_TYPE_GENERAL_DIMM_EN = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_GENERAL_DIMM)), false, DIMMER,
            CHANNEL_GENERAL_DIMM_LABEL_EN, CHANNEL_GENERAL_DIMM_DESCRIPTION_EN, null, Sets.newHashSet(BLACK, DS, JOKER),
            null, null);

    private final ChannelType CHANNEL_TYPE_GENERAL_SWITCH_EN = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_GENERAL_SWITCH)), false, SWITCH,
            CHANNEL_GENERAL_SWITCH_LABEL_EN, CHANNEL_GENERAL_SWITCH_DESCRIPTION_EN, null,
            Sets.newHashSet(BLACK, DS, JOKER), null, null);

    private final ChannelType CHANNEL_TYPE_COMBINED_2_STAGE_SWITCH_EN = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_COMBINED_2_STAGE_SWITCH)), false, STRING,
            CHANNEL_COMBINED_2_STAGE_SWITCH_LABEL_EN, CHANNEL_COMBINED_2_STAGE_SWITCH_DESCRIPTION_EN, "Lights",
            Sets.newHashSet(YELLOW, DS, LIGHT, UMR), getCombinedStageDescription((short) 2, true), null);

    private final ChannelType CHANNEL_TYPE_COMBINED_3_STAGE_SWITCH_EN = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_COMBINED_3_STAGE_SWITCH)), false, STRING,
            CHANNEL_COMBINED_3_STAGE_SWITCH_LABEL_EN, CHANNEL_COMBINED_3_STAGE_SWITCH_DESCRIPTION_EN, "Lights",
            Sets.newHashSet(YELLOW, DS, LIGHT, UMR), getCombinedStageDescription((short) 3, true), null);

    private final ChannelType CHANNEL_TYPE_GENERAL_COMBINED_2_STAGE_SWITCH_EN = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH)), false,
            STRING, CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH_LABEL_EN,
            CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH_DESCRIPTION_EN, null, Sets.newHashSet(BLACK, DS, UMR),
            getCombinedStageDescription((short) 2, true), null);

    private final ChannelType CHANNEL_TYPE_GENERAL_COMBINED_3_STAGE_SWITCH_EN = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH)), false,
            STRING, CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH_LABEL_EN,
            CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH_DESCRIPTION_EN, null, Sets.newHashSet(BLACK, DS, UMR),
            getCombinedStageDescription((short) 3, true), null);

    private final ChannelType CHANNEL_TYPE_SHADE_EN = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_SHADE)), false, SHADE,
            CHANNEL_SHADE_LABEL_EN, CHANNEL_SHADE_DESCRIPTION_EN, "Energy", Sets.newHashSet(GRAY, DS, SHADE_TAG), null,
            null);

    private final ChannelType CHANNEL_TYPE_ACTIVE_POWER_EN = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_ACTIVE_POWER)), false, NUMBER,
            CHANNEL_ACTIVE_POWER_LABEL_EN, CHANNEL_ACTIVE_POWER_DESCRIPTION_EN, null, Sets.newHashSet(ACTIVE_POWER, DS),
            getSensorStateDescription(SensorEnum.ACTIVE_POWER.getUnitShortcut()), null);

    private final ChannelType CHANNEL_TYPE_ELECTRIC_METER_VALUE_EN = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_ELECTRIC_METER)), false, NUMBER,
            CHANNEL_ELECTRIC_METER_LABEL_EN, CHANNEL_ELECTRIC_METER_DESCRIPTION_EN, "Energy",
            Sets.newHashSet(ELECTRIC_METER, DS), getSensorStateDescription(SensorEnum.ELECTRIC_METER.getUnitShortcut()),
            null);

    private final ChannelType CHANNEL_TYPE_OUTPUT_CURRENT_VALUE_EN = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_OUTPUT_CURRENT)), false, NUMBER,
            CHANNEL_OUTPUT_CURRENT_LABEL_EN, CHANNEL_OUTPUT_CURRENT_DESCRIPTION_EN, "Energy",
            Sets.newHashSet(OUTPUT_CURRENT, DS), getSensorStateDescription(SensorEnum.OUTPUT_CURRENT.getUnitShortcut()),
            null);

    private final ChannelType CHANNEL_TYPE_SCENE_VALUE_EN = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_SCENE)), false, SWITCH,
            CHANNEL_SCENE_LABEL_EN, CHANNEL_SCENE_DESCRIPTION_EN, "Energy", Sets.newHashSet(SCENE, DS), null, null);

    /* German */
    // Constants label + description
    private final String CHANNEL_BRIGHTNESS_LABEL_DE = "Helligkeit";
    private final String CHANNEL_BRIGHTNESS_DESCRIPTION_DE = "Regelt die Helligkeit des Lichtes.";

    private final String CHANNEL_LIGHT_SWITCH_LABEL_DE = "Lichtschalter";
    private final String CHANNEL_LIGHT_SWITCH_DESCRIPTION_DE = "Schaltet ein Licht ein oder aus.";

    private final String CHANNEL_SHADE_LABEL_DE = "Schattensteuerung";
    private final String CHANNEL_SHADE_DESCRIPTION_DE = "Erlaubt die Schattensteuerung z.B. von Rollladen oder Markisen.";

    private final String CHANNEL_GENERAL_DIMM_LABEL_DE = "Einstellung Geräteleistung";
    private final String CHANNEL_GENERAL_DIMM_DESCRIPTION_DE = "Stellt die Leistung eines Gerätes ein z.B. eines Deckenventilators.";

    private final String CHANNEL_GENERAL_SWITCH_LABEL_DE = "Geräteschalter";
    private final String CHANNEL_GENERAL_SWITCH_DESCRIPTION_DE = "Schaltet ein Gerät ein und aus z.B. ein HIFI-Anlage.";

    private final String CHANNEL_ACTIVE_POWER_LABEL_DE = "Wirkleistung";
    private final String CHANNEL_ACTIVE_POWER_DESCRIPTION_DE = "Zeigt die aktuelle Wirkleistung des Geräts in Watt (W) an.";

    private final String CHANNEL_OUTPUT_CURRENT_LABEL_DE = "Ausgangsstrom";
    private final String CHANNEL_OUTPUT_CURRENT_DESCRIPTION_DE = "Zeigt den aktuelle Ausgangsstrom des Geräts in Ampere (mA) an.";

    private final String CHANNEL_ELECTRIC_METER_LABEL_DE = "Stromzähler";
    private final String CHANNEL_ELECTRIC_METER_DESCRIPTION_DE = "Zeigt den aktuelle gesammt Stromverbrauch des Geräts in Kilowatt pro Stunde (kWh) an.";

    private final String CHANNEL_COMBINED_2_STAGE_SWITCH_LABEL_DE = "2 Phasen Lichtschalter";
    private final String CHANNEL_COMBINED_2_STAGE_SWITCH_DESCRIPTION_DE = "Der 2 Phasen Lichtschalter Channel erlaubt es zwei angeschlossene Lampen ein oder aus zu schalten oder nur eine der beiden Lampen zu schalten.";

    private final String CHANNEL_COMBINED_3_STAGE_SWITCH_LABEL_DE = "3 Phasen Lichtschalter";
    private final String CHANNEL_COMBINED_3_STAGE_SWITCH_DESCRIPTION_DE = "Der 3 Phasen Lichtschalter Channel erlaubt es zwei angeschlossene Lampen ein oder aus zu schalten oder eine der beiden Lampen seperat von einander zu schalten.";

    private final String CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH_LABEL_DE = "2 Phasen Geräteschalter";
    private final String CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH_DESCRIPTION_DE = "Der 2 Phasen Lichtschalter Channel erlaubt es beide Relais ein oder aus zu schalten oder nur eines der beiden Relais zu schalten.";

    private final String CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH_LABEL_DE = "3 stagePhasen Geräteschalter";
    private final String CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH_DESCRIPTION_DE = "Der 3 Phasen Lichtschalter Channel erlaubt es die beiden Relais ein oder aus zu schalten oder eines der beiden Relais seperat von einander zu schalten.";

    private final String CHANNEL_SCENE_LABEL_DE = "Szene/Stimmung";
    private final String CHANNEL_SCENE_DESCRIPTION_DE = "Ruft eine Szene/Stimmung auf bzw. macht sie rückgänig.";

    // Tags
    private final String GELB = "gelb";
    private final String SCHWARZ = "schwarz";
    private final String GRAU = "grau";
    private final String LICHT = "Licht";
    private final String SCHATTEN = "Schatten";
    private final String WIRKL = "Wirkleistung";
    private final String STROMZ = "Strohmzähler";
    private final String AUSGANGSS = "Ausgagsstrom";
    private final String SZENE = "Szene";
    private final String STIMMUNG = "Stimmung";

    // Channel definitions
    private final ChannelType CHANNEL_TYPE_BRIGHTNESS_DE = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_BRIGHTNESS)), false, DIMMER,
            CHANNEL_BRIGHTNESS_LABEL_DE, CHANNEL_BRIGHTNESS_DESCRIPTION_DE, "dimmableLight",
            Sets.newHashSet(GELB, LICHT, DS), null, null);

    private final ChannelType CHANNEL_TYPE_LIGHT_SWITCH_DE = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_LIGHT_SWITCH)), false, SWITCH,
            CHANNEL_LIGHT_SWITCH_LABEL_DE, CHANNEL_LIGHT_SWITCH_DESCRIPTION_DE, "light",
            Sets.newHashSet(GELB, LICHT, DS), null, null);

    private final ChannelType CHANNEL_TYPE_SHADE_DE = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_SHADE)), false, SHADE,
            CHANNEL_SHADE_LABEL_DE, CHANNEL_SHADE_DESCRIPTION_DE, "Blinds", Sets.newHashSet(GRAU, SCHATTEN, DS), null,
            null);

    private final ChannelType CHANNEL_TYPE_GENERAL_DIMM_DE = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_GENERAL_DIMM)), false, DIMMER,
            CHANNEL_GENERAL_DIMM_LABEL_DE, CHANNEL_GENERAL_DIMM_DESCRIPTION_DE, null, Sets.newHashSet(SCHWARZ, DS),
            null, null);

    private final ChannelType CHANNEL_TYPE_GENERAL_SWITCH_DE = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_GENERAL_SWITCH)), false, SWITCH,
            CHANNEL_GENERAL_SWITCH_LABEL_DE, CHANNEL_GENERAL_SWITCH_DESCRIPTION_DE, null, Sets.newHashSet(SCHWARZ, DS),
            null, null);

    private final ChannelType CHANNEL_TYPE_ACTIVE_POWER_DE = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_ACTIVE_POWER)), false, NUMBER,
            CHANNEL_ACTIVE_POWER_LABEL_DE, CHANNEL_ACTIVE_POWER_DESCRIPTION_DE, "Energy", Sets.newHashSet(WIRKL, DS),
            getSensorStateDescription(SensorEnum.ACTIVE_POWER.getUnitShortcut()), null);

    private final ChannelType CHANNEL_TYPE_ELECTRIC_METER_VALUE_DE = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_ELECTRIC_METER)), false, NUMBER,
            CHANNEL_ELECTRIC_METER_LABEL_DE, CHANNEL_ELECTRIC_METER_DESCRIPTION_DE, "Energy",
            Sets.newHashSet(STROMZ, DS), getSensorStateDescription(SensorEnum.ELECTRIC_METER.getUnitShortcut()), null);

    private final ChannelType CHANNEL_TYPE_OUTPUT_CURRENT_VALUE_DE = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_OUTPUT_CURRENT)), false, NUMBER,
            CHANNEL_OUTPUT_CURRENT_LABEL_DE, CHANNEL_OUTPUT_CURRENT_DESCRIPTION_DE, "Energy",
            Sets.newHashSet(AUSGANGSS, DS), getSensorStateDescription(SensorEnum.OUTPUT_CURRENT.getUnitShortcut()),
            null);

    private final ChannelType CHANNEL_TYPE_COMBINED_2_STAGE_SWITCH_DE = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_COMBINED_2_STAGE_SWITCH)), false, STRING,
            CHANNEL_COMBINED_2_STAGE_SWITCH_LABEL_DE, CHANNEL_COMBINED_2_STAGE_SWITCH_DESCRIPTION_DE, "Lights",
            Sets.newHashSet(GELB, DS, LICHT, UMR), getCombinedStageDescription((short) 2, true), null);

    private final ChannelType CHANNEL_TYPE_COMBINED_3_STAGE_SWITCH_DE = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_COMBINED_3_STAGE_SWITCH)), false, STRING,
            CHANNEL_COMBINED_3_STAGE_SWITCH_LABEL_DE, CHANNEL_COMBINED_3_STAGE_SWITCH_DESCRIPTION_DE, "Lights",
            Sets.newHashSet(GELB, DS, LICHT, UMR), getCombinedStageDescription((short) 3, true), null);

    private final ChannelType CHANNEL_TYPE_GENERAL_COMBINED_2_STAGE_SWITCH_DE = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH)), false,
            STRING, CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH_LABEL_DE,
            CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH_DESCRIPTION_DE, null, Sets.newHashSet(SCHWARZ, DS, UMR),
            getCombinedStageDescription((short) 2, true), null);

    private final ChannelType CHANNEL_TYPE_GENERAL_COMBINED_3_STAGE_SWITCH_DE = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH)), false,
            STRING, CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH_LABEL_DE,
            CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH_DESCRIPTION_DE, null, Sets.newHashSet(SCHWARZ, DS, UMR),
            getCombinedStageDescription((short) 3, true), null);

    private final ChannelType CHANNEL_TYPE_SCENE_VALUE_DE = new ChannelType(
            new ChannelTypeUID(getUID(DigitalSTROMBindingConstants.CHANNEL_SCENE)), false, SWITCH,
            CHANNEL_SCENE_LABEL_DE, CHANNEL_SCENE_DESCRIPTION_DE, "Energy", Sets.newHashSet(SZENE, STIMMUNG, DS), null,
            null);

    /* Maps */
    /* English */
    private HashMap<ChannelTypeUID, ChannelType> channel_types_en = listToHashMap(
            Lists.newArrayList(CHANNEL_TYPE_BRIGHTNESS_EN, CHANNEL_TYPE_LIGHT_SWITCH_EN, CHANNEL_TYPE_ACTIVE_POWER_EN,
                    CHANNEL_TYPE_ELECTRIC_METER_VALUE_EN, CHANNEL_TYPE_OUTPUT_CURRENT_VALUE_EN, CHANNEL_TYPE_SHADE_EN,
                    CHANNEL_TYPE_GENERAL_DIMM_EN, CHANNEL_TYPE_GENERAL_SWITCH_EN,
                    CHANNEL_TYPE_COMBINED_2_STAGE_SWITCH_EN, CHANNEL_TYPE_COMBINED_3_STAGE_SWITCH_EN,
                    CHANNEL_TYPE_GENERAL_COMBINED_2_STAGE_SWITCH_EN, CHANNEL_TYPE_GENERAL_COMBINED_3_STAGE_SWITCH_EN,
                    CHANNEL_TYPE_LIGHT_SWITCH_EN, CHANNEL_TYPE_SCENE_VALUE_EN));
    /* German */
    private HashMap<ChannelTypeUID, ChannelType> channel_types_de = listToHashMap(
            Lists.newArrayList(CHANNEL_TYPE_BRIGHTNESS_DE, CHANNEL_TYPE_LIGHT_SWITCH_DE, CHANNEL_TYPE_ACTIVE_POWER_DE,
                    CHANNEL_TYPE_ELECTRIC_METER_VALUE_DE, CHANNEL_TYPE_OUTPUT_CURRENT_VALUE_DE, CHANNEL_TYPE_SHADE_DE,
                    CHANNEL_TYPE_GENERAL_DIMM_DE, CHANNEL_TYPE_GENERAL_SWITCH_DE,
                    CHANNEL_TYPE_COMBINED_2_STAGE_SWITCH_DE, CHANNEL_TYPE_COMBINED_3_STAGE_SWITCH_DE,
                    CHANNEL_TYPE_GENERAL_COMBINED_2_STAGE_SWITCH_DE, CHANNEL_TYPE_GENERAL_COMBINED_3_STAGE_SWITCH_DE,
                    CHANNEL_TYPE_LIGHT_SWITCH_DE, CHANNEL_TYPE_SCENE_VALUE_DE));

    private HashMap<ChannelTypeUID, ChannelType> listToHashMap(List<ChannelType> channelTypeList) {
        if (channelTypeList != null) {
            HashMap<ChannelTypeUID, ChannelType> map = new HashMap<ChannelTypeUID, ChannelType>();
            for (ChannelType channelType : channelTypeList) {
                map.put(channelType.getUID(), channelType);
            }
            return map;
        }
        return null;
    }

    private String getUID(String id) {
        return DigitalSTROMBindingConstants.BINDING_ID + ":" + id;
    }

    private StateDescription getSensorStateDescription(String shortcutUnit) {
        return new StateDescription(null, null, null, "%d " + shortcutUnit, true, null);
    }

    // TODO: german option translation
    private StateDescription getCombinedStageDescription(short stages, boolean isLight) {
        if (isLight) {
            return stages == 2
                    ? new StateDescription(null, null, null, null, false,
                            Lists.newArrayList(new StateOption("0", DigitalSTROMBindingConstants.OPTION_BOTH_OFF),
                                    new StateOption("200", DigitalSTROMBindingConstants.OPTION_BOTH_ON),
                                    new StateOption("90", DigitalSTROMBindingConstants.OPTION_FIRST_ON)))
                    : new StateDescription(null, null, null, null, false,
                            Lists.newArrayList(new StateOption("0", DigitalSTROMBindingConstants.OPTION_BOTH_OFF),
                                    new StateOption("200", DigitalSTROMBindingConstants.OPTION_BOTH_ON),
                                    new StateOption("90", DigitalSTROMBindingConstants.OPTION_FIRST_ON),
                                    new StateOption("130", DigitalSTROMBindingConstants.OPTION_SECOND_ON)));
        } else {
            return stages == 2
                    ? new StateDescription(null, null, null, null, false,
                            Lists.newArrayList(
                                    new StateOption("0", DigitalSTROMBindingConstants.OPTION_BOTH_RELAIS_OFF),
                                    new StateOption("200", DigitalSTROMBindingConstants.OPTION_BOTH_RELAIS_ON),
                                    new StateOption("90", DigitalSTROMBindingConstants.OPTION_FIRST_RELAIS_ON)))
                    : new StateDescription(null, null, null, null, false,
                            Lists.newArrayList(
                                    new StateOption("0", DigitalSTROMBindingConstants.OPTION_BOTH_RELAIS_OFF),
                                    new StateOption("200", DigitalSTROMBindingConstants.OPTION_BOTH_RELAIS_ON),
                                    new StateOption("90", DigitalSTROMBindingConstants.OPTION_FIRST_RELAIS_ON),
                                    new StateOption("130", DigitalSTROMBindingConstants.OPTION_SECOND_RELAIS_ON)));
        }
    }

    private String getUnitString(SensorEnum sensorType) {
        return sensorType.getUnit() + " (" + sensorType.getUnitShortcut() + ")";
    }

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        return locale != null && locale.equals(Locale.GERMAN) ? channel_types_de.values()
                : this.channel_types_en.values();
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        return locale != null && locale.equals(Locale.GERMAN) ? channel_types_de.get(channelTypeUID)
                : this.channel_types_en.get(channelTypeUID);
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
