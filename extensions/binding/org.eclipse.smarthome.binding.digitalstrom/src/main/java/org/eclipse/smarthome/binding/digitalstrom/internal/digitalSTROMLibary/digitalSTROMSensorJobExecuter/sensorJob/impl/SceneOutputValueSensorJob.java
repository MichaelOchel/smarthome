/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.sensorJob.impl;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.sensorJob.SensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMServerConnection.DigitalSTROMAPI;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DSID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SceneOutputValueSensorJob} is the implementation of a {@link SensorJob}
 * for reading out a device scene configuration of a digitalSTROM-Device and store it into the {@link Device}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class SceneOutputValueSensorJob implements SensorJob {

    private static final Logger logger = LoggerFactory.getLogger(SceneOutputValueSensorJob.class);

    private Device device = null;
    private short sceneId = 0;
    private DSID meterDSID = null;
    private long initalisationTime = 0;

    /**
     * Creates a new {@link SceneOutputValueSensorJob} for the given scene id.
     *
     * @param device
     * @param sceneId
     */
    public SceneOutputValueSensorJob(Device device, short sceneId) {
        this.device = device;
        this.sceneId = sceneId;
        this.meterDSID = device.getMeterDSID();
        this.initalisationTime = System.currentTimeMillis();
    }

    @Override
    public void execute(DigitalSTROMAPI digitalSTROM, String token) {
        int sceneValue = digitalSTROM.getSceneValue(token, this.device.getDSID(), this.sceneId);

        if (sceneValue != -1) {
            this.device.setSceneOutputValue(this.sceneId, sceneValue);
            logger.info("UPDATED sceneOutputValue for dsid: " + this.device.getDSID() + ", sceneID: " + sceneId
                    + ", value: " + sceneValue);

        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SceneOutputValueSensorJob) {
            SceneOutputValueSensorJob other = (SceneOutputValueSensorJob) obj;
            String str = other.device.getDSID().getValue() + "-" + other.sceneId;
            return (this.device.getDSID().getValue() + "-" + this.sceneId).equals(str);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new String(this.device.getDSID().getValue() + this.sceneId).hashCode();
    }

    @Override
    public DSID getDsid() {
        return device.getDSID();
    }

    @Override
    public DSID getMeterDSID() {
        return this.meterDSID;
    }

    @Override
    public long getInitalisationTime() {
        return this.initalisationTime;
    }

    @Override
    public void setInitalisationTime(long time) {
        this.initalisationTime = time;
    }

}
