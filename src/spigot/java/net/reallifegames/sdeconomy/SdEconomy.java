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
import net.reallifegames.sdeconomy.commands.CommandRegistrar;
import net.reallifegames.sdeconomy.inventory.InventoryUtility;
import net.reallifegames.sdeconomy.inventory.ItemListInventory;
import net.reallifegames.sdeconomy.listeners.InventoryClickListener;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.logging.Level;

/**
 * The SdEconomy plugin main class.
 *
 * @author Tyler Bucher
 */
public class SdEconomy extends JavaPlugin {

    /**
     * The {@link Economy} service provided by vault.
     */
    private Economy economyService;

    /**
     * The {@link DecimalFormat decimal formatter} for printing money.
     */
    public DecimalFormat decimalFormat;

    /**
     * The {@link Configuration} for this plugin.
     */
    private Configuration configuration;

    /**
     * Called when this {@link JavaPlugin plugin} is enabled.
     */
    @Override
    public void onEnable() {
        // Setup the decimal formatter
        decimalFormat = new DecimalFormat(".####");
        decimalFormat.setRoundingMode(RoundingMode.DOWN);
        // Get the config
        configuration = new Configuration(this);
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
        if (configuration.isPopulateDatabase()) {
            for (final Material material : Material.values()) {
                if (material.isItem()) {
                    DefaultEconomy.stockPrices.computeIfAbsent(material.name(), k->new DefaultProduct(material.name().toLowerCase(), material.name()));
                }
            }
        }
        // Setup sql data
        try {
            SqlService.createConstantsTable(configuration.getJdbcUrl());
            SqlService.createDefaultProductTable(configuration.getJdbcUrl());
            SqlService.createUuidTable(configuration.getJdbcUrl());
            SqlService.createDefaultTransactionTable(configuration.getJdbcUrl());
            SqlService.setSqlVersion(configuration.getJdbcUrl());
            SqlService.updateToSqlV4(configuration.getJdbcUrl());
            SqlService.updateToSqlV5(configuration.getJdbcUrl());
            SqlService.updateToSqlV6(configuration.getJdbcUrl());
            SqlService.readDefaultProductTable(configuration.getJdbcUrl(), DefaultEconomy.stockPrices);
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Error accessing database. Plugin not loaded", e);
            return;
        }
        // Register commands
        CommandRegistrar.registerCommands(this);
        // Create repeating save task
        SpigotDefaultEconomy.createSaveTask(this);
        // Create repeating decay tasks
        SpigotDefaultEconomy.createDecayTasks(this);
        // Setup Inventory data
        ItemListInventory.addItemStacks(InventoryUtility.getItemStacksFromProducts(DefaultEconomy.stockPrices.values()));
        // Register event listeners
        this.getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
    }

    /**
     * Called when this {@link JavaPlugin plugin} is disabled.
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
            SqlService.updateDefaultProductTable(jdbcUrl, DefaultEconomy.stockPrices);
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Error accessing database", e);
        }
    }

    /**
     * @return the {@link Economy economy service} provided by vault.
     */
    public Economy getEconomyService() {
        return economyService;
    }

    /**
     * @return the {@link Configuration} for this plugin.
     */
    public Configuration getConfiguration() {
        return configuration;
    }
}
