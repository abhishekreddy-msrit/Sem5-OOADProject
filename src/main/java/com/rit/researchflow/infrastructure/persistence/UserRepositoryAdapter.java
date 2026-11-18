package com.rit.researchflow.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.rit.researchflow.domain.repository.UserRepository;
import com.rit.researchflow.domain.user.Administrator;
import com.rit.researchflow.domain.user.Faculty;
import com.rit.researchflow.domain.user.User;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserJpaRepository jpaRepository;

    public UserRepositoryAdapter(SpringDataUserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public List<Administrator> findActiveAdministrators() {
        return jpaRepository.findActiveAdministrators();
    }

    @Override
    public List<Faculty> findActiveFaculty() {
        return jpaRepository.findActiveFaculty();
    }
}
