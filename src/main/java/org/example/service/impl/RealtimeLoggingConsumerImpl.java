package org.example.service.impl;

import org.example.dto.DistanceResult;
import org.example.service.RealtimeLoggingConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class RealtimeLoggingConsumerImpl implements RealtimeLoggingConsumer {
    private static final Logger logger = LoggerFactory.getLogger(RealtimeLoggingConsumerImpl.class);

    @Override
    @KafkaListener(topics = "output", groupId = "distanceLogging")
    public void listen(DistanceResult distanceResult) {
        logger.info("Vehicle {} traveled {} units.", distanceResult.getVehicleId(), distanceResult.getDistance());
    }
}
