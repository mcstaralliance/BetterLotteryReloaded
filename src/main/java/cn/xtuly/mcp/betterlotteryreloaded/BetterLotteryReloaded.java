package cn.xtuly.mcp.betterlotteryreloaded;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterLotteryReloaded extends JavaPlugin {
    public static boolean debugMode = false;
    private static BetterLotteryReloaded instance;

    public static BetterLotteryReloaded getInstance(){
        return instance;
    }

    public void onEnable() {
        initConfig();
        initWorld();
        initEvent();
        getLogger().info("插件加载完毕,欢迎使用");
        instance=this;
    }

    private void initConfig() {
        Config.rootPath = getDataFolder().getPath();
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
    }

    public void initWorld() {
        WorldCreator creator = new WorldCreator("BetterLotteryChest");
        creator.generateStructures(true);
        creator.environment(Environment.NORMAL);
        creator.type(WorldType.FLAT);
        World world = creator.createWorld();
        world.setSpawnLocation(0, 5, 0);
        world.save();
        Bukkit.createWorld(creator);
    }

    private void initEvent() {
        getServer().getPluginManager().registerEvents(new BLREvent(), this);
        getServer().getScheduler().runTaskTimer(this, new LotteryHelper(), 0, 1);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!label.equalsIgnoreCase("bl") || !(sender instanceof Player)) {
            return false;
        }
        if (args.length < 1 || !args[0].equalsIgnoreCase("debug")) {
            GuiHelper.menuGui((Player) sender);
            return true;
        }
        debugMode = true;
        ((Player) sender).teleport(GuiHelper.getChestWorld().getSpawnLocation());
        return true;
    }
}