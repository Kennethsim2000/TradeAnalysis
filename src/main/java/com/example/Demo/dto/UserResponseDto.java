package com.example.Demo.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public class UserResponseDto {
    @NotNull(message = "username cannot be null")
    private String username;

    private LocalDateTime dateCreated;

}
