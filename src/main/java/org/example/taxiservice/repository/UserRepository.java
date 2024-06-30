package org.example.taxiservice.repository;

import org.example.taxiservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByGeohashStartsWithAndRolesContaining(String geohashPrefix, User.Role role);
}
