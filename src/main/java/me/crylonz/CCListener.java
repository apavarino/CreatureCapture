package me.crylonz;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.crylonz.CreatureCapture.*;

public class CCListener implements Listener {

    private Plugin plugin;

    public CCListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void addCustomEnchantToTable(PrepareItemEnchantEvent e) {

        double droprate = plugin.getConfig().getDouble("droprate");

        if (e.getEnchantmentBonus() >= 15) {
            if (randVal >= 100 - droprate && e.getItem().getEnchantments().size() == 0) {
                if (e.getItem().getType() == Material.BOW) {
                    e.setCancelled(false);
                    e.getOffers()[2] = new EnchantmentOffer(Enchantment.SILK_TOUCH, 1, 30);
                    isDisplay = true;
                } else {
                    isDisplay = false;
                }
            }
        }
    }

    @EventHandler
    public void onEntityShootBowEvent(EntityShootBowEvent e) {

        if (e.getBow() != null && e.getBow().getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
            if (e.getEntity() instanceof Player) {
                List<String> lores = Objects.requireNonNull(e.getBow().getItemMeta()).getLore();
                int lifeRemaining = Integer.parseInt(
                        ChatColor.stripColor(Objects.requireNonNull(lores).get(0)).split("/")[0]) - 1;

                if (lifeRemaining == 0) {
                    Player p = (Player) e.getEntity();
                    p.getInventory().removeItem(p.getInventory().getItemInMainHand());
                }

                List<String> newLores = new ArrayList<>();
                newLores.add(lifeRemaining + "/" + lores.get(0).split("/")[1]);

                ItemMeta meta = e.getBow().getItemMeta();
                meta.setLore(newLores);
                e.getBow().setItemMeta(meta);

                players.add((Player) e.getEntity());
                new CreatureCapture.Reminder(3);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {

        if (!spawnersCanBeModifiedByEgg) {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (Objects.requireNonNull(e.getClickedBlock()).getType() == Material.SPAWNER) {
                    e.setCancelled(true);
                }
            }
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK &&
                e.getItem() != null &&
                e.getItem().getType() == Material.POLAR_BEAR_SPAWN_EGG &&
                e.getItem().getItemMeta().getDisplayName().contains("Golem")
        ) {
            e.setCancelled(true);
            Location playerTargetBlock = e.getPlayer().getTargetBlock(null, 0).getLocation().add(0, 1, 0);
            e.getPlayer().getWorld().spawnEntity(playerTargetBlock, EntityType.IRON_GOLEM);
            ItemStack itemInMainHand = e.getPlayer().getInventory().getItemInMainHand();
            ItemStack itemInOffHand = e.getPlayer().getInventory().getItemInOffHand();

            if (itemInMainHand.isSimilar(e.getItem())) {
                itemInMainHand.setAmount(itemInMainHand.getAmount() - 1);
            }

            if (itemInOffHand.isSimilar(e.getItem())) {
                itemInOffHand.setAmount(itemInOffHand.getAmount() - 1);
            }
        }
    }

    @EventHandler
    public void onEnchantItemEvent(EnchantItemEvent e) {

        randVal = Math.random() * 100;

        if (e.getItem().getType() == Material.BOW && e.getExpLevelCost() == 30 && isDisplay) {

            ItemStack item = e.getItem();
            item.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
            ItemMeta meta = item.getItemMeta();
            Objects.requireNonNull(meta).setDisplayName(ChatColor.RED + (ChatColor.BOLD + "Capture Bow") + ChatColor.GOLD);
            ArrayList<String> lore = new ArrayList<>();
            lore.add(maxDurability + "/" + maxDurability);
            meta.setLore(lore);
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
            isDisplay = false;
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {

        if (e.getDamager().getType() == EntityType.ARROW) {
            List<Entity> entities = e.getDamager().getNearbyEntities(50, 30, 50);

            for (Entity entity : entities) {
                if (entity instanceof Player) {
                    Player player = (Player) entity;

                    if (player.hasPermission("creaturecapture.capture")) {

                        // if this mob is disabled in option
                        if (!plugin.getConfig().getBoolean(e.getEntity().getType().toString())) {
                            return;
                        }

                        ItemStack item = player.getInventory().getItemInMainHand();
                        if (item.getType() == Material.BOW && item.getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {

                            if (!(e.getEntity() instanceof Player) && players.contains(player) && chanceToCapture >= Math.random() * 100) {

                                e.setCancelled(true);
                                ItemStack egg;

                                try {
                                    if (Bukkit.getVersion().contains("1.12")) {
                                        egg = new ItemStack(Objects.requireNonNull(Material.getMaterial("MONSTER_EGG")), 1, e.getEntity().getType().getTypeId());
                                    } else {
                                        // issue of the API giving mushroom instead of mooshroom
                                        if(e.getEntity().getType() == EntityType.MUSHROOM_COW) {
                                            egg = new ItemStack(Material.valueOf( "MOOSHROOM_SPAWN_EGG"));
                                        } else {
                                            egg = new ItemStack(Material.valueOf(e.getEntity().getType().name() + "_SPAWN_EGG"));

                                        }
                                    }

                                    player.getWorld().dropItem(e.getEntity().getLocation(), egg);
                                    e.getEntity().remove();
                                    player.getWorld().playEffect(e.getEntity().getLocation(), Effect.ENDER_SIGNAL, 10);
                                    players.remove(player);

                                } catch (IllegalArgumentException ignored) {
                                    plugin.getLogger().severe(ignored.getMessage());
                                    if (e.getEntity().getType() == EntityType.IRON_GOLEM) {
                                        ItemStack golemEgg = new ItemStack(Material.POLAR_BEAR_SPAWN_EGG, 1);
                                        ItemMeta meta = golemEgg.getItemMeta();
                                        meta.setDisplayName(ChatColor.RESET + "Iron Golem Spawn Egg");
                                        golemEgg.setItemMeta(meta);
                                        player.getWorld().dropItem(e.getEntity().getLocation(), golemEgg);
                                        e.getEntity().remove();
                                        player.getWorld().playEffect(e.getEntity().getLocation(), Effect.ENDER_SIGNAL, 10);
                                        players.remove(player);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
