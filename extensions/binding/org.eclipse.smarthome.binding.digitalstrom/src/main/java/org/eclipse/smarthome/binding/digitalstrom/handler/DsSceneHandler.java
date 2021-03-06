/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.handler;

import java.util.Set;

import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.SceneStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMStructureManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.InternalScene;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link DsSceneHandler} is responsible for handling commands,
 * which are send to the channel of an DigitalSTROM-Scene.<br>
 * For that it uses the {@link DsBridgeHandler} to execute the actual command, and implements the
 * {@link SceneStatusListener}
 * to get informed about changes from the accompanying {@link InternalScene}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class DsSceneHandler extends BaseThingHandler implements SceneStatusListener {

    private Logger logger = LoggerFactory.getLogger(DsSceneHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets
            .newHashSet(DigitalSTROMBindingConstants.THING_TYPE_SCENE);

    DssBridgeHandler dssBridgeHandler = null;
    Short sceneId = null;
    Integer zoneID = null;
    Short groupID = null;

    InternalScene scene;

    String sceneThingID = null;

    public DsSceneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing DigitalSTROM Scene handler.");
        if (this.getDssBridgeHandler() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR, "Bridge is missig");
        } else {
            bridgeHandlerInitialized(dssBridgeHandler, this.getBridge());
        }

    }

    @Override
    public void dispose() {
        logger.debug("Handler disposes. Unregistering listener.");
        if (sceneThingID != null) {
            DssBridgeHandler dssBridgeHandler = getDssBridgeHandler();
            if (dssBridgeHandler != null) {
                getDssBridgeHandler().unregisterSceneStatusListener(this);
            }
            sceneThingID = null;
            scene = null;
        }
    }

    @Override
    public void handleRemoval() {
        if (getDssBridgeHandler() != null) {
            this.dssBridgeHandler.childThingRemoved(sceneThingID);
        }
        updateStatus(ThingStatus.REMOVED);
    }

    @Override
    protected void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        String configZoneID;
        String configGroupID;
        String configSceneID;

        if (getConfig().get(DigitalSTROMBindingConstants.SCENE_ZONE_ID) != null) {
            configZoneID = getConfig().get(DigitalSTROMBindingConstants.SCENE_ZONE_ID).toString().toLowerCase();
        } else {
            configZoneID = "";
        }
        if (getConfig().get(DigitalSTROMBindingConstants.SCENE_GROUP_ID) != null) {
            configGroupID = getConfig().get(DigitalSTROMBindingConstants.SCENE_GROUP_ID).toString().toLowerCase();
        } else {
            configGroupID = "";
        }
        if (getConfig().get(DigitalSTROMBindingConstants.SCENE_ID) != null) {
            configSceneID = getConfig().get(DigitalSTROMBindingConstants.SCENE_ID).toString().toLowerCase();
        } else {
            configSceneID = "";
        }
        logger.debug("zoneID: " + configZoneID);
        if (!configSceneID.isEmpty()) {
            this.sceneId = Short.parseShort(configSceneID);

            if (thingHandler instanceof DssBridgeHandler) {
                this.dssBridgeHandler = (DssBridgeHandler) thingHandler;

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "waiting for listener registartion");
                logger.debug("Set status on {}", getThing().getStatus());

            }

            DigitalSTROMStructureManager strucMan = dssBridgeHandler.getStructureManager();
            if (configZoneID.isEmpty()) {
                zoneID = 0;
            } else {

                try {
                    zoneID = Integer.parseInt(configZoneID);
                    logger.debug("zoneID: " + zoneID);
                    if (!strucMan.checkZoneID(zoneID)) {
                        zoneID = null;
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configured zone '"
                                + configZoneID + "' does not exist, please check your configuration.");
                        return;
                    }

                } catch (NumberFormatException e) {

                    this.zoneID = strucMan.getZoneId(configZoneID);

                    if (this.zoneID == -1) {
                        logger.error("Can not found zone id or zone name {}!", configZoneID);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configured zone '"
                                + configGroupID + "' does not exist, please check your configuration.");
                        return;
                    }
                }
            }

            if (configGroupID.isEmpty()) {
                groupID = 0;

            } else {

                try {
                    groupID = Short.parseShort(configGroupID);
                    if (!strucMan.checkZoneGroupID(zoneID, groupID)) {
                        groupID = null;
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configured group '"
                                + configGroupID + "' does not exist, please check your configuration.");
                        return;
                    }

                } catch (NumberFormatException e) {
                    String zoneName = strucMan.getZoneName(zoneID);
                    this.groupID = strucMan.getZoneGroupId(zoneName, configGroupID);
                }

                if (this.groupID == null) {
                    logger.error("Can not found group id or group name {}!", configZoneID);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configured group '"
                            + configGroupID + "' does not exist, please check your configuration.");
                    return;
                }
            }

            this.sceneThingID = zoneID + "-" + groupID + "-" + sceneId;

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "waiting for listener registartion");
            logger.debug("Set status on {}", getThing().getStatus());
            this.dssBridgeHandler.registerSceneStatusListener(this);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No Scene-ID is set!");
            logger.debug("Set status on {}", getThing().getStatus());
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        DssBridgeHandler dssBridgeHandler = getDssBridgeHandler();
        if (dssBridgeHandler == null) {
            logger.warn("DigitalSTROM bridge handler not found. Cannot handle command without bridge.");
            return;
        }

        if (channelUID.getId().equals(DigitalSTROMBindingConstants.CHANNEL_SCENE)) {
            if (command instanceof OnOffType) {
                if (OnOffType.ON.equals(command)) {
                    this.dssBridgeHandler.sendSceneComandToDSS(scene, true);
                } else {
                    this.dssBridgeHandler.sendSceneComandToDSS(scene, false);
                }
            }
        } else {
            logger.warn("Command send to an unknown channel id: " + channelUID);
        }

    }

    private synchronized DssBridgeHandler getDssBridgeHandler() {
        if (this.dssBridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.debug("can't find Bridge");
                return null;
            }
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof DssBridgeHandler) {
                this.dssBridgeHandler = (DssBridgeHandler) handler;
                // TODO: register on dsBridge
                // this.dssBridgeHandler.registerDeviceStatusListener(dSUID, this);
            } else {
                return null;
            }
        }
        return this.dssBridgeHandler;
    }

    @Override
    public void onSceneStateChanged(boolean flag) {
        if (flag) {
            updateState(new ChannelUID(getThing().getUID(), DigitalSTROMBindingConstants.CHANNEL_SCENE), OnOffType.ON);
        } else {
            updateState(new ChannelUID(getThing().getUID(), DigitalSTROMBindingConstants.CHANNEL_SCENE), OnOffType.OFF);
        }

    }

    @Override
    public void onSceneRemoved(InternalScene scene) {
        scene = null;
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void onSceneAdded(InternalScene scene) {
        logger.debug("Scene {} added", scene.getID());
        if (this.dssBridgeHandler != null) {
            ThingStatusInfo statusInfo = this.dssBridgeHandler.getThing().getStatusInfo();
            updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());
            logger.debug("Set status on {}", getThing().getStatus());
        }
        this.scene = scene;
        onSceneStateChanged(scene.isActive());
    }

    @Override
    public String getID() {
        return this.sceneThingID;
    }

}
