package fun.vynofc.christmasPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import fun.vynofc.christmasPlugin.ChristmasPlugin;

import java.util.*;

public class ChristmasCommand implements CommandExecutor, TabCompleter {

    private final ChristmasPlugin plugin;

    public ChristmasCommand(ChristmasPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            plugin.getMessageManager().sendMessage(sender, "usage.christmas");
            return true;
        }

        // /christmas snow
        if (args[0].equalsIgnoreCase("snow")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cThis command can only be used by players!");
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("christmasplus.christmas.snow")) {
                plugin.getMessageManager().sendMessage(player, "general.no-permission");
                return true;
            }

            if (!plugin.getSnowEffectManager().isGloballyEnabled()) {
                plugin.getMessageManager().sendMessage(player, "snow.not-enabled-globally");
                return true;
            }

            plugin.getSnowEffectManager().toggleSnow(player);

            if (plugin.getSnowEffectManager().isSnowEnabled(player)) {
                plugin.getMessageManager().sendMessage(player, "snow.enabled");
            } else {
                plugin.getMessageManager().sendMessage(player, "snow.disabled");
            }

            return true;
        }

        // /christmas santa
        if (args[0].equalsIgnoreCase("santa")) {
            if (!sender.hasPermission("christmasplus.christmas.santa")) {
                plugin.getMessageManager().sendMessage(sender, "general.no-permission");
                return true;
            }

            if (args.length < 2) {
                plugin.getMessageManager().sendMessage(sender, "usage.santa");
                return true;
            }

            // /christmas santa start
            if (args[1].equalsIgnoreCase("start")) {
                if (plugin.getSantaManager().isEventActive()) {
                    plugin.getMessageManager().sendMessage(sender, "santa.event-already-running");
                    return true;
                }

                // Get duration (default from config)
                int duration = plugin.getConfig().getInt("santa-claus.default-duration", 600);
                int argOffset = 2;

                // Check if time is specified
                if (args.length > argOffset && isTimeString(args[argOffset])) {
                    duration = parseTime(args[argOffset]);
                    argOffset++;
                }

                // /christmas santa start [time] random
                if (args.length > argOffset && args[argOffset].equalsIgnoreCase("random")) {
                    plugin.getSantaManager().startEventRandom(duration);
                    return true;
                }

                // /christmas santa start [time] [world] [x] [y] [z]
                if (args.length >= argOffset + 4) {
                    World world = Bukkit.getWorld(args[argOffset]);
                    if (world == null) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("world", args[argOffset]);
                        plugin.getMessageManager().sendMessage(sender, "santa.invalid-world", placeholders);
                        return true;
                    }

                    try {
                        double x = Double.parseDouble(args[argOffset + 1]);
                        double y = Double.parseDouble(args[argOffset + 2]);
                        double z = Double.parseDouble(args[argOffset + 3]);

                        Location location = new Location(world, x, y, z);
                        plugin.getSantaManager().startEvent(location, duration);
                    } catch (NumberFormatException e) {
                        plugin.getMessageManager().sendMessage(sender, "santa.invalid-duration");
                    }

                    return true;
                }

                // If player, use their location
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    plugin.getSantaManager().startEvent(player.getLocation(), duration);
                } else {
                    sender.sendMessage("§cYou must specify coordinates when using from console!");
                    plugin.getMessageManager().sendMessage(sender, "usage.santa");
                }

                return true;
            }

            // /christmas santa stop
            if (args[1].equalsIgnoreCase("stop")) {
                if (!plugin.getSantaManager().isEventActive()) {
                    plugin.getMessageManager().sendMessage(sender, "santa.event-not-running");
                    return true;
                }

                plugin.getSantaManager().stopEvent();
                return true;
            }

            // /christmas santa teleport
            if (args[1].equalsIgnoreCase("teleport")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cThis command can only be used by players!");
                    return true;
                }

                Player player = (Player) sender;

                if (!plugin.getSantaManager().isEventActive()) {
                    plugin.getMessageManager().sendMessage(player, "santa.event-not-running");
                    return true;
                }

                Location santaLoc = plugin.getSantaManager().getSantaLocation();
                if (santaLoc != null) {
                    player.teleport(santaLoc);
                    plugin.getMessageManager().sendMessage(player, "santa.teleported-to-santa");
                }

                return true;
            }

            plugin.getMessageManager().sendMessage(sender, "usage.santa");
            return true;
        }

        // /christmas reload
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("christmasplus.christmas.reload")) {
                plugin.getMessageManager().sendMessage(sender, "general.no-permission");
                return true;
            }

            plugin.reload();
            plugin.getMessageManager().sendMessage(sender, "general.config-reloaded");
            return true;
        }

        plugin.getMessageManager().sendMessage(sender, "usage.christmas");
        return true;
    }

    private boolean isTimeString(String str) {
        return str.matches("\\d+[smh]");
    }

    private int parseTime(String timeStr) {
        try {
            char unit = timeStr.charAt(timeStr.length() - 1);
            int value = Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));

            switch (unit) {
                case 's':
                    return value;
                case 'm':
                    return value * 60;
                case 'h':
                    return value * 3600;
                default:
                    return 600;
            }
        } catch (Exception e) {
            return 600;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("christmasplus.christmas.snow")) {
                completions.add("snow");
            }
            if (sender.hasPermission("christmasplus.christmas.santa")) {
                completions.add("santa");
            }
            if (sender.hasPermission("christmasplus.christmas.reload")) {
                completions.add("reload");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("santa")) {
            completions.add("start");
            completions.add("stop");
            completions.add("teleport");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("santa") && args[1].equalsIgnoreCase("start")) {
            completions.add("5m");
            completions.add("10m");
            completions.add("30m");
            completions.add("1h");
            completions.add("random");
            for (World world : Bukkit.getWorlds()) {
                completions.add(world.getName());
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("santa") && args[1].equalsIgnoreCase("start")) {
            if (isTimeString(args[2])) {
                completions.add("random");
                for (World world : Bukkit.getWorlds()) {
                    completions.add(world.getName());
                }
            }
        }

        return completions;
    }
}

