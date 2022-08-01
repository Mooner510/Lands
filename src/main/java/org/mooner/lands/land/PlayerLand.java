package org.mooner.lands.land;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlayerLand {
    @Getter
    private final int id;
    @Getter
    private final UUID owner;
    @Getter
    private final String name;
    private final Set<UUID> coop;

    @Getter
    private final Square square;
    @Getter
    private final String world;

    @Getter
    private final double cost;

    public PlayerLand(int id, UUID owner, String name, Set<UUID> coop, int x, int z, String world, int size, double cost) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.coop = coop;
        this.square = new Square(x, z, size);
        this.world = world;
        this.cost = cost;
    }

    public boolean isCoop(Player p) {
        return isCoop(p.getUniqueId());
    }

    public boolean isCoop(UUID uuid) {
        return coop.contains(uuid);
    }
}
