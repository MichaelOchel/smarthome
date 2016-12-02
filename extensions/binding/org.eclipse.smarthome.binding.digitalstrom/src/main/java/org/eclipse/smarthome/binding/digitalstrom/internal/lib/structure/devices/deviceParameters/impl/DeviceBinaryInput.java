package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl;

import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DeviceBinaryInput {

    private Short targetGroupType = null;
    private Short targetGroup = null;
    private Short inputType = null;
    private Short inputId = null;
    private Short stateValue = null;

    // TODO: exeption in logger
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
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @return the state
     */
    public Short getState() {
        return stateValue;
    }

    /**
     * @param state the state to set
     */
    public void setState(Short state) {
        this.stateValue = state;
    }

    /**
     * @return the targetGroupType
     */
    public Short getTargetGroupType() {
        return targetGroupType;
    }

    /**
     * @return the targetGroup
     */
    public Short getTargetGroup() {
        return targetGroup;
    }

    /**
     * @return the inputType
     */
    public Short getInputType() {
        return inputType;
    }

    /**
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
