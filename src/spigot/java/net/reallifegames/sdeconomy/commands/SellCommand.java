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
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Sells an item to the server.
 *
 * @author Tyler Bucher
 */
final class SellCommand extends BaseCommand {

    /**
     * Creates a new base command listener.
     *
     * @param pluginInstance the {@link SdEconomy} plugin instance.
     */
    public SellCommand(@Nonnull final SdEconomy pluginInstance) {
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
            // Get defaultProduct and item
            final ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand.getType().equals(Material.AIR)) {
                sender.sendMessage(ChatColor.RED + "You must have an item in your hand to run this command this way.");
                return false;
            }
            final DefaultProduct defaultProduct = SpigotDefaultEconomy.getProductFromItemStack(itemInHand);
            if (defaultProduct == null) {
                sender.sendMessage(ChatColor.RED + "Error selling item.");
                return true;
            }
            // Get player returns and add to player account
            final double returns;
            try {
                returns = DefaultEconomy.sell(defaultProduct, pluginInstance.getConfiguration().getJdbcUrl(),
                        player.getUniqueId().toString(), itemInHand.getAmount());
            } catch (SQLException e) {
                pluginInstance.getLogger().log(Level.SEVERE, "Unable to access database.", e);
                sender.sendMessage(ChatColor.RED + "Error selling item.");
                return true;
            }
            pluginInstance.getEconomyService().depositPlayer(player, returns);
            // Send player message
            sender.sendMessage(ChatColor.GREEN + "You received " + pluginInstance.decimalFormat.format(returns) + " "
                    + pluginInstance.getEconomyService().currencyNamePlural() + ".");
            player.getInventory().getItemInMainHand().setAmount(0);
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "You must be a player to run this command.");
            return false;
        }
    }
}
