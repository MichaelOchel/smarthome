package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener;

import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.StateEnums.ManagerStates;
import org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.StateEnums.ManagerTypes;

/**
 * The {@link DigitalSTROMManagerStatusListener} is notified if the state of digitalSTROM-Manager has changed.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public interface DigitalSTROMManagerStatusListener {

    /**
     * This method is called when ever the state of an digitalkSTROM-Manager has changed.<br>
     * For that it passes the {@link ManagerTypes} and the new {@link ManagerStates}.
     *
     * @param managerType
     * @param statee
     */
    public void onStatusChanged(ManagerTypes managerType, ManagerStates state);

}
