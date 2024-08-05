package lu.crx.financing.services;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class FinancingCalculatorService {

    /**
     * Calculates the financing term in days.
     * Formula: the number of days between today's date and the invoice's maturity date.
     *
     * @param today today's date
     * @param maturityDate the invoice's maturity date
     * @return the financing term in days
     */
    public long calculateFinancingTerm(LocalDate today, LocalDate maturityDate) {
        return ChronoUnit.DAYS.between(today, maturityDate);
    }

    /**
     * Calculates the financing rate.
     * Formula: annualRateInBps * financingTermInDays / 360
     * Example:
     * annualRateInBps = 50 bps, financingTermInDays = 30
     * 50 * 30 / 360 = 4 bps
     *
     * @param annualRateInBps the annual rate in basis points
     * @param financingTermInDays the financing term in days
     * @return the financing rate in basis points
     */
    public int calculateFinancingRate(int annualRateInBps, long financingTermInDays) {
        return (int) ((annualRateInBps * financingTermInDays) / 360);
    }

    /**
     * Calculates the early payment amount.
     * Formula: amount - (amount * rate / 10000)
     * Example:
     * amount = 1000000 cents (10000 EUR), rate = 3 bps
     * 1000000 - (1000000 * 3 / 10000) = 1000000 - 300 = 999700 cents (9997 EUR)
     *
     * @param amount the invoice amount in cents
     * @param rate the financing rate in basis points
     * @return the early payment amount in cents
     */
    public long calculateEarlyPaymentAmount(long amount, int rate) {
        return amount - (amount * rate / 10000);
    }
}

