package org.example.taxiservice.service;

import ch.hsr.geohash.GeoHash;
import org.example.taxiservice.entity.Driver;
import org.example.taxiservice.entity.User;
import org.example.taxiservice.repository.DriverRepository;
import org.example.taxiservice.repository.UserRepository;
import org.example.taxiservice.utils.GeoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    private static final double EARTH_RADIUS = 6371.0;

    public User save(User user) {
        validateUser(user);
        return userRepository.save(user);
    }

    public User update(User user) {
        validateUser(user);
        Optional<User> existingUser = userRepository.findById(user.getId());
        if (existingUser.isPresent()) {
            User userToUpdate = existingUser.get();
            userToUpdate.setUsername(user.getUsername());
            userToUpdate.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
            userToUpdate.setLatitude(user.getLatitude());
            userToUpdate.setLongitude(user.getLongitude());
            userToUpdate.setRoles(user.getRoles());
            userToUpdate.calculateGeohash();
            return userRepository.save(userToUpdate);
        } else {
            throw new RuntimeException("User not found");
        }
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

    public List<User> getUsersByGeohashPrefix(String geohashPrefix, User.Role role) {
        return userRepository.findByGeohashStartsWithAndRolesContaining(geohashPrefix, role);
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