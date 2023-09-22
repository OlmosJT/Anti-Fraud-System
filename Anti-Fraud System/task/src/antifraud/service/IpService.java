package antifraud.service;

import antifraud.exception.ConflictException;
import antifraud.model.SusIpAddress;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

public interface IpService {
    SusIpAddress addIPToBlackList(String IP) throws ConflictException;
    void removeIPFromBlackList(String ip) throws EntityNotFoundException;
    List<SusIpAddress> getBlackListedIPs();
    boolean validateIP(String ip);

    boolean isSuspiciousIp(String ip);
}
