package org.mooner.lands.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mooner.lands.command.IPlayerCommand;

import java.util.Collections;
import java.util.List;

public class Cmd implements IPlayerCommand {
    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean execute(Player p, ItemStack i, Command cmd, String[] arg) {
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command cmd, String[] arg) {
        return Collections.emptyList();
    }
}
