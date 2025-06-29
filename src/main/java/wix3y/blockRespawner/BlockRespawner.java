package wix3y.blockRespawner;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import wix3y.blockRespawner.handlers.BlockBreakHandler;
import wix3y.blockRespawner.util.ConfigUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class BlockRespawner extends JavaPlugin {
    private BlockBreakHandler blockBreakHandler;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigUtil configUtil = new ConfigUtil(this);

        File dataFile = new File(getDataFolder(), "data.yml");
        respawnBlocks(dataFile);
        // TODO respawn any remaining blocks from restart

        blockBreakHandler = new BlockBreakHandler(this, configUtil);

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize("      <gradient:#44AA00:#BBFF99:#44AA00>Block Respawner</gradient>"));
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize("          <gray>v1.0.0"));
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize("          <green>Enabled"));
        Bukkit.getConsoleSender().sendMessage("");
    }

    @Override
    public void onDisable() {
        blockBreakHandler.saveRespawnBlocks();
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize("      <gradient:#44AA00:#BBFF99:#44AA00>Block Respawner</gradient>"));
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize("          <gray>v1.0.0"));
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize("         <red>Disabled"));
        Bukkit.getConsoleSender().sendMessage("");
    }

    private void respawnBlocks(File dataFile) {
        if (dataFile.exists()) {
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(dataFile);

            ConfigurationSection section = configuration.getConfigurationSection("");
            if (section == null) {
                return;
            }

            List<String> ids = new ArrayList<>(section.getKeys(false));
            for (String id : ids) {
                World world = Bukkit.getWorld(configuration.getString(id + ".location.world"));
                Location location = new Location(world, configuration.getInt(id + ".location.x"), configuration.getInt(id + ".location.y"), configuration.getInt(id + ".location.z"));
                location.getBlock().setType(Material.valueOf(configuration.getString(id + ".material")));
            }

            configuration = new YamlConfiguration();
            try {
                configuration.save(dataFile);
            } catch (Exception e) {
                this.getLogger().severe("Failed to clear data file.");
                e.printStackTrace();
            }
        }
    }
}
