package net.blay09.mods.waystones.network;

import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.minecraft.EntityPlayer;
import net.minecraft.I18n;
import net.minecraft.ResourceLocation;

public final class S2CLocalizedMessage implements Packet {
    public static final ResourceLocation CHANNEL = new ResourceLocation("waystones", "message");

    private final String key;
    private final String[] arguments;

    public S2CLocalizedMessage(PacketByteBuf buf) {
        key = buf.readString();
        int count = Math.max(0, Math.min(16, buf.readInt()));
        arguments = new String[count];
        for (int i = 0; i < count; i++) {
            arguments[i] = buf.readString();
        }
    }

    public S2CLocalizedMessage(String key, String[] arguments) {
        this.key = key;
        this.arguments = arguments;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(key);
        buf.writeInt(arguments.length);
        for (String argument : arguments) {
            buf.writeString(argument);
        }
    }

    @Override
    public void apply(EntityPlayer player) {
        if (player != null) {
            player.addChatMessage(I18n.getStringParams(key, (Object[]) arguments));
        }
    }

    @Override
    public ResourceLocation getChannel() {
        return CHANNEL;
    }
}
