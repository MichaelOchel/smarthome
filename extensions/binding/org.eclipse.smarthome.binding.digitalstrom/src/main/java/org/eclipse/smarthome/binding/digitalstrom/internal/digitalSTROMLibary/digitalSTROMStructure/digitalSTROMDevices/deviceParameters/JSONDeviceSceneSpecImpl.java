/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMServerConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.constants.Scene;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.constants.SceneEnum;

import com.google.gson.JsonObject;

/**
 * The {@link JSONDeviceSceneSpecImpl} is the implementation of the {@link DeviceSceneSpec}.
 *
 * @author Alexander Betker
 * @author Michael Ochel - change from SimpleJSON to GSON
 * @author Matthias Siegele - change from SimpleJSON to GSON
 */
public class JSONDeviceSceneSpecImpl implements DeviceSceneSpec {

    private Scene scene = null;
    private boolean dontcare = false;
    private boolean localPrio = false;
    private boolean specialMode = false;
    private boolean flashMode = false;

    public JSONDeviceSceneSpecImpl(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.DEVICE_GET_SCENE_MODE_SCENE_ID.getKey()) != null) {
            int val = -1;
            val = jObject.get(JSONApiResponseKeysEnum.DEVICE_GET_SCENE_MODE_SCENE_ID.getKey()).getAsInt();

            if (val > -1) {
                this.scene = SceneEnum.getScene(val);
            }
        }

        if (jObject.get(JSONApiResponseKeysEnum.DEVICE_GET_SCENE_MODE_DONT_CARE.getKey()) != null) {
            this.dontcare = jObject.get(JSONApiResponseKeysEnum.DEVICE_GET_SCENE_MODE_DONT_CARE.getKey())
                    .getAsBoolean();
        }

        if (jObject.get(JSONApiResponseKeysEnum.DEVICE_GET_SCENE_MODE_LOCAL_PRIO.getKey()) != null) {
            this.localPrio = jObject.get(JSONApiResponseKeysEnum.DEVICE_GET_SCENE_MODE_LOCAL_PRIO.getKey())
                    .getAsBoolean();
        }

        if (jObject.get(JSONApiResponseKeysEnum.DEVICE_GET_SCENE_MODE_SPECIAL_MODE.getKey()) != null) {
            this.specialMode = jObject.get(JSONApiResponseKeysEnum.DEVICE_GET_SCENE_MODE_SPECIAL_MODE.getKey())
                    .getAsBoolean();
        }

        if (jObject.get(JSONApiResponseKeysEnum.DEVICE_GET_SCENE_MODE_FLASH_MODE.getKey()) != null) {
            this.flashMode = jObject.get(JSONApiResponseKeysEnum.DEVICE_GET_SCENE_MODE_FLASH_MODE.getKey())
                    .getAsBoolean();
        }
    }

    public JSONDeviceSceneSpecImpl(Short sceneID) {
        this.scene = SceneEnum.getScene(sceneID);
    }

    public JSONDeviceSceneSpecImpl(String SceneName) {
        this.scene = SceneEnum.valueOf(SceneName);
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    @Override
    public boolean isDontCare() {
        return dontcare;
    }

    @Override
    public synchronized void setDontcare(boolean dontcare) {
        this.dontcare = dontcare;
    }

    @Override
    public boolean isLocalPrio() {
        return localPrio;
    }

    @Override
    public synchronized void setLocalPrio(boolean localPrio) {
        this.localPrio = localPrio;
    }

    @Override
    public boolean isSpecialMode() {
        return specialMode;
    }

    @Override
    public synchronized void setSpecialMode(boolean specialMode) {
        this.specialMode = specialMode;
    }

    @Override
    public boolean isFlashMode() {
        return flashMode;
    }

    @Override
    public synchronized void setFlashMode(boolean flashMode) {
        this.flashMode = flashMode;
    }

    @Override
    public String toString() {
        return "Scene: " + this.getScene() + ", dontcare: " + this.isDontCare() + ", localPrio: " + this.isLocalPrio()
                + ", specialMode: " + this.isSpecialMode() + ", flashMode: " + this.isFlashMode();
    }

}
