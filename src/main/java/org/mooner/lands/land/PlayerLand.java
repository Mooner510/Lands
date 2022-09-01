package org.mooner.lands.land;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.mooner.lands.land.db.DatabaseManager;
import org.mooner.lands.land.db.LandCoopState;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerLand {
    public static int MAX_COOP_MEMBERS = 10;

    @Getter
    private final int id;
    @Getter
    private final UUID owner;
    @Getter
    private final String name;
    @Nullable
    private HashSet<UUID> coop;

    @Getter
    private final Square square;
    @Getter
    private Location spawnLocation;

    @Getter
    private final double cost;

    public PlayerLand(int id, UUID owner, String name, @Nullable String coop, int x, int z, Location loc, int size, double cost) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        if(coop != null) {
            this.coop = Arrays.stream(coop.split(", "))
                    .map(UUID::fromString).collect(Collectors.toCollection(HashSet::new));
        }
        this.square = new Square(x, z, size);
        this.spawnLocation = loc;
        this.cost = cost;
    }

    public Square getCheckSquare() {
        return new Square(square.getX(), square.getZ(), square.getDistance() >= DatabaseManager.init.getMoreFindDistance() ? (square.getDistance() + 40) : square.getDistance());
    }

    public void setSpawnLocation(Location loc) {
        this.spawnLocation = loc;
        DatabaseManager.init.setSpawnLocation(id, loc);
    }

    public boolean isCoop(Player p) {
        return isCoop(p.getUniqueId());
    }

    public boolean isCoop(UUID uuid) {
        if(coop == null) return false;
        return coop.contains(uuid);
    }

    public String getCoop() {
        if(coop == null || coop.isEmpty()) return null;
        String s = coop.toString();
        return s.substring(1, s.length() - 1);
    }

    public List<OfflinePlayer> getCoopMembers() {
        if(coop == null) return Collections.emptyList();
        return coop.stream()
                .map(Bukkit::getOfflinePlayer)
                .sorted(Comparator.comparing(OfflinePlayer::getName))
                .toList();
    }

    public LandCoopState addCoop(String name) {
        final OfflinePlayer offlinePlayer = Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
        if(offlinePlayer == null) return LandCoopState.NOT_FOUND;
        if(coop != null) {
            if (coop.size() >= MAX_COOP_MEMBERS) return LandCoopState.MAX_PLAYER;
            if (coop.contains(offlinePlayer.getUniqueId())) return LandCoopState.ALREADY_EXISTS;
        } else coop = new HashSet<>();
        coop.add(offlinePlayer.getUniqueId());
        return LandCoopState.COMPLETE;
    }

    public LandCoopState addCoop(UUID uuid) {
        if(uuid == null) return LandCoopState.NOT_FOUND;
        if(coop != null) {
            if (coop.size() >= MAX_COOP_MEMBERS) return LandCoopState.MAX_PLAYER;
            if (coop.contains(uuid)) return LandCoopState.ALREADY_EXISTS;
        } else coop = new HashSet<>();
        coop.add(uuid);
        return LandCoopState.COMPLETE;
    }

    public LandCoopState removeCoop(String name) {
        if(coop == null) return LandCoopState.NOT_FOUND;
        final OfflinePlayer offlinePlayer = Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
        if(offlinePlayer == null || !coop.contains(offlinePlayer.getUniqueId())) return LandCoopState.NOT_FOUND;
        coop.remove(offlinePlayer.getUniqueId());
        return LandCoopState.COMPLETE;
    }

    public LandCoopState removeCoop(UUID uuid) {
        if(coop == null || uuid == null || !coop.contains(uuid)) return LandCoopState.NOT_FOUND;
        coop.remove(uuid);
        return LandCoopState.COMPLETE;
    }

    public int getCoopSize() {
        if(coop == null) return 0;
        return coop.size();
    }
}
