package org.mooner.lands.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IPlayerCommand extends ICommand {
    @Override
    default boolean execute(CommandSender sender, Command cmd, String[] arg) {
        Player p = (Player) sender;
        ItemStack i = p.getItemInHand();
        return execute(p, i, cmd, arg);
    }

    boolean execute(Player p, ItemStack i, Command cmd, String[] arg);
}
