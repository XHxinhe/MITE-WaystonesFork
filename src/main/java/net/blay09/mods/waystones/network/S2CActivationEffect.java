package net.blay09.mods.waystones.network;

import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.blay09.mods.waystones.WaystoneConfig;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumParticle;
import net.minecraft.ResourceLocation;

public final class S2CActivationEffect implements Packet {
    public static final ResourceLocation CHANNEL = new ResourceLocation("waystones", "activation_fx");

    private final int dimension;
    private final int x;
    private final int y;
    private final int z;

    public S2CActivationEffect(PacketByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public S2CActivationEffect(int dimension, int x, int y, int z) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(dimension);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    @Override
    public void apply(EntityPlayer player) {
        if (player == null || player.worldObj == null || player.dimension != dimension
                || player.getDistanceSqToBlock(x, y, z) > 4096.0D) {
            return;
        }
        if (WaystoneConfig.sounds) {
            player.playSound("random.levelup", 1.0F, 1.0F);
        }
        if (WaystoneConfig.particles) {
            for (int i = 0; i < 32; i++) {
                player.worldObj.spawnParticle(EnumParticle.enchantmenttable,
                        x + 0.5D + (player.worldObj.rand.nextDouble() - 0.5D) * 2.0D,
                        y + 3.0D, z + 0.5D + (player.worldObj.rand.nextDouble() - 0.5D) * 2.0D,
                        0.0D, -5.0D, 0.0D);
                player.worldObj.spawnParticle(EnumParticle.enchantmenttable,
                        x + 0.5D + (player.worldObj.rand.nextDouble() - 0.5D) * 2.0D,
                        y + 4.0D, z + 0.5D + (player.worldObj.rand.nextDouble() - 0.5D) * 2.0D,
                        0.0D, -5.0D, 0.0D);
            }
        }
    }

    @Override
    public ResourceLocation getChannel() {
        return CHANNEL;
    }
}
