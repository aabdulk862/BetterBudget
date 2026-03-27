package com.revature.project2.models.DTOs;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferFundDTO(
        @NotNull Integer fromId,
        @NotNull Integer toId,
        @NotBlank String transactionTitle,
        String transactionDescription,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal amount
) {
}
