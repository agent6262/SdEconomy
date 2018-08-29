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

import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nonnull;

/**
 * The config object for the SdEconomy plugin.
 *
 * @author Tyler Bucher
 */
public class Configuration {

    /**
     * The jdbc database url.
     */
    private String jdbcUrl;

    /**
     * States if the plugin should attempt to auto populate the database.
     */
    private boolean populateDatabase;

    /**
     * How long in ticks till the products are saved to the database.
     */
    private long saveInterval;

    /**
     * States if there should be a maximum amount per buy command.
     */
    private boolean useMaxItemsPerBuy;

    /**
     * The maximum amount of items you can buy per command.
     */
    private int maxItemsPerBuy;

    /**
     * Can the plugin drop items on the ground.
     */
    private boolean allowItemDrop;

    /**
     * Creates a new configuration object.
     *
     * @param pluginInstance the {@link SdEconomy} plugin instance.
     */
    public Configuration(@Nonnull final SdEconomy pluginInstance) {
        // Set defaults
        Configuration.setDefaults(pluginInstance);
        // Get spigot config object
        final FileConfiguration config = pluginInstance.getConfig();
        //Set config values
        this.jdbcUrl = config.getString("jdbcUrl");
        this.populateDatabase = config.getBoolean("populateDatabase");
        this.saveInterval = config.getLong("saveInterval");
        this.useMaxItemsPerBuy = config.getBoolean("useMaxItemsPerBuy");
        this.maxItemsPerBuy = config.getInt("maxItemsPerBuy");
        this.allowItemDrop = config.getBoolean("allowItemDrop");
    }

    /**
     * Sets the defaults
     *
     * @param pluginInstance the {@link SdEconomy} plugin instance.
     */
    private static void setDefaults(@Nonnull final SdEconomy pluginInstance) {
        // Get spigot config object
        final FileConfiguration config = pluginInstance.getConfig();
        // Set the plugin defaults
        config.addDefault("jdbcUrl", "jdbc:mysql://<host>:<port>/<db>?user=<user>&password=<password>&useSSL=false");
        config.addDefault("populateDatabase", false);
        config.addDefault("saveInterval", 6000);
        config.addDefault("demandDecayInterval", 24000);
        config.addDefault("demandDecayAmount", 64);
        config.addDefault("useMaxItemsPerBuy", false);
        config.addDefault("maxItemsPerBuy", 64);
        config.addDefault("allowItemDrop", true);
        config.options().copyDefaults(true);
        pluginInstance.saveConfig();
        pluginInstance.reloadConfig();
    }

    /**
     * @return the jdbc database url.
     */
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    /**
     * @return true if the plugin should attempt to auto populate the database.
     */
    public boolean isPopulateDatabase() {
        return populateDatabase;
    }

    /**
     * @return how long in ticks till the products are saved to the database.
     */
    public long getSaveInterval() {
        return saveInterval;
    }

    /**
     * @return true if there should be a maximum amount per buy command.
     */
    public boolean isUseMaxItemsPerBuy() {
        return useMaxItemsPerBuy;
    }

    /**
     * @return the maximum amount of items you can buy per command.
     */
    public int getMaxItemsPerBuy() {
        return maxItemsPerBuy;
    }

    /**
     * @return true if the plugin drop items on the ground.
     */
    public boolean isAllowItemDrop() {
        return allowItemDrop;
    }
}
