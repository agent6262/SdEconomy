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
import java.util.logging.Level;

/**
 * Sells an item to the server.
 *
 * @author Tyler Bucher
 */
public class SellCommand extends BaseCommand {

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
            // Get product and item
            final ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand.getType().equals(Material.AIR)) {
                sender.sendMessage(ChatColor.RED + "You must have an item in your hand to run this command this way.");
                return false;
            }
            final Product product = pluginInstance.getProductFromItemStack(itemInHand);
            if (product == null) {
                sender.sendMessage(ChatColor.GOLD + "The price of `" + args[0] + "` has not been set yet.");
                return true;
            }
            // Get player returns and add to player account
            final double returns;
            try {
                returns = Product.sell(product, pluginInstance.getConfig().getString("jdbcUrl"),
                        player.getUniqueId().toString(), itemInHand.getAmount());
            } catch (SQLException e) {
                pluginInstance.getLogger().log(Level.SEVERE, "Unable to access database.", e);
                sender.sendMessage(ChatColor.RED + "Error selling item.");
                return true;
            }
            pluginInstance.getEconomyService().depositPlayer(player, returns);
            // Send player message
            sender.sendMessage(ChatColor.GREEN + "You received " + returns + " "
                    + pluginInstance.getEconomyService().currencyNamePlural() + ".");
            player.getInventory().getItemInMainHand().setAmount(0);
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "You must be a player to run this command.");
            return false;
        }
    }
}
