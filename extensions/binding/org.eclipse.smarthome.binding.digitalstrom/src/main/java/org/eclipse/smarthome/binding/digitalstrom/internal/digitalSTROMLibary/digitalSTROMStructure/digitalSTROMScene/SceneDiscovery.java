/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.SceneStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMSceneManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMStructureManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMServerConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMServerConnection.impl.JSONResponseHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.constants.ApartmentSceneEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.constants.SceneEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.constants.ZoneSceneEnum;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SceneDiscovery} can read out various digitalSTROM-Scene types and generates a list of theirs or managing
 * over the {@link DigitalSTROMSceneManager}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class SceneDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(SceneDiscovery.class);

    private List<InternalScene> namedScenes = new LinkedList<InternalScene>();
    private boolean genList = false;

    private DigitalSTROMSceneManager sceneManager;
    private SceneStatusListener discovery = null;

    private final String query = "/json/property/query?query=/apartment/zones/*(ZoneID)/groups/*(group)/scenes/*(scene,name)";
    private final String reachableScenesQuery = "/json/zone/getReachableScenes?id=";
    private final String reachableGroupsQuery = "/json/apartment/getReachableGroups?token=";

    /**
     * Creates a new {@link SceneDiscovery} with scene managing over the {@link DigitalSTROMSceneManager}
     *
     * @param genList
     */
    public SceneDiscovery(DigitalSTROMSceneManager sceneManager) {
        this.sceneManager = sceneManager;

    }

    /**
     * Creates a new {@link SceneDiscovery} and generate only a list of all scenes if genList is true.
     *
     * @param genList
     */
    public SceneDiscovery(boolean genList) {
        this.genList = genList;
    }

    /**
     * Generates all named, reachable, apratmet and zone scenes.
     *
     * @param connectionManager
     * @param structureManager
     */
    public void generateAllScenes(DigitalSTROMConnectionManager connectionManager,
            DigitalSTROMStructureManager structureManager) {
        generateNamedScenes(connectionManager);
        generateAppartmentScence();
        generateZoneScenes(connectionManager, structureManager);
        generateReachableScenes(connectionManager, structureManager);
    }

    /**
     * Generates all named scenes.
     *
     * @param connectionManager
     * @return success true otherwise false
     */
    public boolean generateNamedScenes(DigitalSTROMConnectionManager connectionManager) {
        if (connectionManager.checkConnection()) {
            String response = connectionManager.getHttpTransport()
                    .execute(query + "&token=" + connectionManager.getSessionToken());
            if (response == null) {
                return false;
            } else {
                JSONObject responsJsonObj = JSONResponseHandler.toJSONObject(response);
                if (JSONResponseHandler.checkResponse(responsJsonObj)) {
                    addScenesToList(JSONResponseHandler.getResultJSONObject(responsJsonObj));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generates all apartment scenes.
     */
    public void generateAppartmentScence() {
        for (ApartmentSceneEnum apartmentScene : ApartmentSceneEnum.values()) {

            InternalScene scene = new InternalScene(null, null, (short) apartmentScene.getSceneNumber(),
                    "Apartment-Scene: " + apartmentScene.toString().toLowerCase().replace("_", " "));
            if (genList) {
                System.out.print("Appartmend Scene: " + scene.toString());
                if (this.namedScenes.add(scene)) {
                    System.out.print(" added to list\n");
                }
            } else {
                sceneDiscoverd(scene);
            }
        }
    }

    private void addScenesToList(JSONObject resultJsonObj) {
        if (resultJsonObj.get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE_ZONES.getKey()) instanceof JSONArray) {
            JSONArray zones = (JSONArray) resultJsonObj
                    .get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE_ZONES.getKey());
            for (int i = 0; i < zones.size(); i++) {

                if (((JSONObject) zones.get(i)).get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE_ZONES_GROUPS
                        .getKey()) instanceof org.json.simple.JSONArray) {

                    JSONArray groups = (JSONArray) ((JSONObject) zones.get(i))
                            .get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE_ZONES_GROUPS.getKey());

                    for (int j = 0; j < groups.size(); j++) {

                        if (((JSONObject) groups.get(j)).get("scenes") instanceof org.json.simple.JSONArray) {

                            JSONArray scenes = (JSONArray) ((JSONObject) groups.get(j)).get("scenes");
                            for (int k = 0; k < scenes.size(); k++) {
                                if (scenes.get(k) instanceof org.json.simple.JSONObject) {

                                    JSONObject sceneJsonObject = ((JSONObject) scenes.get(k));
                                    int zoneID = Integer.parseInt(((JSONObject) zones.get(i)).get("ZoneID").toString());
                                    short groupID = Short
                                            .parseShort(((JSONObject) groups.get(j)).get("group").toString());
                                    InternalScene scene = new InternalScene(zoneID, groupID,
                                            Short.parseShort(sceneJsonObject.get("scene").toString()),
                                            sceneJsonObject.get("name").toString());

                                    if (genList) {
                                        System.out.print("Namend Scene: " + scene.toString());
                                        if (this.namedScenes.add(scene)) {
                                            System.out.print(" added to list\n");
                                        }
                                    } else {
                                        sceneDiscoverd(scene);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * Generates all zone scenes.
     *
     * @param connectionManager
     * @param structureManager
     * @return success true otherwise false
     */
    public boolean generateZoneScenes(DigitalSTROMConnectionManager connectionManager,
            DigitalSTROMStructureManager structureManager) {
        HashMap<Integer, List<Short>> reachableGroups = getReachableGroups(connectionManager);

        if (reachableGroups != null) {
            for (Integer zoneID : reachableGroups.keySet()) {
                if (!reachableGroups.get(zoneID).isEmpty()) {
                    for (ZoneSceneEnum zoneScene : ZoneSceneEnum.values()) {
                        String sceneName = "Zone-Scene: Zone: ";
                        if (structureManager.getZoneName(zoneID) != null
                                && !structureManager.getZoneName(zoneID).isEmpty()) {
                            sceneName = sceneName + structureManager.getZoneName(zoneID);

                        } else {
                            sceneName = sceneName + zoneID;
                        }
                        sceneName = sceneName + " Scene: " + zoneScene.toString().toLowerCase().replace("_", " ");
                        InternalScene scene = new InternalScene(zoneID, null, (short) zoneScene.getSceneNumber(),
                                sceneName);
                        if (genList) {
                            System.out.print("Zone Scene: " + scene.toString());
                            if (this.namedScenes.add(scene)) {
                                System.out.print(" added to list\n");
                            }
                        } else {
                            sceneDiscoverd(scene);
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Generates all reachable scenes.
     *
     * @param connectionManager
     * @param structureManager
     * @return success true otherwise false
     */
    public boolean generateReachableScenes(DigitalSTROMConnectionManager connectionManager,
            DigitalSTROMStructureManager structureManager) {
        HashMap<Integer, List<Short>> reachableGroups = getReachableGroups(connectionManager);

        if (reachableGroups != null) {
            for (Integer zoneID : reachableGroups.keySet()) {
                List<Short> groupIDs = reachableGroups.get(zoneID);
                if (groupIDs != null) {
                    if (connectionManager.checkConnection()) {
                        for (Short groupID : groupIDs) {
                            String response = connectionManager.getHttpTransport().execute(this.reachableScenesQuery
                                    + zoneID + "&groupID=" + groupID + "&token=" + connectionManager.getSessionToken());
                            if (response == null) {
                                return false;
                            } else {
                                JSONObject responsJsonObj = JSONResponseHandler.toJSONObject(response);
                                if (JSONResponseHandler.checkResponse(responsJsonObj)) {
                                    JSONObject resultJsonObj = JSONResponseHandler.getResultJSONObject(responsJsonObj);
                                    if (resultJsonObj.get(JSONApiResponseKeysEnum.ZONE_GET_REACHABLE_SCENES
                                            .getKey()) instanceof JSONArray) {
                                        JSONArray scenes = (JSONArray) resultJsonObj
                                                .get(JSONApiResponseKeysEnum.ZONE_GET_REACHABLE_SCENES.getKey());
                                        if (scenes != null) {
                                            for (int i = 0; i < scenes.size(); i++) {
                                                short sceneNumber = Short.parseShort(scenes.get(i).toString());
                                                String sceneName = null;
                                                if (SceneEnum.getScene(sceneNumber) != null) {
                                                    if (structureManager.getZoneName(zoneID) != null
                                                            && !structureManager.getZoneName(zoneID).isEmpty()) {
                                                        sceneName = "Zone: " + structureManager.getZoneName(zoneID);

                                                    } else {
                                                        sceneName = "Zone: " + zoneID;
                                                    }
                                                    if (structureManager.getZoneGroupName(zoneID, groupID) != null
                                                            && !structureManager.getZoneGroupName(zoneID, groupID)
                                                                    .isEmpty()) {
                                                        sceneName = sceneName + " Group: "
                                                                + structureManager.getZoneGroupName(zoneID, groupID);
                                                    } else {
                                                        sceneName = sceneName + " Group: " + groupID;
                                                    }
                                                    sceneName = sceneName + " Scene: " + SceneEnum.getScene(sceneNumber)
                                                            .toString().toLowerCase().replace("_", " ");
                                                }
                                                InternalScene scene = new InternalScene(zoneID, groupID, sceneNumber,
                                                        sceneName);

                                                if (genList) {
                                                    System.out.print("Reachable Scene: " + scene.toString());
                                                    if (this.namedScenes.add(scene)) {
                                                        System.out.print(" added to list\n");
                                                    }
                                                } else {
                                                    sceneDiscoverd(scene);
                                                }
                                            }
                                        }
                                    }

                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                            }

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();

                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    private HashMap<Integer, List<Short>> getReachableGroups(DigitalSTROMConnectionManager connectionManager) {
        HashMap<Integer, List<Short>> reachableGroupsMap = null;
        if (connectionManager.checkConnection()) {
            String response = connectionManager.getHttpTransport()
                    .execute(this.reachableGroupsQuery + connectionManager.getSessionToken());
            if (response == null) {
                return null;
            } else {
                JSONObject responsJsonObj = JSONResponseHandler.toJSONObject(response);
                if (JSONResponseHandler.checkResponse(responsJsonObj)) {
                    JSONObject resultJsonObj = JSONResponseHandler.getResultJSONObject(responsJsonObj);
                    if (resultJsonObj
                            .get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE_ZONES.getKey()) instanceof JSONArray) {
                        JSONArray zones = (JSONArray) resultJsonObj
                                .get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE_ZONES.getKey());
                        reachableGroupsMap = new HashMap<Integer, List<Short>>(zones.size());
                        List<Short> groupList;
                        for (int i = 0; i < zones.size(); i++) {
                            if (((JSONObject) zones.get(i))
                                    .get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE_ZONES_GROUPS
                                            .getKey()) instanceof JSONArray) {
                                JSONArray groups = (JSONArray) ((JSONObject) zones.get(i))
                                        .get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE_ZONES_GROUPS.getKey());
                                groupList = new LinkedList<Short>();
                                for (int k = 0; k < groups.size(); k++) {
                                    groupList.add(Short.parseShort(groups.get(k).toString()));
                                }
                                reachableGroupsMap.put(
                                        Integer.parseInt(((JSONObject) zones.get(i)).get("zoneID").toString()),
                                        groupList);
                            }
                        }
                    }
                }
            }
        }
        return reachableGroupsMap;
    }

    /**
     * Informs the registered {@link SceneStausListener} as scene discovery about a new scene.
     *
     * @param scene
     */
    public void sceneDiscoverd(InternalScene scene) {
        if (scene != null) {
            if (!isStandardScene(scene.getSceneID())) {
                if (this.discovery != null) {
                    this.discovery.onSceneAdded(scene);
                    logger.debug("Inform scene discovery aboud added scene with id: " + scene.getID());
                } else {
                    logger.debug("Can't inform scene discovery aboud added scene with id: " + scene.getID()
                            + " because scene discovery is disabled");
                }
            }
            this.sceneManager.addInternalScene(scene);
        }
    }

    private boolean isStandardScene(short sceneID) {
        switch (SceneEnum.getScene(sceneID)) {
            case INCREMENT:
            case DECREMENT:
            case STOP:
            case MINIMUM:
            case MAXIMUM:
            case AUTO_OFF:
            case DEVICE_ON:
            case DEVICE_OFF:
            case DEVICE_STOP:
                return true;

            default:
                return false;
        }
    }

    /**
     * Registers the given {@link SceneStatusListener} as scene discovery.
     *
     * @param listener
     */
    public void registerSceneDiscovery(SceneStatusListener listener) {
        this.discovery = listener;
    }

    /**
     * Unregisters the {@link SceneStatusListener} as scene discovery from this {@link InternalScene}.
     */
    public void unRegisterDiscovery() {
        this.discovery = null;
    }

    /**
     * Returns the list of all generated {@link InternalScene}'s if the list should generated.
     *
     * @return List of all {@link InternalScene} or null
     */
    public List<InternalScene> getNamedSceneList() {
        return this.namedScenes;
    }

    @Override
    public String toString() {
        return this.namedScenes.toString();
    }
}
