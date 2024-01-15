package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.VehicleSignal;
import org.example.service.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/signals")
public class SignalController {

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> receiveSignal(@RequestBody @Valid VehicleSignal signal) {
        kafkaProducerService.sendSignal(signal);
        return ResponseEntity.ok("Signal received and sent to Kafka");
    }
}