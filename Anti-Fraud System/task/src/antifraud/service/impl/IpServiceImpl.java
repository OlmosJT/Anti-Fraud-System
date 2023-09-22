package antifraud.service.impl;

import antifraud.exception.ConflictException;
import antifraud.model.SusIpAddress;
import antifraud.repository.IPRepository;
import antifraud.service.IpService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IpServiceImpl implements IpService {

    private final IPRepository repository;

    public IpServiceImpl(IPRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Override
    public SusIpAddress addIPToBlackList(String IP) throws ConflictException {
        if(repository.existsByIp(IP)) {
            throw new ConflictException(IP + " has already in Black List.");
        }

        SusIpAddress susIpAddress = new SusIpAddress();
        susIpAddress.setIp(IP);
        repository.save(susIpAddress);

        return susIpAddress;
    }


    @Transactional
    @Override
    public void removeIPFromBlackList(String ip) throws EntityNotFoundException {
        if(!repository.existsByIp(ip)) {
            throw new EntityNotFoundException(ip + " address not found");
        }
        repository.deleteByIp(ip);
    }

    @Transactional
    @Override
    public List<SusIpAddress> getBlackListedIPs() {
        return repository.findAll(Sort.by(Sort.Order.asc("id")));
    }

    @Override
    public boolean validateIP(String ip) {
        return ip.matches("^((25[0-5]|(2[0-4]|1[0-9]|[1-9])\\d?)\\.){3}(25[0-5]|(2[0-4]|1[0-9]|[1-9])\\d?)$");
    }

    @Override
    public boolean isSuspiciousIp(String ip) {
        return repository.existsByIp(ip);
    }
}
