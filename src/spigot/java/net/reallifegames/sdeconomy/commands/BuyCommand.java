package net.reallifegames.sdeconomy.commands;

import net.reallifegames.sdeconomy.Product;
import net.reallifegames.sdeconomy.SdEconomy;
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
public class BuyCommand extends BaseCommand {

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
            // Get product and item
            final Product product = pluginInstance.getStockPrices().get(args[0]);
            if (product == null) {
                sender.sendMessage(ChatColor.GOLD + "The price of `" + args[0] + "` has not been set yet.");
                return true;
            }
            final Material material = Material.getMaterial(product.type);
            if (material == null) {
                // Send player message
                sender.sendMessage(ChatColor.RED + "Invalid Item type.");
                return true;
            }
            // Get player returns and add to player account
            double cost = Product.checkBuyCost(product, amount);
            final double playerBalance = pluginInstance.getEconomyService().getBalance(player);
            if (playerBalance >= cost) {
                // Withdraw from player
                try {
                    cost = Product.buy(product, pluginInstance.getConfig().getString("jdbcUrl"),
                            player.getUniqueId().toString(), amount);
                } catch (SQLException e) {
                    pluginInstance.getLogger().log(Level.SEVERE, "Unable to access database.", e);
                    sender.sendMessage(ChatColor.RED + "Error buying item.");
                    return true;
                }
                pluginInstance.getEconomyService().withdrawPlayer(player, cost);
                // Send player message
                sender.sendMessage(ChatColor.GREEN + "You received " + amount + " " + args[0] + ".");
                final Map<Integer, ItemStack> leftOverItems = player.getInventory()
                        .addItem(new ItemStack(material, amount, (short) 0, product.unsafeData));
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
