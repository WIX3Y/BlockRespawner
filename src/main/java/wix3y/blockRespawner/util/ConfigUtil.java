package wix3y.blockRespawner.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import wix3y.blockRespawner.BlockRespawner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigUtil {
    private final Map<String, List<Material>> materials = new ConcurrentHashMap<>();
    private final Map<String, Material> replaceMaterials = new ConcurrentHashMap<>();
    private final Map<String, Integer> respawnTimes = new ConcurrentHashMap<>();
    private final List<Area> areaRanges = new ArrayList<>();

    public ConfigUtil(BlockRespawner plugin) {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();

        ConfigurationSection section = config.getConfigurationSection("Areas");
        List<String> areas = new ArrayList<>(section.getKeys(false));
        for (String area : areas) {
            String basePath = "Areas." + area;
            if (config.contains(basePath + ".Materials") &&
                    config.contains(basePath + ".ReplaceMaterial") &&
                    config.contains(basePath + ".RespawnTime") &&
                    config.contains(basePath + ".World") &&
                    config.contains(basePath + ".AreaMax") &&
                    config.contains(basePath + ".AreaMin")) {

                List<String> materialNames = config.getStringList(basePath + ".Materials");
                List<Material> mats = new ArrayList<>();
                for (String materialName: materialNames) {
                    mats.add(Material.valueOf(materialName));
                }

                materials.put(area.toLowerCase(), mats);
                replaceMaterials.put(area.toLowerCase(), Material.getMaterial(config.getString(basePath + ".ReplaceMaterial")));
                respawnTimes.put(area.toLowerCase(), config.getInt(basePath + ".RespawnTime"));

                String worldName = config.getString(basePath + ".World");
                World world = Bukkit.getWorld(worldName);
                List<Integer> locs1 = config.getIntegerList(basePath + ".AreaMax");
                List<Integer> locs2 = config.getIntegerList(basePath + ".AreaMin");

                Location maxLoc = new Location(world, Math.max(locs1.get(0), locs2.get(0)), Math.max(locs1.get(1), locs2.get(1)), Math.max(locs1.get(2), locs2.get(2)));
                Location minLoc = new Location(world, Math.min(locs1.get(0), locs2.get(0)), Math.min(locs1.get(1), locs2.get(1)), Math.min(locs1.get(2), locs2.get(2)));

                areaRanges.add(new Area(area.toLowerCase(), minLoc, maxLoc));
            }
            else {
                plugin.getLogger().warning("Missing entry in section " + area);
            }
        }
    }

    /**
     * Get the material for specified region
     *
     * @param region the region identifier
     * @return a material
     */
    public List<Material> getMaterials(String region) {
        return materials.get(region);
    }

    /**
     * Get the replace material for specified region
     *
     * @param region the region identifier
     * @return a material
     */
    public Material getReplaceMaterials(String region) {
        return replaceMaterials.get(region);
    }

    /**
     * Get the respawn time for specified region
     *
     * @param region the region identifier
     * @return th respawn time in seconds
     */
    public Integer getRespawnTime(String region) {
        return respawnTimes.get(region);
    }

    /**
     * Get list of all areas covering a location
     *
     * @param loc the location
     * @return a list of areas covering the location
     */
    public String getRegion(Location loc) {
        for (Area area: areaRanges) {
            if (loc.getWorld() != area.minLoc().getWorld()) {
                continue;
            }
            else if (loc.getBlockX() < area.minLoc().getBlockX() || loc.getBlockX() > area.maxLoc().getBlockX()) {
                continue;
            }
            else if (loc.getBlockY() < area.minLoc().getBlockY() || loc.getBlockY() > area.maxLoc().getBlockY()) {
                continue;
            }
            else if (loc.getBlockZ() < area.minLoc().getBlockZ() || loc.getBlockZ() > area.maxLoc().getBlockZ()) {
                continue;
            }
            return area.areaName();
        }
        return "";
    }
}