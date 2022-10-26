package org.mooner.lands;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mooner.lands.Lands.lands;

public class MoonerUtils {
    public static void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(lands, runnable);
    }

    public static Player randomPlayer(World world) {
        List<Player> players = world.getPlayers();
        return players.get(new Random().nextInt(players.size()));
    }

    public static String firstUpper(String str) {
        StringBuilder builder = new StringBuilder();
        for(String s: str.split(" ")) {
            if(s.length() <= 1) builder.append(s);
            else builder.append(s.substring(0, 1).toUpperCase()).append(s.substring(1).toLowerCase()).append(" ");
        }
        if(builder.length() <= 0) return str;
        return builder.substring(0, builder.length() - 1);
    }

    /**
     * {@code 0.5} = 0.5%
     * {@code 1} = 1%
     * {@code 22} = 22%
     * {@code 100} = 100%
    **/
    public static boolean chance(double chance) {
         return Math.random() * 100000 <= chance * 1000;
    }

    public static boolean canHold(Player p, ItemStack i) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null || item.getType().equals(Material.AIR)) return true;

            if (item.getItemMeta() != null) {
                if(item.getItemMeta().equals(i.getItemMeta()) && item.getType().equals(i.getType()) && item.getAmount() + i.getAmount() <= i.getMaxStackSize()) {
                    return true;
                }
            }

            if (item.getType().equals(i.getType()) && item.getAmount() + i.getAmount() <= i.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private static final ImmutableList<String> suffix = ImmutableList.of("", "k", "M", "B", "T", "Q");
    public static String numberTic(double value, int a) {
        if (value < 1) {
            return (int) Math.round(value) + "";
        }
        double amount = Math.floor(Math.floor(Math.log10(value)) / 3);
        if (amount != 0) {
            value = value * Math.pow(0.001, amount);
            return parseString(value, a, true) + suffix.get((int) Math.floor(amount));
        }
        return parseString(value, a, true);
    }

    /**
     * @param d = String
     *
     *          ex. 3k / 2.6m / 12.56k
     *
     * @return = formatted duration
     */
    public static double numberFromString(String d) {
        double r = 0;
        try {
            r = Double.parseDouble(d);
        } catch (Exception ignore) {
            try {
                final double v = Double.parseDouble(d.substring(0, d.length() - 1));
                if (d.contains("b") || d.contains("B")) {
                    r += v * 1000000000;
                } else if (d.contains("m") || d.contains("M")) {
                    r += v * 1000000;
                } else if (d.contains("k") || d.contains("K")) {
                    r += v * 1000;
                }
            } catch (Exception e) {
                return 0;
            }
        }
        return r;
    }

    /**
     * @param d = String
     *
     *          ex. 3s / 3d / 5m2s
     *
     * @return = formatted duration
     */
    public static int durationFromString(String d) {
        int r = 0;
        try {
            r = Integer.parseInt(d);
        } catch (Exception ignore) {
            try {
                if (d.contains("d")) {
                    r += Integer.parseInt(d.substring(0, d.indexOf("d"))) * 60 * 60 * 24;
                    if (d.length() > d.indexOf("d") + 1)
                        d = d.substring(d.indexOf("d") + 1);
                }

                if (d.contains("h")) {
                    r += Integer.parseInt(d.substring(0, d.indexOf("h"))) * 60 * 60;
                    if (d.length() > d.indexOf("h") + 1)
                        d = d.substring(d.indexOf("h") + 1);
                }

                if (d.contains("m")) {
                    r += Integer.parseInt(d.substring(0, d.indexOf("m"))) * 60;
                    if (d.length() > d.indexOf("m") + 1)
                        d = d.substring(d.indexOf("m") + 1);
                }

                if (d.contains("s")) {
                    r += Integer.parseInt(d.substring(0, d.indexOf("s")));
                }
            } catch (Exception e) {
                return 0;
            }
        }
        return r;
    }

    /**
     * @param format = String Format
     * @return = Formatted String
     * @see SimpleDateFormat
     * @see SimpleDateFormat#format(Date)
     */
    public static String getDate(String format) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.KOREA);
        return sdf.format(date);
    }

    /**
     * @param d = per second
     * @return = formatted duration
     */
    public static String duration(double d) {
        int second = (int) Math.floor(d);
        int minute = 0;
        int hour = 0;
        while(second >= 60) {
            second -= 60;
            minute++;
        }
        while(minute >= 60) {
            minute -= 60;
            hour++;
        }
        return ((hour > 0) ? toTimeFormatNumber(hour) + ":" : "") + ((minute > 0) ? toTimeFormatNumber(minute) + ":" : ((hour > 0)?"0":"")+"0:") + toTimeFormatNumber(second);
    }

    /**
     * @param d = ms
     * @return = formatted duration
     */
    public static String duration(long d) {
        int second = (int) Math.floor(d / 1000d);
        int minute = 0;
        int hour = 0;
        while(second >= 60) {
            second -= 60;
            minute++;
        }
        while(minute >= 60) {
            minute -= 60;
            hour++;
        }
        return ((hour > 0) ? toTimeFormatNumber(hour) + ":" : "") + ((minute > 0) ? toTimeFormatNumber(minute) + ":" : ((hour > 0)?"0":"")+"0:") + toTimeFormatNumber(second);
    }

    /**
     * @param d = per second
     * @return = formatted duration
     */
    public static String durationTime(double d) {
        int second = (int) Math.floor(d);
        int minute = 0;
        int hour = 0;
        while(second >= 60) {
            second -= 60;
            minute++;
        }
        while(minute >= 60) {
            minute -= 60;
            hour++;
        }
        return ((hour > 0) ? toTimeFormatNumber(hour) + "h " : "") + ((minute > 0) ? toTimeFormatNumber(minute) + "m " : ((hour > 0)?"0":"")+"0m ") + toTimeFormatNumber(second) + "s";
    }

    public static String toTimeFormatNumber(long d) {
        return (d < 10) ? "0" + d : d + "";
    }

    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static String replaceLast(String str, String regex, String replacement) {
        int regexIndexOf = str.lastIndexOf(regex);
        if(regexIndexOf == -1){
            return str;
        } else {
            return str.substring(0, regexIndexOf) + replacement + str.substring(regexIndexOf + regex.length());
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static FileConfiguration loadConfig(String Path, String File) {
        File f = new File(Path, File);
        if(!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileInputStream stream = null;
        try {
            stream = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assert stream != null;
        return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, Charsets.UTF_8));
    }

    public static long calcInt(String calc) {
        return Math.round(calcDouble(calc));
    }

    public static long calcInt(String calc, double value, int multi) {
        return Math.round(calcDouble(calc, value, multi));
    }

    public static double calcDouble(String calc) {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        try {
            return Double.parseDouble(engine.eval(calc).toString());
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double calcDouble(String calc, double value, int multi) {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        calc = calc
                .replace("{a}", (value * multi) + "")
                .replace("{level}", multi+"")
                .replace("{value}", value+"");
        try {
            return Double.parseDouble(engine.eval(calc).toString());
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String chat(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static String rome(long value) {
        StringBuilder s = new StringBuilder();
        if(value <= 0) return value + "";
        if(value >= 10000) return value + "";
        long v = value;
        while(v >= 9000) {
            s.append("FMF");
            v -= 9000;
        }
        while(v >= 5000) {
            s.append("F");
            v -= 5000;
        }
        while(v >= 4000) {
            s.append("MF");
            v -= 4000;
        }
        while(v >= 1000) {
            s.append("M");
            v -= 1000;
        }
        while(v >= 900) {
            s.append("CM");
            v -= 900;
        }
        while(v >= 500) {
            s.append("D");
            v -= 500;
        }
        while(v >= 400) {
            s.append("CD");
            v -= 400;
        }
        while(v >= 100) {
            s.append("C");
            v -= 100;
        }
        while(v >= 90) {
            s.append("XC");
            v -= 100;
        }
        while(v >= 50) {
            s.append("L");
            v -= 50;
        }
        while(v >= 40) {
            s.append("XL");
            v -= 40;
        }
        while(v >= 10) {
            s.append("X");
            v -= 10;
        }
        while(v >= 9) {
            s.append("IX");
            v -= 9;
        }
        while(v >= 5) {
            s.append("V");
            v -= 5;
        }
        while(v >= 4) {
            s.append("IV");
            v -= 4;
        }
        while(v >= 1) {
            s.append("I");
            v -= 1;
        }
        return s.toString();
    }

    public static String commaNumber(int number) {
        return NumberFormat.getInstance().format(number);
    }

    public static String commaNumber(double number) {
        return NumberFormat.getInstance().format(number);
    }

    public static double parseStringNumber(String s) throws Exception {
        if(s.length() <= 0) throw new Exception("Please enter a number!");
        double multiplier = 1;
        String[] strings = new String[]{s, s.substring(s.length() - 1)};
        try {
            Double.parseDouble(strings[1]);
            strings = new String[]{s.substring(0, s.length() - 1), s.substring(s.length() - 1)};
            if (strings[1].equalsIgnoreCase("k")) multiplier = 1000;
            else if (strings[1].equalsIgnoreCase("m")) multiplier = 1000000;
            else if (strings[1].equalsIgnoreCase("b")) multiplier = 1000000000;

            try {
                return Double.parseDouble(strings[0]) * multiplier;
            } catch (NumberFormatException e) {
                throw new Exception("Please enter a number!");
            }
        } catch (Exception ignore) {
            throw new Exception("Please enter a number!");
        }
    }

    public static String parseIfInt(double value, boolean comma) {
        if(value >= Integer.MAX_VALUE) {
            return numberTic(value, 3);
        }
        if(comma) {
            if (Math.floor(value) == value) {
                return commaNumber((int) Math.floor(value));
            }
            return commaNumber(value);
        } else {
            if (Math.floor(value) == value) {
                return ((int) Math.floor(value)) + "";
            }
            return BigDecimal.valueOf(value).toPlainString();
        }
    }

    public static String parseString(double value) {
        BigDecimal b = BigDecimal.valueOf(value);
        b = b.setScale(0, RoundingMode.DOWN);
        return parseIfInt(Double.parseDouble(b.toString()), false);
    }

    public static String parseString(double value, int amount) {
        BigDecimal b = BigDecimal.valueOf(value);
        b = b.setScale(amount, RoundingMode.DOWN);
        return parseIfInt(Double.parseDouble(b.toString()), false);
    }

    public static String parseString(double value, boolean comma) {
        BigDecimal b;
        try {
            b = BigDecimal.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
            return value + "";
        }
        b = b.setScale(0, RoundingMode.DOWN);
        return parseIfInt(Double.parseDouble(b.toString()), comma);
    }

    public static String parseString(double value, int amount, boolean comma) {
        BigDecimal b = BigDecimal.valueOf(value);
        b = b.setScale(amount, RoundingMode.DOWN);
        return parseIfInt(Double.parseDouble(b.toString()), comma);
    }

    public static double parseDouble(double value) {
        BigDecimal b = BigDecimal.valueOf(value);
        b = b.setScale(0, RoundingMode.DOWN);
        return Double.parseDouble(b.toString());
    }

    public static double parseDouble(double value, int amount) {
        BigDecimal b = BigDecimal.valueOf(value);
        b = b.setScale(amount, RoundingMode.DOWN);
        return Double.parseDouble(b.toString());
    }

    public static double parseDouble(BigDecimal value) {
        return Double.parseDouble(value.toString());
    }

    public static double parseDouble(BigDecimal value, int amount) {
        value = value.setScale(amount, RoundingMode.DOWN);
        return Double.parseDouble(value.toString());
    }

    public static String parseDoubleString(double value, int amount) {
        BigDecimal b = BigDecimal.valueOf(value);
        b = b.setScale(amount, RoundingMode.HALF_UP);
        return String.valueOf(Double.parseDouble(b.toString()));
    }

    public static boolean onGround(Player p) {
        if(!p.isFlying()) {
            if(p.getLocation().getBlock().getType().equals(Material.AIR)) {
                if (p.getLocation().subtract(0, 0.005, 0).getBlock().getType().isSolid()) {
                    return true;
                } else return p.getLocation().add(0.3, -0.005, 0.3).getBlock().getType().isSolid() || p.getLocation().add(-0.3, -0.005, -0.3).getBlock().getType().isSolid();
            }
        }
        return false;
    }

    public static long getTimeSec() {
        return (long) Math.floor((double) System.currentTimeMillis() / 1000);
    }

    public static long getTime() {
        return System.currentTimeMillis();
    }

    public static boolean isDay() {
        World world = Bukkit.getWorld("world");
        return (world != null ? world.getTime() : 0) < 12300 || world.getTime() > 23850;
    }

    /**
     * Checks if a location is safe (solid ground with 2 breathable blocks)
     *
     * @param location Location to check
     * @return True if location is safe
     */
    @SuppressWarnings("deprecation")
    public static boolean isSafeLocation(Location location) {
        Block feet = location.getBlock();
        if (!feet.getType().isTransparent() && !feet.getLocation().add(0, 1, 0).getBlock().getType().isTransparent()) {
            return false; // not transparent (will suffocate)
        }
        Block head = feet.getRelative(BlockFace.UP);
        if (!head.getType().isTransparent()) {
            return false; // not transparent (will suffocate)
        }
        Block ground = feet.getRelative(BlockFace.DOWN);
        return ground.getType().isSolid(); // not solid
    }

    public static void playSound(Location l, Sound s, double volume, double pitch) {
        Bukkit.getScheduler().runTask(lands, () -> {
            World w;
            if((w = l.getWorld()) == null) return;
            w.playSound(l, s, (float) volume, (float) pitch);
        });
    }

    public static void playSound(Location l, Sound s, double volume, double pitch, int delay) {
        Bukkit.getScheduler().runTaskLater(lands, () -> {
            World w;
            if((w = l.getWorld()) == null) return;
            w.playSound(l, s, (float) volume, (float) pitch);
        }, delay);
    }

    public static void playSound(Player p, String s, double volume, double pitch) {
        p.playSound(p.getLocation(), s, (float) volume, (float) pitch);
    }

    public static void playSound(Player p, Sound s, double volume, double pitch) {
        p.playSound(p.getLocation(), s, (float) volume, (float) pitch);
    }

    public static void playSound(Player p, Sound s, double volume, double pitch, double delay) {
        Bukkit.getScheduler().runTaskLater(lands, () -> p.playSound(p.getLocation(), s, (float) volume, (float) pitch), (long) Math.floor(delay * 20));
    }

    public static void playSound(Player p, Sound s, double volume, double pitch, int delay) {
        Bukkit.getScheduler().runTaskLater(lands, () -> p.playSound(p.getLocation(), s, (float) volume, (float) pitch), delay);
    }

    public static void createHolo(String text, Location loc, long time) {
        Hologram h = HologramsAPI.createHologram(lands, loc);
        h.appendTextLine(text);
        Bukkit.getScheduler().runTaskLater(lands, h::delete, time);
    }

    public static void dropExp(Location loc, int exp) {
        if(exp > 0) Bukkit.getScheduler().runTask(lands, () -> {
            World w;
            if((w = loc.getWorld()) == null) return;
            w.spawn(loc, ExperienceOrb.class).setExperience(exp);
        });
    }

    public static String itemToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeObject(item);

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            lands.getLogger().warning("Unable to save item stacks. "+ e);
        }
        return null;
    }

    public static ItemStack itemFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            // Read the serialized inventory
            ItemStack item = (ItemStack) dataInput.readObject();

            dataInput.close();
            return item;
        } catch (Exception e) {
            lands.getLogger().warning("Unable to decode class type. "+ e);
        }
        return null;
    }

    public static void saveThrowable(String name, Throwable throwable, Object... objects) {
        saveThrowable(Lands.dataPath + "error/", name, throwable, objects);
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveThrowable(String path, String name, Throwable throwable, Object... objects) {
        new File(path).mkdirs();
        File f = new File(path, name + '-' + UUID.randomUUID());
        try {
            f.createNewFile();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        try(
                FileWriter fileWriter = new FileWriter(f);
                PrintWriter writer = new PrintWriter(fileWriter)
        ) {
            for (Object o : objects) writer.println(o);
            writer.println("[ Error ] " + throwable.getMessage());
            writer.println("[ Error - Localized ] " + throwable.getLocalizedMessage());
            writer.println();
            for (StackTraceElement element : throwable.getStackTrace()) writer.println(element.toString());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
