package fun.vynofc.christmasPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import fun.vynofc.christmasPlugin.ChristmasPlugin;
import fun.vynofc.christmasPlugin.models.Gift;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

public class AdventCalendarManager {

    private final ChristmasPlugin plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;
    private final Map<UUID, Set<Integer>> claimedDays = new HashMap<>();
    private final Set<UUID> testModePlayers = new HashSet<>();

    public AdventCalendarManager(ChristmasPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "calendar-data.yml");
        loadData();
    }

    public void reload() {
        loadData();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create calendar-data.yml!");
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        claimedDays.clear();

        for (String key : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                List<Integer> days = dataConfig.getIntegerList(key);
                claimedDays.put(uuid, new HashSet<>(days));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in calendar-data.yml: " + key);
            }
        }
    }

    public void saveData() {
        for (Map.Entry<UUID, Set<Integer>> entry : claimedDays.entrySet()) {
            dataConfig.set(entry.getKey().toString(), new ArrayList<>(entry.getValue()));
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save calendar-data.yml: " + e.getMessage());
        }
    }

    public boolean hasClaimed(Player player, int day) {
        return claimedDays.getOrDefault(player.getUniqueId(), new HashSet<>()).contains(day);
    }

    public void setClaimed(Player player, int day) {
        claimedDays.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(day);
        saveData();
    }

    public void resetDay(Player player, int day) {
        Set<Integer> days = claimedDays.get(player.getUniqueId());
        if (days != null) {
            days.remove(day);
            saveData();
        }
    }

    public void resetAll(Player player) {
        claimedDays.remove(player.getUniqueId());
        saveData();
    }

    public boolean isTestMode(Player player) {
        return testModePlayers.contains(player.getUniqueId());
    }

    public void toggleTestMode(Player player) {
        if (testModePlayers.contains(player.getUniqueId())) {
            testModePlayers.remove(player.getUniqueId());
        } else {
            testModePlayers.add(player.getUniqueId());
        }
    }

    public boolean canClaimDay(Player player, int day) {
        // Test mode allows claiming all days
        if (isTestMode(player)) {
            return true;
        }

        // Check if already claimed
        if (hasClaimed(player, day)) {
            return false;
        }

        // Check if in calendar period
        LocalDate now = LocalDate.now();
        if (now.getMonth() != Month.DECEMBER) {
            return false;
        }

        int currentDay = now.getDayOfMonth();

        // Check if previous days are allowed
        boolean allowPreviousDays = plugin.getConfig().getBoolean("advent-calendar.allow-previous-days", true);

        if (allowPreviousDays) {
            return day <= currentDay;
        } else {
            return day == currentDay;
        }
    }

    public void openCalendar(Player player) {
        int size = plugin.getConfig().getInt("advent-calendar.gui.size", 54);
        String title = plugin.getMessageManager().colorize(
                plugin.getConfig().getString("advent-calendar.gui.title", "&c&lAdvent Calendar"));

        Inventory inv = Bukkit.createInventory(null, size, title);

        // Fill empty slots
        if (plugin.getConfig().getBoolean("advent-calendar.gui.fill-empty-slots", true)) {
            String fillMaterial = plugin.getConfig().getString("advent-calendar.gui.fill-material", "RED_STAINED_GLASS_PANE");
            ItemStack filler = createFillerItem(fillMaterial);
            for (int i = 0; i < size; i++) {
                inv.setItem(i, filler);
            }
        }

        // Add day items
        LocalDate now = LocalDate.now();
        int currentDay = now.getMonth() == Month.DECEMBER ? now.getDayOfMonth() : 0;

        for (int day = 1; day <= 25; day++) {
            int slot = getDaySlot(day);
            if (slot >= 0 && slot < size) {
                ItemStack dayItem = createDayItem(player, day, currentDay);
                inv.setItem(slot, dayItem);
            }
        }

        player.openInventory(inv);
        plugin.getMessageManager().sendMessage(player, "advent-calendar.gui-opened");
    }

    private int getDaySlot(int day) {
        // Distribute days evenly in the inventory
        // Days 1-25 will be placed starting from slot 10
        int[] slots = {
            10, 11, 12, 13, 14, 15, 16, // Days 1-7
            19, 20, 21, 22, 23, 24, 25, // Days 8-14
            28, 29, 30, 31, 32, 33, 34, // Days 15-21
            37, 38, 39, 40 // Days 22-25
        };

        if (day >= 1 && day <= slots.length) {
            return slots[day - 1];
        }
        return -1;
    }

    private ItemStack createDayItem(Player player, int day, int currentDay) {
        boolean claimed = hasClaimed(player, day);
        boolean canClaim = canClaimDay(player, day);

        Material material;
        String statusColor;
        String statusText;

        if (claimed) {
            material = Material.valueOf(plugin.getConfig().getString("advent-calendar.gui.day-item.claimed", "YELLOW_WOOL"));
            statusColor = "&e";
            statusText = "&7Status: &eClaimed";
        } else if (canClaim) {
            material = Material.valueOf(plugin.getConfig().getString("advent-calendar.gui.day-item.available", "GREEN_WOOL"));
            statusColor = "&a";
            statusText = "&7Status: &aAvailable";
        } else {
            material = Material.valueOf(plugin.getConfig().getString("advent-calendar.gui.day-item.locked", "RED_WOOL"));
            statusColor = "&c";
            statusText = "&7Status: &cLocked";
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(plugin.getMessageManager().colorize(statusColor + "&lDay " + day));

            List<String> lore = new ArrayList<>();
            lore.add(plugin.getMessageManager().colorize(statusText));
            lore.add("");

            if (claimed) {
                lore.add(plugin.getMessageManager().colorize("&7You already claimed this day!"));
            } else if (canClaim) {
                lore.add(plugin.getMessageManager().colorize("&aClick to claim your gifts!"));
            } else {
                lore.add(plugin.getMessageManager().colorize("&cCome back on December " + day + "!"));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createFillerItem(String materialStr) {
        Material material;
        try {
            material = Material.valueOf(materialStr);
        } catch (IllegalArgumentException e) {
            material = Material.RED_STAINED_GLASS_PANE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }

        return item;
    }

    public void claimDay(Player player, int day) {
        plugin.getLogger().info("Player " + player.getName() + " attempting to claim day " + day);

        if (!canClaimDay(player, day)) {
            plugin.getLogger().info("Player cannot claim day " + day + " (already claimed or not available)");
            return;
        }

        // Get gifts for this day
        List<Gift> gifts = plugin.getGiftManager().getGiftsForDay(day, player);
        plugin.getLogger().info("Found " + gifts.size() + " gifts for day " + day);

        if (gifts.isEmpty()) {
            plugin.getLogger().warning("No gifts configured for day " + day);
            return;
        }

        // Give gifts to player
        plugin.getGiftManager().giveGifts(player, gifts);

        // Mark as claimed
        setClaimed(player, day);

        // Send success message
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("day", String.valueOf(day));
        placeholders.put("amount", String.valueOf(gifts.size()));
        plugin.getMessageManager().sendMessage(player, "advent-calendar.claimed-success", placeholders);
    }
}

