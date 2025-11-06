package xyz.niliees.christmasPlugin.managers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.niliees.christmasPlugin.ChristmasPlugin;
import xyz.niliees.christmasPlugin.models.Gift;
import xyz.niliees.christmasPlugin.models.GiftType;

import java.util.*;
import java.util.stream.Collectors;

public class GiftManager {

    private final ChristmasPlugin plugin;
    private FileConfiguration giftsConfig;
    private final Map<Integer, Map<String, List<Gift>>> dayGifts = new HashMap<>();

    public GiftManager(ChristmasPlugin plugin) {
        this.plugin = plugin;
        this.giftsConfig = plugin.getConfigManager().getGiftsConfig();
        loadGifts();
    }

    public void reload() {
        plugin.getConfigManager().reloadConfig("gifts");
        this.giftsConfig = plugin.getConfigManager().getGiftsConfig();
        dayGifts.clear();
        loadGifts();
    }

    private void loadGifts() {
        for (int day = 1; day <= 25; day++) {
            String dayKey = "day-" + day;
            if (giftsConfig.contains(dayKey)) {
                Map<String, List<Gift>> groupGifts = new HashMap<>();
                ConfigurationSection daySection = giftsConfig.getConfigurationSection(dayKey + ".groups");

                if (daySection != null) {
                    for (String group : daySection.getKeys(false)) {
                        List<Gift> gifts = new ArrayList<>();
                        ConfigurationSection itemsSection = daySection.getConfigurationSection(group + ".items");

                        if (itemsSection != null) {
                            for (String itemKey : itemsSection.getKeys(false)) {
                                ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemKey);
                                if (itemSection != null) {
                                    Gift gift = parseGift(itemSection);
                                    if (gift != null) {
                                        gifts.add(gift);
                                    }
                                }
                            }
                        }

                        groupGifts.put(group, gifts);
                    }
                }

                dayGifts.put(day, groupGifts);
            }
        }

        plugin.getLogger().info("Loaded gifts for " + dayGifts.size() + " days");
    }

    private Gift parseGift(ConfigurationSection section) {
        try {
            String typeStr = section.getString("type", "ITEM");
            GiftType type = GiftType.valueOf(typeStr.toUpperCase());

            Gift gift = new Gift(type);

            switch (type) {
                case ITEM:
                    String material = section.getString("material", "STONE");
                    int amount = section.getInt("amount", 1);
                    String name = section.getString("name");
                    List<String> lore = section.getStringList("lore");
                    List<String> enchantments = section.getStringList("enchantments");
                    List<String> storedEnchantments = section.getStringList("stored-enchantments");

                    ItemStack item = createItem(material, amount, name, lore, enchantments, storedEnchantments);
                    gift.setItemStack(item);
                    break;

                case COMMAND:
                    String command = section.getString("command");
                    gift.setCommand(command);
                    break;

                case PERMISSION:
                    String permission = section.getString("permission");
                    int duration = section.getInt("duration", -1);
                    gift.setPermission(permission);
                    gift.setDuration(duration);
                    break;
            }

            return gift;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse gift: " + e.getMessage());
            return null;
        }
    }

    private ItemStack createItem(String materialStr, int amount, String name, List<String> lore,
                                  List<String> enchantments, List<String> storedEnchantments) {
        Material material;
        try {
            material = Material.valueOf(materialStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material: " + materialStr);
            material = Material.STONE;
        }

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (name != null && !name.isEmpty()) {
                meta.setDisplayName(plugin.getMessageManager().colorize(name));
            }

            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore.stream()
                        .map(plugin.getMessageManager()::colorize)
                        .collect(Collectors.toList()));
            }

            if (enchantments != null && !enchantments.isEmpty()) {
                for (String enchStr : enchantments) {
                    String[] parts = enchStr.split(":");
                    if (parts.length == 2) {
                        try {
                            Enchantment ench = Enchantment.getByName(parts[0].toUpperCase());
                            int level = Integer.parseInt(parts[1]);
                            if (ench != null) {
                                meta.addEnchant(ench, level, true);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Invalid enchantment: " + enchStr);
                        }
                    }
                }
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public List<Gift> getGiftsForDay(int day, Player player) {
        Map<String, List<Gift>> groupGifts = dayGifts.get(day);
        if (groupGifts == null) {
            return new ArrayList<>();
        }

        // Find the highest priority group the player belongs to
        String playerGroup = getPlayerGroup(player);

        return groupGifts.getOrDefault(playerGroup, groupGifts.getOrDefault("default", new ArrayList<>()));
    }

    private String getPlayerGroup(Player player) {
        // Check groups in priority order (from config.yml)
        ConfigurationSection groupsSection = plugin.getConfig().getConfigurationSection("gift-groups");
        if (groupsSection == null) {
            return "default";
        }

        // Create a list of groups sorted by priority (descending)
        List<Map.Entry<String, Integer>> groups = new ArrayList<>();
        for (String group : groupsSection.getKeys(false)) {
            int priority = groupsSection.getInt(group + ".priority", 0);
            String permission = groupsSection.getString(group + ".permission");

            if (permission == null || permission.isEmpty() || player.hasPermission(permission)) {
                groups.add(new AbstractMap.SimpleEntry<>(group, priority));
            }
        }

        groups.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        return groups.isEmpty() ? "default" : groups.get(0).getKey();
    }

    public void giveGifts(Player player, List<Gift> gifts) {
        MessageManager messageManager = plugin.getMessageManager();

        for (Gift gift : gifts) {
            switch (gift.getType()) {
                case ITEM:
                    if (gift.getItemStack() != null) {
                        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(gift.getItemStack());
                        if (!leftover.isEmpty()) {
                            for (ItemStack item : leftover.values()) {
                                player.getWorld().dropItemNaturally(player.getLocation(), item);
                            }
                        }
                    }
                    break;

                case COMMAND:
                    if (gift.getCommand() != null) {
                        String command = gift.getCommand().replace("{player}", player.getName());
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                    }
                    break;

                case PERMISSION:
                    if (gift.getPermission() != null && plugin.isVaultEnabled() && plugin.getPermission() != null) {
                        // If duration is -1, it's permanent
                        // Otherwise, you'd need a timed permissions plugin or custom implementation
                        plugin.getPermission().playerAdd(player, gift.getPermission());
                    }
                    break;
            }
        }
    }

    public boolean hasGiftsForDay(int day) {
        return dayGifts.containsKey(day);
    }
}

