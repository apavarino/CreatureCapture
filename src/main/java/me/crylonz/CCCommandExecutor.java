package me.crylonz;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import static me.crylonz.CreatureCapture.generateCaptureBow;

public class CCCommandExecutor implements CommandExecutor {

    private final Plugin plugin;

    public CCCommandExecutor(Plugin p) {
        this.plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player;
        if ((sender instanceof Player)) {
            player = (Player) sender;

            if (cmd.getName().equalsIgnoreCase("cc")) {
                if (player.hasPermission("creaturecapture.cc")) {
                    player.getInventory().addItem(generateCaptureBow());
                }
            }
        }
        return true;
    }
}
