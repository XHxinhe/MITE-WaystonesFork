package net.blay09.mods.waystones.network;

import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.blay09.mods.waystones.WaystoneConfig;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumParticle;
import net.minecraft.ResourceLocation;

import java.util.Random;

public final class S2CTeleportEffect implements Packet {
    public static final ResourceLocation CHANNEL = new ResourceLocation("waystones", "teleport_fx");

    private final int dimension;
    private final double x;
    private final double y;
    private final double z;

    public S2CTeleportEffect(PacketByteBuf buf) {
        this(buf.readInt(), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public S2CTeleportEffect(int dimension, double x, double y, double z) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(dimension);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }

    @Override
    public void apply(EntityPlayer player) {
        if (player == null || player.worldObj == null || player.dimension != dimension
                || player.getDistanceSq(x, y, z) > 4096.0D) {
            return;
        }
        if (WaystoneConfig.sounds && !WaystoneConfig.disableTeleportSound) {
            player.worldObj.playSoundEffect(x, y, z, "portal.travel", 1.0F, 1.0F);
        }
        if (!WaystoneConfig.particles) {
            return;
        }
        Random random = player.worldObj.rand;
        for (int i = 0; i < 128; i++) {
            double px = x + (random.nextDouble() - 0.5D) * 3.0D;
            double py = y + random.nextDouble() * 3.0D;
            double pz = z + (random.nextDouble() - 0.5D) * 3.0D;
            player.worldObj.spawnParticle(EnumParticle.portal_underworld,
                    px, py, pz, (random.nextDouble() - 0.5D) * 2.0D,
                    -random.nextDouble(), (random.nextDouble() - 0.5D) * 2.0D);
        }
    }

    @Override
    public ResourceLocation getChannel() {
        return CHANNEL;
    }
}
