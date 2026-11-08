package org.example.taskmanager.mapper;

import org.example.taskmanager.dto.user.UserResponse;
import org.example.taskmanager.entity.User;

public final class UserMapper {

    private UserMapper() {
    }
    public static UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
