package org.mooner.lands.command.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mooner.lands.Lands;
import org.mooner.lands.command.ICommand;
import org.mooner.lands.gui.gui.DupeLandsGUI;
import org.mooner.lands.gui.gui.HomeGUI;
import org.mooner.lands.gui.gui.MainGUI;
import org.mooner.lands.land.db.DatabaseManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.mooner.lands.MoonerUtils.chat;

public class CmdLand implements ICommand {
    @Override
    public String getName() {
        return "land";
    }

    @Override
    public boolean execute(CommandSender sender, Command cmd, String[] arg) {
        if(arg.length > 0) {
            switch (arg[0]) {
                case "reload" -> {
                    if (sender.isOp()) {
                        DatabaseManager.init.update();
                        sender.sendMessage("Reload Complete");
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
                            return true;
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
        if(arg.length == 1) return sender.isOp() ? List.of("reload", "dupe", "home", "homes") : List.of("home", "homes");
        else if(arg.length == 2 && sender.isOp()) return Arrays.stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getName).filter(Objects::nonNull)
                .filter(o -> o.toLowerCase().startsWith(arg[1].toLowerCase()))
                .toList();
        return Collections.emptyList();
    }
}
