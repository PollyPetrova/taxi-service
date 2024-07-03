package org.example.taxiservice.service;

import ch.hsr.geohash.GeoHash;
import org.example.taxiservice.entity.Driver;
import org.example.taxiservice.entity.Passenger;
import org.example.taxiservice.entity.User;
import org.example.taxiservice.repository.DriverRepository;
import org.example.taxiservice.repository.UserRepository;
import org.example.taxiservice.utils.GeoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    public User save(User user) {
        validateUser(user);
        return userRepository.save(user);
    }

    public User update(User user) {
        validateUser(user);
        return userRepository.save(user);
    }

    public void updateUserFields(User existingUser, Map<String, Object> updates) {
        if (updates.containsKey("username")) {
            existingUser.setUsername((String) updates.get("username"));
        }
        if (updates.containsKey("password")) {
            String rawPassword = (String) updates.get("password");
            if (rawPassword != null && !rawPassword.isEmpty()) {
                existingUser.setPassword(new BCryptPasswordEncoder().encode(rawPassword));
            }
        }
        if (updates.containsKey("latitude")) {
            existingUser.setLatitude((Double) updates.get("latitude"));
        }
        if (updates.containsKey("longitude")) {
            existingUser.setLongitude((Double) updates.get("longitude"));
        }
        if (updates.containsKey("roles")) {
            List<String> roles = (List<String>) updates.get("roles");
            existingUser.setRoles(convertRoles(roles));
        }

        if (existingUser instanceof Driver) {
            Driver existingDriver = (Driver) existingUser;
            if (updates.containsKey("vehicleDetails")) {
                existingDriver.setVehicleDetails((String) updates.get("vehicleDetails"));
            }
            if (updates.containsKey("vehicleColor")) {
                existingDriver.setVehicleColor((String) updates.get("vehicleColor"));
            }
            if (updates.containsKey("driverRating")) {
                existingDriver.setDriverRating((Double) updates.get("driverRating"));
            }
        }

        if (existingUser instanceof Passenger) {
            Passenger existingPassenger = (Passenger) existingUser;
            if (updates.containsKey("paymentMethod")) {
                existingPassenger.setPaymentMethod((String) updates.get("paymentMethod"));
            }
        }
    }

    private Set<User.Role> convertRoles(List<String> roles) {
        Set<User.Role> roleSet = new HashSet<>();
        for (String roleName : roles) {
            roleSet.add(User.Role.valueOf(roleName));
        }
        return roleSet;
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    public List<Driver> findNearestDrivers(double latitude, double longitude, double radiusKm) {
        double[] boundingBox = GeoUtils.getBoundingBox(latitude, longitude, radiusKm);

        List<Driver> drivers = driverRepository.findByLatitudeBetweenAndLongitudeBetweenAndRolesEquals(
                boundingBox[0], boundingBox[1],
                boundingBox[2], boundingBox[3],
                User.Role.DRIVER);

        return drivers.stream()
                .filter(driver -> GeoUtils.distance(latitude, longitude, driver.getLatitude(), driver.getLongitude()) <= radiusKm)
                .collect(Collectors.toList());
    }

    private void validateUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
    }
}