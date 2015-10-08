/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMConfiguration;

public class DigitalSTROMConfig {

    /* Client configuration */

    // connection Configuration
    public static String APPLICATION_NAME = "ESH";
    // Timeouts
    public static final int DEFAULT_CONNECTION_TIMEOUT = 4000;
    public static int CONNECTION_TIMEOUT = DEFAULT_CONNECTION_TIMEOUT;
    public static final int DEFAULT_READ_TIMEOUT = 10000;
    public static int READ_TIMEOUT = DEFAULT_READ_TIMEOUT;
    public static final int DEFAULT_SENSORDATA_CONNECTION_TIMEOUT = 4000;
    public static int CONNECTION_SENSORDATA_TIMEOUT = DEFAULT_SENSORDATA_CONNECTION_TIMEOUT;
    public static final int DEFAULT_SENSORDATA_READ_TIMEOUT = 20000;
    public static int READ_SENSORDATA_TIMEOUT = DEFAULT_SENSORDATA_READ_TIMEOUT;

    public static String TRUST_CERT_PATH = null;

    public static final String EVENT_NAME = "ESH";

    /* Internal Configurations */

    // Trash Bin Config
    /**
     * default days after the trash devices get deleted
     */
    public static final int DEFAULT_TRASH_DEVICE_DELEATE_TIME = 7;
    /**
     * sets days after the trash devices get deleted
     */
    public static int TRASH_DEVICE_DELEATE_TIME = DEFAULT_TRASH_DEVICE_DELEATE_TIME;

    public static final int DEFAULT_BIN_CHECK_TIME = 360000; // in milliseconds
    public static int BIN_CHECK_TIME = DEFAULT_BIN_CHECK_TIME; // in milliseconds

    // Device update config
    public static final int DEFAULT_POLLING_FREQUENCY = 1000; // in milliseconds
    public static int POLLING_FREQUENCY = DEFAULT_POLLING_FREQUENCY; // in milliseconds

    /* Sensordata */

    // Sensodata read config
    public static final int DEFAULT_SENSORDATA_REFRESH_INTERVAL = 10000;
    public static int SENSORDATA_REFRESH_INTERVAL = DEFAULT_SENSORDATA_REFRESH_INTERVAL;

    public static final int DEFAULT_SENSOR_READING_WAIT_TIME = 60000;
    public static int SENSOR_READING_WAIT_TIME = DEFAULT_SENSOR_READING_WAIT_TIME;

    // sensor data Prioritys
    public static final String REFRESH_PRIORITY_NEVER = "never";
    public static final String REFRESH_PRIORITY_LOW = "low";
    public static final String REFRESH_PRIORITY_MEDIUM = "medium";
    public static final String REFRESH_PRIORITY_HIGH = "high";

    // max sensor reading cyclic to wait
    public static final long MEDIUM_PRIORITY_FACTOR = 5;
    public static final long LOW_PRIORITY_FACTOR = 10;

    public static final int DEFAULT_EVENT_LISTENER_REFRESHINTERVAL = 1000;
    public static int EVENT_LISTENER_REFRESHINTERVAL = DEFAULT_EVENT_LISTENER_REFRESHINTERVAL;

    public static final int STANDBY_ACTIVE_POWER = 2;
}
