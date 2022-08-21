package org.mooner.lands.rtp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.mooner.lands.Lands;
import org.mooner.lands.land.db.DatabaseManager;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mooner.lands.MoonerUtils.chat;
import static org.mooner.lands.MoonerUtils.playSound;

public class RTP {
    public static HashMap<UUID, Long> rtpTime = new HashMap<>();
    public static int coolTime = 60000;
    public static int rtpDistance = 40000;

    private BukkitTask task;

    public RTP(Player p) {
        final Long time = rtpTime.get(p.getUniqueId());
        if(time != null && !p.isOp()) {
            final long now = System.currentTimeMillis();
            if (time + coolTime > now) {
                playSound(p, Sound.ENTITY_VILLAGER_NO, 1, 1);
                p.sendMessage(chat("&6" + (time + coolTime - now) / 1000 + "&c초 후에 다시 사용 가능합니다!"));
                return;
            }
        }
        AtomicBoolean done = new AtomicBoolean(false);
        p.sendMessage(chat("&7안전한 장소를 찾는 중입니다..."));
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(Lands.lands, () -> {
            final Random random = new Random();
            Location ran = new Location(p.getWorld(), random.nextInt(rtpDistance), 0, random.nextInt(rtpDistance));
            ran.setY(p.getWorld().getHighestBlockYAt(ran));
//            ran.getChunk().load(true);
            if(!ran.getBlock().isLiquid() && DatabaseManager.init.isSafe(ran)) {
                while (ran.getBlock().isPassable()) ran.add(0, -1, 0);
                if (ran.getBlock().getType().isSolid()) {
                    Bukkit.getScheduler().runTask(Lands.lands, () -> {
                        if (done.get()) return;
                        p.teleport(ran.add(0.5, 1, 0.5));
                        if (!p.isOp()) rtpTime.put(p.getUniqueId(), System.currentTimeMillis());
                        p.sendMessage(chat("&7이동했습니다! x: " + ran.getBlockX() + " y: " + ran.getBlockY() + " z: " + ran.getBlockZ()));
                        playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                        playSound(p, Sound.BLOCK_PORTAL_TRAVEL, 0.9, 0.75);
                        done.set(true);
                    });
                    task.cancel();
                }
            }
        }, 0, 30);
    }
}
