package org.mooner.lands.gui.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.mooner.lands.land.db.DatabaseManager;
import org.mooner.lands.land.db.data.LandsData;

import static org.mooner.lands.Lands.lands;

public record LandNamer(Player p, LandsData data) {
    public LandNamer(Player p, LandsData data) {
        this.p = p;
        this.data = data;

        p.sendMessage(DatabaseManager.init.getMessage("land-create-name"));
        Bukkit.getPluginManager().registerEvents(new Chat(), lands);
    }

    private class Chat implements Listener {
        @EventHandler
        public void onChat(AsyncPlayerChatEvent e) {
            e.setCancelled(true);
            Bukkit.getScheduler().runTask(lands, () -> {
                switch (DatabaseManager.init.setLand(p.getUniqueId(), e.getMessage(), p.getLocation(), data)) {
                    case MAX_LANDS -> {
                        p.sendMessage(DatabaseManager.init.getMessage("land-create-max-land"));
                    }
                    case ALREADY_EXISTS -> {
                        p.sendMessage(DatabaseManager.init.getMessage("land-create-already-exists"));
                    }
                    case OTHER_LAND -> {
                        p.sendMessage(DatabaseManager.init.getMessage("land-create-other-land"));
                    }
                    case NOT_FOUND -> {
                        p.sendMessage(DatabaseManager.init.getMessage("land-create-not-found"));
                    }
                    case COMPLETE -> {
                        p.sendMessage(DatabaseManager.init.getMessage("land-create-complete"));
                    }
                }
            });
            HandlerList.unregisterAll(this);
        }
    }
}
