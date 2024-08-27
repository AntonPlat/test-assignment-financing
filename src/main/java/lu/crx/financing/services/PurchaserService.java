package lu.crx.financing.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.crx.financing.entities.Creditor;
import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.PurchaserFinancingSettings;
import lu.crx.financing.repositories.PurchaserFinancingSettingsRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class PurchaserService {

    private final FinancingCalculatorService financingCalculatorService;
    private final PurchaserFinancingSettingsRepository purchaserFinancingSettingsRepository;

    public PurchaserFinancingSettings selectBestPurchaser(List<PurchaserFinancingSettings> eligiblePurchasers, Creditor creditor,
                                                           long financingTermInDays) {

        return eligiblePurchasers.stream()
                .filter(purchaserFinancingSettings -> isTermEligible(purchaserFinancingSettings, financingTermInDays))
                .filter(purchaserFinancingSettings -> isRateEligible(purchaserFinancingSettings, creditor, financingTermInDays))
                .min(Comparator.comparing(PurchaserFinancingSettings::getAnnualRateInBps))
                .orElse(null);
    }

    public List<PurchaserFinancingSettings> findEligiblePurchasersForInvoices(List<Invoice> invoiceList) {
        Set<Long> uniqueCreditorsIds = invoiceList.stream()
                .map(invoice -> invoice.getCreditor().getId())
                .collect(Collectors.toSet());
        return purchaserFinancingSettingsRepository.findByCreditorIdIn(uniqueCreditorsIds);
    }

    private boolean isTermEligible(PurchaserFinancingSettings settings, long financingTermInDays) {
        return financingTermInDays >= settings.getPurchaser().getMinimumFinancingTermInDays();
    }

    private boolean isRateEligible(PurchaserFinancingSettings settings, Creditor creditor, long financingTermInDays) {
        int calculatedRate = financingCalculatorService.calculateFinancingRate(settings.getAnnualRateInBps(), financingTermInDays);
        return calculatedRate <= creditor.getMaxFinancingRateInBps();
    }
}
