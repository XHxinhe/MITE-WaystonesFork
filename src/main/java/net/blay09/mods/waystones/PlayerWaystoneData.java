package net.blay09.mods.waystones;

import net.blay09.mods.waystones.api.IWaystonePlayer;
import net.minecraft.EntityPlayer;
import net.minecraft.NBTTagCompound;
import net.minecraft.NBTTagList;

import java.util.ArrayList;
import java.util.List;

public final class PlayerWaystoneData {
    private static final String LIST = "WaystoneList";
    private static final String LAST_WARP = "LastWarpStoneUse";
    private static final String LAST_WAYSTONE = "LastWaystone";
    private static final String LAST_FREE_WARP = "LastFreeWarpUse";
    private static final String PINNED = "PinnedWaystones";

    private PlayerWaystoneData() {
    }

    private static NBTTagCompound data(EntityPlayer player) {
        return ((IWaystonePlayer) player).waystones$getData();
    }

    public static List<WaystoneEntry> getWaystones(EntityPlayer player) {
        NBTTagList list = data(player).getTagList(LIST);
        List<WaystoneEntry> result = new ArrayList<>(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            result.add(WaystoneEntry.fromNbt((NBTTagCompound) list.tagAt(i)));
        }
        return result;
    }

    public static void activate(EntityPlayer player, WaystoneEntry entry) {
        setLast(player, entry);
        if (entry.global()) {
            return;
        }
        List<WaystoneEntry> entries = getWaystones(player);
        entries.removeIf(existing -> existing.samePosition(entry));
        entries.add(entry);
        save(player, entries);
    }

    public static void remove(EntityPlayer player, WaystoneEntry entry) {
        List<WaystoneEntry> entries = getWaystones(player);
        entries.removeIf(existing -> existing.samePosition(entry));
        save(player, entries);
    }

    public static void forget(EntityPlayer player, WaystoneEntry entry) {
        remove(player, entry);
        WaystoneEntry last = getLast(player);
        if (last != null && last.samePosition(entry)) {
            data(player).removeTag(LAST_WAYSTONE);
        }
        setPinned(player, entry.name(), false);
    }

    public static void rename(EntityPlayer player, WaystoneEntry oldEntry, WaystoneEntry newEntry) {
        List<WaystoneEntry> entries = getWaystones(player);
        boolean changed = false;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).samePosition(oldEntry)) {
                entries.set(i, newEntry.asGlobal(false));
                changed = true;
            }
        }
        if (changed) {
            save(player, entries);
        }
        WaystoneEntry last = getLast(player);
        if (last != null && last.samePosition(oldEntry)) {
            setLast(player, newEntry);
        }
        List<String> pinnedNames = getPinnedNames(player);
        if (pinnedNames.contains(oldEntry.name())) {
            setPinned(player, oldEntry.name(), false);
            setPinned(player, newEntry.name(), true);
        }
    }

    public static WaystoneEntry getLast(EntityPlayer player) {
        NBTTagCompound tag = data(player).getCompoundTag(LAST_WAYSTONE);
        if (!tag.hasNoTags()) {
            return WaystoneEntry.fromNbt(tag);
        }
        List<WaystoneEntry> entries = getWaystones(player);
        return entries.isEmpty() ? null : entries.get(entries.size() - 1);
    }

    public static void setLast(EntityPlayer player, WaystoneEntry entry) {
        data(player).setTag(LAST_WAYSTONE, entry.toNbt());
    }

    private static void save(EntityPlayer player, List<WaystoneEntry> entries) {
        NBTTagList list = new NBTTagList();
        for (WaystoneEntry entry : entries) {
            list.appendTag(entry.toNbt());
        }
        data(player).setTag(LIST, list);
    }

    public static long getLastWarpStoneUse(EntityPlayer player) {
        return data(player).getLong(LAST_WARP);
    }

    public static void setLastWarpStoneUse(EntityPlayer player, long value) {
        data(player).setLong(LAST_WARP, value);
    }

    public static long getLastFreeWarpUse(EntityPlayer player) {
        return data(player).getLong(LAST_FREE_WARP);
    }

    public static void setLastFreeWarpUse(EntityPlayer player, long value) {
        data(player).setLong(LAST_FREE_WARP, value);
    }

    public static List<String> getPinnedNames(EntityPlayer player) {
        NBTTagList list = data(player).getTagList(PINNED);
        List<String> result = new ArrayList<>(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            if (list.tagAt(i) instanceof net.minecraft.NBTTagString tag) {
                result.add(tag.data);
            }
        }
        return result;
    }

    public static void setPinned(EntityPlayer player, String name, boolean pinned) {
        List<String> names = getPinnedNames(player);
        names.removeIf(existing -> existing.equals(name));
        if (pinned) {
            names.add(name);
        }
        NBTTagList list = new NBTTagList();
        for (String pinnedName : names) {
            list.appendTag(new net.minecraft.NBTTagString("", pinnedName));
        }
        data(player).setTag(PINNED, list);
    }
}
