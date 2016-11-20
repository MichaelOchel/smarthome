/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceSceneSpec;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.Scene;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.SceneEnum;

import com.google.gson.JsonObject;

/**
 * The {@link JSONDeviceSceneSpecImpl} is the implementation of the {@link DeviceSceneSpec}.
 *
 * @author Alexander Betker - Initial contribution
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
        if (jObject.get(JSONApiResponseKeysEnum.SCENE_ID.getKey()) != null) {
            short val = -1;
            val = jObject.get(JSONApiResponseKeysEnum.SCENE_ID.getKey()).getAsShort();

            if (val > -1) {
                this.scene = SceneEnum.getScene(val);
            }
        }
        if (jObject.get(JSONApiResponseKeysEnum.DONT_CARE.getKey()) != null) {
            this.dontcare = jObject.get(JSONApiResponseKeysEnum.DONT_CARE.getKey()).getAsBoolean();
        }
        if (jObject.get(JSONApiResponseKeysEnum.LOCAL_PRIO.getKey()) != null) {
            this.localPrio = jObject.get(JSONApiResponseKeysEnum.LOCAL_PRIO.getKey()).getAsBoolean();
        }
        if (jObject.get(JSONApiResponseKeysEnum.SPECIAL_MODE.getKey()) != null) {
            this.specialMode = jObject.get(JSONApiResponseKeysEnum.SPECIAL_MODE.getKey()).getAsBoolean();
        }
        if (jObject.get(JSONApiResponseKeysEnum.FLASH_MODE.getKey()) != null) {
            this.flashMode = jObject.get(JSONApiResponseKeysEnum.FLASH_MODE.getKey()).getAsBoolean();
        }
    }

    public JSONDeviceSceneSpecImpl(Short sceneID) {
        this.scene = SceneEnum.getScene(sceneID);
    }

    public JSONDeviceSceneSpecImpl(String sceneName) {
        try {
            this.scene = SceneEnum.valueOf(sceneName);
        } catch (IllegalArgumentException e) {
            // ignore it
        }
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