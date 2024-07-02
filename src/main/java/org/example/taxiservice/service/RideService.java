package org.example.taxiservice.service;

import ch.hsr.geohash.GeoHash;
import org.example.taxiservice.entity.Driver;
import org.example.taxiservice.entity.Passenger;
import org.example.taxiservice.entity.Ride;
import org.example.taxiservice.entity.User;
import org.example.taxiservice.repository.RideRepository;
import org.example.taxiservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class RideService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RideRepository rideRepository;

    public boolean requestNearestRide(Long passengerId, Long nearestDriverId) {
        Optional<User> passengerOpt = userRepository.findById(passengerId);
        Optional<User> driverOpt = userRepository.findById(nearestDriverId);

        if (passengerOpt.isPresent() && passengerOpt.get() instanceof Passenger &&
                driverOpt.isPresent() && driverOpt.get() instanceof Driver) {

            return createRide(passengerOpt.get(), (Driver) driverOpt.get());
        }
        return false;
    }

    private boolean createRide(User passenger, Driver driver) {
        Ride ride = new Ride();
        ride.setPassenger(passenger);
        ride.setDriver(driver);
        ride.setStatus(Ride.Status.REQUESTED);
        ride.setRequestTime(LocalDateTime.now());

        rideRepository.save(ride);
        notifyDriver(driver, ride);

        return true;
    }

    private void notifyDriver(Driver driver, Ride ride) {
        System.out.println("Notify driver " + driver.getUsername() + " about new ride request: " + ride.getId());
    }
}
