/*
 * Copyright (c) 2022 by Stefan Schubert under the MIT License (MIT).
 * See project LICENSE file for the detailed terms and conditions.
 */

package de.bluewhale.iot.adapter.model;

import lombok.Data;

import java.io.Serializable;

/**
 * This one is used to submit measurement values through IoT devices
 * like this one https://github.com/StefanSchubert/aquarium_IoT#readme
 * The measurement type and tank will be derived from the API-Key.
 */
@Data
public class IoTMeasurementTo implements Serializable {

    private double measuredValueInCelsius;
    private String apiKey;

    public IoTMeasurementTo(final double measuredValueInCelsius, final String apiKey) {
        this.measuredValueInCelsius = measuredValueInCelsius;
        this.apiKey = apiKey;
    }
}
