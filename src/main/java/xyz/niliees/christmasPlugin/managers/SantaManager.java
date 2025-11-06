package xyz.niliees.christmasPlugin.managers;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import xyz.niliees.christmasPlugin.ChristmasPlugin;

import java.util.*;

public class SantaManager {

    private final ChristmasPlugin plugin;
    private LivingEntity santaEntity;
    private Location santaLocation;
    private boolean eventActive = false;
    private BukkitTask eventTask;
    private BukkitTask giftDropTask;
    private BukkitTask snowTrailTask;
    private long eventEndTime;

    public SantaManager(ChristmasPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isEventActive() {
        return eventActive;
    }

    public Location getSantaLocation() {
        return santaEntity != null ? santaEntity.getLocation() : santaLocation;
    }

    public void startEvent(Location location, int duration) {
        if (eventActive) {
            return;
        }

        this.santaLocation = location;
        this.eventActive = true;
        this.eventEndTime = System.currentTimeMillis() + (duration * 1000L);

        // Spawn Santa
        spawnSanta(location);

        // Start event timer
        startEventTimer(duration);

        // Start gift drop task
        if (plugin.getConfig().getBoolean("santa-claus.gift-drop.enabled", true)) {
            startGiftDropTask();
        }

        // Start snow trail
        if (plugin.getConfig().getBoolean("santa-claus.snow-trail.enabled", true)) {
            startSnowTrail();
        }

        // Broadcast start message
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("world", location.getWorld().getName());
        placeholders.put("x", String.valueOf(location.getBlockX()));
        placeholders.put("y", String.valueOf(location.getBlockY()));
        placeholders.put("z", String.valueOf(location.getBlockZ()));
        placeholders.put("time", formatTime(duration));

        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getMessageManager().sendMessage(player, "santa.event-started", placeholders);
        }
    }

    public void startEventRandom(int duration) {
        List<Location> locations = getSpawnLocations();
        if (locations.isEmpty()) {
            plugin.getLogger().warning("No spawn locations configured!");
            return;
        }

        Random random = new Random();
        Location location = locations.get(random.nextInt(locations.size()));

        startEvent(location, duration);

        // Broadcast random start message
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getMessageManager().sendMessage(player, "santa.event-started-random");
        }
    }

    public void stopEvent() {
        if (!eventActive) {
            return;
        }

        eventActive = false;

        // Cancel tasks
        if (eventTask != null) {
            eventTask.cancel();
            eventTask = null;
        }

        if (giftDropTask != null) {
            giftDropTask.cancel();
            giftDropTask = null;
        }

        if (snowTrailTask != null) {
            snowTrailTask.cancel();
            snowTrailTask = null;
        }

        // Remove Santa entity
        if (santaEntity != null && !santaEntity.isDead()) {
            santaEntity.remove();
        }
        santaEntity = null;

        // Broadcast stop message
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getMessageManager().sendMessage(player, "santa.event-stopped");
        }
    }

    private void spawnSanta(Location location) {
        String entityTypeStr = plugin.getConfig().getString("santa-claus.entity-type", "VILLAGER");
        EntityType entityType;

        try {
            entityType = EntityType.valueOf(entityTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            entityType = EntityType.VILLAGER;
        }

        Entity entity = location.getWorld().spawnEntity(location, entityType);

        if (entity instanceof LivingEntity) {
            santaEntity = (LivingEntity) entity;

            // Set custom name
            String name = plugin.getMessageManager().colorize(
                    plugin.getConfig().getString("santa-claus.name", "&c&lSanta Claus"));
            santaEntity.setCustomName(name);
            santaEntity.setCustomNameVisible(plugin.getConfig().getBoolean("santa-claus.show-name-tag", true));

            // Make it persistent
            santaEntity.setRemoveWhenFarAway(false);

            // Configure as villager if applicable
            if (santaEntity instanceof Villager) {
                Villager villager = (Villager) santaEntity;
                villager.setProfession(Villager.Profession.NONE);
                villager.setVillagerType(Villager.Type.SNOW);
            }

            // Set AI
            santaEntity.setAI(true);
            santaEntity.setCollidable(false);
        }
    }

    private void startEventTimer(int duration) {
        eventTask = new BukkitRunnable() {
            @Override
            public void run() {
                stopEvent();
            }
        }.runTaskLater(plugin, duration * 20L);
    }

    private void startGiftDropTask() {
        int interval = plugin.getConfig().getInt("santa-claus.gift-drop.interval", 5);
        double chance = plugin.getConfig().getDouble("santa-claus.gift-drop.chance", 15.0);

        giftDropTask = new BukkitRunnable() {
            final Random random = new Random();

            @Override
            public void run() {
                if (santaEntity == null || santaEntity.isDead()) {
                    cancel();
                    return;
                }

                if (random.nextDouble() * 100 < chance) {
                    dropGift(santaEntity.getLocation());
                }
            }
        }.runTaskTimer(plugin, interval * 20L, interval * 20L);
    }

    private void startSnowTrail() {
        int particleAmount = plugin.getConfig().getInt("santa-claus.snow-trail.particle-amount", 10);
        int fadeDelay = plugin.getConfig().getInt("santa-claus.snow-trail.fade-delay", 100);

        snowTrailTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (santaEntity == null || santaEntity.isDead()) {
                    cancel();
                    return;
                }

                Location loc = santaEntity.getLocation();
                loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, particleAmount, 0.5, 0.5, 0.5, 0.01);

                // Place temporary snow
                Location groundLoc = loc.clone().subtract(0, 1, 0);
                if (groundLoc.getBlock().getType().isSolid()) {
                    Location snowLoc = groundLoc.clone().add(0, 1, 0);
                    if (snowLoc.getBlock().getType() == Material.AIR) {
                        snowLoc.getBlock().setType(Material.SNOW);

                        // Remove snow after delay
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (snowLoc.getBlock().getType() == Material.SNOW) {
                                    snowLoc.getBlock().setType(Material.AIR);
                                }
                            }
                        }.runTaskLater(plugin, fadeDelay);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void dropGift(Location location) {
        // Drop a random item as a gift
        Material[] gifts = {
            Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT,
            Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE,
            Material.COOKIE, Material.CAKE, Material.PUMPKIN_PIE
        };

        Random random = new Random();
        Material gift = gifts[random.nextInt(gifts.length)];

        location.getWorld().dropItemNaturally(location, new org.bukkit.inventory.ItemStack(gift));
        location.getWorld().spawnParticle(Particle.HEART, location, 10, 0.5, 0.5, 0.5, 0.1);
        location.getWorld().playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
    }

    private List<Location> getSpawnLocations() {
        List<Location> locations = new ArrayList<>();

        if (plugin.getConfig().contains("santa-claus.spawn-locations")) {
            for (String key : plugin.getConfig().getConfigurationSection("santa-claus.spawn-locations").getKeys(false)) {
                String path = "santa-claus.spawn-locations." + key;
                String worldName = plugin.getConfig().getString(path + ".world");
                World world = Bukkit.getWorld(worldName);

                if (world != null) {
                    double x = plugin.getConfig().getDouble(path + ".x");
                    double y = plugin.getConfig().getDouble(path + ".y");
                    double z = plugin.getConfig().getDouble(path + ".z");

                    locations.add(new Location(world, x, y, z));
                }
            }
        }

        return locations;
    }

    private String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m";
        } else {
            return (seconds / 3600) + "h";
        }
    }

    public long getTimeRemaining() {
        if (!eventActive) {
            return 0;
        }
        return Math.max(0, (eventEndTime - System.currentTimeMillis()) / 1000);
    }
}

