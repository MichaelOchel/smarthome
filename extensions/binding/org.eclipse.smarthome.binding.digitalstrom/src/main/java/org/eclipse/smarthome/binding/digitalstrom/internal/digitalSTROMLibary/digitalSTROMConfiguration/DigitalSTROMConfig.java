/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMConfiguration;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.sensorJob.SensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.OutputModeEnum;

/**
 * The {@link DigitalSTROMConfig} contains all configurations for the digitalSTROMLibary.<br>
 * For that it gives default constants and variables that can be set during the run-time.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class DigitalSTROMConfig {

    /* Client configuration */

    // connection configuration

    /**
     * Sets the name for the application name to generate the application token.
     */
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

    /**
     * Sets the path to the SSL-Certification.
     */
    public static String TRUST_CERT_PATH = null;

    /**
     * Defines the name of the events witch will be received by the {@link EventListener}.
     */
    public static final String EVENT_NAME = "ESH";

    /* Internal Configurations */

    // Trash Bin Config
    /**
     * The default number of days after the trash devices is deleted.
     */
    public static final int DEFAULT_TRASH_DEVICE_DELEATE_TIME = 7;
    /**
     * Sets number of days after the trash devices is deleted.
     */
    public static int TRASH_DEVICE_DELEATE_TIME = DEFAULT_TRASH_DEVICE_DELEATE_TIME;

    /**
     * The default milliseconds after the trash devices will be checked if its time to delete.
     */
    public static final int DEFAULT_BIN_CHECK_TIME = 360000; // in milliseconds
    /**
     * Sets the milliseconds after the trash devices will be checked if its time to delete.
     */
    public static int BIN_CHECK_TIME = DEFAULT_BIN_CHECK_TIME; // in milliseconds

    // Device update config

    /**
     * Default interval of the polling frequency in milliseconds. The digitalSTROM-rules state that the
     * polling interval must to be at least 1 second.
     */
    public static final int DEFAULT_POLLING_FREQUENCY = 1000; // in milliseconds
    /**
     * Defines the interval of the polling frequency in milliseconds. The digitalSTROM-rules state that the
     * polling interval must to be at least 1 second.
     */
    public static int POLLING_FREQUENCY = DEFAULT_POLLING_FREQUENCY; // in milliseconds

    /* Sensordata */

    // Sensodata read config

    /**
     * Sets the interval to refresh the sensor data.
     */
    public static final int DEFAULT_SENSORDATA_REFRESH_INTERVAL = 60000;
    public static int SENSORDATA_REFRESH_INTERVAL = DEFAULT_SENSORDATA_REFRESH_INTERVAL;

    /**
     * Default time to wait between another {@link SensorJob} can be executed on a circuit.
     */
    public static final int DEFAULT_SENSOR_READING_WAIT_TIME = 60000;
    /**
     * Sets the time to wait between another {@link SensorJob} can be executed on a circuit.
     */
    public static int SENSOR_READING_WAIT_TIME = DEFAULT_SENSOR_READING_WAIT_TIME;

    // sensor data Prioritys
    public static final String REFRESH_PRIORITY_NEVER = "never";
    public static final String REFRESH_PRIORITY_LOW = "low";
    public static final String REFRESH_PRIORITY_MEDIUM = "medium";
    public static final String REFRESH_PRIORITY_HIGH = "high";

    // max sensor reading cyclic to wait
    public static final long MEDIUM_PRIORITY_FACTOR = 5;
    public static final long LOW_PRIORITY_FACTOR = 10;

    /**
     * Defines the event reading interval of the {@link EventListener} in milliseconds.
     */
    public static int EVENT_LISTENER_REFRESHINTERVAL = DEFAULT_POLLING_FREQUENCY;

    /**
     * Sets the max standby active power for a device. It's needed to set a {@link Device} with output mode
     * {@link OutputModeEnum#WIPE} on if it isen't any more in standby mode.
     */
    public static int STANDBY_ACTIVE_POWER = 2;
}
