/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.event;

import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.types.EventItem;

/**
 * The {@link EventHandler} can be implemented to get informed by {@link EventItem}'s through the {@link EventListener}.
 * <br>
 * For that the {@link #getSupportetEvents()} and {@link #supportsEvent(String)} methods have to be implemented, so that
 * the {@link EventListener} knows whitch events it has to subscribe at the digitalSTROM-server and which handler has
 * to be informed. <br>
 * The implementation of the {@link EventHandler} also has to be registered through
 * {@link EventListener#addEventHandler(EventHandler)} to the {@link EventListener} and the {@link EventListener} has to
 * be started.<br>
 * <br>
 * To handle the {@link EventItem} the method {@link #handleEvent(EventItem)} has to be implemented.
 *
 * @author Michael Ochel
 * @author Matthias Siegele
 */
public interface EventHandler {

    /**
     * Handles a {@link EventItem} e.g. which was detected by the {@link EventListener}.
     *
     * @param eventItem to handle
     */
    public void handleEvent(EventItem eventItem);

    /**
     * Returns a {@link List} that contains the supported events.
     *
     * @return supported events
     */
    public List<String> getSupportetEvents();

    /**
     * Returns true, if the {@link EventHandler} supports the given event.
     *
     * @param eventName to check
     * @return true, if event is supported, otherwise false
     */
    public boolean supportsEvent(String eventName);

    /**
     * Returns the unique id of the {@link EventHandler}.
     *
     * @return uid of the EventHandler
     */
    public String getUID();

    /**
     * Sets a {@link EventListener} to this {@link EventHandler}.
     *
     * @param eventListener to set
     */
    public void setEventListener(EventListener eventListener);

    /**
     * Unsets a {@link EventListener} to this {@link EventHandler}.
     *
     * @param eventListener to unset
     */
    public void unsetEventListener(EventListener eventListener);
}
