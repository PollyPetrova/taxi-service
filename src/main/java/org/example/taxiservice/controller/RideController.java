package org.example.taxiservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.taxiservice.auth.JWTTokenProvider;
import org.example.taxiservice.entity.NearestRideRequest;
import org.example.taxiservice.entity.Ride;
import org.example.taxiservice.service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/ride")
public class RideController {

    @Autowired
    private RideService rideService;

    @Autowired
    private JWTTokenProvider jwtTokenProvider;

    @PostMapping("/request-nearest")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<String> requestNearestRide(@RequestBody NearestRideRequest nearestRideRequest, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        Long passengerId = jwtTokenProvider.getUserIdFromJWT(token);
        Long driverId = nearestRideRequest.getNearestDriverId();
        double destinationLatitude = nearestRideRequest.getDestinationLatitude();
        double destinationLongitude = nearestRideRequest.getDestinationLongitude();

        boolean success = rideService.requestNearestRide(passengerId, driverId, destinationLatitude, destinationLongitude);

        if (success) {
            return ResponseEntity.ok("Wait for driver answer");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Request failed");
        }
    }

    @PutMapping("/accept")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<String> acceptRide(@RequestParam Long rideId, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        Long driverId = jwtTokenProvider.getUserIdFromJWT(token);
        boolean success = rideService.acceptRide(rideId, driverId);
        if (success) {
            return ResponseEntity.ok("Ride accepted. Driver on the way.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to accept ride");
        }
    }

    @GetMapping("/{rideId}")
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER')")
    public ResponseEntity<Ride> getRideDetails(@PathVariable Long rideId, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        Long userId = jwtTokenProvider.getUserIdFromJWT(token);
        Optional<Ride> rideOpt = rideService.getRideByIdAndUser(rideId, userId);
        if (rideOpt.isPresent()) {
            return ResponseEntity.ok(rideOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
