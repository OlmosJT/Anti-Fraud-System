package antifraud.controller;

import antifraud.dto.LockUnlockUserRequest;
import antifraud.dto.UserRoleChangeRequest;
import antifraud.dto.RegisterRequest;
import antifraud.dto.UserInfoResponse;
import antifraud.model.Role;
import antifraud.service.UserDetailsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserDetailsService userDetailsService;

    @Autowired
    public AuthController(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/user")
    public ResponseEntity<UserInfoResponse> registerUser(@Valid @RequestBody @NotNull RegisterRequest request) {
        var user = userDetailsService.registerUser(request);
        return new ResponseEntity<>(
                new UserInfoResponse(
                        user.getId(),
                        user.getName(),
                        user.getUsername(),
                        user.getAuthority()
                ), HttpStatus.CREATED);
    }

    @PreAuthorize(value = "hasRole('ADMINISTRATOR')")
    @DeleteMapping("/user/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        userDetailsService.deleteUser(username);
        return new ResponseEntity<>(
                Map.of("username", username,
                        "status", "Deleted successfully!"
                ), HttpStatus.OK);
    }

    @PreAuthorize(value = "hasAnyRole('ADMINISTRATOR','SUPPORT')")
    @GetMapping("/list")
    public ResponseEntity<?> getUsers() {
        var body = userDetailsService.getUsers()
                .stream()
                .map(it -> new UserInfoResponse(
                                it.getId(),
                                it.getName(),
                                it.getUsername(),
                                it.getAuthority()
                        )
                ).collect(Collectors.toList());
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @PreAuthorize(value = "hasRole('ADMINISTRATOR')")
    @PutMapping("/access")
    public ResponseEntity<?> giveAccessToUser(@RequestBody LockUnlockUserRequest lockUnlockUserRequest) {
        var user = userDetailsService.changeAccess(lockUnlockUserRequest.username(), lockUnlockUserRequest.operation());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("status", "User %s %sed!".formatted(
                        lockUnlockUserRequest.username(),
                        lockUnlockUserRequest.operation())
                )
                );
    }


    @Validated
    @PutMapping("/role")
    @PreAuthorize(value = "hasAnyRole('ADMINISTRATOR')")
    public ResponseEntity<?> changeRoleUser(@RequestBody UserRoleChangeRequest request) {
        if(request.role().equals(Role.ADMINISTRATOR)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Role ADMINISTRATOR cannot be given!"));
        }

        var userEntity = userDetailsService.changeRole(request.username(), request.role());
        var body = new UserInfoResponse(
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getUsername(),
                userEntity.getAuthority()
                );
        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}
