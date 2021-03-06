/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.DigitalSTROMManagerStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.SceneStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.StateEnums.ManagerStates;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.StateEnums.ManagerTypes;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMSceneManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMStructureManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DSID;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.InternalScene;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.SceneDiscovery;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.constants.EventPropertyEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.constants.SceneEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.sceneEvent.EventItem;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.sceneEvent.EventListener;

/**
 * The {@link DigitalSTROMSceneManagerImpl} is the implementation of the {@link DigitalSTROMSceneManager}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class DigitalSTROMSceneManagerImpl implements DigitalSTROMSceneManager {

    private List<String> echoBox = Collections.synchronizedList(new LinkedList<String>());
    private Map<String, InternalScene> internalSceneMap = Collections
            .synchronizedMap(new HashMap<String, InternalScene>());

    private EventListener eventListener;
    private DigitalSTROMStructureManager structureManager;
    private DigitalSTROMConnectionManager connectionManager;
    private SceneDiscovery discovery;
    private DigitalSTROMManagerStatusListener statusListener = null;

    private boolean scenesGenerated = false;

    public DigitalSTROMSceneManagerImpl(DigitalSTROMConnectionManager connectionManager,
            DigitalSTROMStructureManager structureManager) {
        this.structureManager = structureManager;
        this.connectionManager = connectionManager;
        this.discovery = new SceneDiscovery(this);
    }

    @Override
    public void start() {
        if (eventListener == null) {
            eventListener = new EventListener(connectionManager, this);
        }
        this.eventListener.start();
    }

    @Override
    public void stop() {
        if (this.eventListener != null) {
            this.eventListener.shutdown();
            this.eventListener = null;
        }
    }

    @Override
    public void handleEvent(EventItem eventItem) {
        if (eventItem != null) {
            boolean isCallScene = true;
            String isCallStr = eventItem.getProperties().get(EventPropertyEnum.EVENT_NAME);
            if (isCallStr != null) {
                isCallScene = isCallStr.equals("callScene");
            }

            boolean isDeviceCall = false;

            String deviceCallStr = eventItem.getProperties().get(EventPropertyEnum.IS_DEVICE_CALL);
            if (deviceCallStr != null) {
                isDeviceCall = deviceCallStr.equals("true");
            }

            if (isDeviceCall) {
                String dsidStr = null;
                dsidStr = eventItem.getProperties().get(EventPropertyEnum.DSID);
                short sceneId = -1;
                String sceneStr = eventItem.getProperties().get(EventPropertyEnum.SCENEID);
                if (sceneStr != null) {
                    try {
                        sceneId = Short.parseShort(sceneStr);
                    } catch (java.lang.NumberFormatException e) {
                        System.err.println("NumberFormatException by handling event at parsing sceneId: " + sceneStr);
                    }
                }

                if (!isEcho(dsidStr, sceneId)) {
                    if (isCallScene) {
                        this.callDeviceScene(new DSID(dsidStr), sceneId);
                    } else {
                        this.undoDeviceScene(new DSID(dsidStr));
                    }
                }

            } else {
                String intSceneID = null;

                String zoneIDStr = eventItem.getProperties().get(EventPropertyEnum.ZONEID);
                String sceneIDStr = eventItem.getProperties().get(EventPropertyEnum.SCENEID);
                String groupIDStr = eventItem.getProperties().get(EventPropertyEnum.GROUPID);

                if (zoneIDStr != null && sceneIDStr != null && groupIDStr != null) {
                    intSceneID = zoneIDStr + "-" + groupIDStr + "-" + sceneIDStr;
                    if (!isEcho(intSceneID)) {
                        if (isCallScene) {
                            this.callInternalScene(intSceneID);
                        } else {
                            this.undoInternalScene(intSceneID);
                        }
                    }
                }
            }
        }

    }

    private boolean isEcho(String dsid, short sceneId) {
        String echo = dsid + "-" + sceneId;
        return isEcho(echo);
    }

    private boolean isEcho(String echoID) {
        if (echoBox.contains(echoID)) {
            echoBox.remove(echoID);
            return true;
        }

        return false;
    }

    // ... we want to ignore own 'command-echos'
    @Override
    public void addEcho(String dsid, short sceneId) {
        addEcho(dsid + "-" + sceneId);
    }

    // ... we want to ignore own 'command-echos'
    @Override
    public void addEcho(String internalSceneID) {
        echoBox.add(internalSceneID);
    }

    @Override
    public void callInternalScene(InternalScene scene) {
        InternalScene intScene = this.internalSceneMap.get(scene.getID());
        if (intScene != null) {
            intScene.activateScene();
        } else {
            if (SceneEnum.getScene(scene.getSceneID()) != null
                    && structureManager.checkZoneGroupID(scene.getZoneID(), scene.getGroupID())) {
                scene.addReferenceDevices(this.structureManager.getReferenceDeviceListFromZoneXGroupX(scene.getZoneID(),
                        scene.getGroupID()));
                this.internalSceneMap.put(scene.getID(), scene);
            }
        }
    }

    @Override
    public void callInternalScene(String sceneID) {
        InternalScene intScene = this.internalSceneMap.get(sceneID);
        if (intScene != null) {
            intScene.activateScene();
        } else {
            intScene = createNewScene(sceneID);
            if (intScene != null) {
                discovery.sceneDiscoverd(intScene);
                intScene.activateScene();
            }
        }

    }

    @Override
    public void addInternalScene(InternalScene intScene) {
        if (!this.internalSceneMap.containsKey(intScene.getID())) {
            if (SceneEnum.getScene(intScene.getSceneID()) != null
                    && structureManager.checkZoneGroupID(intScene.getZoneID(), intScene.getGroupID())) {
                intScene.addReferenceDevices(this.structureManager
                        .getReferenceDeviceListFromZoneXGroupX(intScene.getZoneID(), intScene.getGroupID()));
                this.internalSceneMap.put(intScene.getID(), intScene);

            }
        } else {
            InternalScene oldScene = this.internalSceneMap.get(intScene.getID());
            String oldSceneName = this.internalSceneMap.get(intScene.getID()).getSceneName();
            String newSceneName = intScene.getSceneName();
            if ((oldSceneName.contains("Zone:") && oldSceneName.contains("Group:") && oldSceneName.contains("Scene:"))
                    && !(newSceneName.contains("Zone:") && newSceneName.contains("Group:")
                            && newSceneName.contains("Scene:"))) {
                oldScene.setSceneName(newSceneName);
                this.discovery.sceneDiscoverd(oldScene);
            }
        }
    }

    @Override
    public void removeInternalScene(String sceneID) {
        this.internalSceneMap.remove(sceneID);
    }

    @Override
    public InternalScene getInternalScene(String sceneID) {
        return this.internalSceneMap.get(sceneID);
    }

    private InternalScene createNewScene(String sceneID) {
        String[] sceneData = sceneID.split("-");
        if (sceneData.length == 3) {
            int zoneID = Integer.parseInt(sceneData[0]);
            short groupID = Short.parseShort(sceneData[1]);
            short sceneNumber = Short.parseShort(sceneData[2]);
            String sceneName = null;
            InternalScene intScene = null;
            if (SceneEnum.getScene(sceneNumber) != null && structureManager.checkZoneGroupID(zoneID, groupID)) {
                if (structureManager.getZoneName(zoneID) != null) {
                    sceneName = "Zone: " + structureManager.getZoneName(zoneID);
                    if (structureManager.getZoneGroupName(zoneID, groupID) != null) {
                        sceneName = sceneName + " Group: " + structureManager.getZoneGroupName(zoneID, groupID);
                    } else {
                        sceneName = sceneName + " Group: " + groupID;
                    }
                } else {
                    sceneName = "Zone: " + zoneID + " Group: " + groupID;
                }
                sceneName = sceneName + " Scene: "
                        + SceneEnum.getScene(sceneNumber).toString().toLowerCase().replace("_", " ");
                intScene = new InternalScene(zoneID, groupID, sceneNumber, sceneName);
            }

            return intScene;
        } else {
            // Fehlermeldung
            return null;
        }
    }

    @Override
    public void callDeviceScene(DSID dSID, Short sceneID) {
        Device device = this.structureManager.getDeviceByDSID(dSID);
        if (device != null) {
            device.internalCallScene(sceneID);
        } else {
            // Fehlermeldung
        }

    }

    @Override
    public void callDeviceScene(Device device, Short sceneID) {
        if (device != null) {
            callDeviceScene(device.getDSID(), sceneID);
        }

    }

    @Override
    public void undoInternalScene(InternalScene scene) {
        if (scene != null) {
            undoInternalScene(scene.getID());
        }
    }

    @Override
    public void undoInternalScene(String sceneID) {
        InternalScene intScene = this.internalSceneMap.get(sceneID);
        if (intScene != null) {
            intScene.deactivateScene();
        } else {
            intScene = createNewScene(sceneID);
            if (intScene != null)
                intScene.deactivateScene();
        }

    }

    @Override
    public void undoDeviceScene(DSID dSID) {
        Device device = this.structureManager.getDeviceByDSID(dSID);
        if (device != null) {
            device.internalUndoScene();
        }

    }

    @Override
    public void undoDeviceScene(Device device) {
        if (device != null) {
            undoDeviceScene(device.getDSID());
        }
    }

    @Override
    public void registerSceneListener(SceneStatusListener sceneListener) {
        if (sceneListener != null) {
            String id = sceneListener.getID();
            if (id.equals(SceneStatusListener.SCENE_DESCOVERY)) {
                discovery.registerSceneDiscovery(sceneListener);
                if (scenesGenerated()) {
                    for (InternalScene scene : internalSceneMap.values()) {
                        discovery.sceneDiscoverd(scene);
                    }
                }
            } else {
                InternalScene intScene = internalSceneMap.get(sceneListener.getID());
                if (intScene != null) {
                    intScene.registerSceneListener(sceneListener);
                } else {
                    // logger.debug("can't find scene form listener with id: {} create new scene.", id);
                    addInternalScene(createNewScene(id));
                    registerSceneListener(sceneListener);
                }
            }
        } else {
            // TODO: Fehlermeldung
        }

    }

    @Override
    public void unregisterSceneListener(SceneStatusListener sceneListener) {
        if (sceneListener != null) {
            String id = sceneListener.getID();
            if (id.equals(SceneStatusListener.SCENE_DESCOVERY)) {
                this.discovery.unRegisterDiscovery();
            } else {
                InternalScene intScene = this.internalSceneMap.get(sceneListener.getID());
                if (intScene != null) {
                    intScene.unregisterSceneListener();
                    discovery.sceneDiscoverd(intScene);
                } else {
                    // TODO:Fehlermeldung
                }
            }
        } else {
            // TODO: Fehlermeldung
        }

    }

    @Override
    public boolean scenesGenerated() {
        return scenesGenerated;
    }

    @Override
    public void generateScenes() {
        stateChanged(ManagerStates.generatingScenes);
        discovery.generateAllScenes(connectionManager, structureManager);
        stateChanged(ManagerStates.scenesGenerated);
        scenesGenerated = true;
    }

    @Override
    public boolean isDiscoveryRegistrated() {
        return this.discovery != null;
    }

    private void stateChanged(ManagerStates state) {
        if (statusListener != null) {
            statusListener.onStatusChanged(ManagerTypes.sceneManager, state);
        }
    }

    @Override
    public List<InternalScene> getScenes() {
        return this.internalSceneMap != null ? new LinkedList<InternalScene>(this.internalSceneMap.values()) : null;
    }

    @Override
    public void registerStatusListener(DigitalSTROMManagerStatusListener statusListener) {
        this.statusListener = statusListener;
    }

    @Override
    public void unregisterStatusListener() {
        this.statusListener = null;
    }
}
