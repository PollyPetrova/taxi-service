package org.example.taxiservice.service;

import org.example.taxiservice.entity.User;
import org.example.taxiservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getDriversByGeohashPrefix(String geohashPrefix) {
        return userRepository.findByGeohashStartsWithAndRolesContaining(geohashPrefix, User.Role.DRIVER);
    }

    private void validateUser(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new RuntimeException("Roles must be specified");
        }
    }
}