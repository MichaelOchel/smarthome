/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.sensorJob;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.AbstractSensorJobExecutor;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.SceneSensorJobExecutor;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.SensorJobExecutor;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMServerConnection.DigitalSTROMAPI;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DSID;

/**
 * The {@link SensorJob} represents an executable job to read out digitalSTROM-Sensors or device configurations like
 * scene values.<br>
 * It can be added to an implementation of the {@link AbstractSensorJobExecutor} e.g. {@link SceneSensorJobExecutor} or
 * {@link SensorJobExecutor}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface SensorJob {

    /**
     * Returns the dSID of the {@link Device} for which this job is to be executed.
     *
     * @return dSID from the device
     */
    public DSID getDsid();

    /**
     * Returns the dSID of the digitalSTROM-Meter on which this job is to be executed.
     *
     * @return
     */
    public DSID getMeterDSID();

    /**
     * Executes the SensorJob.
     *
     * @param digitalSTROM client
     * @param sessionToken
     */
    public void execute(DigitalSTROMAPI digitalSTROM, String sessionToken);

    /**
     * Returns the time when the {@link SensorJob} was initialized.
     *
     * @return the initialization time
     */
    public long getInitalisationTime();

    /**
     * Sets the time when the {@link SensorJob} was initialized e.g. to decrease the priority of this {@link SensorJob}.
     *
     * @param time
     */
    public void setInitalisationTime(long time);
}
