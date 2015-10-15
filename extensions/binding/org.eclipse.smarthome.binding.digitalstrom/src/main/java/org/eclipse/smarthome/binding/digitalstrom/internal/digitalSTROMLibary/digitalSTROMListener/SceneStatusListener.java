/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMDeviceStatusManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMSceneManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.InternalScene;

/**
 * The {@link SceneStatusListener} is notified when a {@link InternalScene} status has changed or a
 * {@link InternalScene} has been removed or added.
 *
 * <p>
 * By implementation with the id {@link #SCENE_DESCOVERY} this listener is a scene discovery and will be informed by
 * the {@link DigitalSTROMSceneManager} if a new scene would be found or is removed from the
 * internal-digitalSTROM-System-model if it is registered over the {@link DigitalSTROMDeviceStatusManager} or directly
 * on the {@link DigitalSTROMSceneManager}.
 * </p>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface SceneStatusListener {

    public final static String SCENE_DESCOVERY = "SceneDiscovey";

    /**
     * This method is called whenever the state of the given scene has changed.
     *
     * @param newState
     *
     */
    public void onSceneStateChanged(boolean newState);

    /**
     * This method is called whenever a scene is removed.
     *
     * @param scene
     *
     */
    public void onSceneRemoved(InternalScene scene);

    /**
     * This method is called whenever a scene is added.
     *
     * @param scene
     *
     */
    public void onSceneAdded(InternalScene scene);

    /**
     * Return the id of this {@link SceneStatusListener}.
     *
     * @return listener id
     */
    public String getID();

}
