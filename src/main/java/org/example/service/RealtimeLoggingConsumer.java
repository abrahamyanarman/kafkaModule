package org.example.service;

import org.example.dto.DistanceResult;

public interface RealtimeLoggingConsumer {
    void listen(DistanceResult distanceResult);
}
