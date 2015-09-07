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

import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.handler.DsDeviceHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.Device;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;

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
    /* English */
    private final ChannelDefinition CHANNEL_DEFINITION_BRIGHTNESS_EN = new ChannelDefinition("brightness",
            new ChannelType(new ChannelTypeUID("digitalstrom:brightness"), false, "Dimmer", "brightness",
                    "The brightness channel allows to dimm a light Device.", "dimmableLight", null, null, null),
            null, "brightness", "The brightness channel allows to dimm a light Device.");

    private final ChannelDefinition CHANNEL_DEFINITION_LIGHT_SWITCH_EN = new ChannelDefinition("lightSwitch",
            new ChannelType(new ChannelTypeUID("digitalstrom:lightSwitch"), false, "Switch", "light switch channel",
                    "The light switch channel allows to turn a light Device on or off.", "light", null, null, null),
            null, "light switch channel", "The light switch channel allows to turn a light Device on or off.");
    /**
     * private final ChannelDefinition CHANNEL_DEFINITION_SCENE_EN = new ChannelDefinition("scene",
     * new ChannelType(new ChannelTypeUID("digitalstrom:scene"), false, "Switch", "scene channel",
     * "this is a channel to turn a scene on or off", "scene", null, null, null),
     * null, "scene channel", "this is a channel to turn a scene on or off");
     **/

    // TODO: digitalSTROM Sensoren auf Richtigkeit überprüfen. States Description hinzufügen
    private final ChannelDefinition CHANNEL_DEFINITION_POWER_CONSUMPTION_EN = new ChannelDefinition("powerConsumption",
            new ChannelType(new ChannelTypeUID("digitalstrom:powerConsumption"), false, "Number",
                    "power consumption channel",
                    "The power consumption channel sums up the power consuption of this device.", "Energy", null, null,
                    null),
            null, "power consumption channel",
            "The power consumption channel sums up the power consuption of this device.");

    private final ChannelDefinition CHANNEL_DEFINITION_ELECTRIC_METER_VALUE_EN = new ChannelDefinition("electricMeter",
            new ChannelType(new ChannelTypeUID("digitalstrom:electricMeter"), false, "Number", "electric meter channel",
                    "The electric meter channel allows to get a electric meter value.", "Energy", null, null, null),
            null, "electric meter channel", "The electric meter channel allows to get a electric meter value.");

    private final ChannelDefinition CHANNEL_DEFINITION_ENERGY_METER_VALUE_EN = new ChannelDefinition("energyMeter",
            new ChannelType(new ChannelTypeUID("digitalstrom:energyMeter"), false, "Number", "energy meter channel",
                    "The energy meter channel allows to get a energy meter value.", "Energy", null, null, null),
            null, "energy meter channel", "The energy meter channel allows to get a energy meter value.");

    private final ChannelDefinition CHANNEL_DEFINITION_SHADE_EN = new ChannelDefinition("shade",
            new ChannelType(new ChannelTypeUID("digitalstrom:shade"), false, "Rollershutter", "Shade control",
                    "The Shade control channel allows to control shade device e.g. a rollershutter or awnings.",
                    "Blinds", null, null, null),
            null, "Shade control", "The Shade control channel allows to control shade device e.g. a rollershutter.");
    /*
     * private final ChannelDefinition CHANNEL_DEFINITION_PLUG_ADAPTER_EN = new ChannelDefinition("plugAdapter",
     * new ChannelType(new ChannelTypeUID("digitalstrom:plugAdapter"), false, "Switch", "Plug adapter",
     * "The plug adapter channel allows to switch the plug adapter on or off.", "Light", null, null, null),
     * null, "Plug adapter", "The plug adapter channel allows to switch the plug adapter on or off.");
     */
    // TODO: Category Joker? Plug Adapter löschen!

    private final ChannelDefinition CHANNEL_DEFINITION_GENERAL_DIMM_EN = new ChannelDefinition("generalDimm",
            new ChannelType(new ChannelTypeUID("digitalstrom:generalDimm"), false, "Dimmer",
                    "devide power control channel",
                    "The devide power control channel allows to control the power of a Device e.g. a ceiling fan.",
                    null, null, null, null),
            null, "general Dimmer",
            "The devide power control channel allows to control the power of a Device e.g. a ceiling fan..");

    private final ChannelDefinition CHANNEL_DEFINITION_GENERAL_SWITCH_EN = new ChannelDefinition("generalSwitch",
            new ChannelType(new ChannelTypeUID("digitalstrom:generalSwitch"), false, "Switch", "device switch",
                    "The device switch channel allows to turn a Device e.g. a HIFI-System.", "Switch", null, null,
                    null),
            null, "general Switch", "The device switch channel allows to turn a Device e.g. a HIFI-System.");

    /* German */
    private final ChannelDefinition CHANNEL_DEFINITION_BRIGHTNESS_DE = new ChannelDefinition("brightness",
            new ChannelType(new ChannelTypeUID("digitalstrom:brightness"), false, "Dimmer", "Helligkeit",
                    "Regelt die Helligkeit des Lichtes.", "dimmableLight", null, null, null),
            null, "brightness", "Regelt die Helligkeit des Lichtes.");

    private final ChannelDefinition CHANNEL_DEFINITION_LIGHT_SWITCH_DE = new ChannelDefinition("lightSwitch",
            new ChannelType(new ChannelTypeUID("digitalstrom:lightSwitch"), false, "Switch", "Lichtschalter",
                    "Schaltet ein Licht ein oder aus.", "light", null, null, null),
            null, "Lichtschalter", "Schaltet ein Licht ein oder aus.");

    // TODO: digitalSTROM Sensoren auf Richtigkeit überprüfen. States Description hinzufügen.

    private final ChannelDefinition CHANNEL_DEFINITION_POWER_CONSUMPTION_DE = new ChannelDefinition("powerConsumption",
            new ChannelType(new ChannelTypeUID("digitalstrom:powerConsumption"), false, "Number", "Stromverbrauch",
                    "Gibt den aktuellen Stromverbrauch des Geräts in Watt an.", "Energy", null, null, null),
            null, "Stromverbrauch", "Gibt den aktuellen Stromverbrauch des Geräts in Watt an.");

    private final ChannelDefinition CHANNEL_DEFINITION_ELECTRIC_METER_VALUE_DE = new ChannelDefinition("electricMeter",
            new ChannelType(new ChannelTypeUID("digitalstrom:electricMeter"), false, "Number", "Stromverbrauch",
                    "Übergibt den gesamten Stromverbrauch in Milliampere.", "Energy", null, null, null),
            null, "Stromverbrauch", "Übergibt den gesamten Stromverbrauch in Milliampere.");

    private final ChannelDefinition CHANNEL_DEFINITION_ENERGY_METER_VALUE_DE = new ChannelDefinition("energyMeter",
            new ChannelType(new ChannelTypeUID("digitalstrom:energyMeter"), false, "Number", "energy meter channel",
                    "The energy meter channel allows to get a energy meter value.", "Energy", null, null, null),
            null, "energy meter channel", "The energy meter channel allows to get a energy meter value.");

    private final ChannelDefinition CHANNEL_DEFINITION_SHADE_DE = new ChannelDefinition("shade",
            new ChannelType(new ChannelTypeUID("digitalstrom:shade"), false, "Rollershutter", "Schattensteuerung",
                    "Erlaubt die Schattensteuerung z.B. von Rollladen oder Markisen.", "Blinds", null, null, null),
            null, "Schattensteuerung", "Erlaubt die Schattensteuerung z.B. von Rollladen oder Markisen.");
    /*
     * private final ChannelDefinition CHANNEL_DEFINITION_PLUG_ADAPTER_DE = new ChannelDefinition("plugAdapter",
     * new ChannelType(new ChannelTypeUID("digitalstrom:plugAdapter"), false, "Switch", "Plug adapter",
     * "The plug adapter channel allows to switch the plug adapter on or off.", "Light", null, null, null),
     * null, "Plug adapter", "The plug adapter channel allows to switch the plug adapter on or off.");
     */
    // TODO: Category Joker? Plug adapter löschen!

    private final ChannelDefinition CHANNEL_DEFINITION_GENERAL_DIMM_DE = new ChannelDefinition("generalDimm",
            new ChannelType(new ChannelTypeUID("digitalstrom:generalDimm"), false, "Dimmer",
                    "Einstellung Geräteleistung", "Stellt die Leistung eines Gerätes ein z.B. eines Deckenventilators.",
                    null, null, null, null),
            null, "Einstellung Geräteleistung", "Stellt die Leistung eines Gerätes ein z.B. eines Deckenventilators.");

    private final ChannelDefinition CHANNEL_DEFINITION_GENERAL_SWITCH_DE = new ChannelDefinition("generalSwitch",
            new ChannelType(new ChannelTypeUID("digitalstrom:generalSwitch"), false, "Switch", "Geräteschalter",
                    "Schaltet ein Gerät ein und aus z.B. ein HIFI-Anlage.", "Switch", null, null, null),
            null, "Geräteschalter", "Schaltet ein Gerät ein und aus z.B. ein HIFI-Anlage.");

    /* ChannelList */
    /* English */
    private final List<ChannelDefinition> GE_CHANNELS_EN = Lists.newArrayList(CHANNEL_DEFINITION_BRIGHTNESS_EN,
            CHANNEL_DEFINITION_LIGHT_SWITCH_EN, CHANNEL_DEFINITION_POWER_CONSUMPTION_EN,
            CHANNEL_DEFINITION_ELECTRIC_METER_VALUE_EN, CHANNEL_DEFINITION_ENERGY_METER_VALUE_EN);
    private final List<ChannelDefinition> GR_CHANNELS_EN = Lists.newArrayList(CHANNEL_DEFINITION_SHADE_EN);
    private final List<ChannelDefinition> SW_CHANNELS_EN = Lists.newArrayList(CHANNEL_DEFINITION_BRIGHTNESS_EN,
            CHANNEL_DEFINITION_LIGHT_SWITCH_EN, CHANNEL_DEFINITION_POWER_CONSUMPTION_EN,
            CHANNEL_DEFINITION_ELECTRIC_METER_VALUE_EN, CHANNEL_DEFINITION_ENERGY_METER_VALUE_EN,
            CHANNEL_DEFINITION_GENERAL_DIMM_EN, CHANNEL_DEFINITION_GENERAL_SWITCH_EN);

    /* German */
    private final List<ChannelDefinition> GE_CHANNELS_DE = Lists.newArrayList(CHANNEL_DEFINITION_BRIGHTNESS_DE,
            CHANNEL_DEFINITION_LIGHT_SWITCH_DE, CHANNEL_DEFINITION_POWER_CONSUMPTION_DE,
            CHANNEL_DEFINITION_ELECTRIC_METER_VALUE_DE, CHANNEL_DEFINITION_ENERGY_METER_VALUE_DE);
    private final List<ChannelDefinition> GR_CHANNELS_DE = Lists.newArrayList(CHANNEL_DEFINITION_SHADE_DE);
    private final List<ChannelDefinition> SW_CHANNELS_DE = Lists.newArrayList(CHANNEL_DEFINITION_BRIGHTNESS_DE,
            CHANNEL_DEFINITION_LIGHT_SWITCH_DE, CHANNEL_DEFINITION_POWER_CONSUMPTION_DE,
            CHANNEL_DEFINITION_ELECTRIC_METER_VALUE_DE, CHANNEL_DEFINITION_ENERGY_METER_VALUE_DE,
            CHANNEL_DEFINITION_GENERAL_DIMM_DE, CHANNEL_DEFINITION_GENERAL_SWITCH_DE);

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
