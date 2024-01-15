package org.example.service;

import org.example.dto.VehicleSignal;

public interface KafkaProducerService {
    public void sendSignal(VehicleSignal signal);
}
