package net.blay09.mods.waystones;

import moddedmite.rustedironcore.network.Network;
import net.blay09.mods.waystones.network.S2CLocalizedMessage;
import net.minecraft.EntityPlayer;
import net.minecraft.ServerPlayer;
import net.minecraft.StatCollector;

public final class WaystoneMessages {
    private WaystoneMessages() {
    }

    public static void send(EntityPlayer player, String key, Object... arguments) {
        if (player instanceof ServerPlayer serverPlayer) {
            String[] values = new String[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                values[i] = String.valueOf(arguments[i]);
            }
            Network.sendToClient(serverPlayer, new S2CLocalizedMessage(key, values));
        } else {
            player.addChatMessage(StatCollector.translateToLocalFormatted(key, arguments));
        }
    }
}
