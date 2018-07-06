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
            sender.sendMessage(ChatColor.GOLD + "The price of `" + args[0] + "` is: " + product.getPrice());
        }
        return true;
    }
}
