package org.mooner.lands.gui.gui;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
import java.util.Set;

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
            player.sendMessage(chat("&7겹쳐지는 땅을 검색하는 중입니다..."));
            final Set<PlayerLand> playerLands = DatabaseManager.init.getPlayerLands();
            final int size = playerLands.size();
            int length = 1;
            int outIndex = 0;
            for (PlayerLand playerLand : playerLands) {
                final List<PlayerLand> lands = DatabaseManager.init.getDupeLands(playerLand);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(chat("&7검색중... &e(" + (length++) + "/" + size + ")")));
                if(!lands.isEmpty()) {
                    if(index >= 54) {
                        outIndex++;
                        continue;
                    }
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
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(chat("&a완료")));
            player.sendMessage(chat("&7모든 땅을 검색해 확인했습니다."));
            if(outIndex > 0)
                player.sendMessage(chat("&6" + outIndex + "&c개의 땅이 누락되었습니다."));

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
