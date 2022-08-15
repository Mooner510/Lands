package org.mooner.lands.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mooner.lands.command.IPlayerCommand;
import org.mooner.lands.rtp.RTP;

import java.util.Collections;
import java.util.List;

public class CmdRTP implements IPlayerCommand {
    @Override
    public String getName() {
        return "rtp";
    }

    @Override
    public boolean execute(Player p, ItemStack i, Command cmd, String[] arg) {
        new RTP(p);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command cmd, String[] arg) {
        return Collections.emptyList();
    }
}
