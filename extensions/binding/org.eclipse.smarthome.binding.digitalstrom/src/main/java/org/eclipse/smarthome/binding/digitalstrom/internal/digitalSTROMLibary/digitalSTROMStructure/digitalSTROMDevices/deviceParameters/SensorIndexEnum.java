/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters;

/**
 * The {@link SensorIndexEnum} lists all available digitalSTROM sensor index.
 *
 * @author Alexander Betker
 * @since 1.3.0
 * @version digitalSTROM-API 1.14.5
 *
 * @author Michael Ochel - add missing java-doc and sensor-type
 * @author Matthias Siegele - add missing java-doc and sensor-type
 */
public enum SensorIndexEnum {

    ACTIVE_POWER(2, 4),
    OUTPUT_CURRENT(3, 5),
    ELECTRIC_METER(4, 6);

    private final int index;
    private final int type;

    private SensorIndexEnum(int index, int type) {
        this.index = index;
        this.type = type;
    }

    /**
     * Returns the sensor index of this {@link SensorIndexEnum} object.
     *
     * @return sensor index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the sensor type id of this {@link SensorIndexEnum} object.
     *
     * @return sensor type id
     */
    public int getType() {
        return type;
    }

}
