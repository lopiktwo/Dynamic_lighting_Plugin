package lopiktwo.github.dynamicLighting;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

import static org.bukkit.Material.LANTERN;

public class MainHandler implements Listener {

    private final JavaPlugin plugin;
    private final HashMap<UUID, BlockState> activeLights = new HashMap<>();
    public int lvl_light;

    public MainHandler(JavaPlugin plugin) {
        this.plugin = plugin;

    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

        boolean holdingTorch = isLightAble(newItem) || isLightAble(player.getInventory().getItemInOffHand());
        updateLightPresence(player, holdingTorch);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if ((from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())) {
            return;
        }

        Player player = event.getPlayer();
        boolean holdingTorch = isHoldingTorch(player);

        if (holdingTorch) {
            Block targetBlock = to.getBlock();
            BlockState currentLightState = activeLights.get(player.getUniqueId());

            if (currentLightState == null || !currentLightState.getBlock().equals(targetBlock)) {
                removeLight(player);
                createLight(player, targetBlock);
            }
        } else {
            removeLight(player);
        }
    }

    @EventHandler
    public void onHandSwap(PlayerSwapHandItemsEvent event) {
        updateLightPresence(event.getPlayer(), isLightAble(event.getMainHandItem()) || isLightAble(event.getOffHandItem()));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            player.getServer().getScheduler().runTask(plugin, () -> {
                updateLightPresence(player, isHoldingTorch(player));
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeLight(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        removeLight(event.getPlayer());
    }

    private void updateLightPresence(Player player, boolean shouldHaveLight) {
        if (shouldHaveLight) {
            Block currentBlock = player.getLocation().getBlock();
            BlockState currentLightState = activeLights.get(player.getUniqueId());

            if (currentLightState == null || !currentLightState.getBlock().equals(currentBlock)) {
                removeLight(player);
                createLight(player, currentBlock);
            }
        } else {
            removeLight(player);
        }
    }

    private void createLight(Player player, Block block) {
        if (!block.getType().isAir() || block.getType().isSolid()) return;

        activeLights.put(player.getUniqueId(), block.getState());

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (isLightAble(mainHand)) {
            lvl_light = howMuchLight(mainHand);
        } else if (isLightAble(offHand)) {
            lvl_light = howMuchLight(offHand);
        } else {
            return;
        }

        block.setType(Material.LIGHT);
        if (block.getBlockData() instanceof Levelled levelled) {
            levelled.setLevel(lvl_light);
            block.setBlockData(levelled);
        }
    }

    private void removeLight(Player player) {
        BlockState originalState = activeLights.remove(player.getUniqueId());

        if (originalState != null) {
            originalState.update(true, false);
        }
    }

    private boolean isHoldingTorch(Player player) {
        return isLightAble(player.getInventory().getItemInMainHand()) || isLightAble(player.getInventory().getItemInOffHand());
    }

    private boolean isLightAble(ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();
        return type == Material.TORCH
                || type == Material.SEA_LANTERN
                || type == Material.LANTERN
                || type == Material.SOUL_LANTERN
                || type == Material.OCHRE_FROGLIGHT
                || type == Material.VERDANT_FROGLIGHT
                || type == Material.PEARLESCENT_FROGLIGHT;
    }

    private int howMuchLight(ItemStack itemStack) {
        ItemStack item = itemStack;
        Material material = item.getType();
        switch (material) {
            case TORCH:
                return plugin.getConfig().getInt("TORCH", 15);
            case LANTERN:
                return plugin.getConfig().getInt("LANTERN", 15);
            case SEA_LANTERN:
                return plugin.getConfig().getInt("SEA_LANTERN", 15);
            case SOUL_LANTERN:
                return plugin.getConfig().getInt("SOUL_LANTERN", 15);
            case OCHRE_FROGLIGHT:
                return plugin.getConfig().getInt("OCHRE_FROGLIGHT", 15);
            case VERDANT_FROGLIGHT:
                return plugin.getConfig().getInt("VERDANT_FROGLIGHT", 15);
            case PEARLESCENT_FROGLIGHT:
                return plugin.getConfig().getInt("PEARLESCENT_FROGLIGHT", 15);

            default:
                return 0;
        }
    }

    public void setLvl(int number) {
        this.lvl_light = number;
    }
}
