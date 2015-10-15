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
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceStateUpdateImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.SensorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DeviceConsumptionSensorJob} is the implementation of a {@link SensorJob}
 * for reading out the current value of the of a digitalSTROM-Device sensor and update the {@link Device}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class DeviceConsumptionSensorJob implements SensorJob {

    private static final Logger logger = LoggerFactory.getLogger(DeviceConsumptionSensorJob.class);
    private Device device = null;
    private SensorEnum sensorType = null;
    private DSID meterDSID = null;
    private long initalisationTime = 0;

    /**
     * Creates a new {@link DeviceConsumptionSensorJob} with the given {@link SensorEnum}.
     *
     * @param device
     * @param type sensor index
     */
    public DeviceConsumptionSensorJob(Device device, SensorEnum type) {
        this.device = device;
        this.sensorType = type;
        this.meterDSID = device.getMeterDSID();
        this.initalisationTime = System.currentTimeMillis();
    }

    @Override
    public void execute(DigitalSTROMAPI digitalSTROM, String token) {
        int consumption = digitalSTROM.getDeviceSensorValue(token, this.device.getDSID(), null, this.sensorType);
        logger.debug("SensorIndex: " + this.sensorType + ", DeviceConsumption : " + consumption + ", DSID: "
                + this.device.getDSID().getValue());

        switch (this.sensorType) {

            case ACTIVE_POWER:
                // logger.info("DeviceConsumption : "+consumption+", DSID: "+this.device.getDSID().getValue());
                this.device.updateInternalDeviceState(
                        new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ACTIVE_POWER, consumption));
                break;
            case OUTPUT_CURRENT:
                this.device.updateInternalDeviceState(
                        new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OUTPUT_CURRENT, consumption));
                break;
            case ELECTRIC_METER:
                this.device.updateInternalDeviceState(
                        new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ELECTRIC_METER, consumption));
                break;
            default:
                break;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeviceConsumptionSensorJob) {
            DeviceConsumptionSensorJob other = (DeviceConsumptionSensorJob) obj;
            String device = this.device.getDSID().getValue() + this.sensorType.getSensorType();
            return device.equals(other.device.getDSID().getValue() + other.sensorType.getSensorType());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new String(this.device.getDSID().getValue() + this.sensorType.getSensorType()).hashCode();
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
