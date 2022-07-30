package org.mooner.lands.command;

import org.bukkit.Bukkit;

import java.util.HashMap;

import static org.mooner.lands.MoonerUtils.chat;

public class CommandManager {
    public static CommandManager init;

    public final HashMap<String, ICommand> commands;

    public CommandManager() {
        commands = new HashMap<>();
        register(
        );
    }

    public void register(ICommand... commands) {
        for (ICommand c : commands) {
            if(c.getName().isEmpty()) {
                Bukkit.getConsoleSender().sendMessage(chat("&cUnregister Command: &c" + c.getClass().getName()));
                continue;
            }
            this.commands.put(c.getName(), c);
        }
    }

    public ICommand getCommand(String cmd) {
        return commands.get(cmd);
    }
}
