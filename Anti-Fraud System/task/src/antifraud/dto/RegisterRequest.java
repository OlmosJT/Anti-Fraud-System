package antifraud.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotNull @NotEmpty
        String name,
        @NotNull @NotEmpty
        String username,
        @NotNull @NotEmpty
        String password
) {
}
