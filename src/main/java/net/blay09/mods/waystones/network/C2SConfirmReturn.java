package net.blay09.mods.waystones.network;

import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.blay09.mods.waystones.WaystoneManager;
import net.minecraft.EntityPlayer;
import net.minecraft.ResourceLocation;

public final class C2SConfirmReturn implements Packet {
    public static final ResourceLocation CHANNEL = new ResourceLocation("waystones", "confirm_return");

    public C2SConfirmReturn() {
    }

    public C2SConfirmReturn(PacketByteBuf ignored) {
    }

    @Override
    public void write(PacketByteBuf buf) {
    }

    @Override
    public void apply(EntityPlayer player) {
        if (player != null && player.onServer()) {
            WaystoneManager.confirmReturn(player);
        }
    }

    @Override
    public ResourceLocation getChannel() {
        return CHANNEL;
    }
}
