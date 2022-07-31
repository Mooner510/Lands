package org.mooner.lands.gui.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mooner.lands.land.db.DatabaseManager;
import org.mooner.lands.land.db.data.LandsData;

import static org.mooner.lands.MoonerUtils.chat;

public class LandNamer {
    private final Player p;
    private final LandsData data;
    private final Chat listener = new Chat();

    public LandNamer(Player p, LandsData data) {
        this.p = p;
        this.data = data;

        p.sendMessage(chat(DatabaseManager.init.getMessage("land-name")));
        Bukkit.getPluginManager().registerEvent();
    }

    // TODO: 2022-08-01  
    private static class Chat implements Listener {
        @EventHandler
        public onChat()
    }
}
