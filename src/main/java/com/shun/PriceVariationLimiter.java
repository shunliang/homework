package com.shun;


import jdk.xml.internal.XMLSecurityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Math.max;
import static java.lang.Math.min;

/** CAUTION: We made an assumption here:
* We assume ONLY handle the ABSOLUTE value of the variations.
* If we want to be VERY SPECIFIC on whether the variation value is positive or negative,
* We need to have a clear definition of variation on the BUY/SELL, AT_ADVANTGE/AT_DISADVANTAGE and order price/reference price.
*/

public class PriceVariationLimiter {

    private static final Logger logger = LogManager.getLogger(PriceVariationLimiter.class);

    /*
TreeMap tickSize is actually a series of range, each range has a corresponding tick_size.
We will loop through the TreeMap, check each Entry, and calculate the totalTickCount.
The tick count within each range (i.e. each Entry), depends on the relative position of:
    the range, the orderPrice and the referencePrice.
 */
    // NOTE: to convert from float to int, use Math.round().
    // Do NOT use (int)(float expression).
    public static int calculateTickCount(TreeMap<Float, Float> tickSize, Float orderPrice, Float referencePrice) {
        int totalTickCount = 0;

        if (tickSize == null || referencePrice == Float.NEGATIVE_INFINITY) {
            return totalTickCount;
        }

        // For easy comparison with the tickSize table,
        // we use priceLower to refer the one with lower value,
        // we use priceHigher to refer to the one with higher value.
        Float priceLower = min(orderPrice, referencePrice);
        Float priceHigher = max(orderPrice, referencePrice);

        Iterator<Map.Entry<Float, Float> > iterator = tickSize.entrySet().iterator();
        Map.Entry<Float, Float> currentEntry = null;

        // loop through each entry
        while (iterator.hasNext()) {
            currentEntry = iterator.next();

            // get the left value (floorKey) and right value (higherKey) of the range represented by currentEntry
            Float floorKey = tickSize.floorKey(currentEntry.getKey());
            // NOTE: higherKey might be null
            // When currentEntry is the last entry of the TreeMap, higherKey will be null.
            Float higherKey = tickSize.higherKey(currentEntry.getKey());

            Float currentTickSize = currentEntry.getValue();

            // handle the last entry of the TreeMap (i.e. higherKey is null)
            // i.e. there is no RIGHT end for the range
            if (higherKey == null) {
                // priceHigher is in the last entry
                if (priceHigher >= floorKey) {

                    // priceLower is also in the last entry
                    /*
                            |
                    ________|________________________________________________________
                            |              |                    |
                        floorKey       priceLower           priceHigher
                    */
                    if (priceLower >= floorKey) {
                        int currentTickCount = Math.round((priceHigher - priceLower) / currentTickSize);
                        totalTickCount += currentTickCount;
                        logger.debug("A currentTickCount={}, totalTickCount={}", currentTickCount, totalTickCount);
                        logger.debug("A floorKey={}, higherKey={}, priceLower={}, priceHigher={}, currentTickSize={}",
                                floorKey, higherKey, priceLower, priceHigher, currentTickSize);
                    }
                    // priceLower is NOT in the last entry
                    /*
                                      |
                    __________________|________________________________________________________
                         |            |                          |
                    priceLower     floorKey                   priceHigher
                    */
                    else {
                        int currentTickCount = Math.round((priceHigher - floorKey) / currentTickSize);
                        totalTickCount += currentTickCount;
                        logger.debug("B currentTickCount={}, totalTickCount={}", currentTickCount, totalTickCount);
                        logger.debug("B floorKey={}, higherKey={}, priceLower={}, priceHigher={}, currentTickSize={}",
                                floorKey, higherKey, priceLower, priceHigher, currentTickSize);
                    }
                }
                // priceHigher is NOT in last entry,
                // which also means, priceLower is NOT in last entry
                else {
                    // do nothing
                    logger.debug("C floorKey={}, higherKey={}, priceLower={}, priceHigher={}, currentTickSize={}",
                            floorKey, higherKey, priceLower, priceHigher, currentTickSize);
                }

                // NO need to execute the remaining codes of the while loop
                continue;
            } // if (higherKey == null)

            // handle other entries of the TreeMap
            // i.e. there are left end and right end of the range

            // priceLower (and priceHigher) is NOT in this entry
            /*
                |                        |
            ____|________________________|________________________________
                |                        |              |
             floorKey               higherKey       priceLower
            */
            // NOTE: there is a "continue" in line 202, which will skip these lines, if higherKey == null
            if (priceLower >= higherKey ) {
                // do nothing
                logger.debug("D floorKey={}, higherKey={}, priceLower={}, priceHigher={}, currentTickSize={}",
                        floorKey, higherKey, priceLower, priceHigher, currentTickSize);
            }
            // priceLower is NOT in this range
            else if (priceLower < floorKey) {

                // priceHigher is NOT in this entry
                /*
                                                  |                        |
                __________________________________|________________________|_____
                     |              |             |                        |
                priceLower     priceHigher   floorKey                higherKey
                */
                if (priceHigher < floorKey) {
                    // do nothing
                    logger.debug("E floorKey={}, higherKey={}, priceLower={}, priceHigher={}, currentTickSize={}",
                            floorKey, higherKey, priceLower, priceHigher, currentTickSize);
                }
                // priceHigher is IN this range
                /*
                                           |                        |
                ___________________________|________________________|_____
                     |                     |          |             |
                priceLower             floorKey  priceHigher   higherKey
                */
                else if (priceHigher < higherKey) {
                    int currentTickCount = Math.round((priceHigher - floorKey) / currentTickSize);
                    totalTickCount += currentTickCount;
                    logger.debug("F currentTickCount={}, totalTickCount={}", currentTickCount, totalTickCount);
                    logger.debug("F floorKey={}, higherKey={}, priceLower={}, priceHigher={}, currentTickSize={}",
                            floorKey, higherKey, priceLower, priceHigher, currentTickSize);
                }
                // priceHigher is HIGHER than this range
                /*
                                           |                        |
                ___________________________|________________________|__________________
                     |                     |                        |         |
                priceLower             floorKey               higherKey  priceHigher
                */
                else {
                    int currentTickCount = Math.round((higherKey - floorKey) / currentTickSize);
                    totalTickCount += currentTickCount;
                    logger.debug("G currentTickCount={}, totalTickCount={}", currentTickCount, totalTickCount);
                    logger.debug("G floorKey={}, higherKey={}, priceLower={}, priceHigher={}, currentTickSize={}",
                            floorKey, higherKey, priceLower, priceHigher, currentTickSize);
                }
            } // if (priceLower < floorKey)
            // priceLower is IN this range
            else {
                // priceHigher is ALSO IN this range
                /*
                        |                                     |
                ________|_____________________________________|__________
                        |          |            |             |
                   floorKey   priceLower   priceHigher   higherKey
                */
                if (priceHigher < higherKey) {
                    int currentTickCount = Math.round((priceHigher - priceLower) / currentTickSize);
                    totalTickCount += currentTickCount;
                    logger.debug("H currentTickCount={}, totalTickCount={}", currentTickCount, totalTickCount);
                    logger.debug("H floorKey={}, higherKey={}, priceLower={}, priceHigher={}, currentTickSize={}",
                            floorKey, higherKey, priceLower, priceHigher, currentTickSize);
                }
                // priceHigher is NOT in this range
                /*
                        |                                     |
                ________|_____________________________________|______________
                        |          |                          |          |
                   floorKey   priceLower                 higherKey  priceHigher
                */
                // (priceHigher >= higherKey)
                else {
                    int currentTickCount = Math.round((higherKey - priceLower) / currentTickSize);
                    totalTickCount += currentTickCount;
                    logger.debug("I currentTickCount={}, totalTickCount={}", currentTickCount, totalTickCount);
                    logger.debug("I floorKey={}, higherKey={}, priceLower={}, priceHigher={}, currentTickSize={}",
                            floorKey, higherKey, priceLower, priceHigher, currentTickSize);
                }
            } // priceLower is IN this range

        } // while

        return totalTickCount;
    }

    public static float calculatePercentage(Float orderPrice, Float referencePrice) {
        if (orderPrice == null || referencePrice == null || referencePrice == 0.0f) {
            return 0;
        }

        return Math.abs(orderPrice - referencePrice) / referencePrice;
    }

    public static float calculateAbsoluteValue(Float orderPrice, Float referencePrice) {
        if (orderPrice == null || referencePrice == null) {
            return 0;
        }

        return Math.abs(orderPrice - referencePrice);
    }

    public static EvaluationResult evaluate(Order order) {
        EvaluationResult evaluationResult = null;

        if (order == null) {
            return evaluationResult;
        }

        String instrument = order.getInstrument();
        Connection connection = DatabaseQueryUtil.getConnection();

        float referencePrice = DatabaseQueryUtil.getReferencePrice(instrument);
        logger.debug("instrument={}, referencePrice={}", instrument, referencePrice);

        if (referencePrice == Float.NEGATIVE_INFINITY) {
            logger.warn("instrument={}, reference price is invalid", instrument);

            String description = "invalid reference price";
            evaluationResult = new EvaluationResult(true,  description);
            logger.debug("A evaluationResult={}", evaluationResult);

            return evaluationResult;
        }

        LimitRule limitRule = DatabaseQueryUtil.getLimitRule(instrument);

        // handle the variation
        float currentValue = 0;
        float variationLimit = limitRule.getVariationLimit();
        if (limitRule.getVariationType() == VariationType.BY_PERCENTAGE) {
            currentValue = PriceVariationLimiter.calculatePercentage(order.getPrice(), referencePrice);
        } else if (limitRule.getVariationType() == VariationType.BY_ABSOLUTE_VALUE) {
            currentValue = PriceVariationLimiter.calculateAbsoluteValue(order.getPrice(), referencePrice);
        } else if (limitRule.getVariationType() == VariationType.BY_TICK_SIZE) {
            TreeMap<Float, Float> tickSize = DatabaseQueryUtil.getTickSize(instrument);
            currentValue = PriceVariationLimiter.calculateTickCount(tickSize, order.getPrice(), referencePrice);
        }

        logger.debug("currentValue={}", currentValue);

        if (Math.abs(currentValue) >= variationLimit) {

            switch (limitRule.getValidationScenarioType()) {
                case ONLY_AT_ADVANTAGE:

                    // when ONLY_AT_ADVANTAGE, skip those disadvantage scenarios
                    if (order.getSide().toUpperCase().equals("BUY") && order.getPrice() > referencePrice)
                    {
                        String description = "buy higher, pass";
                        evaluationResult = new EvaluationResult(false, description);
                        logger.debug("B evaluationResult={}", evaluationResult);

                        return evaluationResult;
                    }

                    if (order.getSide().toUpperCase().equals("SELL") && order.getPrice() < referencePrice)
                    {
                        String description = "sell lower, pass";
                        evaluationResult = new EvaluationResult(false, description);
                        logger.debug("C evaluationResult={}", evaluationResult);

                        return evaluationResult;
                    }

                    // Now handle the variation
                    // We will handle the variation in later codes

                    break;
                case ONLY_AT_DISADVANTAGE:

                    // when ONLY_AT_DISADVANTAGE, skip those advantage scenarios
                    if (order.getSide().toUpperCase().equals("BUY") && order.getPrice() < referencePrice)
                    {
                        String description = "buy lower, pass";
                        evaluationResult = new EvaluationResult(false, description);
                        logger.debug("D evaluationResult={}", evaluationResult);

                        return evaluationResult;
                    }

                    if (order.getSide().toUpperCase().equals("SELL") && order.getPrice() > referencePrice)
                    {
                        String description = "sell higher, pass";
                        evaluationResult = new EvaluationResult(false, description);
                        logger.debug("E evaluationResult={}", evaluationResult);

                        return evaluationResult;
                    }

                    // Now handle the variation
                    // We will handle the variation in later codes

                    break;
                case BOTH:

                    // Nothing to return directly
                    // Now handle the variation
                    // We will handle the variation in later codes

                    break;
                default:
                    logger.warn("invalid validation scenario type : {}", limitRule.getValidationScenarioType());
            }


            String description = String.format("%.2f >= %.2f, block", Math.abs(currentValue), variationLimit);
            evaluationResult = new EvaluationResult(true,  description);
            logger.debug("F evaluationResult={}", evaluationResult);
        } else {
            String description = String.format("%.2f < %.2f, pass", Math.abs(currentValue), variationLimit);
            evaluationResult = new EvaluationResult(false,  description);
            logger.debug("G evaluationResult={}", evaluationResult);
        }

        return evaluationResult;
    }


}
