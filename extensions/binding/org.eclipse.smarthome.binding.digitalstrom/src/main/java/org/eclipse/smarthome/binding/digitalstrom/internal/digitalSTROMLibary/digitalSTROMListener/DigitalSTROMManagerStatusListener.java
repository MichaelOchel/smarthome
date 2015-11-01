/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
