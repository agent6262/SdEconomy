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
package net.reallifegames.sdeconomy.commands;

import net.reallifegames.sdeconomy.SdEconomy;

import javax.annotation.Nonnull;

/**
 * Helps with command registration.
 *
 * @author Tyler Bucher
 */
public final class CommandRegistrar {

    /**
     * Registers commands for the SdEconomy plugin.
     *
     * @param sdEconomy the sd economy plugin instance.
     */
    public static void registerCommands(@Nonnull final SdEconomy sdEconomy) {
        // Set price command
        sdEconomy.getCommand("setprice").setExecutor(new SetPriceCommand(sdEconomy));
        // Remove price command
        sdEconomy.getCommand("removeprice").setExecutor(new RemovePriceCommand(sdEconomy));
        // Get price command
        sdEconomy.getCommand("getinfo").setExecutor(new GetInfoCommand(sdEconomy));
        // Check sell command
        sdEconomy.getCommand("checksell").setExecutor(new CheckSellCommand(sdEconomy));
        // Check buy command
        sdEconomy.getCommand("checkbuy").setExecutor(new CheckBuyCommand(sdEconomy));
        // Sell command
        sdEconomy.getCommand("sell").setExecutor(new SellCommand(sdEconomy));
        // Buy command
        sdEconomy.getCommand("buy").setExecutor(new BuyCommand(sdEconomy));
        // Transaction command
        sdEconomy.getCommand("transactions").setExecutor(new TransactionCommand(sdEconomy));
        // Version command
        sdEconomy.getCommand("sdversion").setExecutor(new GetVersionCommand(sdEconomy));
        // Sd items command
        sdEconomy.getCommand("sditems").setExecutor(new SdItemsCommand(sdEconomy));
    }
}
