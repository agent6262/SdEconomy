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

import net.reallifegames.sdeconomy.DefaultEconomy;
import net.reallifegames.sdeconomy.DefaultProduct;
import net.reallifegames.sdeconomy.SdEconomy;
import net.reallifegames.sdeconomy.SpigotDefaultEconomy;
import net.reallifegames.sdeconomy.inventory.InventoryUtility;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;

/**
 * Buys an item from the server.
 *
 * @author Tyler Bucher
 */
final class BuyCommand extends BaseCommand {

    /**
     * Creates a new base command listener.
     *
     * @param pluginInstance the {@link SdEconomy} plugin instance.
     */
    public BuyCommand(@Nonnull final SdEconomy pluginInstance) {
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
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            sender.sendMessage(ChatColor.GOLD + "" + player.getInventory().getStorageContents().length);
            // Check for arg length
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "You need to specify the item name and price.");
                return false;
            }
            // Get amount of an item
            final int amount;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException | NullPointerException e) {
                sender.sendMessage(ChatColor.RED + args[1] + " is not a number.");
                return false;
            }
            // Get defaultProduct and item
            final DefaultProduct defaultProduct = SpigotDefaultEconomy.stockPrices.get(args[0]);
            if (defaultProduct == null) {
                sender.sendMessage(ChatColor.GOLD + "The price of `" + args[0] + "` has not been set yet.");
                return true;
            }
            final Material material = Material.getMaterial(defaultProduct.type);
            if (material == null) {
                // Send player message
                sender.sendMessage(ChatColor.RED + "Invalid Item type.");
                return true;
            }
            // Check if there is a max buy amount
            if (pluginInstance.getConfiguration().isUseMaxItemsPerBuy()) {
                final int maxAmount = pluginInstance.getConfiguration().getMaxItemsPerBuy();
                if (amount > maxAmount) {
                    sender.sendMessage(ChatColor.RED + "You can only buy a max of `" + maxAmount + "` per command.");
                    return true;
                }
            }
            // Check if the plugin is not allowed to item drop
            if (!pluginInstance.getConfiguration().isAllowItemDrop()) {
                final int itemsLeft = InventoryUtility.canInventoryHold(player.getInventory(), amount);
                if (itemsLeft > 0) {
                    sender.sendMessage(ChatColor.RED + "Your inventory can only hold a max of `" + (amount - itemsLeft) + "` items currently.");
                    return true;
                }
            }
            // Get player returns and add to player account
            double cost = DefaultEconomy.checkBuyCost(defaultProduct, amount);
            final double playerBalance = pluginInstance.getEconomyService().getBalance(player);
            if (playerBalance >= cost) {
                // Withdraw from player
                try {
                    cost = DefaultEconomy.buy(defaultProduct, pluginInstance.getConfiguration().getJdbcUrl(),
                            player.getUniqueId().toString(), amount);
                } catch (SQLException e) {
                    pluginInstance.getLogger().log(Level.SEVERE, "Unable to access database.", e);
                    sender.sendMessage(ChatColor.RED + "Error buying item.");
                    return true;
                }
                pluginInstance.getEconomyService().withdrawPlayer(player, cost);
                // Send player message
                sender.sendMessage(ChatColor.GREEN + "You received " + pluginInstance.decimalFormat.format(amount) + " " + args[0] + ".");
                // A note to all future and current maintainers; As of 7/27/2018 the bukkit / spigot api
                // seems to be in a tentative state for creating items stacks with specific meta data.
                // This could be because of the current state of the minecraft server 'api' which spigot
                // is built on. Once a safer and non deprecated method becomes available this constructor
                // should be removed in favor of said method.
                final Map<Integer, ItemStack> leftOverItems = player.getInventory()
                        .addItem(new ItemStack(material, amount, (short) 0, defaultProduct.unsafeData));
                leftOverItems.forEach((k, v)->player.getWorld().dropItem(player.getLocation(), v));
                return true;
            } else {
                // Send player message
                sender.sendMessage(ChatColor.RED + "You do not have enough funds to buy this much.");
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You must be a player to run this command.");
            return false;
        }
    }
}
