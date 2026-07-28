package fun.vynofc.christmasPlugin.managers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import fun.vynofc.christmasPlugin.ChristmasPlugin;

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

        // Rewards is a list in the config, not a map
        if (section.contains("rewards")) {
            Object rewardsObj = section.get("rewards");
            if (rewardsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<?, ?>> rewardsList = (List<Map<?, ?>>) rewardsObj;
                for (Map<?, ?> rewardMap : rewardsList) {
                    CrackerReward reward = loadRewardFromMap(rewardMap);
                    if (reward != null) {
                        rewards.add(reward);
                        plugin.getLogger().info("  Loaded reward: " + reward.type + " - " +
                            (reward.item != null ? reward.item : reward.command != null ? reward.command : "unknown"));
                    }
                }
            }
        }

        plugin.getLogger().info("Loaded cracker '" + id + "' with " + rewards.size() + " rewards");

        return new CrackerType(id, name, material, customModelData, lore, glow, rewards);
    }

    private CrackerReward loadRewardFromMap(Map<?, ?> map) {
        try {
            String type = (String) map.get("type");
            if (type == null) {
                plugin.getLogger().warning("Reward missing 'type' field");
                return null;
            }

            CrackerReward reward = new CrackerReward(type, 100.0);

            switch (type.toUpperCase()) {
                case "ITEM":
                    reward.item = (String) map.get("item");
                    Object amountObj = map.get("amount");
                    reward.amount = amountObj != null ? ((Number) amountObj).intValue() : 1;

                    if (reward.item == null) {
                        plugin.getLogger().warning("ITEM reward missing 'item' field");
                        return null;
                    }
                    break;

                case "COMMAND":
                    reward.command = (String) map.get("command");
                    if (reward.command == null) {
                        plugin.getLogger().warning("COMMAND reward missing 'command' field");
                        return null;
                    }
                    break;

                case "PERMISSION":
                    reward.permission = (String) map.get("permission");
                    Object durationObj = map.get("duration");
                    reward.duration = durationObj != null ? ((Number) durationObj).intValue() : -1;
                    break;
            }

            return reward;
        } catch (Exception e) {
            plugin.getLogger().warning("Error loading reward: " + e.getMessage());
            return null;
        }
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

        // Give ALL rewards (no probability - players always get everything)
        int itemCount = 0;
        for (CrackerReward reward : cracker.rewards) {
            // Skip permission rewards - only give items and commands
            if (reward.type.equalsIgnoreCase("PERMISSION")) {
                plugin.getLogger().info("Skipping permission reward in cracker (permissions not supported)");
                continue;
            }

            giveReward(player, reward);
            itemCount++;
        }

        plugin.getLogger().info("Player " + player.getName() + " opened " + type + " cracker and received " + itemCount + " rewards");
    }



    private void giveReward(Player player, CrackerReward reward) {
        Map<String, String> placeholders = new HashMap<>();

        switch (reward.type.toUpperCase()) {
            case "ITEM":
                try {
                    Material material = Material.valueOf(reward.item.toUpperCase());
                    ItemStack item = new ItemStack(material, reward.amount);
                    HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);

                    // Drop items that don't fit in inventory
                    if (!leftover.isEmpty()) {
                        for (ItemStack drop : leftover.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), drop);
                        }
                        plugin.getLogger().info("Dropped items on ground for player " + player.getName() + " (inventory full)");
                    }

                    placeholders.put("item", material.name());
                    placeholders.put("amount", String.valueOf(reward.amount));
                    plugin.getMessageManager().sendMessage(player, "crackers.received-item", placeholders);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in cracker reward: " + reward.item);
                }
                break;

            case "COMMAND":
                String command = reward.command.replace("{player}", player.getName());
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                plugin.getMessageManager().sendMessage(player, "crackers.received-command-reward");
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

