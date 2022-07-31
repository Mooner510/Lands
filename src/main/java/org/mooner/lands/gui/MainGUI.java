package org.mooner.lands.gui;

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
import org.mooner.lands.land.db.data.LandsData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.mooner.lands.Lands.lands;
import static org.mooner.lands.MoonerUtils.chat;
import static org.mooner.lands.gui.GUIUtils.allFlags;
import static org.mooner.lands.gui.GUIUtils.createItem;

public class MainGUI {
    private Inventory inventory;
    private Player player;
    private final Click listener = new Click();
    private HashMap<Integer, String> dataMap;

    public MainGUI(Player p) {
        dataMap = new HashMap<>();
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            this.player = p;
            final PlayerLand land = DatabaseManager.init.getLands(p.getLocation());
            if(land != null) {
                this.inventory = Bukkit.createInventory(p, 27, chat("&f&l새로운 땅 구매:"));
                int slot = 0;
                for (LandsData data : DatabaseManager.init.getLandsData()) {
                    dataMap.put(slot, data.getName());
                    inventory.setItem(slot++, allFlags(createItem(data.getMaterial(), 1, data.getName(), data.getLore())));
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
                    LandsData data = DatabaseManager.init.getLandsData(dataMap.get(e.getSlot()));
                    if(data != null) {

                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(inventory.equals(e.getInventory()) && e.getPlayer().getUniqueId().equals(viewer.getUniqueId())) {
                HandlerList.unregisterAll(this);

                HashMap<Integer, Object[]> hash;
                ArrayList<UUID> uuids;
                (uuids = (ArrayList<UUID>) (hash = accInventory.get(player.getUniqueId())).get(page)[1]).remove(viewer.getUniqueId());
                if(uuids.size() <= 0) {
                    if(hash.size() <= 1) {
                        SkyblockPlayer c = new SkyblockPlayer(player);
                        Inventory i = (Inventory) hash.get(page)[0];
                        for (int j = 9; j < i.getSize(); j++) {
                            c.setAcc(page, j, i.getItem(j));
                        }
                        c.saveAcc();
                        accInventory.remove(player.getUniqueId());
                    } else {
                        hash.remove(page);
                    }
                }
            }
        }
    }
}
