package lu.crx.financing.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.crx.financing.entities.Creditor;
import lu.crx.financing.entities.FinancingResult;
import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.PurchaserFinancingSettings;
import lu.crx.financing.entities.enums.InvoiceStatus;
import lu.crx.financing.repositories.InvoiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final FinancingResultService financingResultService;
    private final FinancingCalculatorService financingCalculatorService;
    private final PurchaserService purchaserService;

    public long countNotFinancedInvoicesAfterDate(LocalDate date) {
        return invoiceRepository.countByStatusAndMaturityDateAfter(InvoiceStatus.NOT_FINANCED, date);
    }

    public List<Invoice> getAllNotFinancedInvoicesAfterDateWithPagination(LocalDate date, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return invoiceRepository.findByStatusAndMaturityDateAfter(InvoiceStatus.NOT_FINANCED, date, pageable);
    }

    @Transactional
    public void processBatch(List<Invoice> invoices, LocalDate today, Map<Long, List<PurchaserFinancingSettings>> eligiblePurchasersByCreditor) {
        List<FinancingResult> financingResults = invoices.stream()
                .map(invoice -> evaluateInvoiceForFinancing(invoice, today, eligiblePurchasersByCreditor))
                .filter(Objects::nonNull).collect(Collectors.toList());

        saveFinancingResults(financingResults);
        saveInvoices(invoices);
    }

    private FinancingResult evaluateInvoiceForFinancing(Invoice invoice, LocalDate today, Map<Long, List<PurchaserFinancingSettings>> eligiblePurchasersByCreditor) {
        Creditor creditor = invoice.getCreditor();
        if (creditor == null) {
            invoice.setStatus(InvoiceStatus.CANNOT_BE_FINANCED);
            log.warn("Invoice with ID {} has no creditor.", invoice.getId());
            return null;
        }

        List<PurchaserFinancingSettings> eligiblePurchasers = eligiblePurchasersByCreditor.get(creditor.getId());
        if (eligiblePurchasers == null || eligiblePurchasers.isEmpty()) {
            invoice.setStatus(InvoiceStatus.CANNOT_BE_FINANCED);
            log.info("No suitable purchasers found for creditor ID: {}", creditor.getId());
            return null;
        }

        return processInvoice(invoice, today, eligiblePurchasers, creditor);
    }

    private FinancingResult processInvoice(Invoice invoice, LocalDate today, List<PurchaserFinancingSettings> eligiblePurchasers, Creditor creditor) {
        long financingTermInDays = financingCalculatorService.calculateFinancingTerm(today, invoice.getMaturityDate());

        PurchaserFinancingSettings bestPurchaserSettings = purchaserService
                .selectBestPurchaser(eligiblePurchasers, creditor, financingTermInDays);

        if (bestPurchaserSettings != null) {
            int rate = financingCalculatorService.calculateFinancingRate(bestPurchaserSettings.getAnnualRateInBps(), financingTermInDays);
            long earlyPaymentAmount = financingCalculatorService.calculateEarlyPaymentAmount(invoice.getValueInCents(), rate);

            invoice.setStatus(InvoiceStatus.FINANCED);

            return FinancingResult.builder()
                    .invoice(invoice)
                    .purchaser(bestPurchaserSettings.getPurchaser())
                    .financingDate(today)
                    .financingRateInBps(rate)
                    .earlyPaymentAmountInCents(earlyPaymentAmount)
                    .build();
        } else {
            invoice.setStatus(InvoiceStatus.CANNOT_BE_FINANCED);
            log.info("Invoice ID: {} - No suitable purchaser found.", invoice.getId());
            return null;
        }
    }

    private void saveFinancingResults(List<FinancingResult> financingResults) {
        if (!financingResults.isEmpty()) {
            financingResultService.saveFinancingResultsBatch(financingResults);
        }
    }

    private void saveInvoices(List<Invoice> invoices) {
        if (!invoices.isEmpty()) {
            invoiceRepository.saveAll(invoices);
        }
    }
}
