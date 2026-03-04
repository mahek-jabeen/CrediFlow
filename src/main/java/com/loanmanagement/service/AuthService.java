package com.loanmanagement.service;

import com.loanmanagement.dto.AuthResponse;
import com.loanmanagement.dto.LoginRequest;
import com.loanmanagement.dto.SignupRequest;

public interface AuthService {
    
    AuthResponse signup(SignupRequest signupRequest);
    
    AuthResponse login(LoginRequest loginRequest);
}
