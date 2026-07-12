package net.blay09.mods.waystones.network;

import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.blay09.mods.waystones.WaystoneEntry;
import net.blay09.mods.waystones.client.ClientScreenQueue;
import net.blay09.mods.waystones.client.GuiReturnConfirm;
import net.minecraft.EntityPlayer;
import net.minecraft.ResourceLocation;

public final class S2COpenReturnConfirm implements Packet {
    public static final ResourceLocation CHANNEL = new ResourceLocation("waystones", "open_return_confirm");

    private final WaystoneEntry target;

    public S2COpenReturnConfirm(PacketByteBuf buf) {
        target = WaystoneEntry.read(buf);
    }

    public S2COpenReturnConfirm(WaystoneEntry target) {
        this.target = target;
    }

    @Override
    public void write(PacketByteBuf buf) {
        target.write(buf);
    }

    @Override
    public void apply(EntityPlayer player) {
        ClientScreenQueue.open(new GuiReturnConfirm(target));
    }

    @Override
    public ResourceLocation getChannel() {
        return CHANNEL;
    }
}
