package net.blay09.mods.waystones;

import java.util.List;

public final class ClientWaystoneState {
    private static volatile List<WaystoneEntry> entries = List.of();
    private static volatile WaystoneEntry last;
    private static volatile long lastFreeWarp;
    private static volatile long lastWarpStoneUse;
    private static volatile List<String> pinnedNames = List.of();

    private ClientWaystoneState() {
    }

    public static void clear() {
        entries = List.of();
        last = null;
        lastFreeWarp = 0L;
        lastWarpStoneUse = 0L;
        pinnedNames = List.of();
    }

    public static void update(List<WaystoneEntry> newEntries, WaystoneEntry newLast,
                              long newLastFreeWarp, long newLastWarpStoneUse, List<String> newPinnedNames) {
        entries = List.copyOf(newEntries);
        last = newLast;
        lastFreeWarp = newLastFreeWarp;
        lastWarpStoneUse = newLastWarpStoneUse;
        pinnedNames = List.copyOf(newPinnedNames);
    }

    public static boolean isActive(int dimension, int x, int y, int z) {
        for (WaystoneEntry entry : entries) {
            if (entry.dimension() == dimension && entry.x() == x && entry.y() == y && entry.z() == z) {
                return true;
            }
        }
        return false;
    }

    public static WaystoneEntry getLast() {
        return last;
    }

    public static long getLastFreeWarp() {
        return lastFreeWarp;
    }

    public static long getLastWarpStoneUse() {
        return lastWarpStoneUse;
    }

    public static List<WaystoneEntry> getEntries() {
        return entries;
    }

    public static List<String> getPinnedNames() {
        return pinnedNames;
    }
}
