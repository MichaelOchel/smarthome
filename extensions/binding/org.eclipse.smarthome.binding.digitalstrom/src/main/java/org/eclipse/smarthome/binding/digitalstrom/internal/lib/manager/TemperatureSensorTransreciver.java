package org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager;

public interface TemperatureSensorTransreciver {

    // TODO: in anderes package einordnen

    public static float MAX_TEMP = 50f;
    // TODO: nachgucken wie genau
    public static float MIN_TEMP = -40f;
    public static float MAX_CONTROLL_VALUE = 100f;
    public static float MIN_CONTROLL_VALUE = 0f;

    public boolean pushTargetTemperature(Integer zoneID, Float newValue);

    public boolean pushControlValue(Integer zoneID, Float newValue);
}
