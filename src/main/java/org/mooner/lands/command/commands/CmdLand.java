package org.mooner.lands.command.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mooner.lands.command.IPlayerCommand;
import org.mooner.lands.gui.gui.MainGUI;

import java.util.Collections;
import java.util.List;

public class CmdLand implements IPlayerCommand {
    @Override
    public String getName() {
        return "land";
    }

    @Override
    public boolean execute(Player p, ItemStack i, Command cmd, String[] arg) {
        new MainGUI(p);
        return true;
    }

    @Override
    public List<String> tabComplete(Command cmd, String[] arg) {
        return Collections.emptyList();
    }
}
