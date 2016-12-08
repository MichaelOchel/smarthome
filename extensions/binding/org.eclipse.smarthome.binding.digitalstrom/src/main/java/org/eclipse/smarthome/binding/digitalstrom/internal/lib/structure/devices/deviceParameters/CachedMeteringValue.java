/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters;

import java.util.Date;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringUnitsEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.DSID;

/**
 * The {@link CachedMeteringValue} saves the metering value of an digitalSTROM-Circuit.
 *
 * @author Alexander Betker - Initial contribution
 * @author Michael Ochel - add methods getDateAsDate(), getMeteringType() and getMeteringUnit(); add missing java-doc
 * @author Matthias Siegele - add methods getDateAsDate(), getMeteringType() and getMeteringUnit(); add missing java-doc
 */
public interface CachedMeteringValue {

    /**
     * Returns the {@link DSID} of the digitalSTROM-Circuit.
     *
     * @return dSID of circuit
     */
    public DSID getDsid();

    /**
     * Returns the saved sensor value.
     *
     * @return sensor value
     */
    public double getValue();

    /**
     * Returns the timestamp when the sensor value was read out as {@link String}.
     *
     * @return read out timestamp
     */
    public String getDate();

    /**
     * Returns the timestamp when the sensor value was read out as {@link Date}.
     *
     * @return read out timestamp
     */
    public Date getDateAsDate();

    /**
     * Returns the {@link MeteringTypeEnum} of this {@link CachedMeteringValue}.
     *
     * @return metering type as {@link MeteringTypeEnum}
     */
    public MeteringTypeEnum getMeteringType();

    /**
     * Returns the {@link MeteringUnitsEnum} of this {@link CachedMeteringValue}.
     *
     * @return metering unit as {@link MeteringUnitsEnum}
     */
    public MeteringUnitsEnum getMeteringUnit();
}
