package lopiktwo.github.dynamicLighting;

import com.destroystokyo.paper.utils.PaperPluginLogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class DynamicLighting extends JavaPlugin {

    private MainHandler handler;
    private final Logger logger = PaperPluginLogger.getLogger(this.getPluginMeta());
    private boolean isLightEnabled = true;

    @Override
    public void onEnable() {
        logger.info("started by lopiktwo");
        saveDefaultConfig();

        handler = new MainHandler(this);
        getServer().getPluginManager().registerEvents(handler, this);

        if (this.getCommand("dl") != null) {
            this.getCommand("dl").setExecutor(this);
        }
    }

    @Override
    public void onDisable() {
        if (handler != null) {
            getServer().getOnlinePlayers().forEach(player -> {

            });
        }
        logger.info("DynamicLighting off");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMenu(sender, label);
            return true;
        }
        if (!sender.hasPermission("dynamiclighting.admin")) {
            sender.sendMessage("§cYou do not have permission!");
            return true;
        }
        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "on":
                if (isLightEnabled) {
                    sender.sendMessage("§c[DynamicLighting] Light is already enabled!");
                    return true;
                }
                isLightEnabled = true;
                sender.sendMessage("§a[DynamicLighting] Light has been enabled!");
                break;

            case "off":
                if (!isLightEnabled) {
                    sender.sendMessage("§c[DynamicLighting] Light is already disabled!");
                    return true;
                }
                isLightEnabled = false;
                getServer().getOnlinePlayers().forEach(player -> {
                    try {
                        java.lang.reflect.Method removeLightMethod = handler.getClass().getDeclaredMethod("removeLight", org.bukkit.entity.Player.class);
                        removeLightMethod.setAccessible(true);
                        removeLightMethod.invoke(handler, player);
                    } catch (Exception e) {
                        logger.warning("Could not remove light for player " + player.getName());
                    }
                });
                sender.sendMessage("§c[DynamicLighting] Light has been disabled!");
                break;

            case "reload":

                reloadConfig();

                sender.sendMessage("§e[DynamicLighting] Configuration reloaded!");
                break;



            default:
                sender.sendMessage("§cUnknown command " + label + ". Use help.");
                break;
        }

        return true;
    }

    private void sendHelpMenu(CommandSender sender, String label) {
        sender.sendMessage("§7§m================§r §6DynamicLighting §7§m================");
        sender.sendMessage("§e/" + label + " help §7- Show this menu");
        sender.sendMessage("§e/" + label + " on §7- Enable dynamic lighting");
        sender.sendMessage("§e/" + label + " off §7- Disable dynamic lighting");


        if (sender.hasPermission("dynamiclighting.admin")) {
            sender.sendMessage("§e/" + label + " reload §7- Reload the plugin");
        }
        sender.sendMessage("§7§m=================================================");
    }
}