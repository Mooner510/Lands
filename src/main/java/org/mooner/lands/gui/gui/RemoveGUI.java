package org.mooner.lands.gui.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.mooner.moonereco.API.EcoAPI;
import org.mooner.moonereco.API.LogType;

import static org.mooner.lands.Lands.lands;
import static org.mooner.lands.MoonerUtils.*;
import static org.mooner.lands.gui.GUIUtils.createItem;

public class RemoveGUI {
    private Inventory inventory;
    private Player player;
    private final Click listener = new Click();
    private PlayerLand land;

    public RemoveGUI(Player p, PlayerLand land) {
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            this.player = p;
            this.land = land;
            this.inventory = Bukkit.createInventory(p, 27, chat("정말로 지역 " + land.getName() + "을(를) 삭제하시겠습니까?"));
            for (int i = 0; i < 5; i++) {
                int finalI = i;
                Bukkit.getScheduler().runTaskLater(lands, () -> {
                    if(inventory != null)
                        inventory.setItem(11, createItem(Material.CLOCK, 5 - finalI, "&a삭제 &7(" + (5 - finalI) + ")", "", "&c주의! 되돌릴 수 없습니다!"));
                }, i * 20);
            }
            Bukkit.getScheduler().runTaskLater(lands, () -> {
                if(inventory != null)
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
                    if(e.getCurrentItem().getType() == Material.CLOCK) return;
                    if(e.getSlot() == 11 && e.getCurrentItem().getType() == Material.GREEN_TERRACOTTA) {
                        if(e.getClick().isKeyboardClick()) {
                            player.sendMessage(chat("&7구지.. 이걸 키보드로 누르셔야 됬나요?? 잘못하고 손이 미끄러져 클릭했을 수도 있으니 취소해 드릴게요."));
                            playSound(player, Sound.ENTITY_VILLAGER_NO, 1, 1);
                            player.closeInventory();
                            return;
                        }
                        player.sendMessage(DatabaseManager.init.getMessage("land-delete").replace("{1}", parseString(land.getCost() * 0.20, 2, true)));
                        DatabaseManager.init.deleteLand(land.getId());
                        EcoAPI.init.addPay(player, land.getCost() * 0.20);
                        EcoAPI.init.log(player, LogType.LAND_SELL, land.getCost() * 0.20);
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
