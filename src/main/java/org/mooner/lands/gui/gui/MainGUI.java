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
import org.mooner.lands.gui.events.LandNamer;
import org.mooner.lands.land.PlayerLand;
import org.mooner.lands.land.db.DatabaseManager;
import org.mooner.lands.land.db.data.LandsData;

import java.util.HashMap;

import static org.mooner.lands.Lands.lands;
import static org.mooner.lands.MoonerUtils.chat;
import static org.mooner.lands.gui.GUIUtils.*;

public class MainGUI {
    private Inventory inventory;
    private Player player;
    private final Click listener = new Click();
    private final HashMap<Integer, LandsData> dataMap;

    public MainGUI(Player p) {
        dataMap = new HashMap<>();
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            this.player = p;
            final PlayerLand land = DatabaseManager.init.getCurrentLand(p.getLocation());
            if(land == null) {
                this.inventory = Bukkit.createInventory(p, 27, chat("&f&l새로운 땅 구매:"));
                int slot = 0;
                for (LandsData data : DatabaseManager.init.getLandsData()) {
                    dataMap.put(slot, data);
                    inventory.setItem(slot++, allFlags(createItem(data.getMaterial(), 1, data.getName(), data.getLore())));
                }
            } else {
                this.inventory = Bukkit.createInventory(p, 45, chat("&f&l지역 관리하기"));
                ItemStack pane = createItem(Material.GRAY_STAINED_GLASS_PANE, 1, " ");
                for (int i = 0; i < 9; i++) {
                    inventory.setItem(i, pane);
                    inventory.setItem(i + 36, pane);
                }
                inventory.setItem(9, pane);
                inventory.setItem(17, pane);
                int distance = land.getSquare().getDistance();
                inventory.setItem(4, ench(createItem(Material.BOOK, 1, "&a지역 상세 정보 - " + land.getName(),
                        "&7소유자: " + player.getDisplayName(),
                        "&7공유 중인 플레이어: &b" + land.getCoopSize() + "명",
                        "",
                        "&7지역 크기: &6" + distance * 2 + "x" + distance * 2
                )));
                inventory.setItem(10, createItem(Material.PLAYER_HEAD, 1, "&b공유 플레이어 추가", "&7지역을 공유할 플레이어를 추가합니다.", "&7지역 설정 권한은 주어지지 않습니다.", "", "&7현재 공유 중: &b" + land.getCoopSize() + "명"));
                inventory.setItem(12, createItem(Material.ENDER_PEARL, 1, "&d워프 장소 설정", "&7클릭해 현재 위치를 워프 장소로 설정합니다."));
                inventory.setItem(14, createItem(Material.COMMAND_BLOCK, 1, "&e지역 상세 설정", "&7해당 지역에 대해 상세적으로 설정합니다."));
                inventory.setItem(16, createItem(Material.EXPERIENCE_BOTTLE, 1, "&c경계선 보기", "&7지역의 경계선을 시각적으로 확인합니다."));
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
                    LandsData data = dataMap.get(e.getSlot());
                    if(data != null) {
                        new LandNamer(player, data);
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
