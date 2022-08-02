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
import org.bukkit.inventory.ItemStack;
import org.mooner.lands.land.LandFlags;
import org.mooner.lands.land.db.DatabaseManager;

import static org.mooner.lands.Lands.lands;
import static org.mooner.lands.MoonerUtils.*;
import static org.mooner.lands.gui.GUIUtils.allFlags;
import static org.mooner.lands.gui.GUIUtils.createItem;

public class FlagGUI {
    private Inventory inventory;
    private Player player;
    private final Click listener = new Click();
    private final int page;
    private final int id;
    private long lastClick;

    private void updateSlot(int i, LandFlags flags, LandFlags.LandFlagSetting flag) {
        inventory.setItem(i, allFlags(createItem(flags.getMaterial(), 1,
                (flag == LandFlags.LandFlagSetting.ALLOW ? "&a" :
                        flag == LandFlags.LandFlagSetting.ONLY_COOP ? "&b" :
                                flag == LandFlags.LandFlagSetting.DEFAULT ? "&7" : "&c") + flags.getTag(),
                "&7설정: " + (flag == LandFlags.LandFlagSetting.ALLOW ? "&a허용" :
                        flag == LandFlags.LandFlagSetting.ONLY_COOP ? "&b공유 플레이어만" :
                                flag == LandFlags.LandFlagSetting.DEFAULT ? "&7기본" : "&c거부"), "", "&e클릭하여 설정하세요!")));
    }

    public FlagGUI(Player p, int id, int page) {
        lastClick = getTime();
        this.id = id;
        this.page = page;
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            this.player = p;
            this.inventory = Bukkit.createInventory(p, 45, chat("&8&l설정 메뉴"));
            ItemStack pane = createItem(Material.BLACK_STAINED_GLASS_PANE, 1, " ");
            for (int i = 0; i < 9; i++) {
                inventory.setItem(i, pane);
                inventory.setItem(i + 36, pane);
            }
            int skip = (page - 1) * 27;
            int slot = 9;
            boolean hasNext = false;
            for (LandFlags flags : LandFlags.values()) {
                if(skip-- > 0) continue;
                if(slot >= 36) {
                    hasNext = true;
                    break;
                }
                updateSlot(slot++, flags, DatabaseManager.init.getRealFlag(id, flags));
            }
            if(hasNext) inventory.setItem(42, createItem(Material.PAPER, 1, "다음 페이지"));
            inventory.setItem(40, createItem(Material.RED_WOOL, 1, "&c이전"));
            if(page > 1) inventory.setItem(38, createItem(Material.PAPER, 1, "이전 페이지"));

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
                    if(e.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) return;
                    if(e.getCurrentItem().getType() == Material.PAPER) {
                        if(e.getSlot() == 38) {
                            new FlagGUI(player, id, page - 1);
                        } else if(e.getSlot() == 42) {
                            new FlagGUI(player, id, page + 1);
                        }
                    } else if(e.getCurrentItem().getType() == Material.RED_WOOL) {
                        if(e.getSlot() == 40) {
                            new MainGUI(player);
                        }
                    }
                    if(e.getSlot() >= 9 && e.getSlot() <= 35) {
                        if(lastClick + 500 > getTime()) return;
                        lastClick = getTime();
                        LandFlags flags = LandFlags.values()[e.getSlot() - 9 + (page - 1) * 36];
                        LandFlags.LandFlagSetting flag = DatabaseManager.init.getLandManager(id).nextFlagRequest(flags);
                        updateSlot(e.getSlot(), flags, flag);
                        Bukkit.broadcastMessage(flag.toString());
                        player.updateInventory();
                        playSound(player, Sound.UI_BUTTON_CLICK, 0.85, 1);
                    }
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(inventory.equals(e.getInventory()) && e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                DatabaseManager.init.getLandManager(id).queue();
                inventory = null;
                player = null;
                HandlerList.unregisterAll(this);
            }
        }
    }
}
