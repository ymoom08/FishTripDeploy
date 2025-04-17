package com.fishtripplanner.dto.user;

import lombok.Getter;

@Getter
public class LoginRequest {
    private String username;
    private String password;
}