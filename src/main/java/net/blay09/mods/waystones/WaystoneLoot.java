package net.blay09.mods.waystones;

import moddedmite.rustedironcore.api.event.listener.ILootTableRegisterListener;
import net.minecraft.WeightedRandomChestContent;

import java.util.List;

public final class WaystoneLoot implements ILootTableRegisterListener {
    public static final WaystoneLoot INSTANCE = new WaystoneLoot();

    private WaystoneLoot() {
    }

    private static void add(List<WeightedRandomChestContent> loot, int min, int max, int weight) {
        if (WaystoneConfig.lootReturnScrolls) {
            loot.add(new WeightedRandomChestContent(WaystoneContent.RETURN_SCROLL.itemID, 0, min, max, weight));
        }
    }

    @Override
    public void onBlackSmithRegister(List<WeightedRandomChestContent> loot) {
        add(loot, 1, 1, 5);
    }

    @Override
    public void onMineshaftRegister(List<WeightedRandomChestContent> loot) {
        add(loot, 1, 1, 3);
    }

    @Override
    public void onDungeonOverworldRegister(List<WeightedRandomChestContent> loot) {
        add(loot, 1, 2, 3);
    }

    @Override
    public void onDesertPyramidRegister(List<WeightedRandomChestContent> loot) {
        add(loot, 1, 1, 3);
    }

    @Override
    public void onJunglePyramidRegister(List<WeightedRandomChestContent> loot) {
        add(loot, 1, 1, 2);
    }

    @Override
    public void onStrongholdLibraryRegister(List<WeightedRandomChestContent> loot) {
        add(loot, 1, 1, 2);
    }
}
