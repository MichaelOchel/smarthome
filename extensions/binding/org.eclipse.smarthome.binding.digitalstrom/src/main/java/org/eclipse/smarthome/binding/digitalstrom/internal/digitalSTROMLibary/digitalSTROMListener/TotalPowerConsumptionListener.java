/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener;

/**
 * The {@link TotalPowerConsumptionListener} is notified if the total power consumption or the total electric meter
 * value
 * has changed.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public interface TotalPowerConsumptionListener {

    /**
     * This method is called when ever the total power consumption of the digitalSTROM-System has changed.
     *
     * @param newPowerConsumption
     */
    public void onTotalPowerConsumptionChanged(int newPowerConsumption);

    /**
     * This method is called when ever the total energy meter value of the digitalSTROM-System has changed.
     *
     * @param newPowerConsumption
     */
    public void onEnergyMeterValueChanged(int newEnergyMeterValue);

}
