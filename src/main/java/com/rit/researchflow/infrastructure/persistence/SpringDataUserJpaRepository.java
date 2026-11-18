package com.rit.researchflow.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rit.researchflow.domain.user.Administrator;
import com.rit.researchflow.domain.user.User;

public interface SpringDataUserJpaRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("select a from Administrator a where a.active = true")
    List<Administrator> findActiveAdministrators();
}
