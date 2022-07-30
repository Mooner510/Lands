package org.mooner.lands.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static org.mooner.lands.Lands.lands;
import static org.mooner.lands.MoonerUtils.chat;

public class MainGUI {
    private Inventory inventory;
    private Player player;

    public MainGUI(Player p) {
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            this.inventory = Bukkit.createInventory(p, 27, chat("&f&l새로운 땅 구매:"));
            this.player = p;
        })
    }
}
