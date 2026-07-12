package net.blay09.mods.waystones.network;

import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.blay09.mods.waystones.PlayerWaystoneData;
import net.blay09.mods.waystones.WaystoneEntry;
import net.blay09.mods.waystones.WaystoneManager;
import net.minecraft.EntityPlayer;
import net.minecraft.ResourceLocation;
import net.minecraft.ServerPlayer;

public final class C2SSetPinned implements Packet {
    public static final ResourceLocation CHANNEL = new ResourceLocation("waystones", "set_pinned");
    private final WaystoneEntry entry;
    private final boolean pinned;

    public C2SSetPinned(PacketByteBuf buf) {
        this(WaystoneEntry.read(buf), buf.readBoolean());
    }

    public C2SSetPinned(WaystoneEntry entry, boolean pinned) {
        this.entry = entry;
        this.pinned = pinned;
    }

    @Override
    public void write(PacketByteBuf buf) {
        entry.write(buf);
        buf.writeBoolean(pinned);
    }

    @Override
    public void apply(EntityPlayer player) {
        WaystoneEntry allowed = WaystoneManager.findAccessible(player, entry);
        if (allowed == null) {
            return;
        }
        PlayerWaystoneData.setPinned(player, allowed.name(), pinned);
        if (player instanceof ServerPlayer serverPlayer) {
            WaystoneManager.sendPlayerState(serverPlayer);
        }
    }

    @Override
    public ResourceLocation getChannel() {
        return CHANNEL;
    }
}
