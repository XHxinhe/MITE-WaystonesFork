package net.blay09.mods.waystones;

import moddedmite.rustedironcore.api.event.Handlers;
import net.blay09.mods.waystones.block.TileWaystone;
import net.blay09.mods.waystones.network.WaystonePackets;
import net.blay09.mods.waystones.worldgen.WaystoneWorldgen;
import net.fabricmc.api.ModInitializer;
import net.xiaoyu233.fml.ModResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Waystones implements ModInitializer {
    public static final String MOD_ID = "waystones";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModResourceManager.addResourcePackDomain(MOD_ID);
        WaystoneConfig.load();
        Handlers.TileEntityData.register(event -> {
            int type = event.register(TileWaystone.class);
            TileWaystone.setDescriptionPacketType(type);
            LOGGER.info("Waystone tile data sync registered: type={}", type);
        });
        Handlers.ChunkLoad.register(WaystoneWorldgen.INSTANCE);
        Handlers.Tick.register(WaystoneWorldgen.INSTANCE);
        Handlers.LootTable.register(WaystoneLoot.INSTANCE);
        WaystonePackets.init();
        registerOptionalWailaCompatibility();
        LOGGER.info("Waystones-MITE initialized");
    }

    private static void registerOptionalWailaCompatibility() {
        if (!net.xiaoyu233.fml.FishModLoader.hasMod("waila")) {
            return;
        }
        try {
            Class.forName("net.blay09.mods.waystones.compat.WailaCompat")
                    .getMethod("register")
                    .invoke(null);
            LOGGER.info("WAILA compatibility initialized");
        } catch (ReflectiveOperationException | LinkageError exception) {
            LOGGER.warn("Failed to initialize optional WAILA compatibility", exception);
        }
    }
}
