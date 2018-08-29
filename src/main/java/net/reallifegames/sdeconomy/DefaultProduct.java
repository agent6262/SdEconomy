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

/**
 * A simple data structure to represent an item in memory.
 *
 * @author Tyler Bucher
 */
public class DefaultProduct {

    /**
     * The type of this item.
     */
    @Nonnull
    public final String alias;

    /**
     * The minecraft type of this item.
     */
    @Nonnull
    public String type;

    /**
     * The minecraft unsafe data value of this item.
     */
    public byte unsafeData;

    /**
     * The price modification factor of this item.
     */
    protected float modFactor;

    /**
     * The current price of this item.
     */
    protected float price;

    /**
     * The supply of this item.
     */
    public int supply;

    /**
     * The current demand of this item.
     */
    public int demand;

    /**
     * The amount of an item to remove from this product.
     */
    public int decayAmount;

    /**
     * The amount of time in milliseconds to remove some amount of items from this product.
     */
    public long decayInterval;

    /**
     * The type of decay to use.
     */
    public byte decayType;

    /**
     * Creates a new {@link DefaultProduct} with the price, supply and demand set to 1.
     *
     * @param alias the user friendly name of the item.
     * @param type  the minecraft item type of this product.
     */
    public DefaultProduct(@Nonnull final String alias, @Nonnull final String type) {
        this.alias = alias;
        this.type = type;
        this.unsafeData = 0;
        this.modFactor = 0.1f;
        this.price = 1;
        this.supply = 1;
        this.demand = 1;
        this.decayAmount = 64;
        this.decayInterval = 43200000;
        this.decayType = SqlService.DECAY_CONST_TYPE;
    }

    /**
     * Creates a new {@link DefaultProduct} with a price, supply and demand.
     *
     * @param alias      the user friendly name of the item.
     * @param type       the item type of this product.
     * @param unsafeData the unsafe data value of this item.
     * @param price      the current price of this item.
     * @param supply     the supply of this item.
     * @param demand     the current demand of this item.
     */
    public DefaultProduct(@Nonnull final String alias, @Nonnull final String type, byte unsafeData, float modFactor,
                          float price, int supply, int demand, int decayAmount, long decayInterval, byte decayType) {
        this.alias = alias;
        this.type = type;
        this.unsafeData = unsafeData;
        this.modFactor = modFactor;
        this.price = price;
        this.supply = supply;
        this.demand = demand;
        this.decayAmount = decayAmount;
        this.decayInterval = decayInterval;
        this.decayType = decayType;

    }

    /**
     * @return the current price of this {@link DefaultProduct product}.
     */
    public float getPrice() {
        return price;
    }

    /**
     * @return the current demand of this {@link DefaultProduct product}.
     */
    public float getModFactor() {
        return modFactor;
    }
}
