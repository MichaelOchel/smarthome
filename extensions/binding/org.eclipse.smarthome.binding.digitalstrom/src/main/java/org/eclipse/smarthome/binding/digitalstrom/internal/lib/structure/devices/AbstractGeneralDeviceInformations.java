package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.ChangeableDeviceConfigEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl.DSID;

import com.google.gson.JsonObject;

public abstract class AbstractGeneralDeviceInformations implements GeneralDeviceInformations {

    protected DSID dsid = null;
    protected String dSUID = null;
    protected Boolean isPresent = null;
    protected Boolean isValide = null;
    protected String name = null;
    protected String displayID = null;
    protected DeviceStatusListener listener = null;

    public AbstractGeneralDeviceInformations(JsonObject jsonDeviceObject) {
        if (jsonDeviceObject.get(JSONApiResponseKeysEnum.NAME.getKey()) != null) {
            name = jsonDeviceObject.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
        }
        if (jsonDeviceObject.get(JSONApiResponseKeysEnum.ID.getKey()) != null) {
            dsid = new DSID(jsonDeviceObject.get(JSONApiResponseKeysEnum.ID.getKey()).getAsString());
        } else if (jsonDeviceObject.get(JSONApiResponseKeysEnum.DSID.getKey()) != null) {
            dsid = new DSID(jsonDeviceObject.get(JSONApiResponseKeysEnum.DSID.getKey()).getAsString());
        } else if (jsonDeviceObject.get(JSONApiResponseKeysEnum.DSID_LOWER_CASE.getKey()) != null) {
            dsid = new DSID(jsonDeviceObject.get(JSONApiResponseKeysEnum.DSID_LOWER_CASE.getKey()).getAsString());
        }
        if (jsonDeviceObject.get(JSONApiResponseKeysEnum.DSUID.getKey()) != null) {
            dSUID = jsonDeviceObject.get(JSONApiResponseKeysEnum.DSUID.getKey()).getAsString();
        }
        if (jsonDeviceObject.get(JSONApiResponseKeysEnum.DISPLAY_ID.getKey()) != null) {
            displayID = jsonDeviceObject.get(JSONApiResponseKeysEnum.DISPLAY_ID.getKey()).getAsString();
        }
        if (jsonDeviceObject.get(JSONApiResponseKeysEnum.IS_PRESENT.getKey()) != null) {
            isPresent = jsonDeviceObject.get(JSONApiResponseKeysEnum.IS_PRESENT.getKey()).getAsBoolean();
        } else if (jsonDeviceObject.get(JSONApiResponseKeysEnum.PRESENT.getKey()) != null) {
            isPresent = jsonDeviceObject.get(JSONApiResponseKeysEnum.PRESENT.getKey()).getAsBoolean();
        }
        if (jsonDeviceObject.get(JSONApiResponseKeysEnum.IS_VALID.getKey()) != null) {
            isValide = jsonDeviceObject.get(JSONApiResponseKeysEnum.IS_VALID.getKey()).getAsBoolean();
        }
    }

    @Override
    public synchronized String getName() {
        return this.name;
    }

    @Override
    public synchronized void setName(String name) {
        this.name = name;
        if (listener != null) {
            listener.onDeviceConfigChanged(ChangeableDeviceConfigEnum.DEVICE_NAME);
        }
    }

    @Override
    public DSID getDSID() {
        return dsid;
    }

    @Override
    public String getDSUID() {
        return this.dSUID;
    }

    @Override
    public Boolean isPresent() {
        return isPresent;
    }

    @Override
    public void setIsPresent(boolean isPresent) {
        this.isPresent = isPresent;
        if (listener != null) {
            if (!isPresent) {
                listener.onDeviceRemoved(this);
            } else {
                listener.onDeviceAdded(this);
            }
        }
    }

    @Override
    public Boolean isValide() {
        return isValide;
    }

    @Override
    public void setIsValide(boolean isValide) {
        this.isValide = isValide;
    }

    @Override
    public void registerDeviceStatusListener(DeviceStatusListener listener) {
        if (listener != null) {
            this.listener = listener;
            listener.onDeviceAdded(this);
        }
    }

    @Override
    public DeviceStatusListener unregisterDeviceStatusListener() {
        DeviceStatusListener listener = this.listener;
        this.listener = null;
        return listener;
    }

    @Override
    public boolean isListenerRegisterd() {
        return listener != null;
    }

    @Override
    public DeviceStatusListener getDeviceStatusListener() {
        return listener;
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
        result = prime * result + ((dSUID == null) ? 0 : dSUID.hashCode());
        result = prime * result + ((displayID == null) ? 0 : displayID.hashCode());
        result = prime * result + ((dsid == null) ? 0 : dsid.hashCode());
        result = prime * result + ((isPresent == null) ? 0 : isPresent.hashCode());
        result = prime * result + ((isValide == null) ? 0 : isValide.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        if (!(obj instanceof AbstractGeneralDeviceInformations)) {
            return false;
        }
        AbstractGeneralDeviceInformations other = (AbstractGeneralDeviceInformations) obj;
        if (dSUID == null) {
            if (other.dSUID != null) {
                return false;
            }
        } else if (!dSUID.equals(other.dSUID)) {
            return false;
        }
        if (displayID == null) {
            if (other.displayID != null) {
                return false;
            }
        } else if (!displayID.equals(other.displayID)) {
            return false;
        }
        if (dsid == null) {
            if (other.dsid != null) {
                return false;
            }
        } else if (!dsid.equals(other.dsid)) {
            return false;
        }
        if (isPresent == null) {
            if (other.isPresent != null) {
                return false;
            }
        } else if (!isPresent.equals(other.isPresent)) {
            return false;
        }
        if (isValide == null) {
            if (other.isValide != null) {
                return false;
            }
        } else if (!isValide.equals(other.isValide)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String getDisplayID() {
        return displayID;
    }
}
