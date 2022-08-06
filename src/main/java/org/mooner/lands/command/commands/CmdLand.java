package org.mooner.lands.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mooner.lands.command.ICommand;
import org.mooner.lands.gui.gui.HomeGUI;
import org.mooner.lands.gui.gui.MainGUI;
import org.mooner.lands.land.db.DatabaseManager;

import java.util.Collections;
import java.util.List;

public class CmdLand implements ICommand {
    @Override
    public String getName() {
        return "land";
    }

    @Override
    public boolean execute(CommandSender sender, Command cmd, String[] arg) {
        if(arg.length > 0) {
            switch (arg[0]) {
                case "reload":
                    DatabaseManager.init.update();
                    break;
                case "home":
                    if (sender instanceof Player p) new HomeGUI(p);
                    break;
            }
            return true;
        }
        if(sender instanceof Player p) new MainGUI(p);
        return true;
    }

    @Override
    public List<String> tabComplete(Command cmd, String[] arg) {
        return Collections.emptyList();
    }
}
