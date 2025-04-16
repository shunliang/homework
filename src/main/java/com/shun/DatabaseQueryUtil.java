package com.shun;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class DatabaseQueryUtil {
    private static final Logger logger = LogManager.getLogger(DatabaseQueryUtil.class);

    private static Connection connectionInstance = null;

    private static Connection connectToDatabase(Map<String, String> connectionProperties) {
        Connection connection = null;

        try {
            // load the driver
            Class.forName("org.postgresql.Driver");

            // TODO: add default values for each property, if the key is not found

            if (!connectionProperties.containsKey("host") ||
                    !connectionProperties.containsKey("port") ||
                    !connectionProperties.containsKey("dbname") ||
                    !connectionProperties.containsKey("user") ||
                    !connectionProperties.containsKey("password") ) {
                logger.warn("Required database properties NOT found.");
            }

            // "jdbc:postgresql://127.0.0.1:5432/trading"
            String connectionUrl=String.format("jdbc:postgresql://%s:%s/%s",
                    connectionProperties.get("host"),
                    connectionProperties.get("port"),
                    connectionProperties.get("dbname"));

            // Connect to the PostgreSQL database
            connection = DriverManager.getConnection(connectionUrl, connectionProperties.get("user"), connectionProperties.get("password"));

            if (connection != null) {
                logger.info("Successfully connected to PostgreSQL server.");
            } else {
                logger.error("Failed to connect to PostgreSQL server.");
            }
        }
        catch (SQLException | ClassNotFoundException e) {
            logger.debug("Exception: {}", e.getMessage());
        }

        return connection;
    }

    public static synchronized Connection getConnection()
    {
        if (connectionInstance == null) {

            String propertiesFileName = "database.properties";
            Map<String, String> connectionProperties = Util.readPropertiesToMap(propertiesFileName);
            connectionInstance = DatabaseQueryUtil.connectToDatabase(connectionProperties);
        }

        return connectionInstance;
    }

    public static TreeMap<Float, Float> getTickSize(String instrument) {

        TreeMap<Float, Float> resultMap = new TreeMap<>();

        if (instrument == null || instrument.trim().isEmpty()) {
            logger.warn("instrument is null or empty");
            return resultMap;
        }

        Connection connection = DatabaseQueryUtil.getConnection();

        try {
            // NOTE: do NOT use "SELECT *", in case the table schema is changed.
            PreparedStatement statement = connection.prepareStatement("SELECT min, max, tick_size FROM tick_table where instrument= ?;");
            statement.setString(1, instrument);

            ResultSet queryResult = statement.executeQuery();

            while (queryResult.next()) {
                float min = queryResult.getFloat("min");
                float max = queryResult.getFloat("max");
                float tick_size = queryResult.getFloat("tick_size");
                resultMap.put(min, tick_size);

                logger.debug("instrument={}, min={},max={}, tick_size={}", instrument, min, max, tick_size);
            }
        }
        catch (SQLException e) {
            logger.debug("Exception:{}", e.getMessage());
        }

        return resultMap;
    }

    public static float getReferencePrice(String instrument) {

        float referencePrice = 0f;

        if (instrument == null || instrument.trim().isEmpty()) {
            logger.warn("instrument is null or empty");
            return referencePrice;
        }

        Connection connection = DatabaseQueryUtil.getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT COALESCE(last_trade_price, close_price, theo_price, null) as reference_price FROM reference_price where instrument= ?;");
            statement.setString(1, instrument);

            ResultSet queryResult = statement.executeQuery();

            if (queryResult.next()) {
                referencePrice = queryResult.getFloat("reference_price");

                logger.debug("A instrument={}, reference_price={}", instrument, referencePrice);
            }
        }
        catch (SQLException e) {
            logger.debug("Exception:{}", e.getMessage());
        }

        return referencePrice;
    }

    public static LimitRule getLimitRule(String instrument) {
        LimitRule limitRule = null;

        if (instrument == null || instrument.trim().isEmpty()) {
            logger.warn("instrument is null or empty");
            return limitRule;
        }

        Connection connection = DatabaseQueryUtil.getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT variation_type, validation_scenario_type, variation_limit FROM limit_rules where instrument= ?;");
            statement.setString(1, instrument);

            ResultSet queryResult = statement.executeQuery();

            // TODO: assume only one limitRule per instrument.
            // If we need to support multiple limitRules per instrument,
            //     change here to while, and return a List<LimitRule>
            if (queryResult.next()) {
                VariationType variationType = VariationType.valueOf(queryResult.getString("variation_type"));
                ValidationScenarioType validationScenarioType = ValidationScenarioType.valueOf(queryResult.getString("validation_scenario_type"));
                float variationLimit = queryResult.getFloat("variation_limit");

                limitRule = new LimitRule(instrument,variationType, validationScenarioType, variationLimit);

                logger.debug("instrument={}, limitRule={}", instrument, limitRule);
            }
        }
        catch (SQLException e) {
            logger.debug("Exception:{}", e.getMessage());
        }

        return limitRule;
    }

    public static void updateReferencePrice(String instrument, float referencePrice) {

        if (instrument == null || instrument.trim().isEmpty()) {
            logger.warn("instrument is null or empty");
        }

        Connection connection = DatabaseQueryUtil.getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE reference_price set last_trade_price=? where instrument= ?;");
            statement.setFloat(1, referencePrice);
            statement.setString(2, instrument);

            ResultSet queryResult = statement.executeQuery();

            // TODO: check return result to see whether success or not
            logger.debug("UPDATE reference price: instrument={}, reference_price={}", instrument, referencePrice);
        }
        catch (SQLException e) {
            logger.debug("Exception:{}", e.getMessage());
        }

    }
}
