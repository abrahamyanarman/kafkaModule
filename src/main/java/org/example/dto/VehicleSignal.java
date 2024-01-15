package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class VehicleSignal {

    @NotBlank(message = "vehicleId is mandatory")
    private String vehicleId;
    @NotNull(message = "latitude is mandatory")
    private Float latitude;
    @NotNull(message = "longitude is mandatory")
    private Float longitude;

    public VehicleSignal() {
    }

    public VehicleSignal(String vehicleId, float latitude, float longitude) {
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

}
