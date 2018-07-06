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

/**
 * Checks the amount of money you will receive if you sold at this instant.
 *
 * @author Tyler Bucher
 */
public class CheckSellCommand extends BaseCommand {

    /**
     * Creates a new base command listener.
     *
     * @param pluginInstance the {@link SdEconomy} plugin instance.
     */
    public CheckSellCommand(@Nonnull final SdEconomy pluginInstance) {
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
        // Check item in hand if no arguments
        if (args.length == 0) {
            if (sender instanceof Player) {
                final Player player = (Player) sender;
                final ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand.getType().equals(Material.AIR)) {
                    sender.sendMessage(ChatColor.RED + "You must have an item in your hand to run this command this way.");
                    return false;
                }
                final Product product = pluginInstance.getProductFromItemStack(itemInHand);
                if (product == null) {
                    sender.sendMessage(ChatColor.GOLD + "The price of `" + itemInHand.getType().name() + "` has not been set yet.");
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "You will receive " + Product.checkSellReturns(product, itemInHand.getAmount()) + " "
                        + pluginInstance.getEconomyService().currencyNamePlural() + ".");
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player to run this command with out any arguments.");
                return false;
            }
        } else if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "You need to specify the item name and amount.");
            return false;
        } else {
            // Convert item type to uppercase
            final int amount;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException | NullPointerException e) {
                sender.sendMessage(ChatColor.RED + args[1] + " is not a number.");
                return false;
            }
            final Product product = pluginInstance.getStockPrices().get(args[0]);//todo
            if (product == null) {
                sender.sendMessage(ChatColor.GOLD + "The price of `" + args[0] + "` has not been set yet.");
                return true;
            }
            sender.sendMessage(ChatColor.GOLD + "You will receive " + Product.checkSellReturns(product, amount) + " "
                    + pluginInstance.getEconomyService().currencyNamePlural() + ".");
            return true;
        }
    }
}
