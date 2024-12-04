package com.example.Demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class UserDto {
    @NotNull(message = "username cannot be null")
    private String username;

    @NotNull(message = "password cannot be null")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    private Long partnerId;
}
