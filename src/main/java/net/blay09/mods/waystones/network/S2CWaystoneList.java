package net.blay09.mods.waystones.network;

import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.blay09.mods.waystones.WaystoneEntry;
import net.blay09.mods.waystones.client.ClientScreenQueue;
import net.blay09.mods.waystones.client.GuiWaystoneList;
import net.minecraft.EntityPlayer;
import net.minecraft.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class S2CWaystoneList implements Packet {
    public static final ResourceLocation CHANNEL = new ResourceLocation("waystones", "open_list");

    private final List<WaystoneEntry> entries;
    private final boolean warpStone;
    private final boolean freeWarp;
    private final WaystoneEntry origin;

    public S2CWaystoneList(PacketByteBuf buf) {
        int count = Math.max(0, Math.min(1024, buf.readInt()));
        entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(WaystoneEntry.read(buf));
        }
        warpStone = buf.readBoolean();
        freeWarp = buf.readBoolean();
        origin = buf.readBoolean() ? WaystoneEntry.read(buf) : null;
    }

    public S2CWaystoneList(List<WaystoneEntry> entries, boolean warpStone) {
        this(entries, warpStone, false);
    }

    public S2CWaystoneList(List<WaystoneEntry> entries, boolean warpStone, boolean freeWarp) {
        this(entries, warpStone, freeWarp, null);
    }

    public S2CWaystoneList(List<WaystoneEntry> entries, boolean warpStone, boolean freeWarp,
                           WaystoneEntry origin) {
        this.entries = List.copyOf(entries);
        this.warpStone = warpStone;
        this.freeWarp = freeWarp;
        this.origin = origin;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(entries.size());
        for (WaystoneEntry entry : entries) {
            entry.write(buf);
        }
        buf.writeBoolean(warpStone);
        buf.writeBoolean(freeWarp);
        buf.writeBoolean(origin != null);
        if (origin != null) {
            origin.write(buf);
        }
    }

    @Override
    public void apply(EntityPlayer player) {
        ClientScreenQueue.open(new GuiWaystoneList(entries, warpStone, freeWarp, origin));
    }

    @Override
    public ResourceLocation getChannel() {
        return CHANNEL;
    }
}
