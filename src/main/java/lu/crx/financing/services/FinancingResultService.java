package lu.crx.financing.services;

import lu.crx.financing.entities.FinancingResult;
import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.PurchaserFinancingSettings;
import lu.crx.financing.repositories.FinancingResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;

@Service
public class FinancingResultService {

    private static final Logger logger = LoggerFactory.getLogger(FinancingResultService.class);

    private final FinancingResultRepository financingResultRepository;

    private final InvoiceService invoiceService;

    public FinancingResultService(FinancingResultRepository financingResultRepository, InvoiceService invoiceService) {
        this.financingResultRepository = financingResultRepository;
        this.invoiceService= invoiceService;
    }

    @Transactional
    public void saveFinancingResults(Invoice invoice, PurchaserFinancingSettings bestPurchaserSettings, int rate, long earlyPaymentAmount, LocalDate today) {
        FinancingResult result = FinancingResult.builder()
                .invoice(invoice)
                .purchaser(bestPurchaserSettings.getPurchaser()) //
                .financingDate(today)
                .financingRateInBps(rate)
                .earlyPaymentAmountInCents(earlyPaymentAmount) //
                .build();

        financingResultRepository.save(result);

        // Обновляем состояние счета
        invoiceService.updateInvoiceAsFinanced(invoice);
        logger.info("Invoice ID: {} - Financing results saved with Purchaser ID: {}, rate: {} bps, early payment amount: {} cents",
                invoice.getId(), bestPurchaserSettings.getPurchaser().getId(), rate, earlyPaymentAmount);
    }
}
