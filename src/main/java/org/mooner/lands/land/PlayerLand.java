package org.mooner.lands.land;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class PlayerLand {
    private final int id;
    private final UUID owner;
    private final String name;
    private final List<UUID> coop;

    private final Square square;
    private final String world;

    private final double cost;

    public PlayerLand(int id, UUID owner, String name, List<UUID> coop, int x, int z, String world, int size, double cost) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.coop = coop;
        this.square = new Square(x, z, size);
        this.world = world;
        this.cost = cost;
    }
}
