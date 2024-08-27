package lu.crx.financing.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.PurchaserFinancingSettings;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FinancingService {

    private static final int BATCH_SIZE = 1000;

    private final InvoiceService invoiceService;
    private final PurchaserService purchaserService;

    public void finance() {
        log.info("Starting the financing process.");
        LocalDate today = LocalDate.now();
        long totalInvoices = invoiceService.countNotFinancedInvoicesAfterDate(today);

        log.info("Total invoices to be processed: {}", totalInvoices);

        StopWatch financingProcessTime = new StopWatch();
        financingProcessTime.start();

        int totalProcessedInvoices = 0;
        int pageNumber = 0;

        while (true) {
            List<Invoice> invoiceBatch = invoiceService
                    .getAllNotFinancedInvoicesAfterDateWithPagination(today, pageNumber, BATCH_SIZE);

            if (invoiceBatch.isEmpty()) {
                break;
            }

            log.info("Processing batch with {} invoices.", invoiceBatch.size());

            List<PurchaserFinancingSettings> eligiblePurchasersForInvoices = purchaserService
                    .findEligiblePurchasersForInvoices(invoiceBatch);
            Map<Long, List<PurchaserFinancingSettings>> eligiblePurchasersByCreditor = eligiblePurchasersForInvoices.stream()
                    .collect(Collectors.groupingBy(x -> x.getCreditor().getId()));

            invoiceService.processBatch(invoiceBatch, today, eligiblePurchasersByCreditor);
            totalProcessedInvoices += invoiceBatch.size();

            log.info("Processed {} out of {} invoices", totalProcessedInvoices, totalInvoices);
        }

        financingProcessTime.stop();
        log.info("Financing process completed. Total invoices processed: {} in {} seconds.",
                totalProcessedInvoices, financingProcessTime.getTotalTimeSeconds());
    }
}
