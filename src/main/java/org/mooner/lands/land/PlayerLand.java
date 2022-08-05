package org.mooner.lands.land;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerLand {
    @Getter
    private final int id;
    @Getter
    private final UUID owner;
    @Getter
    private final String name;
    private final HashSet<UUID> coop;

    @Getter
    private final Square square;
    @Getter
    private final Location spawnLocation;

    @Getter
    private final double cost;

    public PlayerLand(int id, UUID owner, String name, HashSet<UUID> coop, int x, int z, Location loc, int size, double cost) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.coop = coop;
        this.square = new Square(x, z, size);
        this.spawnLocation = loc;
        this.cost = cost;
    }

    public boolean isCoop(Player p) {
        return isCoop(p.getUniqueId());
    }

    public boolean isCoop(UUID uuid) {
        if(coop == null) return false;
        return coop.contains(uuid);
    }

    public HashSet<UUID> getCoop() {
        return coop;
    }

    public List<Player> getCoopMembers() {
        if(coop == null) return Collections.emptyList();
        return coop.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(HumanEntity::getName))
                .toList();
    }

    public void addCoop(UUID uuid) {
        coop.add(uuid);
    }

    public void removeCoop(UUID uuid) {
        coop.remove(uuid);
    }

    public int getCoopSize() {
        if(coop == null) return 0;
        return coop.size();
    }
}
