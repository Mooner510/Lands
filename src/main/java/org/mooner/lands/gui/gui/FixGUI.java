package org.mooner.lands.gui.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.mooner.lands.land.PlayerLand;
import org.mooner.lands.land.db.DatabaseManager;
import org.mooner.moonerbungeeapi.api.BungeeAPI;

import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

import static org.mooner.lands.Lands.lands;
import static org.mooner.lands.MoonerUtils.chat;
import static org.mooner.lands.gui.GUIUtils.allFlags;
import static org.mooner.lands.gui.GUIUtils.createItem;

public class FixGUI {
    private Inventory inventory;
    private Player player;
    private final Click listener = new Click();
    private HashMap<Integer, PlayerLand> dataMap;

    public FixGUI(Player p) {
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            this.player = p;
            dataMap = new HashMap<>();
            List<PlayerLand> playerLands = DatabaseManager.init.getPlayerLands(p.getUniqueId());
            int size = playerLands.size();
            this.inventory = Bukkit.createInventory(p, size > 27 ? (size/9)+1 : 27 , chat("소유한 지역 목록 ( " + size + " / " + DatabaseManager.maxLands + " 개)"));
            int slot = 0;
            for (PlayerLand land : playerLands) {
                dataMap.put(slot, land);
                int distance = land.getSquare().getDistance();
                final OfflinePlayer owner = Bukkit.getOfflinePlayer(land.getOwner());
                StringJoiner joiner = new StringJoiner(", ");
                int i = 0;
                final List<OfflinePlayer> coopMembers = land.getCoopMembers();
                for (OfflinePlayer member : coopMembers) {
                    joiner.add(member.getName());
                    if(i++ > 4) {
                        i = -1;
                        break;
                    }
                }
                inventory.setItem(slot++, allFlags(createItem(Material.GRASS_BLOCK, 1, "&a" + land.getName(),
                        "&7소유자: " + BungeeAPI.getPlayerRank(owner).getPrefix() + owner.getName(),
                        "&7공유 중인 플레이어: &b" + joiner + (i == -1 ? " 외 " + (coopMembers.size() - 4) + "명" : ""),
                        "&7지역 크기: &6" + distance * 2 + "x" + distance * 2,
                        "&7구매한 가격: &6" + land.getCost(),
                        "&7지역 위치: &ex: " + land.getSquare().getX() + ", z: " + land.getSquare().getZ(),
                        "&7스폰 지점 위치: &e" + land.getSpawnLocation().getBlockX() + ", " + land.getSpawnLocation().getBlockY() + ", " + land.getSpawnLocation().getBlockZ(),
                        "",
                        "&c클릭하여 해당 땅 삭제"
                )));
            }

            Bukkit.getScheduler().runTask(lands, () -> {
                Bukkit.getPluginManager().registerEvents(listener, lands);
                player.openInventory(inventory);
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
                            new RemoveGUI(player, data, false);
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
