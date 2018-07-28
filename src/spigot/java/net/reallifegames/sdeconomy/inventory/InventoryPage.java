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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.Collections;

/**
 * Represents a page in an inventory.
 *
 * @author Tyler Bucher
 */
public class InventoryPage {

    /**
     * The item to go to the next page.
     */
    public static final ItemStack nextBook = new ItemStack(Material.ENCHANTED_BOOK);

    /**
     * The item to go back one page.
     */
    public static final ItemStack backBook = new ItemStack(Material.ENCHANTED_BOOK);

    /*
     * Initialize static variables.
     */
    static {
        // Set next book item meta
        ItemMeta tempMeta = nextBook.getItemMeta();
        tempMeta.setDisplayName(ChatColor.DARK_PURPLE + "Next page");
        nextBook.setItemMeta(tempMeta);
        // Set back book item meta
        tempMeta = backBook.getItemMeta();
        tempMeta.setDisplayName(ChatColor.DARK_PURPLE + "Back page");
        backBook.setItemMeta(tempMeta);
    }

    /**
     * The list of items on this page.
     */
    private final ItemStack[] items = new ItemStack[54];

    /**
     * The current insert index.
     */
    private int currentIndex = 0;

    /**
     * States if this page is the root node.
     */
    private final boolean rootNode;

    /**
     * The index of this page node.
     */
    private final int pageIndex;

    /**
     * The next page node if available.
     */
    public InventoryPage nextPageNode;

    /**
     * Creates a new inventory page node.
     *
     * @param rootNode  states if this page is the root node.
     * @param pageIndex the index of this page node.
     */
    public InventoryPage(final boolean rootNode, final int pageIndex) {
        // Sets default variables
        this.rootNode = rootNode;
        this.pageIndex = pageIndex;
        // Add back book if this node is not the root node
        if (!rootNode) {
            // Create a clone of the book
            final ItemStack backBookClone = backBook.clone();
            // Setup item meta
            final ItemMeta backBookCloneMeta = backBookClone.getItemMeta();
            backBookCloneMeta.setLore(Collections.singletonList("index:" + (pageIndex - 1)));
            backBookClone.setItemMeta(backBookCloneMeta);
            // Sets the item in the inventory
            items[45] = backBookClone;
        }
    }

    /**
     * Adds an item the this node. If this node is full it will create a new node and add it to that node.
     *
     * @param item the item to add to a node.
     */
    public void addItem(@Nonnull final ItemStack item) {
        // Add item to this node if current index is null
        if (items[currentIndex] == null) {
            items[currentIndex] = item;
            // Add bounds checking to prevent ArrayIndexOutOfBoundsException
            if (currentIndex < 53) {
                currentIndex++;
                // Increase the index again if there is a back book item and the current index is 43
                if (!rootNode && currentIndex == 45) {
                    currentIndex++;
                }
            }
        } else {
            // If there the next node does not exist create it
            if (nextPageNode == null) {
                // Create the next node
                nextPageNode = new InventoryPage(false, pageIndex + 1);
                // Add item where next page book will go
                nextPageNode.addItem(items[53]);
                // Create a clone of the book
                final ItemStack nextBookClone = nextBook.clone();
                // Setup item meta
                final ItemMeta nextBookCloneMeta = nextBookClone.getItemMeta();
                nextBookCloneMeta.setLore(Collections.singletonList("index:" + (pageIndex + 1)));
                nextBookClone.setItemMeta(nextBookCloneMeta);
                // Sets the item in the inventory
                items[53] = nextBookClone;
                // Add item to new node
                nextPageNode.addItem(item);
            } else {
                // Add item to the next node
                this.nextPageNode.addItem(item);
            }
        }
    }

    /**
     * @return the items of this node.
     */
    public ItemStack[] getItems() {
        return items;
    }
}
