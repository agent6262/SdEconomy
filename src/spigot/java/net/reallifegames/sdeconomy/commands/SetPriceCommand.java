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
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Attempts to set the price of an item.
 *
 * @author Tyler Bucher
 */
public class SetPriceCommand extends BaseCommand {

    /**
     * Creates a new base command listener.
     *
     * @param pluginInstance the {@link SdEconomy} plugin instance.
     */
    public SetPriceCommand(@Nonnull final SdEconomy pluginInstance) {
        super(pluginInstance);
    }

    /**
     * Executes the command, returning its success.
     *
     * @param sender       source object which is executing this command.
     * @param commandLabel the alias of the command used.
     * @param args         all arguments passed to the command, split via ' '.
     * @return true if the command was successful, otherwise false.
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            // Check for arg length
            if (args.length != 3) {
                sender.sendMessage(ChatColor.RED + "You need to specify the product name, type and price.");
                return false;
            }
            // Get price
            final float price;
            try {
                price = Float.parseFloat(args[2]);
            } catch (NumberFormatException | NullPointerException e) {
                sender.sendMessage(ChatColor.RED + args[2] + " is not a number.");
                return false;
            }
            final String[] itemTypeInfo = args[1].split(":");
            final byte unsafeData;
            try {
                unsafeData = itemTypeInfo.length == 2 ? Byte.parseByte(itemTypeInfo[1]) : 0;
            } catch (NumberFormatException | NullPointerException e) {
                sender.sendMessage(ChatColor.RED + itemTypeInfo[1] + " is not a number.");
                return false;
            }
            itemTypeInfo[0] = itemTypeInfo[0].toUpperCase();
            final Product product = pluginInstance.getStockPrices().computeIfAbsent(args[0],
                    k->new Product(itemTypeInfo[0]));
            product.type = itemTypeInfo[0];
            product.unsafeData = unsafeData;
            try {
                Product.setPrice(product, pluginInstance.getConfig().getString("jdbcUrl"),
                        player.getUniqueId().toString(), price);
            } catch (SQLException e) {
                pluginInstance.getLogger().log(Level.SEVERE, "Unable to access database.", e);
                sender.sendMessage(ChatColor.RED + "Error setting item price.");
                return true;
            }
            sender.sendMessage(ChatColor.GREEN + "The price of `" + args[0] + "` has been set to " + product.getPrice());
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "You must be a player to run this command.");
            return false;
        }
    }
}
