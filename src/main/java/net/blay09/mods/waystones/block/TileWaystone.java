package net.blay09.mods.waystones.block;

import net.minecraft.NBTTagCompound;
import net.minecraft.Packet;
import net.minecraft.Packet132TileEntityData;
import net.minecraft.TileEntity;

public final class TileWaystone extends TileEntity {
    public static final int VARIANT_STONE = 0;
    public static final int VARIANT_SANDSTONE = 1;
    public static final int VARIANT_MOSSY = 2;
    public static final int VARIANT_STONEBRICK = 3;
    public static final int VARIANT_NETHER = 4;
    public static final int VARIANT_END = 5;
    public static final int VARIANT_MOSSY_STONEBRICK = 6;

    private static int descriptionPacketType = -1;

    private String waystoneName = "";
    private String waystoneOwner = "";
    private int facing;
    private boolean upperPart;
    private boolean global;
    private boolean forceGlobalOnActivation;

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setString("WaystoneName", waystoneName);
        tag.setString("WaystoneOwner", waystoneOwner);
        tag.setInteger("Facing", facing);
        tag.setBoolean("UpperPart", upperPart);
        tag.setBoolean("Global", global);
        tag.setBoolean("ForceGlobalOnActivation", forceGlobalOnActivation);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        waystoneName = tag.getString("WaystoneName");
        waystoneOwner = tag.getString("WaystoneOwner");
        facing = tag.getInteger("Facing");
        upperPart = tag.getBoolean("UpperPart");
        global = tag.getBoolean("Global");
        forceGlobalOnActivation = tag.getBoolean("ForceGlobalOnActivation");
    }

    @Override
    public Packet getDescriptionPacket() {
        if (descriptionPacketType < 0) {
            return null;
        }
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new Packet132TileEntityData(xCoord, yCoord, zCoord, descriptionPacketType, tag);
    }

    public static void setDescriptionPacketType(int type) {
        descriptionPacketType = type;
    }

    public String getWaystoneName() {
        return waystoneName;
    }

    public void setWaystoneName(String name) {
        waystoneName = name == null ? "" : name.trim();
        onInventoryChanged();
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    public String getWaystoneOwner() {
        return waystoneOwner;
    }

    public void setWaystoneOwner(String owner) {
        waystoneOwner = owner == null ? "" : owner;
        onInventoryChanged();
        sync();
    }

    public int getVariant() {
        return getBlockType() instanceof BlockWaystone block ? block.getVariant() : VARIANT_STONE;
    }

    public int getFacing() {
        return facing;
    }

    public void setFacing(int facing) {
        this.facing = facing & 3;
        onInventoryChanged();
        sync();
    }

    public boolean isUpperPart() {
        return upperPart;
    }

    public void setUpperPart(boolean upperPart) {
        this.upperPart = upperPart;
        onInventoryChanged();
        sync();
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
        onInventoryChanged();
        sync();
    }

    public boolean shouldForceGlobalOnActivation() {
        return forceGlobalOnActivation;
    }

    public void setForceGlobalOnActivation(boolean value) {
        forceGlobalOnActivation = value;
        onInventoryChanged();
        sync();
    }

    private void sync() {
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }
}
