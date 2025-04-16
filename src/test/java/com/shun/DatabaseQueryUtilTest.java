package com.shun;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseQueryUtilTest {

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
    void testGetReferencePrice_expect_equal() {
        float referencePrice = DatabaseQueryUtil.getReferencePrice("TEST1");
        assertEquals(referencePrice, 25f);

        referencePrice = DatabaseQueryUtil.getReferencePrice("TEST2");
        assertEquals(referencePrice, 432f);

        referencePrice = DatabaseQueryUtil.getReferencePrice("TEST3");
        assertEquals(referencePrice, 0f);

        referencePrice = DatabaseQueryUtil.getReferencePrice("TEST4");
        assertEquals(referencePrice, 43f);

        referencePrice = DatabaseQueryUtil.getReferencePrice("TEST5");
        assertEquals(referencePrice, 43f);
    }

    @Test
    void testGetLimitRule_expect_equal() {
        LimitRule queriedLimitRule = DatabaseQueryUtil.getLimitRule("KS200400F5.KS");

        LimitRule expectedLimitRule = new LimitRule("KS200400F5.KS",
                VariationType.BY_TICK_SIZE, ValidationScenarioType.ONLY_AT_ADVANTAGE, 8);

        assertEquals(queriedLimitRule.getInstrument(), expectedLimitRule.getInstrument());
        assertEquals(queriedLimitRule.getVariationType(), expectedLimitRule.getVariationType());
        assertEquals(queriedLimitRule.getValidationScenarioType(), expectedLimitRule.getValidationScenarioType());
        assertEquals(queriedLimitRule.getVariationLimit(), expectedLimitRule.getVariationLimit());
    }

    @Test
    void testGetTickSize_expect_equal(){
        TreeMap<Float, Float> queriedTickSize = DatabaseQueryUtil.getTickSize("Apple");

        TreeMap<Float, Float> expectedTickSize = new TreeMap<>();
        expectedTickSize.put(0f, 1f);
        expectedTickSize.put(20f, 5f);
        expectedTickSize.put(100f, 10f);

        assertEquals(queriedTickSize, expectedTickSize);


        queriedTickSize = DatabaseQueryUtil.getTickSize("KS200400F5.KS");

        expectedTickSize = new TreeMap<>();
        expectedTickSize.put(0f, 0.01f);
        expectedTickSize.put(10f, 0.05f);

        assertEquals(queriedTickSize, expectedTickSize);
    }
}
