package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.StateEnums.ManagerStates;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.StateEnums.ManagerTypes;

public interface DigitalSTROMManagerStatusListener {

    public void onStatusChanged(ManagerTypes managerType, ManagerStates state);
}
