package org.mooner.lands.gui.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.mooner.lands.events.CancelRequestEvent;
import org.mooner.lands.land.db.DatabaseManager;
import org.mooner.lands.land.db.data.LandsData;

import static org.mooner.lands.Lands.lands;

public record LandNamer(Player p, LandsData data) {
    public LandNamer(Player p, LandsData data) {
        this.p = p;
        this.data = data;

        Bukkit.getPluginManager().callEvent(new CancelRequestEvent(p));
        p.sendMessage(DatabaseManager.init.getMessage("land-create-name"));
        Bukkit.getPluginManager().registerEvents(new Chat(), lands);
    }

    private class Chat implements Listener {
        @EventHandler
        public void onChat(AsyncPlayerChatEvent e) {
            if(!e.getPlayer().getUniqueId().equals(p.getUniqueId())) return;
            e.setCancelled(true);
            Bukkit.getScheduler().runTask(lands, () -> {
                switch (DatabaseManager.init.setLand(p.getUniqueId(), e.getMessage(), p.getLocation().clone(), data)) {
                    case MAX_LANDS -> p.sendMessage(DatabaseManager.init.getMessage("land-create-max-land"));
                    case ALREADY_EXISTS -> p.sendMessage(DatabaseManager.init.getMessage("land-create-already-exists"));
                    case OTHER_LAND -> p.sendMessage(DatabaseManager.init.getMessage("land-create-other-land"));
                    case NOT_FOUND -> p.sendMessage(DatabaseManager.init.getMessage("land-create-not-found"));
                    case NO_WORLD -> p.sendMessage(DatabaseManager.init.getMessage("land-no-world"));
                    case DUPE_NAME -> p.sendMessage(DatabaseManager.init.getMessage("land-dupe-name").replace("{1}", e.getMessage()));
                    case NO_NAME -> p.sendMessage(DatabaseManager.init.getMessage("land-no-name"));
                    case NOT_ENOUGH_MONEY -> p.sendMessage(DatabaseManager.init.getMessage("land-not-enough-money"));
                    case COMPLETE -> p.sendMessage(DatabaseManager.init.getMessage("land-create-complete"));
                }
            });
            HandlerList.unregisterAll(this);
        }

        @EventHandler
        public void cancel(CancelRequestEvent e) {
            if(!e.getPlayer().getUniqueId().equals(p.getUniqueId())) return;
            HandlerList.unregisterAll(this);
        }
    }
}
