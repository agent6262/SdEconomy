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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests to see if a given product can print money.
 *
 * @author Tyler Bucher
 */
public class DefaultProductTest {

    /**
     * The product to test.
     */
    private DefaultProduct testDefaultProduct;

    /**
     * Initial setup for the test product.
     */
    @Before
    public void setUp() {
        testDefaultProduct = new DefaultProduct("test", "test");
    }

    /**
     * Tests all possible values.
     */
    @Test
    public void testProductMoneyExchange() {
        // Price loop
        for (int i = 1; i <= 20; i++) {
            // Demand loop
            for (int j = 1; j <= 100; j++) {
                // Supply loop
                for (int k = 1; k <= 100; k++) {
                    // Buy/Sell amount
                    for (int l = 1; l <= 100; l++) {
                        DefaultEconomy.setPriceNoSql(testDefaultProduct, (float) i / 5.0f);
                        testDefaultProduct.demand = j;
                        testDefaultProduct.supply = k;
                        double cost = DefaultEconomy.buyNoSql(testDefaultProduct, l);
                        double returns = DefaultEconomy.sellNoSql(testDefaultProduct, l);
                        Assert.assertTrue("A product can print money and cause inflation.", returns <= cost);
                    }
                }
            }
        }
    }
}
