package antifraud.repository;

import antifraud.model.SusIpAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPRepository extends JpaRepository<SusIpAddress, Long> {
    Optional<SusIpAddress> findByIp(String ip);
    void deleteByIp(String ip);
    Boolean existsByIp(String ip);
}
