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
package net.reallifegames.sdeconomy;

import net.milkbowl.vault.economy.Economy;
import net.reallifegames.sdeconomy.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * The SdEconomy plugin main class.
 *
 * @author Tyler Bucher
 */
public class SdEconomy extends JavaPlugin {

    /**
     * The price map for the products.
     */
    private HashMap<String, Product> stockPrices = null;

    /**
     * The economy service provided by vault.
     */
    private Economy economyService;

    /**
     * Called when this plugin is enabled.
     */
    @Override
    public void onEnable() {
        // Load plugin and prices
        stockPrices = new HashMap<>();
        // Get the config
        final FileConfiguration config = this.getConfig();
        // Check to see if the jdbc url is present
        final String jdbcUrl = config.getString("jdbcUrl");
        // Check to see if the populateDatabase option is present
        final boolean populateDatabase = config.getBoolean("populateDatabase");
        // The save interval for the prices.
        final long interval = config.getLong("saveInterval");
        if (jdbcUrl == null) {
            config.addDefault("jdbcUrl", "jdbc:mysql://<host>:<port>/<db>?user=<user>&password=<password>&useSSL=false");
            config.addDefault("populateDatabase", false);
            config.addDefault("saveInterval", 6000);
            config.options().copyDefaults(true);
            saveConfig();
            return;
        }
        // Get vault plugin
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            this.getLogger().log(Level.SEVERE, "Vault plugin not found. Plugin not loaded");
            return;
        }
        // Get economy service
        final RegisteredServiceProvider<Economy> registeredServiceProvider = getServer().getServicesManager()
                .getRegistration(Economy.class);
        if (registeredServiceProvider == null) {
            this.getLogger().log(Level.SEVERE, "Economy service not found. Plugin not loaded");
            return;
        }
        economyService = registeredServiceProvider.getProvider();
        if (economyService == null) {
            this.getLogger().log(Level.SEVERE, "Economy service is null. Plugin not loaded");
            return;
        }
        // Load material names
        for (final Material material : Material.values()) {
            if (material.isItem()) {
                if (populateDatabase) {
                    stockPrices.computeIfAbsent(material.name(), k->new Product(material.name().toLowerCase(), material.name()));
                }
            }
        }
        try {
            SqlService.updateToSqlV2(jdbcUrl);
            SqlService.createProductTable(jdbcUrl);
            SqlService.createTransactionTable(jdbcUrl);
            SqlService.createConstantsTable(jdbcUrl);
            SqlService.readProductTable(jdbcUrl, stockPrices);
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Error accessing database. Plugin not loaded", e);
        }
        // Register commands
        // Set price command
        this.getCommand("setprice").setExecutor(new SetPriceCommand(this));
        // Remove price command
        this.getCommand("removeprice").setExecutor(new RemovePriceCommand(this));
        // Get price command
        this.getCommand("getprice").setExecutor(new GetPriceCommand(this));
        // Check sell command
        this.getCommand("checksell").setExecutor(new CheckSellCommand(this));
        // Check buy command
        this.getCommand("checkbuy").setExecutor(new CheckBuyCommand(this));
        // Sell command
        this.getCommand("sell").setExecutor(new SellCommand(this));
        // Buy command
        this.getCommand("buy").setExecutor(new BuyCommand(this));
        // Create repeating save task
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, ()->{
            // Attempt to save item data
            try {
                SqlService.updateProductTable(jdbcUrl, stockPrices);
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Error accessing database", e);
            }
        }, 80, interval);
    }

    /**
     * Called when this plugin is disabled.
     */
    @Override
    public void onDisable() {
        // Get the config
        final FileConfiguration config = this.getConfig();
        // Check to see if the jdbc url is present
        final String jdbcUrl = config.getString("jdbcUrl");
        if (jdbcUrl == null) {
            return;
        }
        // Attempt to save item data
        try {
            SqlService.updateProductTable(jdbcUrl, stockPrices);
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Error accessing database", e);
        }
    }

    /**
     * Gets a product from a given item stack.
     *
     * @param itemStack the item stack to check against.
     * @return the found product or null.
     */
    @Nullable
    public Product getProductFromItemStack(@Nonnull final ItemStack itemStack) {
        for (Product product : stockPrices.values()) {
            // if the item stack type and unsafeData equal the products then return it.
            if (product.type.equalsIgnoreCase(itemStack.getType().name()) && product.unsafeData == itemStack.getData().getData()) {
                return product;
            }
        }
        return null;
    }

    /**
     * @return the price map for the products.
     */
    public HashMap<String, Product> getStockPrices() {
        return stockPrices;
    }

    /**
     * @return the economy service provided by vault.
     */
    public Economy getEconomyService() {
        return economyService;
    }
}
