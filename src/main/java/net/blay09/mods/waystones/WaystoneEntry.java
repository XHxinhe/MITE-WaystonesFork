package net.blay09.mods.waystones;

import moddedmite.rustedironcore.network.PacketByteBuf;
import net.blay09.mods.waystones.block.TileWaystone;
import net.minecraft.NBTTagCompound;

public record WaystoneEntry(String name, int dimension, int x, int y, int z, boolean global) {
    public WaystoneEntry(TileWaystone tile) {
        this(tile.getWaystoneName(), tile.getWorldObj().provider.dimensionId,
                tile.xCoord, tile.yCoord, tile.zCoord, tile.isGlobal());
    }

    public void write(PacketByteBuf buf) {
        buf.writeString(name);
        buf.writeInt(dimension);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeBoolean(global);
    }

    public static WaystoneEntry read(PacketByteBuf buf) {
        return new WaystoneEntry(buf.readString(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readBoolean());
    }

    public NBTTagCompound toNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Name", name);
        tag.setInteger("Dimension", dimension);
        tag.setInteger("X", x);
        tag.setInteger("Y", y);
        tag.setInteger("Z", z);
        tag.setBoolean("Global", global);
        return tag;
    }

    public static WaystoneEntry fromNbt(NBTTagCompound tag) {
        return new WaystoneEntry(tag.getString("Name"), tag.getInteger("Dimension"),
                tag.getInteger("X"), tag.getInteger("Y"), tag.getInteger("Z"), tag.getBoolean("Global"));
    }

    public boolean samePosition(WaystoneEntry other) {
        return other != null && dimension == other.dimension && x == other.x && y == other.y && z == other.z;
    }

    public WaystoneEntry asGlobal(boolean value) {
        return new WaystoneEntry(name, dimension, x, y, z, value);
    }
}
