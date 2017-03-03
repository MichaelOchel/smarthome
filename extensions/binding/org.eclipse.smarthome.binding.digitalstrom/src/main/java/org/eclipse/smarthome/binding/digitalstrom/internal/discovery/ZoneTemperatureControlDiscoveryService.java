/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.discovery;

import static org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants.BINDING_ID;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.handler.BridgeHandler;
import org.eclipse.smarthome.binding.digitalstrom.handler.ZoneTemperatureControlHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlStatus;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link ZoneTemperatureControlDiscoveryService} discovers all digitalSTROM zones which have temperature control
 * configured. The thing-type has to be given to the
 * {@link #ZoneTemperatureControlDiscoveryService(BridgeHandler, ThingTypeUID)} as {@link ThingTypeUID}. The supported
 * {@link ThingTypeUID} can be found at {@link ZoneTemperatureControlHandler#SUPPORTED_THING_TYPES}
 *
 * @author Michael Ochel
 * @author Matthias Siegele
 */
public class ZoneTemperatureControlDiscoveryService extends AbstractDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(ZoneTemperatureControlDiscoveryService.class);
    BridgeHandler bridgeHandler;
    private final ThingUID BRIDGE_UID;
    private final String THING_TYPE_ID;

    /**
     * Creates a new {@link ZoneTemperatureControlDiscoveryService}.
     *
     * @param bridgeHandler must not be null
     * @param supportedThingType must not be null
     * @throws IllegalArgumentException see {@link AbstractDiscoveryService#AbstractDiscoveryService(int)}
     */
    public ZoneTemperatureControlDiscoveryService(BridgeHandler bridgeHandler, ThingTypeUID supportedThingType)
            throws IllegalArgumentException {
        super(Sets.newHashSet(supportedThingType), 10, true);
        BRIDGE_UID = bridgeHandler.getThing().getUID();
        this.bridgeHandler = bridgeHandler;
        THING_TYPE_ID = supportedThingType.getId();
    }

    @Override
    protected void startScan() {
        for (TemperatureControlStatus tempConStat : bridgeHandler.getTemperatureControlStatusFromAllZones()) {
            internalConfigChanged(tempConStat);
        }
    }

    @Override
    public void deactivate() {
        logger.debug("deactivate discovery service for zone teperature control type remove thing types "
                + super.getSupportedThingTypes().toString());
        removeOlderResults(new Date().getTime());
    }

    /**
     * Method for the background discovery
     *
     * @see org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.TemperatureControlStatusListener#configChanged(TemperatureControlStatus)
     * @param tempControlStatus can be null
     */
    public void configChanged(TemperatureControlStatus tempControlStatus) {
        if (isBackgroundDiscoveryEnabled()) {
            internalConfigChanged(tempControlStatus);
        }
    }

    private void internalConfigChanged(TemperatureControlStatus tempControlStatus) {
        if (tempControlStatus != null) {
            if (tempControlStatus.getIsConfigured()) {
                logger.debug("found configured zone TemperatureControlStatus=" + tempControlStatus);

                ThingUID thingUID = getThingUID(tempControlStatus);
                if (thingUID != null) {
                    Map<String, Object> properties = new HashMap<>(1);
                    properties.put(DigitalSTROMBindingConstants.ZONE_ID, tempControlStatus.getZoneID());
                    String zoneName = tempControlStatus.getZoneName();
                    if (StringUtils.isBlank(zoneName)) {
                        zoneName = tempControlStatus.getZoneID().toString();
                    }
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                            .withBridge(BRIDGE_UID).withLabel(zoneName).build();
                    thingDiscovered(discoveryResult);

                }
            }
        }
    }

    private ThingUID getThingUID(TemperatureControlStatus tempControlStatus) {
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, THING_TYPE_ID);
        if (getSupportedThingTypes().contains(thingTypeUID)) {
            String thingID = tempControlStatus.getZoneID().toString();
            ThingUID thingUID = new ThingUID(thingTypeUID, BRIDGE_UID, thingID);
            return thingUID;
        } else {
            return null;
        }
    }

    /**
     * Returns the ID of this {@link ZoneTemperatureControlDiscoveryService}.
     *
     * @return id of the service
     */
    public String getID() {
        return THING_TYPE_ID;
    }
}
