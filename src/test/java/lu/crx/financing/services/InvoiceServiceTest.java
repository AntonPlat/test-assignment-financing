package lu.crx.financing.services;

import lu.crx.financing.entities.Creditor;
import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.PurchaserFinancingSettings;
import lu.crx.financing.entities.enums.InvoiceStatus;
import lu.crx.financing.repositories.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class InvoiceServiceTest {
    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private FinancingResultService financingResultService;

    @Mock
    private FinancingCalculatorService financingCalculatorService;

    @Mock
    private PurchaserService purchaserService;

    @InjectMocks
    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessBatch() {
        LocalDate today = LocalDate.now();

        Creditor creditor1 = new Creditor();
        creditor1.setId(1L);
        Creditor creditor2 = new Creditor();
        creditor2.setId(2L);

        Invoice invoice1 = new Invoice();
        invoice1.setStatus(InvoiceStatus.NOT_FINANCED);
        invoice1.setCreditor(creditor1);

        Invoice invoice2 = new Invoice();
        invoice2.setStatus(InvoiceStatus.NOT_FINANCED);
        invoice2.setCreditor(creditor2);

        List<Invoice> invoices = Arrays.asList(invoice1, invoice2);
        List<PurchaserFinancingSettings> purchaserSettings = Collections.singletonList(new PurchaserFinancingSettings());
        Map<Long, List<PurchaserFinancingSettings>> eligiblePurchasersByCreditor = new HashMap<>();
        eligiblePurchasersByCreditor.put(1L, purchaserSettings);

        when(financingCalculatorService.calculateFinancingTerm(any(LocalDate.class), any(LocalDate.class))).thenReturn(30L);
        when(purchaserService.selectBestPurchaser(anyList(), any(Creditor.class), anyLong())).thenReturn(new PurchaserFinancingSettings());
        when(financingCalculatorService.calculateFinancingRate(anyInt(), anyLong())).thenReturn(100);
        when(financingCalculatorService.calculateEarlyPaymentAmount(anyLong(), anyInt())).thenReturn(9000L);

        invoiceService.processBatch(invoices, today, eligiblePurchasersByCreditor);

        verify(financingResultService, times(1)).saveFinancingResultsBatch(anyList());
        verify(invoiceRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testProcessBatchInvoiceWithoutCreditor() {
        LocalDate today = LocalDate.now();
        Invoice invoice = new Invoice();
        invoice.setStatus(InvoiceStatus.NOT_FINANCED);

        List<Invoice> invoices = Collections.singletonList(invoice);
        Map<Long, List<PurchaserFinancingSettings>> eligiblePurchasersByCreditor = new HashMap<>();

        invoiceService.processBatch(invoices, today, eligiblePurchasersByCreditor);

        assertEquals(InvoiceStatus.CANNOT_BE_FINANCED, invoice.getStatus());
        verify(financingResultService, never()).saveFinancingResultsBatch(anyList());
        verify(invoiceRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testProcessBatchInvoiceWithNoEligiblePurchasers() {
        LocalDate today = LocalDate.now();
        Invoice invoice = new Invoice();
        Creditor creditor = new Creditor();
        invoice.setCreditor(creditor);
        invoice.setStatus(InvoiceStatus.NOT_FINANCED);

        List<Invoice> invoices = Collections.singletonList(invoice);
        Map<Long, List<PurchaserFinancingSettings>> eligiblePurchasersByCreditor = new HashMap<>();

        invoiceService.processBatch(invoices, today, eligiblePurchasersByCreditor);

        assertEquals(InvoiceStatus.CANNOT_BE_FINANCED, invoice.getStatus());
        verify(financingResultService, never()).saveFinancingResultsBatch(anyList());
        verify(invoiceRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testProcessBatchInvoiceFinancedSuccessfully() {
        LocalDate today = LocalDate.now();
        Invoice invoice = new Invoice();
        Creditor creditor = new Creditor();
        creditor.setId(1l);
        invoice.setCreditor(creditor);
        invoice.setStatus(InvoiceStatus.NOT_FINANCED);

        List<Invoice> invoices = Collections.singletonList(invoice);
        List<PurchaserFinancingSettings> purchaserSettings = Collections.singletonList(new PurchaserFinancingSettings());
        Map<Long, List<PurchaserFinancingSettings>> eligiblePurchasersByCreditor = new HashMap<>();
        eligiblePurchasersByCreditor.put(1L, purchaserSettings);

        when(financingCalculatorService.calculateFinancingTerm(any(LocalDate.class), any(LocalDate.class))).thenReturn(30L);
        when(purchaserService.selectBestPurchaser(anyList(), any(Creditor.class), anyLong())).thenReturn(new PurchaserFinancingSettings());
        when(financingCalculatorService.calculateFinancingRate(anyInt(), anyLong())).thenReturn(100);
        when(financingCalculatorService.calculateEarlyPaymentAmount(anyLong(), anyInt())).thenReturn(9000L);

        invoiceService.processBatch(invoices, today, eligiblePurchasersByCreditor);

        assertEquals(InvoiceStatus.FINANCED, invoice.getStatus());
        verify(financingResultService, times(1)).saveFinancingResultsBatch(anyList());
        verify(invoiceRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testProcessBatchInvoiceCannotBeFinanced() {
        LocalDate today = LocalDate.now();
        Invoice invoice = new Invoice();
        Creditor creditor = new Creditor();
        invoice.setCreditor(creditor);
        invoice.setStatus(InvoiceStatus.NOT_FINANCED);

        List<Invoice> invoices = Collections.singletonList(invoice);
        List<PurchaserFinancingSettings> purchaserSettings = Collections.singletonList(new PurchaserFinancingSettings());
        Map<Long, List<PurchaserFinancingSettings>> eligiblePurchasersByCreditor = new HashMap<>();
        eligiblePurchasersByCreditor.put(1L, purchaserSettings);

        when(financingCalculatorService.calculateFinancingTerm(any(LocalDate.class), any(LocalDate.class))).thenReturn(30L);
        when(purchaserService.selectBestPurchaser(anyList(), any(Creditor.class), anyLong())).thenReturn(null);

        invoiceService.processBatch(invoices, today, eligiblePurchasersByCreditor);

        assertEquals(InvoiceStatus.CANNOT_BE_FINANCED, invoice.getStatus());
        verify(financingResultService, never()).saveFinancingResultsBatch(anyList());
        verify(invoiceRepository, times(1)).saveAll(anyList());
    }

}