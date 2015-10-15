/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure;

import java.util.List;

/**
 * The {@link DetailedGroupInfo} represents a digitalSTROM-Group with a list of all dSID's of the included
 * digitalSTROM-Devices.
 *
 * @author Alexander Betker
 * @since 1.3.0
 */
public interface DetailedGroupInfo extends Group {

    /**
     * Returns the list of all dSUID's of the included digitalSTROM-Devices.
     *
     * @return list of all dSUID
     */
    public List<String> getDeviceList();

}
