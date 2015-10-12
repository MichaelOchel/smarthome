/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.DigitalSTROMManagerStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.SceneStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.TotalPowerConsumptionListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.StateEnums.ManagerStates;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.StateEnums.ManagerTypes;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.sensorJob.SensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.InternalScene;

/**
 *
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public interface DigitalSTROMDeviceStatusManager {

    /**
     *
     */
    public void start();

    /**
     *
     */
    public void stop();

    /**
     *
     */
    public void restart();

    /**
     *
     * @param scene
     * @param call_undo
     */
    public void sendSceneComandsToDSS(InternalScene scene, boolean call_undo);

    /**
     *
     * @param device
     */
    public void sendStopComandsToDSS(Device device);

    /**
     *
     * @param device
     */
    public void sendComandsToDSS(Device device);

    /**
     * This method adds a {@link SensorJobs} with the appropriate priority to the {@link SensorJobExecuter}.
     *
     * @param sensorJob
     * @param priority
     */
    public void updateSensorData(SensorJob sensorJob, String priority);

    /**
     * This method adds a {@link SensorJobs} with the appropriate priority to the {@link SceneSensorJobExecuter}.
     *
     * @param device
     * @param deviceStateUpdate
     */
    public void updateSceneData(Device device, DeviceStateUpdate deviceStateUpdate);

    /**
     *
     * @param deviceListener
     */
    public void registerDeviceListener(DeviceStatusListener deviceListener);

    /**
     *
     * @param deviceListener
     */
    public void unregisterDeviceListener(DeviceStatusListener deviceListener);

    /**
     *
     * @param totalPowerConsumptionListener
     */
    public void registerTotalPowerConsumptionListener(TotalPowerConsumptionListener totalPowerConsumptionListener);

    /**
     *
     */
    public void unregisterTotalPowerConsumptionListener();

    /**
     *
     * @param sceneListener
     */
    public void registerSceneListener(SceneStatusListener sceneListener);

    /**
     *
     * @param sceneListener
     */
    public void unregisterSceneListener(SceneStatusListener sceneListener);

    public void removeDevice(String dSID);

    public void registerStatusListener(DigitalSTROMManagerStatusListener statusListener);

    public void unregisterStatusListener();

    public ManagerTypes getManagerType();

    public ManagerStates getManagerState();
}
