package com.rit.researchflow.domain.repository;

import java.util.List;
import java.util.Optional;

import com.rit.researchflow.domain.user.Administrator;
import com.rit.researchflow.domain.user.User;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<Administrator> findActiveAdministrators();
}
