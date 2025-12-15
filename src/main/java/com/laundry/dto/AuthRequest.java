package com.laundry.dto;


import lombok.Data;

@Data
public class AuthRequest {
    private String firstName;
    private String password;
}
