package antifraud;

import antifraud.model.Region;
import antifraud.model.TransactionLimit;
import antifraud.model.TransactionStatus;
import antifraud.repository.RegionRepository;
import antifraud.repository.LimitTransactionRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class InitializeConfig {

    private final RegionRepository regionRepository;
    private final LimitTransactionRepository limitRepository;

    public InitializeConfig(RegionRepository regionRepository, LimitTransactionRepository limitRepository) {
        this.regionRepository = regionRepository;
        this.limitRepository = limitRepository;
    }

    @PostConstruct
    public void initialize() {
        if(regionRepository.count() == 0) {
            regionRepository.save(Region.builder().code("EAP").description("East Asia and Pacific").build());
            regionRepository.save(Region.builder().code("ECA").description("Europe and Central Asia").build());
            regionRepository.save(Region.builder().code("HIC").description("High-Income countries").build());
            regionRepository.save(Region.builder().code("LAC").description("Latin America and the Caribbean").build());
            regionRepository.save(Region.builder().code("MENA").description("The Middle East and North Africa").build());
            regionRepository.save(Region.builder().code("SA").description("South Asia").build());
            regionRepository.save(Region.builder().code("SSA").description("Sub-Saharan Africa").build());
        }

        if(limitRepository.count() == 0) {
            limitRepository.save(TransactionLimit.builder().name(TransactionStatus.ALLOWED).limitAmount(200).build());
            limitRepository.save(TransactionLimit.builder().name(TransactionStatus.MANUAL_PROCESSING).limitAmount(1500).build());
            limitRepository.save(TransactionLimit.builder().name(TransactionStatus.PROHIBITED).build());
        }
    }
}
