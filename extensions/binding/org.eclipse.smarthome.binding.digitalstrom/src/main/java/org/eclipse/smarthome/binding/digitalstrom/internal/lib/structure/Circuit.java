package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DSID;

public interface Circuit {

    String name = null;
    String dSUID = null;
    DSID dSID = null;
    String DisplayID = null;
    Integer hwVersion = null;
    String hwVersionString = null;
    String swVersion = null;
    Integer armSwVersion = null;
    Integer dspSwVersion = null;
    Integer apiVersion = null;
    String hwName = null;
    Boolean isPresent = null;
    Boolean isValid = null;
    Integer busMemberType = null;
    Boolean hasDevices = null;
    Boolean hasMetering = null;
    String VdcConfigURL = null;
    String VdcModelUID = null;
    String VdcHardwareGuid = null;
    String VdcHardwareModelGuid = null;
    String VdcVendorGuid = null;
    String VdcOemGuid = null;
    Boolean ignoreActionsFromNewDevices = null;

    // TODO: complete circuit interface

}
