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
import org.bukkit.inventory.ItemStack;
import org.mooner.lands.gui.events.LandPlayerAdder;
import org.mooner.lands.land.PlayerLand;
import org.mooner.lands.land.db.DatabaseManager;

import java.util.HashMap;
import java.util.UUID;

import static org.mooner.lands.Lands.lands;
import static org.mooner.lands.MoonerUtils.chat;
import static org.mooner.lands.MoonerUtils.playSound;
import static org.mooner.lands.gui.GUIUtils.createHead;
import static org.mooner.lands.gui.GUIUtils.createItem;

public class PlayerGUI {
    private Inventory inventory;
    private Player player;
    private final Click listener = new Click();
    private PlayerLand land;
    private final HashMap<Integer, UUID> players;

    public PlayerGUI(Player p, PlayerLand land) {
        players = new HashMap<>();
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            this.player = p;
            this.land = land;
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

            inventory.setItem(4, createItem(Material.WRITABLE_BOOK, 1, "&a공유 플레이어 추가", "&7최대 "+PlayerLand.MAX_COOP_MEMBERS+"명과 한 지역을 공유할 수 있습니다.", "", "&e클릭하여 추가하기"));

            inventory.setItem(40, createItem(Material.BARRIER, 1, "&c돌아가기"));

            int slot = 10;
            for (OfflinePlayer member : land.getCoopMembers()) {
                players.put(slot, member.getUniqueId());
                inventory.setItem(slot++, createHead(member, 1, "&b" + member.getName(), "&e클릭하여 제거"));
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
                    if(e.getSlot() == 4) {
                        new LandPlayerAdder(player, land.getId());
                        player.closeInventory();
                    } else if(e.getSlot() == 40) {
                        new MainGUI(player);
                    } else {
                        final UUID uuid = players.get(e.getSlot());
                        if(uuid != null) {
                            switch (DatabaseManager.init.removeCoop(land.getId(), uuid)) {
                                case NOT_FOUND_LAND -> player.sendMessage(DatabaseManager.init.getMessage("land-coop-not-found-land"));
                                case NOT_FOUND -> player.sendMessage(DatabaseManager.init.getMessage("land-coop-not-found"));
                                case COMPLETE -> player.sendMessage(DatabaseManager.init.getMessage("land-coop-remove"));
                            }
                            playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 0.85, 1);
                            new PlayerGUI(player, land);
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
