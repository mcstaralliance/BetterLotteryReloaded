package cn.xtuly.mcp.betterlotteryreloaded;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LotteryHelper implements Runnable {
    public static final int[] INTS = {12, 13, 14, 21, 22, 23, 30, 31, 32};
    public static final HashMap<String, PlayerLotteryInfo> LOTTERY_PLAYERS = new HashMap<>();
    public static final String TITLE_PREFIX = "Lottery-";

    private static void showGui(Player player, Inventory inventory) {
        player.closeInventory();
        player.openInventory(inventory);
    }

    private static ItemStack getItemStack(Material material, String name, String... lore) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        List<String> loreList = new ArrayList<>();
        for (String add : lore) {
            loreList.add(add);
        }
        itemMeta.setLore(loreList);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static void lotteryDraw(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String itemName = player.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
        if (!itemName.contains("&")) {
            String key = itemName.replaceAll("§", "&");
            FileConfiguration config = Config.load("BetterLottery");
            if (config.contains(key)) {
                event.setCancelled(true);
                if (config.getBoolean(String.valueOf(key) + ".enable")) {
                    lotteryDrawGui(player, config, key);
                } else {
                    player.sendMessage("[§aBetterLotteryReload§f] §c无法使用[" + key.replaceAll("&", "§") + "§c]§e>>>该奖池未开启");
                }
            }
        }
    }

    public static void lotteryDrawClicked(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        if (event.getRawSlot() == 22) {
            PlayerLotteryInfo playerLotteryInfo = LOTTERY_PLAYERS.get(player.getName());
            if (playerLotteryInfo != null && !playerLotteryInfo.isRun()) {
                FileConfiguration config = Config.load("BetterLottery");
                if (!config.getBoolean(playerLotteryInfo.getKey() + ".enable")) {
                    player.sendMessage("[§aBetterLotteryReload§f] §c无法启动§e>>>该奖池已关闭");
                    player.closeInventory();
                } else if (!playerLotteryInfo.getHand().equals(player.getInventory().getItemInMainHand())) {
                    player.closeInventory();
                    LOTTERY_PLAYERS.remove(player.getName());
                } else {
                    boolean isFull = true;
                    ItemStack[] itemStacks = player.getInventory().getContents();
                    for (ItemStack itemStack : itemStacks) {
                        if (itemStack == null) {
                            isFull = false;
                            break;
                        }
                    }
                    if (isFull) {
                        player.sendMessage("[§aBetterLotteryReload§f] §c无法启动§e>>>背包至少保留一个空位");
                        player.closeInventory();
                        return;
                    }
                    List<Integer> odds = config.getIntegerList(playerLotteryInfo.getKey() + ".odds");
                    List<Integer> notice = config.getIntegerList(playerLotteryInfo.getKey() + ".notice");
                    int maxOdds = 0;
                    for (Integer i : odds) {
                        if (i > 0) {
                            maxOdds += i;
                        }else{
                            break;
                        }
                    }
                    playerLotteryInfo.setMaxOdds(maxOdds);
                    String[] chestInfo = config.getString(playerLotteryInfo.getKey() + ".chest").split(" ");
                    int x = Integer.parseInt(chestInfo[0]);
                    int y = Integer.parseInt(chestInfo[1]);
                    int z = Integer.parseInt(chestInfo[2]);
                    Inventory buttonInventory = ((Chest) getChestWorld().getBlockAt(x, y, z).getState()).getInventory();
                    ArrayList<PrizeInfo> arrayList = new ArrayList<>();
                    for (int i = 0; i < 27; i++) {
                        ItemStack tempItemStack = buttonInventory.getItem(i);
                        if (tempItemStack != null) {
                            PrizeInfo prizeInfo = new PrizeInfo(tempItemStack.clone(), odds.get(i), notice.get(i) == 1);
                            arrayList.add(prizeInfo);
                        }
                    }
                    Block block = getChestWorld().getBlockAt(x, y + 1, z);
                    if (block.getType().equals(Material.CHEST)) {
                        Inventory topInventory = ((Chest) block.getState()).getInventory();
                        for (int i3 = 0; i3 < 27; i3++) {
                            ItemStack tempItemStack2 = topInventory.getItem(i3);
                            if (tempItemStack2 != null) {
                                PrizeInfo prizeInfo2 = new PrizeInfo(tempItemStack2.clone(), odds.get(i3 + 27), notice.get(i3 + 27) == 1);
                                arrayList.add(prizeInfo2);
                            }
                        }
                    }
                    Collections.shuffle(arrayList);
                    playerLotteryInfo.setPrizeInfos(arrayList);
                    int num = ((int) (Math.random() * ((double) playerLotteryInfo.getMaxOdds()))) + 1;
                    Iterator it = playerLotteryInfo.getPrizeInfos().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        PrizeInfo prizeInfo3 = (PrizeInfo) it.next();
                        num -= prizeInfo3.getOdds();
                        if (num <= 0) {
                            playerLotteryInfo.setPrize(prizeInfo3);
                            break;
                        }
                    }
                    playerLotteryInfo.run();
                }
            }
        }
    }

    public static void lotteryDrawGui(Player player, FileConfiguration config, String key) {
        Inventory inventory = Bukkit.createInventory(player, 45, TITLE_PREFIX + config.getString(key + ".title").replaceAll("&", "§"));
        for (int i = 0; i < 45; i++) {
            inventory.setItem(i, getItemStack(Material.STAINED_GLASS_PANE, "§7暂时被封印的结界"));
        }
        int[] arrayOfInt = INTS;
        int j = arrayOfInt.length;
        for (byte b = 0; b < j; b = (byte) (b + 1)) {
            inventory.setItem(arrayOfInt[b], getItemStack(Material.THIN_GLASS, "§8永远被封印的结界"));
        }
        String[] chestInfo = config.getString(key + ".chest").split(" ");
        int x = Integer.parseInt(chestInfo[0]);
        int y = Integer.parseInt(chestInfo[1]);
        int z = Integer.parseInt(chestInfo[2]);
        Inventory buttonInventory = ((Chest) getChestWorld().getBlockAt(x, y, z).getState()).getInventory();
        int currentPos = 0;
        for (int i2 = 0; i2 < 27; i2++) {
            ItemStack tempItemStack = buttonInventory.getItem(i2);
            if (tempItemStack != null) {
                inventory.setItem(currentPos, tempItemStack);
                while (true) {
                    currentPos++;
                    if (!inventory.getItem(currentPos).getType().equals(Material.THIN_GLASS)) {
                        break;
                    }
                }
            }
        }
        Block block = getChestWorld().getBlockAt(x, y + 1, z);
        if (block.getType().equals(Material.CHEST)) {
            Inventory topInventory = ((Chest) block.getState()).getInventory();
            for (int i3 = 0; i3 < 27; i3++) {
                ItemStack tempItemStack2 = topInventory.getItem(i3);
                if (tempItemStack2 != null) {
                    inventory.setItem(currentPos, tempItemStack2);
                    while (true) {
                        currentPos++;
                        if (currentPos >= 45 || !inventory.getItem(currentPos).getType().equals(Material.THIN_GLASS)) {
                            break;
                        }
                    }
                }
            }
        }
        inventory.setItem(22, getItemStack(Material.LEVER, "§a启动"));
        showGui(player, inventory);
        HashMap<String, PlayerLotteryInfo> hashMap = LOTTERY_PLAYERS;
        String name = player.getName();
        PlayerLotteryInfo playerLotteryInfo = new PlayerLotteryInfo(player, key, inventory, player.getInventory().getItemInMainHand());
        hashMap.put(name, playerLotteryInfo);
    }

    public void run() {
        for (PlayerLotteryInfo playerLotteryInfo : LOTTERY_PLAYERS.values()) {
            playerLotteryInfo.update();
        }
    }

    public static World getChestWorld() {
        return Bukkit.getWorld("BetterLotteryChest");
    }
}