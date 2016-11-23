/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * The {@link MeteringTypeEnum} lists all available digitalSTROM metering types.
 *
 * @author Alexander Betker
 * @author Michael Ochel - add MeteringUnitEnum list
 * @author Matthias Siegele - add MeteringUnitEnum list
 */
public enum MeteringTypeEnum {
    energy(Lists.newArrayList(MeteringUnitsEnum.Wh, MeteringUnitsEnum.Ws)),
    // currently null by request getLast
    // energyDelta(Lists.newArrayList(MeteringUnitsEnum.Wh, MeteringUnitsEnum.Ws)),
    consumption(Lists.newArrayList(MeteringUnitsEnum.Wh));

    public final List<MeteringUnitsEnum> meteringUnits;

    private MeteringTypeEnum(List<MeteringUnitsEnum> meteringUnits) {
        this.meteringUnits = meteringUnits;
    }

    public List<MeteringUnitsEnum> getMeteringUnitList() {
        return meteringUnits;
    }
}
