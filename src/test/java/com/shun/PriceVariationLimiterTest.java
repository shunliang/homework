package com.shun;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

public class PriceVariationLimiterTest {

    private static Connection connection = null;

    @BeforeAll
    static void setup() {
        // setup the common resource(s) for all tests
        connection = DatabaseQueryUtil.getConnection();
    }

    @AfterAll
    static void tearDown() {
        // close the common resource(s) for all tests
    }

    @Test
    void testCalculateTickCount_expect_equal() {

        TreeMap<Float, Float> tickSize = DatabaseQueryUtil.getTickSize("KS200400F5.KS");

        float calculateTickCount = PriceVariationLimiter.calculateTickCount(tickSize, 8.72f, 8.81f);
        float expectedTickCount = 9f;
        assertEquals(calculateTickCount, expectedTickCount);

        calculateTickCount = PriceVariationLimiter.calculateTickCount(tickSize, 10.10f, 9.93f);
        expectedTickCount = 9f;
        assertEquals(calculateTickCount, expectedTickCount);

        calculateTickCount = PriceVariationLimiter.calculateTickCount(tickSize, 10.20f, 9.95f);
        expectedTickCount = 9f;
        assertEquals(calculateTickCount, expectedTickCount);

        calculateTickCount = PriceVariationLimiter.calculateTickCount(tickSize, 9.87f, 9.95f);
        expectedTickCount = 8f;
        assertEquals(calculateTickCount, expectedTickCount);
    }

    @Test
    void testCalculatePercentage_expect_equal() {

        float queriedPercentage = PriceVariationLimiter.calculatePercentage(300f, 230f);
        float expectedPercentage = 0.3043478f;

        assertEquals(queriedPercentage, expectedPercentage);

    }

    @Test
    void testCalculateAbsoluteValue_expect_equal() {

        float queriedAbsoluteValue = PriceVariationLimiter.calculateAbsoluteValue(300f, 230f);
        float expectedAbsoluteValue = 70f;

        assertEquals(queriedAbsoluteValue, expectedAbsoluteValue);
    }

    @Test
    void testEvaluate_expect_true() {

        DatabaseQueryUtil.updateReferencePrice("VOD.L", 245f);

        Order order = new Order(1, "VOD.L", "Buy", 245f, 1);
        EvaluationResult result = PriceVariationLimiter.evaluate(order);
        assertFalse(result.isAlertActivated());

        order = new Order(2, "VOD.L", "Buy", 255f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertTrue(result.isAlertActivated());

        order = new Order(3, "VOD.L", "Buy", 265f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertTrue(result.isAlertActivated());

        order = new Order(4, "VOD.L", "Sell", 245f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertFalse(result.isAlertActivated());

        order = new Order(5, "VOD.L", "Sell", 235f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertTrue(result.isAlertActivated());

        order = new Order(6, "VOD.L", "Sell", 225f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertTrue(result.isAlertActivated());
    }

    @Test
    void testEvaluateComplicated_expect_true() {
// reference price 8.81
        DatabaseQueryUtil.updateReferencePrice("KS200400F5.KS", 8.81f);

        Order order = new Order(0, "KS200400F5.KS", "Buy", 8.81f, 1);
        EvaluationResult result = PriceVariationLimiter.evaluate(order);
        assertFalse(result.isAlertActivated());

        order = new Order(1, "KS200400F5.KS", "Buy", 8.72f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertTrue(result.isAlertActivated());

        order = new Order(2, "KS200400F5.KS", "Buy", 8.90f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertFalse(result.isAlertActivated());

        // reference price 8.91
        DatabaseQueryUtil.updateReferencePrice("KS200400F5.KS", 8.91f);

        order = new Order(3, "KS200400F5.KS", "Sell", 8.92f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertFalse(result.isAlertActivated());

        order = new Order(4, "KS200400F5.KS", "Sell", 8.82f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertFalse(result.isAlertActivated());

        order = new Order(5, "KS200400F5.KS", "Sell", 9.00f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertTrue(result.isAlertActivated());

        // reference price 9.93
        DatabaseQueryUtil.updateReferencePrice("KS200400F5.KS", 9.93f);

        order = new Order(6, "KS200400F5.KS", "Buy", 9.94f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertFalse(result.isAlertActivated());

        order = new Order(7, "KS200400F5.KS", "Buy", 9.84f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertTrue(result.isAlertActivated());

        order = new Order(8, "KS200400F5.KS", "Buy", 10.10f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertFalse(result.isAlertActivated());

        // reference price 9.95
        DatabaseQueryUtil.updateReferencePrice("KS200400F5.KS", 9.95f);

        order = new Order(9, "KS200400F5.KS", "Sell", 9.94f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertFalse(result.isAlertActivated());

        order = new Order(10, "KS200400F5.KS", "Sell", 9.87f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertFalse(result.isAlertActivated());

        order = new Order(11, "KS200400F5.KS", "Sell", 10.20f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertTrue(result.isAlertActivated());

        // reference price 10.15
        DatabaseQueryUtil.updateReferencePrice("KS200400F5.KS", 10.15f);

        order = new Order(12, "KS200400F5.KS", "Buy", 10.10f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertFalse(result.isAlertActivated());

        order = new Order(13, "KS200400F5.KS", "Buy", 9.94f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertTrue(result.isAlertActivated());

        order = new Order(14, "KS200400F5.KS", "Buy", 10.60f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertFalse(result.isAlertActivated());

        // reference price 10.25
        DatabaseQueryUtil.updateReferencePrice("KS200400F5.KS", 10.25f);

        order = new Order(15, "KS200400F5.KS", "Sell", 10.30f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertFalse(result.isAlertActivated());

        order = new Order(16, "KS200400F5.KS", "Sell", 9.96f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertFalse(result.isAlertActivated());

        order = new Order(17, "KS200400F5.KS", "Sell", 10.70f, 1);
        result = PriceVariationLimiter.evaluate(order);
        assertTrue(result.isAlertActivated());
    }

}
