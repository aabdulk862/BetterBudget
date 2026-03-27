package com.revature.project2.models.DTOs;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record EnvelopeDTO(
        @NotNull Integer userId,
        @NotBlank @Size(max = 255) String envelopeDescription,
        @NotNull @DecimalMin("0") BigDecimal balance,
        @NotNull @DecimalMin("0") BigDecimal maxLimit
) {
}
