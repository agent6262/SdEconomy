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

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import javax.annotation.Nonnull;

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
}
