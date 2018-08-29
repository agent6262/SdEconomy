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

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * A spigot extended implementation of the {@link DefaultEconomy}.
 *
 * @author Tyler Bucher
 */
public class SpigotDefaultEconomy extends DefaultEconomy {

    /**
     * Creates decay tasks for all products.
     *
     * @param sdEconomy the {@link SdEconomy} plugin instance.
     */
    public static void createDecayTasks(@Nonnull final SdEconomy sdEconomy) {
        // Create repeating decay task for each decay interval
        final Map<Long, List<DefaultProduct>> productDecayMap = DefaultEconomy.getProductDecayMap();
        for (final Map.Entry<Long, List<DefaultProduct>> kvp : productDecayMap.entrySet()) {
            // Only create task if the time is greater than -1
            if (kvp.getKey() > -1) {
                Bukkit.getScheduler().scheduleSyncRepeatingTask(sdEconomy,
                        new ProductDecayRunnable<DefaultProduct>(kvp.getKey(), kvp.getKey(), kvp.getValue()) {
                            @Override
                            public void run() {
                                try {
                                    // Connect to database
                                    final Connection sqlConnection = DriverManager.getConnection(sdEconomy.getConfiguration().getJdbcUrl());
                                    sqlConnection.setAutoCommit(false);
                                    final PreparedStatement insertStatement = sqlConnection.prepareStatement(SqlService.INSERT_DEFAULT_TRANSACTION_TABLE_SQL);
                                    // Attempt decay product demand
                                    for (DefaultProduct defaultProduct : this.productList) {
                                        final int amount;
                                        if (defaultProduct.decayType == SqlService.DECAY_PERCENTAGE_TYPE) {
                                            amount = DefaultEconomy.decayDemand(defaultProduct,
                                                    (int) Math.ceil(((double) defaultProduct.demand * (double) defaultProduct.decayAmount) / 100.0));
                                        } else {
                                            amount = DefaultEconomy.decayDemand(defaultProduct, defaultProduct.decayAmount);
                                        }
                                        if (amount > 0) {
                                            // Setup prepared statement
                                            insertStatement.setString(1, SqlService.SYSTEM_UUID);
                                            insertStatement.setByte(2, SqlService.DECAY_ACTION);
                                            insertStatement.setString(3, defaultProduct.alias);
                                            insertStatement.setFloat(4, amount);
                                            insertStatement.setDouble(5, 0);
                                            // Execute query
                                            insertStatement.addBatch();
                                        }
                                    }
                                    // Close objects
                                    insertStatement.executeBatch();
                                    sqlConnection.commit();
                                    insertStatement.close();
                                    sqlConnection.close();
                                } catch (SQLException e) {
                                    sdEconomy.getLogger().log(Level.SEVERE, "Error accessing database", e);
                                }
                            }
                        }, kvp.getKey(), kvp.getKey());
            }
        }
    }

    /**
     * Creates save tasks for the {@link DefaultProduct} list.
     *
     * @param sdEconomy the {@link SdEconomy} plugin instance.
     */
    public static void createSaveTask(@Nonnull final SdEconomy sdEconomy) {
        // Create repeating save task
        Bukkit.getScheduler().scheduleSyncRepeatingTask(sdEconomy, ()->{
            // Attempt to save item data
            try {
                SqlService.updateDefaultProductTable(sdEconomy.getConfiguration().getJdbcUrl(), DefaultEconomy.stockPrices);
            } catch (SQLException e) {
                sdEconomy.getLogger().log(Level.SEVERE, "Error accessing database", e);
            }
        }, sdEconomy.getConfiguration().getSaveInterval(), sdEconomy.getConfiguration().getSaveInterval());
    }

    /**
     * Gets a {@link DefaultProduct} from a given item stack.
     *
     * @param itemStack the item stack to check against.
     * @return the found {@link DefaultProduct} or null.
     */
    @Nullable
    public static DefaultProduct getProductFromItemStack(@Nonnull final ItemStack itemStack) {
        for (final DefaultProduct defaultProduct : DefaultEconomy.stockPrices.values()) {
            // if the item stack type and unsafeData equal the products then return it.
            if (defaultProduct.type.equalsIgnoreCase(itemStack.getType().name()) && defaultProduct.unsafeData == itemStack.getData().getData()) {
                return defaultProduct;
            }
        }
        return null;
    }
}
