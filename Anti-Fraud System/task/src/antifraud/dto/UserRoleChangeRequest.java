package antifraud.dto;

import antifraud.model.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UserRoleChangeRequest(
        @NotNull
        @NotEmpty
        String username,
        @NotNull
        @NotEmpty
        Role role
) {
}
