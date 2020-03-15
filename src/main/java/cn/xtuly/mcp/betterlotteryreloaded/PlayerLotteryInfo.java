package cn.xtuly.mcp.betterlotteryreloaded;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerLotteryInfo extends InventoryInfo {
    private boolean animationEnded;
    private int delayIndex;
    private Delay[] delays;
    private ItemStack hand;
    private List<ItemStack> inventoryItemStacks = new ArrayList();
    private boolean isGave;
    private int lastItemStackSelectedID = -1;
    private int maxOdds;
    private Player player;
    private PrizeInfo prize;
    private List<PrizeInfo> prizeInfos;
    private boolean run;
    private long startTime;

    public PlayerLotteryInfo(Player player2, String key, Inventory inventory, ItemStack hand2) {
        super(key, inventory);
        this.hand = hand2;
        this.player = player2;
    }

    public ItemStack getHand() {
        return this.hand;
    }

    public void setHand(ItemStack hand2) {
        this.hand = hand2;
    }

    public PrizeInfo getPrize() {
        return this.prize;
    }

    public void setPrize(PrizeInfo prize2) {
        this.prize = prize2;
        this.prize.setItemStack(this.prize.getItemStack().clone());
    }

    public List<PrizeInfo> getPrizeInfos() {
        return this.prizeInfos;
    }

    public void setPrizeInfos(List<PrizeInfo> prizeInfos2) {
        this.prizeInfos = prizeInfos2;
    }

    public int getMaxOdds() {
        return this.maxOdds;
    }

    public void setMaxOdds(int maxOdds2) {
        this.maxOdds = maxOdds2;
    }

    public void run() {
        ItemStack[] contents = this.player.getInventory().getContents();
        for(int i = 0;i<contents.length;i++){
            if(contents[i] != null && contents[i].equals(this.hand)){
                if (contents[i].getAmount() == 1){
                    this.player.getInventory().setItem(i,null);
                }else{
                    contents[i].setAmount(contents[i].getAmount() - 1);
                    this.player.getInventory().setItem(i,contents[i]);
                }
                break;
            }
        }
        int ranNum = ((int) (Math.random() * ((double) this.prizeInfos.size()))) + this.prizeInfos.size();
        int posCount = 0;
        for(posCount = 0; posCount < this.prizeInfos.size(); posCount++){
            ItemStack itemStack = this.prizeInfos.get((((ranNum + 4) % this.prizeInfos.size()) + posCount) % this.prizeInfos.size()).getItemStack();
            if (itemStack.equals(this.prize.getItemStack())) {
                break;
            }
        }
        this.delays = new Delay[(ranNum + 5 + posCount)];
        for (int i2 = 0; i2 < 5; i2++) {
            this.delays[i2] = new Delay(500 - (i2 * 100));
        }
        for (int i3 = 0; i3 < ranNum; i3++) {
            this.delays[i3 + 5] = new Delay(100);
        }
        for (int i4 = 0; i4 < posCount; i4++) {
            this.delays[i4 + 5 + ranNum] = new Delay(((this.prizeInfos.size() * 20) + 100) - ((this.prizeInfos.size() - i4) * 20));
        }
        for (int i5 = this.delays.length - 1; i5 >= 0; i5--) {
            int count = 0;
            for (int j = 0; j <= i5; j++) {
                count += this.delays[j].getTime();
            }
            this.delays[i5].setTime(count);
        }
        this.run = true;
        this.startTime = System.currentTimeMillis();
        getInventory().setItem(22, null);

        int currentPos = 0;
        for (PrizeInfo prizeInfo : this.prizeInfos) {
            getInventory().setItem(currentPos, prizeInfo.getItemStack());
            this.inventoryItemStacks.add(getInventory().getItem(currentPos));
            currentPos++;
            if (currentPos == 22 || (currentPos < 45 && getInventory().getItem(currentPos).getType().equals(Material.THIN_GLASS))) {

            }
        }
    }

    public void update() {
        if (this.run && !this.animationEnded) {
            long time = System.currentTimeMillis() - this.startTime;
            Delay delay = this.delays[this.delayIndex];
            if (time >= ((long) delay.getTime())) {
                this.delayIndex++;
                if (this.delayIndex == this.delays.length) {
                    this.player.playSound(this.player.getLocation(), Sound.ENTITY_FIREWORK_BLAST, 1.0f, 1.0f);
                    this.animationEnded = true;
                    ended();
                }
            } else if (!delay.isDelayed()) {
                this.player.playSound(this.player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                if (this.lastItemStackSelectedID != -1) {
                    this.inventoryItemStacks.get(this.lastItemStackSelectedID).removeEnchantment(Enchantment.DURABILITY);
                }
                ItemStack itemStack = this.inventoryItemStacks.get(this.delayIndex % this.inventoryItemStacks.size());
                getInventory().setItem(22, itemStack.clone());
                itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
                this.lastItemStackSelectedID = this.delayIndex % this.inventoryItemStacks.size();
                delay.setDelayed(true);
            }
        }
    }

    public boolean isRun() {
        return this.run;
    }

    public boolean isAnimationEnded() {
        return this.animationEnded;
    }

    public void setAnimationEnded(boolean animationEnded2) {
        this.animationEnded = animationEnded2;
    }

    public void ended() {
        if (!this.isGave) {
            this.player.getInventory().addItem(new ItemStack[]{getPrize().getItemStack()});
            LotteryHelper.LOTTERY_PLAYERS.remove(this.player.getName());
            this.isGave = true;
            if (getPrize().isNotice()) {
                Collection<? extends Player> arrayOfPlayer = Bukkit.getOnlinePlayers();
                int i = arrayOfPlayer.size();
                for (byte b = 0; b < i; b = (byte) (b + 1)) {
                    ((Player) arrayOfPlayer.toArray()[b]).sendMessage("[§aBetterLotteryReload§f] §a恭喜玩家§e[" + this.player.getName() + "§e]§a在转转乐当中使用§e[" + this.hand.getItemMeta().getDisplayName() + "§e]§a抽取到了§e[" + ((!this.prize.getItemStack().hasItemMeta() || !this.prize.getItemStack().getItemMeta().hasDisplayName()) ? this.prize.getItemStack().getType().name() : this.prize.getItemStack().getItemMeta().getDisplayName()) + "§e X " + this.prize.getItemStack().getAmount() + "]");
                }
            }
        }
    }
}