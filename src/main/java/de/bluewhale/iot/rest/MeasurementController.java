/*
 * Copyright (c) 2020 by Stefan Schubert under the MIT License (MIT).
 * See project LICENSE file for the detailed terms and conditions.
 */

package de.bluewhale.iot.rest;

import de.bluewhale.iot.adapter.DS18B20_Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "sensor/temp")
@Slf4j
/**
 * Offers API Endpoints to access measurements in a prometheus style.
 */
public class MeasurementController {

    @Autowired
    DS18B20_Device ds18B20Device;

    @RequestMapping(value = "/ds18b20", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> aquireSensorReadings() {

        String prometheusStyleResponse = ds18B20Device.getTemperatureMeasurementInPrometheusStyle();
        return new ResponseEntity<>(prometheusStyleResponse, HttpStatus.OK);
    }

}