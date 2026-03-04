package com.loanmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String message;
    private String email;
    private String fullName;
    private Long userId;
    private String role;
    private String token;
}

