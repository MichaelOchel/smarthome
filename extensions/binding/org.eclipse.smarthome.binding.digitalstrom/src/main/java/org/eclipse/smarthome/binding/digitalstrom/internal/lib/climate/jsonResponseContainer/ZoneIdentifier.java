/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer;

/**
 * The {@link ZoneIdentifier} can be implement to identify a zone.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface ZoneIdentifier {

    /**
     * Returns the zoneID of this zone.
     *
     * @return the zoneID
     */
    public Integer getZoneID();

    /**
     * Returns the zoneName of this zone.
     *
     * @return the zoneName
     */
    public String getZoneName();
}