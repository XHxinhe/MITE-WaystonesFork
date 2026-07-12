package net.blay09.mods.waystones.compat;

import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.Waystones;
import net.xiaoyu233.fml.FishModLoader;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class WaystoneMapCompat {
    private WaystoneMapCompat() {
    }

    public static void addOrUpdate(String name, int dimension, int x, int y, int z) {
        updateJourneyMap(null, name, dimension, x, y, z);
        updateXaero(null, name, x, y, z);
    }

    public static void rename(String oldName, String newName, int dimension, int x, int y, int z) {
        updateJourneyMap(oldName, newName, dimension, x, y, z);
        updateXaero(oldName, newName, x, y, z);
    }

    private static void updateJourneyMap(String oldName, String name, int dimension, int x, int y, int z) {
        if (!WaystoneConfig.journeyMapWaypoints || !FishModLoader.hasMod("journeymap")) {
            return;
        }
        try {
            Class<?> dataClass = Class.forName("journeymap.client.data.WaypointsData");
            if (!(Boolean) dataClass.getMethod("isManagerEnabled").invoke(null)) {
                return;
            }
            Class<?> storeClass = Class.forName("journeymap.client.waypoint.WaypointStore");
            Object store = storeClass.getMethod("instance").invoke(null);
            if (store == null || !(Boolean) storeClass.getMethod("hasLoaded").invoke(store)) {
                return;
            }
            Object waypoint = findByName((Collection<?>) storeClass.getMethod("getAll").invoke(store),
                    oldName == null ? name : oldName);
            int color = journeyColor(name);
            if (oldName != null && waypoint == null) {
                return;
            }
            if (waypoint == null) {
                Class<?> waypointClass = Class.forName("journeymap.client.model.Waypoint");
                @SuppressWarnings("rawtypes")
                Class<? extends Enum> typeClass = Class.forName("journeymap.client.model.Waypoint$Type")
                        .asSubclass(Enum.class);
                Constructor<?> constructor = waypointClass.getConstructor(String.class, int.class, int.class,
                        int.class, Color.class, typeClass, Integer.class);
                @SuppressWarnings({"unchecked", "rawtypes"})
                Object normal = Enum.valueOf(typeClass, "Normal");
                waypoint = constructor.newInstance(name, x, y + WaystoneConfig.journeyMapWaypointYOffset,
                        z, new Color(color), normal, dimension);
            } else {
                Class<?> type = waypoint.getClass();
                type.getMethod("setName", String.class).invoke(waypoint, name);
                type.getMethod("setLocation", int.class, int.class, int.class, int.class)
                        .invoke(waypoint, x, y + WaystoneConfig.journeyMapWaypointYOffset, z, dimension);
                type.getMethod("setColor", Integer.class).invoke(waypoint, color);
                type.getMethod("setEnable", boolean.class).invoke(waypoint, true);
            }
            storeClass.getMethod("save", waypoint.getClass()).invoke(store, waypoint);
        } catch (ReflectiveOperationException | LinkageError exception) {
            debug("JourneyMap", exception);
        }
    }

    private static void updateXaero(String oldName, String name, int x, int y, int z) {
        if (!WaystoneConfig.xaeroMinimapWaypoints
                || !(FishModLoader.hasMod("XaeroMinimap") || FishModLoader.hasMod("xaerominimap"))) {
            return;
        }
        try {
            Class<?> sessionClass = Class.forName("xaero.common.XaeroMinimapSession");
            Object session = sessionClass.getMethod("getCurrentSession").invoke(null);
            if (session == null) {
                return;
            }
            Object manager = sessionClass.getMethod("getWaypointsManager").invoke(session);
            Object world = manager.getClass().getMethod("getCurrentWorld").invoke(manager);
            if (world == null) {
                return;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> sets = (Map<String, Object>) world.getClass().getMethod("getSets").invoke(world);
            Object waypoint = null;
            Object targetSet = world.getClass().getMethod("getCurrentSet").invoke(world);
            if (targetSet == null && sets != null && !sets.isEmpty()) {
                targetSet = sets.values().iterator().next();
            }
            String sought = oldName == null ? name : oldName;
            if (sets != null) {
                for (Object set : sets.values()) {
                    @SuppressWarnings("unchecked")
                    List<Object> waypoints = (List<Object>) set.getClass().getMethod("getList").invoke(set);
                    waypoint = findByName(waypoints, sought);
                    if (waypoint != null) {
                        break;
                    }
                }
            }
            if (oldName != null && waypoint == null) {
                return;
            }
            int color = WaystoneConfig.xaeroMinimapWaypointRandomColor
                    ? (name.hashCode() & Integer.MAX_VALUE) % 16 : WaystoneConfig.xaeroMinimapWaypointColor;
            int waypointY = y + WaystoneConfig.xaeroMinimapWaypointYOffset;
            if (waypoint == null) {
                Class<?> waypointClass = Class.forName("xaero.common.minimap.waypoints.Waypoint");
                waypoint = waypointClass.getConstructor(int.class, int.class, int.class,
                                String.class, String.class, int.class)
                        .newInstance(x, waypointY, z, name, symbol(name), color);
                if (targetSet != null) {
                    @SuppressWarnings("unchecked")
                    List<Object> list = (List<Object>) targetSet.getClass().getMethod("getList").invoke(targetSet);
                    list.add(waypoint);
                }
            } else {
                Class<?> type = waypoint.getClass();
                type.getMethod("setX", int.class).invoke(waypoint, x);
                type.getMethod("setY", int.class).invoke(waypoint, waypointY);
                type.getMethod("setZ", int.class).invoke(waypoint, z);
                type.getMethod("setName", String.class).invoke(waypoint, name);
                type.getMethod("setSymbol", String.class).invoke(waypoint, symbol(name));
                type.getMethod("setColor", int.class).invoke(waypoint, color);
                type.getMethod("setDisabled", boolean.class).invoke(waypoint, false);
            }
            manager.getClass().getMethod("updateWaypoints").invoke(manager);
        } catch (ReflectiveOperationException | LinkageError exception) {
            debug("Xaero's Minimap", exception);
        }
    }

    private static Object findByName(Collection<?> entries, String name) throws ReflectiveOperationException {
        if (entries == null) {
            return null;
        }
        for (Object entry : entries) {
            if (name.equals(entry.getClass().getMethod("getName").invoke(entry))) {
                return entry;
            }
        }
        return null;
    }

    private static int journeyColor(String name) {
        if (WaystoneConfig.journeyMapWaypointRandomColor) {
            float hue = (name.hashCode() & Integer.MAX_VALUE) / (float) Integer.MAX_VALUE;
            return Color.HSBtoRGB(hue, 0.75F, 1.0F) & 0xFFFFFF;
        }
        try {
            return Integer.parseInt(WaystoneConfig.journeyMapWaypointColor.replace("#", ""), 16) & 0xFFFFFF;
        } catch (NumberFormatException ignored) {
            return 0x7FDBFF;
        }
    }

    private static String symbol(String name) {
        for (int i = 0; i < name.length(); i++) {
            if (!Character.isWhitespace(name.charAt(i))) {
                return String.valueOf(Character.toUpperCase(name.charAt(i)));
            }
        }
        return "W";
    }

    private static void debug(String integration, Throwable throwable) {
        if (WaystoneConfig.debugMode) {
            Waystones.LOGGER.warn("Failed to update {} waypoint", integration, throwable);
        }
    }
}
