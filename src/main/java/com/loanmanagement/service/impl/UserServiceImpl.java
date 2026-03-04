package com.loanmanagement.service.impl;

import com.loanmanagement.entity.User;
import com.loanmanagement.repository.UserRepository;
import com.loanmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User createUser(User user) {
        log.debug("Creating user with email: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User user) {
        log.debug("Updating user with id: {}", id);
        user.setId(id);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        log.debug("Fetching user with id: {}", id);
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        log.debug("Fetching user with email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByPhoneNumber(String phoneNumber) {
        log.debug("Fetching user with phone number: {}", phoneNumber);
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(Long id) {
        log.debug("Deleting user with id: {}", id);
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }
}
