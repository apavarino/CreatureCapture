package me.crylonz;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

import static me.crylonz.CreatureCapture.generateCaptureBow;

public class CCCommandExecutor implements CommandExecutor, TabExecutor {

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
				if (args.length < 1) return true;

				if (args[0].equalsIgnoreCase("get") && (player.hasPermission("creaturecapture.cc") || player.hasPermission("creaturecapture.get"))) {
					player.getInventory().addItem(generateCaptureBow());
				}

				if (args[0].equalsIgnoreCase("reload") && player.hasPermission("creaturecapture.reload")) {
					player.sendMessage("reload config....");
					plugin.reloadConfig();
					player.sendMessage("config reloaded.");
				}
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
		List<String> tabs = new ArrayList<>();
		if (args.length == 1) {
			if (sender.hasPermission("creaturecapture.cc") || sender.hasPermission("creaturecapture.get"))
				tabs.add("get");
			if (sender.hasPermission("creaturecapture.reload"))
				tabs.add("reload");
		}
		return tabs;
	}
}
