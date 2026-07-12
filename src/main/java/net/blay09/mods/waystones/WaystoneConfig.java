package net.blay09.mods.waystones;

import net.xiaoyu233.fml.FishModLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class WaystoneConfig {
    private static final File FILE = new File(FishModLoader.CONFIG_DIR, "waystones.properties");
    private static final Properties VALUES = new Properties();

    public static boolean interDimension = true;
    public static boolean globalInterDimension = true;
    public static boolean particles = true;
    public static boolean sounds = true;
    public static boolean disableTeleportSound = false;
    public static boolean disableTextGlow = false;
    public static boolean allowReturnScrolls = true;
    public static boolean lootReturnScrolls = true;
    public static boolean allowWarpStone = true;
    public static boolean creativeModeOnly = false;
    public static boolean invulnerableWaystones = false;
    public static boolean setSpawnPoint = false;
    public static boolean globalNoCooldown = false;
    public static boolean teleportButton = false;
    public static boolean teleportButtonReturnOnly = false;
    public static boolean showNametag = false;
    public static boolean menusPauseGame = false;
    public static boolean enableWorldgen = true;
    public static boolean villageNamesCompat = true;
    public static boolean showCooldownOnWaystone = true;
    public static boolean flatInventoryIcon = false;
    public static boolean disableWaystoneDrops = false;
    public static boolean debugMode = false;
    public static boolean journeyMapWaypoints = true;
    public static boolean journeyMapWaypointRandomColor = true;
    public static String journeyMapWaypointColor = "7FDBFF";
    public static int journeyMapWaypointYOffset = 2;
    public static boolean xaeroMinimapWaypoints = true;
    public static boolean xaeroMinimapWaypointRandomColor = true;
    public static int xaeroMinimapWaypointColor = 11;
    public static int xaeroMinimapWaypointYOffset = 2;
    public static int teleportButtonCooldownSeconds = 300;
    public static int teleportButtonX = 60;
    public static int teleportButtonY = 60;
    public static int warpStoneCooldownSeconds = 300;
    public static int xpBaseCost = 5;
    public static int xpBlocksPerLevel = 100;
    public static int xpCrossDimCost = 5;
    public static int sortingMode = 0;
    public static float waystoneLightLevel = 0.5F;
    public static String[] overlayClipBounds = {
            "variant=stone;lower=-22;upper=-44",
            "variant=sandstone;lower=-22;upper=-38",
            "variant=mossy;lower=-24;upper=-44",
            "variant=stonebrick;lower=-26;upper=-44",
            "variant=mossy_stonebrick;lower=-26;upper=-44",
            "variant=nether;lower=-24;upper=-44",
            "variant=end;lower=-24;upper=-42"
    };
    public static String[] sandyWaystonePathBlocks = {"minecraft:sandstone"};
    public static String[] mossyWaystonePathBlocks = {};
    public static String[] structureWaystoneRules = {
            "structure=village;chance=1;type=auto",
            "structure=temple_desert;chance=1;type=sandy",
            "structure=temple_jungle;chance=1;type=auto",
            "structure=stronghold;chance=1;type=auto",
            "structure=fortress;chance=1;type=nether",
            "structure=end_spike;chance=1;type=end",
            "structure=world_spawn;chance=1;type=stone;dimensionWhitelist=0"
    };

    private WaystoneConfig() {
    }

    public static void load() {
        VALUES.clear();
        if (FILE.isFile()) {
            try (FileInputStream input = new FileInputStream(FILE)) {
                VALUES.load(input);
            } catch (IOException exception) {
                Waystones.LOGGER.error("Failed to read {}", FILE, exception);
            }
        }

        interDimension = booleanValue(VALUES, "interDimension", interDimension);
        globalInterDimension = booleanValue(VALUES, "globalInterDimension", globalInterDimension);
        particles = booleanValue(VALUES, "particles", particles);
        if (VALUES.containsKey("disableParticles")) {
            particles = !booleanValue(VALUES, "disableParticles", false);
        }
        sounds = booleanValue(VALUES, "sounds", sounds);
        disableTeleportSound = booleanValue(VALUES, "disableTeleportSound", disableTeleportSound);
        disableTextGlow = booleanValue(VALUES, "disableTextGlow", disableTextGlow);
        allowReturnScrolls = booleanValue(VALUES, "allowReturnScrolls", allowReturnScrolls);
        lootReturnScrolls = booleanValue(VALUES, "lootReturnScrolls", lootReturnScrolls);
        allowWarpStone = booleanValue(VALUES, "allowWarpStone", allowWarpStone);
        creativeModeOnly = booleanValue(VALUES, "creativeModeOnly", creativeModeOnly);
        invulnerableWaystones = booleanValue(VALUES, "invulnerableWaystones", invulnerableWaystones);
        setSpawnPoint = booleanValue(VALUES, "setSpawnPoint", setSpawnPoint);
        globalNoCooldown = booleanValue(VALUES, "globalNoCooldown", globalNoCooldown);
        teleportButton = booleanValue(VALUES, "teleportButton", teleportButton);
        teleportButtonReturnOnly = booleanValue(VALUES, "teleportButtonReturnOnly", teleportButtonReturnOnly);
        showNametag = booleanValue(VALUES, "showNametag", showNametag);
        menusPauseGame = booleanValue(VALUES, "menusPauseGame", menusPauseGame);
        enableWorldgen = booleanValue(VALUES, "enableWorldgen", enableWorldgen);
        villageNamesCompat = booleanValue(VALUES, "villageNamesCompat", villageNamesCompat);
        showCooldownOnWaystone = booleanValue(VALUES, "showCooldownOnWaystone", showCooldownOnWaystone);
        flatInventoryIcon = booleanValue(VALUES, "flatInventoryIcon", flatInventoryIcon);
        disableWaystoneDrops = booleanValue(VALUES, "disableWaystoneDrops", disableWaystoneDrops);
        debugMode = booleanValue(VALUES, "debugMode", debugMode);
        journeyMapWaypoints = booleanValue(VALUES, "journeyMapWaypoints", journeyMapWaypoints);
        journeyMapWaypointRandomColor = booleanValue(VALUES, "journeyMapWaypointRandomColor", journeyMapWaypointRandomColor);
        journeyMapWaypointColor = VALUES.getProperty("journeyMapWaypointColor", journeyMapWaypointColor);
        journeyMapWaypointYOffset = integerValue(VALUES, "journeyMapWaypointYOffset", journeyMapWaypointYOffset,
                Integer.MIN_VALUE, Integer.MAX_VALUE);
        xaeroMinimapWaypoints = booleanValue(VALUES, "xaeroMinimapWaypoints", xaeroMinimapWaypoints);
        xaeroMinimapWaypointRandomColor = booleanValue(VALUES, "xaeroMinimapWaypointRandomColor", xaeroMinimapWaypointRandomColor);
        xaeroMinimapWaypointColor = integerValue(VALUES, "xaeroMinimapWaypointColor", xaeroMinimapWaypointColor, 0, 15);
        xaeroMinimapWaypointYOffset = integerValue(VALUES, "xaeroMinimapWaypointYOffset", xaeroMinimapWaypointYOffset,
                Integer.MIN_VALUE, Integer.MAX_VALUE);
        teleportButtonCooldownSeconds = integerValue(VALUES, "teleportButtonCooldownSeconds",
                teleportButtonCooldownSeconds, 0, 86_400);
        teleportButtonX = integerValue(VALUES, "teleportButtonX", teleportButtonX, -250, 250);
        teleportButtonY = integerValue(VALUES, "teleportButtonY", teleportButtonY, -250, 250);
        warpStoneCooldownSeconds = integerValue(VALUES, "warpStoneCooldownSeconds",
                warpStoneCooldownSeconds, 0, 86_400);
        xpBaseCost = integerValue(VALUES, "xpBaseCost", xpBaseCost, -1, Integer.MAX_VALUE);
        xpBlocksPerLevel = integerValue(VALUES, "xpBlocksPerLevel", xpBlocksPerLevel, 0, Integer.MAX_VALUE);
        xpCrossDimCost = integerValue(VALUES, "xpCrossDimCost", xpCrossDimCost, 0, Integer.MAX_VALUE);
        sortingMode = integerValue(VALUES, "sortingMode", sortingMode, 0, 1);
        waystoneLightLevel = floatValue(VALUES, "waystoneLightLevel", waystoneLightLevel, 0.0F, 1.0F);
        overlayClipBounds = stringArrayValue(VALUES, "overlayClipBounds", overlayClipBounds);
        sandyWaystonePathBlocks = stringArrayValueAllowEmpty(VALUES,
                "sandyWaystonePathBlocks", sandyWaystonePathBlocks);
        mossyWaystonePathBlocks = stringArrayValueAllowEmpty(VALUES,
                "mossyWaystonePathBlocks", mossyWaystonePathBlocks);
        structureWaystoneRules = stringArrayValue(VALUES, "structureWaystoneRules", structureWaystoneRules);

        VALUES.setProperty("interDimension", Boolean.toString(interDimension));
        VALUES.setProperty("globalInterDimension", Boolean.toString(globalInterDimension));
        VALUES.setProperty("particles", Boolean.toString(particles));
        VALUES.setProperty("disableParticles", Boolean.toString(!particles));
        VALUES.setProperty("sounds", Boolean.toString(sounds));
        VALUES.setProperty("disableTeleportSound", Boolean.toString(disableTeleportSound));
        VALUES.setProperty("disableTextGlow", Boolean.toString(disableTextGlow));
        VALUES.setProperty("allowReturnScrolls", Boolean.toString(allowReturnScrolls));
        VALUES.setProperty("lootReturnScrolls", Boolean.toString(lootReturnScrolls));
        VALUES.setProperty("allowWarpStone", Boolean.toString(allowWarpStone));
        VALUES.setProperty("creativeModeOnly", Boolean.toString(creativeModeOnly));
        VALUES.setProperty("invulnerableWaystones", Boolean.toString(invulnerableWaystones));
        VALUES.setProperty("setSpawnPoint", Boolean.toString(setSpawnPoint));
        VALUES.setProperty("globalNoCooldown", Boolean.toString(globalNoCooldown));
        VALUES.setProperty("teleportButton", Boolean.toString(teleportButton));
        VALUES.setProperty("teleportButtonReturnOnly", Boolean.toString(teleportButtonReturnOnly));
        VALUES.setProperty("showNametag", Boolean.toString(showNametag));
        VALUES.setProperty("menusPauseGame", Boolean.toString(menusPauseGame));
        VALUES.setProperty("flatInventoryIcon", Boolean.toString(flatInventoryIcon));
        VALUES.setProperty("enableWorldgen", Boolean.toString(enableWorldgen));
        VALUES.setProperty("villageNamesCompat", Boolean.toString(villageNamesCompat));
        VALUES.setProperty("showCooldownOnWaystone", Boolean.toString(showCooldownOnWaystone));
        VALUES.setProperty("flatInventoryIcon", Boolean.toString(flatInventoryIcon));
        VALUES.setProperty("disableWaystoneDrops", Boolean.toString(disableWaystoneDrops));
        VALUES.setProperty("debugMode", Boolean.toString(debugMode));
        VALUES.setProperty("journeyMapWaypoints", Boolean.toString(journeyMapWaypoints));
        VALUES.setProperty("journeyMapWaypointRandomColor", Boolean.toString(journeyMapWaypointRandomColor));
        VALUES.setProperty("journeyMapWaypointColor", journeyMapWaypointColor);
        VALUES.setProperty("journeyMapWaypointYOffset", Integer.toString(journeyMapWaypointYOffset));
        VALUES.setProperty("xaeroMinimapWaypoints", Boolean.toString(xaeroMinimapWaypoints));
        VALUES.setProperty("xaeroMinimapWaypointRandomColor", Boolean.toString(xaeroMinimapWaypointRandomColor));
        VALUES.setProperty("xaeroMinimapWaypointColor", Integer.toString(xaeroMinimapWaypointColor));
        VALUES.setProperty("xaeroMinimapWaypointYOffset", Integer.toString(xaeroMinimapWaypointYOffset));
        VALUES.setProperty("teleportButtonCooldownSeconds", Integer.toString(teleportButtonCooldownSeconds));
        VALUES.setProperty("teleportButtonX", Integer.toString(teleportButtonX));
        VALUES.setProperty("teleportButtonY", Integer.toString(teleportButtonY));
        VALUES.setProperty("warpStoneCooldownSeconds", Integer.toString(warpStoneCooldownSeconds));
        VALUES.setProperty("xpBaseCost", Integer.toString(xpBaseCost));
        VALUES.setProperty("xpBlocksPerLevel", Integer.toString(xpBlocksPerLevel));
        VALUES.setProperty("xpCrossDimCost", Integer.toString(xpCrossDimCost));
        VALUES.setProperty("sortingMode", Integer.toString(sortingMode));
        VALUES.setProperty("waystoneLightLevel", Float.toString(waystoneLightLevel));
        VALUES.setProperty("overlayClipBounds", String.join("|", overlayClipBounds));
        VALUES.setProperty("sandyWaystonePathBlocks", String.join("|", sandyWaystonePathBlocks));
        VALUES.setProperty("mossyWaystonePathBlocks", String.join("|", mossyWaystonePathBlocks));
        VALUES.setProperty("structureWaystoneRules", String.join("|", structureWaystoneRules));
        VALUES.putIfAbsent("Server Waystones", "");
        save();
    }

    public static synchronized List<WaystoneEntry> configuredGlobalWaystones() {
        List<WaystoneEntry> result = new ArrayList<>();
        String raw = VALUES.getProperty("Server Waystones", "");
        if (raw.isEmpty()) {
            return result;
        }
        for (String encoded : raw.split("\\n")) {
            String[] parts = encoded.split("\\u00a7", 3);
            if (parts.length != 3) {
                continue;
            }
            String[] position = parts[2].split(",", 3);
            if (position.length != 3) {
                continue;
            }
            try {
                result.add(new WaystoneEntry(parts[0], Integer.parseInt(parts[1]),
                        Integer.parseInt(position[0]), Integer.parseInt(position[1]),
                        Integer.parseInt(position[2]), true));
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    public static synchronized void storeGlobalWaystones(Collection<WaystoneEntry> entries) {
        List<String> encoded = new ArrayList<>();
        for (WaystoneEntry entry : entries) {
            encoded.add(entry.name() + "\u00a7" + entry.dimension() + "\u00a7"
                    + entry.x() + "," + entry.y() + "," + entry.z());
        }
        VALUES.setProperty("Server Waystones", String.join("\n", encoded));
        save();
    }

    private static synchronized void save() {
        File parent = FILE.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (FileOutputStream output = new FileOutputStream(FILE)) {
            VALUES.store(output, "Waystones-MITE configuration");
        } catch (IOException exception) {
            Waystones.LOGGER.error("Failed to write {}", FILE, exception);
        }
    }

    public static void saveCurrent() {
        VALUES.setProperty("sortingMode", Integer.toString(sortingMode));
        VALUES.setProperty("showNametag", Boolean.toString(showNametag));
        VALUES.setProperty("particles", Boolean.toString(particles));
        VALUES.setProperty("disableParticles", Boolean.toString(!particles));
        VALUES.setProperty("sounds", Boolean.toString(sounds));
        VALUES.setProperty("disableTeleportSound", Boolean.toString(disableTeleportSound));
        VALUES.setProperty("showCooldownOnWaystone", Boolean.toString(showCooldownOnWaystone));
        VALUES.setProperty("menusPauseGame", Boolean.toString(menusPauseGame));
        save();
    }

    private static boolean booleanValue(Properties values, String key, boolean fallback) {
        String value = values.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    private static int integerValue(Properties values, String key, int fallback, int min, int max) {
        try {
            int value = Integer.parseInt(values.getProperty(key, Integer.toString(fallback)));
            return Math.max(min, Math.min(max, value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static float floatValue(Properties values, String key, float fallback, float min, float max) {
        try {
            float value = Float.parseFloat(values.getProperty(key, Float.toString(fallback)));
            return Math.max(min, Math.min(max, value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String[] stringArrayValue(Properties values, String key, String[] fallback) {
        String value = values.getProperty(key);
        return value == null || value.isBlank() ? fallback : value.split("\\|");
    }

    private static String[] stringArrayValueAllowEmpty(Properties values, String key, String[] fallback) {
        String value = values.getProperty(key);
        return value == null ? fallback : value.isBlank() ? new String[0] : value.split("\\|");
    }
}
