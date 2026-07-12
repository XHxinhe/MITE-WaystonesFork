package net.blay09.mods.waystones.network;

import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.blay09.mods.waystones.PlayerWaystoneData;
import net.blay09.mods.waystones.WaystoneEntry;
import net.blay09.mods.waystones.WaystoneManager;
import net.minecraft.EntityPlayer;
import net.minecraft.ResourceLocation;

public final class C2STeleport implements Packet {
    public static final ResourceLocation CHANNEL = new ResourceLocation("waystones", "teleport");

    private final WaystoneEntry entry;
    private final boolean warpStone;
    private final boolean freeWarp;

    public C2STeleport(PacketByteBuf buf) {
        entry = WaystoneEntry.read(buf);
        warpStone = buf.readBoolean();
        freeWarp = buf.readBoolean();
    }

    public C2STeleport(WaystoneEntry entry, boolean warpStone) {
        this(entry, warpStone, false);
    }

    public C2STeleport(WaystoneEntry entry, boolean warpStone, boolean freeWarp) {
        this.entry = entry;
        this.warpStone = warpStone;
        this.freeWarp = freeWarp;
    }

    @Override
    public void write(PacketByteBuf buf) {
        entry.write(buf);
        buf.writeBoolean(warpStone);
        buf.writeBoolean(freeWarp);
    }

    @Override
    public void apply(EntityPlayer player) {
        if (player == null || player.onClient()) {
            return;
        }
        WaystoneManager.MenuAuthorization authorization =
                WaystoneManager.consumeMenuAuthorization(player, warpStone, freeWarp);
        if (authorization == null) {
            return;
        }
        boolean cooldownExempt = WaystoneManager.bypassesWarpStoneCooldown(player, entry);
        if (!freeWarp && !cooldownExempt && !player.inCreativeMode()) {
            long elapsed = System.currentTimeMillis() - PlayerWaystoneData.getLastWarpStoneUse(player);
            if (elapsed < WaystoneManager.warpStoneCooldownMs()) {
                net.blay09.mods.waystones.WaystoneMessages.send(player, "message.waystones.cooldown",
                        (WaystoneManager.warpStoneCooldownMs() - elapsed + 999L) / 1000L);
                return;
            }
        }
        if (freeWarp && !cooldownExempt && !player.inCreativeMode()) {
            long elapsed = System.currentTimeMillis() - PlayerWaystoneData.getLastFreeWarpUse(player);
            if (elapsed < WaystoneManager.freeWarpCooldownMs()) {
                return;
            }
        }
        int xpCost = net.blay09.mods.waystones.WaystoneXpCost.get(player, authorization.origin(), entry);
        if (!freeWarp && !net.blay09.mods.waystones.WaystoneXpCost.canAfford(player, xpCost)) {
            net.blay09.mods.waystones.WaystoneMessages.send(player, "message.waystones.not_enough_xp", xpCost);
            return;
        }
        if (WaystoneManager.teleport(player, entry)) {
            if (!freeWarp) {
                net.blay09.mods.waystones.WaystoneXpCost.deduct(player, xpCost);
            }
            if (!freeWarp && !cooldownExempt && !player.inCreativeMode()) {
                PlayerWaystoneData.setLastWarpStoneUse(player, System.currentTimeMillis());
            }
            if (freeWarp && !cooldownExempt && !player.inCreativeMode()) {
                PlayerWaystoneData.setLastFreeWarpUse(player, System.currentTimeMillis());
            }
            if (player instanceof net.minecraft.ServerPlayer serverPlayer) {
                WaystoneManager.sendPlayerState(serverPlayer);
            }
        }
    }

    @Override
    public ResourceLocation getChannel() {
        return CHANNEL;
    }
}
