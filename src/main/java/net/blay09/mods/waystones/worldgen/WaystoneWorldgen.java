package net.blay09.mods.waystones.worldgen;

import moddedmite.rustedironcore.api.event.listener.IChunkLoadListener;
import moddedmite.rustedironcore.api.event.listener.ITickListener;
import net.blay09.mods.waystones.GlobalWaystoneData;
import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.WaystoneContent;
import net.blay09.mods.waystones.WaystoneEntry;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.block.BlockWaystone;
import net.blay09.mods.waystones.block.TileWaystone;
import net.blay09.mods.waystones.compat.VillageNamesCompat;
import net.minecraft.Block;
import net.minecraft.Chunk;
import net.minecraft.ChunkCoordinates;
import net.minecraft.ChunkPosition;
import net.minecraft.BiomeGenBase;
import net.minecraft.TileEntity;
import net.minecraft.WorldServer;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class WaystoneWorldgen implements IChunkLoadListener, ITickListener {
    public static final WaystoneWorldgen INSTANCE = new WaystoneWorldgen();
    private static final String END_SPIKE_KEY = "1:end_spike";
    private final ArrayDeque<PendingChunk> pending = new ArrayDeque<>();

    private WaystoneWorldgen() {
    }

    @Override
    public void onServerChunkLoad(Chunk chunk) {
        if (WaystoneConfig.enableWorldgen && chunk.worldObj instanceof WorldServer world) {
            PendingChunk loaded = new PendingChunk(
                    chunk.worldObj.provider.dimensionId, chunk.xPosition, chunk.zPosition);
            MinecraftServer server = MinecraftServer.getServer();
            if (chunk.isTerrainPopulated && server != null) {
                pending.remove(loaded);
                generateForChunk(server, world, loaded.chunkX(), loaded.chunkZ());
                return;
            }
            ChunkCoordinates spawn = chunk.worldObj.getSpawnPoint();
            if (loaded.dimension() == 0 && loaded.chunkX() == spawn.posX >> 4
                    && loaded.chunkZ() == spawn.posZ >> 4) {
                pending.addFirst(loaded);
            } else {
                pending.addLast(loaded);
            }
        }
    }

    @Override
    public void onServerTick(MinecraftServer server) {
        for (int i = 0; i < 2 && !pending.isEmpty(); i++) {
            PendingChunk next = pending.pollFirst();
            WorldServer world = server.worldServerForDimension(next.dimension());
            Chunk chunk = world == null ? null : world.getChunkIfItExists(next.chunkX(), next.chunkZ());
            if (chunk == null || !chunk.isTerrainPopulated) {
                if (chunk != null) {
                    pending.addLast(next);
                }
                continue;
            }
            generateForChunk(server, world, next.chunkX(), next.chunkZ());
        }
    }

    private void generateForChunk(MinecraftServer server, WorldServer world, int chunkX, int chunkZ) {
        for (WaystoneWorldgenRule rule : rules()) {
            Position structure = locate(world, rule.structure(), chunkX, chunkZ);
            if (structure == null || structure.x() >> 4 != chunkX || structure.z() >> 4 != chunkZ) {
                continue;
            }
            String key = rule.structure().equals("end_spike") ? END_SPIKE_KEY
                    : world.provider.dimensionId + ":" + rule.structure() + ":" + structure.x() + ":" + structure.z();
            WaystoneWorldgenData data = WaystoneWorldgenData.get(server);
            if (data.isProcessed(key)) {
                continue;
            }
            int biome = world.getBiomeGenForCoords(structure.x(), structure.z()).biomeID;
            if (!rule.accepts(world.provider.dimensionId, biome)
                    || !matchesTempleBiome(rule.structure(), world.getBiomeGenForCoords(structure.x(), structure.z()))) {
                data.markProcessed(key);
                continue;
            }
            Random random = new Random(world.getSeed() ^ key.hashCode() * 341873128712L);
            if (random.nextDouble() > rule.chance()) {
                data.markProcessed(key);
                continue;
            }
            Position placement = findPlacement(world, structure, rule.structure());
            if (placement != null) {
                place(server, world, placement, rule);
            }
            data.markProcessed(key);
        }
    }

    private static Position locate(WorldServer world, String structure, int chunkX, int chunkZ) {
        int centerX = (chunkX << 4) + 8;
        int centerZ = (chunkZ << 4) + 8;
        if (structure.equals("world_spawn")) {
            ChunkCoordinates spawn = world.getSpawnPoint();
            return new Position(spawn.posX, spawn.posY, spawn.posZ);
        }
        if (structure.equals("end_spike")) {
            if (world.provider.dimensionId != 1) {
                return null;
            }
            ChunkCoordinates spawn = world.getSpawnPoint();
            return new Position(spawn.posX, spawn.posY, spawn.posZ);
        }
        String vanillaName = switch (structure) {
            case "village" -> "Village";
            case "stronghold" -> "Stronghold";
            case "fortress" -> "Fortress";
            case "temple_desert", "temple_jungle" -> "Temple";
            default -> null;
        };
        if (vanillaName == null) {
            return null;
        }
        ChunkPosition found = world.findClosestStructure(vanillaName, centerX, 64, centerZ);
        return found == null ? null : new Position(found.x, found.y, found.z);
    }

    private static boolean matchesTempleBiome(String structure, BiomeGenBase biome) {
        if (structure.equals("temple_desert")) {
            return biome == BiomeGenBase.desert || biome == BiomeGenBase.desertHills;
        }
        if (structure.equals("temple_jungle")) {
            return biome == BiomeGenBase.jungle || biome == BiomeGenBase.jungleHills;
        }
        return true;
    }

    private static Position findPlacement(WorldServer world, Position origin, String structure) {
        boolean surface = structure.equals("village") || structure.startsWith("temple")
                || structure.equals("world_spawn") || structure.equals("end_spike");
        for (int radius = 0; radius <= 8; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int x = origin.x() + dx;
                    int z = origin.z() + dz;
                    int baseY = surface ? world.getTopSolidOrLiquidBlock(x, z) : origin.y();
                    for (int dy = 0; dy <= (surface ? 0 : 8); dy++) {
                        int y = baseY + dy;
                        if (y > 1 && y < world.getHeight() - 2
                                && world.isBlockSolid(x, y - 1, z)
                                && world.isAirOrPassableBlock(x, y, z, true)
                                && world.isAirOrPassableBlock(x, y + 1, z, true)) {
                            return new Position(x, y, z);
                        }
                    }
                }
            }
        }
        return null;
    }

    private static void place(MinecraftServer server, WorldServer world, Position pos, WaystoneWorldgenRule rule) {
        int variant = rule.variant() >= 0 ? rule.variant() : automaticVariant(world, pos);
        BlockWaystone block = WaystoneContent.getWaystoneBlock(variant);
        world.setBlock(pos.x(), pos.y(), pos.z(), block.blockID, 0, 3);
        world.setBlock(pos.x(), pos.y() + 1, pos.z(), block.blockID, 1, 3);
        TileEntity baseEntity = world.getBlockTileEntity(pos.x(), pos.y(), pos.z());
        TileEntity upperEntity = world.getBlockTileEntity(pos.x(), pos.y() + 1, pos.z());
        if (!(baseEntity instanceof TileWaystone base)) {
            return;
        }
        if (upperEntity instanceof TileWaystone upper) {
            upper.setUpperPart(true);
        }
        String generatedName = null;
        if (rule.fixedName().isEmpty() && rule.structure().equals("village")
                && WaystoneConfig.villageNamesCompat) {
            generatedName = VillageNamesCompat.ensureVillageName(world, pos.x(), pos.y(), pos.z());
        }
        String requestedName = rule.fixedName().isEmpty() ? generatedName : rule.fixedName();
        String name = requestedName == null || requestedName.isEmpty()
                ? "" : uniqueName(server, requestedName);
        base.setWaystoneName(name);
        base.setGlobal(rule.forceGlobal());
        base.setForceGlobalOnActivation(rule.forceGlobal() && !rule.autoActivateGlobal());
        if (rule.forceGlobal() && rule.autoActivateGlobal()) {
            GlobalWaystoneData.get(server).put(new WaystoneEntry(base));
        }
        Waystones.LOGGER.info("Generated {} at {},{},{} in dimension {}",
                block.getRegistryName(), pos.x(), pos.y(), pos.z(), world.provider.dimensionId);
    }

    private static int automaticVariant(WorldServer world, Position pos) {
        if (world.provider.dimensionId == -1) {
            return TileWaystone.VARIANT_NETHER;
        }
        if (world.provider.dimensionId == 1) {
            return TileWaystone.VARIANT_END;
        }
        Block below = world.getBlock(pos.x(), pos.y() - 1, pos.z());
        if (below == Block.sand || below == Block.sandStone
                || configuredPathBlock(below, WaystoneConfig.sandyWaystonePathBlocks)) {
            return TileWaystone.VARIANT_SANDSTONE;
        }
        if (below == Block.cobblestoneMossy
                || configuredPathBlock(below, WaystoneConfig.mossyWaystonePathBlocks)) {
            return TileWaystone.VARIANT_MOSSY;
        }
        return TileWaystone.VARIANT_STONE;
    }

    private static boolean configuredPathBlock(Block block, String[] configuredNames) {
        if (block == null) {
            return false;
        }
        String unlocalized = block.getUnlocalizedName();
        String simpleName = unlocalized == null ? "" : unlocalized.replaceFirst("^(tile|block)\\.", "");
        for (String configuredName : configuredNames) {
            String value = configuredName.trim().toLowerCase(Locale.ROOT);
            int separator = value.indexOf(':');
            String path = separator >= 0 ? value.substring(separator + 1) : value;
            if (path.equals(simpleName.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static String uniqueName(MinecraftServer server, String base) {
        String name = base;
        int suffix = 2;
        while (GlobalWaystoneData.get(server).findByName(name) != null) {
            name = base + " " + suffix++;
        }
        return name;
    }

    private static List<WaystoneWorldgenRule> rules() {
        List<WaystoneWorldgenRule> result = new ArrayList<>();
        for (String encoded : WaystoneConfig.structureWaystoneRules) {
            WaystoneWorldgenRule rule = WaystoneWorldgenRule.parse(encoded);
            if (rule != null) {
                result.add(rule);
            }
        }
        return result;
    }

    public static void onEndSpikeGenerated(net.minecraft.World rawWorld, Random random,
                                           int originX, int originY, int originZ) {
        if (!WaystoneConfig.enableWorldgen || !(rawWorld instanceof WorldServer world)
                || world.provider.dimensionId != 1) {
            return;
        }
        MinecraftServer server = MinecraftServer.getServer();
        WaystoneWorldgenData data = WaystoneWorldgenData.get(server);
        if (data.isProcessed(END_SPIKE_KEY)) {
            return;
        }
        WaystoneWorldgenRule endRule = null;
        for (WaystoneWorldgenRule rule : rules()) {
            if (rule.structure().equals("end_spike")) {
                endRule = rule;
                break;
            }
        }
        if (endRule == null) {
            data.markProcessed(END_SPIKE_KEY);
            return;
        }
        int biome = world.getBiomeGenForCoords(originX, originZ).biomeID;
        if (!endRule.accepts(1, biome) || random.nextDouble() > endRule.chance()) {
            data.markProcessed(END_SPIKE_KEY);
            return;
        }
        for (int attempt = 0; attempt < 32; attempt++) {
            int x = originX + random.nextInt(41) - 20;
            int z = originZ + random.nextInt(41) - 20;
            int radius = 1 + random.nextInt(3);
            int chunkX = x >> 4;
            int chunkZ = z >> 4;
            if (!world.getChunkProvider().chunkExists(chunkX, chunkZ)
                    || ((x - radius) >> 4) != chunkX || ((x + radius) >> 4) != chunkX
                    || ((z - radius) >> 4) != chunkZ || ((z + radius) >> 4) != chunkZ) {
                continue;
            }
            int top = world.getTopSolidOrLiquidBlock(x, z);
            int height = 1 + random.nextInt(3);
            if (!validEndSpikeSite(world, x, top, z, radius, height)) {
                continue;
            }
            for (int y = top; y < top + height; y++) {
                for (int px = x - radius; px <= x + radius; px++) {
                    for (int pz = z - radius; pz <= z + radius; pz++) {
                        int dx = px - x;
                        int dz = pz - z;
                        if (dx * dx + dz * dz <= radius * radius + 1) {
                            world.setBlock(px, y, pz, Block.obsidian.blockID, 0, 2);
                        }
                    }
                }
            }
            place(server, world, new Position(x, top + height, z), endRule);
            data.markProcessed(END_SPIKE_KEY);
            return;
        }
    }

    private static boolean validEndSpikeSite(WorldServer world, int x, int top, int z,
                                             int radius, int height) {
        for (int px = x - radius; px <= x + radius; px++) {
            for (int pz = z - radius; pz <= z + radius; pz++) {
                int dx = px - x;
                int dz = pz - z;
                if (dx * dx + dz * dz > radius * radius + 1) {
                    continue;
                }
                if (world.getBlock(px, top - 1, pz) != Block.whiteStone) {
                    return false;
                }
                for (int y = top; y <= top + height + 1; y++) {
                    if (!world.isAirBlock(px, y, pz)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private record PendingChunk(int dimension, int chunkX, int chunkZ) {
    }

    private record Position(int x, int y, int z) {
    }
}
