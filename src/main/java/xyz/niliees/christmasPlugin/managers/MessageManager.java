package xyz.niliees.christmasPlugin.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.niliees.christmasPlugin.ChristmasPlugin;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final ChristmasPlugin plugin;
    private FileConfiguration messagesConfig;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MessageManager(ChristmasPlugin plugin) {
        this.plugin = plugin;
        this.messagesConfig = plugin.getConfigManager().getMessagesConfig();
    }

    public void reload() {
        plugin.getConfigManager().reloadConfig("messages");
        this.messagesConfig = plugin.getConfigManager().getMessagesConfig();
    }

    public String getMessage(String path) {
        return messagesConfig.getString(path, "&cMessage not found: " + path);
    }

    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);

        // Replace prefix placeholder
        String prefix = messagesConfig.getString("prefix", "&c&l[Christmas] &r");
        message = message.replace("{prefix}", prefix);

        // Replace custom placeholders
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return colorize(message);
    }

    public void sendMessage(CommandSender sender, String path) {
        sendMessage(sender, path, null);
    }

    public void sendMessage(CommandSender sender, String path, Map<String, String> placeholders) {
        String message = getMessage(path, placeholders);
        sender.sendMessage(message);
    }

    public void sendMessage(Player player, String path) {
        sendMessage(player, path, null);
    }

    public void sendMessage(Player player, String path, Map<String, String> placeholders) {
        String message = getMessage(path, placeholders);
        player.sendMessage(message);
    }

    public String colorize(String message) {
        // Support both legacy color codes and MiniMessage format
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public Component getMiniMessageComponent(String message) {
        return miniMessage.deserialize(message);
    }

    public static Map<String, String> createPlaceholderMap() {
        return new HashMap<>();
    }

    public static Map<String, String> placeholder(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    public static Map<String, String> placeholders(String... keysAndValues) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keysAndValues.length - 1; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }
}

