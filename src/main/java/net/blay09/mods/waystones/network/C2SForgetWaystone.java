package net.blay09.mods.waystones.network;

import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.blay09.mods.waystones.PlayerWaystoneData;
import net.blay09.mods.waystones.WaystoneEntry;
import net.blay09.mods.waystones.WaystoneManager;
import net.minecraft.EntityPlayer;
import net.minecraft.ResourceLocation;
import net.minecraft.ServerPlayer;

public final class C2SForgetWaystone implements Packet {
    public static final ResourceLocation CHANNEL = new ResourceLocation("waystones", "forget");
    private final WaystoneEntry entry;

    public C2SForgetWaystone(PacketByteBuf buf) {
        this(WaystoneEntry.read(buf));
    }

    public C2SForgetWaystone(WaystoneEntry entry) {
        this.entry = entry;
    }

    @Override
    public void write(PacketByteBuf buf) {
        entry.write(buf);
    }

    @Override
    public void apply(EntityPlayer player) {
        WaystoneEntry allowed = WaystoneManager.findAccessible(player, entry);
        if (allowed == null || allowed.global()) {
            return;
        }
        PlayerWaystoneData.forget(player, allowed);
        if (player instanceof ServerPlayer serverPlayer) {
            WaystoneManager.sendPlayerState(serverPlayer);
        }
    }

    @Override
    public ResourceLocation getChannel() {
        return CHANNEL;
    }
}
