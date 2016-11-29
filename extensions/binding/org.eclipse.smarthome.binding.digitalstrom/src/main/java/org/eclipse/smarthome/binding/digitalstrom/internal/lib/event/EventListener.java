/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.config.Config;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.types.Event;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.types.EventItem;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.types.JSONEventImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.impl.JSONResponseHandler;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link EventListener} listens for events which will be thrown by the digitalSTROM-Server and notifies the added
 * {@link EventHandler} about the detected events, if they supports the event-type.<br>
 * You can add {@link EventHandler}'s though the constructors or the methods {@link #addEventHandler(EventHandler)} and
 * {@link #addEventHandlers(List)}.<br>
 * You can also delete a {@link EventHandler} though the method {@link #removeEventHandler(EventHandler)}.<br>
 * If the {@link EventListener} is started, both methods subscribe and unsubscribe the event-types of the
 * {@link EventHandler}/s automatically.<br>
 * <br>
 * To start and stop the listening you have to call the methods {@link #start()} and {@link #stop()}. The methods
 * subscribe and unsubscribe the supported event-types of the added {@link EventHandler}'s automatically.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class EventListener {

    private Logger logger = LoggerFactory.getLogger(EventListener.class);
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(Config.THREADPOOL_NAME);
    private ScheduledFuture<?> pollingScheduler = null;
    private ScheduledFuture<?> subscriptionScheduler = null;

    private int subscriptionID = 15;
    private int timeout = 500;
    private List<String> subscribedEvents = Collections.synchronizedList(new LinkedList<String>());;
    private boolean subscribed = false;

    // error message
    private final String INVALID_SESSION = "Invalid session!";
    private final String UNKNOWN_TOKEN = "Token " + subscriptionID + " not found!";

    private final ConnectionManager connManager;
    private List<EventHandler> eventHandlers = null;
    private Config config;
    private boolean isStarted = false;

    /**
     * Creates a new {@link EventListener} to listen to the supported event-types of the given eventHandler and notify
     * about a detected event.<br>
     * <br>
     * To get notified by events you have to call {@link #start()}.
     *
     * @param connectionManager must not be null
     * @param eventHandler must not be null
     */
    public EventListener(ConnectionManager connectionManager, EventHandler eventHandler) {
        this.connManager = connectionManager;
        this.config = connectionManager.getConfig();
        addEventHandler(eventHandler);
    }

    /**
     * This constructor can add more than one {@link EventHandler} as a list of {@link EventHandler}'s.
     *
     * @param connectionManager must not be null
     * @param list of eventHandlers must not be null
     * @see #EventListener(ConnectionManager, EventHandler)
     */
    public EventListener(ConnectionManager connectionManager, List<EventHandler> eventHandlers) {
        this.connManager = connectionManager;
        this.config = connectionManager.getConfig();
        addEventHandlers(eventHandlers);
    }

    /**
     * Creates a new {@link EventListener} without a {@link EventHandler}<br>
     * <br>
     * To get notified by events you have to call {@link #start()} and {@link #addEventHandler(EventHandler)} or
     * {@link #addEventHandlers(List)}.
     *
     * @param connectionManager must not be null
     * @param eventHandler must not be null
     */
    public EventListener(ConnectionManager connectionManager) {
        this.connManager = connectionManager;
        this.config = connectionManager.getConfig();
    }

    /**
     * Stops this {@link EventListener} and unsubscribe events.
     */
    public synchronized void stop() {
        logger.debug("Stop EventListener");
        isStarted = false;
    }

    private void internalStop() {
        if (subscriptionScheduler != null && !subscriptionScheduler.isCancelled()) {
            subscriptionScheduler.cancel(true);
            subscriptionScheduler = null;
        }
        if (pollingScheduler != null && !pollingScheduler.isCancelled()) {
            pollingScheduler.cancel(true);
            pollingScheduler = null;
            unsubscribe();
            logger.debug("internal stop EventListener");
        }
    }

    /**
     * Starts this {@link EventListener} and subscribe events.
     */
    public synchronized void start() {
        logger.debug("Start EventListener");
        isStarted = true;
    }

    private void internalStart() {
        if (eventHandlers != null && !eventHandlers.isEmpty()
                && (pollingScheduler == null || pollingScheduler.isCancelled())) {
            pollingScheduler = scheduler.scheduleAtFixedRate(runableListener, 0,
                    config.getEventListenerRefreshinterval(), TimeUnit.MILLISECONDS);
            logger.debug("internal start EventListener");
        }
    }

    /**
     * Adds a {@link List} of {@link EventHandler}'s and subscribe the supported event-types, if the
     * {@link EventListener} is started and the event-types are not already subscribed.
     *
     * @param eventHandlers
     */
    public void addEventHandlers(List<EventHandler> eventHandlers) {
        if (eventHandlers != null) {
            for (EventHandler eventHandler : eventHandlers) {
                addEventHandler(eventHandler);
            }
        }
    }

    /**
     * Adds a {@link EventHandler}'s and subscribe the supported event-types, if the
     * {@link EventListener} is started and the event-types are not already subscribed.<br>
     * <br>
     * <b>Note:<b><br>
     * If {@link #start()} was called before the {@link EventListener} will start now, otherwise you have to call
     * {@link #start()} to get notified by events.
     *
     * @param eventHandler
     */
    public void addEventHandler(EventHandler eventHandler) {
        if (eventHandler != null) {
            if (eventHandlers == null) {
                eventHandlers = Collections.synchronizedList(new LinkedList<EventHandler>());
            }
            boolean handlerExist = false;
            for (EventHandler handler : eventHandlers) {
                if (handler.getUID().equals(eventHandler.getUID())) {
                    handlerExist = true;
                }
            }
            if (!handlerExist) {
                eventHandlers.add(eventHandler);
                addSubscribeEvents(eventHandler.getSupportetEvents());
                logger.debug("eventHandler: " + eventHandler.getUID() + " added");
                if (isStarted) {
                    internalStart();
                }
            }
        }
    }

    /**
     * Remove a {@link EventHandler} and unsubscribes the supported event-types, if the
     * {@link EventListener} is started and no other {@link EventHandler} needed the event-types.
     *
     * @param eventHandler
     */
    public void removeEventHandler(EventHandler eventHandler) {
        if (eventHandler != null && eventHandlers.contains(eventHandler)) {
            List<String> tempSubsList = new ArrayList<String>();
            int index = -1;
            EventHandler intEventHandler = null;
            boolean subscribedEventsChanged = false;
            for (int i = 0; i < eventHandlers.size(); i++) {
                intEventHandler = eventHandlers.get(i);
                if (intEventHandler.getUID().equals(eventHandler.getUID())) {
                    index = i;
                } else {
                    tempSubsList.addAll(intEventHandler.getSupportetEvents());
                }
            }
            if (index != -1) {
                intEventHandler = eventHandlers.remove(index);
                for (String eventName : intEventHandler.getSupportetEvents()) {
                    if (!tempSubsList.contains(eventName)) {
                        subscribedEvents.remove(eventName);
                        subscribedEventsChanged = true;
                    }
                }
            }
            if (subscribedEventsChanged) {
                // Because of the json-call unsubscribe?eventName=XY&subscriptionID=Z doesn't work like it is explained
                // in the dS-JSON-API, the whole EventListener will be restarted. The problem is, that not only the
                // given eventName, rather all events of the subscitionID will be deleted.
                internalStop();
                if (!eventHandlers.isEmpty() && isStarted) {
                    logger.debug("Min one subscribed events was deleted, EventListener will be restarted");
                    internalStart();
                }
            }
        }
    }

    public void addSubscribe(String subscribeEvent) {
        if (!subscribedEvents.contains(subscribeEvent)) {
            subscribedEvents.add(subscribeEvent);
            logger.debug("subscibeEvent: " + subscribeEvent + " added");
            if (subscribed) {
                subscribe(subscribeEvent);
            }
        }
    }

    public void addSubscribeEvents(List<String> subscribeEvents) {
        for (String eventName : subscribeEvents) {
            subscribe(eventName);
        }
    }

    private void getSubscriptionID() {
        boolean subscriptionIDavailable = false;
        while (!subscriptionIDavailable) {
            String response = connManager.getDigitalSTROMAPI().getEvent(connManager.getSessionToken(), subscriptionID,
                    timeout);

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                subscriptionID++;
            } else {
                String errorStr = null;
                if (responseObj != null && responseObj.get(JSONApiResponseKeysEnum.MESSAGE.getKey()) != null) {
                    errorStr = responseObj.get(JSONApiResponseKeysEnum.MESSAGE.getKey()).getAsString();
                }
                if (errorStr != null && errorStr.contains(UNKNOWN_TOKEN)) {
                    subscriptionIDavailable = true;
                }
            }
        }
    }

    private boolean subscribe(String eventName) {
        if (connManager.checkConnection()) {
            if (!subscribed) {
                getSubscriptionID();
            }
            subscribed = connManager.getDigitalSTROMAPI().subscribeEvent(connManager.getSessionToken(), eventName,
                    subscriptionID, config.getConnectionTimeout(), config.getReadTimeout());

            if (subscribed) {
                logger.debug("subscribed event: {} to subscriptionID: {}", eventName, subscriptionID);
            } else {
                logger.error(
                        "Couldn't subscribe event {} ... maybe timeout because system is to busy ... event will subscribe later again ... ");
            }
            return subscribed;
        } else {
            logger.error("Couldn't subscribe eventListener, because there is no token (no connection)");
        }
        return false;
    }

    private void subscribe(final List<String> evetNames) {
        final Iterator<String> eventNameIter = evetNames.iterator();
        subscriptionScheduler = scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                while (eventNameIter.hasNext()) {
                    subscribe(eventNameIter.next());
                }
                subscriptionScheduler.cancel(true);
            }

        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    private Runnable runableListener = new Runnable() {

        @Override
        public void run() {
            if (connManager.checkConnection()) {
                if (subscribed) {
                    String response = connManager.getDigitalSTROMAPI().getEvent(connManager.getSessionToken(),
                            subscriptionID, timeout);
                    JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                    if (JSONResponseHandler.checkResponse(responseObj)) {
                        JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                        if (obj != null && obj.get(JSONApiResponseKeysEnum.EVENTS.getKey()).isJsonArray()) {
                            JsonArray array = obj.get(JSONApiResponseKeysEnum.EVENTS.getKey()).getAsJsonArray();
                            try {
                                handleEvent(array);
                            } catch (Exception e) {
                                logger.error("An Exception occurred", e);
                            }
                        }
                    } else {
                        String errorStr = null;
                        if (responseObj != null && responseObj.get(JSONApiResponseKeysEnum.MESSAGE.getKey()) != null) {
                            errorStr = responseObj.get(JSONApiResponseKeysEnum.MESSAGE.getKey()).getAsString();
                        }
                        if (errorStr != null
                                && (errorStr.equals(INVALID_SESSION) || errorStr.contains(UNKNOWN_TOKEN))) {
                            unsubscribe();
                            subscribe(subscribedEvents);
                        } else if (errorStr != null) {
                            pollingScheduler.cancel(true);
                            logger.error("Unknown error message at event response: " + errorStr);
                        }
                    }
                } else {
                    subscribe(subscribedEvents);
                }
            }
        }
    };

    private void unsubscribe() {
        if (connManager.checkConnection()) {
            for (String eventName : this.subscribedEvents) {
                connManager.getDigitalSTROMAPI().unsubscribeEvent(this.connManager.getSessionToken(), eventName,
                        this.subscriptionID, config.getConnectionTimeout(), config.getReadTimeout());
            }
        }
    }

    private void handleEvent(JsonArray array) {
        if (array.size() > 0) {
            Event event = new JSONEventImpl(array);
            for (EventItem item : event.getEventItems()) {
                logger.debug("detect event {}", item.toString());
                for (EventHandler handler : eventHandlers) {
                    if (handler.supportsEvent(item.getName())) {
                        logger.debug("inform handler with id {} about event {}", handler.getUID(), item.toString());
                        handler.handleEvent(item);
                    }
                }
            }
        }
    }
}