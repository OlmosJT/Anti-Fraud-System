package antifraud.dto;

import antifraud.model.LockUnlockUser;

public record LockUnlockUserRequest(String username, LockUnlockUser operation) {

}


