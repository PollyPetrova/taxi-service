package org.example.taxiservice.service;

import ch.hsr.geohash.GeoHash;
import jakarta.transaction.Transactional;
import org.example.taxiservice.entity.Driver;
import org.example.taxiservice.entity.Passenger;
import org.example.taxiservice.entity.Ride;
import org.example.taxiservice.entity.User;
import org.example.taxiservice.repository.RideRepository;
import org.example.taxiservice.repository.UserRepository;
import org.example.taxiservice.utils.GeoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class RideService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RideRepository rideRepository;

    public boolean requestNearestRide(Long passengerId, Long nearestDriverId, double destinationLatitude, double destinationLongitude) {
        Optional<User> passengerOpt = userRepository.findById(passengerId);
        Optional<User> driverOpt = userRepository.findById(nearestDriverId);

        if (passengerOpt.isPresent() && passengerOpt.get() instanceof Passenger &&
                driverOpt.isPresent() && driverOpt.get() instanceof Driver) {

            return createRide(passengerOpt.get(), (Driver) driverOpt.get(), destinationLatitude, destinationLongitude);
        }
        return false;
    }

    private boolean createRide(User passenger, Driver driver, double destinationLatitude, double destinationLongitude) {
        Ride ride = new Ride();
        ride.setPassenger(passenger);
        ride.setDriver(driver);
        ride.setStatus(Ride.Status.REQUESTED);
        ride.setRequestTime(LocalDateTime.now());
        ride.setDestinationLatitude(destinationLatitude);
        ride.setDestinationLongitude(destinationLongitude);

        rideRepository.save(ride);
        return true;
    }

    public boolean acceptRide(Long rideId, Long driverId) {
        Optional<Ride> rideOpt = rideRepository.findById(rideId);
        if (rideOpt.isPresent()) {
            Ride ride = rideOpt.get();
            if (ride.getDriver().getId().equals(driverId)) {
                ride.setStatus(Ride.Status.ACCEPTED);

                // Рассчитываем и устанавливаем время прибытия
                double distanceKm = GeoUtils.distance(ride.getDriver().getLatitude(), ride.getDriver().getLongitude(),
                        ride.getPassenger().getLatitude(), ride.getPassenger().getLongitude());
                double averageSpeedKmPerHour = 60;
                double travelTimeHours = distanceKm / averageSpeedKmPerHour;
                LocalDateTime arrivalTime = LocalDateTime.now().plusHours((long) travelTimeHours);
                ride.setArrivalTime(arrivalTime);

                rideRepository.save(ride);
                return true;
            }
        }
        return false;
    }

    public Optional<Ride> getRideByIdAndUser(Long rideId, Long userId) {
        return rideRepository.findById(rideId)
                .filter(ride -> ride.getPassenger().getId().equals(userId) || ride.getDriver().getId().equals(userId));
    }

}
