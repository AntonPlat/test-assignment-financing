package lu.crx.financing.services;

import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lu.crx.financing.entities.*;
import lu.crx.financing.repositories.FinancingResultRepository;
import lu.crx.financing.repositories.InvoiceRepository;
import lu.crx.financing.repositories.PurchaserFinancingSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class FinancingService {

    private static final Logger logger = LoggerFactory.getLogger(FinancingService.class);

    private final InvoiceService invoiceService;
    private final PurchaserFinancingSettingsRepository purchaserFinancingSettingsRepository;

    private final FinancingResultService financingResultService;

    private final FinancingCalculatorService financingCalculatorService;
    private final PurchaserSelectionService purchaserSelectionService;

    public FinancingService(InvoiceService invoiceService,
                            PurchaserFinancingSettingsRepository purchaserFinancingSettingsRepository,
                            FinancingCalculatorService financingCalculatorService,
                            PurchaserSelectionService purchaserSelectionService,
                            FinancingResultService financingResultService) {
        this.invoiceService = invoiceService;
        this.purchaserFinancingSettingsRepository = purchaserFinancingSettingsRepository;
        this.financingCalculatorService = financingCalculatorService;
        this.purchaserSelectionService = purchaserSelectionService;
        this.financingResultService = financingResultService;
    }

    public void finance() {
        logger.info("Starting the financing process.");
        StopWatch financingProcessTime = new StopWatch();
        financingProcessTime.start();

        // Получаем все непрофинансированные счета
       List<Invoice> invoices = invoiceService.getAllNotFinancedInvoices();
        logger.info("Found not processed invoices {}", invoices.size() );
        LocalDate today = LocalDate.now();

        // Обрабатываем каждый счет
        for (Invoice invoice : invoices) {
            processInvoice(invoice, today);
        }

        financingProcessTime.stop();
        logger.info("Financed {} invoices in {} ms", invoices.size(), financingProcessTime.getTotalTimeSeconds());
    }

    private void processInvoice(Invoice invoice, LocalDate today) {
        // Получаем кредитора для счета
        Creditor creditor = invoice.getCreditor();
        if (creditor == null) {
            logger.warn("Invoice ID: {} has no creditor.", invoice.getId());
            return;
        }
        // Рассчитываем срок финансирования
        long financingTermInDays = financingCalculatorService.calculateFinancingTerm(today, invoice.getMaturityDate());

        // Находим подходящих покупателей
        List<PurchaserFinancingSettings> eligiblePurchasers = findEligiblePurchasers(creditor.getId());

        // Выбираем лучшего покупателя
        PurchaserFinancingSettings bestPurchaserSettings = purchaserSelectionService
                .selectBestPurchaser(eligiblePurchasers, creditor, financingTermInDays);

        if (bestPurchaserSettings != null) {
            // Рассчитываем ставку финансирования и сумму досрочного платежа
            int rate = financingCalculatorService.calculateFinancingRate(bestPurchaserSettings.getAnnualRateInBps(), financingTermInDays);
            long earlyPaymentAmount = financingCalculatorService.calculateEarlyPaymentAmount(invoice.getValueInCents(), rate);

            // Сохраняем результаты финансирования
            financingResultService.saveFinancingResults(invoice, bestPurchaserSettings, rate, earlyPaymentAmount, today);
        } else {
            logger.info("Invoice ID: {} - No suitable purchaser found.", invoice.getId());
        }
    }

    private List<PurchaserFinancingSettings> findEligiblePurchasers(Long creditorId) {
        return purchaserFinancingSettingsRepository.findByCreditorId(creditorId);
    }

}
