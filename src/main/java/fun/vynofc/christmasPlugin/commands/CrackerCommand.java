package fun.vynofc.christmasPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fun.vynofc.christmasPlugin.ChristmasPlugin;

import java.util.*;

public class CrackerCommand implements CommandExecutor, TabCompleter {

    private final ChristmasPlugin plugin;

    public CrackerCommand(ChristmasPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            plugin.getMessageManager().sendMessage(sender, "usage.cracker");
            return true;
        }

        // /cracker list
        if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("christmasplus.cracker.list")) {
                plugin.getMessageManager().sendMessage(sender, "general.no-permission");
                return true;
            }

            plugin.getMessageManager().sendMessage(sender, "crackers.list-header");
            for (String type : plugin.getCrackerManager().getCrackerTypes()) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("cracker", type);
                plugin.getMessageManager().sendMessage(sender, "crackers.list-entry", placeholders);
            }

            return true;
        }

        // /cracker give <player> <cracker> [amount]
        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("christmasplus.cracker.give")) {
                plugin.getMessageManager().sendMessage(sender, "general.no-permission");
                return true;
            }

            if (args.length < 3) {
                plugin.getMessageManager().sendMessage(sender, "usage.cracker");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", args[1]);
                plugin.getMessageManager().sendMessage(sender, "general.player-not-found", placeholders);
                return true;
            }

            String crackerType = args[2];
            if (!plugin.getCrackerManager().crackerExists(crackerType)) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("cracker", crackerType);
                plugin.getMessageManager().sendMessage(sender, "crackers.cracker-not-found", placeholders);
                return true;
            }

            int amount = 1;
            if (args.length >= 4) {
                try {
                    amount = Integer.parseInt(args[3]);
                    if (amount < 1 || amount > 64) {
                        plugin.getMessageManager().sendMessage(sender, "crackers.invalid-amount");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("input", args[3]);
                    plugin.getMessageManager().sendMessage(sender, "general.invalid-number", placeholders);
                    return true;
                }
            }

            // Give crackers
            ItemStack cracker = plugin.getCrackerManager().createCracker(crackerType);
            if (cracker != null) {
                cracker.setAmount(amount);

                if (target.getInventory().firstEmpty() == -1) {
                    plugin.getMessageManager().sendMessage(sender, "crackers.inventory-full");
                    return true;
                }

                target.getInventory().addItem(cracker);

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("amount", String.valueOf(amount));
                placeholders.put("cracker", crackerType);
                placeholders.put("player", target.getName());

                plugin.getMessageManager().sendMessage(sender, "crackers.given-to-player", placeholders);

                placeholders.put("player", sender.getName());
                plugin.getMessageManager().sendMessage(target, "crackers.received-from-player", placeholders);
            }

            return true;
        }

        // /cracker get <cracker> [amount]
        if (args[0].equalsIgnoreCase("get")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cThis command can only be used by players!");
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("christmasplus.cracker.get")) {
                plugin.getMessageManager().sendMessage(player, "general.no-permission");
                return true;
            }

            if (args.length < 2) {
                plugin.getMessageManager().sendMessage(sender, "usage.cracker");
                return true;
            }

            String crackerType = args[1];
            if (!plugin.getCrackerManager().crackerExists(crackerType)) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("cracker", crackerType);
                plugin.getMessageManager().sendMessage(player, "crackers.cracker-not-found", placeholders);
                return true;
            }

            int amount = 1;
            if (args.length >= 3) {
                if (!player.hasPermission("christmasplus.cracker.get.multiple")) {
                    plugin.getMessageManager().sendMessage(player, "crackers.no-permission-multiple");
                    return true;
                }

                try {
                    amount = Integer.parseInt(args[2]);
                    if (amount < 1 || amount > 64) {
                        plugin.getMessageManager().sendMessage(player, "crackers.invalid-amount");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("input", args[2]);
                    plugin.getMessageManager().sendMessage(player, "general.invalid-number", placeholders);
                    return true;
                }
            }

            // Give cracker
            ItemStack cracker = plugin.getCrackerManager().createCracker(crackerType);
            if (cracker != null) {
                cracker.setAmount(amount);

                if (player.getInventory().firstEmpty() == -1) {
                    plugin.getMessageManager().sendMessage(player, "crackers.inventory-full");
                    return true;
                }

                player.getInventory().addItem(cracker);

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("amount", String.valueOf(amount));
                placeholders.put("cracker", crackerType);
                placeholders.put("player", player.getName());

                plugin.getMessageManager().sendMessage(player, "crackers.received-from-player", placeholders);
            }

            return true;
        }

        plugin.getMessageManager().sendMessage(sender, "usage.cracker");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("christmasplus.cracker.list")) {
                completions.add("list");
            }
            if (sender.hasPermission("christmasplus.cracker.give")) {
                completions.add("give");
            }
            if (sender.hasPermission("christmasplus.cracker.get")) {
                completions.add("get");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            } else if (args[0].equalsIgnoreCase("get")) {
                completions.addAll(plugin.getCrackerManager().getCrackerTypes());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(plugin.getCrackerManager().getCrackerTypes());
        } else if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            completions.add("1");
            completions.add("5");
            completions.add("10");
            completions.add("32");
            completions.add("64");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("get")) {
            if (sender.hasPermission("christmasplus.cracker.get.multiple")) {
                completions.add("1");
                completions.add("5");
                completions.add("10");
                completions.add("32");
                completions.add("64");
            }
        }

        return completions;
    }
}

