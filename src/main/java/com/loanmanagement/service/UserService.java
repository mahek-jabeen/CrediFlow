package com.loanmanagement.service;

import com.loanmanagement.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User createUser(User user);

    User updateUser(Long id, User user);

    Optional<User> getUserById(Long id);

    Optional<User> getUserByEmail(String email);

    Optional<User> getUserByPhoneNumber(String phoneNumber);

    List<User> getAllUsers();

    void deleteUser(Long id);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
