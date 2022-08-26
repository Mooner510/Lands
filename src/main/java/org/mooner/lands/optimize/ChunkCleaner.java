package org.mooner.lands.optimize;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkCleaner implements Listener {
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        for (Entity entity : e.getChunk().getEntities()) {
            if(entity.getType() == EntityType.ENDER_DRAGON || entity.getType() == EntityType.ELDER_GUARDIAN || entity.getType() == EntityType.WITHER) continue;
            if(entity instanceof Monster && entity.getCustomName() == null) entity.remove();
        }
    }

//    private final int MAX_ENTITY_IN_CHUNK = 12;
//
//    @EventHandler
//    public void onSpawn(CreatureSpawnEvent e) {
//        switch (e.getSpawnReason()) {
//            case NATURAL, SPAWNER, EGG, JOCKEY, BREEDING, MOUNT -> {
//                e.getLocation().getChunk().getEntities().length
//            }
//            case VILLAGE_DEFENSE -> {
//
//            }
//            case NETHER_PORTAL -> e.setCancelled(true);
//            case INFECTION, CURED -> {
//
//            }
//        }
//    }
}
