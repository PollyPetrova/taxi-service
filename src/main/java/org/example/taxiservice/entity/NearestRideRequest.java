package org.example.taxiservice.entity;

import lombok.Data;

@Data
public class NearestRideRequest {
    private Long nearestDriverId;
    private double destinationLatitude;
    private double destinationLongitude;
}
