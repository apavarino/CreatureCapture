package me.crylonz;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class CreatureCapture extends JavaPlugin implements Listener {

    public static List<Player> players = new ArrayList<>();
    public static double randVal = 0;
    public static boolean isDisplay = false;

    public final Logger log = Logger.getLogger("Minecraft");

    // Config File
    public static int maxDurability = 10;
    public static double chanceToCapture = 50;
    public static boolean spawnersCanBeModifiedByEgg = true;


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

    public void onEnable() {

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new CCListener(this), this);

        Objects.requireNonNull(this.getCommand("cc"), "Command dc not found")
                .setExecutor(new CCCommandExecutor(this));

        File configFile = new File(this.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getConfig().options().header("droprate : Chance in percentage to have capture bow appear in enchant table\n" +
                    "captureBowDurability : Number of arrows that the bow need to break\n" +
                    "chanceToCapture : Percentage of chance to capture the desired mob with the capture bow\n" +
                    "spawnersCanBeModifiedByEgg : Enable/Disable right clicking on spawners with egg\n" +
                    "If you want to disable the ability to capture a specific mob just set false next to mob name\n" +
                    "PLEASE RELOAD AFTER ANY CHANGE");
            getConfig().set("droprate", 3.0);
            getConfig().set("captureBowDurability", maxDurability);
            getConfig().set("chanceToCapture", chanceToCapture);
            getConfig().set("spawnersCanBeModifiedByEgg", spawnersCanBeModifiedByEgg);

            for (EntityType e : EntityType.values()) {
                if (e.isAlive() && e.isSpawnable()) {
                    getConfig().set(e.toString(), true);
                }
            }
            saveConfig();
        }

        maxDurability = getConfig().getInt("captureBowDurability");
        chanceToCapture = getConfig().getDouble("chanceToCapture");
        spawnersCanBeModifiedByEgg = getConfig().getBoolean("spawnersCanBeModifiedByEgg");
        randVal = Math.random() * 100;
    }

    public void onDisable() {
    }

    public static class Reminder {
        Timer timer;

        public Reminder(int seconds) {
            timer = new Timer();
            timer.schedule(new RemindTask(), seconds * 1000L);
        }

        class RemindTask extends TimerTask {
            public void run() {
                players.clear();
                timer.cancel();
            }
        }
    }
}

