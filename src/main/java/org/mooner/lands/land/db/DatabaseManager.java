package org.mooner.lands.land.db;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.mooner.lands.Lands;
import org.mooner.lands.exception.PlayerLandNotFoundException;
import org.mooner.lands.land.LandFlags;
import org.mooner.lands.land.LandManager;
import org.mooner.lands.land.PlayerLand;
import org.mooner.lands.land.Square;
import org.mooner.lands.land.db.data.FlagData;
import org.mooner.lands.land.db.data.LandsData;
import org.mooner.moonereco.API.EcoAPI;
import org.mooner.moonereco.API.LogType;

import javax.annotation.Nullable;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.mooner.lands.Lands.dataPath;
import static org.mooner.lands.Lands.lands;
import static org.mooner.lands.MoonerUtils.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class DatabaseManager {
    public static DatabaseManager init;
    private static final String CONNECTION = "jdbc:sqlite:" + dataPath + "DB/lands.db";
//    private int maxSize;
    public static int maxLands;
    private int moreFindDistance;

    private Map<String, String> message;
//    private Map<String, List<String>> listMessage;

    private Set<String> worlds;
    public Map<Integer, LandManager> landManagerMap;
    private Map<String, LandsData> landsData;
    private List<LandsData> landsDataSorted;
    private Set<PlayerLand> playerLands;

    public DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public int getMoreFindDistance() {
        return moreFindDistance;
    }

    public void setUp() {
        new File(dataPath+"DB/").mkdirs();
        File db = new File(dataPath + "DB/", "lands.db");
        if(!db.exists()) {
            try {
                db.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS Lands (" +
                                "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                                "owner TEXT NOT NULL," +
                                "name TEXT NOT NULL," +
                                "coop TEXT," +
                                "x INTEGER NOT NULL," +
                                "z INTEGER NOT NULL," +
                                "world TEXT NOT NULL," +
                                "spawn TEXT NOT NULL," +
                                "size INTEGER NOT NULL," +
                                "cost REAL NOT NULL," +
                                "deleted INTEGER NOT NULL" +
                                ")")
        ) {
            s.execute();
            Lands.lands.getLogger().info("성공적으로 Lands DB를 생성했습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS Flags (" +
                                "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                                "land INTEGER NOT NULL," +
                                "flag TEXT NOT NULL," +
                                "value INTEGER NOT NULL" +
                                ")")
        ) {
            s.execute();
            Lands.lands.getLogger().info("성공적으로 Flags DB를 생성했습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        landManagerMap = new HashMap<>();
        HashMap<Integer, ArrayList<FlagData>> flags = new HashMap<>();
        try(
                Connection c = DriverManager.getConnection(CONNECTION);
                ResultSet r = c.prepareStatement("SELECT * FROM Flags").executeQuery()
        ) {
            while(r.next()) {
                int land = r.getInt("land");
                flags.putIfAbsent(land, new ArrayList<>());
                flags.get(land).add(new FlagData(r.getInt("id"), land, LandFlags.valueOf(r.getString("flag")), LandFlags.LandFlagSetting.values()[r.getInt("value")]));
            }
            Lands.lands.getLogger().info("성공적으로 Land의 설정들을 불러왔습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

//        maxSize = 0;
        playerLands = new HashSet<>();
        try(
                Connection c = DriverManager.getConnection(CONNECTION);
                ResultSet r = c.prepareStatement("SELECT * FROM Lands WHERE deleted == false").executeQuery()
        ) {
            while(r.next()) {
                String[] spawns = r.getString("spawn").split(":");
                int id = r.getInt("id");
                PlayerLand land = new PlayerLand(
                        id,
                        UUID.fromString(r.getString("owner")),
                        r.getString("name"),
                        r.getString("coop"),
                        r.getInt("x"),
                        r.getInt("z"),
                        new Location(Bukkit.getWorld(r.getString("world")), Double.parseDouble(spawns[0]), Double.parseDouble(spawns[1]), Double.parseDouble(spawns[2]), Float.parseFloat(spawns[3]), Float.parseFloat(spawns[4])),
                        r.getInt("size"),
                        r.getDouble("cost")
                );
                playerLands.add(land);
                try {
                    new LandManager(land, flags.get(id)).register();
                } catch (PlayerLandNotFoundException e) {
                    saveThrowable("ERROR", e, land);
                }
//                maxSize = Math.max(maxSize, r.getInt("size"));
            }
            Lands.lands.getLogger().info("성공적으로 플레이어의 Land를 불러왔습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        File f = new File(dataPath, "lands.yml");
        if(!f.exists()) {
            try {
                f.createNewFile();
                InputStream i = Lands.lands.getClass().getResourceAsStream("/lands.yml");
                OutputStream o = new FileOutputStream(f);

                int length;
                byte[] buffer = new byte[1024];

                while (i != null && (length = i.read(buffer)) > 0) o.write(buffer, 0, length);
                o.flush();
                o.close();
                if(i != null) i.close();
                Lands.lands.getLogger().info("성공적으로 lands.yml을(를) 생성했습니다.");
            } catch (IOException e) {
                e.printStackTrace();
                Lands.lands.getLogger().warning("lands.yml을(를) 생성하지 못했습니다.");
            }
        }

        File f2 = new File(dataPath, "config.yml");
        if(!f2.exists()) {
            try {
                f2.createNewFile();
                InputStream i = Lands.lands.getClass().getResourceAsStream("/config.yml");
                OutputStream o = new FileOutputStream(f2);

                int length;
                byte[] buffer = new byte[1024];

                while (i != null && (length = i.read(buffer)) > 0) o.write(buffer, 0, length);
                o.flush();
                o.close();
                if(i != null) i.close();
                Lands.lands.getLogger().info("성공적으로 config.yml을(를) 생성했습니다.");
            } catch (IOException e) {
                e.printStackTrace();
                Lands.lands.getLogger().warning("config.yml을(를) 생성하지 못했습니다.");
            }
        }

        landsData = new HashMap<>();
        landsDataSorted = new ArrayList<>();
        FileConfiguration config = loadConfig(dataPath, "lands.yml");
        try {
            config.getKeys(false).stream().sorted().forEach(key -> {
                ConfigurationSection c = config.getConfigurationSection(key);
                try {
                    if (c != null) {
                        final LandsData data = new LandsData(chat(c.getString("name")), Material.valueOf(c.getString("material", "").toUpperCase().replace(" ", "_")), c.getInt("slotSize"), c.getInt("size"), c.getDouble("cost"), c.getStringList("lore"));
                        landsData.put(c.getString("name"), data);
                        landsDataSorted.add(data);
                    }
                } catch (Throwable e) {
                    lands.getLogger().warning(e.getMessage());
                    e.printStackTrace();
                }
            });
            lands.getLogger().warning("Lands 데이터 로드 완료");
        } catch (Throwable e) {
            lands.getLogger().warning("Lands 데이터를 불러오는 도중 오류가 발생했습니다.");
            e.printStackTrace();
        }

        message = new HashMap<>();
        FileConfiguration mConfig = loadConfig(dataPath, "config.yml");
        try {
            ConfigurationSection sc;
            if ((sc = mConfig.getConfigurationSection("messages")) != null) {
                sc.getKeys(false).forEach(key -> message.put(key, chat(mConfig.getString("messages."+key))));
            }
            if(mConfig.isSet("worlds")) {
                worlds = new HashSet<>(mConfig.getStringList("worlds"));
            }
            maxLands = mConfig.getInt("maxLands", 4);
            moreFindDistance = mConfig.getInt("findDistance", 100);
            lands.getLogger().warning("Lands config 로드 완료");
        } catch (Throwable e) {
            lands.getLogger().warning("Lands config를 불러오는 도중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    public LandsData getLandsData(String s) {
        if(s == null) return null;
        return landsData.get(s);
    }

    public List<LandsData> getLandsData() {
        return landsDataSorted;
    }

    public String getMessage(String s) {
        return message.getOrDefault(s, s);
    }

    public boolean notContainsWorld(World w) {
        if(w == null) return false;
        return worlds.contains(w.getName());
    }

    public LandState setLand(UUID uuid, String name, Location loc, LandsData data) {
        final Location location = loc.clone();
        final OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
        if(!EcoAPI.init.hasPay(offline, data.getCost())) return LandState.NOT_ENOUGH_MONEY;
        if(!notContainsWorld(location.getWorld())) return LandState.NO_WORLD;
        if(name.contains("#")) return LandState.NO_NAME;
        if(checkPlayerLandsWithName(uuid, name)) return LandState.DUPE_NAME;
        List<PlayerLand> lands = getPlayerLands(uuid);
        if(lands.size() >= maxLands) return LandState.MAX_LANDS;
        if(lands.stream().anyMatch(land -> land.getName().equals(name))) return LandState.ALREADY_EXISTS;
        if(!canBuy(uuid, location, data)) return LandState.OTHER_LAND;
        World w = location.getWorld();
        if(w == null) return LandState.NOT_FOUND;
        EcoAPI.init.removePay(offline, data.getCost());
        EcoAPI.init.log(offline, LogType.LAND_BUY, data.getCost());
        final int x = location.getBlockX();
        final int z = location.getBlockZ();
        Bukkit.getScheduler().runTaskAsynchronously(Lands.lands, () -> {
            try(
                    Connection c = DriverManager.getConnection(CONNECTION);
                    PreparedStatement s = c.prepareStatement("INSERT INTO Lands (owner, name, coop, x, z, world, spawn, size, cost) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)")
            ) {
                s.setString(1, uuid.toString());
                s.setString(2, name);
                s.setString(3, null);
                s.setInt(4, x);
                s.setInt(5, z);
                s.setString(6, w.getName());
                s.setString(7, location.getX()+":"+location.getY()+":"+location.getZ()+":"+location.getYaw()+":"+location.getPitch());
                s.setInt(8, data.getSize());
                s.setDouble(9, data.getCost());
                s.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Bukkit.getScheduler().runTaskTimer(Lands.lands, task -> {
                PlayerLand land = getPlayerLandInDB(uuid, name);
                if(land != null && land.getOwner() != null) {
                    Bukkit.getScheduler().runTaskLater(Lands.lands, () -> land.getSquare().getOutline(arr -> new Location(w, arr[0] + x, w.getHighestBlockYAt(arr[0] + x, arr[1] + z) + 1, arr[1] + z).getBlock().setType(Material.OAK_FENCE)), 20L);
                    playerLands.add(land);
                    try {
                        new LandManager(land, null).register();
                    } catch (PlayerLandNotFoundException e) {
                        saveThrowable("ERROR", e, land);
                    }
                    task.cancel();
                }
            }, 0, 2);
        });
        return LandState.COMPLETE;
    }

    public void setCenterLocation(int land, int x, int z) {
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            try (
                    Connection c = DriverManager.getConnection(DatabaseManager.CONNECTION);
                    PreparedStatement s2 = c.prepareStatement("UPDATE Lands SET x=?, z=? where id=?")
            ) {
                s2.setInt(1, x);
                s2.setInt(2, z);
                s2.setInt(3, land);
                s2.executeUpdate();
            } catch (SQLException e) {
                if (e.getErrorCode() == 5 || e.getErrorCode() == 6)
                    Bukkit.getScheduler().runTaskLater(lands, () -> setCenterLocation(land, x, z), 5);
                else e.printStackTrace();
            }
        });
    }

    public void setCenterLocation(int land, Location location) {
        setCenterLocation(land, location.getBlockX(), location.getBlockZ());
    }

    public void setSpawnLocation(int land, Location location) {
        Location loc = location.clone();
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            try (
                    Connection c = DriverManager.getConnection(DatabaseManager.CONNECTION);
                    PreparedStatement s2 = c.prepareStatement("UPDATE Lands SET spawn=? where id=?")
            ) {
                s2.setString(1, loc.getX()+":"+loc.getY()+":"+loc.getZ()+":"+loc.getYaw()+":"+loc.getPitch());
                s2.setInt(2, land);
                s2.executeUpdate();
            } catch (SQLException e) {
                if (e.getErrorCode() == 5 || e.getErrorCode() == 6)
                    Bukkit.getScheduler().runTaskLater(lands, () -> setSpawnLocation(land, loc), 5);
                else e.printStackTrace();
            }
        });
    }

    public LandCoopState addCoop(int land, String name) {
        PlayerLand l;
        try {
            l = getPlayerLand(land);
        } catch (PlayerLandNotFoundException e) {
            saveThrowable("ERROR", e);
            throw new RuntimeException(e);
        }
        if(l == null) return LandCoopState.NOT_FOUND_LAND;
        final LandCoopState state = l.addCoop(name);
        if (state != LandCoopState.COMPLETE) return state;
        try (
                Connection c = DriverManager.getConnection(DatabaseManager.CONNECTION);
                PreparedStatement s2 = c.prepareStatement("UPDATE Lands SET coop=? where id=?")
        ) {
            s2.setString(1, l.getCoop());
            s2.setInt(2, land);
            s2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return state;
    }

    public LandCoopState addCoop(int land, UUID uuid) {
        PlayerLand l;
        try {
            l = getPlayerLand(land);
        } catch (PlayerLandNotFoundException e) {
            saveThrowable("ERROR", e);
            throw new RuntimeException(e);
        }
        if(l == null) return LandCoopState.NOT_FOUND_LAND;
        final LandCoopState state = l.addCoop(uuid);
        if (state != LandCoopState.COMPLETE) return state;
        try (
                Connection c = DriverManager.getConnection(DatabaseManager.CONNECTION);
                PreparedStatement s2 = c.prepareStatement("UPDATE Lands SET coop=? where id=?")
        ) {
            s2.setString(1, l.getCoop());
            s2.setInt(2, land);
            s2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return state;
    }

    private void addCoopFromDB(int id, String coop) {
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            try (
                    Connection c = DriverManager.getConnection(DatabaseManager.CONNECTION);
                    PreparedStatement s2 = c.prepareStatement("UPDATE Lands SET coop=? where id=?")
            ) {
                s2.setString(1, coop);
                s2.setInt(2, id);
                s2.executeUpdate();
            } catch (SQLException e) {
                if (e.getErrorCode() == 5 || e.getErrorCode() == 6)
                    Bukkit.getScheduler().runTaskLater(lands, () -> addCoopFromDB(id, coop), 5);
                else e.printStackTrace();
            }
        });
    }

    public LandCoopState removeCoop(int land, String name) {
        PlayerLand l;
        try {
            l = getPlayerLand(land);
        } catch (PlayerLandNotFoundException e) {
            saveThrowable("ERROR", e);
            throw new RuntimeException(e);
        }
        if(l == null) return LandCoopState.NOT_FOUND_LAND;
        final LandCoopState state = l.removeCoop(name);
        if (state != LandCoopState.COMPLETE) return state;
        removeCoopFromDB(land, l.getCoop());
        return state;
    }

    private void removeCoopFromDB(int id, String coop) {
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            try (
                    Connection c = DriverManager.getConnection(DatabaseManager.CONNECTION);
                    PreparedStatement s2 = c.prepareStatement("UPDATE Lands SET coop=? where id=?")
            ) {
                s2.setString(1, coop);
                s2.setInt(2, id);
                s2.executeUpdate();
            } catch (SQLException e) {
                if(e.getErrorCode() == 5 || e.getErrorCode() == 6)
                    Bukkit.getScheduler().runTaskLater(lands, () -> removeCoop(id, coop), 5);
                else e.printStackTrace();
            }
        });
    }

    public LandCoopState removeCoop(int land, UUID uuid) {
        PlayerLand l = null;
        try {
            l = getPlayerLand(land);
        } catch (PlayerLandNotFoundException e) {
            saveThrowable("ERROR", e);
        }
        if(l == null) return LandCoopState.NOT_FOUND_LAND;
        final LandCoopState state = l.removeCoop(uuid);
        if (state != LandCoopState.COMPLETE) return state;
        removeCoopFromDB(land, l.getCoop());
        return state;
    }

    public LandFlags.LandFlagSetting getRealFlag(int land, LandFlags flag) {
        LandManager manager = this.landManagerMap.get(land);
        if(manager == null) return null;
        return manager.getRealFlag(flag);
    }

    public LandFlags.LandFlagSetting getFlag(int land, LandFlags flag) {
        LandManager manager = this.landManagerMap.get(land);
        if(manager == null) return null;
        return manager.getFlag(flag);
    }

    public FlagData getFlagFromDB(int land, LandFlags flag) {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                ResultSet r = c.prepareStatement("SELECT * from Flags where land="+land+" and flag=\""+flag+"\"").executeQuery()
        ) {
            if (r.next()) {
                return new FlagData(r.getInt("id"), r.getInt("land"), LandFlags.valueOf(r.getString("flag")), LandFlags.LandFlagSetting.values()[r.getInt("value")]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LandManager getLandManager(int land) {
        return this.landManagerMap.get(land);
    }

    @Nullable
    public FlagData setFlag(int land, LandFlags flag, LandFlags.LandFlagSetting setting) {
        try (
                Connection c = DriverManager.getConnection(DatabaseManager.CONNECTION);
                PreparedStatement s = c.prepareStatement("INSERT INTO Flags (land, flag, value) VALUES(?, ?, ?)");
                PreparedStatement s2 = c.prepareStatement("UPDATE Flags SET value=? where land=? and flag=?")
        ) {
            s2.setInt(1, setting.ordinal());
            s2.setInt(2, land);
            s2.setString(3, flag.toString());
            if(s2.executeUpdate() == 0) {
                s.setInt(1, land);
                s.setString(2, flag.toString());
                s.setInt(3, setting.ordinal());
                s.executeUpdate();
            }
            return getFlagFromDB(land, flag);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteLand(int land) {
        playerLands.removeIf(l -> l.getId() == land);
        landManagerMap.remove(land).unregister();
        deleteLandFromDB(land);
    }

    private void deleteLandFromDB(int land) {
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            try (
                    Connection c = DriverManager.getConnection(DatabaseManager.CONNECTION);
                    PreparedStatement s = c.prepareStatement("DELETE From Flags where land=?");
                    PreparedStatement s2 = c.prepareStatement("UPDATE Lands SET deleted=true where id=?")
            ) {
                s.setInt(1, land);
                s.execute();
                s2.setInt(1, land);
                s2.execute();
            } catch (SQLException e) {
                if(e.getErrorCode() == 5 || e.getErrorCode() == 6)
                    Bukkit.getScheduler().runTaskLater(lands, () -> deleteLandFromDB(land), 5);
                else e.printStackTrace();
            }
        });
    }

    public boolean canBuy(UUID uuid, Location location, LandsData data) {
        Square s = new Square(location.getBlockX(), location.getBlockZ(), data.getSize());
        return playerLands.stream()
                .noneMatch(land -> land.getOwner().equals(uuid) ? land.getSquare().isIn(s) : land.getCheckSquare().isIn(s));
    }
    public List<PlayerLand> getDupeLands(PlayerLand l) {
        Square s = l.getSquare();
        return playerLands.stream()
                .filter(land -> l.getId() != land.getId() && land.getCheckSquare().isIn(s))
                .toList();
    }

    public boolean isSafe(Location location) {
        return playerLands.stream()
                .noneMatch(land -> land.getSquare().in(location));
    }

    public PlayerLand getCurrentLand(Player p) {
        final Location location = p.getLocation();
        final int x = location.getBlockX();
        final int z = location.getBlockZ();
        final UUID uuid = p.getUniqueId();
        if(p.isOp())
            return playerLands.stream()
                .filter(land -> land.getSquare().in(x, z))
                .findFirst()
                .orElse(null);
        else
            return playerLands.stream()
                    .filter(land -> land.getOwner().equals(uuid) && land.getSquare().in(x, z))
                    .findFirst()
                    .orElse(null);
    }

    @Nullable
    public PlayerLand getPlayerLandInDB(int id) {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                ResultSet r = c.prepareStatement("SELECT * from Lands where id=\""+id+"\"").executeQuery()
        ) {
            if (r.next()) {
                String[] spawns = r.getString("spawn").split(":");
                return new PlayerLand(
                        r.getInt("id"),
                        UUID.fromString(r.getString("owner")),
                        r.getString("name"),
                        r.getString("coop"),
                        r.getInt("x"),
                        r.getInt("z"),
                        new Location(Bukkit.getWorld(r.getString("world")), Double.parseDouble(spawns[0]), Double.parseDouble(spawns[1]), Double.parseDouble(spawns[2]), Float.parseFloat(spawns[3]), Float.parseFloat(spawns[4])),
                        r.getInt("size"),
                        r.getDouble("cost")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public PlayerLand getPlayerLandInDB(UUID uuid, String name) {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                ResultSet r = c.prepareStatement("SELECT * from Lands where owner=\""+uuid+"\" and name=\""+name+"\"").executeQuery()
        ) {
            if (r.next()) {
                String[] spawns = r.getString("spawn").split(":");
                return new PlayerLand(
                        r.getInt("id"),
                        UUID.fromString(r.getString("owner")),
                        r.getString("name"),
                        r.getString("coop"),
                        r.getInt("x"),
                        r.getInt("z"),
                        new Location(Bukkit.getWorld(r.getString("world")), Double.parseDouble(spawns[0]), Double.parseDouble(spawns[1]), Double.parseDouble(spawns[2]), Float.parseFloat(spawns[3]), Float.parseFloat(spawns[4])),
                        r.getInt("size"),
                        r.getDouble("cost")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public PlayerLand getPlayerLand(UUID uuid, String name) {
        PlayerLand land = playerLands.stream()
                .filter(l -> l.getOwner().equals(uuid) && l.getName().equals(name))
                .findFirst()
                .orElse(null);
        if(land != null) return land;
        land = getPlayerLandInDB(uuid, name);
        if(land != null) playerLands.add(land);
        return land;
    }

    public PlayerLand getPlayerLand(int id) throws PlayerLandNotFoundException {
        PlayerLand land = playerLands.stream()
                .filter(l -> l.getId() == id)
                .findFirst()
                .orElse(null);
        if(land != null) return land;
        land = getPlayerLandInDB(id);
        if(land != null) {
            playerLands.add(land);
            return land;
        }
        throw new PlayerLandNotFoundException("Cannot find land with id '" + id + "'");
    }

    public boolean checkPlayerLandsWithName(UUID uuid, String name) {
        return playerLands.stream()
                .anyMatch(l -> uuid.equals(l.getOwner()) && l.getName().equalsIgnoreCase(name));
    }

    public List<PlayerLand> getPlayerLands(UUID uuid) {
        playerLands = playerLands.stream()
                .map(land -> {
                    if(land.getOwner() == null) {
                        getLandManager(land.getId()).unregister();
                        return getPlayerLandInDB(land.getId());
                    } else return land;
                })
                .collect(Collectors.toSet());
        return playerLands.stream()
                .filter(land -> land.getOwner() != null && land.getOwner().equals(uuid))
                .sorted(Comparator.comparing(PlayerLand::getName))
                .toList();
    }

    public List<PlayerLand> searchPlayerLands(String name) {
        String finalName = name.toLowerCase();
        return playerLands.stream()
                .filter(land -> land.getOwner() != null && Bukkit.getOfflinePlayer(land.getOwner()).getName().contains(name) || land.getName().toLowerCase().replace(" ", "").contains(finalName))
                .sorted(Comparator.comparing(PlayerLand::getName))
                .limit(12)
                .toList();
    }

    public Set<PlayerLand> getPlayerLands() {
        return playerLands;
    }

    public List<String> getPlayerLandNames() {
        return playerLands.stream()
                .map(PlayerLand::getName)
                .toList();
    }
}
