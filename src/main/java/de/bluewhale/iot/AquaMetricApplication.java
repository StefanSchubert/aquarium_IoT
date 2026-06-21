/*
 * Copyright (c) 2020 by Stefan Schubert under the MIT License (MIT).
 * See project LICENSE file for the detailed terms and conditions.
 */
package de.bluewhale.iot;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AquaMetricApplication {

    public static void main(String[] args) {
        SpringApplication.run(AquaMetricApplication.class, args);
    }

    @Autowired(required = false)
    private org.springframework.boot.info.BuildProperties buildProperties;

    @PostConstruct
    public void check() {
        System.out.println("BuildProperties bean = " + buildProperties);
    }

}
