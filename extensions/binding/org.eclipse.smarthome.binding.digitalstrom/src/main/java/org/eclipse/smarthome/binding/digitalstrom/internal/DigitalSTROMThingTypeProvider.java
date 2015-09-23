/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.handler.DsDeviceHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.SensorEnum;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.types.StateDescription;

import com.google.common.collect.Lists;

public class DigitalSTROMThingTypeProvider implements ThingTypeProvider {

    List<String> supportedBridgeTypeUIDs = Lists
            .newArrayList(DigitalSTROMBindingConstants.THING_TYPE_DSS_BRIDGE.toString());

    /* ThingTypeMaps */
    HashMap<String, ThingType> thingTypeMapEN = new HashMap<String, ThingType>();
    HashMap<String, ThingType> thingTypeMapDE = new HashMap<String, ThingType>();

    /* Label and description build constants */
    /* English */
    private final String PREFIX_LABEL_EN = "digitalSTROM device ";
    private final String PREFIX_LABEL_PLUGIN_ADAPTER_EN = "digitalSTROM plugin-adapter.";
    private final String PREFIX_DESC_EN = "This is a digitalSTROM ";
    private final String POSTFIX_DESC_EN = " device.";
    private final String POSTFIX_DESC_PLUGIN_ADAPTER_EN = " plugin-adapter.";

    private final String GE_EN = "(yellow)";
    private final String GE_FUNC_EN = "light";
    private final String GR_EN = "(gray)";
    private final String GR_FUNC_EN = "shade";
    private final String SW_EN = "(black)";
    private final String SW_FUNC_EN = "joker";

    /* German */
    private final String PREFIX_LABEL_DE = "digitalSTROM Klemme ";
    private final String PREFIX_LABEL_PLUGIN_ADAPTER_DE = "digitalSTROM Zwischenstecker.";
    private final String PREFIX_DESC_DE = "Dies ist eine digitalSTROM ";
    private final String POSTFIX_DESC_DE = " Klemme.";
    private final String POSTFIX_DESC_PLUGIN_ADAPTER_DE = " Zwischenstecker.";

    private final String GE_DE = "(gelb)";
    private final String GE_FUNC_DE = "Licht";
    private final String GR_DE = "(grau)";
    private final String GR_FUNC_DE = "Schatten";
    private final String SW_DE = "(schwarz)";
    private final String SW_FUNC_DE = "Joker";

    /* ChannelDefinitions */
    // items
    private final String DIMMER = "Dimmer";
    private final String SWITCH = "Switch";
    private final String SHADE = "Rollershutter";
    private final String NUMBER = "Number";

    /* English */
    // Constants label + description
    private final String CHANNEL_BRIGHTNESS_LABEL_EN = "brightness";
    private final String CHANNEL_BRIGHTNESS_DESCRIPTION_EN = "The brightness channel allows to dimm a light device.";

    private final String CHANNEL_LIGHT_SWITCH_LABEL_EN = "light switch";
    private final String CHANNEL_LIGHT_SWITCH_DESCRIPTION_EN = "The light switch channel allows to turn a light device on or off.";

    private final String CHANNEL_SHADE_LABEL_EN = "shade control";
    private final String CHANNEL_SHADE_DESCRIPTION_EN = "The shade control channel allows to control shade device e.g. a rollershutter or awnings.";

    private final String CHANNEL_GENERAL_DIMM_LABEL_EN = "device power control";
    private final String CHANNEL_GENERAL_DIMM_DESCRIPTION_EN = "The device power control channel allows to control the power of a device e.g. a ceiling fan.";

    private final String CHANNEL_GENERAL_SWITCH_LABEL_EN = "device switch";
    private final String CHANNEL_GENERAL_SWITCH_DESCRIPTION_EN = "The device switch channel allows to turn a device on or off e.g. a HIFI-System.";

    private final String CHANNEL_ACTIVE_POWER_LABEL_EN = "active power";
    private final String CHANNEL_ACTIVE_POWER_DESCRIPTION_EN = "The active power channel indicates the current active power in "
            + getUnitString(SensorEnum.ACTIVE_POWER) + " of the device.";

    private final String CHANNEL_OUTPUT_CURRENT_LABEL_EN = "output current";
    private final String CHANNEL_OUTPUT_CURRENT_DESCRIPTION_EN = "The output current channel indicates the current output current in "
            + getUnitString(SensorEnum.OUTPUT_CURRENT) + " of the device.";

    private final String CHANNEL_ELECTRIC_METER_LABEL_EN = "electric meter";
    private final String CHANNEL_ELECTRIC_METER_DESCRIPTION_EN = "The electric meter channel indicates the current electric meter value in "
            + getUnitString(SensorEnum.ELECTRIC_METER) + " of the device.";

    // Channel definitions
    private final ChannelDefinition CHANNEL_DEFINITION_BRIGHTNESS_EN = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_BRIGHTNESS, DIMMER, CHANNEL_BRIGHTNESS_LABEL_EN,
            CHANNEL_BRIGHTNESS_DESCRIPTION_EN, "dimmableLight", null, null);

    private final ChannelDefinition CHANNEL_DEFINITION_LIGHT_SWITCH_EN = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_LIGHT_SWITCH, SWITCH, CHANNEL_LIGHT_SWITCH_LABEL_EN,
            CHANNEL_LIGHT_SWITCH_DESCRIPTION_EN, "light", null, null);

    /*
     * private final ChannelDefinition CHANNEL_DEFINITION_PLUG_ADAPTER_EN = new ChannelDefinition("plugAdapter",
     * new ChannelType(new ChannelTypeUID("digitalstrom:plugAdapter"), false, "Switch", "Plug adapter",
     * "The plug adapter channel allows to switch the plug adapter on or off.", "Light", null, null, null),
     * null, "Plug adapter", "The plug adapter channel allows to switch the plug adapter on or off.");
     */
    // TODO: Category Joker? Plug Adapter löschen!

    private final ChannelDefinition CHANNEL_DEFINITION_GENERAL_DIMM_EN = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_GENERAL_DIMM, DIMMER, CHANNEL_GENERAL_DIMM_LABEL_EN,
            CHANNEL_GENERAL_DIMM_DESCRIPTION_EN, null, null, null);

    private final ChannelDefinition CHANNEL_DEFINITION_GENERAL_SWITCH_EN = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_GENERAL_SWITCH, SWITCH, CHANNEL_GENERAL_SWITCH_LABEL_EN,
            CHANNEL_GENERAL_SWITCH_DESCRIPTION_EN, null, null, null);

    private final ChannelDefinition CHANNEL_DEFINITION_SHADE_EN = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_SHADE, SHADE, CHANNEL_SHADE_LABEL_EN, CHANNEL_SHADE_DESCRIPTION_EN,
            "Blinds", null, null);

    // TODO: digitalSTROM Sensoren auf Richtigkeit überprüfen. States Description hinzufügen
    private final ChannelDefinition CHANNEL_DEFINITION_ACTIVE_POWER_EN = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_ACTIVE_POWER, NUMBER, CHANNEL_ACTIVE_POWER_LABEL_EN,
            CHANNEL_ACTIVE_POWER_DESCRIPTION_EN, "Energy", null,
            getSensorStateDescription(SensorEnum.ACTIVE_POWER.getUnitShortcut()));

    private final ChannelDefinition CHANNEL_DEFINITION_ELECTRIC_METER_VALUE_EN = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_ELECTRIC_METER, NUMBER, CHANNEL_ELECTRIC_METER_LABEL_EN,
            CHANNEL_ELECTRIC_METER_DESCRIPTION_EN, "Energy", null,
            getSensorStateDescription(SensorEnum.ELECTRIC_METER.getUnitShortcut()));

    private final ChannelDefinition CHANNEL_DEFINITION_OUTPUT_CURRENT_VALUE_EN = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_OUTPUT_CURRENT, NUMBER, CHANNEL_OUTPUT_CURRENT_LABEL_EN,
            CHANNEL_OUTPUT_CURRENT_DESCRIPTION_EN, "Energy", null,
            getSensorStateDescription(SensorEnum.OUTPUT_CURRENT.getUnitShortcut()));

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

    // Channel definitions
    private final ChannelDefinition CHANNEL_DEFINITION_BRIGHTNESS_DE = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_BRIGHTNESS, DIMMER, CHANNEL_BRIGHTNESS_LABEL_DE,
            CHANNEL_BRIGHTNESS_DESCRIPTION_DE, "dimmableLight", null, null);

    private final ChannelDefinition CHANNEL_DEFINITION_LIGHT_SWITCH_DE = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_LIGHT_SWITCH, SWITCH, CHANNEL_LIGHT_SWITCH_LABEL_DE,
            CHANNEL_LIGHT_SWITCH_DESCRIPTION_DE, "light", null, null);

    private final ChannelDefinition CHANNEL_DEFINITION_SHADE_DE = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_SHADE, SHADE, CHANNEL_SHADE_LABEL_DE, CHANNEL_SHADE_DESCRIPTION_DE,
            "Blinds", null, null);

    private final ChannelDefinition CHANNEL_DEFINITION_GENERAL_DIMM_DE = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_GENERAL_DIMM, DIMMER, CHANNEL_GENERAL_DIMM_LABEL_DE,
            CHANNEL_GENERAL_DIMM_DESCRIPTION_DE, null, null, null);

    private final ChannelDefinition CHANNEL_DEFINITION_GENERAL_SWITCH_DE = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_GENERAL_SWITCH, SWITCH, CHANNEL_GENERAL_SWITCH_LABEL_DE,
            CHANNEL_GENERAL_SWITCH_DESCRIPTION_DE, null, null, null);

    /*
     * private final ChannelDefinition CHANNEL_DEFINITION_PLUG_ADAPTER_DE = new ChannelDefinition("plugAdapter",
     * new ChannelType(new ChannelTypeUID("digitalstrom:plugAdapter"), false, "Switch", "Plug adapter",
     * "The plug adapter channel allows to switch the plug adapter on or off.", "Light", null, null, null),
     * null, "Plug adapter", "The plug adapter channel allows to switch the plug adapter on or off.");
     */
    // TODO: Category Joker? Plug adapter löschen!

    // TODO: digitalSTROM Sensoren auf Richtigkeit überprüfen. States Description hinzufügen
    private final ChannelDefinition CHANNEL_DEFINITION_ACTIVE_POWER_DE = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_ACTIVE_POWER, NUMBER, CHANNEL_ACTIVE_POWER_LABEL_DE,
            CHANNEL_ACTIVE_POWER_DESCRIPTION_DE, "Energy", null,
            getSensorStateDescription(SensorEnum.ACTIVE_POWER.getUnitShortcut()));

    private final ChannelDefinition CHANNEL_DEFINITION_ELECTRIC_METER_VALUE_DE = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_ELECTRIC_METER, NUMBER, CHANNEL_ELECTRIC_METER_LABEL_DE,
            CHANNEL_ELECTRIC_METER_DESCRIPTION_DE, "Energy", null,
            getSensorStateDescription(SensorEnum.ELECTRIC_METER.getUnitShortcut()));

    private final ChannelDefinition CHANNEL_DEFINITION_OUTPUT_CURRENT_VALUE_DE = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_OUTPUT_CURRENT, NUMBER, CHANNEL_OUTPUT_CURRENT_LABEL_DE,
            CHANNEL_OUTPUT_CURRENT_DESCRIPTION_DE, "Energy", null,
            getSensorStateDescription(SensorEnum.OUTPUT_CURRENT.getUnitShortcut()));

    /* ChannelList */
    /* English */
    private final List<ChannelDefinition> GE_CHANNELS_EN = Lists.newArrayList(CHANNEL_DEFINITION_BRIGHTNESS_EN,
            CHANNEL_DEFINITION_LIGHT_SWITCH_EN, CHANNEL_DEFINITION_ACTIVE_POWER_EN,
            CHANNEL_DEFINITION_ELECTRIC_METER_VALUE_EN, CHANNEL_DEFINITION_OUTPUT_CURRENT_VALUE_EN);
    private final List<ChannelDefinition> GR_CHANNELS_EN = Lists.newArrayList(CHANNEL_DEFINITION_SHADE_EN);
    private final List<ChannelDefinition> SW_CHANNELS_EN = Lists.newArrayList(CHANNEL_DEFINITION_BRIGHTNESS_EN,
            CHANNEL_DEFINITION_LIGHT_SWITCH_EN, CHANNEL_DEFINITION_ACTIVE_POWER_EN,
            CHANNEL_DEFINITION_ELECTRIC_METER_VALUE_EN, CHANNEL_DEFINITION_OUTPUT_CURRENT_VALUE_EN,
            CHANNEL_DEFINITION_GENERAL_DIMM_EN, CHANNEL_DEFINITION_GENERAL_SWITCH_EN);

    /* German */
    private final List<ChannelDefinition> GE_CHANNELS_DE = Lists.newArrayList(CHANNEL_DEFINITION_BRIGHTNESS_DE,
            CHANNEL_DEFINITION_LIGHT_SWITCH_DE, CHANNEL_DEFINITION_ACTIVE_POWER_DE,
            CHANNEL_DEFINITION_ELECTRIC_METER_VALUE_DE, CHANNEL_DEFINITION_OUTPUT_CURRENT_VALUE_DE);
    private final List<ChannelDefinition> GR_CHANNELS_DE = Lists.newArrayList(CHANNEL_DEFINITION_SHADE_DE);
    private final List<ChannelDefinition> SW_CHANNELS_DE = Lists.newArrayList(CHANNEL_DEFINITION_BRIGHTNESS_DE,
            CHANNEL_DEFINITION_LIGHT_SWITCH_DE, CHANNEL_DEFINITION_ACTIVE_POWER_DE,
            CHANNEL_DEFINITION_ELECTRIC_METER_VALUE_DE, CHANNEL_DEFINITION_OUTPUT_CURRENT_VALUE_DE,
            CHANNEL_DEFINITION_GENERAL_DIMM_DE, CHANNEL_DEFINITION_GENERAL_SWITCH_DE);

    private ChannelDefinition getChannelDefinition(String id, String item, String label, String description,
            String category, Set<String> tags, StateDescription state) {
        return new ChannelDefinition(id, new ChannelType(new ChannelTypeUID(getUID(id)), false, item, label,
                description, category, tags, state, null), null, label, description);
    }

    private String getUID(String id) {
        return DigitalSTROMBindingConstants.BINDING_ID + ":" + id;
    }

    private StateDescription getSensorStateDescription(String shortcutUnit) {
        return new StateDescription(null, null, null, "%d shortcutUnit", true, null);
    }

    private String getUnitString(SensorEnum sensorType) {
        return sensorType.getUnit() + " (" + sensorType.getUnitShortcut() + ")";
    }

    public void registerConnectionManagerHandler(DigitalSTROMConnectionManager connMan) {
        if (connMan != null) {
            if (connMan.checkConnection()) {
                generateReachableThingTypes(
                        connMan.getDigitalSTROMAPI().getApartmentDevices(connMan.getSessionToken(), false));
            }
        }
    }

    @Override
    public Collection<ThingType> getThingTypes(Locale locale) {
        return locale != null && locale.getLanguage().equals(Locale.GERMAN) ? this.thingTypeMapDE.values()
                : this.thingTypeMapEN.values();
    }

    @Override
    public ThingType getThingType(ThingTypeUID thingTypeUID, Locale locale) {
        ThingType thingType = null;
        String hwInfo = thingTypeUID.getId();
        if (locale != null && locale.getDisplayLanguage().equals(Locale.GERMAN)) {
            thingType = this.thingTypeMapDE.get(hwInfo);
        } else {
            thingType = this.thingTypeMapEN.get(hwInfo);
        }

        if (thingType == null) {
            thingType = generateThingType(thingTypeUID, locale);
        }
        return thingType;
    }

    private ThingType generateThingType(ThingTypeUID thingTypeUID, Locale locale) {
        return generateThingType(thingTypeUID.getId(), locale, thingTypeUID);

    }

    private ThingType generateThingType(String hwInfo, Locale locale, ThingTypeUID thingTypeUID) {
        String hwType = hwInfo.substring(0, 2);

        String labelEN;
        String descEN;
        String labelDE;
        String descDE;
        URI configDesc = null;
        List<ChannelDefinition> channelDefsEN = GE_CHANNELS_EN;
        List<ChannelDefinition> channelDefsDE = GE_CHANNELS_DE;

        try {
            configDesc = new URI(DigitalSTROMBindingConstants.DEVICE_CONFIG);
        } catch (URISyntaxException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        switch (hwType) {
            case "GE":
                labelEN = PREFIX_LABEL_EN + hwInfo + " " + GE_EN;
                descEN = PREFIX_DESC_EN + " " + GE_FUNC_EN + " " + POSTFIX_DESC_EN;
                labelDE = PREFIX_LABEL_DE + hwInfo + " " + GE_DE;
                descDE = PREFIX_DESC_DE + " " + GE_FUNC_DE + " " + POSTFIX_DESC_DE;
                break;
            case "GR":
                labelEN = PREFIX_LABEL_EN + hwInfo + " " + GR_EN;
                descEN = PREFIX_DESC_EN + " " + GR_FUNC_EN + " " + POSTFIX_DESC_EN;
                labelDE = PREFIX_LABEL_DE + hwInfo + " " + GR_DE;
                descDE = PREFIX_DESC_DE + " " + GR_FUNC_DE + " " + POSTFIX_DESC_DE;
                channelDefsEN = GR_CHANNELS_EN;
                channelDefsDE = GR_CHANNELS_DE;
                try {
                    configDesc = new URI(DigitalSTROMBindingConstants.GRAY_DEVICE_CONFIG);
                } catch (URISyntaxException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                break;
            case "SW":
                if (hwInfo.contains("ZS")) {
                    labelEN = PREFIX_LABEL_PLUGIN_ADAPTER_EN + hwInfo + " " + SW_EN;
                    descEN = PREFIX_DESC_EN + " " + SW_FUNC_EN + " " + POSTFIX_DESC_PLUGIN_ADAPTER_EN;
                    labelDE = PREFIX_LABEL_PLUGIN_ADAPTER_DE + hwInfo + " " + SW_DE;
                    descDE = PREFIX_DESC_DE + " " + SW_FUNC_DE + " " + POSTFIX_DESC_PLUGIN_ADAPTER_DE;
                } else {
                    labelEN = PREFIX_LABEL_EN + hwInfo + " " + SW_EN;
                    descEN = PREFIX_DESC_EN + " " + SW_FUNC_EN + " " + POSTFIX_DESC_EN;
                    labelDE = PREFIX_LABEL_DE + hwInfo + " " + SW_DE;
                    descDE = PREFIX_DESC_DE + " " + SW_FUNC_DE + " " + POSTFIX_DESC_DE;
                }
                channelDefsEN = SW_CHANNELS_EN;
                channelDefsDE = SW_CHANNELS_DE;
                break;
            default:
                return null;
        }

        if (thingTypeUID == null) {
            thingTypeUID = new ThingTypeUID(DigitalSTROMBindingConstants.BINDING_ID, hwInfo);
        }

        ThingType thingTypeEN = new ThingType(thingTypeUID, supportedBridgeTypeUIDs, labelEN, descEN, channelDefsEN,
                null, null, configDesc);
        this.thingTypeMapEN.put(hwInfo, thingTypeEN);
        ThingType thingTypeDE = new ThingType(thingTypeUID, supportedBridgeTypeUIDs, labelDE, descDE, channelDefsDE,
                null, null, configDesc);
        this.thingTypeMapDE.put(hwInfo, thingTypeDE);

        DsDeviceHandler.SUPPORTED_THING_TYPES.add(thingTypeUID);

        if (locale != null) {
            return locale.getLanguage().equals(Locale.GERMAN) ? thingTypeDE : thingTypeEN;
        }

        return null;
    }

    private void generateReachableThingTypes(List<Device> deviceList) {
        if (deviceList != null) {
            for (Device device : deviceList) {
                if (!this.thingTypeMapDE.containsKey(device.getHWinfo())) {
                    if (device.isDeviceWithOutput()) {
                        generateThingType(device.getHWinfo(), null, null);
                    }
                }
            }
        }
    }

}
