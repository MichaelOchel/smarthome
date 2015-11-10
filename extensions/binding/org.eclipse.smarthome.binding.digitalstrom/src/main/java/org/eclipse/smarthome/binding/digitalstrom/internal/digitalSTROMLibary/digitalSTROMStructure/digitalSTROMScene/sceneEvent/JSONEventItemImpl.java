/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.sceneEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMServerConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.constants.EventPropertyEnum;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link JSONEventItemImpl} is the implementation of the {@link EventItem}.
 *
 * @author Alexander Betker
 * @since 1.3.0
 */
public class JSONEventItemImpl implements EventItem {

    private String name = null;

    private Map<EventPropertyEnum, String> properties = new HashMap<EventPropertyEnum, String>();

    /**
     * Creates a new {@link JSONEventItemImpl} from the given DigitalSTROM-Event-Item {@link JsonObject}.
     *
     * @param event item json object
     */
    public JSONEventItemImpl(JsonObject object) {

        name = object.get(JSONApiResponseKeysEnum.EVENT_NAME.getKey()).getAsString();

        if (object.get(JSONApiResponseKeysEnum.EVENT_PROPERTIES.getKey()) instanceof JsonObject) {

            JsonObject propObj = (JsonObject) object.get(JSONApiResponseKeysEnum.EVENT_PROPERTIES.getKey());

            Iterator<Entry<String, JsonElement>> inter = propObj.entrySet().iterator();

            while (inter.hasNext()) {
                Entry<String, JsonElement> entry = inter.next();
                if (EventPropertyEnum.containsId(entry.getKey())) {
                    addProperty(EventPropertyEnum.getProperty(entry.getKey()), entry.getValue().getAsString());
                }
            }

        }

    }

    private void addProperty(EventPropertyEnum prop, String value) {
        properties.put(prop, value);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<EventPropertyEnum, String> getProperties() {
        return properties;
    }

}