/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link DigitalSTROMBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Ochel - Initial contribution
 * @author Mathias Siegele - Initial contribution
 */
public class DigitalSTROMBindingConstants {

    public static final String BINDING_ID = "digitalstrom";

    // List of all Thing Type Ids
    public static final String THING_TYPE_ID_DSS_BRIDGE = "dssBridge";

    public static final String THING_TYPE_ID_SCENE = "scene";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_DSS_BRIDGE = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_DSS_BRIDGE);

    public final static ThingTypeUID THING_TYPE_SCENE = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_SCENE);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_DSS_BRIDGE);

    /* List of all Channels */

    // Light
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_LIGHT_SWITCH = "lightSwitch";
    public static final String CHANNEL_COMBINED_2_STAGE_SWITCH = "Combined2StageSwitch";
    public static final String CHANNEL_COMBINED_3_STAGE_SWITCH = "Combined3StageSwitch";

    // black
    public static final String CHANNEL_GENERAL_DIMM = "generalDimm";
    public static final String CHANNEL_GENERAL_SWITCH = "generalSwitch";
    public static final String CHANNEL_GENERAL_SHADE = "generalShade";
    public static final String CHANNEL_GENERAL_COMBINED_2_STAGE_SWITCH = "generalCombined2StageSwitch";
    public static final String CHANNEL_GENERAL_COMBINED_3_STAGE_SWITCH = "generalCombined3StageSwitch";

    // shade
    public static final String CHANNEL_SHADE = "shade";
    // scene
    public static final String CHANNEL_SCENE = "scene";

    // sensor
    public static final String CHANNEL_ELECTRIC_METER = "electricMeter";
    public static final String CHANNEL_OUTPUT_CURRENT = "outputCurrent";
    public static final String CHANNEL_ACTIVE_POWER = "activePower";
    public static final String CHANNEL_TOTAL_ACTIVE_POWER = "totalActivePower"; // changed
    public static final String CHANNEL_TOTAL_ELECTRIC_METER = "totalElectricMeter";

    // options combined switches
    public static final String OPTION_COMBINED_BOTH_OFF = "0";
    public static final String OPTION_COMBINED_BOTH_ON = "200";
    public static final String OPTION_COMBINED_FIRST_ON = "90";
    public static final String OPTION_COMBINED_SECOND_ON = "130";

    /* config URIs */
    public static final String DEVICE_CONFIG = "binding:digitalstrom:device";
    public static final String GRAY_DEVICE_CONFIG = "binding:digitalstrom:grayDevice";
    public static final String DSS_BRIDE_CONFIG = "binding:digitalstrom:dssBridge";

    /* Bridge config properties */

    public static final String HOST = "ipAddress";
    public static final String USER_NAME = "userName";
    public static final String PASSWORD = "password";
    public static final String APPLICATION_TOKEN = "applicationToken";
    public static final String DS_ID = "dSID";
    public static final String DS_NAME = "dsName";
    public static final String SENSOR_DATA_UPDATE_INTERVALL = "sensorDataUpdateIntervall";
    public static final String DEFAULT_TRASH_DEVICE_DELEATE_TIME_KEY = "defaultTrashBinDeleateTime";
    public static final String TRUST_CERT_PATH_KEY = "trustCertPath";
    public final static String SENSOR_WAIT_TIME = "sensorWaitTime";

    /* Device config properties */

    public static final String DEVICE_UID = "dSUID";
    public static final String DEVICE_NAME = "deviceName";
    public static final String DEVICE_DSID = "dSID";
    public static final String DEVICE_HW_INFO = "hwInfo";
    public static final String DEVICE_ZONE_ID = "zoneID";
    public static final String DEVICE_GROUPS = "groups";
    public static final String DEVICE_OUTPUT_MODE = "outputmode";
    public static final String DEVICE_FUNCTIONAL_COLOR_GROUP = "funcColorGroup";
    public static final String DEVICE_METER_ID = "meterDSID";

    // Device properties scene
    public static final String DEVICE_SCENE = "scene"; // + number of scene

    // Sensor data channel properties
    public static final String ACTIVE_POWER_REFRESH_PRIORITY = "ActivePowerRefreshPriority";
    public static final String ELECTRIC_METER_REFRESH_PRIORITY = "ElectricMeterRefreshPriority";
    public static final String OUTPUT_CURRENT_REFRESH_PRIORITY = "OutputCurrentRefreshPriority";
    // options
    public static final String REFRESH_PRIORITY_NEVER = "never";
    public static final String REFRESH_PRIORITY_LOW = "low";
    public static final String REFRESH_PRIORITY_MEDIUM = "medium";
    public static final String REFRESH_PRIORITY_HIGH = "high";

    // public final static int DEFAULT_SENSOR_READING_WAIT_TIME = 60000;

    /* Scene config */
    public static final String SCENE_NAME = "sceneName";
    public static final String SCENE_ZONE_ID = "zoneID";
    public static final String SCENE_GROUP_ID = "groupID";
    public static final String SCENE_ID = "sceneID";

}
