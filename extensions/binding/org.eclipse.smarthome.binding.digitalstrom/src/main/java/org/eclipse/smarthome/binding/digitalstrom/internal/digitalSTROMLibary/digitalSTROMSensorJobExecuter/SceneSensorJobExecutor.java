/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager.DigitalSTROMConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.sensorJob.SensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.sensorJob.impl.SceneConfigSensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.sensorJob.impl.SceneOutputValueSensorJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SceneSensorJobExecutor} is the implementation of the {@link AbstractSensorJobExecuter} to execute
 * digitalSTROM-Device scene configurations {@link SensorJob}'s e.g. {@link SceneConfigSensorJob} and
 * {@link SceneOutputValueSensorJob}.
 * <p>
 * In addition priorities can be assigned to jobs therefore an {@link SceneSensorJobExecutor} offers the methods
 * {@link #addHighPriorityJob()}, {@link #addLowPriorityJob()} and {@link #addLowPriorityJob()}.
 * </p>
 * <p>
 * <b>NOTE:</b><br>
 * In contrast to the {@link SensorJobExecutor} the {@link SceneSensorJobExecutor} will execute {@link SensorJob}'s with
 * high priority always before medium priority {@link SensorJob}s and so on.
 * </p>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class SceneSensorJobExecutor extends AbstractSensorJobExecutor {

    private Logger logger = LoggerFactory.getLogger(SceneSensorJobExecutor.class);

    public SceneSensorJobExecutor(DigitalSTROMConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    public void addHighPriorityJob(SensorJob sensorJob) {
        if (sensorJob == null)
            return;
        sensorJob.setInitalisationTime(0);
        addSensorJobToCircuitScheduler(sensorJob);
        logger.debug("Add SceneSensorJob from device with dSID {} and high-priority to SceneJensorJobExecuter",
                sensorJob.getDsid());

    }

    @Override
    public void addMediumPriorityJob(SensorJob sensorJob) {
        if (sensorJob == null)
            return;
        sensorJob.setInitalisationTime(1);
        addSensorJobToCircuitScheduler(sensorJob);
        logger.debug("Add SceneSensorJob from device with dSID {} and medium-priority to SceneJensorJobExecuter",
                sensorJob.getDsid());
    }

    @Override
    public void addLowPriorityJob(SensorJob sensorJob) {
        if (sensorJob == null)
            return;
        sensorJob.setInitalisationTime(2);
        addSensorJobToCircuitScheduler(sensorJob);
        logger.debug("Add SceneSensorJob from device with dSID {} and low-priority to SceneJensorJobExecuter",
                sensorJob.getDsid());
    }

}
