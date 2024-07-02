package org.example.taxiservice.controller;

import org.example.taxiservice.entity.NearestRideRequest;
import org.example.taxiservice.service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ride")
public class RideController {

    @Autowired
    private RideService rideService;

    @PostMapping("/request-nearest")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<String> requestNearestRide(@RequestBody NearestRideRequest nearestRideRequest) {
        boolean success = rideService.requestNearestRide(nearestRideRequest.getPassengerId(), nearestRideRequest.getNearestDriverId());
        if (success) {
            return ResponseEntity.ok("Nearest ride requested successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to request nearest ride");
        }
    }
}
