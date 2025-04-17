package com.fishtripplanner.dto.user;

import com.fishtripplanner.domain.UserRole;
import lombok.Getter;

@Getter
public class JoinRequest {
    private String username;
    private String password;
    private String email;
    private String address;
    private UserRole role;
}