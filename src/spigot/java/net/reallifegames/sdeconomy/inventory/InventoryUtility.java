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

import net.reallifegames.sdeconomy.DefaultProduct;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Contains utility functions for spigot inventorys.
 *
 * @author Tyler Bucher
 */
public class InventoryUtility {

    /**
     * Checks to see if a inventory can hold the total amount of items.
     *
     * @param playerInventory the inventory to check.
     * @param totalItems      the amount of items to add.
     * @return true if there are less then 0 items left.
     */
    public static int canInventoryHold(@Nonnull final PlayerInventory playerInventory, final int totalItems) {
        // Current list of items to remove
        int itemsLeft = totalItems;
        final int maxStackSize = playerInventory.getMaxStackSize();
        // Check to see if all itemsLeft can be removed
        for (ItemStack itemStack : playerInventory.getStorageContents()) {
            if (itemStack == null) {
                itemsLeft -= maxStackSize;
            }
        }
        // If there are less then 0 left than return true
        return itemsLeft;
    }

    /**
     * Creates a item stack array with item meta from a list of defaultProducts.
     *
     * @param defaultProducts the list of defaultProducts to create a item stack array from.
     * @return the create item stack array.
     */
    public static ItemStack[] getItemStacksFromProducts(@Nonnull final Collection<DefaultProduct> defaultProducts) {
        final List<DefaultProduct> sortedDefaultProducts = new ArrayList<>(defaultProducts);
        sortedDefaultProducts.sort(Comparator.comparing(o->o.alias));
        // Create item stack array
        final ItemStack[] itemStacks = new ItemStack[sortedDefaultProducts.size()];
        // item stack index counter
        int itemStackIndex = 0;
        for (DefaultProduct defaultProduct : sortedDefaultProducts) {
            // Get defaultProduct material type
            final Material material = Material.getMaterial(defaultProduct.type);
            if (material != null) {
                // Create item stack
                // A note to all future and current maintainers; As of 7/27/2018 the bukkit / spigot api
                // seems to be in a tentative state for creating items stacks with specific meta data.
                // This could be because of the current state of the minecraft server 'api' which spigot
                // is built on. Once a safer and non deprecated method becomes available this constructor
                // should be removed in favor of said method.
                final ItemStack itemStack = new ItemStack(material, 1, (short) 0, defaultProduct.unsafeData);
                // Get item stack meta
                final ItemMeta itemStackMeta = itemStack.getItemMeta();
                // Set item meta information
                itemStackMeta.setDisplayName(defaultProduct.alias);
                itemStack.setItemMeta(itemStackMeta);
                // Add item to item stack array
                itemStacks[itemStackIndex++] = itemStack;
            }
        }
        return itemStacks;
    }
}
