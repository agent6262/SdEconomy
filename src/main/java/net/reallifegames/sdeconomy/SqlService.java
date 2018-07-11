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
import java.util.Map;

/**
 * Manages universal sql query's for multiple plugin APIs.
 *
 * @author Tyler Bucher
 */
public class SqlService {

    /**
     * The current sql version of this plugin.
     */
    public static final int SQL_VERSION = 2;

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
     * The product table creation sql query string.
     */
    @Nonnull
    private static final String PRODUCT_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `sd_prices` (`id` int(11) NOT NULL " +
            "AUTO_INCREMENT, `alias` VARCHAR(255) NOT NULL, `name` VARCHAR(255) NOT NULL, `unsafeData` TINYINT(4) " +
            "DEFAULT '0', `price` FLOAT NOT NULL, `supply` INT NOT NULL, `demand` INT NOT NULL, PRIMARY KEY (`id`), " +
            "UNIQUE KEY `alias_2` (`alias`), KEY `name` (`name`), KEY `alias` (`alias`)) ENGINE = InnoDB;";

    /**
     * The product table update sql query string.
     */
    @Nonnull
    private static final String UPDATE_PRODUCT_TABLE_SQL = "UPDATE `sd_prices` SET `name`=?,`unsafeData`=?,`price`=?," +
            "`supply`=?, `demand`=? WHERE LOWER(`alias`)=?";

    /**
     * The product table insert sql query string.
     */
    @Nonnull
    private static final String INSERT_PRODUCT_TABLE_SQL = "INSERT INTO `sd_prices`(`alias`, `name`, `unsafeData`, " +
            "`price`, `supply`, `demand`) VALUES (?,?,?,?,?,?)";

    /**
     * Checks to see if a row exists.
     */
    @Nonnull
    private static final String EXIST_PRODUCT_TABLE_SQL = "SELECT EXISTS(SELECT 1 FROM `sd_prices` where LOWER(`alias`)=?);";

    /**
     * The product table select sql query string.
     */
    @Nonnull
    private static final String READ_PRODUCT_TABLE_SQL = "SELECT * FROM `sd_prices`";

    /**
     * The product table delete sql query string.
     */
    @Nonnull
    private static final String DELETE_PRODUCT_TABLE_SQL = "DELETE FROM `sd_prices` WHERE LOWER(`alias`) = ?;";

    /**
     * The economy transaction table.
     */
    @Nonnull
    private static final String TRANSACTION_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `sd_transaction` (`uuid` char(36) " +
            "NOT NULL,`action` tinyint(4) NOT NULL,`price_id` int(11) NOT NULL,`date` timestamp NOT NULL DEFAULT " +
            "CURRENT_TIMESTAMP,`amount` float NOT NULL, KEY `uuid` (`uuid`), KEY `action` (`action`), KEY `price_id` " +
            "(`price_id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1;";

    /**
     * The economy transaction table fk sql.
     */
    @Nonnull
    private static final String TRANSACTION_TABLE_FK_SQL = "ALTER TABLE `sd_transaction` ADD CONSTRAINT `fk_price_id` " +
            "FOREIGN KEY (`price_id`) REFERENCES `sd_prices` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;";

    /**
     * The transaction table insert sql query string.
     */
    @Nonnull
    private static final String INSERT_TRANSACTION_TABLE_SQL = "INSERT INTO `sd_transaction`(`uuid`, `action`, " +
            "`price_id`, `amount`) VALUES (?,?,(SELECT `id` FROM `sd_prices` WHERE LOWER(`alias`)=?),?);";

    /**
     * The set price action for the transaction table.
     */
    public static final byte SET_ACTION = 0;

    /**
     * The buy action for the transaction table.
     */
    public static final byte BUY_ACTION = 1;

    /**
     * The sell action for the transaction table.
     */
    public static final byte SELL_ACTION = 2;

    /**
     * The product table creation sql query string.
     */
    @Nonnull
    private static final String CONSTANTS_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `sd_constants` (`kkey` int(11) NOT " +
            "NULL, `value` VARCHAR(255) NOT NULL, PRIMARY KEY (`kkey`)) ENGINE = InnoDB;";

    /**
     * The sql version constant.
     */
    public static final byte SQL_VERSION_CONSTANT = 0;

    /**
     * The constants table select sql version query string.
     */
    @Nonnull
    private static final String SELECT_SQL_VERSION = "SELECT `value` FROM `sd_constants` WHERE `kkey`=?;";

    /**
     * The constants table select sql version query string.
     */
    @Nonnull
    private static final String INSERT_UPDATE_SQL_VERSION = "INSERT INTO `sd_constants`(`kkey`, `value`) VALUES (?,?) " +
            "ON DUPLICATE KEY UPDATE `kkey` = `kkey`;";

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
     * gets the sql version of the database.
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
        preparedStatement.setInt(1, SQL_VERSION_CONSTANT);
        final ResultSet results = preparedStatement.executeQuery();
        final int returnVal = results.next() ? Integer.parseInt(results.getString(1)) : -1;
        // Close objects
        sqlConnection.close();
        return returnVal;
    }

    /**
     * Attempts to create the product table.
     *
     * @param jdbcUrl the url of the database.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null
     */
    public static void createProductTable(@Nonnull final String jdbcUrl) throws SQLException {
        // Connect to table
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        // Create table if it does not exist
        sqlConnection.prepareStatement(PRODUCT_TABLE_SQL).execute();
        // Close objects
        sqlConnection.close();
    }

    /**
     * Attempts to create the transaction table.
     *
     * @param jdbcUrl the url of the database.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void createTransactionTable(@Nonnull final String jdbcUrl) throws SQLException {
        // Connect to table
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        // Create table if it does not exist
        sqlConnection.prepareStatement(TRANSACTION_TABLE_SQL).execute();
        if (!foreignKeysExits(jdbcUrl, "fk_price_id")) {
            sqlConnection.prepareStatement(TRANSACTION_TABLE_FK_SQL).execute();
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
        final PreparedStatement updateStatement = sqlConnection.prepareStatement(INSERT_UPDATE_SQL_VERSION);
        updateStatement.setInt(1, SQL_VERSION_CONSTANT);
        updateStatement.setString(2, String.valueOf(SQL_VERSION));
        updateStatement.executeUpdate();
        updateStatement.close();
        // Close objects
        sqlConnection.close();
    }

    /**
     * Attempt to update the product table with the current list of products from memory.
     *
     * @param jdbcUrl    the url of the database.
     * @param productMap the list of products which live in memory.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void updateProductTable(@Nonnull final String jdbcUrl, @Nonnull final Map<String, Product> productMap)
            throws SQLException {
        // Connect to database
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        // Traverse product map
        for (final Map.Entry<String, Product> kvp : productMap.entrySet()) {
            final PreparedStatement preparedStatement = sqlConnection.prepareStatement(EXIST_PRODUCT_TABLE_SQL);
            // Set parameters
            preparedStatement.setString(1, kvp.getKey());
            // Execute query
            final ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                if (results.getBoolean(1)) {
                    // Setup prepared statement
                    final PreparedStatement updateStatement = sqlConnection.prepareStatement(UPDATE_PRODUCT_TABLE_SQL);
                    updateStatement.setString(1, kvp.getValue().type);
                    updateStatement.setByte(2, kvp.getValue().unsafeData);
                    updateStatement.setFloat(3, kvp.getValue().getPrice());
                    updateStatement.setInt(4, kvp.getValue().supply);
                    updateStatement.setInt(5, kvp.getValue().demand);
                    updateStatement.setString(6, kvp.getKey());
                    // Execute query
                    updateStatement.executeUpdate();
                    updateStatement.close();
                } else {
                    // Setup prepared statement
                    final PreparedStatement insertStatement = sqlConnection.prepareStatement(INSERT_PRODUCT_TABLE_SQL);
                    insertStatement.setString(1, kvp.getKey());
                    insertStatement.setString(2, kvp.getValue().type);
                    insertStatement.setByte(3, kvp.getValue().unsafeData);
                    insertStatement.setFloat(4, kvp.getValue().getPrice());
                    insertStatement.setInt(5, kvp.getValue().supply);
                    insertStatement.setInt(6, kvp.getValue().demand);
                    // Execute query
                    insertStatement.executeUpdate();
                    insertStatement.close();
                }
            }
            // Close objects
            results.close();
            preparedStatement.close();
        }
        // Close objects
        sqlConnection.close();
    }

    /**
     * Attempts to delete an item from the sql table.
     *
     * @param jdbcUrl the url of the database.
     * @param item    the name of the item.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void deleteItemFromSdPrices(@Nonnull final String jdbcUrl, @Nonnull final String item) throws SQLException {
        // Connect to table
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        // Create table if it does not exist
        final PreparedStatement deleteStatement = sqlConnection.prepareStatement(DELETE_PRODUCT_TABLE_SQL);
        deleteStatement.setString(1, item);
        deleteStatement.executeUpdate();
        // Close objects
        deleteStatement.close();
        sqlConnection.close();
    }

    /**
     * Attempt to read the database list of products into memory.
     *
     * @param jdbcUrl    the url of the database.
     * @param productMap the list of products which live in memory.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void readProductTable(@Nonnull final String jdbcUrl,
                                        @Nonnull final Map<String, Product> productMap) throws SQLException {
        // Connect to database
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        // Read the whole table
        final ResultSet result = sqlConnection.prepareStatement(READ_PRODUCT_TABLE_SQL).executeQuery();
        // Add products to productMap if the do not exist
        while (result.next()) {
            final String alias = result.getString("alias").toLowerCase();
            productMap.putIfAbsent(alias, new Product(alias,
                    result.getString("name"),
                    result.getByte("unsafeData"),
                    result.getFloat("price"),
                    result.getInt("supply"),
                    result.getInt("demand")));
        }
        // Close objects
        result.close();
        sqlConnection.close();
    }

    /**
     * Attempt to insert a transaction into the table.
     *
     * @param jdbcUrl the url of the database.
     * @param uuid    the uuid of the player.
     * @param action  the action preformed.
     * @param alias   the item name.
     * @param amount  the amount set, bought, or sold.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void insertTransaction(@Nonnull final String jdbcUrl, @Nonnull final String uuid, final byte action,
                                         @Nonnull final String alias, final float amount) throws SQLException {
        // Connect to database
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        // Setup prepared statement
        final PreparedStatement insertStatement = sqlConnection.prepareStatement(INSERT_TRANSACTION_TABLE_SQL);
        insertStatement.setString(1, uuid);
        insertStatement.setByte(2, action);
        insertStatement.setString(3, alias);
        insertStatement.setFloat(4, amount);
        // Execute query
        insertStatement.executeUpdate();
        // Close objects
        insertStatement.close();
        insertStatement.close();
        sqlConnection.close();
    }
}
