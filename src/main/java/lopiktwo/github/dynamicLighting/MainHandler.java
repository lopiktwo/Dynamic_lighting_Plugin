package lopiktwo.github.dynamicLighting;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class MainHandler implements Listener {

    private final JavaPlugin plugin;
    // Использование UUID вместо объекта Player предотвращает утечки памяти
    private final HashMap<UUID, Block> activeLights = new HashMap<>();

    // Конструктор для доступа к вашему главному классу (нужен для шедулера)
    public MainHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

        // Проверяем, будет ли в руке факел после завершения этого события
        boolean holdingTorch = isTorch(newItem) || isTorch(player.getInventory().getItemInOffHand());

        updateLightPresence(player, holdingTorch);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        // Срабатывает только если игрок действительно перешагнул в новый блок
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        if (!activeLights.containsKey(player.getUniqueId())) return;

        // Дополнительно проверяем, держит ли он всё ещё факел при движении
        if (isHoldingTorch(player)) {
            removeLight(player);
            createLight(player, to.getBlock());
        } else {
            removeLight(player);
        }
    }

    // --- Дополнительные события для отлова всех ситуаций ---

    @EventHandler
    public void onHandSwap(PlayerSwapHandItemsEvent event) {
        // Срабатывает при нажатии клавиши 'F' (перекладывание в левую руку)
        updateLightPresence(event.getPlayer(), isTorch(event.getMainHandItem()) || isTorch(event.getOffHandItem()));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Срабатывает, если игрок перекладывает факел мышкой внутри инвентаря
        if (event.getWhoClicked() instanceof Player player) {
            // Выполняем проверку на 1 тик позже, чтобы инвентарь успел обновиться
            player.getServer().getScheduler().runTask(plugin, () -> {
                updateLightPresence(player, isHoldingTorch(player));
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Обязательно убираем свет и чистим мапу, когда игрок выходить с сервера
        removeLight(event.getPlayer());
    }



    private void updateLightPresence(Player player, boolean shouldHaveLight) {
        if (shouldHaveLight) {
            Block currentBlock = player.getLocation().getBlock();

            if (!activeLights.containsKey(player.getUniqueId()) || !activeLights.get(player.getUniqueId()).equals(currentBlock)) {
                removeLight(player);
                createLight(player, currentBlock);
            }
        } else {
            removeLight(player);
        }
    }

    private void createLight(Player player, Block block) {

        if (block.getType() != Material.AIR && block.getType() != Material.LIGHT) return;

        block.setType(Material.LIGHT);
        if (block.getBlockData() instanceof Levelled levelled) {
            levelled.setLevel(15);
            block.setBlockData(levelled);
        }
        activeLights.put(player.getUniqueId(), block);
    }

    private void removeLight(Player player) {
        Block oldBlock = activeLights.remove(player.getUniqueId());

        if (oldBlock != null && oldBlock.getType() == Material.LIGHT) {
            oldBlock.setType(Material.AIR);
        }
    }

    private boolean isHoldingTorch(Player player) {
        return isTorch(player.getInventory().getItemInMainHand()) || isTorch(player.getInventory().getItemInOffHand());
    }

    private boolean isTorch(ItemStack item) {
        return item != null && item.getType() == Material.TORCH;
    }
}