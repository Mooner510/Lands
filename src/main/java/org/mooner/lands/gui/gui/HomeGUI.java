package org.mooner.lands.gui.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.mooner.lands.land.PlayerLand;
import org.mooner.lands.land.db.DatabaseManager;
import org.mooner.sethome.api.TpaAPI;

import java.util.HashMap;
import java.util.List;

import static org.mooner.lands.Lands.lands;
import static org.mooner.lands.MoonerUtils.chat;
import static org.mooner.lands.MoonerUtils.playSound;
import static org.mooner.lands.gui.GUIUtils.allFlags;
import static org.mooner.lands.gui.GUIUtils.createItem;

public class HomeGUI {
    private Inventory inventory;
    private OfflinePlayer player;
    private Player viewer;
    private final Click listener = new Click();
    private HashMap<Integer, PlayerLand> dataMap;

    public HomeGUI(OfflinePlayer p, Player viewer) {
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            this.player = p;
            this.viewer = viewer;
            dataMap = new HashMap<>();
            List<PlayerLand> playerLands = DatabaseManager.init.getPlayerLands(p.getUniqueId());
            int size = playerLands.size();
            this.inventory = Bukkit.createInventory(viewer, size > 27 ? (size/9)+1 : 27 , chat("소유한 지역 목록 ( " + size + " / " + DatabaseManager.maxLands + " 개)"));
            int slot = 0;
            for (PlayerLand land : playerLands) {
                dataMap.put(slot, land);
                inventory.setItem(slot++, allFlags(createItem(Material.GRASS_BLOCK, 1, "&a" + land.getName(),
                        "",
                        "&7위치: &e" + land.getSpawnLocation().getBlockX() + ", " + land.getSpawnLocation().getBlockY() + ", " + land.getSpawnLocation().getBlockZ(),
                        "",
                        "&e클릭하여 이동"
                )));
            }

            Bukkit.getScheduler().runTask(lands, () -> {
                Bukkit.getPluginManager().registerEvents(listener, lands);
                this.viewer.openInventory(inventory);
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
                    PlayerLand data = dataMap.get(e.getSlot());
                    if(dataMap != null) {
                        if (data != null) {
                            Player p = viewer;
                            viewer.closeInventory();
                            TpaAPI.backHere(p);
                            p.sendMessage(DatabaseManager.init.getMessage("teleport-home").replace("{1}", data.getName()));
                            playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                            p.teleport(data.getSpawnLocation());
//                            if(player != null) player.teleport(data.getSpawnLocation());
                        }
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
