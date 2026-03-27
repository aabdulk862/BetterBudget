package com.revature.project2.models.DTOs;

import jakarta.validation.constraints.NotBlank;

public record IncomingLogin(
        @NotBlank String username,
        @NotBlank String password
) {
}
