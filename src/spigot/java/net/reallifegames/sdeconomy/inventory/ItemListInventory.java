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
package net.reallifegames.sdeconomy.inventory;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * Holds data and functions related to the item list inventory.
 *
 * @author Tyler Bucher
 */
public final class ItemListInventory {

    /**
     * The root inventory page node.
     */
    @Nonnull
    private static final InventoryPage rootPageNode = new InventoryPage(true, 0);

    /**
     * The name of SdEconomy inventory's.
     */
    @Nonnull
    public static final String INVENTORY_NAME = "SdEconomy Item List";

    /**
     * Adds item stacks to the page nodes.
     *
     * @param itemStacks the item stacks to add.
     */
    public static void addItemStacks(@Nonnull final ItemStack... itemStacks) {
        for (ItemStack itemStack : itemStacks) {
            rootPageNode.addItem(itemStack);
        }
    }

    /**
     * Creates a new inventory.
     *
     * @return the newly created inventory on page 0.
     */
    public static Inventory createInventory() {
        final Inventory itemListInventory = Bukkit.createInventory(null, 54, INVENTORY_NAME);
        itemListInventory.setContents(ItemListInventory.rootPageNode.getItems());
        return itemListInventory;
    }

    /**
     * Returns a page node at the given index.
     *
     * @param index the index to fetch a node for.
     * @return the fetched node at the given index.
     */
    public static InventoryPage getPageNode(final int index) {
        InventoryPage pageNode = null;
        for (int i = 0; i <= index; i++) {
            if (pageNode == null) {
                pageNode = rootPageNode;
            } else {
                pageNode = pageNode.nextPageNode;
            }
        }
        return pageNode;
    }
}
