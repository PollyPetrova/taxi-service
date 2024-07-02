package org.example.taxiservice.entity;

import ch.hsr.geohash.GeoHash;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "tb_user")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private Double latitude;
    private Double longitude;
    private String geohash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Set<Role> roles = new HashSet<>();

    public enum Role implements GrantedAuthority {
        DRIVER, PASSENGER;

        @Override
        public String getAuthority() {
            return this.name();
        }
    }

    @PrePersist
    @PreUpdate
    public void calculateGeohash() {
        geohash = GeoHash.geoHashStringWithCharacterPrecision(latitude, longitude, 7);
    }

    public Set<GrantedAuthority> getAuthorities() {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.name())).collect(Collectors.toSet());
    }
}
