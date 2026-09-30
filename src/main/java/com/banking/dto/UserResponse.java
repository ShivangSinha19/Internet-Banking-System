package com.banking.dto;

import com.banking.entity.UserEntity;

public record UserResponse(Integer userId, String firstName, String lastName, String email, String role) {
    public static UserResponse from(UserEntity user) {
        return new UserResponse(user.getUserId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getRole());
    }
}