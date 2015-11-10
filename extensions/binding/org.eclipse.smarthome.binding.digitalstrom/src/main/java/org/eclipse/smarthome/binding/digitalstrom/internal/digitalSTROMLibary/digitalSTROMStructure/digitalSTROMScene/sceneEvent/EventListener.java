/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.sceneEvent;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMConfiguration.DigitalSTROMConfig;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMSceneManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMServerConnection.DigitalSTROMAPI;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMServerConnection.HttpTransport;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMServerConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMServerConnection.constants.JSONRequestConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMServerConnection.impl.JSONResponseHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.InternalScene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * If someone call or undo a scene the {@link DigitalSTROMSceneManager} will get a notification
 * to update the state of the internal saved {@link InternalScene} or directly the {@link Device} if it was a
 * device-scene.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class EventListener {

    protected static final long SLEEPTIME = DigitalSTROMConfig.EVENT_LISTENER_REFRESHINTERVAL;

    private Logger logger = LoggerFactory.getLogger(EventListener.class);

    private Thread listener = null;
    private Boolean shutdown = true;
    private final String EVENT_NAME = DigitalSTROMConfig.EVENT_NAME;
    private final int ID = 11;
    private final DigitalSTROMConnectionManager connManager;

    private final String INVALID_SESSION = "Invalid session!";
    private final String UNKNOWN_TOKEN = "Token " + ID + " not found!";

    private HttpTransport transport = null;
    private DigitalSTROMAPI digitalSTROM;
    private DigitalSTROMSceneManager sceneManager;

    public EventListener(DigitalSTROMConnectionManager connectionManager, DigitalSTROMSceneManager sceneManager) {
        this.transport = connectionManager.getHttpTransport();
        this.digitalSTROM = connectionManager.getDigitalSTROMAPI();
        this.connManager = connectionManager;
        this.sceneManager = sceneManager;
    }

    /**
     * Shutdown this {@link EventListener}.
     */
    public synchronized void shutdown() {
        if (!shutdown) {
            this.shutdown = true;
        }
        this.listener = null;
    }

    /**
     * Starts this {@link EventListener}.
     */
    public synchronized void start() {
        if (shutdown) {
            this.shutdown = false;
            subscribe();
        }
        if (listener == null) {
            this.listener = new Thread(runableListener);
            listener.start();
        }

    }

    private boolean subscribe() {
        if (connManager.checkConnection()) {

            boolean transmitted = digitalSTROM.subscribeEvent(this.connManager.getSessionToken(), EVENT_NAME, this.ID,
                    DigitalSTROMConfig.DEFAULT_CONNECTION_TIMEOUT, DigitalSTROMConfig.DEFAULT_READ_TIMEOUT);

            if (!transmitted) {
                this.shutdown = true;

                logger.error("Couldn't subscribe eventListener ... maybe timeout because system is to busy ...");
            } else {
                logger.debug("subscribe successfull");
                return true;
            }
        } else {
            logger.error("Couldn't subscribe eventListener because there is no token (no connection)");
        }

        return false;
    }

    private Runnable runableListener = new Runnable() {

        @Override
        public void run() {
            logger.debug("DigitalSTROMEventListener startet");
            while (!shutdown) {
                String request = getEventAsRequest(ID, 500);

                if (request != null) {
                    String response = transport.execute(request);

                    JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                    if (JSONResponseHandler.checkResponse(responseObj)) {
                        JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);

                        if (obj != null
                                && obj.get(JSONApiResponseKeysEnum.EVENT_GET_EVENT.getKey()) instanceof JsonArray) {
                            JsonArray array = (JsonArray) obj.get(JSONApiResponseKeysEnum.EVENT_GET_EVENT.getKey());
                            try {
                                handleEvent(array);
                            } catch (Exception e) {
                                logger.debug("EXCEPTION in eventListener thread : " + e.getLocalizedMessage());
                            }
                        }
                    } else {
                        String errorStr = null;
                        if (responseObj != null
                                && responseObj.get(JSONApiResponseKeysEnum.EVENT_GET_EVENT_ERROR.getKey()) != null) {
                            errorStr = responseObj.get(JSONApiResponseKeysEnum.EVENT_GET_EVENT_ERROR.getKey())
                                    .toString();
                        }

                        if (errorStr != null
                                && (errorStr.equals(INVALID_SESSION) || errorStr.contains(UNKNOWN_TOKEN))) {
                            unsubscribe();
                            subscribe();
                        } else if (errorStr != null) {
                            logger.error("Unknown error message in event response: " + errorStr);
                        }
                    }
                }
                try {
                    synchronized (this) {
                        wait(SLEEPTIME);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            unsubscribe();
        }

    };

    private String getEventAsRequest(int subscriptionID, int timeout) {
        if (connManager.checkConnection()) {
            return JSONRequestConstants.JSON_EVENT_GET + JSONRequestConstants.PARAMETER_TOKEN
                    + connManager.getSessionToken() + JSONRequestConstants.INFIX_PARAMETER_SUBSCRIPTION_ID
                    + subscriptionID + JSONRequestConstants.INFIX_PARAMETER_TIMEOUT + timeout;
        }
        return null;
    }

    private boolean unsubscribeEvent(String name, int subscriptionID) {
        if (connManager.checkConnection()) {
            return digitalSTROM.unsubscribeEvent(connManager.getSessionToken(), EVENT_NAME, this.ID,
                    DigitalSTROMConfig.DEFAULT_CONNECTION_TIMEOUT, DigitalSTROMConfig.DEFAULT_READ_TIMEOUT);
        }
        return false;
    }

    private boolean unsubscribe() {
        return this.unsubscribeEvent(this.EVENT_NAME, this.ID);
    }

    private void handleEvent(JsonArray array) {
        if (array.size() > 0) {
            Event event = new JSONEventImpl(array);

            for (EventItem item : event.getEventItems()) {
                if (item.getName() != null && item.getName().contains(this.EVENT_NAME)) {
                    logger.debug(item.getName());
                    this.sceneManager.handleEvent(item);
                }
            }
        }
    }

}