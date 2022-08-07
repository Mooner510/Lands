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
import org.mooner.lands.land.PlayerLand;
import org.mooner.lands.land.db.DatabaseManager;

import static org.mooner.lands.Lands.lands;
import static org.mooner.lands.MoonerUtils.chat;
import static org.mooner.lands.gui.GUIUtils.createItem;

public class RemoveGUI {
    private Inventory inventory;
    private Player player;
    private final Click listener = new Click();
    private int id;

    public RemoveGUI(Player p, PlayerLand land) {
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            this.player = p;
            this.id = land.getId();
            this.inventory = Bukkit.createInventory(p, 27, chat("정말로 지역 " + land.getName() + "을(를) 삭제하시겠습니까?"));
            for (int i = 0; i < 5; i++) {
                int finalI = i;
                Bukkit.getScheduler().runTaskLater(lands, () -> {
                    inventory.setItem(11, createItem(Material.CLOCK, 5 - finalI, "&a삭제 &7(" + (5 - finalI) + ")", "", "&c주의! 되돌릴 수 없습니다!"));
                }, i * 20);
            }
            Bukkit.getScheduler().runTaskLater(lands, () -> {
                inventory.setItem(11, createItem(Material.GREEN_TERRACOTTA, 1, "&a삭제", "", "&c주의! 되돌릴 수 없습니다!"));
            }, 100);
            inventory.setItem(15, createItem(Material.RED_TERRACOTTA, 1, "&c취소"));

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
                    }
                    player.closeInventory();
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