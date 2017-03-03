/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl;

import java.util.Map.Entry;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.DeviceBinarayInputEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.impl.DeviceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link DeviceBinaryInput} contains all information of an device binary input, e.g. binary input type id (see
 * {@link DeviceBinarayInputEnum}, state and so on.
 *
 * @author Michael Ochel - initial contributer
 * @author Matthias Siegele - initial contributer
 *
 */
public class DeviceBinaryInput {

    private Logger logger = LoggerFactory.getLogger(DeviceBinaryInput.class);

    private Short targetGroupType = null;
    private Short targetGroup = null;
    private Short inputType = null;
    private Short inputId = null;
    private Short stateValue = null;

    /**
     * Creates a new {@link DeviceBinarayInputEnum} through the {@link JsonObject} of the binary inputs at json response
     * from digitalSTROM JSON-API or property-tree. Will be automatically added to a {@link DeviceImpl}, if binary
     * inputs exists.
     *
     * @param jsonObject must not be null
     */
    public DeviceBinaryInput(JsonObject jsonObject) {
        try {
            for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                try {
                    getClass().getDeclaredField(entry.getKey()).set(this, entry.getValue().getAsShort());
                } catch (NoSuchFieldException e) {
                    // ignore
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
            if (stateValue == null && jsonObject.get(JSONApiResponseKeysEnum.STATE.getKey()) != null) {
                stateValue = jsonObject.get(JSONApiResponseKeysEnum.STATE.getKey()).getAsShort();
            }
        } catch (IllegalArgumentException e) {
            logger.error("A IllegalArgumentException occurred: ", e);
        } catch (IllegalAccessException e) {
            logger.error("A IllegalAccessException occurred: ", e);
        } catch (SecurityException e) {
            logger.error("A SecurityException occurred: ", e);
        }
    }

    /**
     * Returns the current state of this {@link DeviceBinaryInput}.
     *
     * @return the state
     */
    public Short getState() {
        return stateValue;
    }

    /**
     * Sets the state of this {@link DeviceBinaryInput}.
     *
     * @param state the state to set
     */
    public void setState(Short state) {
        this.stateValue = state;
    }

    /**
     * Returns the target group type id of this {@link DeviceBinaryInput}.
     *
     * @return the targetGroupType
     */
    public Short getTargetGroupType() {
        return targetGroupType;
    }

    /**
     * Returns the target group id of this {@link DeviceBinaryInput}.
     *
     * @return the targetGroup
     */
    public Short getTargetGroup() {
        return targetGroup;
    }

    /**
     * Returns the input type id of this {@link DeviceBinaryInput}. Available input types see
     * {@link DeviceBinarayInputEnum}.
     *
     * @return the inputType
     */
    public Short getInputType() {
        return inputType;
    }

    /**
     * Returns the input id of this {@link DeviceBinaryInput}.
     *
     * @return the inputId
     */
    public Short getInputId() {
        return inputId;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((inputType == null) ? 0 : inputType.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DeviceBinaryInput)) {
            return false;
        }
        DeviceBinaryInput other = (DeviceBinaryInput) obj;
        if (inputType == null) {
            if (other.inputType != null) {
                return false;
            }
        } else if (!inputType.equals(other.inputType)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DeviceBinaryInput [targetGroupType=" + targetGroupType + ", targetGroup=" + targetGroup + ", inputType="
                + inputType + ", inputId=" + inputId + ", state=" + stateValue + "]";
    }

}
