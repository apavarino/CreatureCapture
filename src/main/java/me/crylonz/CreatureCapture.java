package me.crylonz;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CreatureCapture extends JavaPlugin implements Listener {

	public static List<Player> players = new ArrayList<>();
	public static double randVal = 0;
	public static boolean isDisplay = false;

	public final Logger log = Logger.getLogger("Minecraft");

	// Config File
	private int dropRate = 3;
	public static int maxDurability = 10;
	public static double chanceToCapture = 50;
	public static boolean spawnersCanBeModifiedByEgg = true;
	public static Map<String, Boolean> spawnableMobEggs;


	public static ItemStack generateCaptureBow() {
		ItemStack item = new ItemStack(Material.BOW);
		item.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
		ItemMeta meta = item.getItemMeta();
		Objects.requireNonNull(meta).setDisplayName(ChatColor.RED + (ChatColor.BOLD + "Capture Bow") + ChatColor.GOLD);
		ArrayList<String> lore = new ArrayList<>();
		lore.add(maxDurability + "/" + maxDurability);
		meta.setLore(lore);
		meta.setUnbreakable(true);
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public void onEnable() {

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new CCListener(this), this);

		Objects.requireNonNull(this.getCommand("cc"), "Command dc not found").setExecutor(new CCCommandExecutor(this));

		File configFile = new File(this.getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			setConfig(getConfig());
			Map<String, Boolean> mobs = new HashMap<>();
			for (EntityType e : EntityType.values()) {
				if (e.isAlive() && e.isSpawnable()) {
					mobs.put(e.toString(), true);
				}
			}
			getConfig().set("Mob_eggs", mobs);
			saveConfig();
		}

		maxDurability = getConfig().getInt("captureBowDurability");
		chanceToCapture = getConfig().getDouble("chanceToCapture");
		spawnersCanBeModifiedByEgg = getConfig().getBoolean("spawnersCanBeModifiedByEgg");
		dropRate = getConfig().getInt("droprate");
		Map<String, Boolean> mobs = checkOldMobSpawnEggs();
		convertOldConfig(configFile, dropRate, mobs);

		spawnableMobEggs = loadMobSpawnEggs();
		Map<String, Boolean> mobsToAdd = checkIfValuesMissing(spawnableMobEggs);
		if (!mobsToAdd.isEmpty()) {
			mobsToAdd.forEach((entityName, v) -> getConfig().set("Mob_eggs." + entityName, true));
			saveConfig();
			reloadConfig();
			spawnableMobEggs = loadMobSpawnEggs();

		}
		randVal = Math.random() * 100;
	}

	@Override
	public void onDisable() {
	}

	public static class Reminder {
		Timer timer;

		public Reminder(int seconds) {
			timer = new Timer();
			timer.schedule(new RemindTask(), seconds * 1000L);
		}

		class RemindTask extends TimerTask {
			@Override
			public void run() {
				players.clear();
				timer.cancel();
			}
		}
	}

	private void setConfig(FileConfiguration config) {
		config.options().header("droprate : Chance in percentage to have capture bow appear in enchant table\n"
				+ "captureBowDurability : Number of arrows that the bow need to break\n"
				+ "chanceToCapture : Percentage of chance to capture the desired mob with the capture bow\n"
				+ "spawnersCanBeModifiedByEgg : Enable/Disable right clicking on spawners with egg\n"
				+ "If you want to disable the ability to capture a specific mob just set false next to mob name\n"
				+ "PLEASE RELOAD AFTER ANY CHANGE");
		config.set("droprate", dropRate);
		config.set("captureBowDurability", maxDurability);
		config.set("chanceToCapture", chanceToCapture);
		config.set("spawnersCanBeModifiedByEgg", spawnersCanBeModifiedByEgg);
	}
	private void convertOldConfig(final File configFile, final int dropRate, final Map<String, Boolean> mobs) {
		if (!mobs.isEmpty()) {
			YamlConfiguration yamlConfiguration = new YamlConfiguration();
			setConfig(yamlConfiguration);
			yamlConfiguration.set("Mob_eggs", mobs);
			try {
				yamlConfiguration.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			reloadConfig();
		}
	}
	private Map<String, Boolean> loadMobSpawnEggs() {
		Map<String, Boolean> spawnableMobEggs = new HashMap<>();
		ConfigurationSection configurationSection = getConfig().getConfigurationSection("Mob_eggs");
		if (configurationSection != null)
			for (String key : configurationSection.getKeys(true)) {
				spawnableMobEggs.put(key, getConfig().getBoolean("Mob_eggs." +key));
			}
		return spawnableMobEggs;
	}

	private Map<String, Boolean> checkOldMobSpawnEggs() {
		Map<String, Boolean> mobs = new HashMap<>();
		for (EntityType e : EntityType.values()) {
			if (e.isAlive() && e.isSpawnable()) {
				if (getConfig().contains(e.toString())) {
					mobs.put(e.toString(), getConfig().getBoolean(e.toString()));
				}
			}
		}
		return mobs;
	}

	private Map<String, Boolean> checkIfValuesMissing(final Map<String, Boolean> spawnableMobEggs) {
		return Arrays.stream(EntityType.values())
				.filter(entityType -> entityType.isAlive() && entityType.isSpawnable() &&
						spawnableMobEggs.get(entityType.toString()) == null)
				.collect(Collectors.toMap(
						Enum::toString,
						entityType -> true
				));
	}
}

