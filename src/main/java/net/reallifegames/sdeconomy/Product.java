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

/**
 * A simple data structure to represent an item in memory.
 *
 * @author Tyler Bucher
 */
public class Product {

    /**
     * The type of this item.
     */
    @Nonnull
    public String type;

    /**
     * The unsafe data value of this item.
     */
    public byte unsafeData;

    /**
     * The current price of this item.
     */
    private float price;

    /**
     * The supply of this item.
     */
    public int supply;

    /**
     * The current demand of this item.
     */
    public int demand;

    /**
     * Creates a new item with the price, supply and demand set to 1.
     *
     * @param type the item type of this product.
     */
    public Product(@Nonnull final String type) {
        this.type = type;
        this.unsafeData = 0;
        this.price = 1;
        this.supply = 1;
        this.demand = 1;
    }

    /**
     * Creates a new item with a price, supply and demand.
     *
     * @param type       the item type of this product.
     * @param unsafeData the unsafe data value of this item.
     * @param price      the current price of this item.
     * @param supply     the supply of this item.
     * @param demand     the current demand of this item.
     */
    public Product(@Nonnull final String type, byte unsafeData, float price, int supply, int demand) {
        this.type = type;
        this.unsafeData = unsafeData;
        this.price = price;
        this.supply = supply;
        this.demand = demand;
    }

    /**
     * @return the current price of this item.
     */
    public float getPrice() {
        return price;
    }

    /**
     * Gets the amount of money you would receive if you sold this instant.
     *
     * @param product the product to check.
     * @param amount  the amount sold.
     * @return the amount of money you would receive.
     */
    public static double checkSellReturns(@Nonnull final Product product, final int amount) {
        float returnValue = 0;
        int tSupply = product.supply;
        for (int i = 0; i < amount; i++) {
            returnValue += product.price * ((float) product.demand / (float) tSupply);
            tSupply++;
        }
        return returnValue;
    }

    /**
     * Sell an amount of an item to the server.
     *
     * @param product the product to sell.
     * @param jdbcUrl the url of the database.
     * @param uuid    the uuid of the player setting the price.
     * @param amount  the amount sold.
     * @return the amount of money to give to the player.
     *
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static double sell(@Nonnull Product product, @Nonnull final String jdbcUrl, @Nonnull final String uuid,
                              final int amount) throws SQLException {
        SqlService.insertTransaction(jdbcUrl, uuid, SqlService.SELL_ACTION, product.type, amount);
        float returnValue = 0;
        for (int i = 0; i < amount; i++) {
            returnValue += product.price * ((float) product.demand / (float) product.supply);
            product.supply++;
        }
        return returnValue;
    }

    /**
     * Gets the amount of money it would cost if you bought some amount of items at this instant.
     *
     * @param product the product to check.
     * @param amount  the amount bought.
     * @return the amount of money it would cost to buy some amount of items.
     */
    public static double checkBuyCost(@Nonnull final Product product, final int amount) {
        float cost = 0;
        int tDemand = product.demand;
        for (int i = 0; i < amount; i++) {
            cost += product.price * ((float) tDemand / (float) product.supply);
            tDemand++;
        }
        return cost;
    }

    /**
     * Sell an amount of an item to the server.
     *
     * @param product the product to sell.
     * @param jdbcUrl the url of the database.
     * @param uuid    the uuid of the player setting the price.
     * @param amount  the amount sold.
     * @return the amount of money to take from the player.
     *
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static double buy(@Nonnull Product product, @Nonnull final String jdbcUrl, @Nonnull final String uuid,
                             final int amount) throws SQLException {
        SqlService.insertTransaction(jdbcUrl, uuid, SqlService.BUY_ACTION, product.type, amount);
        float returnValue = 0;
        for (int i = 0; i < amount; i++) {
            returnValue += product.price * ((float) product.demand / (float) product.supply);
            product.demand++;
        }
        return returnValue;
    }

    /**
     * Sets the price of a product.
     *
     * @param product the product to alter.
     * @param jdbcUrl the url of the database.
     * @param uuid    the uuid of the player setting the price.
     * @param price   the price to be set.
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or an
     *                      argument is supplied to this method. If a database access error occurs or the url is null.
     */
    public static void setPrice(@Nonnull Product product, @Nonnull final String jdbcUrl, @Nonnull final String uuid,
                                final float price) throws SQLException {
        SqlService.insertTransaction(jdbcUrl, uuid, SqlService.SET_ACTION, product.type, price);
        product.price = price;
    }
}