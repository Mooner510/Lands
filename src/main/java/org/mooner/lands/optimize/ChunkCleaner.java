package org.mooner.lands.optimize;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkCleaner implements Listener {
    private static final int MAX_ENTITY_IN_CHUNK = 30;

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        for (Entity entity : e.getChunk().getEntities()) {
            if(entity.getType() == EntityType.ENDER_DRAGON || entity.getType() == EntityType.ELDER_GUARDIAN || entity.getType() == EntityType.WITHER) continue;
            if(entity instanceof Monster && entity.getCustomName() == null) entity.remove();
        }
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) {
        if(e.getEntity().getType() == EntityType.PLAYER || e.getEntity().getType() == EntityType.IRON_GOLEM) return;
        if(e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.RAID || e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.PATROL) return;
        final double random = Math.random();
        e.setCancelled(switch (e.getEntityType()) {
            case ZOMBIFIED_PIGLIN -> random <= 0.8;
            case ENDERMAN -> e.getEntity().getWorld().getName().equals("world_the_end") && random <= 0.5;
            default -> false;
        });
        e.setCancelled(e.isCancelled() || switch (e.getSpawnReason()) {
            case NATURAL, SPAWNER, EGG, JOCKEY, BREEDING, MOUNT, BUILD_SNOWMAN ->
                    e.getLocation().getChunk().getEntities().length > MAX_ENTITY_IN_CHUNK;
//            case VILLAGE_DEFENSE -> {
//
//            }
//            case NETHER_PORTAL -> true;
//            case INFECTION, CURED -> {
//
//            }
            default -> false;
        });
    }
}
