package xyz.niliees.christmasPlugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.niliees.christmasPlugin.commands.AdventCalendarCommand;
import xyz.niliees.christmasPlugin.commands.ChristmasCommand;
import xyz.niliees.christmasPlugin.commands.CrackerCommand;
import xyz.niliees.christmasPlugin.config.ConfigManager;
import xyz.niliees.christmasPlugin.listeners.AdventCalendarListener;
import xyz.niliees.christmasPlugin.listeners.CrackerListener;
import xyz.niliees.christmasPlugin.listeners.SantaListener;
import xyz.niliees.christmasPlugin.managers.AdventCalendarManager;
import xyz.niliees.christmasPlugin.managers.CrackerManager;
import xyz.niliees.christmasPlugin.managers.GiftManager;
import xyz.niliees.christmasPlugin.managers.MessageManager;
import xyz.niliees.christmasPlugin.managers.SantaManager;
import xyz.niliees.christmasPlugin.managers.SnowEffectManager;

public final class ChristmasPlugin extends JavaPlugin {

    private static ChristmasPlugin instance;

    // Managers
    private ConfigManager configManager;
    private MessageManager messageManager;
    private AdventCalendarManager adventCalendarManager;
    private SantaManager santaManager;
    private CrackerManager crackerManager;
    private SnowEffectManager snowEffectManager;
    private GiftManager giftManager;

    // Vault (stored as Object to avoid ClassNotFoundException if Vault is not installed)
    private Object vaultPermission;
    private boolean vaultEnabled = false;

    // WorldGuard
    private boolean worldGuardEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

        long startTime = System.currentTimeMillis();
        getLogger().info("╔════════════════════════════════════╗");
        getLogger().info("║   Christmas Plugin by niliees      ║");
        getLogger().info("║   Loading...                       ║");
        getLogger().info("╚════════════════════════════════════╝");

        // Load configuration
        initializeConfig();

        // Setup hooks
        setupVault();
        setupWorldGuard();

        // Initialize managers
        initializeManagers();

        // Register commands
        registerCommands();

        // Register listeners
        registerListeners();

        long loadTime = System.currentTimeMillis() - startTime;
        getLogger().info("╔════════════════════════════════════╗");
        getLogger().info("║   Christmas Plugin Enabled!        ║");
        getLogger().info("║   Loaded in " + loadTime + "ms" + " ".repeat(Math.max(0, 19 - String.valueOf(loadTime).length())) + "║");
        getLogger().info("╚════════════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        getLogger().info("╔════════════════════════════════════╗");
        getLogger().info("║   Disabling Christmas Plugin...    ║");
        getLogger().info("╚════════════════════════════════════╝");

        // Stop santa event if running
        if (santaManager != null && santaManager.isEventActive()) {
            santaManager.stopEvent();
        }

        // Stop snow effects
        if (snowEffectManager != null) {
            snowEffectManager.shutdown();
        }

        // Save all data
        if (adventCalendarManager != null) {
            adventCalendarManager.saveData();
        }

        getLogger().info("Christmas Plugin disabled successfully!");
    }

    private void initializeConfig() {
        // Save default configs
        saveDefaultConfig();
        saveResource("messages.yml", false);
        saveResource("gifts.yml", false);

        // Initialize config manager
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
    }

    private void setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            try {
                // Use reflection to avoid ClassNotFoundException when Vault is not present
                Class<?> permissionClass = Class.forName("net.milkbowl.vault.permission.Permission");
                Object rsp = getServer().getServicesManager().getRegistration(permissionClass);
                if (rsp != null) {
                    // Get the provider using reflection
                    vaultPermission = rsp.getClass().getMethod("getProvider").invoke(rsp);
                    vaultEnabled = true;
                    getLogger().info("✓ Vault hooked successfully!");
                } else {
                    getLogger().warning("✗ Vault found but no Permission provider registered!");
                }
            } catch (Exception e) {
                getLogger().warning("✗ Error hooking into Vault: " + e.getMessage());
                vaultEnabled = false;
            }
        } else {
            getLogger().info("✗ Vault not found - will work without it.");
        }
    }

    private void setupWorldGuard() {
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuardEnabled = true;
            getLogger().info("✓ WorldGuard hooked successfully!");
        } else {
            getLogger().info("✗ WorldGuard not found - will work without it.");
        }
    }

    private void initializeManagers() {
        messageManager = new MessageManager(this);
        giftManager = new GiftManager(this);
        adventCalendarManager = new AdventCalendarManager(this);
        santaManager = new SantaManager(this);
        crackerManager = new CrackerManager(this);
        snowEffectManager = new SnowEffectManager(this);

        getLogger().info("✓ All managers initialized!");
    }

    private void registerCommands() {
        // Paper plugins require programmatic command registration
        try {
            // Advent Calendar command
            AdventCalendarCommand adventCommand = new AdventCalendarCommand(this);
            org.bukkit.command.Command adventCalendarCmd = new org.bukkit.command.Command("adventcalendar",
                    "Open the advent calendar GUI",
                    "/adventcalendar [reset <player> <day|all> | test]",
                    java.util.List.of("ac", "calendar", "advent")) {
                @Override
                public boolean execute(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
                    return adventCommand.onCommand(sender, this, commandLabel, args);
                }

                @Override
                public java.util.List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                    return adventCommand.onTabComplete(sender, this, alias, args);
                }
            };
            adventCalendarCmd.setPermission("christmasplus.adventcalendar");
            getServer().getCommandMap().register("christmasplus", adventCalendarCmd);

            // Christmas main command
            ChristmasCommand christmasCommand = new ChristmasCommand(this);
            org.bukkit.command.Command christmasCmd = new org.bukkit.command.Command("christmas",
                    "Main christmas plugin command",
                    "/christmas <snow|santa|reload>",
                    java.util.List.of("xmas")) {
                @Override
                public boolean execute(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
                    return christmasCommand.onCommand(sender, this, commandLabel, args);
                }

                @Override
                public java.util.List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                    return christmasCommand.onTabComplete(sender, this, alias, args);
                }
            };
            christmasCmd.setPermission("christmasplus.christmas");
            getServer().getCommandMap().register("christmasplus", christmasCmd);

            // Cracker command
            CrackerCommand crackerCommand = new CrackerCommand(this);
            org.bukkit.command.Command crackerCmd = new org.bukkit.command.Command("cracker",
                    "Christmas cracker commands",
                    "/cracker <list|give|get>",
                    java.util.List.of("crackers")) {
                @Override
                public boolean execute(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
                    return crackerCommand.onCommand(sender, this, commandLabel, args);
                }

                @Override
                public java.util.List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                    return crackerCommand.onTabComplete(sender, this, alias, args);
                }
            };
            crackerCmd.setPermission("christmasplus.cracker");
            getServer().getCommandMap().register("christmasplus", crackerCmd);

            getLogger().info("✓ Commands registered!");
        } catch (Exception e) {
            getLogger().severe("✗ Failed to register commands: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new AdventCalendarListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CrackerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SantaListener(this), this);

        getLogger().info("✓ Listeners registered!");
    }

    public void reload() {
        // Reload configurations
        reloadConfig();
        configManager.loadConfigs();
        messageManager.reload();
        giftManager.reload();

        // Reload managers
        adventCalendarManager.reload();
        crackerManager.reload();
        snowEffectManager.reload();

        getLogger().info("Configuration reloaded successfully!");
    }

    // Getters
    public static ChristmasPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public AdventCalendarManager getAdventCalendarManager() {
        return adventCalendarManager;
    }

    public SantaManager getSantaManager() {
        return santaManager;
    }

    public CrackerManager getCrackerManager() {
        return crackerManager;
    }

    public SnowEffectManager getSnowEffectManager() {
        return snowEffectManager;
    }

    public GiftManager getGiftManager() {
        return giftManager;
    }

    public Object getVaultPermission() {
        return vaultPermission;
    }

    public boolean isVaultEnabled() {
        return vaultEnabled;
    }

    public boolean isWorldGuardEnabled() {
        return worldGuardEnabled;
    }
}
