/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.dataTypes.CachedSensorValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.CachedMeteringValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.constants.MeteringUnitsEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link JSONCachedMeteringValueImpl} is the implementation of the {@link CachedMeteringValue}.
 *
 * @author Alexander Betker - Initial contribution
 * @author Michael Ochel - change from SimpleJSON to GSON, add getDateAsDate()
 * @author Matthias Siegele - change from SimpleJSON to GSON, add getDateAsDate()
 */
public class JSONCachedMeteringValueImpl implements CachedMeteringValue {

    private DSID dsid = null;
    private double value = 0;
    private String date = null;
    private MeteringTypeEnum meteringType = null;
    private MeteringUnitsEnum meteringUnit = null;
    private Logger logger = LoggerFactory.getLogger(CachedSensorValue.class);

    /**
     * Creates a new {@link JSONCachedMeteringValueImpl}.
     *
     * @param jObject
     * @param meteringType
     * @param meteringUnit
     */
    public JSONCachedMeteringValueImpl(JsonObject jObject, MeteringTypeEnum meteringType,
            MeteringUnitsEnum meteringUnit) {
        this.meteringType = meteringType;
        if (meteringUnit != null) {
            this.meteringUnit = meteringUnit;
        } else {
            this.meteringUnit = MeteringUnitsEnum.WH;
        }
        if (jObject.get(JSONApiResponseKeysEnum.DSID_LOWER_CASE.getKey()) != null) {
            this.dsid = new DSID(jObject.get(JSONApiResponseKeysEnum.DSID_LOWER_CASE.getKey()).getAsString());
        }
        if (jObject.get(JSONApiResponseKeysEnum.VALUE.getKey()) != null) {
            this.value = jObject.get(JSONApiResponseKeysEnum.VALUE.getKey()).getAsDouble();
        }
        if (jObject.get(JSONApiResponseKeysEnum.DATE.getKey()) != null) {
            this.date = jObject.get(JSONApiResponseKeysEnum.DATE.getKey()).getAsString();
        }
    }

    @Override
    public DSID getDsid() {
        return dsid;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public String getDate() {
        return date;
    }

    @Override
    public Date getDateAsDate() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            logger.error("A ParseException occurred by parsing date string: " + date, e);
        }
        return null;
    }

    @Override
    public MeteringTypeEnum getMeteringType() {
        return meteringType;
    }

    @Override
    public MeteringUnitsEnum getMeteringUnit() {
        return meteringUnit;
    }

    @Override
    public String toString() {
        return "dSID: " + this.getDsid() + ", metering-type " + meteringType.toString() + ", metering-unit "
                + meteringUnit + ", date: " + this.getDate() + ", value: " + this.getValue();
    }
}
