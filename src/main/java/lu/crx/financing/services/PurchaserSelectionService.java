package lu.crx.financing.services;

import lu.crx.financing.entities.Creditor;
import lu.crx.financing.entities.PurchaserFinancingSettings;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class PurchaserSelectionService {

    private final FinancingCalculatorService financingCalculatorService;

    public PurchaserSelectionService(FinancingCalculatorService financingCalculatorService) {
        this.financingCalculatorService = financingCalculatorService;
    }

    public PurchaserFinancingSettings selectBestPurchaser(List<PurchaserFinancingSettings> eligiblePurchasers, Creditor creditor,
                                                           long financingTermInDays) {
        //TODO сделать несколько фильтро или сделать методы
        return eligiblePurchasers.stream()
                .filter(purchaserFinancingSettings -> isTermEligible(purchaserFinancingSettings, financingTermInDays))
                .filter(purchaserFinancingSettings -> isRateEligible(purchaserFinancingSettings, creditor, financingTermInDays))
                .min(Comparator.comparing(PurchaserFinancingSettings::getAnnualRateInBps))
                .orElse(null);
    }

    private boolean isTermEligible(PurchaserFinancingSettings settings, long financingTermInDays) {
        return financingTermInDays >= settings.getPurchaser().getMinimumFinancingTermInDays();
    }

    private boolean isRateEligible(PurchaserFinancingSettings settings, Creditor creditor, long financingTermInDays) {
        int calculatedRate = financingCalculatorService.calculateFinancingRate(settings.getAnnualRateInBps(), financingTermInDays);
        return calculatedRate <= creditor.getMaxFinancingRateInBps();
    }
}
