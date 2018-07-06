package net.reallifegames.sdeconomy.commands;

import net.reallifegames.sdeconomy.Product;
import net.reallifegames.sdeconomy.SdEconomy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

/**
 * Checks the amount of money it will cost to buy an amount of an item.
 *
 * @author Tyler Bucher
 */
public class CheckBuyCommand extends BaseCommand {

    /**
     * Creates a new base command listener.
     *
     * @param pluginInstance the {@link SdEconomy} plugin instance.
     */
    public CheckBuyCommand(@Nonnull final SdEconomy pluginInstance) {
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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check item in hand if no arguments
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "You need to specify the item name and amount.");
            return false;
        } else {
            final int amount;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException | NullPointerException e) {
                sender.sendMessage(ChatColor.RED + args[1] + " is not a number.");
                return false;
            }
            final Product product = pluginInstance.getStockPrices().get(args[0]);
            if (product == null) {
                sender.sendMessage(ChatColor.GOLD + "The price of `" + args[0] + "` has not been set yet.");
                return true;
            }
            sender.sendMessage(ChatColor.GOLD + "It will cost " + Product.checkBuyCost(product, amount) + " "
                    + pluginInstance.getEconomyService().currencyNamePlural() + " to buy '" + args[0] + "'");
            return true;
        }
    }
}
