/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Tyler Bucher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.reallifegames.sdeconomy;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages universal sql query's for multiple plugin APIs.
 *
 * @author Tyler Bucher
 */
public final class SqlService {

    /**
     * The current sql version of this plugin.
     */
    public static final int SQL_VERSION = 6;

    /**
     * Checks to see if a table exists.
     */
    @Nonnull
    private static final String TABLES_EXITS = "SELECT EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?);";

    /**
     * Checks to see if a foreign key exists.
     */
    @Nonnull
    private static final String FK_EXITS = "SELECT EXISTS(SELECT `constraint_name` FROM `information_schema`.`referential_constraints` " +
            "WHERE `constraint_name` = ?);";

    /**
     * Gets the type of a column.
     */
    @Nonnull
    private static final String GET_COLUMN_TYPE = "SELECT `DATA_TYPE` FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE " +
            "TABLE_NAME=? AND COLUMN_NAME=?;";

    /**
     * The {@link DefaultProduct product} table creation sql query string.
     */
    @Nonnull
    private static final String DEFAULT_PRODUCT_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `sd_products` (`id` int(11) NOT NULL " +
            "AUTO_INCREMENT, `alias` VARCHAR(255) NOT NULL, `type` VARCHAR(255) NOT NULL, `unsafe_data` TINYINT(4) " +
            "DEFAULT '0', `mod_factor` FLOAT NOT NULL DEFAULT '0.1', `price` FLOAT NOT NULL, `supply` INT NOT NULL, " +
            "`demand` INT NOT NULL, `decay_amount` INT NOT NULL DEFAULT '64', PRIMARY KEY (`id`), `decay_interval` " +
            "BIGINT NOT NULL DEFAULT '43200000', `decay_type` TINYINT NOT NULL DEFAULT '0', UNIQUE KEY `alias_2` " +
            "(`alias`), KEY `alias` (`alias`)) ENGINE = InnoDB;";

    /**
     * The {@link DefaultProduct product} table insert and update sql query string.
     */
    @Nonnull
    private static final String INSERT_UPDATE_DEFAULT_PRODUCT_TABLE_SQL = "INSERT INTO `sd_products`(`alias`, `type`, " +
            "`unsafe_data`, `mod_factor`, `price`, `supply`, `demand`, `decay_amount`, `decay_interval`, `decay_type`) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE `type`=?,`unsafe_data`=?,`mod_factor`=?,`price`=?," +
            "`supply`=?,`demand`=?,`decay_amount`=?,`decay_interval`=?,`decay_type`=?;";

    /**
     * The {@link DefaultProduct product} table select sql query string.
     */
    @Nonnull
    private static final String READ_DEFAULT_PRODUCT_TABLE_SQL = "SELECT * FROM `sd_products`";

    /**
     * The {@link DefaultProduct product} table delete sql query string.
     */
    @Nonnull
    private static final String DELETE_DEFAULT_PRODUCT_TABLE_SQL = "DELETE FROM `sd_products` WHERE `alias` = ?;";

    /**
     * The set price action for the {@link DefaultProduct default product} transaction table.
     */
    public static final byte DECAY_CONST_TYPE = 0;

    /**
     * The buy action for the {@link DefaultProduct default product} transaction table.
     */
    public static final byte DECAY_PERCENTAGE_TYPE = 1;

    /**
     * The {@link DefaultProduct default product} transaction table.
     */
    @Nonnull
    private static final String DEFAULT_TRANSACTION_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `sd_transaction` (`uuid_id` INT " +
            "NOT NULL,`action` tinyint(4) NOT NULL,`price_id` int(11) NOT NULL,`date` timestamp NOT NULL DEFAULT " +
            "CURRENT_TIMESTAMP,`amount` float NOT NULL, `money_exchanged` double NOT NULL DEFAULT '0', KEY `uuid_id` " +
            "(`uuid_id`), KEY `action` (`action`), KEY `price_id` (`price_id`), KEY `date` (`date`)) ENGINE=InnoDB " +
            "DEFAULT CHARSET=latin1;";

    /**
     * The {@link DefaultProduct default product} transaction table fk sql.
     */
    @Nonnull
    private static final String DEFAULT_TRANSACTION_TABLE_FK_PRICE_SQL = "ALTER TABLE `sd_transaction` ADD CONSTRAINT `fk_price_id` " +
            "FOREIGN KEY (`price_id`) REFERENCES `sd_products` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;";

    /**
     * The {@link DefaultProduct default product} transaction table fk sql.
     */
    @Nonnull
    private static final String DEFAULT_TRANSACTION_TABLE_FK_UUID_SQL = "ALTER TABLE `sd_transaction` ADD CONSTRAINT `fk_uuid_id` " +
            "FOREIGN KEY (`uuid_id`) REFERENCES `sd_uuid`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;";

    /**
     * The {@link DefaultProduct default product} transaction table insert sql query string.
     */
    @Nonnull
    public static final String INSERT_DEFAULT_TRANSACTION_TABLE_SQL = "INSERT INTO `sd_transaction`(`uuid_id`, `action`, " +
            "`price_id`, `amount`, `money_exchanged`) VALUES ((SELECT `id` FROM `sd_uuid` WHERE `uuid`=?),?," +
            "(SELECT `id` FROM `sd_products` WHERE LOWER(`alias`)=?),?,?);";

    /**
     * The set price action for the {@link DefaultProduct default product} transaction table.
     */
    public static final byte SET_PRICE_ACTION = 0;

    /**
     * The buy action for the {@link DefaultProduct default product} transaction table.
     */
    public static final byte BUY_ACTION = 1;

    /**
     * The sell action for the {@link DefaultProduct default product} transaction table.
     */
    public static final byte SELL_ACTION = 2;

    /**
     * The demand decay action for the {@link DefaultProduct default product} transaction table.
     */
    public static final byte DECAY_ACTION = 3;

    /**
     * The set mod factor action for the {@link DefaultProduct default product} transaction table.
     */
    public static final byte SET_MOD_FACTOR_ACTION = 4;

    /**
     * The uuid of the system. Used in {@link DefaultProduct default product} transaction table.
     */
    @Nonnull
    public static final String SYSTEM_UUID = "00000000-0000-0000-0000-000000000000";

    /**
     * The {@link DefaultProduct default product} table creation sql query string.
     */
    @Nonnull
    private static final String CONSTANTS_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `sd_constants` (`kkey` VARCHAR(255) NOT " +
            "NULL, `value` VARCHAR(255) NOT NULL, PRIMARY KEY (`kkey`)) ENGINE = InnoDB;";

    /**
     * The sql version constant.
     */
    @Nonnull
    public static final String SQL_VERSION_CONSTANT = "sql_version";

    /**
     * The constants table select sql version query string.
     */
    @Nonnull
    private static final String SELECT_SQL_VERSION = "SELECT `value` FROM `sd_constants` WHERE `kkey`=?;";

    /**
     * The insert update sql version query string.
     */
    @Nonnull
    private static final String INSERT_UPDATE_SQL_VERSION = "INSERT INTO `sd_constants`(`kkey`, `value`) VALUES (?,?) " +
            "ON DUPLICATE KEY UPDATE `kkey` = `kkey`;";

    /**
     * Searches for keys in an array.
     */
    @Nonnull
    private static final String SEARCH_KEYS_IN_CONSTANTS = "SELECT `kkey`, `value` FROM `sd_constants` WHERE `kkey` IN ";

    /**
     * Searches for a users {@link DefaultProduct default product} transactions.
     */
    @Nonnull
    public static final String SEARCH_DEFAULT_USERS_TRANSACTIONS = "SELECT `sd_transaction`.`action`, `sd_products`.`alias`, " +
            "`sd_transaction`.`date`, `sd_transaction`.`amount`, `sd_transaction`.`money_exchanged` FROM `sd_transaction` INNER JOIN `sd_products` ON " +
            "`sd_products`.`id`=`sd_transaction`.`price_id` INNER JOIN `sd_uuid` ON `sd_uuid`.`id`=`sd_transaction`." +
            "`uuid_id` WHERE `sd_uuid`.`uuid`=? ORDER BY `sd_transaction`.`date` DESC LIMIT ?,20;";

    @Nonnull
    private static final String UUID_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `sd_uuid` ( `id` INT NOT NULL AUTO_INCREMENT " +
            ", `uuid` CHAR(36) NOT NULL , PRIMARY KEY (`id`), UNIQUE KEY `uuid` (`uuid`)) ENGINE = InnoDB;";

    /**
     * Checks to see if all of the tables passed exist in the database.
     *
     * @param jdbcUrl the url of the database.
     * @param tables  the table names to check.
     * @return true if all tables are present false otherwise.
     *
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    @SuppressWarnings ("Duplicates")
    private static boolean tablesExits(@Nonnull final String jdbcUrl, @Nonnull final String... tables) throws SQLException {
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        for (String tableName : tables) {
            final PreparedStatement preparedStatement = sqlConnection.prepareStatement(TABLES_EXITS);
            preparedStatement.setString(1, tableName);
            final ResultSet results = preparedStatement.executeQuery();
            if (!results.next() || !results.getBoolean(1))
                return false;
        }
        sqlConnection.close();
        return true;
    }

    /**
     * Checks to see if all of the foreign keys passed exist in the database.
     *
     * @param jdbcUrl the url of the database.
     * @param keys    the foreign key names to check.
     * @return true if all tables are present false otherwise.
     *
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    @SuppressWarnings ("Duplicates")
    private static boolean foreignKeysExits(@Nonnull final String jdbcUrl, @Nonnull final String... keys) throws SQLException {
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        for (String keyName : keys) {
            final PreparedStatement preparedStatement = sqlConnection.prepareStatement(FK_EXITS);
            preparedStatement.setString(1, keyName);
            final ResultSet results = preparedStatement.executeQuery();
            if (!results.next() || !results.getBoolean(1))
                return false;
        }
        sqlConnection.close();
        return true;
    }

    /**
     * Updates the database from version 1 to version 2.
     *
     * @param jdbcUrl the url of the database.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void updateToSqlV2(@Nonnull final String jdbcUrl) throws SQLException {
        if (tablesExits(jdbcUrl, "sd_prices", "sd_transaction") && !tablesExits(jdbcUrl, "sd_constants")) {
            final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
            // sd_price table
            sqlConnection.prepareStatement("ALTER TABLE `sd_prices` DROP PRIMARY KEY;").execute();
            sqlConnection.prepareStatement("ALTER TABLE `sd_prices` ADD `id` INT NOT NULL AUTO_INCREMENT FIRST, ADD " +
                    "PRIMARY KEY (`id`);").execute();
            sqlConnection.prepareStatement("ALTER TABLE `sd_prices` ADD INDEX(`alias`);").execute();
            sqlConnection.prepareStatement("ALTER TABLE `sd_prices` ADD UNIQUE(`alias`);").execute();
            // sd_transaction table
            sqlConnection.prepareStatement("ALTER TABLE `sd_transaction` ADD `price_id` INT NOT NULL AFTER `action`;").execute();
            sqlConnection.prepareStatement("UPDATE `sd_transaction` RIGHT JOIN `sd_prices` ON `sd_transaction`.`type` = " +
                    "`sd_prices`.`name` AND `sd_transaction`.`unsafeData` = `sd_prices`.`unsafeData` SET `sd_transaction`." +
                    "`price_id` = `sd_prices`.`id`;").execute();
            sqlConnection.prepareStatement("ALTER TABLE `sd_transaction` DROP `type`;").execute();
            sqlConnection.prepareStatement("ALTER TABLE `sd_transaction` DROP `unsafeData`;").execute();
            sqlConnection.prepareStatement("ALTER TABLE `sd_transaction` ADD INDEX(`price_id`);").execute();
            sqlConnection.prepareStatement("DELETE FROM `sd_transaction` WHERE `price_id` = 0;").execute();
            sqlConnection.prepareStatement("ALTER TABLE `sd_transaction` ADD CONSTRAINT `fk_price_id` FOREIGN KEY " +
                    "(`price_id`) REFERENCES `sd_prices`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;").execute();
            // Close objects
            sqlConnection.close();
        }
    }

    /**
     * Updates the database from version 2 to version 3.
     *
     * @param jdbcUrl the url of the database.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void updateToSqlV3(@Nonnull final String jdbcUrl) throws SQLException {
        if (tablesExits(jdbcUrl, "sd_constants") &&
                getSqlType(jdbcUrl, "sd_constants", "kkey").equalsIgnoreCase("int")) {
            final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
            // sd_constants table
            sqlConnection.prepareStatement("TRUNCATE `sd_constants`").execute();
            sqlConnection.prepareStatement("ALTER TABLE `sd_constants` CHANGE `kkey` `kkey` VARCHAR(255) NOT NULL;").execute();
            // Close objects
            sqlConnection.close();
        }
    }

    /**
     * Updates the database from version 3 to version 4.
     *
     * @param jdbcUrl the url of the database.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void updateToSqlV4(@Nonnull final String jdbcUrl) throws SQLException {
        final int sqlVersion = getSqlVersion(jdbcUrl);
        if (sqlVersion == 3 || sqlVersion == -1) {
            final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
            // sd_price table
            sqlConnection.prepareStatement("DROP INDEX `name` ON `sd_prices`;").execute();
            sqlConnection.prepareStatement("UPDATE `sd_prices` SET `alias`=LOWER(`alias`);").execute();
            // sd_transaction table
            sqlConnection.prepareStatement("ALTER TABLE `sd_transaction` ADD INDEX(`date`);").execute();
            // Update sql version
            final PreparedStatement updateStatement = sqlConnection.prepareStatement("UPDATE `sd_constants` SET `value`=? WHERE `kkey`=?;");
            updateStatement.setString(1, String.valueOf(4));
            updateStatement.setString(2, SQL_VERSION_CONSTANT);
            updateStatement.executeUpdate();
            updateStatement.close();
            // Close objects
            sqlConnection.close();
        }
    }

    /**
     * Updates the database from version 4 to version 5.
     *
     * @param jdbcUrl the url of the database.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void updateToSqlV5(@Nonnull final String jdbcUrl) throws SQLException {
        final int sqlVersion = getSqlVersion(jdbcUrl);
        if (sqlVersion == 4) {
            final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
            // sd_transaction table
            sqlConnection.prepareStatement("ALTER TABLE `sd_transaction` ADD `money_exchanged` DOUBLE NOT NULL DEFAULT '0' AFTER `amount`;").execute();
            // sd_uuid
            sqlConnection.prepareStatement(UUID_TABLE_SQL).execute();
            // sd_transaction table
            sqlConnection.prepareStatement("INSERT INTO `sd_uuid`(`uuid`) SELECT DISTINCT `uuid` FROM " +
                    "`sd_transaction` ON DUPLICATE KEY UPDATE `id`=`id`;").execute();
            sqlConnection.prepareStatement("ALTER TABLE `sd_transaction` ADD `uuid_id` INT NOT NULL FIRST;").execute();
            sqlConnection.prepareStatement("ALTER TABLE `sd_transaction` ADD INDEX(`uuid_id`);").execute();
            sqlConnection.prepareStatement("UPDATE `sd_transaction` INNER JOIN `sd_uuid` ON `sd_uuid`.`uuid`=" +
                    "`sd_transaction`.`uuid` SET `uuid_id`=`sd_uuid`.`id`;").execute();
            sqlConnection.prepareStatement("ALTER TABLE `sd_transaction` DROP `uuid`;").execute();
            sqlConnection.prepareStatement(DEFAULT_TRANSACTION_TABLE_FK_UUID_SQL).execute();
            // Update sql version
            final PreparedStatement updateStatement = sqlConnection.prepareStatement("UPDATE `sd_constants` SET `value`=? WHERE `kkey`=?;");
            updateStatement.setString(1, String.valueOf(5));
            updateStatement.setString(2, SQL_VERSION_CONSTANT);
            updateStatement.executeUpdate();
            updateStatement.close();
            // Close objects
            sqlConnection.close();
        }
    }

    /**
     * Updates the database from version 5 to version 6.
     *
     * @param jdbcUrl the url of the database.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void updateToSqlV6(@Nonnull final String jdbcUrl) throws SQLException {
        final int sqlVersion = getSqlVersion(jdbcUrl);
        if (sqlVersion == 5) {
            final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
            // sd_prices table
            if(tablesExits(jdbcUrl, "sd_products")) {
                sqlConnection.prepareStatement("DROP TABLE `sd_products`;");
            }
            sqlConnection.prepareStatement("ALTER TABLE `sd_prices` CHANGE `name` `type` VARCHAR(255);").execute();
            sqlConnection.prepareStatement("ALTER TABLE `sd_prices` CHANGE `unsafeData` `unsafe_data` TINYINT(4);").execute();
            sqlConnection.prepareStatement("ALTER TABLE `sd_prices` ADD `decay_amount` INT NOT NULL DEFAULT '64' AFTER `demand`;").execute();
            sqlConnection.prepareStatement("ALTER TABLE `sd_prices` ADD `decay_interval` BIGINT NOT NULL DEFAULT '43200000' AFTER `decay_amount`;").execute();
            sqlConnection.prepareStatement("ALTER TABLE `sd_prices` ADD `decay_type` TINYINT NOT NULL DEFAULT '0' AFTER `decay_interval`;").execute();
            sqlConnection.prepareStatement("ALTER TABLE `sd_prices` ADD `mod_factor` FLOAT NOT NULL DEFAULT '0.1' AFTER `unsafe_data`;").execute();
            sqlConnection.prepareStatement("RENAME TABLE `sd_prices` TO `sd_products`;").execute();
            // Update sql version
            final PreparedStatement updateStatement = sqlConnection.prepareStatement("UPDATE `sd_constants` SET `value`=? WHERE `kkey`=?;");
            updateStatement.setString(1, String.valueOf(6));
            updateStatement.setString(2, SQL_VERSION_CONSTANT);
            updateStatement.executeUpdate();
            updateStatement.close();
            // Close objects
            sqlConnection.close();
        }
    }

    /**
     * Gets the sql version of the database.
     *
     * @param jdbcUrl the url of the database.
     * @return the sql version of the database or -1 if it is not set.
     *
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null
     */
    public static int getSqlVersion(@Nonnull final String jdbcUrl) throws SQLException {
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        // sd_price table
        final PreparedStatement preparedStatement = sqlConnection.prepareStatement(SELECT_SQL_VERSION);
        preparedStatement.setString(1, SQL_VERSION_CONSTANT);
        final ResultSet results = preparedStatement.executeQuery();
        final int returnVal = results.next() ? Integer.parseInt(results.getString(1)) : -1;
        // Close objects
        sqlConnection.close();
        return returnVal;
    }

    /**
     * Gets the sql type of a database column.
     *
     * @param jdbcUrl   the url of the database.
     * @param tableName the name of the table to check.
     * @param column    the name of a column which belongs to the passed table.
     * @return the sql version of the database or -1 if it is not set.
     *
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null
     */
    @Nonnull
    private static String getSqlType(@Nonnull final String jdbcUrl, @Nonnull final String tableName, @Nonnull final String column) throws SQLException {
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        // sd_price table
        final PreparedStatement preparedStatement = sqlConnection.prepareStatement(GET_COLUMN_TYPE);
        preparedStatement.setString(1, tableName);
        preparedStatement.setString(2, column);
        final ResultSet results = preparedStatement.executeQuery();
        final String returnVal = results.next() ? results.getString(1) : "";
        // Close objects
        sqlConnection.close();
        return returnVal;
    }

    /**
     * Attempts to create the {@link DefaultProduct default product} table.
     *
     * @param jdbcUrl the url of the database.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null
     */
    public static void createDefaultProductTable(@Nonnull final String jdbcUrl) throws SQLException {
        // Connect to table
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        // Create table if it does not exist
        sqlConnection.prepareStatement(DEFAULT_PRODUCT_TABLE_SQL).execute();
        // Close objects
        sqlConnection.close();
    }

    /**
     * Attempts to create the {@link DefaultProduct default product} transaction table.
     *
     * @param jdbcUrl the url of the database.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void createDefaultTransactionTable(@Nonnull final String jdbcUrl) throws SQLException {
        // Connect to table
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        final int sqlVersion = getSqlVersion(jdbcUrl);
        // Create table if it does not exist
        sqlConnection.prepareStatement(DEFAULT_TRANSACTION_TABLE_SQL).execute();
        if (!foreignKeysExits(jdbcUrl, "fk_price_id")) {
            sqlConnection.prepareStatement(DEFAULT_TRANSACTION_TABLE_FK_PRICE_SQL).execute();
        }
        if ((sqlVersion == -1 || sqlVersion == 5) && !foreignKeysExits(jdbcUrl, "fk_uuid_id")) {
            sqlConnection.prepareStatement(DEFAULT_TRANSACTION_TABLE_FK_UUID_SQL).execute();
        }
        // Close objects
        sqlConnection.close();
    }

    /**
     * Attempts to create the constants table.
     *
     * @param jdbcUrl the url of the database.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void createConstantsTable(@Nonnull final String jdbcUrl) throws SQLException {
        // Connect to table
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        // Create table if it does not exist
        sqlConnection.prepareStatement(CONSTANTS_TABLE_SQL).execute();
        // Close objects
        sqlConnection.close();
    }

    /**
     * Attempts to create the uuid table.
     *
     * @param jdbcUrl the url of the database.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void createUuidTable(@Nonnull final String jdbcUrl) throws SQLException {
        // Connect to table
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        // Create table if it does not exist
        sqlConnection.prepareStatement(UUID_TABLE_SQL).execute();
        sqlConnection.prepareStatement("INSERT INTO `sd_uuid`(`uuid`) VALUES " +
                "('" + SYSTEM_UUID + "') ON DUPLICATE KEY UPDATE `id` = `id`;").execute();
        // Close objects
        sqlConnection.close();
    }

    /**
     * Attempts to set the sql version.
     *
     * @param jdbcUrl the url of the database.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void setSqlVersion(@Nonnull final String jdbcUrl) throws SQLException {
        // Connect to table
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        // Create table if it does not exist
        final PreparedStatement updateStatement = sqlConnection.prepareStatement(INSERT_UPDATE_SQL_VERSION);
        updateStatement.setString(1, SQL_VERSION_CONSTANT);
        updateStatement.setString(2, String.valueOf(SQL_VERSION));
        updateStatement.executeUpdate();
        updateStatement.close();
        // Close objects
        sqlConnection.close();
    }

    /**
     * Attempt to update the {@link DefaultProduct default product} table with the current list of {@link DefaultProduct
     * products} from memory.
     *
     * @param jdbcUrl    the url of the database.
     * @param productMap the list of {@link DefaultProduct products} which live in memory.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void updateDefaultProductTable(@Nonnull final String jdbcUrl, @Nonnull final ConcurrentMap<String, DefaultProduct> productMap)
            throws SQLException {
        // Connect to database
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        sqlConnection.setAutoCommit(false);
        final PreparedStatement updateStatement = sqlConnection.prepareStatement(INSERT_UPDATE_DEFAULT_PRODUCT_TABLE_SQL);
        // Traverse product map
        for (final Map.Entry<String, DefaultProduct> kvp : productMap.entrySet()) {
            // Setup prepared statement
            updateStatement.setString(1, kvp.getValue().alias);
            updateStatement.setString(2, kvp.getValue().type);
            updateStatement.setByte(3, kvp.getValue().unsafeData);
            updateStatement.setFloat(4, kvp.getValue().getModFactor());
            updateStatement.setFloat(5, kvp.getValue().getPrice());
            updateStatement.setInt(6, kvp.getValue().supply);
            updateStatement.setInt(7, kvp.getValue().demand);
            updateStatement.setInt(8, kvp.getValue().decayAmount);
            updateStatement.setLong(9, kvp.getValue().decayInterval);
            updateStatement.setByte(10, kvp.getValue().decayType);
            // update
            updateStatement.setString(11, kvp.getValue().type);
            updateStatement.setByte(12, kvp.getValue().unsafeData);
            updateStatement.setFloat(13, kvp.getValue().getModFactor());
            updateStatement.setFloat(14, kvp.getValue().getPrice());
            updateStatement.setInt(15, kvp.getValue().supply);
            updateStatement.setInt(16, kvp.getValue().demand);
            updateStatement.setInt(17, kvp.getValue().decayAmount);
            updateStatement.setLong(18, kvp.getValue().decayInterval);
            updateStatement.setByte(19, kvp.getValue().decayType);
            updateStatement.addBatch();
        }
        updateStatement.executeBatch();
        sqlConnection.commit();
        // Close objects
        updateStatement.close();
        sqlConnection.close();
    }

    /**
     * Attempt to update the {@link DefaultProduct product} table with the current {@link DefaultProduct products} from
     * memory.
     *
     * @param jdbcUrl        the url of the database.
     * @param defaultProduct the {@link DefaultProduct products} which live in memory.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void updateDefaultProduct(@Nonnull final String jdbcUrl, @Nonnull final DefaultProduct defaultProduct) throws SQLException {
        // Connect to database
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        final PreparedStatement updateStatement = sqlConnection.prepareStatement(INSERT_UPDATE_DEFAULT_PRODUCT_TABLE_SQL);
        // Traverse defaultProduct map
        // Setup prepared statement
        updateStatement.setString(1, defaultProduct.alias);
        updateStatement.setString(2, defaultProduct.type);
        updateStatement.setByte(3, defaultProduct.unsafeData);
        updateStatement.setFloat(4, defaultProduct.getModFactor());
        updateStatement.setFloat(5, defaultProduct.getPrice());
        updateStatement.setInt(6, defaultProduct.supply);
        updateStatement.setInt(7, defaultProduct.demand);
        updateStatement.setInt(8, defaultProduct.decayAmount);
        updateStatement.setLong(9, defaultProduct.decayInterval);
        updateStatement.setByte(10, defaultProduct.decayType);
        // update
        updateStatement.setString(11, defaultProduct.type);
        updateStatement.setByte(12, defaultProduct.unsafeData);
        updateStatement.setFloat(13, defaultProduct.getModFactor());
        updateStatement.setFloat(14, defaultProduct.getPrice());
        updateStatement.setInt(15, defaultProduct.supply);
        updateStatement.setInt(16, defaultProduct.demand);
        updateStatement.setInt(17, defaultProduct.decayAmount);
        updateStatement.setLong(18, defaultProduct.decayInterval);
        updateStatement.setByte(19, defaultProduct.decayType);
        updateStatement.executeUpdate();
        // Close objects
        updateStatement.close();
        sqlConnection.close();
    }

    /**
     * Attempts to delete a {@link DefaultProduct default product} from the sql table.
     *
     * @param jdbcUrl the url of the database.
     * @param item    the name of the {@link DefaultProduct product}.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void deleteItemFromSdPrices(@Nonnull final String jdbcUrl, @Nonnull final String item) throws SQLException {
        // Connect to table
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        // Create table if it does not exist
        final PreparedStatement deleteStatement = sqlConnection.prepareStatement(DELETE_DEFAULT_PRODUCT_TABLE_SQL);
        deleteStatement.setString(1, item);
        deleteStatement.executeUpdate();
        // Close objects
        deleteStatement.close();
        sqlConnection.close();
    }

    /**
     * Attempt to read the database list of {@link DefaultProduct products} into memory.
     *
     * @param jdbcUrl    the url of the database.
     * @param productMap the list of {@link DefaultProduct products} which live in memory.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void readDefaultProductTable(@Nonnull final String jdbcUrl,
                                               @Nonnull final ConcurrentMap<String, DefaultProduct> productMap) throws SQLException {
        // Connect to database
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        // Read the whole table
        final ResultSet result = sqlConnection.prepareStatement(READ_DEFAULT_PRODUCT_TABLE_SQL).executeQuery();
        // Add products to productMap if the do not exist
        while (result.next()) {
            final String alias = result.getString("alias").toLowerCase();
            productMap.putIfAbsent(alias, new DefaultProduct(alias,
                    result.getString("type"),
                    result.getByte("unsafe_data"),
                    result.getFloat("mod_factor"),
                    result.getFloat("price"),
                    result.getInt("supply"),
                    result.getInt("demand"),
                    result.getInt("decay_amount"),
                    result.getLong("decay_interval"),
                    result.getByte("decay_type")));
        }
        // Close objects
        result.close();
        sqlConnection.close();
    }

    /**
     * Attempt to insert a {@link DefaultProduct default product} transaction into the table.
     *
     * @param jdbcUrl the url of the database.
     * @param uuid    the uuid of the player.
     * @param action  the action preformed.
     * @param alias   the {@link DefaultProduct product} name.
     * @param amount  the amount set, bought, or sold.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void insertDefaultTransaction(@Nonnull final String jdbcUrl, @Nonnull final String uuid, final byte action,
                                                @Nonnull final String alias, final float amount, final double moneyExchanged) throws SQLException {
        // Connect to database
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        // Check to see if user exists
        final PreparedStatement searchUser = sqlConnection.prepareStatement("SELECT EXISTS(SELECT `id` FROM `sd_uuid` WHERE `uuid`=?);");
        searchUser.setString(1, uuid);
        final ResultSet resultSet = searchUser.executeQuery();
        // Did the sql query get any values if sao skip this
        if (!resultSet.next() || !resultSet.getBoolean(1)) {
            final PreparedStatement insertUuidStatement = sqlConnection.prepareStatement("INSERT INTO `sd_uuid`(`uuid`) VALUES (?);");
            insertUuidStatement.setString(1, uuid);
            insertUuidStatement.executeUpdate();
            insertUuidStatement.close();
        }
        resultSet.close();
        searchUser.close();
        // Setup prepared statement
        final PreparedStatement insertStatement = sqlConnection.prepareStatement(INSERT_DEFAULT_TRANSACTION_TABLE_SQL);
        insertStatement.setString(1, uuid);
        insertStatement.setByte(2, action);
        insertStatement.setString(3, alias);
        insertStatement.setFloat(4, amount);
        insertStatement.setDouble(5, moneyExchanged);
        // Execute query
        insertStatement.executeUpdate();
        // Close objects
        insertStatement.close();
        insertStatement.close();
        sqlConnection.close();
    }

    /**
     * Searches for constants in the constant table.
     *
     * @param jdbcUrl the url of the database.
     * @param keyList the list of keys to search for.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static Map<String, String> searchConstants(@Nonnull final String jdbcUrl, @Nonnull final List<String> keyList) throws SQLException {
        // Connect to database
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        final StringBuilder builder = new StringBuilder(SEARCH_KEYS_IN_CONSTANTS);
        builder.append('(');
        keyList.forEach(key->builder.append('\'').append(key).append('\'').append(','));
        builder.deleteCharAt(builder.length() - 1);
        builder.append(");");
        // Setup prepared statement
        final PreparedStatement searchStatement = sqlConnection.prepareStatement(builder.toString());
        // Execute query
        final ResultSet resultSet = searchStatement.executeQuery();
        final Map<String, String> returnMap = new HashMap<>();
        while (resultSet.next()) {
            returnMap.put(resultSet.getString("kkey"), resultSet.getString("value"));
        }
        // Close objects
        resultSet.close();
        searchStatement.close();
        sqlConnection.close();
        return returnMap;
    }
}
