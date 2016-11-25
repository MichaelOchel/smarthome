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
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringUnitsEnum;
import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class DsDeviceThingTypeProvider implements ThingTypeProvider {

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

    private I18nProvider i18n = null;
    private Bundle bundle = null;
    private Logger logger = LoggerFactory.getLogger(DsDeviceThingTypeProvider.class);

    private final String DEVICE_WITH_POWER_SENSORS = "binding:digitalstrom:deviceWithPowerSensors";
    private final String DEVICE_WITHOUT_POWER_SENSORS = "binding:digitalstrom:deviceWithoutPowerSensors";

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

    private void init() {
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

    private String getText(String key, Locale locale) {
        return i18n != null ? i18n.getText(bundle, key, i18n.getText(bundle, key, key, Locale.ENGLISH), locale) : key;
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
                    for (MeteringUnitsEnum meteringUnit : meteringType.getMeteringUnitList()) {
                        channelDefinitions
                                .add(new ChannelDefinition(meteringType.toString() + "_" + meteringUnit.toString(),
                                        new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID,
                                                meteringType.toString() + "_" + meteringUnit.toString())));
                    }
                }
            }

            return new ThingType(thingTypeUID,
                    Lists.newArrayList(DigitalSTROMBindingConstants.THING_TYPE_ID_DSS_BRIDGE),
                    getText(thingTypeUID.getId() + "_label", locale), getText(thingTypeUID.getId() + "_desc", locale),
                    channelDefinitions, null, null, configDesc);
        } catch (IllegalArgumentException e) {
            // ignore
        }
        return null;
    }

}
