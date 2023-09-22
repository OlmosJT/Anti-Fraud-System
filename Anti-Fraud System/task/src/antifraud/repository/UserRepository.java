package antifraud.repository;

import antifraud.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findUserEntityByUsername(String username);

    List<UserEntity> findAllByOrderByIdAsc();
    Boolean existsByUsernameIgnoreCase(String username);

    Optional<UserEntity> findByUsernameIgnoreCase(String username);

    void deleteByUsername(String username);

    @Query("SELECT COUNT(*) FROM UserEntity")
    Long countUsers();
}
