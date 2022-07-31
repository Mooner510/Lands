package org.mooner.lands.land.db;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.mooner.lands.Lands;
import org.mooner.lands.land.PlayerLand;
import org.mooner.lands.land.Square;
import org.mooner.lands.land.db.data.LandsData;

import java.io.*;
import java.sql.*;
import java.util.*;

import static org.mooner.lands.Lands.dataPath;
import static org.mooner.lands.Lands.lands;
import static org.mooner.lands.MoonerUtils.chat;
import static org.mooner.lands.MoonerUtils.loadConfig;

public class DatabaseManager {
    public static DatabaseManager init;
    private static final String CONNECTION = "jdbc:sqlite:" + dataPath + " DB/lands.db";
    private static int maxSize;

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
                                "world TEXT NOT NULL" +
                                "cost REAL NOT NULL," +
                                "PRIMARY KEY(id, AUTOINCREMENT)" +
                                ");")
        ) {
            s.execute();
            Lands.lands.getLogger().info("성공적으로 DB를 불러왔습니다.");
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
                Lands.lands.getLogger().info("성공적으로 Config를 생성했습니다.");
            } catch (IOException e) {
                e.printStackTrace();
                Lands.lands.getLogger().warning("Config를 생성하지 못했습니다.");
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
                        coop == null ? null : Arrays.stream(coop.split(",")).map(UUID::fromString).toList(),
                        r.getInt("x"),
                        r.getInt("z"),
                        r.getString("world"),
                        r.getInt("size"),
                        r.getDouble("cost")
                ));
                maxSize = Math.max(maxSize, r.getInt("size"));
            }
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
    }

    public Collection<LandsData> getLandsData() {
        return landsData.values();
    }

    public boolean setLand(UUID uuid, String name, Location location, LandsData data) {
        if(!canBuy(location, data)) return false;
        World w = location.getWorld();
        if(w == null) return false;
        Square square = new Square(location.getBlockX(), location.getBlockZ(), data.getSize());
        final int x = location.getBlockX();
        final int z = location.getBlockZ();
        square.getOutline(arr -> w.getHighestBlockAt(arr[0] + x, arr[1] + z).setType(Material.OAK_FENCE));
        Bukkit.getScheduler().runTaskAsynchronously(lands, () -> {
            try(
                    Connection c = DriverManager.getConnection(CONNECTION);
                    PreparedStatement s = c.prepareStatement("INSERT INTO Lands VALUES(0, ?, ?, ?, ?, ?, ?, ?, ?)")
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
        return true;
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
}
