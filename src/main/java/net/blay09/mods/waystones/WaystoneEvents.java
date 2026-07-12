package net.blay09.mods.waystones;

import com.google.common.eventbus.Subscribe;
import huix.glacier.api.extension.creativetab.GlacierCreativeTabs;
import net.blay09.mods.waystones.block.TileWaystone;
import net.blay09.mods.waystones.client.render.RenderWaystone;
import net.minecraft.Block;
import net.minecraft.CreativeTabs;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.xiaoyu233.fml.reload.event.RecipeRegistryEvent;
import net.xiaoyu233.fml.reload.event.ItemRegistryEvent;
import net.xiaoyu233.fml.reload.event.MITEEvents;
import net.xiaoyu233.fml.reload.event.TileEntityRegisterEvent;
import net.xiaoyu233.fml.reload.event.TileEntityRendererRegisterEvent;
import net.xiaoyu233.fml.reload.event.PlayerLoggedInEvent;
import moddedmite.rustedironcore.network.Network;
import net.blay09.mods.waystones.network.S2CWaystoneConfig;

import java.util.ArrayList;
import java.util.List;

public final class WaystoneEvents {
    public static final WaystoneEvents INSTANCE = new WaystoneEvents();

    private WaystoneEvents() {
    }

    private static boolean registered;

    public static void register() {
        if (!registered) {
            MITEEvents.MITE_EVENT_BUS.register(INSTANCE);
            registered = true;
        }
    }

    @Subscribe
    public void registerItems(ItemRegistryEvent event) {
        for (net.blay09.mods.waystones.block.BlockWaystone block : WaystoneContent.WAYSTONES) {
            event.registerItemBlock(Waystones.MOD_ID, block.getRegistryName(), block);
        }
        event.register(Waystones.MOD_ID, "return_scroll", WaystoneContent.RETURN_SCROLL, WaystoneContent.CREATIVE_TAB);
        event.register(Waystones.MOD_ID, "warp_stone", WaystoneContent.WARP_STONE, WaystoneContent.CREATIVE_TAB);
    }

    @Subscribe
    public void registerTileEntities(TileEntityRegisterEvent event) {
        event.register(TileWaystone.class, Waystones.MOD_ID + ":waystone");
    }

    @Subscribe
    public void registerTileEntityRenderers(TileEntityRendererRegisterEvent event) {
        event.register(TileWaystone.class, new RenderWaystone());
    }

    @Subscribe
    public void registerRecipes(RecipeRegistryEvent event) {
        if (WaystoneConfig.allowReturnScrolls) {
            event.registerShapedRecipe(new ItemStack(WaystoneContent.RETURN_SCROLL, 3), true,
                    "GEG", "PPP",
                    'G', new ItemStack(Item.goldNugget),
                    'E', new ItemStack(Item.enderPearl),
                    'P', new ItemStack(Item.paper));
        }
        if (WaystoneConfig.allowWarpStone) {
            event.registerShapedRecipe(new ItemStack(WaystoneContent.WARP_STONE), true,
                    "DED", "EGE", "DED",
                    'D', new ItemStack(Item.dyePowder, 1, 5),
                    'E', new ItemStack(Item.enderPearl),
                    'G', new ItemStack(Item.emerald));
        }
        if (!WaystoneConfig.creativeModeOnly) {
            registerWaystoneRecipe(event, WaystoneContent.WAYSTONE, new ItemStack(Block.stoneBrick));
            registerWaystoneRecipe(event, WaystoneContent.WAYSTONE_SANDSTONE, new ItemStack(Block.sandStone));
            registerWaystoneRecipe(event, WaystoneContent.WAYSTONE_MOSSY, new ItemStack(Block.cobblestoneMossy));
            registerWaystoneRecipe(event, WaystoneContent.WAYSTONE_STONEBRICK, new ItemStack(Block.stoneBrick, 1, 0));
            registerWaystoneRecipe(event, WaystoneContent.WAYSTONE_MOSSY_STONEBRICK, new ItemStack(Block.stoneBrick, 1, 1));
            registerWaystoneRecipe(event, WaystoneContent.WAYSTONE_NETHER, new ItemStack(Block.netherBrick));
            registerWaystoneRecipe(event, WaystoneContent.WAYSTONE_END, new ItemStack(Block.whiteStone));
        }
        verifyCreativeInventory();
    }

    @Subscribe
    public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        Network.sendToClient(event.getPlayer(), new S2CWaystoneConfig());
        WaystoneManager.sendPlayerState(event.getPlayer());
    }

    private static void verifyCreativeInventory() {
        int tabIndex = WaystoneContent.CREATIVE_TAB.getTabIndex();
        if (tabIndex < 12 || tabIndex >= GlacierCreativeTabs.newCreativeTabArray.size()
                || GlacierCreativeTabs.newCreativeTabArray.get(tabIndex) != WaystoneContent.CREATIVE_TAB) {
            throw new IllegalStateException("Waystones creative tab is missing from the RIC paged tab registry");
        }
        for (net.blay09.mods.waystones.block.BlockWaystone block : WaystoneContent.WAYSTONES) {
            requireCreativeEntry(WaystoneContent.CREATIVE_TAB, block.blockID, block.getRegistryName());
        }
        requireCreativeEntry(WaystoneContent.CREATIVE_TAB, WaystoneContent.RETURN_SCROLL.itemID, "return_scroll");
        requireCreativeEntry(WaystoneContent.CREATIVE_TAB, WaystoneContent.WARP_STONE.itemID, "warp_stone");
        Waystones.LOGGER.info("Creative inventory verified: tab={} page=2, waystone={}, return_scroll={}, warp_stone={}",
                tabIndex, WaystoneContent.WAYSTONE.blockID,
                WaystoneContent.RETURN_SCROLL.itemID, WaystoneContent.WARP_STONE.itemID);
    }

    private static void registerWaystoneRecipe(RecipeRegistryEvent event, Block result, ItemStack material) {
        event.registerShapedRecipe(new ItemStack(result), true,
                " S ", "SWS", "OOO",
                'S', material,
                'W', new ItemStack(WaystoneContent.WARP_STONE),
                'O', new ItemStack(Block.obsidian));
    }

    @SuppressWarnings("unchecked")
    private static void requireCreativeEntry(CreativeTabs tab, int itemId, String name) {
        List<ItemStack> stacks = new ArrayList<>();
        tab.displayAllReleventItems(stacks);
        boolean found = stacks.stream().anyMatch(stack -> stack != null && stack.itemID == itemId);
        if (!found) {
            throw new IllegalStateException("Missing Waystones creative entry: " + name + " (" + itemId + ")");
        }
    }
}
