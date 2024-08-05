package lu.crx.financing.services;

import lu.crx.financing.entities.Creditor;
import lu.crx.financing.entities.Purchaser;
import lu.crx.financing.entities.PurchaserFinancingSettings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PurchaserSelectionServiceTest {

    private static FinancingCalculatorService financingCalculatorService;

    private static PurchaserSelectionService purchaserSelectionService;

    @BeforeAll
    public static void initTest() {
        financingCalculatorService = new FinancingCalculatorService();
        purchaserSelectionService = new PurchaserSelectionService(financingCalculatorService);
    }

    @Test
    public void testSelectBestPurchaser() {
        // Arrange
        Creditor creditor = new Creditor();
        creditor.setMaxFinancingRateInBps(500);

        Purchaser purchaser1 = new Purchaser();
        purchaser1.setMinimumFinancingTermInDays(10);

        Purchaser purchaser2 = new Purchaser();
        purchaser2.setMinimumFinancingTermInDays(5);

        PurchaserFinancingSettings settings1 = new PurchaserFinancingSettings();
        settings1.setPurchaser(purchaser1);
        settings1.setAnnualRateInBps(100);

        PurchaserFinancingSettings settings2 = new PurchaserFinancingSettings();
        settings2.setPurchaser(purchaser2);
        settings2.setAnnualRateInBps(50);

        List<PurchaserFinancingSettings> eligiblePurchasers = Arrays.asList(settings1, settings2);

        long financingTermInDays = 15;

        // Act
        PurchaserFinancingSettings bestPurchaser = purchaserSelectionService.selectBestPurchaser(eligiblePurchasers, creditor, financingTermInDays);

        // Assert
        assertEquals(settings2, bestPurchaser);
    }

    @Test
    public void testSelectBestPurchaserNoEligiblePurchasers() {
        // Arrange
        Creditor creditor = new Creditor();
        creditor.setMaxFinancingRateInBps(3);

        Purchaser purchaser = new Purchaser();
        purchaser.setMinimumFinancingTermInDays(10);

        PurchaserFinancingSettings settings = new PurchaserFinancingSettings();
        settings.setPurchaser(purchaser);
        settings.setAnnualRateInBps(6);
        settings.setCreditor(creditor);
        // This rate is higher than the creditor's max rate

        List<PurchaserFinancingSettings> eligiblePurchasers = Arrays.asList(settings);

        long financingTermInDays = 11;

        // Act
        PurchaserFinancingSettings bestPurchaser = purchaserSelectionService.selectBestPurchaser(eligiblePurchasers, creditor, financingTermInDays);

        // Assert
        assertNull(bestPurchaser);
    }

    @Test
    public void testSelectBestPurchaserWithNoEligiblePurchasersDueToTerm() {
        // Arrange
        Creditor creditor = new Creditor();
        creditor.setMaxFinancingRateInBps(500);

        Purchaser purchaser = new Purchaser();
        purchaser.setMinimumFinancingTermInDays(20); // Minimum term is greater than financing term

        PurchaserFinancingSettings settings = new PurchaserFinancingSettings();
        settings.setPurchaser(purchaser);
        settings.setAnnualRateInBps(100);

        List<PurchaserFinancingSettings> eligiblePurchasers = Arrays.asList(settings);

        long financingTermInDays = 15;

        // Act
        PurchaserFinancingSettings bestPurchaser = purchaserSelectionService.selectBestPurchaser(eligiblePurchasers, creditor, financingTermInDays);

        // Assert
        assertNull(bestPurchaser);
    }

    @Test
    public void testSelectBestPurchaserWithMultipleEligiblePurchasers() {
        // Arrange
        Creditor creditor = new Creditor();
        creditor.setMaxFinancingRateInBps(500);

        Purchaser purchaser1 = new Purchaser();
        purchaser1.setMinimumFinancingTermInDays(10);

        Purchaser purchaser2 = new Purchaser();
        purchaser2.setMinimumFinancingTermInDays(5);

        Purchaser purchaser3 = new Purchaser();
        purchaser3.setMinimumFinancingTermInDays(7);

        PurchaserFinancingSettings settings1 = new PurchaserFinancingSettings();
        settings1.setPurchaser(purchaser1);
        settings1.setAnnualRateInBps(100);

        PurchaserFinancingSettings settings2 = new PurchaserFinancingSettings();
        settings2.setPurchaser(purchaser2);
        settings2.setAnnualRateInBps(50);

        PurchaserFinancingSettings settings3 = new PurchaserFinancingSettings();
        settings3.setPurchaser(purchaser3);
        settings3.setAnnualRateInBps(70);

        List<PurchaserFinancingSettings> eligiblePurchasers = Arrays.asList(settings1, settings2, settings3);

        long financingTermInDays = 15;

        // Act
        PurchaserFinancingSettings bestPurchaser = purchaserSelectionService.selectBestPurchaser(eligiblePurchasers, creditor, financingTermInDays);

        // Assert
        assertEquals(settings2, bestPurchaser);
    }
}