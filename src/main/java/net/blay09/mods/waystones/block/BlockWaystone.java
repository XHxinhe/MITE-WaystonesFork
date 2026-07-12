package net.blay09.mods.waystones.block;

import moddedmite.rustedironcore.network.Network;
import net.blay09.mods.waystones.PlayerWaystoneData;
import net.blay09.mods.waystones.WaystoneEntry;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.GlobalWaystoneData;
import net.blay09.mods.waystones.WaystoneManager;
import net.blay09.mods.waystones.WaystoneMessages;
import net.blay09.mods.waystones.ClientWaystoneState;
import net.blay09.mods.waystones.network.S2CActivationEffect;
import net.blay09.mods.waystones.network.S2COpenName;
import net.blay09.mods.waystones.network.S2CWaystoneList;
import net.minecraft.BlockConstants;
import net.minecraft.BlockBreakInfo;
import net.minecraft.ChunkCoordinates;
import net.minecraft.Block;
import net.minecraft.BlockContainer;
import net.minecraft.CreativeTabs;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumFace;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.IBlockAccess;
import net.minecraft.Material;
import net.minecraft.EnumParticle;
import net.minecraft.ServerPlayer;
import net.minecraft.TileEntity;
import net.minecraft.World;
import net.xiaoyu233.fml.reload.utils.IdUtil;
import net.minecraft.server.MinecraftServer;

import java.util.Random;

public final class BlockWaystone extends BlockContainer {
    public static final int RENDER_TYPE = IdUtil.getNextRenderType();
    private Icon icon;
    private final int variant;
    private final String registryName;

    public BlockWaystone(int id, int variant, String registryName) {
        super(id, Material.stone, new BlockConstants());
        this.variant = variant;
        this.registryName = registryName;
        setHardness(5.0F);
        setResistance(2000.0F);
        setStepSound(soundStoneFootstep);
        setCreativeTab(CreativeTabs.tabDecorations);
        setLightValue(WaystoneConfig.waystoneLightLevel);
    }

    @Override
    public void registerIcons(IconRegister register) {
        icon = register.registerIcon(Waystones.MOD_ID + ":" + registryName);
    }

    @Override
    public Icon getIcon(int side, int metadata) {
        return icon;
    }

    @Override
    public TileEntity createNewTileEntity(World world) {
        return new TileWaystone();
    }

    public int getVariant() {
        return variant;
    }

    public String getRegistryName() {
        return registryName;
    }

    @Override
    public int getRenderType() {
        return RENDER_TYPE;
    }

    @Override
    public int dropBlockAsItself(BlockBreakInfo info) {
        return WaystoneConfig.disableWaystoneDrops ? 0 : super.dropBlockAsItself(info);
    }

    @Override
    public boolean isStandardFormCube(boolean[] result, int metadata) {
        return false;
    }

    @Override
    public boolean canBePlacedAt(World world, int x, int y, int z, int metadata) {
        return super.canBePlacedAt(world, x, y, z, metadata)
                && world.isAirOrPassableBlock(x, y + 1, z, true);
    }

    @Override
    public boolean onBlockPlacedMITE(World world, int x, int y, int z, int metadata, Entity placer, boolean testOnly) {
        if (WaystoneConfig.creativeModeOnly && placer instanceof EntityPlayer player && !player.inCreativeMode()) {
            return false;
        }
        if (!testOnly && !world.isRemote) {
            TileWaystone tile = getTile(world, x, y, z);
            if (tile != null) {
                tile.setFacing((int) Math.floor((placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3);
                if (placer instanceof EntityPlayer player) {
                    tile.setWaystoneOwner(player.getEntityName());
                }
            }
            world.setBlock(x, y + 1, z, blockID, 1, 3);
            TileWaystone upper = getTile(world, x, y + 1, z);
            if (upper != null) {
                upper.setUpperPart(true);
            }
            if (tile != null && placer instanceof ServerPlayer serverPlayer) {
                Network.sendToClient(serverPlayer, new S2COpenName(
                        tile.xCoord, tile.yCoord, tile.zCoord,
                        tile.getWaystoneName(), tile.isGlobal()));
            }
        }
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    EnumFace face, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }
        TileWaystone tile = getBaseTile(world, x, y, z);
        if (tile == null || !(player instanceof ServerPlayer serverPlayer)) {
            return true;
        }
        if (tile.getWaystoneName().isEmpty()) {
            if (WaystoneConfig.creativeModeOnly && !player.inCreativeMode()) {
                return true;
            }
            Network.sendToClient(serverPlayer, new S2COpenName(tile.xCoord, tile.yCoord, tile.zCoord,
                    tile.getWaystoneName(), tile.isGlobal()));
            return true;
        }

        boolean alreadyActive = WaystoneManager.findAccessible(player, new WaystoneEntry(tile)) != null;
        if (tile.shouldForceGlobalOnActivation()) {
            tile.setGlobal(true);
            tile.setForceGlobalOnActivation(false);
            GlobalWaystoneData.get(MinecraftServer.getServer()).put(new WaystoneEntry(tile));
        }
        WaystoneEntry entry = new WaystoneEntry(tile);
        PlayerWaystoneData.activate(player, entry);
        WaystoneManager.sendPlayerState(serverPlayer);
        WaystoneManager.sendMapWaypoint(serverPlayer, entry);
        if (WaystoneConfig.setSpawnPoint) {
            player.setSpawnChunk(new ChunkCoordinates(
                    tile.xCoord + WaystoneManager.offsetX(tile.getFacing()),
                    tile.yCoord,
                    tile.zCoord + WaystoneManager.offsetZ(tile.getFacing())), true);
        }
        if (!alreadyActive) {
            WaystoneMessages.send(player, "message.waystones.activated", entry.name());
            Network.sendToAllPlayers(new S2CActivationEffect(
                    world.provider.dimensionId, tile.xCoord, tile.yCoord, tile.zCoord));
        }
        if (alreadyActive || player.isSneaking()) {
            WaystoneManager.openDestinationMenu(serverPlayer, false, false, entry);
        }
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, int blockId, int metadata) {
        if (!world.isRemote) {
            TileWaystone base = metadata == 1 ? getTile(world, x, y - 1, z) : getTile(world, x, y, z);
            if (base != null && base.isGlobal() && MinecraftServer.getServer() != null) {
                GlobalWaystoneData.get(MinecraftServer.getServer()).remove(new WaystoneEntry(base));
                WaystoneManager.sendAllPlayerStates();
            }
            if (metadata == 1 && world.getBlock(x, y - 1, z) == this) {
                world.setBlockToAir(x, y - 1, z);
            } else if (metadata != 1 && world.getBlock(x, y + 1, z) == this) {
                world.setBlockToAir(x, y + 1, z);
            }
        }
        super.breakBlock(world, x, y, z, blockId, metadata);
    }

    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random random) {
        if (!WaystoneConfig.particles || world.getBlockMetadata(x, y, z) == 1 || random.nextFloat() > 0.75F
                || !ClientWaystoneState.isActive(world.provider.dimensionId, x, y, z)) {
            return;
        }
        double px = x + 0.5D + (random.nextDouble() - 0.5D) * 1.5D;
        double pz = z + 0.5D + (random.nextDouble() - 0.5D) * 1.5D;
        world.spawnParticle(EnumParticle.portal_underworld, px, y + 0.5D, pz, 0.0D, 0.0D, 0.0D);
        world.spawnParticle(EnumParticle.enchantmenttable, px, y + 0.5D, pz, 0.0D, 0.0D, 0.0D);
    }

    private static TileWaystone getTile(World world, int x, int y, int z) {
        TileEntity tile = world.getBlockTileEntity(x, y, z);
        return tile instanceof TileWaystone ? (TileWaystone) tile : null;
    }

    private static TileWaystone getBaseTile(World world, int x, int y, int z) {
        TileWaystone tile = getTile(world, x, y, z);
        if (tile != null && tile.isUpperPart()) {
            return getTile(world, x, y - 1, z);
        }
        return tile;
    }
}
