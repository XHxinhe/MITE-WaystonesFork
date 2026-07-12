package net.blay09.mods.waystones.network;

import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.blay09.mods.waystones.ClientWaystoneState;
import net.blay09.mods.waystones.WaystoneEntry;
import net.minecraft.EntityPlayer;
import net.minecraft.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class S2CWaystoneState implements Packet {
    public static final ResourceLocation CHANNEL = new ResourceLocation("waystones", "state");

    private final List<WaystoneEntry> entries;
    private final WaystoneEntry last;
    private final long lastFreeWarp;
    private final long lastWarpStoneUse;
    private final List<String> pinnedNames;

    public S2CWaystoneState(PacketByteBuf buf) {
        int count = Math.max(0, Math.min(1024, buf.readInt()));
        entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(WaystoneEntry.read(buf));
        }
        last = buf.readBoolean() ? WaystoneEntry.read(buf) : null;
        lastFreeWarp = buf.readLong();
        lastWarpStoneUse = buf.readLong();
        int pinnedCount = Math.max(0, Math.min(1024, buf.readInt()));
        pinnedNames = new ArrayList<>(pinnedCount);
        for (int i = 0; i < pinnedCount; i++) {
            pinnedNames.add(buf.readString());
        }
    }

    public S2CWaystoneState(List<WaystoneEntry> entries, WaystoneEntry last,
                            long lastFreeWarp, long lastWarpStoneUse, List<String> pinnedNames) {
        this.entries = List.copyOf(entries);
        this.last = last;
        this.lastFreeWarp = lastFreeWarp;
        this.lastWarpStoneUse = lastWarpStoneUse;
        this.pinnedNames = List.copyOf(pinnedNames);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(entries.size());
        for (WaystoneEntry entry : entries) {
            entry.write(buf);
        }
        buf.writeBoolean(last != null);
        if (last != null) {
            last.write(buf);
        }
        buf.writeLong(lastFreeWarp);
        buf.writeLong(lastWarpStoneUse);
        buf.writeInt(pinnedNames.size());
        for (String name : pinnedNames) {
            buf.writeString(name);
        }
    }

    @Override
    public void apply(EntityPlayer player) {
        ClientWaystoneState.update(entries, last, lastFreeWarp, lastWarpStoneUse, pinnedNames);
    }

    @Override
    public ResourceLocation getChannel() {
        return CHANNEL;
    }
}
