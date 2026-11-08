package org.example.taskmanager.dto.user;

public record UserResponse(
        Long id,
        String name,
        String email
) {}
