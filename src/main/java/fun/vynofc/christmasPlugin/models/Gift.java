package fun.vynofc.christmasPlugin.models;

import org.bukkit.inventory.ItemStack;

public class Gift {

    private final GiftType type;
    private ItemStack itemStack;
    private String command;
    private String permission;
    private int duration; // -1 for permanent

    public Gift(GiftType type) {
        this.type = type;
        this.duration = -1;
    }

    public GiftType getType() {
        return type;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}

