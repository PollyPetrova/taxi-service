package org.example.taxiservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.taxiservice.auth.JWTTokenProvider;
import org.example.taxiservice.entity.Driver;
import org.example.taxiservice.entity.Passenger;
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

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateUser(@RequestBody User updatedUser, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        Long userId = jwtTokenProvider.getUserIdFromJWT(token);

        User existingUser = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setPassword(new BCryptPasswordEncoder().encode(updatedUser.getPassword()));
        existingUser.setLatitude(updatedUser.getLatitude());
        existingUser.setLongitude(updatedUser.getLongitude());
        existingUser.setRoles(updatedUser.getRoles());

        if (existingUser instanceof Driver && updatedUser instanceof Driver) {
            Driver existingDriver = (Driver) existingUser;
            Driver updatedDriver = (Driver) updatedUser;
            existingDriver.setVehicleDetails(updatedDriver.getVehicleDetails());
            existingDriver.setVehicleColor(updatedDriver.getVehicleColor());
            existingDriver.setDriverRating(updatedDriver.getDriverRating());
        }

        if (existingUser instanceof Passenger && updatedUser instanceof Passenger) {
            Passenger existingPassenger = (Passenger) existingUser;
            Passenger updatedPassenger = (Passenger) updatedUser;
            existingPassenger.setPaymentMethod(updatedPassenger.getPaymentMethod());
        }

        User savedUser = userService.update(existingUser);

        String updatedToken = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getUsername(), savedUser.getRoles());

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
