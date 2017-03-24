/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants;

/**
 * The {@link MeteringUnitsEnum} lists all available digitalSTROM metering units.
 *
 * @author Alexander Betker - initial contributer
 * @author Michael Ochel - remove W, because it does not exist any more
 * @author Matthias Siegele - remove W, because it does not exist any more
 */
public enum MeteringUnitsEnum {
    WH("Wh"),
    WS("Ws");

    public final String UNIT;

    private MeteringUnitsEnum(String unit) {
        this.UNIT = unit;
    }
}
