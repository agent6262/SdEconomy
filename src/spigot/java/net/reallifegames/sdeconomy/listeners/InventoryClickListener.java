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
package net.reallifegames.sdeconomy.listeners;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.reallifegames.sdeconomy.inventory.ItemListInventory;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Handles click events for the SdEconomy items inventory.
 *
 * @author Tyler Bucher
 */
public class InventoryClickListener implements Listener {

    /**
     * Handles click events for the SdEconomy items inventory.
     *
     * @param event the click event fired for inventory's.
     */
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        // get the inventory clicked
        final Inventory clickedInventory = event.getClickedInventory();
        // Get the slot clicked
        final int slot = event.getSlot();
        // Check to make sure the inventory is not null
        if (clickedInventory != null) {
            // Check to make sure the clicked inventory is the sd economy one and the slots are inside the inventory
            if (clickedInventory.getTitle().equalsIgnoreCase(ItemListInventory.INVENTORY_NAME) && slot >= 0 && slot <= 53) {
                // Get the clicked item
                final ItemStack clickedItem = event.getCurrentItem();
                // Make sure the clicked item is not null and has item meta
                if (clickedItem != null && clickedItem.hasItemMeta()) {
                    final ItemMeta clickedItemMeta = clickedItem.getItemMeta();
                    boolean indexItem = false;
                    // Check to see if the item meta has lore
                    if (clickedItemMeta.hasLore()) {
                        for (String lore : clickedItemMeta.getLore()) {
                            // Checks to see if the item stack is a page movement item.
                            if (lore.startsWith("index:")) {
                                indexItem = true;
                                final int pageIndex = Integer.parseInt(lore.substring(6));
                                // Change pages and sets the inventory contents
                                clickedInventory.setContents(ItemListInventory.getPageNode(pageIndex).getItems());
                                // Update inventory contents for all viewers
                                for (HumanEntity humanEntity : event.getViewers()) {
                                    ((Player) humanEntity).updateInventory();
                                }
                            }
                        }
                    }
                    // Send client a message on how to buy the item
                    if (!indexItem) {
                        TextComponent message = new TextComponent("The command to buy this item is: " + ChatColor.GOLD +
                                "/buy " + clickedItemMeta.getDisplayName() + " <amount>");
                        message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/buy " +
                                clickedItemMeta.getDisplayName() + " "));
                        event.getWhoClicked().spigot().sendMessage(message);
                    }
                }
                // Cancel the event
                event.setCancelled(true);
            }
        }
    }
}
