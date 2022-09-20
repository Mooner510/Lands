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
import org.mooner.lands.gui.GUIUtils;
import org.mooner.lands.land.PlayerLand;
import org.mooner.lands.land.db.DatabaseManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mooner.lands.Lands.lands;
import static org.mooner.lands.MoonerUtils.chat;
import static org.mooner.lands.gui.GUIUtils.createItem;
import static org.mooner.lands.gui.GUIUtils.ench;

public class DupeLandsGUI {
    private Inventory inventory;
    private Player player;
    private final Click listener = new Click();

    private HashMap<Integer, PlayerLand> map;

    private HashMap<Integer, List<PlayerLand>> listMap;

    public DupeLandsGUI(Player p) {
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            this.player = p;
            this.inventory = Bukkit.createInventory(p, 54, "겹치는 땅 검색기");
            map = new HashMap<>();
            listMap = new HashMap<>();
            int index = 0;
            for (PlayerLand playerLand : DatabaseManager.init.getPlayerLands()) {
                final List<PlayerLand> lands = DatabaseManager.init.getDupeLands(playerLand);
                if(!lands.isEmpty()) {
                    int distance = playerLand.getSquare().getDistance();
                    final ItemStack i = ench(createItem(Material.BOOK, 1, playerLand.getName(),
                            "&7소유자: " + Bukkit.getOfflinePlayer(playerLand.getOwner()).getName(),
                            "&7공유 중인 플레이어: &b" + playerLand.getCoopSize() + "명",
                            "&7지역 크기: &6" + distance * 2 + "x" + distance * 2,
                            "&7지역 위치: &ex: " + playerLand.getSquare().getX() + ", z: " + playerLand.getSquare().getZ(),
                            "&7스폰 지점 위치: &e" + playerLand.getSpawnLocation().getBlockX() + ", " + playerLand.getSpawnLocation().getBlockY() + ", " + playerLand.getSpawnLocation().getBlockZ(),
                            "",
                            "&c겹치는 땅 목록: "
                    ));
                    ArrayList<String> list = new ArrayList<>();
                    for (PlayerLand land : lands)
                        list.add(chat(" &e• &f" + Bukkit.getOfflinePlayer(land.getOwner()).getName()) + "&7님의 땅 &b" + land.getName());
                    map.put(index, playerLand);
                    listMap.put(index, lands);
                    inventory.setItem(index++, GUIUtils.addLore(i, list, "", "&e클릭하여 세부 설정하세요!"));
                }
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
                    new DupeInfoGUI(player, map.get(e.getSlot()), listMap.get(e.getSlot()));
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
