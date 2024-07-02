package org.example.taxiservice.entity;

import lombok.Data;

@Data
public class NearestRideRequest {
    private Long passengerId;
    private Long nearestDriverId;
}
