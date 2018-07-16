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

import net.reallifegames.sdeconomy.SdEconomy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.sql.*;
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
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "You need to specify the player uuid and page number.");
            return false;
        }
        // Get amount of an item
        final int pageNumber;
        try {
            int parse = Integer.parseInt(args[1]) - 1;
            pageNumber = parse < 1 ? 1 : parse;
        } catch (NumberFormatException | NullPointerException e) {
            sender.sendMessage(ChatColor.RED + args[1] + " is not a number.");
            return false;
        }
        // Connect to database
        try {
            final Connection sqlConnection = DriverManager.getConnection(pluginInstance.getConfig().getString("jdbcUrl"));
            // Setup prepared statement
            final PreparedStatement searchStatement = sqlConnection.prepareStatement(SEARCH_USERS_TRANSACTIONS);
            searchStatement.setString(1, args[0]);
            searchStatement.setInt(2, pageNumber - 1);
            // Execute query
            final ResultSet resultSet = searchStatement.executeQuery();
            boolean returns = false;
            while (resultSet.next()) {
                returns = true;
                sender.sendMessage(getTextAction(resultSet.getInt("action")) + " " +
                        resultSet.getString("alias") + " " + resultSet.getString("date")
                        + " " + resultSet.getFloat("amount"));
            }
            // Close objects
            resultSet.close();
            searchStatement.close();
            sqlConnection.close();
            if (!returns) {
                sender.sendMessage("Player has no rows for this page number or has not interacted with the economy yet.");
            }
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
    private static String getTextAction(final int action) {
        switch (action) {
            case 0:
                return "SET PRICE";
            case 1:
                return "BUY";
            case 2:
                return "SELL";
        }
        return null;
    }
}
