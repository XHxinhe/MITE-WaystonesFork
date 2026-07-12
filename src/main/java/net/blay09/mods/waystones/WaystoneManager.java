package net.blay09.mods.waystones;

import net.blay09.mods.waystones.block.TileWaystone;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.Potion;
import net.minecraft.PotionEffect;
import net.minecraft.TileEntity;
import net.minecraft.WorldServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.ServerPlayer;
import moddedmite.rustedironcore.network.Network;
import net.blay09.mods.waystones.network.S2COpenReturnConfirm;
import net.blay09.mods.waystones.network.S2CTeleportEffect;
import net.blay09.mods.waystones.network.S2CWaystoneList;
import net.blay09.mods.waystones.network.S2CWaystoneState;
import net.blay09.mods.waystones.network.S2CMapWaypoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WaystoneManager {
    private static final long RETURN_CONFIRM_TIMEOUT_MS = 15_000L;
    private static final long MENU_TIMEOUT_MS = 30_000L;
    private static final Map<String, PendingReturn> PENDING_RETURNS = new HashMap<>();
    private static final Map<String, PendingMenu> PENDING_MENUS = new HashMap<>();

    private WaystoneManager() {
    }

    public static boolean teleport(EntityPlayer player, WaystoneEntry requested) {
        WaystoneEntry allowed = findAccessible(player, requested);
        if (allowed == null) {
            WaystoneMessages.send(player, "message.waystones.not_activated");
            return false;
        }

        MinecraftServer server = MinecraftServer.getServer();
        boolean dimensionWarp = player.dimension != allowed.dimension();
        boolean dimensionAllowed = allowed.global()
                ? WaystoneConfig.globalInterDimension : WaystoneConfig.interDimension;
        if (dimensionWarp && !dimensionAllowed) {
            WaystoneMessages.send(player, "message.waystones.interdimensional_disabled");
            return false;
        }
        WorldServer targetWorld = server.worldServerForDimension(allowed.dimension());
        targetWorld.getChunkProvider().loadChunk(allowed.x() >> 4, allowed.z() >> 4);
        TileEntity tile = targetWorld.getBlockTileEntity(allowed.x(), allowed.y(), allowed.z());
        if (!(tile instanceof TileWaystone waystone)) {
            if (allowed.global()) {
                GlobalWaystoneData.get(server).remove(allowed);
            } else {
                PlayerWaystoneData.remove(player, allowed);
            }
            if (player instanceof ServerPlayer serverPlayer) {
                sendPlayerState(serverPlayer);
            }
            WaystoneMessages.send(player, "message.waystones.missing");
            return false;
        }

        WaystoneEntry current = new WaystoneEntry(waystone);
        PlayerWaystoneData.activate(player, current);
        if (player instanceof ServerPlayer serverPlayer) {
            sendPlayerState(serverPlayer);
            sendMapWaypoint(serverPlayer, current);
        }
        Network.sendToAllPlayers(new S2CTeleportEffect(
                player.dimension, player.posX, player.posY, player.posZ));
        player.addPotionEffect(new PotionEffect(Potion.blindness.id, 20, 3));
        if (player.dimension != current.dimension()) {
            WaystoneTeleportContext.runWithoutPortal(player, () -> player.travelToDimension(current.dimension()));
        }
        ExitPosition exit = findExitPosition(waystone);
        player.rotationYaw = yawForFacing(exit.facing());
        player.setPositionAndUpdate(exit.x() + 0.5D, exit.y() + 0.1D, exit.z() + 0.5D);
        player.fallDistance = 0.0F;
        Network.sendToAllPlayers(new S2CTeleportEffect(
                current.dimension(), exit.x() + 0.5D, exit.y() + 0.1D, exit.z() + 0.5D));
        return true;
    }

    public static List<WaystoneEntry> getAccessibleWaystones(EntityPlayer player) {
        List<WaystoneEntry> result = new ArrayList<>(PlayerWaystoneData.getWaystones(player));
        for (WaystoneEntry global : GlobalWaystoneData.get(MinecraftServer.getServer()).entries()) {
            result.removeIf(entry -> entry.samePosition(global));
            result.add(global.asGlobal(true));
        }
        return result;
    }

    public static WaystoneEntry findAccessible(EntityPlayer player, WaystoneEntry requested) {
        for (WaystoneEntry entry : getAccessibleWaystones(player)) {
            if (entry.samePosition(requested)) {
                return entry;
            }
        }
        return null;
    }

    public static boolean bypassesWarpStoneCooldown(EntityPlayer player, WaystoneEntry requested) {
        WaystoneEntry allowed = findAccessible(player, requested);
        return allowed != null && allowed.global() && WaystoneConfig.globalNoCooldown;
    }

    public static boolean hasCooldownFreeGlobalWaystone(EntityPlayer player) {
        return WaystoneConfig.globalNoCooldown
                && getAccessibleWaystones(player).stream().anyMatch(WaystoneEntry::global);
    }

    public static long warpStoneCooldownMs() {
        return WaystoneConfig.warpStoneCooldownSeconds * 1000L;
    }

    public static void requestReturnConfirmation(ServerPlayer player, boolean consumeScroll, boolean freeWarp) {
        WaystoneEntry target = PlayerWaystoneData.getLast(player);
        if (target == null) {
            WaystoneMessages.send(player, "message.waystones.none_activated");
            return;
        }
        PENDING_RETURNS.put(player.getEntityName(),
                new PendingReturn(target, System.currentTimeMillis(), consumeScroll, freeWarp));
        Network.sendToClient(player, new S2COpenReturnConfirm(target));
    }

    public static void openDestinationMenu(ServerPlayer player, boolean warpStone, boolean freeWarp) {
        openDestinationMenu(player, warpStone, freeWarp, null);
    }

    public static void openDestinationMenu(ServerPlayer player, boolean warpStone, boolean freeWarp,
                                           WaystoneEntry origin) {
        PENDING_MENUS.put(player.getEntityName(),
                new PendingMenu(warpStone, freeWarp, origin, System.currentTimeMillis()));
        Network.sendToClient(player,
                new S2CWaystoneList(getAccessibleWaystones(player), warpStone, freeWarp, origin));
    }

    public static MenuAuthorization consumeMenuAuthorization(EntityPlayer player, boolean warpStone, boolean freeWarp) {
        PendingMenu pending = PENDING_MENUS.remove(player.getEntityName());
        boolean valid = pending != null
                && System.currentTimeMillis() - pending.createdAt() <= MENU_TIMEOUT_MS
                && pending.warpStone() == warpStone
                && pending.freeWarp() == freeWarp;
        return valid ? new MenuAuthorization(pending.origin()) : null;
    }

    public static void confirmReturn(EntityPlayer player) {
        PendingReturn pending = PENDING_RETURNS.remove(player.getEntityName());
        if (pending == null || System.currentTimeMillis() - pending.createdAt() > RETURN_CONFIRM_TIMEOUT_MS) {
            return;
        }
        ItemStack held = player.getHeldItemStack();
        WaystoneEntry currentTarget = PlayerWaystoneData.getLast(player);
        if ((pending.consumeScroll() && (held == null || held.getItem() != WaystoneContent.RETURN_SCROLL))
                || currentTarget == null || !currentTarget.samePosition(pending.target())) {
            return;
        }
        if (pending.freeWarp() && !player.inCreativeMode()) {
            long elapsed = System.currentTimeMillis() - PlayerWaystoneData.getLastFreeWarpUse(player);
            if (elapsed < freeWarpCooldownMs()) {
                return;
            }
        }
        boolean teleported = teleport(player, pending.target());
        if (teleported && pending.freeWarp()) {
            PlayerWaystoneData.setLastFreeWarpUse(player, System.currentTimeMillis());
        }
        if (teleported && pending.consumeScroll() && !player.inCreativeMode()) {
            if (--held.stackSize <= 0) {
                player.setHeldItemStack(null);
            }
        }
        if (teleported && player instanceof ServerPlayer serverPlayer) {
            sendPlayerState(serverPlayer);
        }
    }

    public static long freeWarpCooldownMs() {
        return WaystoneConfig.teleportButtonCooldownSeconds * 1000L;
    }

    public static int offsetX(int facing) {
        return switch (facing & 3) {
            case 1 -> -1;
            case 3 -> 1;
            default -> 0;
        };
    }

    public static int offsetZ(int facing) {
        return switch (facing & 3) {
            case 0 -> 1;
            case 2 -> -1;
            default -> 0;
        };
    }

    public static float yawForFacing(int facing) {
        return switch (facing & 3) {
            case 0 -> 0.0F;
            case 1 -> 90.0F;
            case 2 -> 180.0F;
            default -> -90.0F;
        };
    }

    private static ExitPosition findExitPosition(TileWaystone waystone) {
        int preferred = waystone.getFacing() & 3;
        int[] directions = {preferred, (preferred + 1) & 3, (preferred + 3) & 3, (preferred + 2) & 3};
        for (int distance : new int[]{2, 1}) {
            for (int direction : directions) {
                int x = waystone.xCoord + offsetX(direction) * distance;
                int y = waystone.yCoord;
                int z = waystone.zCoord + offsetZ(direction) * distance;
                if (waystone.getWorldObj().isAirOrPassableBlock(x, y, z, true)
                        && waystone.getWorldObj().isAirOrPassableBlock(x, y + 1, z, true)) {
                    return new ExitPosition(x, y, z, direction);
                }
            }
        }
        return new ExitPosition(waystone.xCoord + offsetX(preferred), waystone.yCoord,
                waystone.zCoord + offsetZ(preferred), preferred);
    }

    public static void sendPlayerState(ServerPlayer player) {
        Network.sendToClient(player, new S2CWaystoneState(
                getAccessibleWaystones(player), PlayerWaystoneData.getLast(player),
                PlayerWaystoneData.getLastFreeWarpUse(player),
                PlayerWaystoneData.getLastWarpStoneUse(player),
                PlayerWaystoneData.getPinnedNames(player)));
    }

    public static void sendMapWaypoint(ServerPlayer player, WaystoneEntry entry) {
        Network.sendToClient(player, new S2CMapWaypoint("", entry.name(), entry.dimension(),
                entry.x(), entry.y(), entry.z()));
    }

    @SuppressWarnings("unchecked")
    public static void sendAllPlayerStates() {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.getConfigurationManager() == null) {
            return;
        }
        for (Object player : server.getConfigurationManager().playerEntityList) {
            if (player instanceof ServerPlayer serverPlayer) {
                sendPlayerState(serverPlayer);
            }
        }
    }

    private record PendingReturn(WaystoneEntry target, long createdAt, boolean consumeScroll, boolean freeWarp) {
    }

    private record PendingMenu(boolean warpStone, boolean freeWarp, WaystoneEntry origin, long createdAt) {
    }

    public record MenuAuthorization(WaystoneEntry origin) {
    }

    private record ExitPosition(int x, int y, int z, int facing) {
    }
}
