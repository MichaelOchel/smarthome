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
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.SensorIndexEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DeviceConsumptionSensorJob} is the implementation of a {@link SensorJob}
 * for reading out a power consumption of a digitalSTROM-Device.
 *
 * @author Alexander Betker
 * @author Alex Maier
 * @author Michael Ochel - updated and added some methods
 * @author Matthias Siegele - updated and added some methods
 *
 */
public class DeviceConsumptionSensorJob implements SensorJob {

    private static final Logger logger = LoggerFactory.getLogger(DeviceConsumptionSensorJob.class);
    private Device device = null;
    private SensorEnum sensorIndex = null;
    private DSID meterDSID = null;
    private long initalisationTime = 0;

    /**
     * Creates a new {@link DeviceConsumptionSensorJob} with the given {@link SensorIndexEnum}.
     *
     * @param device
     * @param index sensor index
     */
    public DeviceConsumptionSensorJob(Device device, SensorEnum index) {
        this.device = device;
        this.sensorIndex = index;
        this.meterDSID = device.getMeterDSID();
        this.initalisationTime = System.currentTimeMillis();
    }

    @Override
    public void execute(DigitalSTROMAPI digitalSTROM, String token) {
        int consumption = digitalSTROM.getDeviceSensorValue(token, this.device.getDSID(), null, this.sensorIndex);
        logger.debug("SensorIndex: " + this.sensorIndex + ", DeviceConsumption : " + consumption + ", DSID: "
                + this.device.getDSID().getValue());

        switch (this.sensorIndex) {

            case ACTIVE_POWER:
                // logger.info("DeviceConsumption : "+consumption+", DSID: "+this.device.getDSID().getValue());
                this.device.updateInternalDeviceState(
                        new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ACTIVE_POWER, consumption));
                break;
            case OUTPUT_CURRENT:
                this.device.updateInternalDeviceState(
                        new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ELECTRIC_METER, consumption));
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
            String device = this.device.getDSID().getValue() + this.sensorIndex.getSensorType();
            return device.equals(other.device.getDSID().getValue() + other.sensorIndex.getSensorType());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new String(this.device.getDSID().getValue() + this.sensorIndex.getSensorType()).hashCode();
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
