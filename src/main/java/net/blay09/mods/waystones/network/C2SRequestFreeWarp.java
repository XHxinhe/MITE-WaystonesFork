package net.blay09.mods.waystones.network;

import moddedmite.rustedironcore.network.Network;
import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.blay09.mods.waystones.PlayerWaystoneData;
import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.WaystoneManager;
import net.blay09.mods.waystones.WaystoneMessages;
import net.minecraft.EntityPlayer;
import net.minecraft.ResourceLocation;
import net.minecraft.ServerPlayer;

public final class C2SRequestFreeWarp implements Packet {
    public static final ResourceLocation CHANNEL = new ResourceLocation("waystones", "request_free_warp");

    public C2SRequestFreeWarp() {
    }

    public C2SRequestFreeWarp(PacketByteBuf ignored) {
    }

    @Override
    public void write(PacketByteBuf buf) {
    }

    @Override
    public void apply(EntityPlayer player) {
        if (!(player instanceof ServerPlayer serverPlayer) || !WaystoneConfig.teleportButton) {
            return;
        }
        if (WaystoneManager.getAccessibleWaystones(player).isEmpty()) {
            WaystoneMessages.send(player, "message.waystones.none_activated");
            return;
        }
        long remaining = WaystoneManager.freeWarpCooldownMs()
                - (System.currentTimeMillis() - PlayerWaystoneData.getLastFreeWarpUse(player));
        if (!player.inCreativeMode() && remaining > 0) {
            WaystoneMessages.send(player, "message.waystones.free_cooldown", (remaining + 999) / 1000);
            return;
        }
        if (WaystoneConfig.teleportButtonReturnOnly) {
            WaystoneManager.requestReturnConfirmation(serverPlayer, false, true);
        } else {
            WaystoneManager.openDestinationMenu(serverPlayer, false, true);
        }
    }

    @Override
    public ResourceLocation getChannel() {
        return CHANNEL;
    }
}
