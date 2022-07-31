package org.mooner.lands.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.mooner.lands.land.PlayerLand;
import org.mooner.lands.land.db.DatabaseManager;
import org.mooner.lands.land.db.data.LandsData;

import static org.mooner.lands.Lands.lands;
import static org.mooner.lands.MoonerUtils.chat;
import static org.mooner.lands.gui.GUIUtils.allFlags;
import static org.mooner.lands.gui.GUIUtils.createItem;

public class MainGUI {
    private Inventory inventory;
    private Player player;

    public MainGUI(Player p) {
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            this.player = p;
            final PlayerLand lands = DatabaseManager.init.getLands(p.getLocation());
            if(lands != null) {
                this.inventory = Bukkit.createInventory(p, 27, chat("&f&l새로운 땅 구매:"));
                int slot = 0;
                for (LandsData data : DatabaseManager.init.getLandsData()) {
                    inventory.setItem(slot++, allFlags(createItem(data.getMaterial(), 1, data.getName(), data.getLore())));
                }
            }
        });
    }
}
