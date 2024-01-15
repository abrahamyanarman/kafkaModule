package org.example.service.impl;

import org.example.dto.Coordinates;
import org.example.dto.DistanceResult;
import org.example.dto.VehicleSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DistanceCalculatorConsumerImpl {
    private static Logger LOGGER = LoggerFactory.getLogger(DistanceCalculatorConsumerImpl.class);
    private final Map<String, List<Coordinates>> vehicleCoordinatesMap = new ConcurrentHashMap<>();

    @Autowired
    private KafkaTemplate<String, DistanceResult> kafkaTemplate;

    @Value("${distance.threshold}")
    private double distanceThreshold;

    @KafkaListener(topics = "input", groupId = "distanceCalculating")
    public void listen(VehicleSignal signal) {
        LOGGER.info("received signal with id: " + signal.getVehicleId());
        String vehicleId = signal.getVehicleId();
        Coordinates currentCoordinates = new Coordinates(signal.getLatitude(), signal.getLongitude());

        vehicleCoordinatesMap.computeIfAbsent(vehicleId, k -> new ArrayList<>()).add(currentCoordinates);

        double distance = calculateDistance(vehicleCoordinatesMap.get(vehicleId));

        if (distance > distanceThreshold) {
            DistanceResult distanceResult = new DistanceResult(vehicleId, distance);
            LOGGER.info("Sending distance: " + distance + "to output");
            kafkaTemplate.send("output", distanceResult);
        }
    }


    /**
     * Method uses the Haversine formula to calculate the distance between a series of coordinates
     *
     * @param coordinatesList coordinates representing the path
     * @return double
     */
    private double calculateDistance(List<Coordinates> coordinatesList) {
        if (coordinatesList.size() < 2) {
            return 0.0; // Not enough coordinates to calculate distance
        }

        final int earthRadiusKm = 6371; // Earth radius in kilometers (change to 3959 for miles)

        Coordinates prevCoordinates = coordinatesList.get(0);
        double totalDistance = 0.0;

        for (int i = 1; i < coordinatesList.size(); i++) {
            Coordinates currentCoordinates = coordinatesList.get(i);
            double lat1 = Math.toRadians(prevCoordinates.getLatitude());
            double lon1 = Math.toRadians(prevCoordinates.getLongitude());
            double lat2 = Math.toRadians(currentCoordinates.getLatitude());
            double lon2 = Math.toRadians(currentCoordinates.getLongitude());

            double dLat = lat2 - lat1;
            double dLon = lon2 - lon1;

            double chordLength = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(lat1) * Math.cos(lat2) *
                            Math.sin(dLon / 2) * Math.sin(dLon / 2);

            double centralAngle = 2 * Math.atan2(Math.sqrt(chordLength), Math.sqrt(1 - chordLength));

            double distance = earthRadiusKm * centralAngle;
            totalDistance += distance;

            prevCoordinates = currentCoordinates;
        }
        return totalDistance;
    }
}
