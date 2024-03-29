package org.mooner.lands.land.listener;

import com.google.common.collect.ImmutableSet;
import de.epiceric.shopchest.event.ShopCreateEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mooner.lands.exception.PlayerLandNotFoundException;
import org.mooner.lands.land.LandFlags;
import org.mooner.lands.land.Square;
import org.mooner.lands.land.db.DatabaseManager;

import java.util.UUID;

import static org.mooner.lands.MoonerUtils.saveThrowable;

public class LandListener implements Listener {
    private final int land;
    private final UUID uuid;
    private final UUID owner;
    private final Square square;

    public LandListener(int land, UUID world, Square s) throws PlayerLandNotFoundException {
        this.square = s;
        this.uuid = world;
        this.land = land;
        this.owner = DatabaseManager.init.getPlayerLand(land).getOwner();
    }

    private boolean check(LandFlags flag) {
        return DatabaseManager.init.getFlag(land, flag) == LandFlags.LandFlagSetting.ALLOW;
    }

    private boolean check(LandFlags flag, @NonNull Player p) {
        if((flag.isForcedOwner() && p.getUniqueId().equals(owner)) || p.isOp()) return true;
        LandFlags.LandFlagSetting s;
        if((s = DatabaseManager.init.getFlag(land, flag)) == LandFlags.LandFlagSetting.ALLOW) return true;
        if(s == LandFlags.LandFlagSetting.ONLY_COOP) {
            try {
                return p.getUniqueId().equals(owner) || DatabaseManager.init.getPlayerLand(land).isCoop(p);
            } catch (PlayerLandNotFoundException e) {
                saveThrowable("ERROR", e);
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    public static boolean loc(Location loc1, Location loc2) {
        return loc2 != null && loc1.getX() == loc2.getX() && loc1.getZ() == loc2.getZ() && loc1.getYaw() == loc2.getYaw() && loc1.getPitch() == loc2.getPitch();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMove(PlayerMoveEvent e) {
        if (e.isCancelled()) return;
        if(!e.getPlayer().getWorld().getUID().equals(uuid)) return;
        if(loc(e.getFrom(), e.getTo())) return;
        if(e.getTo() != null && square.in(e.getTo())) {
            e.setCancelled(!check(LandFlags.MOVE_IN, e.getPlayer()));
        }
        if(!e.isCancelled() && e.getTo() != null && square.in(e.getFrom()) && !square.in(e.getTo())) {
            e.setCancelled(!check(LandFlags.MOVE_OUT, e.getPlayer()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) return;
        if(e.getPlayer().isOp()) return;
        if(check(LandFlags.BLOCK_PLACE, e.getPlayer())) return;
        if(!e.getBlock().getWorld().getUID().equals(uuid)) return;
        if(!square.in(e.getBlock().getLocation())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMultiPlace(BlockMultiPlaceEvent e) {
        if (e.isCancelled()) return;
        if(e.getPlayer().isOp()) return;
        if(check(LandFlags.BLOCK_PLACE, e.getPlayer())) return;
        if(!e.getBlock().getWorld().getUID().equals(uuid)) return;
        if(!square.in(e.getBlock().getLocation())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketFill(PlayerBucketEmptyEvent e) {
        if (e.isCancelled()) return;
        if(e.getPlayer().isOp()) return;
        if(check(LandFlags.BLOCK_PLACE, e.getPlayer())) return;
        if(!e.getBlock().getWorld().getUID().equals(uuid)) return;
        if(!square.in(e.getBlock().getLocation())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        if(e.getPlayer().isOp()) return;
        if(check(LandFlags.BLOCK_BREAK, e.getPlayer())) return;
        if(!e.getBlock().getWorld().getUID().equals(uuid)) return;
        if(!square.in(e.getBlock().getLocation())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (e.isCancelled()) return;
        if(e.getPlayer().isOp()) return;
        if(check(LandFlags.BLOCK_BREAK, e.getPlayer())) return;
        if(!e.getBlock().getWorld().getUID().equals(uuid)) return;
        if(!square.in(e.getBlock().getLocation())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if (e.isCancelled()) return;
        if(check(LandFlags.USE_PISTON)) return;
        if(!e.getBlock().getWorld().getUID().equals(uuid)) return;
        if(!square.in(e.getBlock().getLocation())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if (e.isCancelled()) return;
        if(check(LandFlags.USE_PISTON)) return;
        if(!e.getBlock().getWorld().getUID().equals(uuid)) return;
        if(!square.in(e.getBlock().getLocation())) return;
        e.setCancelled(true);
    }

    private final ImmutableSet<Material> doors = ImmutableSet.of(Material.DARK_OAK_DOOR, Material.DARK_OAK_TRAPDOOR, Material.ACACIA_DOOR, Material.ACACIA_TRAPDOOR, Material.BIRCH_DOOR, Material.BIRCH_TRAPDOOR, Material.CRIMSON_DOOR, Material.CRIMSON_TRAPDOOR, Material.JUNGLE_DOOR, Material.JUNGLE_TRAPDOOR, Material.OAK_DOOR, Material.OAK_TRAPDOOR, Material.SPRUCE_DOOR, Material.SPRUCE_TRAPDOOR, Material.WARPED_DOOR, Material.WARPED_TRAPDOOR);

    private final ImmutableSet<Material> buttons = ImmutableSet.of(Material.ACACIA_BUTTON, Material.BIRCH_BUTTON, Material.CRIMSON_BUTTON, Material.DARK_OAK_BUTTON, Material.JUNGLE_BUTTON, Material.OAK_BUTTON, Material.POLISHED_BLACKSTONE_BUTTON, Material.SPRUCE_BUTTON, Material.STONE_BUTTON, Material.WARPED_BUTTON, Material.LEVER);

    private final ImmutableSet<Material> plates = ImmutableSet.of(Material.ACACIA_PRESSURE_PLATE, Material.BIRCH_PRESSURE_PLATE, Material.CRIMSON_PRESSURE_PLATE, Material.DARK_OAK_PRESSURE_PLATE, Material.HEAVY_WEIGHTED_PRESSURE_PLATE, Material.JUNGLE_PRESSURE_PLATE, Material.LIGHT_WEIGHTED_PRESSURE_PLATE, Material.OAK_PRESSURE_PLATE, Material.POLISHED_BLACKSTONE_PRESSURE_PLATE, Material.SPRUCE_PRESSURE_PLATE, Material.STONE_PRESSURE_PLATE, Material.WARPED_PRESSURE_PLATE, Material.STRING);

    private final ImmutableSet<Material> shulker_box = ImmutableSet.of(Material.SHULKER_BOX, Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX);

    private final ImmutableSet<Material> beds = ImmutableSet.of(Material.BLACK_BED, Material.BLUE_BED, Material.BROWN_BED, Material.CYAN_BED, Material.GRAY_BED, Material.GREEN_BED, Material.LIGHT_BLUE_BED, Material.LIGHT_GRAY_BED, Material.LIME_BED, Material.MAGENTA_BED, Material.ORANGE_BED, Material.PINK_BED, Material.PURPLE_BED, Material.RED_BED, Material.WHITE_BED, Material.YELLOW_BED);

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        if (e.useInteractedBlock() == Event.Result.DENY || e.useItemInHand() == Event.Result.DENY) return;
        if(e.getPlayer().isOp()) return;
        if(!e.getPlayer().getWorld().getUID().equals(uuid)) return;
        Block b = e.getClickedBlock();
        if(e.getAction() == Action.PHYSICAL && b != null) {
            if(!plates.contains(b.getType())) return;
            if(check(LandFlags.USE_PLATE, e.getPlayer())) return;
            if(!square.in(b.getLocation())) return;
            e.setCancelled(true);
        } else if(e.getAction() == Action.LEFT_CLICK_BLOCK && b != null) {
            if (b.getType() == Material.NOTE_BLOCK) {
                if(check(LandFlags.PLAY_NOTE_BLOCK, e.getPlayer())) return;
                if(!square.in(b.getLocation())) return;
                e.setCancelled(true);
            }else if (b.getType() == Material.JUKEBOX) {
                if(check(LandFlags.USE_JUKEBOX, e.getPlayer())) return;
                if(!square.in(b.getLocation())) return;
                e.setCancelled(true);
            }
        } else if(e.getAction() == Action.RIGHT_CLICK_BLOCK && b != null) {
            if (e.getItem() != null && e.getItem().getType().isBlock() && e.getPlayer().isSneaking()) return;
            if (b.getType() == Material.ANVIL || b.getType() == Material.CHIPPED_ANVIL || b.getType() == Material.DAMAGED_ANVIL) {
                if(check(LandFlags.USE_ANVIL, e.getPlayer())) return;
                if(!square.in(b.getLocation())) return;
                e.setCancelled(true);
            } else if (b.getType() == Material.ENCHANTING_TABLE || b.getType() == Material.GRINDSTONE) {
                if(check(LandFlags.USE_ENCHANTMENTS, e.getPlayer())) return;
                if(!square.in(b.getLocation())) return;
                e.setCancelled(true);
            } else if (b.getType() == Material.REPEATER || b.getType() == Material.COMPARATOR) {
                if(check(LandFlags.USE_REDSTONE, e.getPlayer())) return;
                if(!square.in(b.getLocation())) return;
                e.setCancelled(true);
            } else if (b.getType() == Material.NOTE_BLOCK) {
                if(check(LandFlags.EDIT_NOTE_BLOCK, e.getPlayer())) return;
                if(!square.in(b.getLocation())) return;
                e.setCancelled(true);
            } else if (b.getType() == Material.JUKEBOX) {
                if(check(LandFlags.USE_JUKEBOX, e.getPlayer())) return;
                if(!square.in(b.getLocation())) return;
                e.setCancelled(true);
            } else if (b.getType() == Material.FURNACE || b.getType() == Material.SMOKER || b.getType() == Material.BLAST_FURNACE || b.getType() == Material.CAMPFIRE || b.getType() == Material.SOUL_CAMPFIRE) {
                if(check(LandFlags.USE_FURNACE, e.getPlayer())) return;
                if(!square.in(b.getLocation())) return;
                e.setCancelled(true);
            } else if (b.getType() == Material.FLOWER_POT || b.getType() == Material.COMPOSTER) {
                if(check(LandFlags.USE_FARM_BLOCK, e.getPlayer())) return;
                if(!square.in(b.getLocation())) return;
                e.setCancelled(true);
            } else if (b.getType() == Material.BEE_NEST || b.getType() == Material.BEEHIVE) {
                if(check(LandFlags.USE_BEE, e.getPlayer())) return;
                if(!square.in(b.getLocation())) return;
                e.setCancelled(true);
            } else if (b.getType() == Material.LODESTONE) {
                if(check(LandFlags.USE_LODESTONE, e.getPlayer())) return;
                if(!square.in(b.getLocation())) return;
                e.setCancelled(true);
            } else if (b.getType() == Material.CHEST || b.getType() == Material.ENDER_CHEST || b.getType() == Material.TRAPPED_CHEST || b.getType() == Material.BARREL || b.getType() == Material.HOPPER || shulker_box.contains(b.getType())) {
                if(check(LandFlags.USE_CHEST, e.getPlayer())) return;
                if(!square.in(b.getLocation())) return;
                e.setCancelled(true);
            } else if (doors.contains(b.getType())) {
                if(check(LandFlags.OPEN, e.getPlayer())) return;
                if(!square.in(b.getLocation())) return;
                e.setCancelled(true);
            } else if (buttons.contains(b.getType())) {
                if(check(LandFlags.USE_BUTTON, e.getPlayer())) return;
                if(!square.in(b.getLocation())) return;
                e.setCancelled(true);
            } else if (beds.contains(b.getType())) {
                if(check(LandFlags.USE_BED, e.getPlayer())) return;
                if(!square.in(b.getLocation())) return;
                e.setCancelled(true);
            }
        } else if(e.getAction() == Action.RIGHT_CLICK_AIR) {
            if(e.getItem() != null) {
                if (e.getItem().getType() == Material.ENDER_PEARL) {
                    if(check(LandFlags.ENDER_PEARL_TELEPORT, e.getPlayer())) return;
                    if(!square.in(e.getPlayer().getLocation())) return;
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        if (e.isCancelled()) return;
        if(e.getPlayer().isOp()) return;
        if(!e.getPlayer().getWorld().getUID().equals(uuid)) return;
        Entity entity = e.getRightClicked();
        if(entity.getType() == EntityType.ITEM_FRAME || entity.getType() == EntityType.GLOW_ITEM_FRAME) {
            if (check(LandFlags.USE_ITEM_FRAME, e.getPlayer())) return;
            if(!square.in(entity.getLocation())) return;
            e.setCancelled(true);
        } else if(entity.getType() == EntityType.VILLAGER) {
            if(check(LandFlags.TRADE, e.getPlayer())) return;
            if(!square.in(entity.getLocation())) return;
            e.setCancelled(true);
        } else if(entity.getType() == EntityType.MINECART_HOPPER || entity.getType() == EntityType.MINECART_CHEST) {
            if(check(LandFlags.USE_CHEST, e.getPlayer())) return;
            if(!square.in(entity.getLocation())) return;
            e.setCancelled(true);
        } else if(entity instanceof Vehicle) {
            if(check(LandFlags.RIDE, e.getPlayer())) return;
            if(!square.in(entity.getLocation())) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBookRemove(PlayerTakeLecternBookEvent e) {
        if (e.isCancelled()) return;
        if(e.getPlayer().isOp()) return;
        if(check(LandFlags.REMOVE_LECTERN_BOOK, e.getPlayer())) return;
        if(!e.getPlayer().getWorld().getUID().equals(uuid)) return;
        if(!square.in(e.getLectern().getLocation())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.isCancelled()) return;
        if(e.getPlayer().isOp()) return;
        if(e.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND || e.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN || e.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN) return;
        if(!e.getPlayer().getWorld().getUID().equals(uuid)) return;
        final boolean to = e.getTo() != null && square.in(e.getTo());
//        final boolean from = square.in(e.getFrom());
        switch (e.getCause()) {
//            case COMMAND -> {
//                if ((from && !check(LandFlags.MOVE_OUT, e.getPlayer())) || (to && !check(LandFlags.MOVE_IN, e.getPlayer()))) {
//                    e.setCancelled(true);
//                }
//            }
            case ENDER_PEARL, CHORUS_FRUIT -> {
                if(to && !check(LandFlags.ENDER_PEARL_TELEPORT, e.getPlayer())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLightning(LightningStrikeEvent e) {
        if (e.isCancelled()) return;
        if(check(LandFlags.LIGHTNING)) return;
        if(!e.getWorld().getUID().equals(uuid)) return;
        if(!square.in(e.getLightning().getLocation())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPickup(EntityPickupItemEvent e) {
        if (e.isCancelled()) return;
        if(!e.getEntity().getWorld().getUID().equals(uuid)) return;
        if (e.getEntity() instanceof Player p) {
            if(p.isOp()) return;
            if(check(LandFlags.ITEM_PICKUP, p)) return;
            if(!square.in(e.getItem().getLocation())) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onThrow(PlayerDropItemEvent e) {
        if (e.isCancelled()) return;
        if(e.getPlayer().isOp()) return;
        if(check(LandFlags.ITEM_THROW, e.getPlayer())) return;
        if(!e.getPlayer().getWorld().getUID().equals(uuid)) return;
        if(!square.in(e.getPlayer().getLocation())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpread(BlockSpreadEvent e) {
        if (e.isCancelled()) return;
        if(!e.getBlock().getWorld().getUID().equals(uuid)) return;
        if (e.getBlock().getType() == Material.FIRE) {
            if(check(LandFlags.FIRE)) return;
            if(!square.in(e.getBlock().getLocation())) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onForm(BlockFormEvent e) {
        if (e.isCancelled()) return;
        if(!e.getBlock().getWorld().getUID().equals(uuid)) return;
        if (e.getBlock().getType() == Material.SNOW) {
            if(check(LandFlags.SNOW)) return;
            if(!square.in(e.getBlock().getLocation())) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFromTo(BlockFromToEvent e) {
        if (e.isCancelled()) return;
        if(!e.getBlock().getWorld().getUID().equals(uuid)) return;
        if (e.getBlock().getType() == Material.LAVA) {
            if(check(LandFlags.LAVA_FLOW)) return;
            if(!square.in(e.getBlock().getLocation())) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onShopCreate(ShopCreateEvent e) {
        if (e.isCancelled()) return;
        if(check(LandFlags.CREATE_SHOP)) return;
        if(!e.getPlayer().getWorld().getUID().equals(uuid)) return;
        if(!square.in(e.getShop().getLocation())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if(e.getEntity() instanceof Player) {
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                if (check(LandFlags.FALL_DAMAGE)) return;
                if (!square.in(e.getEntity().getLocation())) return;
                e.setCancelled(true);
            }
        } else if(e.getEntityType() == EntityType.VILLAGER) {
            if (!check(LandFlags.PROTECT_VILLAGER)) return;
            if (!square.in(e.getEntity().getLocation())) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamageEntity(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if(!e.getEntity().getWorld().getUID().equals(uuid)) return;
        if(e.getDamager() instanceof Player p) {
            if(p.isOp()) return;
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
            if (check(LandFlags.PLAYER_DAMAGE_BY_ENTITY, p)) return;
            if(!square.in(e.getEntity().getLocation())) return;
            e.setCancelled(true);
        } else if(e.getEntity() instanceof Animals) {
            if (!(e.getDamager() instanceof Player p)) return;
            if (check(LandFlags.ANIMAL_DAMAGE_BY_PLAYER, p)) return;
            if(!square.in(e.getEntity().getLocation())) return;
            e.setCancelled(true);
        } else if(e.getEntityType() == EntityType.VILLAGER) {
            if (!square.in(e.getEntity().getLocation())) return;
            if (e.getDamager() instanceof Player p) {
                if (check(LandFlags.VILLAGER_DAMAGE_BY_PLAYER, p)) return;
                e.setCancelled(true);
            } else if(e.getDamager().getType() == EntityType.ZOMBIE || e.getDamager().getType() == EntityType.ZOMBIE_VILLAGER) {
                if (check(LandFlags.VILLAGER_DAMAGE_BY_ZOMBIE)) return;
                e.setCancelled(true);
            } else {
                if (!check(LandFlags.PROTECT_VILLAGER)) return;
                e.setCancelled(true);
            }
        } else if(e.getEntityType() == EntityType.ITEM_FRAME || e.getEntityType() == EntityType.GLOW_ITEM_FRAME) {
            if(e.getDamager() instanceof Player p) {
                if (check(LandFlags.USE_ITEM_FRAME, p)) return;
            } else {
                if (check(LandFlags.PROTECT_ITEM_FRAME)) return;
            }
            if(!square.in(e.getEntity().getLocation())) return;
            e.setCancelled(true);
        }
    }

//    private final Set<EntityType> types = Set.of(EntityType.ARMOR_STAND, EntityType.ARROW, EntityType.DROPPED_ITEM, EntityType.GLOW_ITEM_FRAME, EntityType.ITEM_FRAME, EntityType.BOAT, EntityType.EGG, EntityType.SNOWBALL, EntityType.SNOWMAN, EntityType.IRON_GOLEM, EntityType.LEASH_HITCH, EntityType.BEE, EntityType.);

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpawn(CreatureSpawnEvent e) {
        if (e.isCancelled()) return;
        if(!e.getEntity().getWorld().getUID().equals(uuid)) return;
        if(e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.INFECTION || e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CURED) return;
        if(e.getEntityType() == EntityType.PHANTOM) {
            if (check(LandFlags.PHANTOM_SPAWN)) return;
            if(!square.in(e.getLocation())) return;
            e.setCancelled(true);
            return;
        }
        if(!(e.getEntity() instanceof Monster)) return;
        if (check(LandFlags.ENTITY_SPAWN)) return;
        if(!square.in(e.getLocation())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockChange(EntityChangeBlockEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntityType() != EntityType.ENDERMAN) return;
        if (check(LandFlags.ENDERMAN_BLOCK)) return;
        if(!e.getEntity().getWorld().getUID().equals(uuid)) return;
        if(!square.in(e.getBlock().getLocation())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onGrow(BlockGrowEvent e) {
        if (e.isCancelled()) return;
        if (check(LandFlags.GROW)) return;
        if(!e.getBlock().getWorld().getUID().equals(uuid)) return;
        if(!square.in(e.getBlock().getLocation())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onStructureGrow(StructureGrowEvent e) {
        if (e.isCancelled()) return;
        if (check(LandFlags.GROW)) return;
        if(!e.getWorld().getUID().equals(uuid)) return;
        if(!square.in(e.getLocation())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPickupEXP(PlayerExpChangeEvent e) {
        if (e.getAmount() == 0) return;
        if(e.getPlayer().isOp()) return;
        if (check(LandFlags.EXP_PICKUP, e.getPlayer())) return;
        if(!e.getPlayer().getWorld().getUID().equals(uuid)) return;
        if(!square.in(e.getPlayer().getLocation())) return;
        e.setAmount(0);
    }
}
