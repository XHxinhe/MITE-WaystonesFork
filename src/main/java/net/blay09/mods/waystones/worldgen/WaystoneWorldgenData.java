package net.blay09.mods.waystones.worldgen;

import net.minecraft.MapStorage;
import net.minecraft.NBTTagCompound;
import net.minecraft.NBTTagList;
import net.minecraft.NBTTagString;
import net.minecraft.WorldSavedData;
import net.minecraft.server.MinecraftServer;

import java.util.HashSet;
import java.util.Set;

public final class WaystoneWorldgenData extends WorldSavedData {
    private static final String DATA_NAME = "WaystonesWorldgen";
    private final Set<String> processed = new HashSet<>();

    public WaystoneWorldgenData() {
        this(DATA_NAME);
    }

    public WaystoneWorldgenData(String name) {
        super(name);
    }

    public static WaystoneWorldgenData get(MinecraftServer server) {
        MapStorage storage = server.getOverworld().mapStorage;
        WaystoneWorldgenData data = (WaystoneWorldgenData) storage.loadData(WaystoneWorldgenData.class, DATA_NAME);
        if (data == null) {
            data = new WaystoneWorldgenData();
            storage.setData(DATA_NAME, data);
        }
        return data;
    }

    public boolean markProcessed(String key) {
        if (!processed.add(key)) {
            return false;
        }
        markDirty();
        return true;
    }

    public boolean isProcessed(String key) {
        return processed.contains(key);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        processed.clear();
        NBTTagList list = tag.getTagList("Processed");
        for (int i = 0; i < list.tagCount(); i++) {
            if (list.tagAt(i) instanceof NBTTagString value) {
                processed.add(value.data);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (String key : processed) {
            list.appendTag(new NBTTagString("", key));
        }
        tag.setTag("Processed", list);
    }
}
