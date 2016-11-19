package org.eclipse.smarthome.binding.digitalstrom.internal.lib.event;

import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.types.EventItem;

public interface EventHandler {

    /**
     * Handles a {@link EventItem} e.g. which was detected by the {@link EventListener}.
     *
     * @param eventItem
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
     * @param eventName
     * @return true, if event is supported, otherwise false
     */
    public boolean supportsEvent(String eventName);

    /**
     * Returns the unique id of the {@link EventHandler}.
     *
     * @return uid of the EventHandler
     */
    public String getUID();

    public void setEventListener(EventListener eventListener);

    public void unsetEventListener(EventListener eventListener);
}
