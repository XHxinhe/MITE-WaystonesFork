package net.blay09.mods.waystones;

import net.blay09.mods.waystones.block.BlockWaystone;
import net.blay09.mods.waystones.block.TileWaystone;
import net.blay09.mods.waystones.item.ItemReturnScroll;
import net.blay09.mods.waystones.item.ItemWarpStone;
import net.xiaoyu233.fml.reload.utils.IdUtil;

public final class WaystoneContent {
    public static final BlockWaystone WAYSTONE = block(TileWaystone.VARIANT_STONE, "waystone");
    public static final BlockWaystone WAYSTONE_SANDSTONE = block(TileWaystone.VARIANT_SANDSTONE, "waystone_sandstone");
    public static final BlockWaystone WAYSTONE_MOSSY = block(TileWaystone.VARIANT_MOSSY, "waystone_mossy");
    public static final BlockWaystone WAYSTONE_STONEBRICK = block(TileWaystone.VARIANT_STONEBRICK, "waystone_stonebrick");
    public static final BlockWaystone WAYSTONE_MOSSY_STONEBRICK = block(TileWaystone.VARIANT_MOSSY_STONEBRICK, "waystone_mossy_stonebrick");
    public static final BlockWaystone WAYSTONE_NETHER = block(TileWaystone.VARIANT_NETHER, "waystone_nether");
    public static final BlockWaystone WAYSTONE_END = block(TileWaystone.VARIANT_END, "waystone_end");
    public static final ItemReturnScroll RETURN_SCROLL = new ItemReturnScroll(IdUtil.getNextItemID());
    public static final ItemWarpStone WARP_STONE = new ItemWarpStone(IdUtil.getNextItemID());

    public static final BlockWaystone[] WAYSTONES = {
            WAYSTONE, WAYSTONE_SANDSTONE, WAYSTONE_MOSSY, WAYSTONE_STONEBRICK,
            WAYSTONE_MOSSY_STONEBRICK, WAYSTONE_NETHER, WAYSTONE_END
    };
    public static final WaystoneCreativeTab CREATIVE_TAB = new WaystoneCreativeTab();

    static {
        for (BlockWaystone block : WAYSTONES) {
            block.setCreativeTab(CREATIVE_TAB);
        }
        RETURN_SCROLL.setCreativeTab(CREATIVE_TAB);
        WARP_STONE.setCreativeTab(CREATIVE_TAB);
    }

    private WaystoneContent() {
    }

    private static BlockWaystone block(int variant, String name) {
        return new BlockWaystone(IdUtil.getNextBlockID(), variant, name);
    }

    public static BlockWaystone getWaystoneBlock(int variant) {
        return switch (variant) {
            case TileWaystone.VARIANT_SANDSTONE -> WAYSTONE_SANDSTONE;
            case TileWaystone.VARIANT_MOSSY -> WAYSTONE_MOSSY;
            case TileWaystone.VARIANT_STONEBRICK -> WAYSTONE_STONEBRICK;
            case TileWaystone.VARIANT_MOSSY_STONEBRICK -> WAYSTONE_MOSSY_STONEBRICK;
            case TileWaystone.VARIANT_NETHER -> WAYSTONE_NETHER;
            case TileWaystone.VARIANT_END -> WAYSTONE_END;
            default -> WAYSTONE;
        };
    }
}
