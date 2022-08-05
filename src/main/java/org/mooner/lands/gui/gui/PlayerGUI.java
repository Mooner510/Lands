package org.mooner.lands.gui.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mooner.lands.land.PlayerLand;
import org.mooner.lands.land.db.DatabaseManager;

import static org.mooner.lands.Lands.lands;
import static org.mooner.lands.MoonerUtils.chat;
import static org.mooner.lands.gui.GUIUtils.createHead;
import static org.mooner.lands.gui.GUIUtils.createItem;

public class PlayerGUI {
    private Inventory inventory;
    private Player player;
    private final Click listener = new Click();
    private int id;

    public PlayerGUI(Player p, PlayerLand land) {
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            this.player = p;
            this.id = land.getId();
            this.inventory = Bukkit.createInventory(p, 45, chat("현재 멤버"));
            ItemStack pane = createItem(Material.BLACK_STAINED_GLASS_PANE, 1, " ");
            for (int i = 0; i < 9; i++) {
                inventory.setItem(i, pane);
                inventory.setItem(i + 36, pane);
            }
            inventory.setItem(9, pane);
            inventory.setItem(17, pane);
            inventory.setItem(18, pane);
            inventory.setItem(26, pane);
            inventory.setItem(27, pane);
            inventory.setItem(35, pane);

            int slot = 10;
            for (Player member : land.getCoopMembers()) {
                inventory.setItem(slot++, createHead(member, 1, member.getDisplayName(), "&e클릭하여 제거"));
            }

            Bukkit.getScheduler().runTask(lands, () -> {
                Bukkit.getPluginManager().registerEvents(listener, lands);
                this.player.openInventory(inventory);
            });
        });
    }

    public class Click implements Listener {
        @EventHandler
        public void onClick(InventoryClickEvent e) {
            if(e.getInventory().equals(inventory)) {
                if(e.getClickedInventory() == null || e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR))
                    return;
                if(e.getClickedInventory().equals(inventory)) {
                    e.setCancelled(true);
                    if(e.getSlot() == 11 && e.getCurrentItem().getType() == Material.GREEN_TERRACOTTA) {
                        DatabaseManager.init.deleteLand(id);
                        player.sendMessage(DatabaseManager.init.getMessage("land-delete"));
                    } else {
                        player.closeInventory();
                    }
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(inventory.equals(e.getInventory()) && e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                HandlerList.unregisterAll(this);
                inventory = null;
                player = null;
            }
        }
    }
}
