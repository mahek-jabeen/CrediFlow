package com.loanmanagement.service.impl;

import com.loanmanagement.dto.AuthResponse;
import com.loanmanagement.dto.LoginRequest;
import com.loanmanagement.dto.SignupRequest;
import com.loanmanagement.entity.User;
import com.loanmanagement.repository.UserRepository;
import com.loanmanagement.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.loanmanagement.service.JwtService;

import java.time.LocalDate;

@Service
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public AuthResponse signup(SignupRequest signupRequest) {
        log.info("Starting signup for email: {}", signupRequest.getEmail());

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setFullName(signupRequest.getFullName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        // 🔥 FIX ROLE TYPE MISMATCH
        if (signupRequest.getRole() != null) {
            user.setRole(User.Role.valueOf(signupRequest.getRole().toUpperCase()));
        } else {
            user.setRole(User.Role.USER);
        }

        User savedUser = userRepository.save(user);
        log.info("User saved successfully with ID: {}", savedUser.getId());

        // 🔐 Generate JWT using UserDetails
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtService.generateToken(userDetails);
        log.info("JWT token generated for user: {}", savedUser.getEmail());

        return new AuthResponse(
                "User registered successfully",
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getId(),
                savedUser.getRole().name(),
                token
        );
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Starting login for email: {}", loginRequest.getEmail());

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (loginRequest.getRole() != null) {
            if (loginRequest.getRole() != user.getRole()) {
                throw new RuntimeException("Role mismatch");
            }
        }

        log.info("User authenticated successfully: {}", user.getEmail());

        // 🔐 Generate JWT using UserDetails
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);
        log.info("JWT token generated for user: {}", user.getEmail());

        return new AuthResponse(
                "Login successful",
                user.getEmail(),
                user.getFullName(),
                user.getId(),
                user.getRole().name(),
                token
        );
    }
}

