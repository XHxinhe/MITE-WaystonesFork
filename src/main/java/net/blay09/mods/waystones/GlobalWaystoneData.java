package net.blay09.mods.waystones;

import net.minecraft.MapStorage;
import net.minecraft.NBTTagCompound;
import net.minecraft.NBTTagList;
import net.minecraft.WorldSavedData;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public final class GlobalWaystoneData extends WorldSavedData {
    private static final String DATA_NAME = "WaystonesGlobal";
    private final List<WaystoneEntry> entries = new ArrayList<>();

    public GlobalWaystoneData() {
        this(DATA_NAME);
    }

    public GlobalWaystoneData(String name) {
        super(name);
    }

    public static GlobalWaystoneData get(MinecraftServer server) {
        MapStorage storage = server.getOverworld().mapStorage;
        GlobalWaystoneData data = (GlobalWaystoneData) storage.loadData(GlobalWaystoneData.class, DATA_NAME);
        if (data == null) {
            data = new GlobalWaystoneData();
            storage.setData(DATA_NAME, data);
        }
        return data;
    }

    public List<WaystoneEntry> entries() {
        return List.copyOf(entries);
    }

    public void put(WaystoneEntry entry) {
        entries.removeIf(existing -> existing.samePosition(entry) || existing.name().equals(entry.name()));
        entries.add(entry.asGlobal(true));
        markDirty();
    }

    public void remove(WaystoneEntry entry) {
        if (entries.removeIf(existing -> existing.samePosition(entry))) {
            markDirty();
        }
    }

    public WaystoneEntry find(WaystoneEntry entry) {
        for (WaystoneEntry existing : entries) {
            if (existing.samePosition(entry)) {
                return existing;
            }
        }
        return null;
    }

    public WaystoneEntry findByName(String name) {
        for (WaystoneEntry existing : entries) {
            if (existing.name().equals(name)) {
                return existing;
            }
        }
        return null;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        entries.clear();
        NBTTagList list = tag.getTagList("Entries");
        for (int i = 0; i < list.tagCount(); i++) {
            entries.add(WaystoneEntry.fromNbt((NBTTagCompound) list.tagAt(i)).asGlobal(true));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (WaystoneEntry entry : entries) {
            list.appendTag(entry.toNbt());
        }
        tag.setTag("Entries", list);
    }
}
