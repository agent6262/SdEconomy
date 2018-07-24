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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.Collection;
import java.util.logging.Level;

import static net.reallifegames.sdeconomy.SqlService.SEARCH_USERS_TRANSACTIONS;

/**
 * Gets the transactions for a user.
 *
 * @author Tyler Bucher.
 */
public class TransactionCommand extends BaseCommand {

    /**
     * Creates a new base command listener.
     *
     * @param pluginInstance the {@link SdEconomy} plugin instance.
     */
    public TransactionCommand(@Nonnull final SdEconomy pluginInstance) {
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
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "You need to specify the player name or and the page number.");
            return false;
        }
        // Get amount of an item
        final int pageNumber;
        try {
            int parse = args.length == 1 ? 0 : Integer.parseInt(args[1]) - 1;
            pageNumber = parse < 0 ? 0 : parse;
        } catch (NumberFormatException | NullPointerException e) {
            sender.sendMessage(ChatColor.RED + args[1] + " is not a number.");
            return false;
        }
        // Get player uuid
        String uuid = null;
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName().equalsIgnoreCase(args[0])) {
                uuid = offlinePlayer.getUniqueId().toString();
            }
        }
        // Return if uuid is null
        if (uuid == null) {
            sender.sendMessage("Player has no rows for this page number or has not interacted with the economy yet.");
            return true;
        }
        // Connect to database
        try {
            final Connection sqlConnection = DriverManager.getConnection(pluginInstance.getConfig().getString("jdbcUrl"));
            // Setup prepared statement
            final PreparedStatement searchStatement = sqlConnection.prepareStatement(SEARCH_USERS_TRANSACTIONS);
            searchStatement.setString(1, uuid);
            searchStatement.setInt(2, pageNumber);
            // Execute query
            final ResultSet resultSet = searchStatement.executeQuery();
            if (!resultSet.next()) {
                sender.sendMessage("Player has no rows for this page number or has not interacted with the economy yet.");
                return true;
            }
            final StringBuilder builder = new StringBuilder();
            final int aliasMaxLength = getMaxLength(pluginInstance.getStockPrices().values());
            do {
                builder.append(ChatColor.DARK_AQUA)
                        .append(postPadString(getTextAction(resultSet.getInt("action")), 9)).append(' ')
                        .append(ChatColor.RESET).append(postPadString(resultSet.getString("alias"), aliasMaxLength))
                        .append(' ').append(ChatColor.GOLD)
                        .append(postPadString(resultSet.getString("date"), 19)).append(' ')
                        .append(ChatColor.GREEN).append(resultSet.getFloat("amount"));
                sender.sendMessage(builder.toString());
                builder.setLength(0);
            } while (resultSet.next());
            // Close objects
            resultSet.close();
            searchStatement.close();
            sqlConnection.close();
            return true;
        } catch (SQLException e) {
            pluginInstance.getLogger().log(Level.SEVERE, "Unable to access database.", e);
            sender.sendMessage(ChatColor.RED + "Error connecting to db.");
            return true;
        }
    }

    /**
     * @param action the action to get text for.
     * @return the action or null if not found.
     */
    @Nonnull
    private static String getTextAction(final int action) {
        switch (action) {
            case 0:
                return "SET PRICE";
            case 1:
                return "BUY";
            case 2:
                return "SELL";
        }
        return "";
    }

    /**
     * @param text      the text to check and pad.
     * @param maxLength the maximum length of the padded string to match.
     * @return the newly padded string or the passed original.
     */
    private static String postPadString(@Nonnull final String text, final int maxLength) {
        if (text.length() < maxLength) {
            final StringBuilder builder = new StringBuilder(text);
            for (int i = 0; i < maxLength - text.length(); i++) {
                builder.append(' ');
            }
            return builder.toString();
        }
        return text;
    }

    /**
     * @param productList the list of products to check
     * @return the maximum length of an alias.
     */
    private static int getMaxLength(@Nonnull final Collection<Product> productList) {
        int length = 0;
        for (final Product product : productList) {
            if (product.alias.length() > length) {
                length = product.alias.length();
            }
        }
        return length;
    }
}
