/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.constants;

import java.util.HashMap;

/**
 * The {@link ZoneSceneEnum} list all zone scenes which are available on the dss-web-interface.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public enum ZoneSceneEnum implements Scene {

    DEEP_OFF(68),
    STANDBY(67),
    SLEEPING(69),
    WAKEUP(70);

    private final int sceneNumber;

    static final HashMap<Integer, ZoneSceneEnum> zoneScenes = new HashMap<Integer, ZoneSceneEnum>();

    static {
        for (ZoneSceneEnum zs : ZoneSceneEnum.values()) {
            zoneScenes.put(zs.getSceneNumber(), zs);
        }
    }

    private ZoneSceneEnum(int sceneNumber) {
        this.sceneNumber = sceneNumber;
    }

    /**
     * Returns the {@link ZoneSceneEnum} of the given scene number.
     *
     * @param sceneNumber
     * @return ZoneSceneEnum
     */
    public static ZoneSceneEnum getZoneScene(int sceneNumber) {
        return zoneScenes.get(sceneNumber);
    }

    @Override
    public int getSceneNumber() {
        return this.sceneNumber;
    }

}
