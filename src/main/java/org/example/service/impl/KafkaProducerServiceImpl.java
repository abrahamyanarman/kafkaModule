package org.example.service.impl;

import org.example.dto.VehicleSignal;
import org.example.service.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerServiceImpl implements KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, VehicleSignal> kafkaTemplate;

    public void sendSignal(VehicleSignal signal) {
        kafkaTemplate.send("input", signal);
    }
}
