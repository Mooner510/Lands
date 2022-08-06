package org.mooner.lands.land.listener;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.mooner.lands.Lands;
import org.mooner.lands.land.LandFlags;
import org.mooner.lands.land.Square;
import org.mooner.lands.land.db.DatabaseManager;

import java.util.UUID;

public class LandListener implements Listener {
    private final int land;
    private final UUID owner;
    private final Square square;

    public LandListener(int land, Square s) {
        this.square = s;
        this.land = land;
        this.owner = DatabaseManager.init.getPlayerLand(land).getOwner();
    }

    private boolean check(LandFlags flag) {
        return DatabaseManager.init.getFlag(land, flag) == LandFlags.LandFlagSetting.ALLOW;
    }

    private boolean check(LandFlags flag, Player p) {
        if(p != null && p.getUniqueId().equals(owner)) return true;
        LandFlags.LandFlagSetting s;
        if((s = DatabaseManager.init.getFlag(land, flag)) == LandFlags.LandFlagSetting.ALLOW) return true;
        if(p != null && s == LandFlags.LandFlagSetting.ONLY_COOP) {
            return DatabaseManager.init.getPlayerLand(land).isCoop(p);
        }
        return false;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if(e.getTo() != null && square.in(e.getTo())) {
            e.setCancelled(!check(LandFlags.MOVE_IN, e.getPlayer()));
        }
        if(!e.isCancelled() && e.getTo() != null && square.in(e.getFrom()) && !square.in(e.getTo())) {
            e.setCancelled(!check(LandFlags.MOVE_OUT, e.getPlayer()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent e) {
        if(!square.in(e.getBlock().getLocation())) return;
        if(check(LandFlags.BLOCK_PLACE, e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMultiPlace(BlockMultiPlaceEvent e) {
        if(!square.in(e.getBlock().getLocation())) return;
        if(check(LandFlags.BLOCK_PLACE, e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent e) {
        if(!square.in(e.getBlock().getLocation())) return;
        if(check(LandFlags.BLOCK_BREAK, e.getPlayer())) return;
        e.setCancelled(true);
    }

    private final ImmutableSet<Material> doors = ImmutableSet.of(Material.DARK_OAK_DOOR, Material.DARK_OAK_TRAPDOOR, Material.ACACIA_DOOR, Material.ACACIA_TRAPDOOR, Material.BIRCH_DOOR, Material.BIRCH_TRAPDOOR, Material.CRIMSON_DOOR, Material.CRIMSON_TRAPDOOR, Material.JUNGLE_DOOR, Material.JUNGLE_TRAPDOOR, Material.OAK_DOOR, Material.OAK_TRAPDOOR, Material.SPRUCE_DOOR, Material.SPRUCE_TRAPDOOR, Material.WARPED_DOOR, Material.WARPED_TRAPDOOR);

    private final ImmutableSet<Material> buttons = ImmutableSet.of(Material.ACACIA_BUTTON, Material.BIRCH_BUTTON, Material.CRIMSON_BUTTON, Material.DARK_OAK_BUTTON, Material.JUNGLE_BUTTON, Material.OAK_BUTTON, Material.POLISHED_BLACKSTONE_BUTTON, Material.SPRUCE_BUTTON, Material.STONE_BUTTON, Material.WARPED_BUTTON, Material.LEVER);

    private final ImmutableSet<Material> plates = ImmutableSet.of(Material.ACACIA_PRESSURE_PLATE, Material.BIRCH_PRESSURE_PLATE, Material.CRIMSON_PRESSURE_PLATE, Material.CRIMSON_PRESSURE_PLATE, Material.DARK_OAK_PRESSURE_PLATE, Material.HEAVY_WEIGHTED_PRESSURE_PLATE, Material.JUNGLE_PRESSURE_PLATE, Material.LIGHT_WEIGHTED_PRESSURE_PLATE, Material.OAK_PRESSURE_PLATE, Material.POLISHED_BLACKSTONE_PRESSURE_PLATE, Material.SPRUCE_PRESSURE_PLATE, Material.STONE_PRESSURE_PLATE, Material.WARPED_PRESSURE_PLATE, Material.STRING);

    private final ImmutableSet<Material> shulker_box = ImmutableSet.of(Material.SHULKER_BOX, Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX);

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        Block b = e.getClickedBlock();
        if(e.getAction() == Action.PHYSICAL && b != null) {
            if(!plates.contains(b.getType())) return;
            if(!square.in(b.getLocation())) return;
            if(check(LandFlags.USE_PLATE, e.getPlayer())) return;
            e.setCancelled(true);
        } else if(e.getAction() == Action.LEFT_CLICK_BLOCK && b != null) {
            if (b.getType() == Material.NOTE_BLOCK) {
                if(!square.in(b.getLocation())) return;
                if(check(LandFlags.PLAY_NOTE_BLOCK, e.getPlayer())) return;
                e.setCancelled(true);
            }else if (b.getType() == Material.JUKEBOX) {
                if(!square.in(b.getLocation())) return;
                if(check(LandFlags.USE_JUKEBOX, e.getPlayer())) return;
                e.setCancelled(true);
            }
        } else if(e.getAction() == Action.RIGHT_CLICK_BLOCK && b != null) {
            if (e.getItem() != null && e.getItem().getType().isBlock() && e.getPlayer().isSneaking()) return;
            if (doors.contains(b.getType())) {
                if(!square.in(b.getLocation())) return;
                if(check(LandFlags.OPEN, e.getPlayer())) return;
                e.setCancelled(true);
            } else if (buttons.contains(b.getType())) {
                if(!square.in(b.getLocation())) return;
                if(check(LandFlags.USE_BUTTON, e.getPlayer())) return;
                e.setCancelled(true);
            } else if (b.getType() == Material.CHEST || b.getType() == Material.ENDER_CHEST || b.getType() == Material.TRAPPED_CHEST || b.getType() == Material.BARREL || b.getType() == Material.HOPPER || shulker_box.contains(b.getType())) {
                if(!square.in(b.getLocation())) return;
                if(check(LandFlags.USE_CHEST, e.getPlayer())) return;
                e.setCancelled(true);
            } else if (b.getType() == Material.ANVIL || b.getType() == Material.CHIPPED_ANVIL || b.getType() == Material.DAMAGED_ANVIL) {
                if(!square.in(b.getLocation())) return;
                if(check(LandFlags.USE_ANVIL, e.getPlayer())) return;
                e.setCancelled(true);
            } else if (b.getType() == Material.ENCHANTING_TABLE || b.getType() == Material.GRINDSTONE) {
                if(!square.in(b.getLocation())) return;
                if(check(LandFlags.USE_ENCHANTMENTS, e.getPlayer())) return;
                e.setCancelled(true);
            } else if (b.getType() == Material.REPEATER || b.getType() == Material.COMPARATOR) {
                if(!square.in(b.getLocation())) return;
                if(check(LandFlags.USE_REDSTONE, e.getPlayer())) return;
                e.setCancelled(true);
            } else if (b.getType() == Material.NOTE_BLOCK) {
                if(!square.in(b.getLocation())) return;
                if(check(LandFlags.EDIT_NOTE_BLOCK, e.getPlayer())) return;
                e.setCancelled(true);
            } else if (b.getType() == Material.JUKEBOX) {
                if(!square.in(b.getLocation())) return;
                if(check(LandFlags.USE_JUKEBOX, e.getPlayer())) return;
                e.setCancelled(true);
            } else if (b.getType() == Material.FURNACE || b.getType() == Material.SMOKER || b.getType() == Material.BLAST_FURNACE || b.getType() == Material.CAMPFIRE || b.getType() == Material.SOUL_CAMPFIRE) {
                if(!square.in(b.getLocation())) return;
                if(check(LandFlags.USE_FURNACE, e.getPlayer())) return;
                e.setCancelled(true);
            } else if (b.getType() == Material.FLOWER_POT || b.getType() == Material.COMPOSTER) {
                if(!square.in(b.getLocation())) return;
                if(check(LandFlags.USE_FARM_BLOCK, e.getPlayer())) return;
                e.setCancelled(true);
            } else if (b.getType() == Material.BEE_NEST || b.getType() == Material.BEEHIVE) {
                if(!square.in(b.getLocation())) return;
                if(check(LandFlags.USE_BEE, e.getPlayer())) return;
                e.setCancelled(true);
            }
        } else if(e.getAction() == Action.RIGHT_CLICK_AIR) {
            if(e.getItem() != null) {
                if (e.getItem().getType() == Material.ENDER_PEARL) {
                    if(!square.in(e.getPlayer().getLocation())) return;
                    if(check(LandFlags.ENDER_PEARL_TELEPORT, e.getPlayer())) return;
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        Entity entity = e.getRightClicked();
        if(entity.getType() == EntityType.ITEM_FRAME || entity.getType() == EntityType.GLOW_ITEM_FRAME) {
            if(!square.in(entity.getLocation())) return;
            if (check(LandFlags.USE_ITEM_FRAME, e.getPlayer())) return;
            e.setCancelled(true);
        } else if(entity.getType() == EntityType.VILLAGER) {
            if(!square.in(entity.getLocation())) return;
            if(check(LandFlags.TRADE, e.getPlayer())) return;
            e.setCancelled(true);
        } else if(entity.getType() == EntityType.MINECART_HOPPER || entity.getType() == EntityType.MINECART_CHEST) {
            if(!square.in(entity.getLocation())) return;
            if(check(LandFlags.USE_CHEST, e.getPlayer())) return;
            e.setCancelled(true);
        } else if(entity instanceof Vehicle) {
            if(!square.in(entity.getLocation())) return;
            if(check(LandFlags.RIDE, e.getPlayer())) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent e) {
        if(e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            if(!square.in(e.getFrom())) return;
            if(check(LandFlags.ENDER_PEARL_TELEPORT, e.getPlayer()) && check(LandFlags.MOVE_IN, e.getPlayer())) return;
            e.setCancelled(true);
        } else if(e.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
            if(!square.in(e.getPlayer().getLocation())) return;
            if(check(LandFlags.FRUIT_TELEPORT, e.getPlayer())) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplode(BlockExplodeEvent e) {
        if(!square.in(e.getBlock().getLocation())) return;
        if(check(LandFlags.EXPLODE, null)) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplodeEntity(EntityExplodeEvent e) {
        if(!square.in(e.getLocation())) return;
        if(check(LandFlags.EXPLODE, null)) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLightning(LightningStrikeEvent e) {
        if(!square.in(e.getLightning().getLocation())) return;
        if(check(LandFlags.LIGHTNING, null)) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player p) {
            if(!square.in(e.getItem().getLocation())) return;
            if(check(LandFlags.ITEM_PICKUP, p)) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onThrow(PlayerDropItemEvent e) {
        if(!square.in(e.getPlayer().getLocation())) return;
        if(check(LandFlags.ITEM_THROW, e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpread(BlockSpreadEvent e) {
        if (e.getBlock().getType() == Material.FIRE) {
            if(!square.in(e.getBlock().getLocation())) return;
            if(check(LandFlags.FIRE, null)) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent e) {
        if(e.getDamager() instanceof Player p) {
            if(!square.in(e.getEntity().getLocation())) return;
            if (e.getEntity() instanceof Player) {
                if (check(LandFlags.PVP)) return;
                e.setCancelled(true);
                return;
            }
            if (check(LandFlags.ENTITY_DAMAGE_BY_PLAYER, p)) return;
            e.setCancelled(true);
        } else if(e.getEntity() instanceof Player p) {
            if (e.getDamager() instanceof Player) return;
            if(!square.in(e.getEntity().getLocation())) return;
            if (check(LandFlags.PLAYER_DAMAGE_BY_ENTITY, p)) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(EntitySpawnEvent e) {
        if(!square.in(e.getLocation())) return;
        if (check(LandFlags.ENTITY_SPAWN, null)) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockChange(EntityChangeBlockEvent e) {
        if (e.getEntityType() != EntityType.ENDERMAN) return;
        if(!square.in(e.getBlock().getLocation())) return;
        if (check(LandFlags.ENDERMAN_BLOCK, null)) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGrow(BlockGrowEvent e) {
        if(!square.in(e.getBlock().getLocation())) return;
        if (check(LandFlags.GROW, null)) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStructureGrow(StructureGrowEvent e) {
        if(!square.in(e.getLocation())) return;
        if (check(LandFlags.GROW, null)) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickupEXP(PlayerExpChangeEvent e) {
        if(!square.in(e.getPlayer().getLocation())) return;
        if (check(LandFlags.EXP_PICKUP, null)) return;
        e.setAmount(0);
    }
}
