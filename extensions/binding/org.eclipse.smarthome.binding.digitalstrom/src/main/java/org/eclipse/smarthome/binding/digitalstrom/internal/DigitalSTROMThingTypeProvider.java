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
    /* English */
    private final ChannelDefinition CHANNEL_DEFINITION_BRIGHTNESS_EN = new ChannelDefinition("brightness",
            new ChannelType(new ChannelTypeUID("digitalstrom:brightness"), false, "Dimmer", "Dimm channel",
                    "this is a channel to dimm an light Device", "light", null, null, null),
            null, "Dimm channel", "this is a channel to to dimm an light Device");

    private final ChannelDefinition CHANNEL_DEFINITION_LIGHT_SWITCH_EN = new ChannelDefinition("lightSwitch",
            new ChannelType(new ChannelTypeUID("digitalstrom:lightSwitch"), false, "Switch", "light switch channel",
                    "this is a channel to turn an Device on or off", "light", null, null, null),
            null, "light switch channel", "this is a channel to turn an Device on or off");
    /*
     * public ChannelDefinition(String id, ChannelType type, Map<String, String> properties, String label,
     * String description)
     */
    /*
     * public StateDescription(BigDecimal minimum, BigDecimal maximum, BigDecimal step, String pattern, boolean
     * readOnly,
     * List<StateOption> options)
     */
    /*
     * <channel-type id="brightness">
     * <item-type>Dimmer</item-type>
     * <label>Light brightness</label>
     * <description>The brightness channel allows to control the brightness of a light.
     * It is also possible to switch the light on and off.
     * </description>
     * <category>DimmableLight</category>
     * </channel-type>
     * <channel-type id="lightSwitch">
     * <item-type>Switch</item-type>
     * <label>Light switch</label>
     * <description>The light switch channel allows to switch the light on and off.
     * </description>
     * <category>Light</category>
     * </channel-type>
     * <!--Channel for shades-->
     * <channel-type id="shade">
     * <item-type>Rollershutter</item-type>
     * <label>Shade control</label>
     * <description>The Shade controll channel allows to control shade device e.g. a rollershutter.
     * </description>
     * <category>Blinds</category>
     * </channel-type>
     * <!--Plug adapter--><channel-type id="plugAdapter">
     * <item-type>Switch</item-type>
     * <label>Plug adapter</label>
     * <description>The plug adapter channel allows to switch the plug adapter on or off.
     * </description>
     * <category>Light</category>
     * </channel-type>
     * <!--Scene channel-->
     * <channel-type id="scene">
     * <item-type>Switch</item-type>
     * <label>Scene</label>
     * <description>The scene channel allows to call or undo a scene from DigitalSTROM.
     * </description>
     * <tags>
     * <tag>Scene</tag>
     * </tags>
     * </channel-type><!--Sensor channels-->
     * <channel-type id="powerConsumption">
     * <item-type>Number</item-type>
     * <label>Power consumption</label>
     * <description>The power consumption channel shows the current power consumption from this device.</description>
     * <category>Energy</category>
     * <tags>
     * <tag>power consumption</tag>
     * </tags>
     * <state readOnly="true" pattern="%d W"></state>
     * </channel-type>
     * <channel-type id="electricMeterValue">
     * <item-type>Number</item-type>
     * <label>Electric meter value</label>
     * <description>The electric meter value channel shows the current electric meter value from this
     * device.</description>
     * <category>Energy</category>
     * <tags>
     * <tag>electric meter value</tag>
     * </tags>
     * <state readOnly="true" pattern="%d mA"></state>
     * </channel-type>
     * <channel-type id="energyMeterValue">
     * <item-type>Number</item-type>
     * <label>Energy meter value</label>
     * <description>The energy meter value channel shows the current energy meter value from this device.</description>
     * <category>Energy</category>
     * <tags>
     * <tag>energy meter value</tag>
     * </tags>
     * <state readOnly="true" pattern="%d Wh"></state>
     * </channel-type>
     */

    private StateDescription getStateDescription(String plattern) {
        return new StateDescription(null, null, null, plattern, true, null);
    }

    private final StateDescription STATE_DESC_POWER_CONSUMPTION = new StateDescription(null, null, null, "%d W", true,
            null);
    private final ChannelType CHANNEL_TYPE_POWER_CONSUMPTION = new ChannelType(
            new ChannelTypeUID(
                    DigitalSTROMBindingConstants.BINDING_ID + ":" + DigitalSTROMBindingConstants.CHANNEL_LIGHT_SWITCH),
            false, "Switch", "light switch channel", "this is a channel to turn an light device on or off", "light",
            null, getStateDescription("%d W"), null);

    private final ChannelDefinition CHANNEL_DEFINITION_POWER_CONSUMPTION_EN = new ChannelDefinition("lightSwitch",
            CHANNEL_TYPE_POWER_CONSUMPTION, null, "light switch channel",
            "this is a channel to turn an Device on or off");

    /* ChannelList */
    /* English */
    private final List<ChannelDefinition> GE_CHANNELS_EN = Lists.newArrayList(CHANNEL_DEFINITION_BRIGHTNESS_EN,
            CHANNEL_DEFINITION_LIGHT_SWITCH_EN);
    private final List<ChannelDefinition> GR_CHANNELS_EN = Lists.newArrayList();
    private final List<ChannelDefinition> SW_CHANNELS_EN = Lists.newArrayList(GE_CHANNELS_EN);

    /* German */
    private final List<ChannelDefinition> GE_CHANNELS_DE = Lists.newArrayList();
    private final List<ChannelDefinition> SW_CHANNELS_DE = Lists.newArrayList();
    private final List<ChannelDefinition> GR_CHANNELS_DE = Lists.newArrayList();

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
