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
     * The product table creation sql query string.
     */
    @Nonnull
    private static final String PRODUCT_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `sd_prices` (`alias` VARCHAR(255) " +
            "NOT NULL, `name` VARCHAR(255) NOT NULL, `unsafeData` TINYINT(4) DEFAULT '0', `price` FLOAT NOT NULL, `supply` INT " +
            "NOT NULL, `demand` INT NOT NULL, PRIMARY KEY (`alias`), KEY `name` (`name`)) ENGINE = InnoDB;";

    /**
     * The product table update sql query string.
     */
    @Nonnull
    private static final String UPDATE_PRODUCT_TABLE_SQL = "UPDATE `sd_prices` SET `price`=?,`supply`=?, `demand`=? " +
            "WHERE `alias`=?";

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
    private static final String EXIST_PRODUCT_TABLE_SQL = "SELECT EXISTS(SELECT 1 FROM `sd_prices` where `alias`=?)";

    /**
     * The product table select sql query string.
     */
    @Nonnull
    private static final String READ_PRODUCT_TABLE_SQL = "SELECT * FROM `sd_prices`";

    /**
     * The economy transaction table.
     */
    @Nonnull
    private static final String TRANSACTION_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `sd_transaction` ( `uuid` CHAR(36) " +
            "NOT NULL , `action` TINYINT NOT NULL , `type` VARCHAR(255) NOT NULL , `date` TIMESTAMP NOT NULL DEFAULT " +
            "CURRENT_TIMESTAMP , `amount` FLOAT NOT NULL , INDEX (`uuid`), INDEX (`action`), INDEX (`type`)) ENGINE = " +
            "InnoDB;";

    /**
     * The transaction table insert sql query string.
     */
    @Nonnull
    private static final String INSERT_TRANSACTION_TABLE_SQL = "INSERT INTO `sd_transaction`(`uuid`, `action`, `type`, " +
            "`amount`) VALUES (?,?,?,?)";

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
                    updateStatement.setFloat(1, kvp.getValue().getPrice());
                    updateStatement.setInt(2, kvp.getValue().supply);
                    updateStatement.setInt(3, kvp.getValue().demand);
                    updateStatement.setString(4, kvp.getKey());
                    // Execute query
                    updateStatement.executeUpdate();
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
                }
            }
        }
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
            productMap.putIfAbsent(result.getString("alias"), new Product(
                    result.getString("name"),
                    result.getByte("unsafeData"),
                    result.getFloat("price"),
                    result.getInt("supply"),
                    result.getInt("demand")));
        }
    }

    /**
     * Attempt to insert a transaction into the table.
     *
     * @param jdbcUrl the url of the database.
     * @param uuid    the uuid of the player.
     * @param action  the action preformed.
     * @param type    the type of product affected.
     * @param amount  the amount set, bought, or sold.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void insertTransaction(@Nonnull final String jdbcUrl, @Nonnull final String uuid, final byte action,
                                         @Nonnull final String type, final float amount) throws SQLException {
        // Connect to database
        final Connection sqlConnection = DriverManager.getConnection(jdbcUrl);
        // Setup prepared statement
        final PreparedStatement insertStatement = sqlConnection.prepareStatement(INSERT_TRANSACTION_TABLE_SQL);
        insertStatement.setString(1, uuid);
        insertStatement.setByte(2, action);
        insertStatement.setString(3, type);
        insertStatement.setFloat(4, amount);
        // Execute query
        insertStatement.executeUpdate();
    }
}
