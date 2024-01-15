package org.example.dto;

public class DistanceResult {
    private String vehicleId;
    private double distance;

    public DistanceResult() {
    }

    public DistanceResult(String vehicleId, double distance) {
        this.vehicleId = vehicleId;
        this.distance = distance;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
