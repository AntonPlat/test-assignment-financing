package lu.crx.financing.repositories;

import lu.crx.financing.entities.PurchaserFinancingSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PurchaserFinancingSettingsRepository extends JpaRepository<PurchaserFinancingSettings, Long> {
    List<PurchaserFinancingSettings> findByCreditorIdIn(Set<Long> creditorIds);

}
