package lopiktwo.github.dynamicLighting;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class MainHandler implements Listener {

    private final JavaPlugin plugin;
    private final DynamicLighting dynamicLighting;
    private final HashMap<UUID, BlockState> activeLights = new HashMap<>();

    public MainHandler(JavaPlugin plugin,DynamicLighting o) {
        this.plugin = plugin;
        this.dynamicLighting = o;

    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        if(!(dynamicLighting.getIsEnable())) return;
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            updateLightPresence(player, isHoldingTorch(player));
        });
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(!(dynamicLighting.getIsEnable())) return;
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        updateLightPresence(player, isHoldingTorch(player));
    }

    @EventHandler
    public void onHandSwap(PlayerSwapHandItemsEvent event) {
        if(!(dynamicLighting.getIsEnable())) return;
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            updateLightPresence(player, isHoldingTorch(player));
        });
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(!(dynamicLighting.getIsEnable())) return;
        if (event.getWhoClicked() instanceof Player player) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                updateLightPresence(player, isHoldingTorch(player));
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(!(dynamicLighting.getIsEnable())) return;
        removeLight(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if(!(dynamicLighting.getIsEnable())) return;
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            updateLightPresence(player, isHoldingTorch(player));
        });
    }

    private void updateLightPresence(Player player, boolean shouldHaveLight) {
        Block currentBlock = player.getLocation().getBlock();
        BlockState currentLightState = activeLights.get(player.getUniqueId());

        if (shouldHaveLight) {
            int targetLightLevel = getLightLevel(player);

            if (currentLightState == null || !currentLightState.getBlock().equals(currentBlock)) {
                removeLight(player);
                createLight(player, currentBlock, targetLightLevel);
            } else {
                Block block = currentLightState.getBlock();
                if (block.getBlockData() instanceof Light lightData) {
                    if (lightData.getLevel() != targetLightLevel) {
                        removeLight(player);
                        createLight(player, currentBlock, targetLightLevel);
                    }
                }
            }
        } else {
            removeLight(player);
        }
    }

    private void createLight(Player player, Block block, int lightLevel) {
        if (!block.getType().isAir() && block.getType() != Material.CAVE_AIR && block.getType() != Material.VOID_AIR) {
            return;
        }

        if (lightLevel <= 0) return;

        activeLights.put(player.getUniqueId(), block.getState());

        block.setType(Material.LIGHT, false);
        if (block.getBlockData() instanceof Light lightData) {
            lightData.setLevel(lightLevel);
            block.setBlockData(lightData, true);
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
        if (item == null || item.getType().isAir()) return false;
        Material type = item.getType();
        return type == Material.TORCH
                || type == Material.SEA_LANTERN
                || type == Material.LANTERN
                || type == Material.SOUL_LANTERN
                || type == Material.OCHRE_FROGLIGHT
                || type == Material.VERDANT_FROGLIGHT
                || type == Material.PEARLESCENT_FROGLIGHT
                || type == Material.GLOWSTONE
                || type == Material.GLOW_LICHEN
                || type == Material.REDSTONE_TORCH


                ;
    }

    private int getLightLevel(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (isLightAble(mainHand)) {
            return getConfigLightValue(mainHand.getType());
        } else if (isLightAble(offHand)) {
            return getConfigLightValue(offHand.getType());
        }
        return 0;
    }

    private int getConfigLightValue(Material material) {
        return switch (material) {
            case TORCH, LANTERN, OCHRE_FROGLIGHT, VERDANT_FROGLIGHT, PEARLESCENT_FROGLIGHT, GLOWSTONE -> plugin.getConfig().getInt(material.name(), 15);
            case SOUL_LANTERN -> plugin.getConfig().getInt(material.name(), 10);
            case SEA_LANTERN -> plugin.getConfig().getInt(material.name(), 15);
            case GLOW_LICHEN, REDSTONE_TORCH -> plugin.getConfig().getInt(material.name(), 7);
            default -> 0;
        };
}}