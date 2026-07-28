package fun.vynofc.christmasPlugin.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import fun.vynofc.christmasPlugin.ChristmasPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final ChristmasPlugin plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final Map<String, File> configFiles = new HashMap<>();

    public ConfigManager(ChristmasPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        loadConfig("messages");
        loadConfig("gifts");
    }

    private void loadConfig(String name) {
        File file = new File(plugin.getDataFolder(), name + ".yml");
        if (!file.exists()) {
            plugin.saveResource(name + ".yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        configs.put(name, config);
        configFiles.put(name, file);
    }

    public FileConfiguration getConfig(String name) {
        return configs.getOrDefault(name, plugin.getConfig());
    }

    public void saveConfig(String name) {
        FileConfiguration config = configs.get(name);
        File file = configFiles.get(name);

        if (config != null && file != null) {
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save " + name + ".yml: " + e.getMessage());
            }
        }
    }

    public void reloadConfig(String name) {
        File file = configFiles.get(name);
        if (file != null) {
            configs.put(name, YamlConfiguration.loadConfiguration(file));
        }
    }

    public FileConfiguration getMessagesConfig() {
        return getConfig("messages");
    }

    public FileConfiguration getGiftsConfig() {
        return getConfig("gifts");
    }
}

