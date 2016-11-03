/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.types;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.constants.EventResponseEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link EventItemImpl} is the implementation of the {@link EventItem}.
 *
 * @author Michael Ochel
 * @author Mathias Siegele
 */
public class EventItemImpl implements EventItem {

    private String name = null;
    private Map<EventResponseEnum, String> properties;
    private Map<EventResponseEnum, String> source;

    /**
     * Creates a new {@link EventItemImpl} from the given digitalSTROM-Event-Item {@link JsonObject}.
     *
     * @param jsonEventItem
     */
    public EventItemImpl(JsonObject jsonEventItem) {
        name = jsonEventItem.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();

        if (jsonEventItem.get(JSONApiResponseKeysEnum.PROPERTIES.getKey()).isJsonObject()) {
            Set<Entry<String, JsonElement>> propObjEntrySet = jsonEventItem
                    .get(JSONApiResponseKeysEnum.PROPERTIES.getKey()).getAsJsonObject().entrySet();
            properties = new HashMap<EventResponseEnum, String>(propObjEntrySet.size());
            for (Entry<String, JsonElement> entry : propObjEntrySet) {
                if (EventResponseEnum.containsId(entry.getKey())) {
                    addProperty(EventResponseEnum.getProperty(entry.getKey()), entry.getValue().getAsString());
                }
            }
        }
        if (jsonEventItem.get(JSONApiResponseKeysEnum.SOURCE.getKey()).isJsonObject()) {
            Set<Entry<String, JsonElement>> sourceObjEntrySet = jsonEventItem
                    .get(JSONApiResponseKeysEnum.SOURCE.getKey()).getAsJsonObject().entrySet();
            source = new HashMap<EventResponseEnum, String>(sourceObjEntrySet.size());
            for (Entry<String, JsonElement> entry : sourceObjEntrySet) {
                if (EventResponseEnum.containsId(entry.getKey())) {
                    addSource(EventResponseEnum.getProperty(entry.getKey()), entry.getValue().getAsString());
                }
            }
        }
    }

    private void addProperty(EventResponseEnum propertieKey, String value) {
        properties.put(propertieKey, value);
    }

    private void addSource(EventResponseEnum sourceKey, String value) {
        source.put(sourceKey, value);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<EventResponseEnum, String> getProperties() {
        return properties;
    }

    @Override
    public Map<EventResponseEnum, String> getSource() {
        return source;
    }
}