/*
 * Copyright (c) 2022 by Stefan Schubert under the MIT License (MIT).
 * See project LICENSE file for the detailed terms and conditions.
 */

package de.bluewhale.iot.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
/**
 * This class attempts to access a DS1B20 sensor, reads the temperature and provides it as prometheus gauge-style value.
 * Needs your rasperry pi and sensor up and running (see Readme.md) and the corresponding deviceID written in the
 * application.properties.
 */
public class DS18B20_Device {

    private static final String POSITIVE_CRC_FLAG = "YES";
    private static final String DEVICE_PATH = "/sys/bus/w1/devices";
    private static final String VALUE_READOUT_FILE = "w1_slave";

    @Value("${ds18b20.device.id}")
    String sensorDevice;

    public String getTemperatureMeasurementInPrometheusStyle() {

        Path path = FileSystems.getDefault().getPath(DEVICE_PATH, sensorDevice, VALUE_READOUT_FILE);

        double temperatureInCelcius = 0;
        long measureRequestElapsedTimeInMS;

        Instant start = Instant.now();
        temperatureInCelcius = getConreateTemperatueValueInCelsius();
        Instant measureTaken = Instant.now();
        measureRequestElapsedTimeInMS = Duration.between(start, measureTaken).toMillis();

        String response1 = String.format("# Temperature in celsius (in case of access errors celsius will be 0.0)\n" +
                "aqua_measure_celsius{sensor=\"%s\"} %s\n", sensorDevice, temperatureInCelcius);
        String response2 = String.format("# duration of measurement in millis\n" +
                "aqua_measure_duration{sensor=\"%s\"} %d\n", sensorDevice, measureRequestElapsedTimeInMS);

        return response1+response2;
    }

    /**
     * Reads the temperature
     * @return current temperature Reading in celsius or null if device was inaccessible
     */
    public Double getConreateTemperatueValueInCelsius() {

        List<String> lines;
        Path path = FileSystems.getDefault().getPath(DEVICE_PATH, sensorDevice, VALUE_READOUT_FILE);
        boolean selfcheckPassed = false;
        int circuitBreaker = 1;
        double temperatureInCelcius = 0;

        // Just in case the sensor wasn't ready for some reasons we make 4 repeated readout attempts.
        // A single readout of this sensor type usually lasts approximately a second.
        while (circuitBreaker <= 4) {
            try {
                lines = Files.readAllLines(path);
                for (String line : lines) {
                    if (line.endsWith(POSITIVE_CRC_FLAG))
                        selfcheckPassed = true;
                    else if (line.matches(".*t=[0-9-]+") && selfcheckPassed)
                        temperatureInCelcius = Integer.valueOf(line.substring(line.indexOf("=") + 1)) / 1000.0;
                }
                break;
            } catch (Exception e) {
                log.error("Sensor readout failed on " + circuitBreaker + ". attempt. Reason: {}",e.getCause());
                // sensor value will stick to 0 if we are not successful in the end.
            }
            circuitBreaker++;
        }

        log.debug(String.format("Temperature readout: %s °C having %d attempts.",
                temperatureInCelcius, circuitBreaker--));

        return temperatureInCelcius;
    }
}