package net.blay09.mods.waystones.network;

import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.blay09.mods.waystones.client.ClientScreenQueue;
import net.blay09.mods.waystones.client.GuiWaystoneName;
import net.minecraft.EntityPlayer;
import net.minecraft.ResourceLocation;

public final class S2COpenName implements Packet {
    public static final ResourceLocation CHANNEL = new ResourceLocation("waystones", "open_name");

    private final int x;
    private final int y;
    private final int z;
    private final String name;
    private final boolean global;

    public S2COpenName(PacketByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readString(), buf.readBoolean());
    }

    public S2COpenName(int x, int y, int z, String name, boolean global) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
        this.global = global;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeString(name);
        buf.writeBoolean(global);
    }

    @Override
    public void apply(EntityPlayer player) {
        ClientScreenQueue.open(new GuiWaystoneName(x, y, z, name, global));
    }

    @Override
    public ResourceLocation getChannel() {
        return CHANNEL;
    }
}
