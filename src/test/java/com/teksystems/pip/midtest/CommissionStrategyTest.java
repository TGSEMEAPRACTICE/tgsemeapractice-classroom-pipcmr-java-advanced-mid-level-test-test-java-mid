package com.teksystems.pip.midtest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CommissionStrategyTest {

    @Test
    void usdCommissionIsTwoPercent() {
        MidTestApplication.CommissionStrategy strategy = new MidTestApplication.USDCommissionStrategy();
        Double commission = strategy.calculateCommission(100.0);
        assertEquals(2.0, commission, 1e-9);
    }

    @Test
    void eurCommissionIsOnePercent() {
        MidTestApplication.CommissionStrategy strategy = new MidTestApplication.EURCommissionStrategy();
        Double commission = strategy.calculateCommission(200.0);
        assertEquals(2.0, commission, 1e-9);
    }

    @Test
    void defaultCommissionIsFivePercentAndHandlesNull() {
        MidTestApplication.CommissionStrategy strategy = new MidTestApplication.DefaultCommissionStrategy();
        Double commission = strategy.calculateCommission(50.0);
        assertEquals(2.5, commission, 1e-9);

        Double nullCommission = strategy.calculateCommission(null);
        assertEquals(0.0, nullCommission, 1e-9);
    }
}
