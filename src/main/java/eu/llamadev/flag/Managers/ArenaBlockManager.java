package eu.llamadev.flag.Managers;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ArenaBlockManager {

    private final File dataFolder;

    public ArenaBlockManager(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    public void saveArenaBlocks(Arena arena) {
        Location pos1 = arena.getPos1();
        Location pos2 = arena.getPos2();
        World world = pos1.getWorld();

        File file = new File(dataFolder, "arenas/" + arena.getName() + "_blocks.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        int index = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() != Material.AIR) {
                        String path = "blocks." + index++;
                        config.set(path + ".x", x);
                        config.set(path + ".y", y);
                        config.set(path + ".z", z);
                        config.set(path + ".type", block.getType().name());
                        if (block.getBlockData() != null) {
                            config.set(path + ".data", block.getBlockData().getAsString());
                        }
                    }
                }
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void restoreArenaBlocks(Arena arena) {
        File file = new File(dataFolder, "arenas/" + arena.getName() + "_blocks.yml");
        if (!file.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        World world = arena.getPos1().getWorld();

        ConfigurationSection blocksSection = config.getConfigurationSection("blocks");
        if (blocksSection == null) {
            return;
        }

        for (String key : blocksSection.getKeys(false)) {
            int x = blocksSection.getInt(key + ".x");
            int y = blocksSection.getInt(key + ".y");
            int z = blocksSection.getInt(key + ".z");
            Material type = Material.valueOf(blocksSection.getString(key + ".type"));
            String data = blocksSection.getString(key + ".data");

            Block block = world.getBlockAt(x, y, z);
            block.setType(type);
            if (data != null) {
                block.setBlockData(Bukkit.createBlockData(data));
            }
        }
    }
}