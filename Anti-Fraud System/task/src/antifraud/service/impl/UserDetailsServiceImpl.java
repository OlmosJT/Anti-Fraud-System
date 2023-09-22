package antifraud.service.impl;

import antifraud.dto.RegisterRequest;
import antifraud.exception.ConflictException;
import antifraud.exception.OperationException;
import antifraud.model.LockUnlockUser;
import antifraud.model.Role;
import antifraud.model.UserEntity;
import antifraud.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService, antifraud.service.UserDetailsService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserDetailsServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(()->
                        new UsernameNotFoundException(
                                "Username `%s` not found".formatted(username)
                        )
                );
    }

    @Override
    @Transactional
    public UserEntity registerUser(RegisterRequest request) throws EntityExistsException {
        if(repository.existsByUsernameIgnoreCase(request.username())) {
            throw new EntityExistsException("Conflicted username %s ".formatted(request.username()));
        }
        var user = new UserEntity();
        user.setName(request.name());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        if((repository.countUsers() > 0)) {
            user.setHasAccess(false);
            user.setAuthority(Role.MERCHANT);
        } else {
            user.setHasAccess(true);
            user.setAuthority(Role.ADMINISTRATOR);
        }
        return repository.save(user);
    }

    @Override
    @Transactional
    public UserEntity changeRole(String username, Role role) throws EntityNotFoundException, ConflictException {
        var user = repository.findUserEntityByUsername(username)
                .orElseThrow(()-> new EntityNotFoundException("Username %s not found".formatted(username))
                );
        if(user.getAuthority().equals(role)) {
            throw new ConflictException("User already has the role " + role.name());
        }
        user.setAuthority(role);
        repository.save(user); // updates
        return user;
    }

    @Override
    @Transactional
    public UserEntity changeAccess(String username, LockUnlockUser access) throws EntityNotFoundException, OperationException {
        var user = repository.findUserEntityByUsername(username)
                .orElseThrow(()-> new EntityNotFoundException("Username %s not found".formatted(username)));

        if(user.getAuthority().equals(Role.ADMINISTRATOR) && access.equals(LockUnlockUser.LOCK)) {
            throw new OperationException("ADMINISTRATOR cannot be blocked");
        }
        user.setHasAccess(!access.equals(LockUnlockUser.LOCK));
        repository.save(user);
        return user;
    }

    @Override
    public List<UserEntity> getUsers() {
        return repository.findAllByOrderByIdAsc();
    }

    @Override
    @Transactional
    public void deleteUser(String username) throws EntityNotFoundException {
        if(!repository.existsByUsernameIgnoreCase(username)) {
            throw new EntityNotFoundException("username %s not found".formatted(username));
        }
        repository.deleteByUsername(username);
    }
}
