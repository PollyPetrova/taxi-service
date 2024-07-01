package org.example.taxiservice.controller;

import org.example.taxiservice.auth.JWTTokenProvider;
import org.example.taxiservice.entity.User;
import org.example.taxiservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        User createdUser = userService.save(user);
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        Optional<User> byUsername = userService.getUserByUsername(user.getUsername());
        if (byUsername.isPresent() && new BCryptPasswordEncoder().matches(user.getPassword(), byUsername.get().getPassword())) {
            String token = jwtTokenProvider.generateToken(byUsername.get().getId(), byUsername.get().getUsername(), byUsername.get().getRoles());
            return ResponseEntity.ok(token);
        }
        throw new RuntimeException("Invalid username or password");
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        User updatedUser = userService.update(user);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<User>> getNearbyDrivers(@RequestParam double latitude, @RequestParam double longitude, @RequestParam double radiusKm) {
        List<User> drivers = userService.findNearestDrivers(latitude, longitude, radiusKm);
        return ResponseEntity.ok(drivers);
    }
}
