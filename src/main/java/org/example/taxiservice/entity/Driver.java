package org.example.taxiservice.entity;

import ch.hsr.geohash.GeoHash;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

@Data
@Entity
@DiscriminatorValue("DRIVER")
public class Driver extends User {

    private String vehicleDetails;
    private String vehicleColor;
    private double driverRating;

}
