package cn.xtuly.mcp.betterlotteryreloaded;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiHelper {
    public static final HashMap<String, String> CREATING_OPS = new HashMap<>();
    public static final String MENU_TITLE = "         §4BetterLottery-奖池菜单";
    public static final String MESSAGE_PREFIX = "[§aBetterLotteryReload§f] ";
    public static final HashMap<String, Integer> SELECT_POOL_OPS = new HashMap<>();
    public static final String SELECT_POOL_TITLE = "         §4BetterLottery-选择操作";
    public static final HashMap<String, Integer> SETTING_ODDS_OPS = new HashMap<>();
    public static final HashMap<String, InventoryInfo> SETTING_OPS = new HashMap<>();
    public static final String SETTING_TITLE = "         §4BetterLottery-更改设置";
    public static final String TITLE_PREFIX = "         §4BetterLottery-";

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

    public static void menuGui(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 9, MENU_TITLE);
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, getItemStack(Material.THIN_GLASS, "§8永远被封印的结界"));
        }
        inventory.setItem(3, getItemStack(Material.ANVIL, "§a创建抽奖池", "§c创建操作： §e左键单击"));
        inventory.setItem(5, getItemStack(Material.SIGN, "§a抽奖池设置", "§c设置操作： §e左键单击"));
        showGui(player, inventory);
    }

    public static void playerClickedGui(InventoryClickEvent event) {
        String title = event.getInventory().getTitle();
        Player player = (Player) event.getWhoClicked();
        int rawSlot = event.getRawSlot();
        if (title.equals(MENU_TITLE)) {
            event.setCancelled(true);
            menuClicked(player, rawSlot);
        }
        if (title.equals(SETTING_TITLE)) {
            if (rawSlot < 54 || event.isShiftClick() || event.getClick() == ClickType.DOUBLE_CLICK) {
                event.setCancelled(true);
            }
            settingClicked(event);
        }
        if (title.equals(SELECT_POOL_TITLE)) {
            event.setCancelled(true);
            selectPoolClicked(event);
        }
    }

    public static void menuClicked(Player player, int rawSlot) {
        if (rawSlot == 3) {
            player.sendMessage("[§aBetterLotteryReload§f] §e第一步>>>请在聊天框中输入奖池名称§c(颜色符&,名称可以重复)");
            CREATING_OPS.put(player.getName(), null);
            player.closeInventory();
        } else if (rawSlot == 5) {
            SELECT_POOL_OPS.put(player.getName(), 0);
            selectPoolGui(player);
        }
    }

    public static void createPool(Player player, String message) {
        String message2 = message.replaceAll("&", "§");
        String playerName = player.getName();
        String opMessage = CREATING_OPS.get(playerName);
        if (opMessage == null) {
            CREATING_OPS.put(playerName, message2);
            player.sendMessage("[§aBetterLotteryReload§f] §e奖池名称设置为：§f" + message2);
            player.sendMessage("");
            player.sendMessage("[§aBetterLotteryReload§f] §e第二步>>>请在聊天框中输入抽奖券名称§c(颜色符&,名称不可以重复)");
            return;
        }
        String message3 = "&f" + message2;
        player.sendMessage("[§aBetterLotteryReload§f] §e抽奖券名称设置为：§f" + message3);
        player.sendMessage("");
        FileConfiguration config = Config.load("BetterLottery");
        if (config.contains(message3.replaceAll("§", "&"))) {
            player.sendMessage("[§aBetterLotteryReload§f] §c" + message3 + "§e>>>创建失败，抽奖券名称已经被占用，请重新点击创建按钮进行创建");
        } else {
            player.sendMessage("[§aBetterLotteryReload§f] §c" + opMessage + "§e>>>创建完成，请点击抽奖池设置按钮进行配置");
            player.sendMessage("");
            config.set(message3.replaceAll("§", "&") + ".title", opMessage.replaceAll("§", "&"));
            config.set(message3.replaceAll("§", "&") + ".enable", Boolean.FALSE);
            Config.save(config, "BetterLottery");
        }
        CREATING_OPS.remove(playerName);
    }

    public static void selectPoolClicked(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int rawSlot = event.getRawSlot();
        FileConfiguration config = Config.load("BetterLottery");
        Set<String> keys = config.getKeys(false);
        int startValue = SELECT_POOL_OPS.get(player.getName()) * 36;
        String[] strings = new String[keys.size()];
        keys.toArray(strings);
        if (event.isLeftClick()) {
            if (!event.isShiftClick()) {
                if (rawSlot < 36 && startValue + rawSlot < strings.length) {
                    settingGui(player, strings[startValue + rawSlot]);
                } else if (rawSlot == 48) {
                    if (startValue > 0) {
                        SELECT_POOL_OPS.put(player.getName(), SELECT_POOL_OPS.get(player.getName()) - 1);
                        selectPoolGui(player);
                    }
                } else if (rawSlot == 49) {
                    menuGui(player);
                } else if (rawSlot == 50 && (strings.length - startValue) + 36 > 0) {
                    SELECT_POOL_OPS.put(player.getName(), (Integer) SELECT_POOL_OPS.get(player.getName()) + 1);
                    selectPoolGui(player);
                }
            } else if (startValue + rawSlot < strings.length) {
                String key = strings[startValue + rawSlot];
                ItemStack itemStack = player.getInventory().getItemInMainHand();
                if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                    player.sendMessage("[§aBetterLotteryReload§f] §c拓印失败>>>手持物品不能为空");
                    return;
                }
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(key.replaceAll("&", "§"));
                itemStack.setItemMeta(itemMeta);
                player.sendMessage("[§aBetterLotteryReload§f] §c拓印完成>>>手持右键即可食用");
            }
        } else if (event.isRightClick() && event.isShiftClick() && startValue + rawSlot < strings.length) {
            config.set(strings[startValue + rawSlot], null);
            Config.save(config, "BetterLottery");
            selectPoolGui(player);
        }
    }

    public static void selectPoolGui(Player player) {
        FileConfiguration config = Config.load("BetterLottery");
        Set<String> keys = config.getKeys(false);
        Inventory inventory = Bukkit.createInventory(player, 54, SELECT_POOL_TITLE);
        for (int i = 0; i < 36; i++) {
            inventory.setItem(i, getItemStack(Material.STAINED_GLASS_PANE, "§7暂时被封印的结界"));
        }
        for (int i2 = 36; i2 < 54; i2++) {
            inventory.setItem(i2, getItemStack(Material.THIN_GLASS, "§8永远被封印的结界"));
        }
        int id = 0;
        String[] strings = new String[keys.size()];
        keys.toArray(strings);
        int startValue = SELECT_POOL_OPS.get(player.getName()) * 36;
        int i3 = startValue;
        for(int id2 = id;i3 < startValue + 36 && i3 < strings.length;i3++){
            String title = "§a奖池名称： §e" + config.getString(strings[i3] + ".title").replaceAll("&", "§");
            boolean enable = config.getBoolean(strings[i3] + ".enable");
            ItemStack itemStack = getItemStack(Material.WOOL, title, "§a奖券名称： §e" + strings[i3].replaceAll("&", "§"), "§a当前状态： §e" + (enable ? "开启" : "关闭"), "§c更改设置： §e左键单击", "§c拓印奖券： §e手持需要拓印的物品 Shift+左键单击", "§c删除奖池： §eShift+右键单击");
            itemStack.setDurability((short) (enable ? 5 : 14));
            inventory.setItem(id2, itemStack);
        }
        boolean hasNext = (strings.length - startValue) + 36 > 0;
        inventory.setItem(48, getItemStack(Material.LEATHER_CHESTPLATE, String.valueOf(startValue > 0 ? "§a" : "§7") + "上一页", "§c翻页操作： §e左键单击"));
        inventory.setItem(49, getItemStack(Material.ENDER_PEARL, "§a返回上层", "§c返回操作： §e左键单击"));
        inventory.setItem(50, getItemStack(Material.LEATHER_LEGGINGS, String.valueOf(hasNext ? "§a" : "§7") + "下一页", "§c翻页操作： §e左键单击"));
        showGui(player, inventory);
    }

    public static void settingGui(Player player, String key) {
        FileConfiguration config = Config.load("BetterLottery");
        Inventory inventory = Bukkit.createInventory(player, 54, SETTING_TITLE);
        for (int i = 0; i < 36; i++) {
            inventory.setItem(i, getItemStack(Material.STAINED_GLASS_PANE, "§7暂时被封印的结界"));
        }
        for (int i2 = 36; i2 < 54; i2++) {
            inventory.setItem(i2, getItemStack(Material.THIN_GLASS, "§8永远被封印的结界"));
        }
        if (config.contains(String.valueOf(key) + ".chest")) {
            String[] chestInfo = config.getString(key + ".chest").split(" ");
            int x = Integer.parseInt(chestInfo[0]);
            int y = Integer.parseInt(chestInfo[1]);
            int z = Integer.parseInt(chestInfo[2]);
            Inventory buttonInventory = ((Chest)getChestWorld().getBlockAt(x, y, z).getState()).getInventory();
            List<Integer> odds = config.getIntegerList(String.valueOf(key) + ".odds");
            List<Integer> notice = config.getIntegerList(String.valueOf(key) + ".notice");
            int maxOdds = 0;
            for (Integer i3 : odds) {
                if (i3 > 0) {
                    maxOdds += i3;
                }
            }
            for (int i4 = 0; i4 < 27; i4++) {
                ItemStack tempItemStack = buttonInventory.getItem(i4);
                if (tempItemStack != null) {
                    ItemStack tempItemStack2 = tempItemStack.clone();
                    ItemMeta itemMeta = tempItemStack2.getItemMeta();
                    List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
                    if (itemMeta.hasLore()) {
                        lore.add("§f--------------------");
                    }
                    lore.add("§a中奖几率： §e" + ((Integer) odds.get(i4) == -1 ? "未设置" : odds.get(i4) + "/" + maxOdds));
                    lore.add("§a全服公告： §e" + ((Integer) notice.get(i4) == 0 ? "关闭" : "开启"));
                    lore.add("§c设置几率： §e左键单击");
                    lore.add("§c" + ((Integer) notice.get(i4) == 0 ? "开启" : "关闭") + "公告： §eShift+左键单击");
                    lore.add("§c移除奖品： §eShift+右键单击");
                    itemMeta.setLore(lore);
                    tempItemStack2.setItemMeta(itemMeta);
                    inventory.setItem(i4, tempItemStack2);
                }
            }
            Block block = getChestWorld().getBlockAt(x, y + 1, z);
            if (block.getType().equals(Material.CHEST)) {
                Inventory topInventory = ((Chest)block.getState()).getInventory();
                for (int i5 = 0; i5 < 27; i5++) {
                    ItemStack tempItemStack3 = topInventory.getItem(i5);
                    if (tempItemStack3 != null) {
                        ItemStack tempItemStack4 = tempItemStack3.clone();
                        ItemMeta itemMeta2 = tempItemStack4.getItemMeta();
                        List<String> lore2 = itemMeta2.hasLore() ? itemMeta2.getLore() : new ArrayList<>();
                        if (itemMeta2.hasLore()) {
                            lore2.add("§f--------------------");
                        }
                        lore2.add("§a中奖几率： §e" + ((Integer) odds.get(i5 + 27) == -1 ? "未设置" : odds.get(i5 + 27) + "/" + maxOdds));
                        lore2.add("§a全服公告： §e" + ((Integer) notice.get(i5 + 27) == 0 ? "关闭" : "开启"));
                        lore2.add("§c设置几率： §e左键单击");
                        lore2.add("§c" + ((Integer) notice.get(i5 + 27) == 0 ? "开启" : "关闭") + "公告： §eShift+左键单击");
                        lore2.add("§c移除奖品： §eShift+右键单击");
                        itemMeta2.setLore(lore2);
                        tempItemStack4.setItemMeta(itemMeta2);
                        inventory.setItem(i5 + 27, tempItemStack4);
                    }
                }
            }
        }
        inventory.setItem(48, getItemStack(Material.HOPPER, "§a添加奖品", "§c添加操作： §e拖拽物品到此处左键单击"));
        inventory.setItem(49, getItemStack(Material.ENDER_PEARL, "§a返回上层", "§c返回操作： §e左键单击"));
        boolean enable = config.getBoolean(String.valueOf(key) + ".enable");
        Material material = enable ? Material.WATER_BUCKET : Material.BUCKET;
        String str = "§a" + (enable ? "关闭" : "开启") + "奖池";
        String[] strArr = new String[1];
        strArr[0] = "§c" + (enable ? "关闭" : "开启") + "操作： §e左键单击";
        inventory.setItem(50, getItemStack(material, str, strArr));
        HashMap<String, InventoryInfo> hashMap = SETTING_OPS;
        String name = player.getName();
        InventoryInfo inventoryInfo = new InventoryInfo(key, inventory);
        hashMap.put(name, inventoryInfo);
        showGui(player, inventory);
    }

    public static void settingClicked(InventoryClickEvent event) {
        Block block;
        Inventory chestInventory;
        Player player = (Player) event.getWhoClicked();
        int rawSlot = event.getRawSlot();
        FileConfiguration config = Config.load("BetterLottery");
        InventoryInfo inventoryInfo = (InventoryInfo) SETTING_OPS.get(player.getName());
        String key = inventoryInfo.getKey();
        boolean isOpened = config.getBoolean(String.valueOf(key) + ".enable");
        if (event.isLeftClick()) {
            if (!event.isShiftClick()) {
                if (rawSlot == 48) {
                    if (!isOpened) {
                        ItemStack itemStack = player.getItemOnCursor();
                        if (itemStack.getTypeId() > 0) {
                            boolean isTopInventory = false;
                            if (config.contains(String.valueOf(key) + ".chest")) {
                                String[] chestInfo = config.getString(String.valueOf(key) + ".chest").split(" ");
                                int x = Integer.parseInt(chestInfo[0]);
                                int y = Integer.parseInt(chestInfo[1]);
                                int z = Integer.parseInt(chestInfo[2]);
                                Inventory buttonInventory = ((Chest)getChestWorld().getBlockAt(x, y, z).getState()).getInventory();
                                boolean isFull = true;
                                int i = 0;
                                while (true) {
                                    if (i >= 27) {
                                        break;
                                    } else if (buttonInventory.getItem(i) == null) {
                                        isFull = false;
                                        break;
                                    } else {
                                        i++;
                                    }
                                }
                                if (isFull) {
                                    Block block2 = getChestWorld().getBlockAt(x, y + 1, z);
                                    if (block2.getType().equals(Material.AIR)) {
                                        block2.setType(Material.CHEST);
                                    }
                                    chestInventory = ((Chest)block2.getState()).getInventory();
                                    isTopInventory = true;
                                } else {
                                    chestInventory = buttonInventory;
                                }
                            } else {
                                int i2 = 0;
                                while (true) {
                                    block = getChestWorld().getBlockAt(i2, 4, 0);
                                    if (block.getType().equals(Material.AIR)) {
                                        break;
                                    }
                                    i2 += 2;
                                }
                                block.setType(Material.CHEST);
                                chestInventory = ((Chest)block.getState()).getInventory();
                                config.set(String.valueOf(key) + ".chest", String.valueOf(block.getX()) + " " + block.getY() + " " + block.getZ());
                                ArrayList arrayList = new ArrayList();
                                ArrayList arrayList2 = new ArrayList();
                                for (int j = 0; j < 36; j++) {
                                    arrayList.add(-1);
                                    arrayList2.add(0);
                                }
                                config.set(String.valueOf(key) + ".odds", arrayList);
                                config.set(String.valueOf(key) + ".notice", arrayList2);
                                Config.save(config, "BetterLottery");
                            }
                            int i3 = 0;
                            while (true) {
                                if (i3 >= (isTopInventory ? 9 : 27)) {
                                    break;
                                } else if (chestInventory.getItem(i3) == null) {
                                    chestInventory.setItem(i3, itemStack);
                                    break;
                                } else {
                                    i3++;
                                }
                            }
                            List<Integer> odds = config.getIntegerList(String.valueOf(key) + ".odds");
                            List<Integer> notice = config.getIntegerList(String.valueOf(key) + ".notice");
                            int maxOdds = 0;
                            for (Integer i4 : odds) {
                                if (i4.intValue() > 0) {
                                    maxOdds += i4.intValue();
                                }
                            }
                            int i5 = 0;
                            while (i5 < 36) {
                                ItemStack tempItemStack = inventoryInfo.getInventory().getItem(i5);
                                if (!tempItemStack.getType().equals(Material.STAINED_GLASS_PANE) || !tempItemStack.getItemMeta().getDisplayName().equals("§7暂时被封印的结界")) {
                                    if (i5 == 35) {
                                        player.sendMessage("[§aBetterLotteryReload§f] §c添加失败§e>>>奖池已满，无法继续添加");
                                    }
                                    i5++;
                                } else {
                                    ItemStack itemStack2 = itemStack.clone();
                                    ItemMeta itemMeta = itemStack2.getItemMeta();
                                    List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
                                    if (itemMeta.hasLore()) {
                                        lore.add("§f--------------------");
                                    }
                                    lore.add("§a中奖几率： §e" + ((Integer) odds.get(i5) == -1 ? "未设置" : odds.get(i5) + "/" + maxOdds));
                                    lore.add("§a全服公告： §e" + ((Integer) notice.get(i5) == 0 ? "关闭" : "开启"));
                                    lore.add("§c设置几率： §e左键单击");
                                    lore.add("§c" + ((Integer) notice.get(i5) == 0 ? "开启" : "关闭") + "公告： §eShift+左键单击");
                                    lore.add("§c移除奖品： §eShift+右键单击");
                                    itemMeta.setLore(lore);
                                    itemStack2.setItemMeta(itemMeta);
                                    inventoryInfo.getInventory().setItem(i5, itemStack2);
                                    return;
                                }
                            }
                            return;
                        }
                        return;
                    }
                    player.sendMessage("[§aBetterLotteryReload§f] §c添加失败§e>>>必须先关闭奖池才能进行操作");
                } else if (rawSlot == 49) {
                    selectPoolGui(player);
                } else if (rawSlot == 50) {
                    if (config.getBoolean(String.valueOf(key) + ".enable")) {
                        config.set(String.valueOf(key) + ".enable", Boolean.FALSE);
                        Config.save(config, "BetterLottery");
                        settingGui(player, key);
                        return;
                    }
                    boolean allowOpen = true;
                    int maxOdds2 = 0;
                    if (config.contains(String.valueOf(key) + ".odds")) {
                        List<Integer> odds2 = config.getIntegerList(String.valueOf(key) + ".odds");
                        int i6 = 0;
                        while (true) {
                            if (i6 >= 36) {
                                break;
                            }
                            ItemStack tempItemStack2 = inventoryInfo.getInventory().getItem(i6);
                            if (!tempItemStack2.getType().equals(Material.STAINED_GLASS_PANE) || !tempItemStack2.getItemMeta().getDisplayName().equals("§7暂时被封印的结界")) {
                                if ((Integer) odds2.get(i6) == -1) {
                                    allowOpen = false;
                                    break;
                                }
                                maxOdds2 += (Integer) odds2.get(i6);
                            }
                            i6++;
                        }
                    }
                    if (!allowOpen) {
                        player.sendMessage("[§aBetterLotteryReload§f] §c开启失败>>>必须为所有奖品设置几率");
                    } else if (maxOdds2 == 0) {
                        player.sendMessage("[§aBetterLotteryReload§f] §c开启失败>>>必须有一个几率大于0的奖品");
                    } else {
                        config.set(String.valueOf(key) + ".enable", Boolean.TRUE);
                        Config.save(config, "BetterLottery");
                        settingGui(player, key);
                        player.sendMessage("[§aBetterLotteryReload§f] §c开启完毕>>>获取奖券之后手持奖券右键即可进行抽奖");
                    }
                } else if (rawSlot >= 36) {
                } else {
                    if (!isOpened) {
                        ItemStack tempItemStack3 = inventoryInfo.getInventory().getItem(rawSlot);
                        if (!tempItemStack3.getType().equals(Material.STAINED_GLASS_PANE) || !tempItemStack3.getItemMeta().getDisplayName().equals("§7暂时被封印的结界")) {
                            player.closeInventory();
                            SETTING_ODDS_OPS.put(player.getName(), rawSlot);
                            player.sendMessage("[§aBetterLotteryReload§f] §e设置几率>>>请在聊天框中输入该奖品的几率§c(必须为正整数，可以是0)");
                            return;
                        }
                        return;
                    }
                    player.sendMessage("[§aBetterLotteryReload§f] §c设置失败§e>>>必须先关闭奖池才能进行操作");
                }
            } else if (rawSlot >= 36) {
            } else {
                if (!isOpened) {
                    ItemStack tempItemStack4 = inventoryInfo.getInventory().getItem(rawSlot);
                    if (!tempItemStack4.getType().equals(Material.STAINED_GLASS_PANE) || !tempItemStack4.getItemMeta().getDisplayName().equals("§7暂时被封印的结界")) {
                        String string = config.getString(String.valueOf(key) + ".chest");
                        List<Integer> odds3 = config.getIntegerList(String.valueOf(key) + ".odds");
                        List<Integer> notice2 = config.getIntegerList(String.valueOf(key) + ".notice");
                        notice2.set(rawSlot, (Integer) notice2.get(rawSlot) == 0 ? 1 : 0);
                        config.set(String.valueOf(key) + ".notice", notice2);
                        Config.save(config, "BetterLottery");
                        int maxOdds3 = 0;
                        for (Integer i7 : odds3) {
                            if (i7 > 0) {
                                maxOdds3 += i7;
                            }
                        }
                        String[] chestInfo2 = string.split(" ");
                        int x2 = Integer.parseInt(chestInfo2[0]);
                        int y2 = Integer.parseInt(chestInfo2[1]);
                        int z2 = Integer.parseInt(chestInfo2[2]);
                        World chestWorld = getChestWorld();
                        if (rawSlot >= 27) {
                            y2++;
                        }
                        ItemStack itemStack3 = ((Chest)chestWorld.getBlockAt(x2, y2, z2).getState()).getInventory().getItem(rawSlot % 27).clone();
                        ItemMeta itemMeta2 = itemStack3.getItemMeta();
                        List<String> lore2 = itemMeta2.hasLore() ? itemMeta2.getLore() : new ArrayList<>();
                        if (itemMeta2.hasLore()) {
                            lore2.add("§f--------------------");
                        }
                        lore2.add("§a中奖几率： §e" + ((Integer) odds3.get(rawSlot) == -1 ? "未设置" : odds3.get(rawSlot) + "/" + maxOdds3));
                        lore2.add("§a全服公告： §e" + ((Integer) notice2.get(rawSlot) == 0 ? "关闭" : "开启"));
                        lore2.add("§c设置几率： §e左键单击");
                        lore2.add("§c" + ((Integer) notice2.get(rawSlot) == 0 ? "开启" : "关闭") + "公告： §eShift+左键单击");
                        lore2.add("§c移除奖品： §eShift+右键单击");
                        itemMeta2.setLore(lore2);
                        itemStack3.setItemMeta(itemMeta2);
                        inventoryInfo.getInventory().setItem(rawSlot, itemStack3);
                        return;
                    }
                    return;
                }
                player.sendMessage("[§aBetterLotteryReload§f] §c设置失败§e>>>必须先关闭奖池才能进行操作");
            }
        } else if (event.isShiftClick() && event.isRightClick() && rawSlot < 36) {
            if (!isOpened) {
                ItemStack tempItemStack5 = inventoryInfo.getInventory().getItem(rawSlot);
                if (!tempItemStack5.getType().equals(Material.STAINED_GLASS_PANE) || !tempItemStack5.getItemMeta().getDisplayName().equals("§7暂时被封印的结界")) {
                    String[] chestInfo3 = config.getString(String.valueOf(key) + ".chest").split(" ");
                    int x3 = Integer.parseInt(chestInfo3[0]);
                    int y3 = Integer.parseInt(chestInfo3[1]);
                    int z3 = Integer.parseInt(chestInfo3[2]);
                    World chestWorld2 = getChestWorld();
                    if (rawSlot >= 27) {
                        y3++;
                    }
                    ((Chest)chestWorld2.getBlockAt(x3, y3, z3).getState()).getInventory().setItem(rawSlot < 27 ? rawSlot : rawSlot % 27, null);
                    List<Integer> odds4 = config.getIntegerList(String.valueOf(key) + ".odds");
                    List<Integer> notice3 = config.getIntegerList(String.valueOf(key) + ".notice");
                    odds4.set(rawSlot, -1);
                    notice3.set(rawSlot, 0);
                    config.set(String.valueOf(key) + ".odds", odds4);
                    config.set(String.valueOf(key) + ".notice", notice3);
                    Config.save(config, "BetterLottery");
                    settingGui(player, key);
                    return;
                }
                return;
            }
            player.sendMessage("[§aBetterLotteryReload§f] §c移除失败§e>>>必须先关闭奖池才能进行操作");
        }
    }

    public static void setOdds(Player player, String message) {
        String playerName = player.getName();
        int rawSlot = (Integer) SETTING_ODDS_OPS.get(playerName);
        FileConfiguration config = Config.load("BetterLottery");
        String key = ((InventoryInfo) SETTING_OPS.get(player.getName())).getKey();
        try {
            int odds = Integer.parseInt(message);
            if (odds < 0) {
                throw new Exception();
            }
            List<Integer> oddsList = config.getIntegerList(String.valueOf(key) + ".odds");
            oddsList.set(rawSlot, odds);
            config.set(String.valueOf(key) + ".odds", oddsList);
            Config.save(config, "BetterLottery");
            player.sendMessage("[§aBetterLotteryReload§f] §e奖品几率设置为：§f" + message);
            player.sendMessage("");
            SETTING_ODDS_OPS.remove(playerName);
            settingGui(player, key);
        } catch (Exception e) {
            player.sendMessage("[§aBetterLotteryReload§f] §c设置失败，必须输入正整数，可以是0");
        }
    }

    public static World getChestWorld() {
        return Bukkit.getWorld("BetterLotteryChest");
    }
}
