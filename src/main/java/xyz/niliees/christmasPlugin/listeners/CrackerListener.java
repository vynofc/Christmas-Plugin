package xyz.niliees.christmasPlugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import xyz.niliees.christmasPlugin.ChristmasPlugin;

public class CrackerListener implements Listener {

    private final ChristmasPlugin plugin;

    public CrackerListener(ChristmasPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) {
            return;
        }

        // Check if the item is a cracker
        if (!plugin.getCrackerManager().isCracker(item)) {
            return;
        }

        event.setCancelled(true);

        // Get cracker type
        String crackerType = plugin.getCrackerManager().getCrackerType(item);
        if (crackerType == null) {
            return;
        }

        // Remove one cracker from player's hand
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        // Open the cracker
        plugin.getCrackerManager().openCracker(player, crackerType);
    }
}

