/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMManager;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.DigitalSTROMConnectionListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.DigitalSTROMManagerStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.SceneStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.TotalPowerConsumptionListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.StateEnums.ManagerStates;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.StateEnums.ManagerTypes;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.SceneSensorJobExecutor;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.SensorJobExecutor;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMSensorJobExecuter.sensorJob.SensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMScene.InternalScene;

/**
 * <p>
 * The {@link DigitalSTROMDeviceStatusManager} is responsible for the synchronization between the internal model of the
 * digitalSTROM-devices and the real
 * existing digitalSTROM-devices. You can change the state of an device by sending a direct command to the devices or by
 * calling a scene. Furthermore the {@link DigitalSTROMDeviceStatusManager} get informed over the
 * {@link DigitalSTROMScenesManager} by the {@link EventListener} if scenes are called by
 * external sources. All configurations of the physically device will be synchronized to the internally managed model
 * and updated as required. The {@link DigitalSTROMDeviceStatusManager} also initializes {@link SensorJob}'s with the
 * {@link SensorJobExecutor} and {@link SceneSensorJobExecutor} to update
 * required sensor and scene data.
 * </p>
 * <p>
 * Therefore the {@link DigitalSTROMDeviceStatusManager} uses the {@link DigitalSTROMStructureManager} for the internal
 * management of the structure of the digitalSTROM-system,
 * {@link DigitalSTROMConnectionManager} to check the connectivity, {@link DigitalSTROMScenesManager} to identify
 * externally called scenes and to update the affected devices
 * of these called scenes to the internal model. The most methods of the above-named managers will be directly called
 * over the {@link DigitalSTROMDeviceStatusManager}, because they are linked to the affected managers.
 * </P>
 * <p>
 * To get informed by all relevant informations you can register some useful listeners. Here the list of all listeners
 * which may be registered or unregistered:
 * <ul>
 * <li>{@link DeviceStatusListener} over {@link #registerDeviceListener(DeviceStatusListener)} respectively
 * {@link #unregisterDeviceListener(DeviceStatusListener)}</li>
 * <li>{@link SceneStatusListener} over {@link #registerSceneListener(SceneStatusListener)} respectively
 * {@link #unregisterSceneListener(SceneStatusListener)}</li>
 * <li>{@link TotalPowerConsumtionListener} over
 * {@link #registerTotalPowerConsumptionListener(TotalPowerConsumptionListener)} respectively
 * {@link #unregisterTotalPowerConsumptionListener(TotalPowerConsumptionListener)}</li>
 * <li>{@link DigitalSTROMManagerStatusListener} over {@link #registerStatusListener(DigitalSTROMManagerStatusListener)}
 * respectively {@link #unregisterStatusListener()}</li>
 * <li>{@link DigitalSTROMConnectionListener} over {@link #registerDeviceListener(DeviceStatusListener)} respectively
 * {@link #unregisterDeviceListener(DeviceStatusListener)}</li>
 * </ul>
 * For what the listener can be used please have a look at the listener.
 * </p>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public interface DigitalSTROMDeviceStatusManager {

    /**
     * Starts the working process for device synchronization, may existing {@link SensorJobExecutor} and
     * {@link SceneSensorJobExecutor} and the {@link DigitalSTROMSceneManager}.
     */
    public void start();

    /**
     * Stops all started threads.
     */
    public void stop();

    /**
     * This method sends a call scene command for the given scene if call_undo is true to the
     * digitalSTROM-Server otherwise it sends a undo command.
     * <br>
     * It also update the scene state if the command was send successful.
     *
     * @param scene
     * @param call_undo (true = call | false = undo)
     */
    public void sendSceneComandsToDSS(InternalScene scene, boolean call_undo);

    /**
     * This method sends a stop command for the given device to the digitalSTROM-Server.
     * <br>
     * It also reads out the current output value of the device and updates it if the command was send successful.
     *
     * @param device
     */
    public void sendStopComandsToDSS(Device device);

    /**
     * This method sends all not sent status updates of the given device over {@link Device#getNextDeviceUpdateState()}
     * to the digitalSTROM-Server.
     * <br>
     * It also updates the device if the command was send successful.
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
     * Registers the given {@link DeviceStatusListener} to the {@link Device} if it exist or registers it as a
     * device-discovery if the id of the deviceListener is {@link DeviceStatusListener#DEVICE_DESCOVERY}.
     *
     * @param deviceListener
     */
    public void registerDeviceListener(DeviceStatusListener deviceListener);

    /**
     * Unregisters the given {@link DeviceStatusListener} from the {@link Device} if it exist or unregisters the
     * device-discovery if the id of the deviceListener is {@link DeviceStatusListener#DEVICE_DESCOVERY}.
     *
     * @param deviceListener
     */
    public void unregisterDeviceListener(DeviceStatusListener deviceListener);

    /**
     * Registers the given {@link TotalPowerConsumptionListener} to the {@link DigitalSTROMDeviceStatusManager}.
     *
     * @param totalPowerConsumptionListener
     */
    public void registerTotalPowerConsumptionListener(TotalPowerConsumptionListener totalPowerConsumptionListener);

    /**
     * Unregisters the {@link TotalPowerConsumptionListener} from the {@link DigitalSTROMDeviceStatusManager}.
     */
    public void unregisterTotalPowerConsumptionListener();

    /**
     * Registers the given {@link SceneStatusListener} to the {@link InternalScene} if it exist or registers it as a
     * scene-discovery if the id of the sceneListener is {@link SceneStatusListener#SCENE_DESCOVERY}.
     *
     * @param sceneListener
     */
    public void registerSceneListener(SceneStatusListener sceneListener);

    /**
     * Unregisters the given {@link SceneStatusListener} from the {@link InternalScene} if it exist or unregisters the
     * scene-discovery if the id of the deviceListener is {@link SceneStatusListener#SCENE_DESCOVERY}.
     *
     * @param sceneListener
     */
    public void unregisterSceneListener(SceneStatusListener sceneListener);

    /**
     * Registers the given {@link DigitalSTROMConnectionListener} to the {@link DigitalSTROMConnectionManager}.
     *
     * @param connectionListener
     */
    public void reisterDigitalSTROMConnectionListener(DigitalSTROMConnectionListener connectionListener);

    /**
     * Unregisters the {@link DigitalSTROMConnectionListener} from the {@link DigitalSTROMConnectionManager}.
     */
    public void unreisterDigitalSTROMConnectionListener();

    /**
     * Removes the {@link Device} with the given dSID from the internal digitalSTROM model if it exists.
     *
     * @param dSID
     */
    public void removeDevice(String dSID);

    /**
     * Registers the given {@link DigitalSTROMManagerStatusListener} to all available digitalSTROM-managers.
     *
     * @param statusListener
     */
    public void registerStatusListener(DigitalSTROMManagerStatusListener statusListener);

    /**
     * Unregisters the {@link DigitalSTROMManagerStatusListener} from all available digitalSTROM-managers.
     */
    public void unregisterStatusListener();

    /**
     * Returns the {@link ManagerTypes} of this class.
     *
     * @return these {@link ManagerTypes}
     */
    public ManagerTypes getManagerType();

    /**
     * Returns the current {@link ManagerStates}.
     *
     * @return current {@link ManagerStates}
     */
    public ManagerStates getManagerState();
}
