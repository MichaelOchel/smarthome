/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.providers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.handler.CircuitHandler;
import org.eclipse.smarthome.binding.digitalstrom.handler.DeviceHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.GeneralDeviceInformations;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringUnitsEnum;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * The {@link DsDeviceThingTypeProvider} creates the {@link ThingType}'s for the subclasses of the
 * {@link GeneralDeviceInformations}. It also adds the {@link ThingTypeUID}'s to the related handlers. So only the
 * {@link SupportedThingTypes} enum has to be adjusted, if new device types of digitalSTROM should be supported.
 * Provided the new digitalSTROM devices uses the same mechanism like now.
 *
 * @author Michael Ochel - initial contributer
 * @author Matthias Siegele - initial contributer
 */
public class DsDeviceThingTypeProvider extends BaseDsI18n implements ThingTypeProvider {

    /**
     * Through the {@link SupportedThingTypes} the {@link ThingType}'s will be created. For that the enum name will be
     * used as thing type id, the first field will set the responsible handler and the last enum field will set the
     * supporting of the power sensor refresh configurations (config-description with refresh priority setting or not).
     *
     * @author Michael Ochel - initial contributer
     * @author Matthias Siegele - initial contributer
     */
    public static enum SupportedThingTypes {
        // ThingType, responsible ThingHanlder, Device config-description with power-sensors
        GE(DeviceHandler.class.getSimpleName(), true),
        GR(DeviceHandler.class.getSimpleName(), false),
        SW(DeviceHandler.class.getSimpleName(), true),
        BL(DeviceHandler.class.getSimpleName(), true),
        dSiSens200(DeviceHandler.class.getSimpleName(), false),
        circuit(CircuitHandler.class.getSimpleName(), false);

        private final String handler;
        private final boolean havePowerSensors;

        private SupportedThingTypes(String handler, boolean havePowerSensors) {
            this.handler = handler;
            this.havePowerSensors = havePowerSensors;
        }
    }

    private Logger logger = LoggerFactory.getLogger(DsDeviceThingTypeProvider.class);

    private final String DEVICE_WITH_POWER_SENSORS = "binding:digitalstrom:deviceWithPowerSensors";
    private final String DEVICE_WITHOUT_POWER_SENSORS = "binding:digitalstrom:deviceWithoutPowerSensors";

    @Override
    protected void init() {
        for (SupportedThingTypes supportedThingType : SupportedThingTypes.values()) {
            if (supportedThingType.handler.equals(DeviceHandler.class.getSimpleName())) {
                DeviceHandler.SUPPORTED_THING_TYPES
                        .add(new ThingTypeUID(DigitalSTROMBindingConstants.BINDING_ID, supportedThingType.toString()));
            }
            if (supportedThingType.handler.equals(CircuitHandler.class.getSimpleName())) {
                CircuitHandler.SUPPORTED_THING_TYPES
                        .add(new ThingTypeUID(DigitalSTROMBindingConstants.BINDING_ID, supportedThingType.toString()));
            }
        }
    }

    @Override
    public Collection<ThingType> getThingTypes(Locale locale) {
        List<ThingType> thingTypes = new LinkedList<ThingType>();
        for (SupportedThingTypes supportedThingType : SupportedThingTypes.values()) {
            thingTypes.add(getThingType(
                    new ThingTypeUID(DigitalSTROMBindingConstants.BINDING_ID, supportedThingType.toString()), locale));
        }
        return thingTypes;
    }

    @Override
    public ThingType getThingType(ThingTypeUID thingTypeUID, Locale locale) {
        try {
            SupportedThingTypes supportedThingType = SupportedThingTypes.valueOf(thingTypeUID.getId());
            URI configDesc = null;
            try {
                if (supportedThingType.havePowerSensors) {
                    configDesc = new URI(DEVICE_WITH_POWER_SENSORS);
                } else {
                    configDesc = new URI(DEVICE_WITHOUT_POWER_SENSORS);
                }
            } catch (URISyntaxException e) {
                logger.debug("An URISyntaxException occurred: ", e);
            }
            List<ChannelDefinition> channelDefinitions = null;
            if (SupportedThingTypes.GR.equals(supportedThingType)) {
                channelDefinitions = Lists.newArrayList(new ChannelDefinition(DsChannelTypeProvider.SHADE,
                        new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID, DsChannelTypeProvider.SHADE)));
            }
            if (SupportedThingTypes.circuit.equals(supportedThingType)) {
                channelDefinitions = new ArrayList<ChannelDefinition>(3);
                for (MeteringTypeEnum meteringType : MeteringTypeEnum.values()) {
                    channelDefinitions.add(new ChannelDefinition(
                            DsChannelTypeProvider.getMeteringChannelID(meteringType, MeteringUnitsEnum.WH, false),
                            new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID, DsChannelTypeProvider
                                    .getMeteringChannelID(meteringType, MeteringUnitsEnum.WH, false))));
                }
            }

            return new ThingType(thingTypeUID,
                    Lists.newArrayList(DigitalSTROMBindingConstants.THING_TYPE_DSS_BRIDGE.getAsString()),
                    getLabelText(thingTypeUID.getId(), locale), getDescText(thingTypeUID.getId(), locale),
                    channelDefinitions, null, null, configDesc);
        } catch (IllegalArgumentException e) {
            // ignore
        }
        return null;
    }

}
