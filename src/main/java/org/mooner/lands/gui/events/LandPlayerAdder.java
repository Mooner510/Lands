package org.mooner.lands.gui.events;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.mooner.lands.events.CancelRequestEvent;
import org.mooner.lands.exception.PlayerLandNotFoundException;
import org.mooner.lands.gui.gui.PlayerGUI;
import org.mooner.lands.land.db.DatabaseManager;

import static org.mooner.lands.Lands.lands;
import static org.mooner.lands.MoonerUtils.playSound;
import static org.mooner.lands.MoonerUtils.saveThrowable;

public record LandPlayerAdder(Player p, int land) {
    public LandPlayerAdder(Player p, int land) {
        this.p = p;
        this.land = land;

        Bukkit.getPluginManager().callEvent(new CancelRequestEvent(p));
        p.sendMessage(DatabaseManager.init.getMessage("land-coop-request"));
        Bukkit.getPluginManager().registerEvents(new Chat(), lands);
    }

    private class Chat implements Listener {
        @EventHandler
        public void onChat(AsyncPlayerChatEvent e) {
            if(!e.getPlayer().getUniqueId().equals(p.getUniqueId())) return;
            e.setCancelled(true);
            Bukkit.getScheduler().runTask(lands, () -> {
                switch (DatabaseManager.init.addCoop(land, e.getMessage())) {
                    case MAX_PLAYER -> p.sendMessage(DatabaseManager.init.getMessage("land-coop-max-player"));
                    case ALREADY_EXISTS -> p.sendMessage(DatabaseManager.init.getMessage("land-coop-already-exists"));
                    case NOT_FOUND_LAND -> p.sendMessage(DatabaseManager.init.getMessage("land-coop-not-found-land"));
                    case NOT_FOUND -> p.sendMessage(DatabaseManager.init.getMessage("land-coop-not-found"));
                    case COMPLETE -> p.sendMessage(DatabaseManager.init.getMessage("land-coop-add"));
                }
                playSound(p, Sound.BLOCK_NOTE_BLOCK_PLING, 0.85, 1);
                try {
                    new PlayerGUI(p, DatabaseManager.init.getPlayerLand(land));
                } catch (PlayerLandNotFoundException ex) {
                    saveThrowable("ERROR", ex);
                    throw new RuntimeException(ex);
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
