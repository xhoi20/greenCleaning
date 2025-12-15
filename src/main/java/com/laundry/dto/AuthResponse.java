package com.laundry.dto;


import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String firstName;
    private String role;
}