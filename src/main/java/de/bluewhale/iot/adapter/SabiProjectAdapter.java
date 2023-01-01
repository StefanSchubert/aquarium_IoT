/*
 * Copyright (c) 2022 by Stefan Schubert under the MIT License (MIT).
 * See project LICENSE file for the detailed terms and conditions.
 */

package de.bluewhale.iot.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bluewhale.iot.adapter.model.IoTMeasurementTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
/**
 * This Adapter talks to sabi-project.net ( see https://github.com/StefanSchubert/sabi#readme )
 * by using the client API introduced through https://github.com/StefanSchubert/sabi/issues/75
 * Meaning it will report measurements (currently only temperature) to sabi.
 */
public class SabiProjectAdapter {

    @Autowired
    DS18B20_Device temperatureSensor;

    @Autowired
    ObjectMapper objectMapper;  // json mapper

    @Value("${sabi.support.enabled}")
    Boolean activatesSabiSupport;

    @Value("${sabi.tank.temperature.api-key}")
    String sabiTemperatureApiKey;

    @Value("${sabi.tank.temperature.reportrate}")
    String reportRateInHours;

    @Value("${sabi.temperature.api.endpoint}")
    String sabiTemperatureRestEndpointURI;


    @Scheduled(fixedRateString = "${sabi.tank.temperature.reportrate}", timeUnit = TimeUnit.HOURS)
    public void reportTemperature() {

        if (activatesSabiSupport.equals(Boolean.FALSE)) return; // NOP if user hasn't activated it.

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHttpHeaders();

        Double temperatueValue = temperatureSensor.getConreateTemperatueValueInCelsius();
        if (temperatueValue == null) {
            log.error("Scheduling Task couldn't access sensor device! Nothing to report");
            return;
        }

        IoTMeasurementTo ioTMeasurementTo = new IoTMeasurementTo(temperatueValue, sabiTemperatureApiKey);
        String jsonPayload = null;
        try {
            jsonPayload = objectMapper.writeValueAsString(ioTMeasurementTo);
        } catch (JsonProcessingException e) {
            log.error("Could not build TO! Reported nothing.", e);
            return;
        }

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(sabiTemperatureRestEndpointURI, requestEntity, String.class);

        if (responseEntity != null && !responseEntity.getStatusCode().is2xxSuccessful()) {
            log.error("Couldn't reach SABIs backend! " + (requestEntity == null ? "Empty Response!" : responseEntity.getStatusCode().toString()));
        }

    }

    private static HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("user-agent", "Aquarium IoT Client");
        return headers;
    }

}