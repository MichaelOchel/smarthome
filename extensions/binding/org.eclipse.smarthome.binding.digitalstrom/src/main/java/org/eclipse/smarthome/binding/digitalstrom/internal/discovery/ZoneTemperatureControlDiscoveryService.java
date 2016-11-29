package org.eclipse.smarthome.binding.digitalstrom.internal.discovery;

import static org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants.BINDING_ID;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.handler.BridgeHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlStatus;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.TemperatureControlStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.TemperatureSensorTransreciver;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class ZoneTemperatureControlDiscoveryService extends AbstractDiscoveryService
        implements TemperatureControlStatusListener {

    private Logger logger = LoggerFactory.getLogger(ZoneTemperatureControlDiscoveryService.class);
    // private TemperatureControlManager tempConMan;
    BridgeHandler bridgeHandler;
    private final ThingUID BRIDGE_UID;

    public ZoneTemperatureControlDiscoveryService(BridgeHandler bridgeHandler, ThingTypeUID supportedThingType)
            throws IllegalArgumentException {
        super(Sets.newHashSet(supportedThingType), 10, false);
        // tempConMan = bridgeHandler.getTemperatureControlManager();
        BRIDGE_UID = bridgeHandler.getThing().getUID();
        this.bridgeHandler = bridgeHandler;
        bridgeHandler.registerTemperatureControlStatusListener(this);
    }

    @Override
    protected void startScan() {
        if (bridgeHandler.getTemperatureControlManager() != null) {
            if (bridgeHandler.getTemperatureControlManager().getTemperatureControlStatusFromAllZones() != null) {
                for (TemperatureControlStatus tempConStat : bridgeHandler.getTemperatureControlManager()
                        .getTemperatureControlStatusFromAllZones()) {
                    internalConfigChanged(tempConStat);
                }
            }
        }
    }

    @Override
    public void deactivate() {
        logger.debug("deactivate discovery service for zone teperature control type remove thing types "
                + super.getSupportedThingTypes().toString());
        removeOlderResults(new Date().getTime());
    }

    @Override
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
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID,
                DigitalSTROMBindingConstants.THING_TYPE_ID_ZONE_TEMERATURE_CONTROL);

        if (getSupportedThingTypes().contains(thingTypeUID)) {
            String thingID = tempControlStatus.getZoneID().toString();
            ThingUID thingUID = new ThingUID(thingTypeUID, BRIDGE_UID, thingID);
            return thingUID;
        } else {
            return null;
        }
    }

    @Override
    public void onTemperatureControlIsNotConfigured() {
        // nothing to do
    }

    @Override
    public void registerTemperatureSensorTransreciver(TemperatureSensorTransreciver temperatureSensorTransreciver) {
        // nothing to do
    }

    @Override
    public Integer getID() {
        return TemperatureControlStatusListener.DISCOVERY;
    }

    @Override
    public void onTargetTemperatureChanged(Float newValue) {
        // nothing to do

    }

    @Override
    public void onControlValueChanged(Integer newValue) {
        // nothing to do

    }

}
