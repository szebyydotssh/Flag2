package eu.llamadev.flag.Listeners;

import eu.llamadev.flag.Flag;
import eu.llamadev.flag.Managers.Arena;
import eu.llamadev.flag.Managers.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LuckyBlockBreakListener implements Listener {

    private final Flag plugin = Flag.getPlugin(Flag.class);

    @EventHandler
    public void onLuckyBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location blockLocation = event.getBlock().getLocation();

        if (event.getBlock().getType() != Material.YELLOW_STAINED_GLASS) return;

        Arena arena = plugin.getArenaManager().getArenaByPlayer(player);
        if (arena == null || plugin.getGameManager().getGameState() != GameState.INGAME) return;

        event.setCancelled(true);
        event.getBlock().setType(Material.AIR);

        ConfigurationSection luckyBlockSection = plugin.getConfig().getConfigurationSection("lucky-block");
        if (luckyBlockSection == null) return;

        int waitTime = luckyBlockSection.getInt("wait", 5); // Default to 5 seconds if not specified

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            blockLocation.getBlock().setType(Material.YELLOW_STAINED_GLASS);
            player.sendMessage("§aThe LuckyBlock has been reset!");
        }, waitTime * 20L);

        Random random = new Random();
        for (String key : luckyBlockSection.getKeys(false)) {
            ConfigurationSection blockConfig = luckyBlockSection.getConfigurationSection(key);
            if (blockConfig == null) continue;

            double chance = blockConfig.getDouble("chance", 0.0);
            if (random.nextDouble() > chance) continue;

            String type = blockConfig.getString("type");
            if (type == null) continue;

            switch (type.toLowerCase()) {
                case "item":
                    handleItemLuckyBlock(player, blockConfig);
                    break;
                case "effect":
                    handleEffectLuckyBlock(player, blockConfig);
                    break;
                case "mob":
                    handleMobLuckyBlock(blockLocation, blockConfig);
                    break;
                case "explosion":
                    handleExplosionLuckyBlock(blockLocation, blockConfig);
                    break;
                default:
                    Bukkit.getLogger().warning("Unknown LuckyBlock type: " + type);
            }
            break;
        }
    }

    private void handleItemLuckyBlock(Player player, ConfigurationSection blockConfig) {
        String itemId = blockConfig.getString("item.id");
        int amount = blockConfig.getInt("item.amount", 1);
        String name = blockConfig.getString("item.name");
        List<String> enchantments = blockConfig.getStringList("item.enchantments");
        List<String> lore = blockConfig.getStringList("item.lore");

        Material material = Material.getMaterial(itemId);
        if (material == null) {
            player.sendMessage("§cInvalid item ID in LuckyBlock configuration: " + itemId);
            return;
        }

        ItemStack item = new ItemStack(material, amount);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null && !name.isEmpty()) {
                meta.setDisplayName(eu.llamadev.flag.Utils.Color.translate(name));
            }

            if (lore != null && !lore.isEmpty()) {
                List<String> translatedLore = new ArrayList<>();
                for (String line : lore) {
                    translatedLore.add(eu.llamadev.flag.Utils.Color.translate(line));
                }
                meta.setLore(translatedLore);
            }

            item.setItemMeta(meta);

            for (String enchantmentString : enchantments) {
                String[] parts = enchantmentString.split(":");
                if (parts.length == 2) {
                    try {
                        String enchantmentName = parts[0].toUpperCase();
                        int level = Integer.parseInt(parts[1]);
                        org.bukkit.enchantments.Enchantment enchantment = org.bukkit.enchantments.Enchantment.getByName(enchantmentName);
                        if (enchantment != null) {
                            item.addUnsafeEnchantment(enchantment, level);
                        } else {
                            Bukkit.getLogger().warning("Invalid enchantment name: " + enchantmentName);
                        }
                    } catch (NumberFormatException e) {
                        Bukkit.getLogger().warning("Invalid enchantment level: " + enchantmentString);
                    }
                } else {
                    Bukkit.getLogger().warning("Invalid enchantment format: " + enchantmentString);
                }
            }
        }

        player.getInventory().addItem(item);
        player.sendMessage("§aYou received an item from the LuckyBlock!");
    }
    private void handleEffectLuckyBlock(Player player, ConfigurationSection blockConfig) {
        String effectName = blockConfig.getString("effect.name");
        int level = blockConfig.getInt("effect.level", 1);
        int time = blockConfig.getInt("effect.time", 10);

        PotionEffectType effectType = PotionEffectType.getByName(effectName);
        if (effectType != null) {
            player.addPotionEffect(new PotionEffect(effectType, time * 20, level - 1));
            player.sendMessage("§aYou received a potion effect from the LuckyBlock!");
        }
    }

    private void handleMobLuckyBlock(Location location, ConfigurationSection blockConfig) {
        String mobName = blockConfig.getString("mob.name");
        int amount = blockConfig.getInt("mob.amount", 1);

        for (int i = 0; i < amount; i++) {
            Zombie zombie = location.getWorld().spawn(location, Zombie.class);
            zombie.setCustomName(mobName);
            zombie.setCustomNameVisible(true);
        }
    }

    private void handleExplosionLuckyBlock(Location location, ConfigurationSection blockConfig) {
        int radius = blockConfig.getInt("explosion.radius", 1);
        boolean wait = blockConfig.getInt("explosion.wait", 0) > 0;

        if (wait) {
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    location.getWorld().createExplosion(location, radius, false, false), 20L);
        } else {
            location.getWorld().createExplosion(location, radius, false, false);
        }
    }
}