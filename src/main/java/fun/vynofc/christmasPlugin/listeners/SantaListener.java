package fun.vynofc.christmasPlugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import fun.vynofc.christmasPlugin.ChristmasPlugin;

public class SantaListener implements Listener {

    private final ChristmasPlugin plugin;

    public SantaListener(ChristmasPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getSantaManager().isEventActive()) {
            return;
        }

        // Check if the damaged entity is Santa
        if (event.getEntity().equals(plugin.getSantaManager().getSantaLocation().getWorld()
                .getNearbyEntities(plugin.getSantaManager().getSantaLocation(), 1, 1, 1)
                .stream()
                .findFirst()
                .orElse(null))) {
            // Prevent damage to Santa
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!plugin.getSantaManager().isEventActive()) {
            return;
        }

        // Prevent Santa from dying
        // Note: This is a basic implementation. In a production plugin,
        // you'd want to store the Santa entity's UUID and check against it.
    }
}

