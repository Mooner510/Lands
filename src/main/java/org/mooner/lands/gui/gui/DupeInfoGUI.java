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

import java.util.List;

import static org.mooner.lands.Lands.lands;
import static org.mooner.lands.gui.GUIUtils.createItem;
import static org.mooner.lands.gui.GUIUtils.ench;

public class DupeInfoGUI {
    private Inventory inventory;
    private Player player;
    private final Click listener = new Click();

    private List<PlayerLand> list;

    public DupeInfoGUI(Player p, PlayerLand land, List<PlayerLand> list) {
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            this.player = p;
            this.inventory = Bukkit.createInventory(p, 54, Bukkit.getOfflinePlayer(land.getOwner()).getName() + "님의 땅: " + land.getName());
            this.list = list;
            int distance = land.getSquare().getDistance();
            inventory.setItem(4, ench(createItem(Material.GRASS_BLOCK, 1, land.getName(),
                    "&7소유자: " + Bukkit.getOfflinePlayer(land.getOwner()).getName(),
                    "&7공유 중인 플레이어: &b" + land.getCoopSize() + "명",
                    "&7지역 크기: &6" + distance * 2 + "x" + distance * 2,
                    "&7지역 위치: &ex: " + land.getSquare().getX() + ", z: " + land.getSquare().getZ(),
                    "&7스폰 지점 위치: &e" + land.getSpawnLocation().getBlockX() + ", " + land.getSpawnLocation().getBlockY() + ", " + land.getSpawnLocation().getBlockZ(),
                    "",
                    "&e클릭하여 이동하세요!"
            )));
            int index = 9;
            for (PlayerLand playerLand : list) {
                distance = playerLand.getSquare().getDistance();
                inventory.setItem(index++, ench(createItem(Material.OAK_FENCE, 1, playerLand.getName(),
                        "&7소유자: " + Bukkit.getOfflinePlayer(playerLand.getOwner()).getName(),
                        "&7공유 중인 플레이어: &b" + playerLand.getCoopSize() + "명",
                        "&7지역 크기: &6" + distance * 2 + "x" + distance * 2,
                        "&7지역 위치: &ex: " + playerLand.getSquare().getX() + ", z: " + playerLand.getSquare().getZ(),
                        "&7스폰 지점 위치: &e" + land.getSpawnLocation().getBlockX() + ", " + land.getSpawnLocation().getBlockY() + ", " + land.getSpawnLocation().getBlockZ(),
                        "",
                        "&e클릭하여 이동하세요!"
                )));
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
                    final Player p = player;
                    p.closeInventory();
                    p.teleport(list.get(e.getSlot()).getSpawnLocation());
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(inventory.equals(e.getInventory()) && e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                inventory = null;
                player = null;
                HandlerList.unregisterAll(this);
            }
        }
    }
}
