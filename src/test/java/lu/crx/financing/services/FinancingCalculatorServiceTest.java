package lu.crx.financing.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FinancingCalculatorServiceTest {

    private static FinancingCalculatorService financingCalculatorService;

    @BeforeAll
    public static void initTest() {
        financingCalculatorService = new FinancingCalculatorService();
    }

    @Test
    public void calculateFinancingTerm() {
        LocalDate today = LocalDate.of(2023, 5, 27);
        LocalDate maturityDate = LocalDate.of(2023, 6, 26);
        long expectedTerm = 30;

        long actualTerm = financingCalculatorService.calculateFinancingTerm(today, maturityDate);

        assertEquals(expectedTerm, actualTerm);
    }

    @Test
    public void calculateFinancingRate() {
        int annualRateInBps = 40;
        long financingTermInDays = 30;
        int expectedRate = 3; // 40 * 30 / 360 = 3.333

        int actualRate = financingCalculatorService.calculateFinancingRate(annualRateInBps, financingTermInDays);

        assertEquals(expectedRate, actualRate);
    }

    @Test
    public void calculateEarlyPaymentAmount() {
        long amount = (long) 1000000.00;
        int rate = 3;
        long expectedEarlyPaymentAmount = (long) 999700.00;


        long actualEarlyPaymentAmount = financingCalculatorService.calculateEarlyPaymentAmount(amount, rate);

        assertEquals(expectedEarlyPaymentAmount, actualEarlyPaymentAmount);
    }
}