package net.blay09.mods.waystones.compat;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import net.blay09.mods.waystones.block.BlockWaystone;
import net.blay09.mods.waystones.block.TileWaystone;
import net.minecraft.EnumChatFormatting;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;
import net.minecraft.ServerPlayer;
import net.minecraft.TileEntity;
import net.minecraft.World;

import java.util.List;

public final class WailaCompat implements IWailaDataProvider {
    private static final String NAME_TAG = "WaystoneName";
    private static final String GLOBAL_TAG = "WaystoneGlobal";

    private WailaCompat() {
    }

    public static void register() {
        WailaCompat provider = new WailaCompat();
        ModuleRegistrar.instance().registerBodyProvider(provider, BlockWaystone.class);
        ModuleRegistrar.instance().registerNBTProvider(provider, BlockWaystone.class);
    }

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack stack, List<String> tooltip,
                                     IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return tooltip;
    }

    @Override
    public List<String> getWailaBody(ItemStack stack, List<String> tooltip,
                                     IWailaDataAccessor accessor, IWailaConfigHandler config) {
        NBTTagCompound tag = accessor.getNBTData();
        String name = tag == null ? "" : tag.getString(NAME_TAG);
        boolean global = tag != null && tag.getBoolean(GLOBAL_TAG);
        if (name.isEmpty()) {
            TileWaystone tile = getBaseTile(accessor.getTileEntity());
            if (tile != null) {
                name = tile.getWaystoneName();
                global = tile.isGlobal();
            }
        }
        if (!name.isEmpty()) {
            tooltip.add((global ? EnumChatFormatting.YELLOW : EnumChatFormatting.AQUA) + name);
        }
        return tooltip;
    }

    @Override
    public List<String> getWailaTail(ItemStack stack, List<String> tooltip,
                                     IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return tooltip;
    }

    @Override
    public NBTTagCompound getNBTData(ServerPlayer player, TileEntity tile, NBTTagCompound tag,
                                     World world, int x, int y, int z) {
        TileWaystone base = getBaseTile(tile);
        if (base == null && world != null && world.getBlockMetadata(x, y, z) == 1) {
            TileEntity below = world.getBlockTileEntity(x, y - 1, z);
            base = below instanceof TileWaystone ? (TileWaystone) below : null;
        }
        if (base != null) {
            tag.setString(NAME_TAG, base.getWaystoneName());
            tag.setBoolean(GLOBAL_TAG, base.isGlobal());
        }
        return tag;
    }

    private static TileWaystone getBaseTile(TileEntity tile) {
        if (!(tile instanceof TileWaystone waystone)) {
            return null;
        }
        if (!waystone.isUpperPart() || waystone.getWorldObj() == null) {
            return waystone;
        }
        TileEntity below = waystone.getWorldObj().getBlockTileEntity(
                waystone.xCoord, waystone.yCoord - 1, waystone.zCoord);
        return below instanceof TileWaystone ? (TileWaystone) below : null;
    }
}
