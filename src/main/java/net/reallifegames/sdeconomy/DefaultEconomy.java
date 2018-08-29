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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultEconomy {

    /**
     * The list of {@link DefaultProduct products}.
     */
    @Nonnull
    public static final ConcurrentHashMap<String, DefaultProduct> stockPrices = new ConcurrentHashMap<>();

    /**
     * @return a map of {@link DefaultProduct products} grouped by their decay interval time.
     */
    protected static Map<Long, List<DefaultProduct>> getProductDecayMap() {
        // Create the map which we will return from this function
        final Map<Long, List<DefaultProduct>> productDecayMap = new HashMap<>();
        // Loop through all of the products
        for (final DefaultProduct defaultProduct : stockPrices.values()) {
            // Get list for the products decay interval. If one does not exists yet create it.
            final List<DefaultProduct> productList = productDecayMap.computeIfAbsent(defaultProduct.decayInterval,
                    k->new ArrayList<>());
            // Add product to that list
            productList.add(defaultProduct);
        }
        return productDecayMap;
    }

    /**
     * Gets the amount of money you would receive if you sold this instant.
     *
     * @param defaultProduct the {@link DefaultProduct product} to check.
     * @param amount         the amount sold.
     * @return the amount of money you would receive.
     */
    public static double checkSellReturns(@Nonnull final DefaultProduct defaultProduct, final int amount) {
        double returnValue = 0;
        int tSupply = defaultProduct.supply;
        for (int i = 0; i < amount; i++) {
            if (tSupply < Integer.MAX_VALUE) {
                tSupply++;
            }
            returnValue += defaultProduct.modFactor * ((double) defaultProduct.demand / (double) tSupply) + defaultProduct.price;
        }
        return returnValue;
    }

    /**
     * Sell an amount of a {@link DefaultProduct product} to the server.
     *
     * @param defaultProduct the {@link DefaultProduct product} to sell.
     * @param jdbcUrl        the url of the database.
     * @param uuid           the uuid of the player setting the price.
     * @param amount         the amount sold.
     * @return the amount of money to give to the player.
     *
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static double sell(@Nonnull DefaultProduct defaultProduct, @Nonnull final String jdbcUrl, @Nonnull final String uuid,
                              final int amount) throws SQLException {
        double returnValue = sellNoSql(defaultProduct, amount);
        SqlService.insertDefaultTransaction(jdbcUrl, uuid, SqlService.SELL_ACTION, defaultProduct.alias, amount, returnValue);
        return returnValue;
    }

    /**
     * Sell an amount of a {@link DefaultProduct product} to the server without sql.
     *
     * @param defaultProduct the {@link DefaultProduct product} to sell.
     * @param amount         the amount sold.
     * @return the amount of money to give to the player.
     */
    @SuppressWarnings ("Duplicates")
    public static double sellNoSql(@Nonnull DefaultProduct defaultProduct, final int amount) {
        double returnValue = 0;
        for (int i = 0; i < amount; i++) {
            if (defaultProduct.supply < Integer.MAX_VALUE) {
                defaultProduct.supply++;
            }
            defaultProduct.demand -= defaultProduct.demand == 1 ? 0 : 1;
            returnValue += defaultProduct.modFactor * ((double) defaultProduct.demand / (double) defaultProduct.supply) + defaultProduct.price;
        }
        return returnValue;
    }

    /**
     * Gets the amount of money it would cost if you bought some amount of {@link DefaultProduct products} at this
     * instant.
     *
     * @param defaultProduct the {@link DefaultProduct product} to check.
     * @param amount         the amount bought.
     * @return the amount of money it would cost to buy some amount of {@link DefaultProduct products}.
     */
    public static double checkBuyCost(@Nonnull final DefaultProduct defaultProduct, final int amount) {
        double cost = 0;
        int tDemand = defaultProduct.demand;
        int tSupply = defaultProduct.supply;
        for (int i = 0; i < amount; i++) {
            tSupply -= tSupply == 1 ? 0 : 1;
            if (tDemand < Integer.MAX_VALUE) {
                tDemand++;
            }
            cost += defaultProduct.modFactor * ((double) tDemand / (double) tSupply) + defaultProduct.price;
        }
        return cost;
    }

    /**
     * Sell an amount of a {@link DefaultProduct product} to the server.
     *
     * @param defaultProduct the {@link DefaultProduct product} to sell.
     * @param jdbcUrl        the url of the database.
     * @param uuid           the uuid of the player setting the price.
     * @param amount         the amount sold.
     * @return the amount of money to take from the player.
     *
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static double buy(@Nonnull DefaultProduct defaultProduct, @Nonnull final String jdbcUrl, @Nonnull final String uuid,
                             final int amount) throws SQLException {
        double returnValue = buyNoSql(defaultProduct, amount);
        SqlService.insertDefaultTransaction(jdbcUrl, uuid, SqlService.BUY_ACTION, defaultProduct.alias, amount, returnValue);
        return returnValue;
    }

    /**
     * Sell an amount of a {@link DefaultProduct product} to the server without sql logging.
     *
     * @param defaultProduct the {@link DefaultProduct product} to sell.
     * @param amount         the amount sold.
     * @return the amount of money to take from the player.
     */
    @SuppressWarnings ("Duplicates")
    public static double buyNoSql(@Nonnull DefaultProduct defaultProduct, final int amount) {
        double returnValue = 0;
        for (int i = 0; i < amount; i++) {
            if (defaultProduct.demand < Integer.MAX_VALUE) {
                defaultProduct.demand++;
            }
            defaultProduct.supply -= defaultProduct.supply == 1 ? 0 : 1;
            returnValue += defaultProduct.modFactor * ((double) defaultProduct.demand / (double) defaultProduct.supply) + defaultProduct.price;
        }
        return returnValue;
    }

    /**
     * Sets the price of a {@link DefaultProduct product}.
     *
     * @param defaultProduct the {@link DefaultProduct product} to alter.
     * @param jdbcUrl        the url of the database.
     * @param uuid           the uuid of the player setting the price.
     * @param price          the price to be set.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void setPrice(@Nonnull DefaultProduct defaultProduct, @Nonnull final String jdbcUrl, @Nonnull final String uuid,
                                final float price) throws SQLException {
        setPriceNoSql(defaultProduct, price);
        SqlService.insertDefaultTransaction(jdbcUrl, uuid, SqlService.SET_PRICE_ACTION, defaultProduct.alias, price, 0);
    }

    /**
     * Sets the price of a {@link DefaultProduct product} without sql.
     *
     * @param defaultProduct the {@link DefaultProduct product} to alter.
     * @param price          the price to be set.
     * @return the price set.
     */
    public static float setPriceNoSql(@Nonnull DefaultProduct defaultProduct, final float price) {
        return defaultProduct.price = price;
    }

    /**
     * Sets the mod factor of a {@link DefaultProduct product}.
     *
     * @param defaultProduct the {@link DefaultProduct product} to alter.
     * @param jdbcUrl        the url of the database.
     * @param uuid           the uuid of the player setting the price.
     * @param modFactor      the mod factor to be set.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void setModFactor(@Nonnull DefaultProduct defaultProduct, @Nonnull final String jdbcUrl, @Nonnull final String uuid,
                                    final float modFactor) throws SQLException {
        setModFactorNoSql(defaultProduct, modFactor);
        SqlService.insertDefaultTransaction(jdbcUrl, uuid, SqlService.SET_MOD_FACTOR_ACTION, defaultProduct.alias, modFactor, 0);
    }

    /**
     * Sets the mod factor of a {@link DefaultProduct product} without sql.
     *
     * @param defaultProduct the {@link DefaultProduct product} to alter.
     * @param modFactor      the mod factor to be set.
     * @return the mod factor set.
     */
    public static float setModFactorNoSql(@Nonnull DefaultProduct defaultProduct, final float modFactor) {
        return defaultProduct.modFactor = modFactor;
    }

    /**
     * Decays the amount of the {@link DefaultProduct product} demand.
     *
     * @param defaultProduct the {@link DefaultProduct product} to alter.
     * @param amount         the amount to decay.
     */
    public static int decayDemand(@Nonnull DefaultProduct defaultProduct, final int amount) {
        int decayAmount = 0;
        for (; decayAmount < amount; decayAmount++) {
            if (defaultProduct.demand > 1) {
                defaultProduct.demand--;
            } else {
                break;
            }
        }
        return decayAmount > 0 ? decayAmount : 0;
    }
}
