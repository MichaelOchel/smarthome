package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DSID;

public interface Circuit {

    String name = null;
    DSID dSID = null;
    Integer hwVersion = null;
    Integer armSwVersion = null;
    Integer dspSwVersion = null;
    Integer apiVersion = null;
    String hwName = null;
    Boolean isPresent = null;
    Boolean isValid = null;

    // TODO: complete circuit interface

}
