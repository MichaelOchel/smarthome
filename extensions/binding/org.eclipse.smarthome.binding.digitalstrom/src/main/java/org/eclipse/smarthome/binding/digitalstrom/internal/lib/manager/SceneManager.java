/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager;

import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.EventHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.ManagerStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.SceneStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.stateEnums.ManagerStates;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.stateEnums.ManagerTypes;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.InternalScene;

/**
 * The {@link SceneManager} manages all functions concerning scenes without sending the commands itself.
 *
 * <p>
 * So it manages a list of all {@link InternalScenes} they called in the past or was generated by calling
 * {@link #generateScenes()}.<br>
 * Through this class you can also register {@link SceneStatusListener}'s to the {@link InternalScene}'s or register a
 * scene discovery. With {@link #addEcho(String)} or {@link #addEcho(String, short)} scene calls form the library can be
 * ignored. To update the state of an {@link InternalScene} or {@link Device} the methods
 * {@link #callInternalScene(InternalScene)}, {@link #callInternalScene(String)},
 * {@link #callDeviceScene(Device, Short)}
 * , {@link #callDeviceScene(String, Short)} etc. can be used.
 * </p>
 * <p>
 * If you call the {@link #start()} method a {@link EventListener} will be started to handle scene calls and undos from
 * the outside.
 * </p>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public interface SceneManager extends EventHandler {

    /**
     * Activates the given {@link InternalScene}, if it exists. Otherwise it will be added to the scene list and
     * activated, if it is a callable scene.
     *
     * @param scene
     */
    public void callInternalScene(InternalScene scene);

    /**
     * Activates a {@link InternalScene} with the given id, if it exists. Otherwise a new
     * {@link InternalScene} will be created and activated, if it is a callable scene.
     *
     * @param sceneID
     */
    public void callInternalScene(String sceneID);

    /**
     * Call the given sceneID on the {@link Device} with the given dSID, if the {@link Device} exists.
     *
     * @param dSID
     * @param sceneID
     */
    public void callDeviceScene(String dSID, Short sceneID);

    /**
     * Call the given sceneID on the given {@link Device}, if the {@link Device} exists.
     *
     * @param device
     * @param sceneID
     */
    public void callDeviceScene(Device device, Short sceneID);

    /**
     * Deactivates the given {@link InternalScene}, if it exists. Otherwise it will added to the scene list and
     * deactivated, if it is a callable scene.
     *
     * @param scene
     */
    public void undoInternalScene(InternalScene scene);

    /**
     * Deactivates a {@link InternalScene} with the given sceneID, if it exists. Otherwise a new
     * {@link InternalScene} will be created and deactivated, if it is a callable scene.
     *
     * @param sceneID
     */
    public void undoInternalScene(String sceneID);

    /**
     * Undo the last scene on the {@link Device} with the given dSID, if the {@link Device} exists.
     *
     * @param dSID
     */
    public void undoDeviceScene(String dSID);

    /**
     * Undo the last scene on the {@link Device}, if the {@link Device} exists.
     *
     * @param device
     */
    public void undoDeviceScene(Device device);

    /**
     * Registers the given {@link SceneStatusListener} to the {@link InternalScene}, if it exists or registers it as a
     * Scene-Discovery if the id of the {@link SceneStatusListener} is {@link SceneStatusListener#SCENE_DISCOVERY}.
     *
     * @param sceneListener
     */
    public void registerSceneListener(SceneStatusListener sceneListener);

    /**
     * Unregisters the given {@link SceneStatusListener} from the {@link InternalScene}, if it exists or unregisters the
     * Scene-Discovery, if the id of the {@link SceneStatusListener} is {@link SceneStatusListener#SCENE_DISCOVERY}.
     *
     * @param sceneListener
     */
    public void unregisterSceneListener(SceneStatusListener sceneListener);

    /**
     * Adds the given {@link InternalScene} to the scene list, if it is a callable scene.
     *
     * @param intScene
     */
    public void addInternalScene(InternalScene intScene);

    /**
     * Adds the scene call with the given dSID and sceneId as an echo to ignore them by detecting the {@link EventItem}.
     *
     * @param dSID
     * @param sceneId
     */
    public void addEcho(String dSID, short sceneId);

    /**
     * Adds the scene call with the given internal scene id as an echo to ignore them by detecting the {@link EventItem}
     * .
     *
     * @param internalSceneID
     */
    public void addEcho(String internalSceneID);

    /**
     * Returns the list of all {@link InternalScenes}.
     *
     * @return list of all scenes
     */
    public List<InternalScene> getScenes();

    /**
     * Returns true, if all reachable scenes are already generated, otherwise false.
     *
     * @return true = reachable scenes generated, otherwise false
     */
    public boolean scenesGenerated();

    /**
     * Generates all reachable scenes.
     *
     */
    public void generateScenes();

    /**
     * Will be called from the {@link SceneDiscovery}, if a scene type is generated or is fail.<br>
     * For that the scenesGenerated char array has four chars. Each char represents one scene type in the following
     * direction:
     * <ul>
     * <li><b>first:</b> named scenes</li>
     * <li><b>second:</b> apartment scenes</li>
     * <li><b>third:</b> zone scenes</li>
     * <li><b>fourth</b>: group scenes, if they can call by push buttons</li>
     * </ul>
     * If a scene type is not generated the char is "0". If a scene type is generated the char is "1" and, if it is fail
     * the char is "2".
     *
     * @param scenesGenerated
     */
    public void scenesGenerated(char[] scenesGenerated);

    /**
     * Returns true, if a discovery is registered, otherwise false.
     *
     * @return true discovery is registered, otherwise false
     */
    public boolean isDiscoveryRegistrated();

    /**
     * Starts the {@link EventListener}.
     */
    public void start();

    /**
     * Stops the {@link EventListener}.
     */
    public void stop();

    /**
     * Removes the {@link InternalScene} with the given sceneID.
     *
     * @param sceneID
     */
    void removeInternalScene(String sceneID);

    /**
     * Returns the {@link InternalScene} with the given sceneID.
     *
     * @param sceneID
     * @return internal scenes
     */
    public InternalScene getInternalScene(String sceneID);

    /**
     * Registers the given {@link ManagerStatusListener} to this class.
     *
     * @param statusListener
     */
    public void registerStatusListener(ManagerStatusListener statusListener);

    /**
     * Unregisters the {@link ManagerStatusListener} from this class.
     */
    public void unregisterStatusListener();

    /**
     * Returns the {@link ManagerTypes} of this class.
     *
     * @return these {@link ManagerTypes}
     */
    public ManagerTypes getManagerType();

    /**
     * Returns the current {@link ManagerStates}.
     *
     * @return current {@link ManagerStates}
     */
    public ManagerStates getManagerState();

    /**
     * Calls a scene without inform the scene discovery about the conceivably new {@link InternalScene}.
     *
     * @param zoneID
     * @param groupID
     * @param sceneID
     */
    public void callInternalSceneWithoutDiscovery(Integer zoneID, Short groupID, Short sceneID);
}
