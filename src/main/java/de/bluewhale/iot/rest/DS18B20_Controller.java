/*
 * Copyright (c) 2020 by Stefan Schubert under the MIT License (MIT).
 * See project LICENSE file for the detailed terms and conditions.
 */

package de.bluewhale.iot.rest;

import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "sensor/temp")
@Log
public class DS18B20_Controller {

    @RequestMapping(value = {"/ds18b20"}, method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> stateDependedAnswer() {

        // TODO STS (27.10.20):
        String returnText = "Implement me :-)";

        // Just received a ping request - NOP
        return new ResponseEntity<>(returnText, HttpStatus.OK);
    }

}