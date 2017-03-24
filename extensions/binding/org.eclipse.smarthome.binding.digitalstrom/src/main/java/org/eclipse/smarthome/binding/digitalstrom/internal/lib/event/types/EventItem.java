/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.types;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.constants.EventResponseEnum;

/**
 * The {@link EventItem} represents a event item of an digitalSTROM-Event.
 *
 * @author Alexander Betker
 * @author Michael Ochel - add getSource()
 * @author Matthias Siegele - add getSource()
 */
public interface EventItem {

    /**
     * Returns the name of this {@link EventItem}.
     *
     * @return name of this {@link EventItem}
     */
    public String getName();

    /**
     * Returns {@link HashMap} with the properties fiels of this {@link EventItem}.
     * The key is a {@link EventResponseEnum} and represents the property name
     * and the value is the property value.
     *
     * @return the properties of this {@link EventItem}
     */
    public Map<EventResponseEnum, String> getProperties();

    /**
     * Returns {@link HashMap} with the source fields of this {@link EventItem}.
     * The key is a {@link EventResponseEnum} and represents the property name
     * and the value is the property value.
     *
     * @return the properties of this {@link EventItem}
     */
    public Map<EventResponseEnum, String> getSource();
}
