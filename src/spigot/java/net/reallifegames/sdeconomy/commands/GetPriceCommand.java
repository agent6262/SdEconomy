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

import net.reallifegames.sdeconomy.Product;
import net.reallifegames.sdeconomy.SdEconomy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

/**
 * Attempts to get the price of an item.
 *
 * @author Tyler Bucher.
 */
public class GetPriceCommand extends BaseCommand {

    /**
     * Creates a new base command listener.
     *
     * @param pluginInstance the {@link SdEconomy} plugin instance.
     */
    public GetPriceCommand(@Nonnull final SdEconomy pluginInstance) {
        super(pluginInstance);
    }

    /**
     * Executes the given command, returning its success.
     *
     * @param sender  source of the command.
     * @param command command which was executed.
     * @param label   alias of the command which was used.
     * @param args    passed command arguments.
     * @return true if a valid command, otherwise false.
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        // Check for arg length
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "You need to specify the item name.");
            return false;
        }
        // Get item price
        final Product product = pluginInstance.getStockPrices().get(args[0]);
        if (product == null) {
            sender.sendMessage(ChatColor.GOLD + "The price of `" + args[0] + "` has not been set yet.");
        } else {
            sender.sendMessage(ChatColor.GOLD + "The price of `" + args[0] + "` is: " +
                    pluginInstance.decimalFormat.format(product.getPrice()));
        }
        return true;
    }
}
