/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.event;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.config.Config;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.types.Event;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.types.EventItem;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.event.types.JSONEventImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.SceneManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.impl.JSONResponseHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.InternalScene;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * If someone call or undo a scene, the {@link SceneManager} will get a notification
 * to update the state of the internal saved {@link InternalScene} or directly the {@link Device}, if it was a
 * device scene.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class EventListener {

    private Logger logger = LoggerFactory.getLogger(EventListener.class);
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(Config.THREADPOOL_NAME);
    private ScheduledFuture<?> pollingScheduler = null;

    private int subscriptionID = 15;
    private int timeout = 500;
    List<String> subscribeEvents;
    private boolean subscribed = false;

    // error message
    private final String INVALID_SESSION = "Invalid session!";
    private final String UNKNOWN_TOKEN = "Token " + subscriptionID + " not found!";

    private final ConnectionManager connManager;
    private EventHandler sceneManager;
    private Config config;

    /**
     * Creates a new {@link EventListener}. To get notified by call and undo scene events you have to call
     * {@link #start()}.
     *
     * @param connectionManager must not be null
     * @param eventHandler must not be null
     */
    public EventListener(ConnectionManager connectionManager, EventHandler eventHandler, List<String> subscribeEvents) {
        this.connManager = connectionManager;
        this.subscribeEvents = subscribeEvents;
        this.config = connectionManager.getConfig();
        this.sceneManager = eventHandler;
    }

    /**
     * Stops this {@link EventListener}.
     */
    public synchronized void stop() {
        if (pollingScheduler != null || !pollingScheduler.isCancelled()) {
            pollingScheduler.cancel(true);
            pollingScheduler = null;
            unsubscribe();
            logger.debug("Stop EventListener");
        }
    }

    /**
     * Starts this {@link EventListener}.
     */
    public synchronized void start() {
        if ((pollingScheduler == null || pollingScheduler.isCancelled())) {
            pollingScheduler = scheduler.scheduleAtFixedRate(runableListener, 0,
                    config.getEventListenerRefreshinterval(), TimeUnit.MILLISECONDS);
            logger.debug("Start EventListener");
        }
    }

    private void subscribe() {
        if (connManager.checkConnection()) {
            boolean subscriptionIDavailable = false;
            while (!subscriptionIDavailable) {
                String response = connManager.getDigitalSTROMAPI().getEvent(connManager.getSessionToken(),
                        subscriptionID, timeout);
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
            for (String eventName : this.subscribeEvents) {
                subscribed = connManager.getDigitalSTROMAPI().subscribeEvent(this.connManager.getSessionToken(),
                        eventName, this.subscriptionID, config.getConnectionTimeout(), config.getReadTimeout());
            }

            if (!subscribed) {
                logger.error("Couldn't subscribe EventListener ... maybe timeout because system is to busy ...");
            } else {
                logger.debug("subscribe successfull, subscription id is " + subscriptionID + " subscribed events are: "
                        + subscribeEvents.toString());
            }
        } else {
            logger.error("Couldn't subscribe eventListener, because there is no token (no connection)");
        }
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
                            subscribe();
                        } else if (errorStr != null) {
                            pollingScheduler.cancel(true);
                            logger.error("Unknown error message at event response: " + errorStr);
                        }
                    }
                } else {
                    subscribe();
                }
            }
        }
    };

    private void unsubscribe() {
        if (connManager.checkConnection()) {
            for (String eventName : this.subscribeEvents) {
                connManager.getDigitalSTROMAPI().unsubscribeEvent(this.connManager.getSessionToken(), eventName,
                        this.subscriptionID, config.getConnectionTimeout(), config.getReadTimeout());
            }
        }
    }

    private void handleEvent(JsonArray array) {
        if (array.size() > 0) {
            Event event = new JSONEventImpl(array);
            for (EventItem item : event.getEventItems()) {
                logger.info(item.getProperties().toString());
                this.sceneManager.handleEvent(item);
            }
        }
    }
}