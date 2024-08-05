package lu.crx.financing.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StopWatch;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class FinancingServicePerformanceTest {

    @Autowired
    private DataSetupService dataSetupService;

    @Autowired
    private FinancingService financingService;


    @Test
    public void testPerformance() {
        // Setup mock data
        dataSetupService.setupMockData();
        StopWatch invoicesProcessTime = new StopWatch();

        // Measure time for financing
        invoicesProcessTime.start();
        financingService.finance();
        invoicesProcessTime.stop();

        System.out.println("Processed 10,000 invoices in " + invoicesProcessTime.getTotalTimeSeconds() + " second");
       // assertTimeout(); TODO use timeout or liba JMH
        assertTrue(invoicesProcessTime.getTotalTimeMillis() < 30000,
                "Performance test failed: processing took longer than 30 seconds.");
    }
}