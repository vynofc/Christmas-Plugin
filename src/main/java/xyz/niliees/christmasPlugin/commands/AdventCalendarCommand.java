package xyz.niliees.christmasPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.niliees.christmasPlugin.ChristmasPlugin;

import java.util.*;

public class AdventCalendarCommand implements CommandExecutor, TabCompleter {

    private final ChristmasPlugin plugin;

    public AdventCalendarCommand(ChristmasPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // /adventcalendar - open GUI
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cThis command can only be used by players!");
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("christmasplus.adventcalendar")) {
                plugin.getMessageManager().sendMessage(player, "general.no-permission");
                return true;
            }

            plugin.getAdventCalendarManager().openCalendar(player);
            return true;
        }

        // /adventcalendar reset <player> <day|all>
        if (args[0].equalsIgnoreCase("reset")) {
            if (!sender.hasPermission("christmasplus.adventcalendar.reset")) {
                plugin.getMessageManager().sendMessage(sender, "general.no-permission");
                return true;
            }

            if (args.length < 3) {
                plugin.getMessageManager().sendMessage(sender, "usage.advent-calendar");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", args[1]);
                plugin.getMessageManager().sendMessage(sender, "general.player-not-found", placeholders);
                return true;
            }

            if (args[2].equalsIgnoreCase("all")) {
                plugin.getAdventCalendarManager().resetAll(target);
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", target.getName());
                plugin.getMessageManager().sendMessage(sender, "advent-calendar.reset-all-success", placeholders);
            } else {
                try {
                    int day = Integer.parseInt(args[2]);
                    if (day < 1 || day > 25) {
                        sender.sendMessage("§cDay must be between 1 and 25!");
                        return true;
                    }

                    plugin.getAdventCalendarManager().resetDay(target, day);
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", target.getName());
                    placeholders.put("day", String.valueOf(day));
                    plugin.getMessageManager().sendMessage(sender, "advent-calendar.reset-success", placeholders);
                } catch (NumberFormatException e) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("input", args[2]);
                    plugin.getMessageManager().sendMessage(sender, "general.invalid-number", placeholders);
                }
            }

            return true;
        }

        // /adventcalendar test
        if (args[0].equalsIgnoreCase("test")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cThis command can only be used by players!");
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("christmasplus.adventcalendar.test")) {
                plugin.getMessageManager().sendMessage(player, "general.no-permission");
                return true;
            }

            plugin.getAdventCalendarManager().toggleTestMode(player);

            if (plugin.getAdventCalendarManager().isTestMode(player)) {
                plugin.getMessageManager().sendMessage(player, "advent-calendar.test-mode-enabled");
            } else {
                plugin.getMessageManager().sendMessage(player, "advent-calendar.test-mode-disabled");
            }

            return true;
        }

        plugin.getMessageManager().sendMessage(sender, "usage.advent-calendar");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("christmasplus.adventcalendar.reset")) {
                completions.add("reset");
            }
            if (sender.hasPermission("christmasplus.adventcalendar.test")) {
                completions.add("test");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("reset")) {
            completions.add("all");
            for (int i = 1; i <= 25; i++) {
                completions.add(String.valueOf(i));
            }
        }

        return completions;
    }
}

