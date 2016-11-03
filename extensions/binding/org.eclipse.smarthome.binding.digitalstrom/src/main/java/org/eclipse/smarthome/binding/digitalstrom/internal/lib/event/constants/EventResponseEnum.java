/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.constants;

import java.util.HashMap;

/**
 * The {@link EventResponseEnum} contains digitalSTROM-Event properties.
 *
 * @author Michael Ochel
 * @author Mathias Siegele
 */
public enum EventResponseEnum {

    // general
    NAME("Name"),
    PROPERTIES("properties"),
    SOURCE("source"),
    SET("set"),
    DSID("dsid"),
    ZONEID("zoneID"),
    GROUPID("groupID"),
    IS_APARTMENT("isApartment"),
    IS_GROUP("isGroup"),
    IS_DEVICE("isDevice"),

    // scene event
    FORCED("forced"),
    ORIGEN_TOKEN("originToken"),
    CALL_ORIGEN("callOrigin"),
    ORIGEN_DSUID("originDSUID"),
    SCENEID("sceneID"),
    ORIGIN_DEVICEID("originDeviceID");

    private final String ID;
    static final HashMap<String, EventResponseEnum> eventResponseFields = new HashMap<String, EventResponseEnum>();

    static {
        for (EventResponseEnum ev : EventResponseEnum.values()) {
            eventResponseFields.put(ev.getId(), ev);
        }
    }

    /**
     * Returns true, if the given property exists at the ESH event properties, otherwise false.
     *
     * @param property
     * @return contains property (true = yes | false = no)
     */
    public static boolean containsId(String property) {
        return eventResponseFields.keySet().contains(property);
    }

    /**
     * Returns the {@link EventResponseEnum} to the given property.
     *
     * @param property
     * @return EventPropertyEnum
     */
    public static EventResponseEnum getProperty(String property) {
        return eventResponseFields.get(property);
    }

    private EventResponseEnum(String id) {
        this.ID = id;
    }

    /**
     * Returns the id of this {@link EventResponseEnum}.
     *
     * @return id of this {@link EventResponseEnum}
     */

    public String getId() {
        return ID;
    }
}
