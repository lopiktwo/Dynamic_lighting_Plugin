package lopiktwo.github.dynamicLighting;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainHandler implements Listener {
    public HashMap<Player,Block> list = new HashMap<>();
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        int newSlot = event.getNewSlot();


        ItemStack newItem = player.getInventory().getItem(newSlot);

        if ((newItem != null && !newItem.getType().isAir() && newItem.getType() == Material.TORCH) ||
        player.getInventory().getItemInOffHand().getType() == Material.TORCH) {
            Location loc = player.getLocation();
            Block block = loc.getBlock();
            if(!block.getType().isAir()) return;
            createLight(loc,15);
            list.put(player,block);
        }else {
            player.sendMessage("удаление");
            player.getLocation().getBlock().setType(Material.AIR);
            list.remove(player);
        }
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(!list.containsKey(player)) return;
        Location from = event.getFrom();
        Location to = event.getTo();


        if (to == null) return;
        if(from.getBlock().getType() == Material.AIR && to.getBlock().getType() == Material.AIR){

        }
        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()) {
            from.getBlock().setType(Material.AIR);
            createLight(to,15);
            player.sendMessage("Вы двинулись с места!");
        }
    }
    private void createLight(Location loc,int lvl) {
        Block block = loc.getBlock();
        block.setType(Material.LIGHT);


        if (block.getBlockData() instanceof Levelled) {
            Levelled levelled = (Levelled) block.getBlockData();
            levelled.setLevel(lvl);
            block.setBlockData(levelled);
        }
    }


}
