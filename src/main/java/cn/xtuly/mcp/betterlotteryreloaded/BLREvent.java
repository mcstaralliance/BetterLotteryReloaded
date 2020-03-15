package cn.xtuly.mcp.betterlotteryreloaded;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;


public class BLREvent implements Listener {
    @EventHandler
    public void playerPickupItem(EntityPickupItemEvent event) {
        if(event.getEntity() instanceof Player){
            Player player = (Player) event.getEntity();
            if (LotteryHelper.LOTTERY_PLAYERS.containsKey(player.getName()) && LotteryHelper.LOTTERY_PLAYERS.get(player.getName()).isRun()) {
                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void playerCloseInventory(InventoryCloseEvent event) {
        HumanEntity humanEntity = event.getPlayer();
        if (humanEntity instanceof Player) {
            Player player = (Player) humanEntity;
            if (LotteryHelper.LOTTERY_PLAYERS.containsKey(player.getName())) {
                PlayerLotteryInfo playerLotteryInfo = LotteryHelper.LOTTERY_PLAYERS.get(player.getName());
                if (playerLotteryInfo.isRun()) {
                    playerLotteryInfo.setAnimationEnded(true);
                    playerLotteryInfo.ended();
                }
            }
        }
    }

    @EventHandler
    public void playerQuitGame(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (LotteryHelper.LOTTERY_PLAYERS.containsKey(player.getName())) {
            PlayerLotteryInfo playerLotteryInfo = LotteryHelper.LOTTERY_PLAYERS.get(player.getName());
            if (playerLotteryInfo.isRun()) {
                playerLotteryInfo.setAnimationEnded(true);
                playerLotteryInfo.ended();
            }
        }
    }

    @EventHandler
    public void playerRightUseItem(PlayerInteractEvent event) {
        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
        if ((event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            LotteryHelper.lotteryDraw(event);
        }
    }

    @EventHandler
    public void playerPreJoinWorld(PlayerTeleportEvent event) {
        if (event.getTo().getWorld().getName().equals("BetterLotteryChest") && !BetterLotteryReloaded.debugMode) {
            event.getPlayer().sendMessage("[§aBetterLottery-Reloaded§f] §c该世界禁止进入，对~ OP也不行╮(￣▽￣\")╭");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerPreJoinWorld(PlayerPortalEvent event) {
        if (event.getTo().getWorld().getName().equals("BetterLotteryChest") && !BetterLotteryReloaded.debugMode) {
            event.getPlayer().sendMessage("[§aBetterLottery-Reloaded§f] §c该世界禁止进入，对~ OP也不行╮(￣▽￣\")╭");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void guiClicked(InventoryClickEvent event) {
        if ((event.getWhoClicked() instanceof Player) && event.getRawSlot() >= 0) {
            if (event.getInventory().getTitle().startsWith(GuiHelper.TITLE_PREFIX)) {
                GuiHelper.playerClickedGui(event);
            } else if (event.getInventory().getTitle().startsWith(LotteryHelper.TITLE_PREFIX)) {
                LotteryHelper.lotteryDrawClicked(event);
            }
        }
    }

    @EventHandler
    public void opInputMessage(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (GuiHelper.CREATING_OPS.containsKey(player.getName())) {
            GuiHelper.createPool(player, event.getMessage());
            event.setCancelled(true);
        } else if (GuiHelper.SETTING_ODDS_OPS.containsKey(player.getName())) {
            GuiHelper.setOdds(player, event.getMessage());
            event.setCancelled(true);
        }
    }

}
