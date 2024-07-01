package org.example.taxiservice.service;

import ch.hsr.geohash.GeoHash;
import org.example.taxiservice.entity.User;
import org.example.taxiservice.repository.UserRepository;
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

    private static final double EARTH_RADIUS = 6371.0; // Радиус Земли в километрах

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

    public List<User> findNearestDrivers(double latitude, double longitude, double radiusKm) {
        String geohashPrefix = GeoHash.geoHashStringWithCharacterPrecision(latitude, longitude, 7);
        List<User> users = userRepository.findByGeohashStartsWithAndRolesContaining(geohashPrefix, User.Role.DRIVER);

        return users.stream()
                .filter(user -> distance(latitude, longitude, user.getLatitude(), user.getLongitude()) <= radiusKm)
                .collect(Collectors.toList());
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula to calculate distance between two points specified by latitude and longitude
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    private void validateUser(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new RuntimeException("Roles must be specified");
        }
    }
}