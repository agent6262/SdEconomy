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
 * Tests to see if a given
 *
 * @author Tyler Bucher
 */
public class ProductTest {

    private Product testProduct;

    @Before
    public void setUp() {
        testProduct = new Product("test", "test");
    }

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
                        Product.setPriceNoSql(testProduct, i);//(float) i / 5.0f
                        testProduct.demand = j;
                        testProduct.supply = k;
                        //System.out.println(testProduct.getPrice()+ " "+testProduct.demand+" "+testProduct.supply+" "+l);
                        double cost = Product.buyNoSql(testProduct, l);
                        double returns = Product.sellNoSql(testProduct, l);
                        Assert.assertTrue("A product can print money and cause inflation."+" "+testProduct.getPrice()+
                                " "+testProduct.demand+" "+testProduct.supply+" "+l+" "+cost+" "+returns, returns <= cost);
                    }
                }
            }
        }
    }
}
