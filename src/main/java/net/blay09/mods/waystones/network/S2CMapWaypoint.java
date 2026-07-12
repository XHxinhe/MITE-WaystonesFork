package net.blay09.mods.waystones.network;

import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.blay09.mods.waystones.compat.WaystoneMapCompat;
import net.minecraft.EntityPlayer;
import net.minecraft.ResourceLocation;

public final class S2CMapWaypoint implements Packet {
    public static final ResourceLocation CHANNEL = new ResourceLocation("waystones", "map_waypoint");
    private final String oldName;
    private final String name;
    private final int dimension;
    private final int x;
    private final int y;
    private final int z;

    public S2CMapWaypoint(PacketByteBuf buf) {
        this(buf.readString(), buf.readString(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public S2CMapWaypoint(String oldName, String name, int dimension, int x, int y, int z) {
        this.oldName = oldName == null ? "" : oldName;
        this.name = name;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(oldName);
        buf.writeString(name);
        buf.writeInt(dimension);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    @Override
    public void apply(EntityPlayer player) {
        if (oldName.isEmpty()) {
            WaystoneMapCompat.addOrUpdate(name, dimension, x, y, z);
        } else {
            WaystoneMapCompat.rename(oldName, name, dimension, x, y, z);
        }
    }

    @Override
    public ResourceLocation getChannel() {
        return CHANNEL;
    }
}
