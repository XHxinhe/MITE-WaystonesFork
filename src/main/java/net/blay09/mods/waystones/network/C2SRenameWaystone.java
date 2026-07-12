package net.blay09.mods.waystones.network;

import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.blay09.mods.waystones.PlayerWaystoneData;
import net.blay09.mods.waystones.WaystoneEntry;
import net.blay09.mods.waystones.GlobalWaystoneData;
import net.blay09.mods.waystones.WaystoneManager;
import net.blay09.mods.waystones.block.TileWaystone;
import net.minecraft.EntityPlayer;
import net.minecraft.ResourceLocation;
import net.minecraft.ServerPlayer;
import net.minecraft.TileEntity;
import net.minecraft.server.MinecraftServer;

public final class C2SRenameWaystone implements Packet {
    public static final ResourceLocation CHANNEL = new ResourceLocation("waystones", "rename");

    private final int x;
    private final int y;
    private final int z;
    private final String name;
    private final boolean global;
    private final boolean reopenMenu;

    public C2SRenameWaystone(PacketByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readString(), buf.readBoolean(), buf.readBoolean());
    }

    public C2SRenameWaystone(int x, int y, int z, String name, boolean global) {
        this(x, y, z, name, global, false);
    }

    public C2SRenameWaystone(int x, int y, int z, String name, boolean global, boolean reopenMenu) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
        this.global = global;
        this.reopenMenu = reopenMenu;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeString(name);
        buf.writeBoolean(global);
        buf.writeBoolean(reopenMenu);
    }

    @Override
    public void apply(EntityPlayer player) {
        if (player == null || player.onClient() || player.getDistanceSqToBlock(x, y, z) > 100.0D) {
            return;
        }
        TileEntity tile = player.worldObj.getBlockTileEntity(x, y, z);
        if (!(tile instanceof TileWaystone waystone)) {
            return;
        }
        if (net.blay09.mods.waystones.WaystoneConfig.creativeModeOnly && !player.inCreativeMode()) {
            return;
        }
        String cleaned = name == null ? "" : name.replaceAll("[\\p{Cntrl}]", "").trim();
        if (cleaned.length() > 32) {
            cleaned = cleaned.substring(0, 32);
        }
        final String validatedName = cleaned;
        WaystoneEntry oldEntry = new WaystoneEntry(waystone);
        GlobalWaystoneData globals = GlobalWaystoneData.get(MinecraftServer.getServer());
        WaystoneEntry occupied = globals.findByName(validatedName);
        boolean playerNameOccupied = PlayerWaystoneData.getWaystones(player).stream()
                .anyMatch(entry -> entry.name().equalsIgnoreCase(validatedName) && !entry.samePosition(oldEntry));
        if (validatedName.isEmpty() || playerNameOccupied
                || (occupied != null && !occupied.samePosition(oldEntry))) {
            net.blay09.mods.waystones.WaystoneMessages.send(
                    player, "message.waystones.name_occupied", validatedName);
            return;
        }
        globals.remove(oldEntry);
        waystone.setWaystoneName(validatedName);
        if (waystone.getWaystoneOwner().isEmpty()) {
            waystone.setWaystoneOwner(player.getEntityName());
        }
        waystone.setGlobal(global && player.inCreativeMode());
        WaystoneEntry newEntry = new WaystoneEntry(waystone);
        net.minecraft.Packet tileUpdate = waystone.getDescriptionPacket();
        if (tileUpdate != null) {
            MinecraftServer.getServer().getConfigurationManager().sendToAllNear(
                    x + 0.5D, y + 0.5D, z + 0.5D, 64.0D, player.dimension, tileUpdate);
        }
        if (newEntry.global()) {
            globals.put(newEntry);
            PlayerWaystoneData.remove(player, newEntry);
        }
        PlayerWaystoneData.activate(player, newEntry);
        @SuppressWarnings("unchecked")
        java.util.List<Object> onlinePlayers = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        for (Object online : onlinePlayers) {
            if (online instanceof EntityPlayer onlinePlayer) {
                if (newEntry.global()) {
                    PlayerWaystoneData.rename(onlinePlayer, oldEntry, newEntry);
                    PlayerWaystoneData.remove(onlinePlayer, oldEntry);
                } else if (oldEntry.global() && onlinePlayer != player) {
                    PlayerWaystoneData.forget(onlinePlayer, oldEntry);
                } else {
                    PlayerWaystoneData.rename(onlinePlayer, oldEntry, newEntry);
                }
                if (onlinePlayer instanceof net.minecraft.ServerPlayer serverPlayer
                        && WaystoneManager.findAccessible(onlinePlayer, newEntry) != null) {
                    moddedmite.rustedironcore.network.Network.sendToClient(serverPlayer,
                            new S2CMapWaypoint(oldEntry.name(), newEntry.name(), newEntry.dimension(),
                                    newEntry.x(), newEntry.y(), newEntry.z()));
                }
            }
        }
        WaystoneManager.sendAllPlayerStates();
        if (reopenMenu && player instanceof ServerPlayer serverPlayer) {
            WaystoneManager.openDestinationMenu(serverPlayer, false, false, newEntry);
        }
    }

    @Override
    public ResourceLocation getChannel() {
        return CHANNEL;
    }
}
