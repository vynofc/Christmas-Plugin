package xyz.niliees.christmasPlugin.managers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.niliees.christmasPlugin.ChristmasPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class CrackerManager {

    private final ChristmasPlugin plugin;
    private final Map<String, CrackerType> crackerTypes = new HashMap<>();

    public CrackerManager(ChristmasPlugin plugin) {
        this.plugin = plugin;
        loadCrackers();
    }

    public void reload() {
        crackerTypes.clear();
        loadCrackers();
    }

    private void loadCrackers() {
        ConfigurationSection crackersSection = plugin.getConfig().getConfigurationSection("crackers.types");

        if (crackersSection == null) {
            plugin.getLogger().warning("No crackers configured!");
            return;
        }

        for (String key : crackersSection.getKeys(false)) {
            ConfigurationSection crackerSection = crackersSection.getConfigurationSection(key);
            if (crackerSection != null) {
                CrackerType cracker = loadCracker(key, crackerSection);
                crackerTypes.put(key, cracker);
            }
        }

        plugin.getLogger().info("Loaded " + crackerTypes.size() + " cracker types");
    }

    private CrackerType loadCracker(String id, ConfigurationSection section) {
        String name = section.getString("name", "&eCracker");
        Material material = Material.valueOf(section.getString("material", "PAPER"));
        int customModelData = section.getInt("custom-model-data", 0);
        List<String> lore = section.getStringList("lore");
        boolean glow = section.getBoolean("glow", false);

        List<CrackerReward> rewards = new ArrayList<>();
        ConfigurationSection rewardsSection = section.getConfigurationSection("rewards");

        if (rewardsSection != null) {
            for (String rewardKey : rewardsSection.getKeys(false)) {
                ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(rewardKey);
                if (rewardSection != null) {
                    CrackerReward reward = loadReward(rewardSection);
                    if (reward != null) {
                        rewards.add(reward);
                    }
                }
            }
        }

        return new CrackerType(id, name, material, customModelData, lore, glow, rewards);
    }

    private CrackerReward loadReward(ConfigurationSection section) {
        String type = section.getString("type", "ITEM");
        double chance = section.getDouble("chance", 100.0);

        CrackerReward reward = new CrackerReward(type, chance);

        switch (type.toUpperCase()) {
            case "ITEM":
                reward.item = section.getString("item");
                reward.amount = section.getInt("amount", 1);
                break;
            case "COMMAND":
                reward.command = section.getString("command");
                break;
            case "PERMISSION":
                reward.permission = section.getString("permission");
                reward.duration = section.getInt("duration", -1);
                break;
        }

        return reward;
    }

    public ItemStack createCracker(String type) {
        CrackerType cracker = crackerTypes.get(type);
        if (cracker == null) {
            return null;
        }

        ItemStack item = new ItemStack(cracker.material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(plugin.getMessageManager().colorize(cracker.name));

            if (!cracker.lore.isEmpty()) {
                meta.setLore(cracker.lore.stream()
                        .map(plugin.getMessageManager()::colorize)
                        .collect(Collectors.toList()));
            }

            if (cracker.customModelData > 0) {
                meta.setCustomModelData(cracker.customModelData);
            }

            if (cracker.glow) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            // Add NBT tag to identify as cracker
            item.setItemMeta(meta);
        }

        return item;
    }

    public boolean isCracker(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }

        String displayName = meta.getDisplayName();

        // Check if the display name matches any cracker type
        for (CrackerType cracker : crackerTypes.values()) {
            String crackerName = plugin.getMessageManager().colorize(cracker.name);
            if (displayName.equals(crackerName)) {
                return true;
            }
        }

        return false;
    }

    public String getCrackerType(ItemStack item) {
        if (!isCracker(item)) {
            return null;
        }

        String displayName = item.getItemMeta().getDisplayName();

        for (CrackerType cracker : crackerTypes.values()) {
            String crackerName = plugin.getMessageManager().colorize(cracker.name);
            if (displayName.equals(crackerName)) {
                return cracker.id;
            }
        }

        return null;
    }

    public void openCracker(Player player, String type) {
        CrackerType cracker = crackerTypes.get(type);
        if (cracker == null) {
            return;
        }

        // Send opened message
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("cracker", plugin.getMessageManager().colorize(cracker.name));
        plugin.getMessageManager().sendMessage(player, "crackers.opened", placeholders);

        // Select a reward based on chance
        CrackerReward reward = selectReward(cracker.rewards);

        if (reward != null) {
            giveReward(player, reward);
        }
    }

    private CrackerReward selectReward(List<CrackerReward> rewards) {
        if (rewards.isEmpty()) {
            return null;
        }

        // Normalize chances
        double totalChance = rewards.stream().mapToDouble(r -> r.chance).sum();
        double random = Math.random() * totalChance;

        double cumulative = 0;
        for (CrackerReward reward : rewards) {
            cumulative += reward.chance;
            if (random <= cumulative) {
                return reward;
            }
        }

        return rewards.get(rewards.size() - 1);
    }

    private void giveReward(Player player, CrackerReward reward) {
        Map<String, String> placeholders = new HashMap<>();

        switch (reward.type.toUpperCase()) {
            case "ITEM":
                Material material = Material.valueOf(reward.item.toUpperCase());
                ItemStack item = new ItemStack(material, reward.amount);
                player.getInventory().addItem(item);

                placeholders.put("item", material.name());
                placeholders.put("amount", String.valueOf(reward.amount));
                plugin.getMessageManager().sendMessage(player, "crackers.received-item", placeholders);
                break;

            case "COMMAND":
                String command = reward.command.replace("{player}", player.getName());
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                plugin.getMessageManager().sendMessage(player, "crackers.received-command-reward");
                break;

            case "PERMISSION":
                if (plugin.isVaultEnabled() && plugin.getPermission() != null) {
                    plugin.getPermission().playerAdd(player, reward.permission);
                    placeholders.put("permission", reward.permission);
                    plugin.getMessageManager().sendMessage(player, "crackers.received-permission", placeholders);
                }
                break;
        }
    }

    public Set<String> getCrackerTypes() {
        return crackerTypes.keySet();
    }

    public boolean crackerExists(String type) {
        return crackerTypes.containsKey(type);
    }

    // Inner classes
    private static class CrackerType {
        String id;
        String name;
        Material material;
        int customModelData;
        List<String> lore;
        boolean glow;
        List<CrackerReward> rewards;

        CrackerType(String id, String name, Material material, int customModelData,
                   List<String> lore, boolean glow, List<CrackerReward> rewards) {
            this.id = id;
            this.name = name;
            this.material = material;
            this.customModelData = customModelData;
            this.lore = lore;
            this.glow = glow;
            this.rewards = rewards;
        }
    }

    private static class CrackerReward {
        String type;
        double chance;
        String item;
        int amount;
        String command;
        String permission;
        int duration;

        CrackerReward(String type, double chance) {
            this.type = type;
            this.chance = chance;
        }
    }
}

