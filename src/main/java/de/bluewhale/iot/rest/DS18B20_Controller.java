/*
 * Copyright (c) 2020 by Stefan Schubert under the MIT License (MIT).
 * See project LICENSE file for the detailed terms and conditions.
 */

package de.bluewhale.iot.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping(value = "sensor/temp")
@Slf4j
/**
 * This controller attempts to access a DS1B20 sensor, reads the temperature and provides it as prometheus gauge-style value.
 * Needs your rasperry pi and sensor up and running (see Readme.md) and the corresponding deviceID written in the
 * application.properties.
 */
public class DS18B20_Controller {

    private static final String POSITIVE_CRC_FLAG = "YES";
    private static final String DEVICE_PATH = "/sys/bus/w1/devices";
    private static final String VALUE_READOUT_FILE = "w1_slave";

    @Value("${ds18b20.device.id}")
    String sensorDevice;

    @RequestMapping(value = "/ds18b20", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> howWarmIsIt() {

        Path path = FileSystems.getDefault().getPath(DEVICE_PATH, sensorDevice, VALUE_READOUT_FILE);

        double temperatureInCelcius = 0;
        long measureRequestElapsedTimeInMS;
        List<String> lines;
        int circuitBreaker = 1;
        boolean selfcheckPassed = false;

        // Just in case the sensor wasn't ready for some reasons we make 4 repeated readout attempts.
        // A single readout on this sensor type lasts usally <2secs.
        Instant start = Instant.now();
        while (circuitBreaker <= 4) {
            try {
                lines = Files.readAllLines(path);
                for (String line : lines) {
                    if (line.endsWith(POSITIVE_CRC_FLAG))
                        selfcheckPassed = true;
                    else if (line.matches(".*t=[0-9-]+") && selfcheckPassed)
                        temperatureInCelcius = Integer.valueOf(line.substring(line.indexOf("=") + 1)) / 1000.0;
                }
            } catch (Exception e) {
                log.error("Sensor readout failed on " + circuitBreaker + ". attempt. Reason: {}",e.getCause());
                // sensor value will stick to 0 if we are not successful in the end.
            }
            circuitBreaker++;
        }
        Instant measureTaken = Instant.now();
        measureRequestElapsedTimeInMS = Duration.between(start, measureTaken).toMillis();

        log.info(String.format("Temperature readout: %s °C in %d ms having %d attempts.",
                temperatureInCelcius, measureRequestElapsedTimeInMS, circuitBreaker--));

        String response1 = String.format("# Temperature in celsius and duration in nanos in case of access errors celsius will be exact 0,000000\n" +
                "aqua_measure_celsius{sensor=\"%s\"} %f %d\n", sensorDevice, temperatureInCelcius, measureTaken.getNano());
        String response2 = String.format("# duration of measurement in millis\n" +
                "aqua_measure_duration{sensor=\"%s\"} %d\n", sensorDevice, measureRequestElapsedTimeInMS);

        return new ResponseEntity<>(response1+response2, HttpStatus.OK);
    }

}