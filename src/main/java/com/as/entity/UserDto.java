package com.as.entity;

import jakarta.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class UserDto {

    @NotEmpty
    private String userName;

    @NotEmpty
    private String password;
}