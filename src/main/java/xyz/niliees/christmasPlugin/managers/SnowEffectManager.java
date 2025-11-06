package xyz.niliees.christmasPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import xyz.niliees.christmasPlugin.ChristmasPlugin;

import java.util.*;

public class SnowEffectManager {

    private final ChristmasPlugin plugin;
    private final Map<UUID, Integer> playerSnowLevels = new HashMap<>();
    private final Set<UUID> enabledPlayers = new HashSet<>();
    private BukkitTask snowTask;

    public SnowEffectManager(ChristmasPlugin plugin) {
        this.plugin = plugin;
        startSnowTask();
    }

    public void reload() {
        stopSnowTask();
        startSnowTask();
    }

    public void shutdown() {
        stopSnowTask();
    }

    private void startSnowTask() {
        if (!plugin.getConfig().getBoolean("snow-effect.enabled", true)) {
            return;
        }

        snowTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (isSnowEnabled(player)) {
                        showSnowEffect(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void stopSnowTask() {
        if (snowTask != null) {
            snowTask.cancel();
            snowTask = null;
        }
    }

    public boolean isSnowEnabled(Player player) {
        return enabledPlayers.contains(player.getUniqueId());
    }

    public void toggleSnow(Player player) {
        UUID uuid = player.getUniqueId();
        if (enabledPlayers.contains(uuid)) {
            enabledPlayers.remove(uuid);
            playerSnowLevels.remove(uuid);
        } else {
            enabledPlayers.add(uuid);
            int defaultLevel = plugin.getConfig().getInt("snow-effect.default-intensity", 3);
            playerSnowLevels.put(uuid, defaultLevel);
        }
    }

    public void setSnowLevel(Player player, int level) {
        if (level < 1 || level > 5) {
            return;
        }
        playerSnowLevels.put(player.getUniqueId(), level);
        if (!enabledPlayers.contains(player.getUniqueId())) {
            enabledPlayers.add(player.getUniqueId());
        }
    }

    public int getSnowLevel(Player player) {
        return playerSnowLevels.getOrDefault(player.getUniqueId(),
                plugin.getConfig().getInt("snow-effect.default-intensity", 3));
    }

    private void showSnowEffect(Player player) {
        // Check if world is blacklisted
        List<String> blacklistedWorlds = plugin.getConfig().getStringList("snow-effect.disabled-worlds");
        if (blacklistedWorlds.contains(player.getWorld().getName())) {
            return;
        }

        int level = getSnowLevel(player);
        String levelPath = "snow-effect.levels." + level;

        int particlesPerTick = plugin.getConfig().getInt(levelPath + ".particles-per-tick", 3);
        int radius = plugin.getConfig().getInt(levelPath + ".radius", 15);

        Location playerLoc = player.getLocation();
        World world = player.getWorld();
        Random random = new Random();

        for (int i = 0; i < particlesPerTick; i++) {
            // Random position around player
            double offsetX = (random.nextDouble() - 0.5) * radius * 2;
            double offsetZ = (random.nextDouble() - 0.5) * radius * 2;
            double offsetY = random.nextDouble() * 10 + 5;

            Location snowLoc = playerLoc.clone().add(offsetX, offsetY, offsetZ);

            // Spawn snowflake particle
            player.spawnParticle(Particle.SNOWFLAKE, snowLoc, 1, 0, -0.5, 0, 0.02);
        }
    }

    public boolean isGloballyEnabled() {
        return plugin.getConfig().getBoolean("snow-effect.enabled", true);
    }
}

