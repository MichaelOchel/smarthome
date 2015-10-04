/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.providers;

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

    /* Channel definitions */
    private final ChannelDefinition CHANNEL_DEFINITION_BRIGHTNESS = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_BRIGHTNESS);

    private final ChannelDefinition CHANNEL_DEFINITION_LIGHT_SWITCH = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_LIGHT_SWITCH);

    private final ChannelDefinition CHANNEL_DEFINITION_GENERAL_DIMM = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_GENERAL_DIMM);

    private final ChannelDefinition CHANNEL_DEFINITION_GENERAL_SWITCH = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_GENERAL_SWITCH);

    private final ChannelDefinition CHANNEL_DEFINITION_COMBINED_2_STAGE_SWITCH = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_COMBINED_2_STAGE_SWITCH);

    private final ChannelDefinition CHANNEL_DEFINITION_COMBINED_3_STAGE_SWITCH = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_COMBINED_3_STAGE_SWITCH);

    private final ChannelDefinition CHANNEL_DEFINITION_GENERAL_COMBINED_2_STAGE_SWITCH = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH);

    private final ChannelDefinition CHANNEL_DEFINITION_GENERAL_COMBINED_3_STAGE_SWITCH = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH);

    private final ChannelDefinition CHANNEL_DEFINITION_SHADE = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_SHADE);

    private final ChannelDefinition CHANNEL_DEFINITION_ACTIVE_POWER = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_ACTIVE_POWER);

    private final ChannelDefinition CHANNEL_DEFINITION_ELECTRIC_METER_VALUE = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_ELECTRIC_METER);

    private final ChannelDefinition CHANNEL_DEFINITION_OUTPUT_CURRENT_VALUE = getChannelDefinition(
            DigitalSTROMBindingConstants.CHANNEL_OUTPUT_CURRENT);

    /* ChannelLists */
    private final List<ChannelDefinition> GE_CHANNELS = Lists.newArrayList(CHANNEL_DEFINITION_BRIGHTNESS,
            CHANNEL_DEFINITION_LIGHT_SWITCH, CHANNEL_DEFINITION_ACTIVE_POWER, CHANNEL_DEFINITION_ELECTRIC_METER_VALUE,
            CHANNEL_DEFINITION_OUTPUT_CURRENT_VALUE);
    private final List<ChannelDefinition> GR_CHANNELS = Lists.newArrayList(CHANNEL_DEFINITION_SHADE);
    private final List<ChannelDefinition> SW_CHANNELS = Lists.newArrayList(CHANNEL_DEFINITION_BRIGHTNESS,
            CHANNEL_DEFINITION_LIGHT_SWITCH, CHANNEL_DEFINITION_ACTIVE_POWER, CHANNEL_DEFINITION_ELECTRIC_METER_VALUE,
            CHANNEL_DEFINITION_OUTPUT_CURRENT_VALUE, CHANNEL_DEFINITION_GENERAL_DIMM,
            CHANNEL_DEFINITION_GENERAL_SWITCH);
    private final List<ChannelDefinition> SW_UMR_CHANNELS = Lists.newArrayList(
            CHANNEL_DEFINITION_COMBINED_2_STAGE_SWITCH, CHANNEL_DEFINITION_COMBINED_3_STAGE_SWITCH,
            CHANNEL_DEFINITION_GENERAL_COMBINED_2_STAGE_SWITCH, CHANNEL_DEFINITION_GENERAL_COMBINED_3_STAGE_SWITCH,
            CHANNEL_DEFINITION_LIGHT_SWITCH, CHANNEL_DEFINITION_ACTIVE_POWER, CHANNEL_DEFINITION_ELECTRIC_METER_VALUE,
            CHANNEL_DEFINITION_OUTPUT_CURRENT_VALUE);

    private ChannelDefinition getChannelDefinition(String id) {
        return new ChannelDefinition(id, new ChannelTypeUID(getUID(id)));
    }

    /*
     * new ChannelType(new ChannelTypeUID(getUID(id)), false, item, label,
     * description, category, tags, state, null), null, label, description)
     */
    private String getUID(String id) {
        return DigitalSTROMBindingConstants.BINDING_ID + ":" + id;
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
        return locale != null && locale.equals(Locale.GERMAN) ? this.thingTypeMapDE.values()
                : this.thingTypeMapEN.values();
    }

    @Override
    public ThingType getThingType(ThingTypeUID thingTypeUID, Locale locale) {
        ThingType thingType = null;
        String hwInfo = thingTypeUID.getId();
        if (locale != null && locale.equals(Locale.GERMAN)) {
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
        List<ChannelDefinition> channelDefs = GE_CHANNELS;

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
                channelDefs = GR_CHANNELS;
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
                if (hwInfo.contains("UMR")) {
                    channelDefs = SW_UMR_CHANNELS;
                } else {
                    channelDefs = SW_CHANNELS;
                }
                break;
            default:
                return null;
        }

        if (thingTypeUID == null) {
            thingTypeUID = new ThingTypeUID(DigitalSTROMBindingConstants.BINDING_ID, hwInfo);
        }

        ThingType thingTypeEN = new ThingType(thingTypeUID, supportedBridgeTypeUIDs, labelEN, descEN, channelDefs, null,
                null, configDesc);
        this.thingTypeMapEN.put(hwInfo, thingTypeEN);
        ThingType thingTypeDE = new ThingType(thingTypeUID, supportedBridgeTypeUIDs, labelDE, descDE, channelDefs, null,
                null, configDesc);
        this.thingTypeMapDE.put(hwInfo, thingTypeDE);

        DsDeviceHandler.SUPPORTED_THING_TYPES.add(thingTypeUID);

        if (locale != null) {
            return locale.equals(Locale.GERMAN) ? thingTypeDE : thingTypeEN;
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
