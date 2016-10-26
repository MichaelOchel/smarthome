package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.BaseSensorValues;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class WeatherSensorData extends BaseSensorValues {

    private Logger logger = LoggerFactory.getLogger(WeatherSensorData.class);

    private String weatherIconId = null;
    private String weatherConditionId = null;
    private String weatherServiceId = null;
    private String weatherServiceTime = null;

    public WeatherSensorData(JsonObject jObject) {
        super.addSensorValue(jObject, true);
        if (jObject.get(JSONApiResponseKeysEnum.WEATHER_ICON_ID.getKey()) != null) {
            weatherIconId = jObject.get(JSONApiResponseKeysEnum.WEATHER_ICON_ID.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.WEATHER_CONDITION_ID.getKey()) != null) {
            weatherConditionId = jObject.get(JSONApiResponseKeysEnum.WEATHER_CONDITION_ID.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.WEATHER_SERVICE_ID.getKey()) != null) {
            weatherServiceId = jObject.get(JSONApiResponseKeysEnum.WEATHER_SERVICE_ID.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.WEATHER_SERVICE_TIME.getKey()) != null) {
            weatherServiceTime = jObject.get(JSONApiResponseKeysEnum.WEATHER_SERVICE_TIME.getKey()).getAsString();
        }
    }

    /**
     * @return the weatherIconId
     */
    public String getWeatherIconId() {
        return weatherIconId;
    }

    /**
     * @return the weatherConditionId
     */
    public String getWeatherConditionId() {
        return weatherConditionId;
    }

    /**
     * @return the weatherServiceId
     */
    public String getWeatherServiceId() {
        return weatherServiceId;
    }

    /**
     * @return the weatherServiceTime as {@link String}
     */
    public String getWeatherServiceTimeAsSting() {
        return weatherServiceTime;
    }

    /**
     * @return the weatherServiceTime as {@link Date}
     */
    public Date getWeatherServiceTimeAsDate() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SS");
        try {
            return formatter.parse(weatherServiceTime);
        } catch (ParseException e) {
            logger.error("A ParseException occurred by parsing date string: " + weatherServiceTime, e);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "WeatherSensorData [weatherIconId=" + weatherIconId + ", weatherConditionId=" + weatherConditionId
                + ", weatherServiceId=" + weatherServiceId + ", weatherServiceTime=" + weatherServiceTime + ", "
                + super.toString() + "]";
    }
}
