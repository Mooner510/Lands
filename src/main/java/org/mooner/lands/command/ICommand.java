package org.mooner.lands.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface ICommand {
    String getName();

    boolean execute(CommandSender sender, Command cmd, String[] arg);

    List<String> tabComplete(Command cmd, String[] arg);
}
