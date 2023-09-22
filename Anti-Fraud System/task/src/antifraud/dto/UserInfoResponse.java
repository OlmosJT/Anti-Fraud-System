package antifraud.dto;

import antifraud.model.Role;

public record UserInfoResponse(Long id, String name, String username, Role role) {
}
