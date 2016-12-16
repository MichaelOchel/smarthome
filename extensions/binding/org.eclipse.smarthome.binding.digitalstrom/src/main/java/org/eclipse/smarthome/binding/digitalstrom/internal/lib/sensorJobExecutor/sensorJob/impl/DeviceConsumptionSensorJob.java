/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.impl;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.SensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.DsAPI;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.SensorEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.DSID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DeviceConsumptionSensorJob} is the implementation of a {@link SensorJob}
 * for reading out the current value of the of a digitalSTROM-Device sensor and updates the {@link Device}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class DeviceConsumptionSensorJob implements SensorJob {

    private static final Logger logger = LoggerFactory.getLogger(DeviceConsumptionSensorJob.class);
    private Device device = null;
    private SensorEnum sensorType = null;
    private DSID meterDSID = null;
    private long initalisationTime = 0;
    private boolean updateDevice = true;

    /**
     * Creates a new {@link DeviceConsumptionSensorJob}. Through updateDevice you can set, if the {@link Device} will be
     * updates automatically.
     *
     * @param device
     * @param type
     * @param updateDevice (true = automatically device, otherwise false)
     * @see #DeviceConsumptionSensorJob(Device, SensorEnum)
     */
    public DeviceConsumptionSensorJob(Device device, SensorEnum type, boolean updateDevice) {
        this.device = device;
        this.sensorType = type;
        this.meterDSID = device.getMeterDSID();
        this.initalisationTime = System.currentTimeMillis();
        this.updateDevice = updateDevice;
    }

    /**
     * Creates a new {@link DeviceConsumptionSensorJob} with the given {@link SensorEnum} for the given {@link Device}
     * and automatically {@link Device} update.
     *
     * @param device
     * @param type
     */
    public DeviceConsumptionSensorJob(Device device, SensorEnum type) {
        this.device = device;
        this.sensorType = type;
        this.meterDSID = device.getMeterDSID();
        this.initalisationTime = System.currentTimeMillis();
    }

    @Override
    public void execute(DsAPI digitalSTROM, String token) {
        int consumption = digitalSTROM.getDeviceSensorValue(token, this.device.getDSID(), null, null,
                device.getSensorIndex(sensorType));
        logger.debug("Executes {} new device consumption is {}", this.toString(), consumption);
        if (updateDevice) {
            device.setDeviceSensorDsValueBySensorJob(sensorType, consumption);
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
    public DSID getDSID() {
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

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DeviceConsumptionSensorJob [sensorType=" + sensorType + ", sensorIndex="
                + device.getSensorIndex(sensorType) + ", deviceDSID : " + device.getDSID().getValue() + ", meterDSID="
                + meterDSID + ", initalisationTime=" + initalisationTime + "]";
    }

    @Override
    public String getID() {
        return getID(device, sensorType);
    }

    /**
     * Returns the id for a {@link DeviceConsumptionSensorJob} with the given {@link Device} and {@link SensorEnum}.
     *
     * @param device
     * @param sensorType
     * @return id
     */
    public static String getID(Device device, SensorEnum sensorType) {
        return DeviceConsumptionSensorJob.class.getSimpleName() + "-" + device.getDSID().getValue() + "-"
                + sensorType.toString();
    }
}
