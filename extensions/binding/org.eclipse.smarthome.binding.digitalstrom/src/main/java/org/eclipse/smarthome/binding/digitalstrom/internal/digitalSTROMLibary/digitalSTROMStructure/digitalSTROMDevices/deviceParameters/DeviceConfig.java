/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMStructure.digitalSTROMDevices.deviceParameters;

/**
 * The {@link DeviceConfig} saves device configurations.
 *
 * @author Alexander Betker
 * @since 1.3.0
 *
 * @author Michael Ochel - add missing java-doc
 * @author Matthias Siegele - add missing java-doc
 */
public interface DeviceConfig {

    /**
     * Returns the digitalSTROM-Device parameter class.
     *
     * @return configuration class
     */
    public int getClass_();

    /**
     * Returns the digitalSTROM-Device configuration index.
     *
     * @return configuration index
     */
    public int getIndex();

    /**
     * Returns the digitalSTROM-Device configuration value.
     *
     * @return configuration value
     */
    public int getValue();
}
