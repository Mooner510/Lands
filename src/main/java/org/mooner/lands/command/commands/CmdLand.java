package org.mooner.lands.command.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mooner.lands.command.ICommand;
import org.mooner.lands.gui.gui.DupeLandsGUI;
import org.mooner.lands.gui.gui.FixGUI;
import org.mooner.lands.gui.gui.HomeGUI;
import org.mooner.lands.gui.gui.MainGUI;
import org.mooner.lands.land.PlayerLand;
import org.mooner.lands.land.db.DatabaseManager;
import org.mooner.moonerbungeeapi.api.BungeeAPI;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.mooner.lands.MoonerUtils.chat;

public class CmdLand implements ICommand {
    @Override
    public String getName() {
        return "land";
    }

    @Override
    public boolean execute(CommandSender sender, Command cmd, String[] arg) {
        boolean max = false;
        if (sender instanceof Player p) {
            max = DatabaseManager.init.getPlayerLands(p.getUniqueId()).size() > DatabaseManager.maxLands;
            if(!(arg.length > 0 && (arg[0].equalsIgnoreCase("fix") || arg[0].equalsIgnoreCase("home") || arg[0].equalsIgnoreCase("homes")))) {
                if (max) {
                    sender.sendMessage("", chat("   &cLand 오류로 인해 제한된 수량보다 더 많은 땅을 소유하고 있습니다."));
                    sender.sendMessage("", chat("   &c먼저 &6/land home&c으로 소유한 땅을 확인하신 후, 아래를 눌러 땅을 제거해 주세요."));
                    TextComponent text = new TextComponent(chat("   &b여기&c를 클릭해 땅을 제거하세요."));
                    text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/land fix"));
                    sender.spigot().sendMessage(text);
                    sender.sendMessage("");
                    return true;
                }
            }
        }
        if(arg.length > 0) {
            switch (arg[0]) {
                case "reload" -> {
                    if (sender.isOp()) {
                        DatabaseManager.init.update();
                        sender.sendMessage("Reload Complete");
                    }
                }
                case "fix" -> {
                    if (sender instanceof Player p) {
                        if(max) new FixGUI(p);
                    }
                }
                case "home", "homes" -> {
                    if (sender instanceof Player p) {
                        if (sender.isOp()) {
                            if (arg.length > 1) {
                                final OfflinePlayer off = Arrays.stream(Bukkit.getOfflinePlayers())
                                        .filter(o -> o.getName() != null && o.getName().equalsIgnoreCase(arg[1]))
                                        .findFirst().orElse(null);
                                if (off == null) {
                                    p.sendMessage(ChatColor.RED + arg[1] + "님을 찾을 수 없습니다!");
                                    return true;
                                }
                                new HomeGUI(off, p);
                            } else {
                                new HomeGUI(p, p);
                            }
                            return true;
                        }
                        new HomeGUI(p, p);
                    }
                }
                case "dupe" -> {
                    if (sender instanceof Player p) {
                        if (sender.isOp()) {
                            new DupeLandsGUI(p);
                        }
                    }
                }
                case "center" -> {
                    if (sender instanceof Player p) {
                        if (sender.isOp()) {
                            if(arg.length > 1) {
                                try {
                                    if(!arg[1].startsWith("##")) throw new NumberFormatException();
                                    int id = Integer.parseInt(arg[1].substring(2));
                                    final PlayerLand land = DatabaseManager.init.getPlayerLand(id);
                                    if(land == null) {
                                        p.sendMessage(chat("&c해당 id의 땅을 찾을 수 없습니다."));
                                        return true;
                                    }
                                    final OfflinePlayer owner = Bukkit.getOfflinePlayer(land.getOwner());
                                    final int fromX = land.getSquare().getX();
                                    final int fromZ = land.getSquare().getZ();
                                    final Location location = p.getLocation().clone();
                                    p.sendMessage(chat(BungeeAPI.getPlayerRank(owner).getPrefix() + owner.getName() + "&a님의 땅 &b"+land.getName()+"&a의 중심을 &6" + fromX + ", " + fromZ + "&a에서 &6" + location.getBlockX() + ", " + location.getZ() + "&a으로 변경했습니다."));
                                    TextComponent text = new TextComponent(chat("&7변경사항을 취소하시려면 &b&l여기&r&7를 클릭하세요."));
                                    text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/land restore " + p.getUniqueId() + " " + id + " " + fromX + " " + fromZ));
                                    text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(chat("&e클릭하여 복구하세요!"))));
                                    p.spigot().sendMessage(text);
                                    land.setCenterLocation(location);
                                    DatabaseManager.init.getLandManager(id).update(location);
                                } catch (NumberFormatException e) {
                                    final List<PlayerLand> lands = DatabaseManager.init.searchPlayerLands(arg[1]);
                                    if(lands.isEmpty()) {
                                        p.sendMessage(chat("&c해당 이름 또는 닉네임을 포함한 땅을 찾을 수 없습니다."));
                                        return true;
                                    }
                                    p.sendMessage(chat("&e==== &7아래 목록 중에서 이동할 땅을 선택하세요. &e===="));
                                    TextComponent text;
                                    OfflinePlayer owner;
                                    for (PlayerLand land : lands) {
                                        owner = Bukkit.getOfflinePlayer(land.getOwner());
                                        text = new TextComponent(chat("&7• " + BungeeAPI.getPlayerRank(owner).getPrefix() + owner.getName() + "&7님의 땅 &b" + land.getName()));
                                        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(chat(BungeeAPI.getPlayerRank(owner).getPrefix() + owner.getName() + "&7님의 땅 &b" + land.getName() + "\n&e클릭하여 선택하세요!"))));
                                        text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/land center ##" + land.getId()));
                                        p.spigot().sendMessage(text);
                                    }
                                    p.sendMessage(chat("&e==== &7위 목록 중에서 이동할 땅을 선택하세요. &e===="));
                                }
                            } else {
                                p.sendMessage(chat("&c플레이어 이름 또는 땅 이름을 입력하세요."));
                            }
                        }
                    }
                }
                case "restore" -> {
                    if (sender instanceof Player p) {
                        if (sender.isOp()) {
                            if(arg.length >= 5) {
                                if(arg[1].equals(p.getUniqueId().toString())) {
                                    int id = Integer.parseInt(arg[2]);
                                    final int fromX = Integer.parseInt(arg[3]);
                                    final int fromZ = Integer.parseInt(arg[4]);
                                    final PlayerLand land = DatabaseManager.init.getPlayerLand(id);
                                    if(land == null) {
                                        p.sendMessage(chat("&c해당 id의 땅을 찾을 수 없습니다."));
                                        return true;
                                    }
                                    final OfflinePlayer owner = Bukkit.getOfflinePlayer(land.getOwner());
                                    p.sendMessage(chat(BungeeAPI.getPlayerRank(owner).getPrefix() + owner.getName() + "&a님의 땅 중심 변경을 취소했습니다."));
                                    land.setCenterLocation(fromX, fromZ);
                                }
                            }
                        }
                    }
                }
            }
            return true;
        }
        if(sender instanceof Player p) new MainGUI(p);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command cmd, String[] arg) {
        if(arg.length == 1) return sender.isOp() ? List.of("reload", "dupe", "center", "home", "homes") : List.of("home", "homes");
        else if(arg.length == 2 && sender.isOp()) {
            final String s = arg[1].toLowerCase();
            if(arg[0].equalsIgnoreCase("center")) {
                return Stream.concat(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName), DatabaseManager.init.getPlayerLandNames().stream())
                        .filter(o -> Objects.nonNull(o) && o.toLowerCase().startsWith(s))
                        .toList();
            }
            return Arrays.stream(Bukkit.getOfflinePlayers())
                    .map(OfflinePlayer::getName)
                    .filter(o -> Objects.nonNull(o) && o.toLowerCase().startsWith(s))
                    .toList();
        }
        return Collections.emptyList();
    }
}
