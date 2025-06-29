package wix3y.blockRespawner.util;

import org.bukkit.Location;

public record Area(String areaName, Location minLoc, Location maxLoc) {
}