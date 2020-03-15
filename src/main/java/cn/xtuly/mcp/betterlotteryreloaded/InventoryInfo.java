package cn.xtuly.mcp.betterlotteryreloaded;

import org.bukkit.inventory.Inventory;

public class InventoryInfo {
    private Inventory inventory;
    private String key;

    public InventoryInfo(String key2, Inventory inventory2) {
        this.key = key2;
        this.inventory = inventory2;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key2) {
        this.key = key2;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(Inventory inventory2) {
        this.inventory = inventory2;
    }
}