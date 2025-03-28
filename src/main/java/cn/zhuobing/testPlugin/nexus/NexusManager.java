package cn.zhuobing.testPlugin.nexus;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NexusManager {
    private final Map<String, Location> teamNexusLocations = new HashMap<>();
    private final Map<String, Integer> teamNexusHealth = new HashMap<>();
    private final Map<String, Location> borderFirst = new HashMap<>();
    private final Map<String, Location> borderSecond = new HashMap<>();
    private static final int DEFAULT_HEALTH = 75;
    private final Plugin plugin;
    private File configFile;
    private FileConfiguration config;

    public NexusManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        // 获取插件数据文件夹
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        // 创建配置文件
        configFile = new File(dataFolder, "nexus-config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 加载配置文件
        config = YamlConfiguration.loadConfiguration(configFile);
        // 加载核心位置
        if (config.contains("nexus.locations")) {
            for (String teamName : config.getConfigurationSection("nexus.locations").getKeys(false)) {
                Location location = config.getLocation("nexus.locations." + teamName);
                teamNexusLocations.put(teamName, location);
                // 将核心位置的方块替换为末地石
                if (location != null) {
                    World world = location.getWorld();
                    if (world != null) {
                        world.getBlockAt(location).setType(Material.END_STONE);
                    }
                }
            }
        }
        // 加载核心血量
        if (config.contains("nexus.health")) {
            for (String teamName : config.getConfigurationSection("nexus.health").getKeys(false)) {
                int health = config.getInt("nexus.health." + teamName);
                teamNexusHealth.put(teamName, health);
            }
        }
        // 加载保护区域
        if (config.contains("nexus.borders")) {
            for (String team : config.getConfigurationSection("nexus.borders").getKeys(false)) {
                Location first = config.getLocation("nexus.borders." + team + ".first");
                Location second = config.getLocation("nexus.borders." + team + ".second");
                if (first != null) borderFirst.put(team, first);
                if (second != null) borderSecond.put(team, second);
            }
        }
    }

    public void saveConfig() {
        // 保存核心位置
        for (Map.Entry<String, Location> entry : teamNexusLocations.entrySet()) {
            String teamName = entry.getKey();
            Location location = entry.getValue();
            config.set("nexus.locations." + teamName, location);
        }
        // 保存核心血量
        for (Map.Entry<String, Integer> entry : teamNexusHealth.entrySet()) {
            String teamName = entry.getKey();
            int health = entry.getValue();
            config.set("nexus.health." + teamName, health);
        }
        // 保存保护区域
        for (String team : borderFirst.keySet()) {
            config.set("nexus.borders." + team + ".first", borderFirst.get(team));
        }
        for (String team : borderSecond.keySet()) {
            config.set("nexus.borders." + team + ".second", borderSecond.get(team));
        }
        try {
            // 保存配置文件
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setNexusLocation(String teamName, Location location) {
        teamNexusLocations.put(teamName, location);
        teamNexusHealth.put(teamName, DEFAULT_HEALTH);
    }

    public void setNexusHealth(String teamName, int health) {
        teamNexusHealth.put(teamName, health);
    }

    public Location getTeamNexusLocation(String teamName) {
        return teamNexusLocations.get(teamName);
    }

    public Map<String, Location> getNexusLocations() {
        return teamNexusLocations;
    }

    public int getNexusHealth(String teamName) {
        return teamNexusHealth.getOrDefault(teamName, 0);
    }

    public Map<String, Integer> getNexusHealthOfAllTeam() {
        return teamNexusHealth;
    }

    public boolean hasNexus(String teamName) {
        return teamNexusLocations.containsKey(teamName);
    }

    public void removeNexus(String teamName) {
        teamNexusLocations.remove(teamName);
        teamNexusHealth.remove(teamName);
    }

    // 保护区域设置方法
    public void setBorder(String team, String position, Location loc) {
        if (position.equalsIgnoreCase("first")) {
            borderFirst.put(team, loc);
        } else {
            borderSecond.put(team, loc);
        }
    }

    // 保护区域检查方法
    public boolean isInProtectedArea(Location loc) {
        for (String team : borderFirst.keySet()) {
            Location first = borderFirst.get(team);
            Location second = borderSecond.get(team);
            if (first == null || second == null) continue;

            int minX = Math.min(first.getBlockX(), second.getBlockX());
            int maxX = Math.max(first.getBlockX(), second.getBlockX());
            int minZ = Math.min(first.getBlockZ(), second.getBlockZ());
            int maxZ = Math.max(first.getBlockZ(), second.getBlockZ());

            if (loc.getBlockX() >= minX && loc.getBlockX() <= maxX &&
                    loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ) {
                return true;
            }
        }
        return false;
    }
}