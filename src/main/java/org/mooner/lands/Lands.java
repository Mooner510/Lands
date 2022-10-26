package org.mooner.lands;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mooner.lands.command.CommandManager;
import org.mooner.lands.command.ICommand;
import org.mooner.lands.command.IPlayerCommand;
import org.mooner.lands.land.db.DatabaseManager;
import org.mooner.lands.optimize.ChunkCleaner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mooner.lands.MoonerUtils.chat;
import static org.mooner.lands.MoonerUtils.saveThrowable;

public final class Lands extends JavaPlugin {
    public static Lands lands;

    public static final String dataPath = "plugins/Lands/";

    @Override
    public void onEnable() {
        lands = this;
        getLogger().info("Plugin Enabled!");
        Bukkit.getPluginManager().registerEvents(new ChunkCleaner(), this);
        CommandManager.init = new CommandManager();

        DatabaseManager.init = new DatabaseManager();
        DatabaseManager.init.setUp();
        DatabaseManager.init.update();
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin Disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] arg) {
        boolean b = false;
        try {
            ICommand command = CommandManager.init.getCommand(cmd.getName());
            if(command != null) {
                if(command instanceof IPlayerCommand && !(sender instanceof Player)) {
                    sender.sendMessage("&cThis command can only be used by players.");
                    return true;
                }
                b = command.execute(sender, cmd, arg);
            }
//            b = CommandUtils.runCommand(sender, cmd, arg);
        } catch (Exception e) {
            e.printStackTrace();
            if (this.getDescription().getCommands().containsKey(cmd.getName())) {
                sender.sendMessage(chat("  &4Error: " + e.getMessage()));
                sender.sendMessage(chat("  &c예기치 않은 문제가 발생했습니다. 즉시 관리자에게 문의해 주세요."));
            }
            saveThrowable(dataPath + "error/", "ERROR-" + sender.getName(), e);
        }
        return b;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] arg) {
        ICommand command = CommandManager.init.getCommand(cmd.getName());
        if(command != null) return command.tabComplete(sender, cmd, arg);
        return Collections.emptyList();
    }
}
