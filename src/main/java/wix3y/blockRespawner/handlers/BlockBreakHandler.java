package wix3y.blockRespawner.handlers;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import wix3y.blockRespawner.BlockRespawner;
import wix3y.blockRespawner.util.BlockInfo;
import wix3y.blockRespawner.util.ConfigUtil;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BlockBreakHandler implements Listener {
    private final BlockRespawner plugin;
    private final ConfigUtil configUtil;
    private final Map<String, BlockInfo> respawnBlocks = new ConcurrentHashMap<>();

    public BlockBreakHandler(BlockRespawner plugin, ConfigUtil configUtil) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    /**
     * Check if block broken was a block that should respawn and respawn it
     * If it was not a block to respawn but was in such a region, cancel the block break
     *
     * @param event the player block break event
     */
    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("blockrespawner.bypass")) {
            return;
        }

        Block brokenBlock = event.getBlock();
        Material material = brokenBlock.getType();
        Location blockLoc = brokenBlock.getLocation();

        String region = configUtil.getRegion(blockLoc);
        if (region.isEmpty()) {
            return;
        }

        if (configUtil.getMaterials(region).contains(material)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                brokenBlock.setType(configUtil.getReplaceMaterials(region), false);
                }, 1);
            scheduleRespawnBlock(material, brokenBlock.getLocation(), configUtil.getRespawnTime(region)*20);
        }
        else {
            // cancel block break events in region for unspecified block materials
            event.setCancelled(true);
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You can not break this block."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
        }
    }

    /**
     * Schedule block respawn after certain number of ticks
     *
     * @param material the block material
     * @param location the blocks location
     * @param ticks number of ticks until respawn
     */
    private void scheduleRespawnBlock(Material material, Location location, int ticks) {
        String id = UUID.randomUUID().toString();
        respawnBlocks.put(id, new BlockInfo(material, location));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Block block = location.getBlock();
            block.setType(material);
            if (block.getBlockData() instanceof Ageable ageable) {
                ageable.setAge(ageable.getMaximumAge());
                block.setBlockData(ageable);
            }
            respawnBlocks.remove(id);
        }, ticks);
    }

    /**
     * Save any blocks that have not yet been respawned to file so they can be respawned upon next server restart
     *
     */
    public void saveRespawnBlocks() {
        File dataFile = new File(plugin.getDataFolder(), "data.yml");

        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to create data file.");
                e.printStackTrace();
            }
        }

        FileConfiguration configuration = YamlConfiguration.loadConfiguration(dataFile);

        for (String id: respawnBlocks.keySet()) {
            configuration.set(id + ".material", respawnBlocks.get(id).material().toString());
            configuration.set(id + ".location.world", respawnBlocks.get(id).loc().getWorld().getName());
            configuration.set(id + ".location.x", respawnBlocks.get(id).loc().getBlockX());
            configuration.set(id + ".location.y", respawnBlocks.get(id).loc().getBlockY());
            configuration.set(id + ".location.z", respawnBlocks.get(id).loc().getBlockZ());
        }

        try {
            configuration.save(dataFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save blocks to respawn.");
            e.printStackTrace();
        }
    }
}