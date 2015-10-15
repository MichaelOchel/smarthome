/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMConfiguration.DigitalSTROMConfig;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.sensorJob.SensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.sensorJob.impl.DeviceConsumptionSensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.sensorJob.impl.DeviceOutputValueSensorJob;

/**
 * The {@link SensorJobExecutor} is the implementation of the {@link AbstractSensorJobExecuter} to execute
 * digitalSTROM-Device {@link SensorJob}'s e.g. {@link DeviceConsumptionSensorJob} and
 * {@link DeviceOutputValueSensorJob}.
 *
 * <p>
 * In addition priorities can be assigned to jobs, but the follow list schows the maximum devaluation of a
 * {@link SensorJob} per priority.
 * <ul>
 * <li>low priority: read circuits before execution set in {@link DigitalSTROMConfig.LOW_PRIORITY_FACTOR}</li>
 * <li>medium priority: read circuits before execution set in {@link DigitalSTROMConfig.MEDIUM_PRIORITY_FACTOR}</li>
 * <li>high priority:read circuits before execution 0</li>
 * </ul>
 * </p>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class SensorJobExecutor extends AbstractSensorJobExecutor {

    private final long mediumFactor = DigitalSTROMConfig.SENSOR_READING_WAIT_TIME
            * DigitalSTROMConfig.MEDIUM_PRIORITY_FACTOR;
    private final long lowFactor = DigitalSTROMConfig.SENSOR_READING_WAIT_TIME * DigitalSTROMConfig.LOW_PRIORITY_FACTOR;

    public SensorJobExecutor(DigitalSTROMConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    public void addHighPriorityJob(SensorJob sensorJob) {
        if (sensorJob == null)
            return;
        addSensorJobToCircuitScheduler(sensorJob);
    }

    @Override
    public void addMediumPriorityJob(SensorJob sensorJob) {
        if (sensorJob == null)
            return;
        sensorJob.setInitalisationTime(sensorJob.getInitalisationTime() + this.mediumFactor);
        addSensorJobToCircuitScheduler(sensorJob);
    }

    @Override
    public void addLowPriorityJob(SensorJob sensorJob) {
        if (sensorJob == null)
            return;
        sensorJob.setInitalisationTime(sensorJob.getInitalisationTime() + this.lowFactor);
        addSensorJobToCircuitScheduler(sensorJob);
    }

}
