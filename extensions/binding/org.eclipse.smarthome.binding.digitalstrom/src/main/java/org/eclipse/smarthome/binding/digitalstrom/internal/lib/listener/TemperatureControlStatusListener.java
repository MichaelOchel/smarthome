package org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlStatus;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.TemperatureSensorTransreciver;

public interface TemperatureControlStatusListener {

    public static Integer DISCOVERY = -2;

    public void configChanged(TemperatureControlStatus tempControlStatus);

    public void onTargetTemperatureChanged(Float newValue);

    public void onControlValueChanged(Integer newValue);

    public void onTemperatureControlIsNotConfigured();

    public void registerTemperatureSensorTransreciver(TemperatureSensorTransreciver temperatureSensorTransreciver);

    public Integer getID();
}
