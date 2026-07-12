package net.blay09.mods.waystones;

import huix.glacier.api.extension.creativetab.GlacierCreativeTabs;
import net.minecraft.Item;
import net.minecraft.ItemStack;

import java.util.List;

public final class WaystoneCreativeTab extends GlacierCreativeTabs {
    public WaystoneCreativeTab() {
        super("waystones");
    }

    @Override
    public Item getTabIconItem() {
        Item icon = Item.itemsList[WaystoneContent.WAYSTONE.blockID];
        return icon != null ? icon : WaystoneContent.WARP_STONE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void displayAllReleventItems(List items) {
        for (net.blay09.mods.waystones.block.BlockWaystone block : WaystoneContent.WAYSTONES) {
            items.add(new ItemStack(block));
        }
        items.add(new ItemStack(WaystoneContent.RETURN_SCROLL));
        items.add(new ItemStack(WaystoneContent.WARP_STONE));
    }
}
