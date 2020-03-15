package cn.xtuly.mcp.betterlotteryreloaded;

import org.bukkit.inventory.ItemStack;

public class PrizeInfo {
    private ItemStack itemStack;
    private boolean notice;
    private int odds;

    public PrizeInfo(ItemStack itemStack2, int odds2, boolean notice2) {
        this.itemStack = itemStack2;
        this.odds = odds2;
        this.notice = notice2;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public void setItemStack(ItemStack itemStack2) {
        this.itemStack = itemStack2;
    }

    public int getOdds() {
        return this.odds;
    }

    public void setOdds(int odds2) {
        this.odds = odds2;
    }

    public boolean isNotice() {
        return this.notice;
    }

    public void setNotice(boolean notice2) {
        this.notice = notice2;
    }

    public String toString() {
        return "PrizeInfo [itemStack=" + this.itemStack + ", odds=" + this.odds + ", notice=" + this.notice + "]";
    }
}