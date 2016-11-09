package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.ChangeableDeviceConfigEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DSID;

import com.google.gson.JsonObject;

public abstract class AbstractGeneralDeviceInformations implements GeneralDeviceInformations {

    protected DSID dsid = null;
    protected String dSUID = null;
    protected boolean isPresent = false;
    protected boolean isValide = false;
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
        if (jsonDeviceObject.get(JSONApiResponseKeysEnum.IS_VALIDE.getKey()) != null) {
            isValide = jsonDeviceObject.get(JSONApiResponseKeysEnum.IS_VALIDE.getKey()).getAsBoolean();
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
    public synchronized boolean isPresent() {
        return isPresent;
    }

    @Override
    public synchronized void setIsPresent(boolean isPresent) {
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
    public boolean isValide() {
        return isValide;
    }

    @Override
    public void setIsValide(boolean isValide) {
        this.isValide = isValide;
    }

    @Override
    public void registerDeviceStateListener(DeviceStatusListener listener) {
        if (listener != null) {
            this.listener = listener;
            listener.onDeviceAdded(this);
        }
    }

    @Override
    public DeviceStatusListener unregisterDeviceStateListener() {
        DeviceStatusListener listener = this.listener;
        this.listener = null;
        return listener;
    }

    @Override
    public boolean isListenerRegisterd() {
        return (listener != null);
    }

    @Override
    public String getDisplayID() {
        return displayID;
    }
}
