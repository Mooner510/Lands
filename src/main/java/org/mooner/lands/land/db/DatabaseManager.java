package org.mooner.lands.land.db;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.mooner.lands.Lands;
import org.mooner.lands.land.FlagManager;
import org.mooner.lands.land.LandFlags;
import org.mooner.lands.land.PlayerLand;
import org.mooner.lands.land.Square;
import org.mooner.lands.land.db.data.FlagData;
import org.mooner.lands.land.db.data.LandsData;

import javax.annotation.Nullable;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.mooner.lands.Lands.dataPath;
import static org.mooner.lands.Lands.lands;
import static org.mooner.lands.MoonerUtils.chat;
import static org.mooner.lands.MoonerUtils.loadConfig;

public class DatabaseManager {
    public static DatabaseManager init;
    public static final String CONNECTION = "jdbc:sqlite:" + dataPath + "DB/lands.db";
    private int maxSize;
    private int maxLands;

    private Map<String, String> message;
//    private Map<String, List<String>> listMessage;

    private Map<Integer, FlagManager> flagManagerMap;
    private Map<String, LandsData> landsData;
    private Set<PlayerLand> playerLands;

    public DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception x) {
            x.printStackTrace();
        }
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
                                "id INTEGER," +
                                "owner TEXT NOT NULL," +
                                "name TEXT NOT NULL," +
                                "coop TEXT," +
                                "x INTEGER NOT NULL," +
                                "z INTEGER NOT NULL," +
                                "size INTEGER NOT NULL," +
                                "world TEXT NOT NULL," +
                                "cost REAL NOT NULL," +
                                "PRIMARY KEY(id AUTOINCREMENT)" +
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
                                "id INTEGER," +
                                "land INTEGER NOT NULL," +
                                "flag TEXT NOT NULL," +
                                "value INTEGER NOT NULL," +
                                "PRIMARY KEY(id AUTOINCREMENT)" +
                                ")")
        ) {
            s.execute();
            Lands.lands.getLogger().info("성공적으로 Flags DB를 생성했습니다.");
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

        maxSize = 0;
        playerLands = new HashSet<>();
        try(
                Connection c = DriverManager.getConnection(CONNECTION);
                ResultSet r = c.prepareStatement("SELECT * FROM Lands").executeQuery()
        ) {
            while(r.next()) {
                String coop = r.getString("coop");
                playerLands.add(new PlayerLand(
                        r.getInt("id"),
                        UUID.fromString(r.getString("owner")),
                        r.getString("name"),
                        coop == null ? null : Arrays.stream(coop.split(",")).map(UUID::fromString).collect(Collectors.toSet()),
                        r.getInt("x"),
                        r.getInt("z"),
                        r.getString("world"),
                        r.getInt("size"),
                        r.getDouble("cost")
                ));
                maxSize = Math.max(maxSize, r.getInt("size"));
            }
            Lands.lands.getLogger().info("성공적으로 플레이어의 Land를 불러왔습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        flagManagerMap = new HashMap<>();
        try(
                Connection c = DriverManager.getConnection(CONNECTION);
                ResultSet r = c.prepareStatement("SELECT * FROM Flags").executeQuery()
        ) {
            HashMap<Integer, ArrayList<FlagData>> flags = new HashMap<>();
            while(r.next()) {
                int land = r.getInt("land");
                flags.putIfAbsent(land, new ArrayList<>());
                flags.get(land).add(new FlagData(r.getInt("id"), land, LandFlags.valueOf(r.getString("flag")), LandFlags.LandFlagSetting.values()[r.getInt("value")-1]));
            }
            flags.forEach((i, list) -> flagManagerMap.put(i, new FlagManager(i, list)));
            Lands.lands.getLogger().info("성공적으로 Land의 설정들을 불러왔습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        landsData = new HashMap<>();
        FileConfiguration config = loadConfig(dataPath, "lands.yml");
        try {
            config.getKeys(false).forEach(key -> {
                ConfigurationSection c = config.getConfigurationSection(key);
                try {
                    if (c != null)
                        landsData.put(c.getString("name"), new LandsData(chat(c.getString("name")), Material.valueOf(c.getString("material", "").toUpperCase().replace(" ", "_")), c.getInt("size"), c.getDouble("cost"), c.getStringList("lore")));
                } catch (Throwable e) {
                    lands.getLogger().warning(e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Throwable e) {
            lands.getLogger().warning("저장된 Lands를 불러오는 도중 오류가 발생했습니다.");
            e.printStackTrace();
        }

        message = new HashMap<>();
        FileConfiguration mConfig = loadConfig(dataPath, "config.yml");
        try {
            if (mConfig.isSet("messages")) {
                mConfig.getConfigurationSection("messages").getKeys(false).forEach(key -> message.put(key, chat(mConfig.getString(key))));
            }
            maxLands = mConfig.getInt("maxLands", 4);
        } catch (Throwable e) {
            lands.getLogger().warning("저장된 Lands를 불러오는 도중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    public LandsData getLandsData(String s) {
        if(s == null) return null;
        return landsData.get(s);
    }

    public Collection<LandsData> getLandsData() {
        return landsData.values();
    }

    public String getMessage(String s) {
        return message.getOrDefault(s, s);
    }

    public LandState setLand(UUID uuid, String name, Location location, LandsData data) {
        Set<PlayerLand> lands = getPlayerLands(uuid);
        if(lands.size() >= maxLands) return LandState.MAX_LANDS;
        if(lands.stream().anyMatch(land -> land.getName().equals(name))) return LandState.ALREADY_EXISTS;
        if(!canBuy(location, data)) return LandState.OTHER_LAND;
        World w = location.getWorld();
        if(w == null) return LandState.NOT_FOUND;
        Square square = new Square(location.getBlockX(), location.getBlockZ(), data.getSize());
        final int x = location.getBlockX();
        final int z = location.getBlockZ();
        square.getOutline(arr -> w.getHighestBlockAt(arr[0] + x, arr[1] + z).setType(Material.OAK_FENCE));
        Bukkit.getScheduler().runTaskAsynchronously(Lands.lands, () -> {
            try(
                    Connection c = DriverManager.getConnection(CONNECTION);
                    PreparedStatement s = c.prepareStatement("INSERT INTO Lands (owner, name, coop, x, z, size, world, cost) VALUES(?, ?, ?, ?, ?, ?, ?, ?)")
            ) {
                s.setString(1, uuid.toString());
                s.setString(2, name);
                s.setString(3, null);
                s.setInt(4, location.getBlockX());
                s.setInt(5, location.getBlockZ());
                s.setInt(6, data.getSize());
                s.setString(7, w.getName());
                s.setDouble(8, data.getCost());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return LandState.COMPLETE;
    }

    public LandFlags.LandFlagSetting getFlag(int land, LandFlags flag) {
        FlagManager manager = this.flagManagerMap.get(land);
        if(manager == null) return null;
        return manager.getFlag(flag);
    }

    public FlagData getFlagFromDB(int id) {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                ResultSet r = c.prepareStatement("SELECT * from Flags where id="+id).executeQuery()
                ) {
            if (r.next()) {
                return new FlagData(r.getInt("id"), r.getInt("land"), LandFlags.valueOf(r.getString("flag")), LandFlags.LandFlagSetting.values()[r.getInt("value")-1]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public FlagData getFlagFromDB(int land, LandFlags flag) {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                ResultSet r = c.prepareStatement("SELECT * from Flags where land="+land+" and flag=\""+flag+"\"").executeQuery()
        ) {
            if (r.next()) {
                return new FlagData(r.getInt("id"), r.getInt("land"), LandFlags.valueOf(r.getString("flag")), LandFlags.LandFlagSetting.values()[r.getInt("value")-1]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public FlagData setFlag(int land, LandFlags flag, LandFlags.LandFlagSetting setting) {
        if(setting == LandFlags.LandFlagSetting.DEFAULT) {
            try (
                    Connection c = DriverManager.getConnection(DatabaseManager.CONNECTION);
                    PreparedStatement s = c.prepareStatement("DELETE From Flags where land=? and flag=?");
            ) {
                s.setInt(1, land);
                s.setString(2, flag.toString());
                s.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
        try (
                Connection c = DriverManager.getConnection(DatabaseManager.CONNECTION);
                PreparedStatement s = c.prepareStatement("INSERT INTO Flag (land, flag, value) VALUES(?, ?, ?)");
        ) {
            s.setInt(1, land);
            s.setString(2, flag.toString());
            s.setInt(3, setting.ordinal());
            s.executeUpdate();
            return getFlagFromDB(land, flag);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setFlag(int flagId, LandFlags.LandFlagSetting setting) {
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            if(setting == LandFlags.LandFlagSetting.DEFAULT) {
                try (
                        Connection c = DriverManager.getConnection(DatabaseManager.CONNECTION);
                        PreparedStatement s2 = c.prepareStatement("UPDATE Flags SET value=? WHERE id=?")
                ) {
                    s2.setInt(1, flagId);
                    s2.setInt(2, setting.ordinal());
                    s2.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean canBuy(Location location, LandsData data) {
        final int x = location.getBlockX();
        final int z = location.getBlockZ();
        Square s = new Square(x, location.getBlockZ(), data.getSize());
        return playerLands.stream()
                .filter(land -> (x + maxSize >= land.getSquare().getX() || x - maxSize <= land.getSquare().getX()) && (z + maxSize >= land.getSquare().getZ() || z - maxSize <= land.getSquare().getZ()))
                .noneMatch(land -> land.getSquare().isIn(s));
    }

    public PlayerLand getLands(Location location) {
        final int x = location.getBlockX();
        final int z = location.getBlockZ();
        return playerLands.stream()
                .filter(land -> land.getSquare().in(x, z))
                .findFirst()
                .orElse(null);
    }

    public PlayerLand getPlayerLand(UUID uuid, String name) {
        return playerLands.stream()
                .filter(land -> land.getOwner() == uuid && land.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public PlayerLand getPlayerLand(int id) {
        return playerLands.stream()
                .filter(land -> land.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public Set<PlayerLand> getPlayerLands(UUID uuid) {
        return playerLands.stream()
                .filter(land -> land.getOwner() == uuid)
                .collect(Collectors.toSet());
    }
}
