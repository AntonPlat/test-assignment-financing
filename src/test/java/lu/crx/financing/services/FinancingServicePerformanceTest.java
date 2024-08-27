package lu.crx.financing.services;

import lu.crx.financing.repositories.FinancingResultRepository;
import lu.crx.financing.repositories.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
public class FinancingServicePerformanceTest {

    @Autowired
    private DataSetupService dataSetupService;

    @Autowired
    private FinancingService financingService;

    @Autowired
    private FinancingResultRepository resultRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Test
    public void testPerformance() {
        int numbersUnpaidInvoices = 10000;
        int numbersPaidInvoices = 1000000;

        // Setup mock data
        dataSetupService.setupMockData(numbersUnpaidInvoices, numbersPaidInvoices);
        StopWatch invoicesProcessTime = new StopWatch();

        // Measure time for financing
        invoicesProcessTime.start();
        financingService.finance();
        invoicesProcessTime.stop();

        long numbersSavedResults = resultRepository.count();

        assertEquals(numbersUnpaidInvoices, numbersSavedResults);
        assertTrue(invoicesProcessTime.getTotalTimeSeconds() < 30,
                "Performance test failed: processing took longer than 30 seconds.");
    }
}