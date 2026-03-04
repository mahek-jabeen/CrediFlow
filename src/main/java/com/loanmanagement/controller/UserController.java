package com.loanmanagement.controller;

import com.loanmanagement.dto.ApiResponse;
import com.loanmanagement.entity.User;
import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        log.info("Creating new user with email: {}", user.getEmail());
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(ApiResponse.success("User created successfully", createdUser), 
                HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        log.info("Fetching user with id: {}", id);
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<User>> getUserByEmail(@PathVariable String email) {
        log.info("Fetching user with email: {}", email);
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<ApiResponse<User>> getUserByPhoneNumber(@PathVariable String phoneNumber) {
        log.info("Fetching user with phone number: {}", phoneNumber);
        User user = userService.getUserByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User", "phoneNumber", phoneNumber));
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @RequestBody User user) {
        log.info("Updating user with id: {}", id);
        userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);
        userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }
}
