package antifraud.service;

import antifraud.dto.RegisterRequest;
import antifraud.model.LockUnlockUser;
import antifraud.model.Role;
import antifraud.model.UserEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public interface UserDetailsService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    UserEntity registerUser(RegisterRequest request);
    UserEntity changeRole(String username, Role role);
    UserEntity changeAccess(String username, LockUnlockUser access);

    List<UserEntity> getUsers();

    void deleteUser(String username);
}
