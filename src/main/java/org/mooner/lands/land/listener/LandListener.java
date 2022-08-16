package org.mooner.lands.land.listener;

import com.google.common.collect.ImmutableSet;
import de.epiceric.shopchest.event.ShopCreateEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
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

    private boolean check(LandFlags flag, @NonNull Player p) {
        if((flag.isForcedOwner() && p.getUniqueId().equals(owner)) || p.isOp()) return true;
        LandFlags.LandFlagSetting s;
        if((s = DatabaseManager.init.getFlag(land, flag)) == LandFlags.LandFlagSetting.ALLOW) return true;
        if(s == LandFlags.LandFlagSetting.ONLY_COOP) {
            return p.getUniqueId().equals(owner) || DatabaseManager.init.getPlayerLand(land).isCoop(p);
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMove(PlayerMoveEvent e) {
        if (e.isCancelled()) return;
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
        if(!square.in(e.getBlock().getLocation())) return;
        if(check(LandFlags.BLOCK_PLACE, e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMultiPlace(BlockMultiPlaceEvent e) {
        if (e.isCancelled()) return;
        if(e.getPlayer().isOp()) return;
        if(!square.in(e.getBlock().getLocation())) return;
        if(check(LandFlags.BLOCK_PLACE, e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketFill(PlayerBucketEmptyEvent e) {
        if (e.isCancelled()) return;
        if(e.getPlayer().isOp()) return;
        if(!square.in(e.getBlock().getLocation())) return;
        if(check(LandFlags.BLOCK_PLACE, e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        if(e.getPlayer().isOp()) return;
        if(!square.in(e.getBlock().getLocation())) return;
        if(check(LandFlags.BLOCK_BREAK, e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (e.isCancelled()) return;
        if(e.getPlayer().isOp()) return;
        if(!square.in(e.getBlock().getLocation())) return;
        if(check(LandFlags.BLOCK_BREAK, e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if (e.isCancelled()) return;
        if(e.getBlocks().stream().anyMatch(b -> square.in(b.getLocation()))) {
            if (check(LandFlags.USE_PISTON)) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if (e.isCancelled()) return;
        if(e.getBlocks().stream().anyMatch(b -> square.in(b.getLocation()))) {
            if (check(LandFlags.USE_PISTON)) return;
            e.setCancelled(true);
        }
    }

    private final ImmutableSet<Material> doors = ImmutableSet.of(Material.DARK_OAK_DOOR, Material.DARK_OAK_TRAPDOOR, Material.ACACIA_DOOR, Material.ACACIA_TRAPDOOR, Material.BIRCH_DOOR, Material.BIRCH_TRAPDOOR, Material.CRIMSON_DOOR, Material.CRIMSON_TRAPDOOR, Material.JUNGLE_DOOR, Material.JUNGLE_TRAPDOOR, Material.OAK_DOOR, Material.OAK_TRAPDOOR, Material.SPRUCE_DOOR, Material.SPRUCE_TRAPDOOR, Material.WARPED_DOOR, Material.WARPED_TRAPDOOR);

    private final ImmutableSet<Material> buttons = ImmutableSet.of(Material.ACACIA_BUTTON, Material.BIRCH_BUTTON, Material.CRIMSON_BUTTON, Material.DARK_OAK_BUTTON, Material.JUNGLE_BUTTON, Material.OAK_BUTTON, Material.POLISHED_BLACKSTONE_BUTTON, Material.SPRUCE_BUTTON, Material.STONE_BUTTON, Material.WARPED_BUTTON, Material.LEVER);

    private final ImmutableSet<Material> plates = ImmutableSet.of(Material.ACACIA_PRESSURE_PLATE, Material.BIRCH_PRESSURE_PLATE, Material.CRIMSON_PRESSURE_PLATE, Material.DARK_OAK_PRESSURE_PLATE, Material.HEAVY_WEIGHTED_PRESSURE_PLATE, Material.JUNGLE_PRESSURE_PLATE, Material.LIGHT_WEIGHTED_PRESSURE_PLATE, Material.OAK_PRESSURE_PLATE, Material.POLISHED_BLACKSTONE_PRESSURE_PLATE, Material.SPRUCE_PRESSURE_PLATE, Material.STONE_PRESSURE_PLATE, Material.WARPED_PRESSURE_PLATE, Material.STRING);

    private final ImmutableSet<Material> shulker_box = ImmutableSet.of(Material.SHULKER_BOX, Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX);

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        if (e.isCancelled()) return;
        if(e.getPlayer().isOp()) return;
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        if (e.isCancelled()) return;
        if(e.getPlayer().isOp()) return;
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.isCancelled()) return;
        if(e.getPlayer().isOp()) return;
        final boolean to = e.getTo() != null && square.in(e.getTo());
        final boolean from = square.in(e.getFrom());
        if(e.getCause() != PlayerTeleportEvent.TeleportCause.COMMAND) {
            if (from && !check(LandFlags.MOVE_OUT, e.getPlayer())) {
                e.setCancelled(true);
                return;
            }
            if (to && !check(LandFlags.MOVE_IN, e.getPlayer())) {
                e.setCancelled(true);
                return;
            }
        }
        if(e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            if(to && !check(LandFlags.ENDER_PEARL_TELEPORT, e.getPlayer())) {
                e.setCancelled(true);
            }
        } else if(e.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
            if(to && !check(LandFlags.FRUIT_TELEPORT, e.getPlayer())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLightning(LightningStrikeEvent e) {
        if (e.isCancelled()) return;
        if(!square.in(e.getLightning().getLocation())) return;
        if(check(LandFlags.LIGHTNING)) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPickup(EntityPickupItemEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof Player p) {
            if(p.isOp()) return;
            if(!square.in(e.getItem().getLocation())) return;
            if(check(LandFlags.ITEM_PICKUP, p)) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onThrow(PlayerDropItemEvent e) {
        if (e.isCancelled()) return;
        if(e.getPlayer().isOp()) return;
        if(!square.in(e.getPlayer().getLocation())) return;
        if(check(LandFlags.ITEM_THROW, e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpread(BlockSpreadEvent e) {
        if (e.isCancelled()) return;
        if (e.getBlock().getType() == Material.FIRE) {
            if(!square.in(e.getBlock().getLocation())) return;
            if(check(LandFlags.FIRE)) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onForm(BlockFormEvent e) {
        if (e.isCancelled()) return;
        if (e.getBlock().getType() == Material.SNOW) {
            if(!square.in(e.getBlock().getLocation())) return;
            if(check(LandFlags.SNOW)) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFromTo(BlockFromToEvent e) {
        if (e.isCancelled()) return;
        if (e.getBlock().getType() == Material.LAVA) {
            if(!square.in(e.getBlock().getLocation())) return;
            if(check(LandFlags.LAVA_FLOW)) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onShopCreate(ShopCreateEvent e) {
        if (e.isCancelled()) return;
        if(!square.in(e.getShop().getLocation())) return;
        if(check(LandFlags.CREATE_SHOP)) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
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
            if(!square.in(e.getEntity().getLocation())) return;
            if (check(LandFlags.PLAYER_DAMAGE_BY_ENTITY, p)) return;
            e.setCancelled(true);
        }
    }

//    private final Set<EntityType> types = Set.of(EntityType.ARMOR_STAND, EntityType.ARROW, EntityType.DROPPED_ITEM, EntityType.GLOW_ITEM_FRAME, EntityType.ITEM_FRAME, EntityType.BOAT, EntityType.EGG, EntityType.SNOWBALL, EntityType.SNOWMAN, EntityType.IRON_GOLEM, EntityType.LEASH_HITCH, EntityType.BEE, EntityType.);

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpawn(EntitySpawnEvent e) {
        if (e.isCancelled()) return;
        if(!(e.getEntity() instanceof Monster)) return;
        if(!square.in(e.getLocation())) return;
        if (check(LandFlags.ENTITY_SPAWN)) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockChange(EntityChangeBlockEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntityType() != EntityType.ENDERMAN) return;
        if(!square.in(e.getBlock().getLocation())) return;
        if (check(LandFlags.ENDERMAN_BLOCK)) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onGrow(BlockGrowEvent e) {
        if (e.isCancelled()) return;
        if(!square.in(e.getBlock().getLocation())) return;
        if (check(LandFlags.GROW)) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onStructureGrow(StructureGrowEvent e) {
        if (e.isCancelled()) return;
        if(!square.in(e.getLocation())) return;
        if (check(LandFlags.GROW)) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPickupEXP(PlayerExpChangeEvent e) {
        if (e.getAmount() == 0) return;
        if(e.getPlayer().isOp()) return;
        if(!square.in(e.getPlayer().getLocation())) return;
        if (check(LandFlags.EXP_PICKUP, e.getPlayer())) return;
        e.setAmount(0);
    }
}
