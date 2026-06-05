package lopiktwo.github.dynamicLighting;

import com.destroystokyo.paper.utils.PaperPluginLogger;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class DynamicLighting extends JavaPlugin {
    MainHandler handler = new MainHandler();
    Logger logger = PaperPluginLogger.getLogger(this.getPluginMeta());
    @Override
    public void onEnable() {
        logger.info("DynamicLighting start");
        getServer().getPluginManager().registerEvents(new MainHandler(),this);

    }


    @Override
    public void onDisable() {
        logger.info("DynamicLighting off");
    }
}
