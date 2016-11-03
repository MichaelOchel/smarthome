package org.eclipse.smarthome.binding.digitalstrom.internal.lib.event;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.types.EventItem;

public interface EventHandler {

    /**
     * Handles a {@link EventItem} e.g. which was detected by the {@link EventListener}.
     *
     * @param eventItem
     */
    public void handleEvent(EventItem eventItem);
}
