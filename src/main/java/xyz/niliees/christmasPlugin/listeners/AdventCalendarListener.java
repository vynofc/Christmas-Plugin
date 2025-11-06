package xyz.niliees.christmasPlugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import xyz.niliees.christmasPlugin.ChristmasPlugin;

public class AdventCalendarListener implements Listener {

    private final ChristmasPlugin plugin;

    public AdventCalendarListener(ChristmasPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        String calendarTitle = plugin.getMessageManager().colorize(
                plugin.getConfig().getString("advent-calendar.gui.title", "&c&lAdvent Calendar"));

        if (!title.equals(calendarTitle)) {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
        }

        String displayName = clickedItem.getItemMeta().getDisplayName();

        // Parse day number from display name
        // Format: "&a&lDay 1", "&e&lDay 2", etc.
        String strippedName = stripColor(displayName);
        if (strippedName.startsWith("Day ")) {
            try {
                String dayStr = strippedName.substring(4);
                int day = Integer.parseInt(dayStr);

                // Check if player can claim this day
                if (!plugin.getAdventCalendarManager().canClaimDay(player, day)) {
                    if (plugin.getAdventCalendarManager().hasClaimed(player, day)) {
                        plugin.getMessageManager().sendMessage(player, "advent-calendar.already-claimed");
                    } else {
                        java.util.Map<String, String> placeholders = new java.util.HashMap<>();
                        placeholders.put("day", String.valueOf(day));
                        plugin.getMessageManager().sendMessage(player, "advent-calendar.not-available-yet", placeholders);
                    }
                    return;
                }

                // Claim the day
                plugin.getAdventCalendarManager().claimDay(player, day);

                // Refresh the GUI
                player.closeInventory();
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    plugin.getAdventCalendarManager().openCalendar(player);
                }, 5L);

            } catch (NumberFormatException e) {
                // Not a valid day item
            }
        }
    }

    private String stripColor(String str) {
        return str.replaceAll("§[0-9a-fk-or]", "");
    }
}

