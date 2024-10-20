package com.uskov.pet.services;

import com.uskov.pet.data.User;
import com.uskov.pet.data.UserRepository;
import com.uskov.pet.exception.AlreadyExistException;
import com.uskov.pet.webClient.model.NewUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> get(UUID id) {
        return repository.findById(id);
    }

    public User update(User entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<User> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<User> list(Pageable pageable, Specification<User> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public User save(User user) {
        return repository.save(user);
    }

    public User create(NewUserRequest user) {
        if (repository.existsByUsername(user.getUsername())) {
            throw new AlreadyExistException("Username already exist");
        }

        return save(User.builder()
                .username(user.getUsername())
                .hashedPassword(passwordEncoder.encode(user.getPassword()))
                .roles(user.getRoles())
                .build());
    }

}
