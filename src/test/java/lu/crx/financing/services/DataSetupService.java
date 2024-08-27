package lu.crx.financing.services;

import lu.crx.financing.entities.Creditor;
import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.Purchaser;
import lu.crx.financing.entities.PurchaserFinancingSettings;
import lu.crx.financing.entities.enums.InvoiceStatus;
import lu.crx.financing.repositories.InvoiceRepository;
import lu.crx.financing.repositories.PurchaserFinancingSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class DataSetupService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PurchaserFinancingSettingsRepository purchaserFinancingSettingsRepository;

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public void setupMockData(int numbersUnpaidInvoices, int numbersPaidInvoices) {
        // Create mock Creditor
        Creditor creditor = new Creditor();
        creditor.setName("Test Creditor");
        creditor.setMaxFinancingRateInBps(500); // Example maximum rate
        entityManager.persist(creditor);

        // Create mock Purchaser
        Purchaser purchaser = new Purchaser();
        purchaser.setName("Test Purchaser");
        purchaser.setMinimumFinancingTermInDays(10);
        entityManager.persist(purchaser);

        // Create mock PurchaserFinancingSettings
        PurchaserFinancingSettings settings = new PurchaserFinancingSettings();
        settings.setCreditor(creditor);
        settings.setAnnualRateInBps(40); // Example annual rate
        settings.setPurchaser(purchaser);

        // Save PurchaserFinancingSettings
        purchaserFinancingSettingsRepository.save(settings);

        // Create unpaid invoices
        List<Invoice> invoices = IntStream.range(0, numbersUnpaidInvoices)
                .mapToObj(i -> {
                    Invoice invoice = new Invoice();
                    invoice.setCreditor(creditor);
                    invoice.setValueInCents(100000); // Example invoice value
                    invoice.setMaturityDate(LocalDate.now().plusDays(30)); // Example maturity date
                    invoice.setStatus(InvoiceStatus.NOT_FINANCED);
                    return invoice;
                })
                .collect(Collectors.toList());

        invoiceRepository.saveAll(invoices);

        // Create paid invoices
        List<Invoice> financedInvoices = IntStream.range(0, numbersPaidInvoices)
                .mapToObj(i -> {
                    Invoice invoice = new Invoice();
                    invoice.setCreditor(creditor);
                    invoice.setValueInCents(100000); // Example invoice value
                    invoice.setMaturityDate(LocalDate.now().plusDays(30)); // Example maturity date
                    invoice.setStatus(InvoiceStatus.FINANCED);
                    return invoice;
                })
                .collect(Collectors.toList());

        invoiceRepository.saveAll(financedInvoices);
    }
}
