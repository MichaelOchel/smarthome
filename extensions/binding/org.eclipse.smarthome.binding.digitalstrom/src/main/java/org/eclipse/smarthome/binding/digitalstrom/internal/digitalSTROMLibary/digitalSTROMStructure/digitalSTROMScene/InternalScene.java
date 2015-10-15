/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.SceneStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.Device;

/**
 * The {@link InternalScene} represents a digitalSTRROM-Scene for the internal model.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class InternalScene {

    private final Short SCENE_ID;
    private final Short GROUP_ID;
    private final Integer ZONE_ID;
    private String SceneName;
    private final String NAMED_SCENE_ID;
    private boolean active = false;

    private List<Device> devices = Collections.synchronizedList(new LinkedList<Device>());
    private SceneStatusListener listener = null;

    public InternalScene(Integer zoneID, Short groupID, Short sceneID, String sceneName) {
        if (sceneID == null)
            throw new IllegalArgumentException("The parameter sceneID can't be null!");
        this.SCENE_ID = sceneID;

        if (groupID == null) {
            this.GROUP_ID = 0;
        } else {
            this.GROUP_ID = groupID;
        }

        if (zoneID == null) {
            this.ZONE_ID = 0;
        } else {
            this.ZONE_ID = zoneID;
        }

        this.NAMED_SCENE_ID = this.ZONE_ID + "-" + this.GROUP_ID + "-" + this.SCENE_ID;
        if (sceneName == null || sceneName.isEmpty()) {
            this.SceneName = this.NAMED_SCENE_ID;
        } else {
            this.SceneName = sceneName;
        }
    }

    /**
     * Activate this Scene.
     */
    public void activateScene() {
        this.active = true;
        informListener();
        if (this.devices != null) {
            for (Device device : this.devices) {
                device.callInternalScene(this);
            }
        }
    }

    /**
     * Deactivate this Scene.
     */
    public void deactivateScene() {
        this.active = false;
        informListener();
        if (this.devices != null) {
            for (Device device : this.devices) {
                device.undoInternalScene();
            }
        }
    }

    private void informListener() {
        if (this.listener != null) {
            listener.onSceneStateChanged(this.active);
        }
    }

    /**
     * Returned true if this Scene is active, otherwise false.
     *
     * @return active? (true = yes | false = no)
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * Adds an affected {@link Device} to this {@link InternalScene} device list.
     *
     * @param device
     */
    public void addDevice(Device device) {
        if (!this.devices.contains(device)) {
            this.devices.add(device);
        }
        int prio = 0;
        if (this.listener != null) {
            prio = 1000;
        } else {
            prio = 2000;
        }
        device.checkSceneConfig(SCENE_ID, prio);
    }

    /**
     * Override the existing device list of this {@link InternalScene} with a new reference to a {@link List} of
     * affected {@link Device}'s.
     *
     * @param deviceList
     */
    public void addReferenceDevices(List<Device> deviceList) {
        this.devices = deviceList;
        checkDeviceSceneConfig();
    }

    /**
     * Proves if the scene configuration is saved to all {@link Device}'s. If not the device initials the reading out of
     * the missing configuration with low priority if no listener is added, medium priority if a listener is added and
     * high priority if this scene has been activated.
     */
    public void checkDeviceSceneConfig() {
        int prio = 0;
        if (this.listener != null) {
            prio = 1000;
        } else {
            prio = 2000;
        }
        if (devices != null) {
            for (Device device : devices) {
                device.checkSceneConfig(SCENE_ID, prio);
            }
        }
    }

    /**
     * Returns the list of the affected {@link Device}'s.
     *
     * @return device list
     */
    public List<Device> getDeviceList() {
        return this.devices;

    }

    /**
     * Adds a {@link List} of affected {@link Device}'s.
     *
     * @param deviceList
     */
    public void addDevices(List<Device> deviceList) {
        for (Device device : deviceList) {
            addDevice(device);
        }
    }

    /**
     * Removes a not anymore affected {@link Device} from the device list.
     * 
     * @param device
     */
    public void removeDevice(Device device) {
        this.devices.remove(device);
    }

    /**
     * Updates the affected {@link Device}'s with the given deviceList.
     *
     * @param deviceList
     */
    public void updateDeviceList(List<Device> deviceList) {
        if (!this.devices.equals(deviceList)) {
            this.devices.clear();
            addDevices(deviceList);
        }
    }

    /**
     * This method have a device to call if this scene was active and the device state has change.
     *
     * @param sceneNumber
     */
    public void deviceSceneChanged(short sceneNumber) {
        if (this.SCENE_ID != sceneNumber) {
            if (active) {
                active = false;
                informListener();
            }
        }
    }

    /**
     * Returns the Scene name.
     *
     * @return scene name
     */
    public String getSceneName() {
        return SceneName;
    }

    /**
     * Sets the Scene name to the given scene name.
     *
     * @param sceneName
     */
    public void setSceneName(String sceneName) {
        SceneName = sceneName;
    }

    /**
     * Returns the Scene id of this scene call.
     *
     * @return scene id
     */
    public Short getSceneID() {
        return SCENE_ID;
    }

    /**
     * Returns the group id of this scene call.
     *
     * @return group id
     */
    public Short getGroupID() {
        return GROUP_ID;
    }

    /**
     * Returns the zone id of this scene call.
     *
     * @return zone id
     */
    public Integer getZoneID() {
        return ZONE_ID;
    }

    /**
     * Returns the id of this scene call.
     *
     * @return scene call id
     */
    public String getID() {
        return NAMED_SCENE_ID;
    }

    /**
     * Register a {@link SceneStatusListener} to this {@link InternalScene}.
     *
     * @param listener
     */
    public synchronized void registerSceneListener(SceneStatusListener listener) {
        this.listener = listener;
        this.listener.onSceneAdded(this);
        checkDeviceSceneConfig();

    }

    /**
     * Unregister the {@link SceneStatusListener} from this {@link InternalScene}.
     */
    public synchronized void unregisterSceneListener() {
        if (listener != null) {
            this.listener.onSceneRemoved(this);
            this.listener = null;
        }
    }

    @Override
    public String toString() {
        return "NamedScene [SceneName=" + SceneName + ", NAMED_SCENE_ID=" + NAMED_SCENE_ID + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((NAMED_SCENE_ID == null) ? 0 : NAMED_SCENE_ID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof InternalScene))
            return false;
        InternalScene other = (InternalScene) obj;
        if (NAMED_SCENE_ID == null) {
            if (other.getID() != null)
                return false;
        } else if (!NAMED_SCENE_ID.equals(other.getID()))
            return false;
        return true;
    }

}
