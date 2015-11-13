/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.config.Config;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.SensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DSID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link CircuitScheduler} represents a circuit in the digitalSTROM-System and manages the priorities and
 * execution times for the {@link SensorJob}s on this circuit.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class CircuitScheduler {

    private Logger logger = LoggerFactory.getLogger(CircuitScheduler.class);

    private class SensorJobComparator implements Comparator<SensorJob> {

        @Override
        public int compare(SensorJob job1, SensorJob job2) {
            return ((Long) job1.getInitalisationTime()).compareTo(job2.getInitalisationTime());
        }
    }

    private final DSID meterDSID;
    private long nextExecutionTime = System.currentTimeMillis();
    private PriorityQueue<SensorJob> sensorJobQueue = new PriorityQueue<SensorJob>(10, new SensorJobComparator());
    private Config config;

    /**
     * Creates a new {@link CircuitScheduler}.
     *
     * @param meterDSID
     * @param config
     */
    public CircuitScheduler(DSID meterDSID, Config config) {
        if (meterDSID == null) {
            throw new IllegalArgumentException("The meterDSID must not be null!");
        }
        this.meterDSID = meterDSID;
        this.config = config;
    }

    /**
     * Creates a new {@link CircuitScheduler} and add the first {@link SensorJob} to this {@link CircuitScheduler}.
     *
     * @param sensorJob
     * @param config
     */
    public CircuitScheduler(SensorJob sensorJob, Config config) {
        this.meterDSID = sensorJob.getMeterDSID();
        this.sensorJobQueue.add(sensorJob);
        this.config = config;
        logger.debug("create circuitScheduler: " + this.getMeterDSID() + " and add sensorJob: "
                + sensorJob.getDSID().toString());
    }

    /**
     * Returns the meterDSID of the dS-Meter in which the {@link SensorJob}s will be executed.
     *
     * @return meterDSID
     */
    public DSID getMeterDSID() {
        return this.meterDSID;
    }

    /**
     * Adds a new SensorJob to this {@link CircuitScheduler}, if no {@link SensorJob} with a higher priority exists.
     *
     * @param sensorJob
     */
    public void addSensorJob(SensorJob sensorJob) {
        synchronized (sensorJobQueue) {
            if (!this.sensorJobQueue.contains(sensorJob)) {
                sensorJobQueue.add(sensorJob);
                logger.debug("Add sensorJob: " + sensorJob.toString() + "to circuitScheduler: " + this.getMeterDSID());
            } else if (checkSensorJobPrio(sensorJob)) {
                logger.debug("add sensorJob: " + sensorJob.toString() + "with higher priority to circuitScheduler: "
                        + this.getMeterDSID());
            } else {
                logger.debug("sensorJob: " + sensorJob.getDSID().toString() + " allready exist with a higher priority");
            }
        }
    }

    private boolean checkSensorJobPrio(SensorJob sensorJob) {
        synchronized (sensorJobQueue) {
            for (Iterator<SensorJob> iter = sensorJobQueue.iterator(); iter.hasNext();) {
                SensorJob existSensorJob = iter.next();
                if (existSensorJob.equals(sensorJob)) {
                    if (existSensorJob != null
                            && sensorJob.getInitalisationTime() < existSensorJob.getInitalisationTime()) {
                        iter.remove();
                        sensorJobQueue.add(sensorJob);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns the next {@link SensorJob} which can be executed or null, if there are no more {@link SensorJob} to
     * execute or the wait time between the {@link SensorJob}s executions has not expired yet.
     *
     * @return next SensorJob or null
     */
    public SensorJob getNextSensorJob() {
        synchronized (sensorJobQueue) {
            if (sensorJobQueue.peek() != null && this.nextExecutionTime <= System.currentTimeMillis()) {
                nextExecutionTime = System.currentTimeMillis() + config.getSensorReadingWaitTime();
                return sensorJobQueue.poll();
            } else {
                return null;
            }
        }
    }

    /**
     * Returns the time when the next {@link SensorJob} can be executed.
     *
     * @return next SesnorJob execution time
     */
    public Long getNextExecutionTime() {
        return this.nextExecutionTime;
    }

    /**
     * Returns the delay when the next {@link SensorJob} can be executed.
     *
     * @return next SesnorJob execution delay
     */
    public Long getNextExecutionDelay() {
        long delay = this.nextExecutionTime - System.currentTimeMillis();
        return delay > 0 ? delay : 0;
    }

    /**
     * Removes all {@link SensorJob} of a specific {@link Device} with the given {@link DSID}.
     *
     * @param dSID of the device
     */
    public void removeSensorJob(DSID dSID) {
        synchronized (sensorJobQueue) {
            for (Iterator<SensorJob> iter = sensorJobQueue.iterator(); iter.hasNext();) {
                SensorJob job = iter.next();
                if (job.getDSID().equals(dSID)) {
                    iter.remove();
                }
            }
            logger.debug("Remove SensorJobs from device with dSID {}." + dSID);
        }
    }

    /**
     * Returns true, if there are no more {@link SensorJob}s to execute, otherwise false.
     *
     * @return no more SensorJobs? (true | false)
     */
    public boolean noMoreJobs() {
        synchronized (sensorJobQueue) {
            return this.sensorJobQueue.isEmpty();
        }
    }
}
