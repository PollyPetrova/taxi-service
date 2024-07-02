package org.example.taxiservice.repository;

import org.example.taxiservice.entity.Driver;
import org.example.taxiservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    List<Driver> findByLatitudeBetweenAndLongitudeBetweenAndRolesEquals(
            double minLatitude, double maxLatitude,
            double minLongitude, double maxLongitude,
            User.Role role);
}
