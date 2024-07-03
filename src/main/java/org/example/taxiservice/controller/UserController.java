package org.example.taxiservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.taxiservice.auth.JWTTokenProvider;
import org.example.taxiservice.entity.Driver;
import org.example.taxiservice.entity.Passenger;
import org.example.taxiservice.entity.User;
import org.example.taxiservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTTokenProvider jwtTokenProvider;

    @PostMapping("/register/driver")
    public ResponseEntity<Driver> createDriver(@RequestBody Driver driver) {
        driver.setPassword(new BCryptPasswordEncoder().encode(driver.getPassword()));
        Driver createdDriver = (Driver) userService.save(driver);
        return ResponseEntity.ok(createdDriver);
    }

    @PostMapping("/register/passenger")
    public ResponseEntity<Passenger> createPassenger(@RequestBody Passenger passenger) {
        passenger.setPassword(new BCryptPasswordEncoder().encode(passenger.getPassword()));
        Passenger createdPassenger = (Passenger) userService.save(passenger);
        return ResponseEntity.ok(createdPassenger);
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

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER')")
    public ResponseEntity<String> updateUser(@RequestBody Map<String, Object> updates, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        Long userId = jwtTokenProvider.getUserIdFromJWT(token);

        User existingUser = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isUsernameUpdated = updates.containsKey("username");
        boolean isPasswordUpdated = updates.containsKey("password");
        boolean isRolesUpdated = updates.containsKey("roles");

        userService.updateUserFields(existingUser, updates);

        User savedUser = userService.update(existingUser);

        String updatedToken = token;
        if (isUsernameUpdated || isPasswordUpdated || isRolesUpdated) {
            updatedToken = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getUsername(), savedUser.getRoles());
        }

        return ResponseEntity.ok(updatedToken);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> userOptional = userService.getUserById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<Driver>> getNearbyDrivers(@RequestParam double latitude, @RequestParam double longitude, @RequestParam double radiusKm) {
        List<Driver> drivers = userService.findNearestDrivers(latitude, longitude, radiusKm);
        return ResponseEntity.ok(drivers);
    }
}
