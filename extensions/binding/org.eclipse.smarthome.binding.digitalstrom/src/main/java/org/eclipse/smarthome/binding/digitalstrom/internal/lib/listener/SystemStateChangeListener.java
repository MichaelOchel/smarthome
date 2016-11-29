package org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener;

public interface SystemStateChangeListener {

    public void onSystemStateChanged(String stateType, String newState);
}
