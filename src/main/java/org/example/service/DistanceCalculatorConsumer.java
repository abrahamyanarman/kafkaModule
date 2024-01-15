package org.example.service;

import org.example.dto.VehicleSignal;

public interface DistanceCalculatorConsumer {
    void listen(VehicleSignal signal);

}
