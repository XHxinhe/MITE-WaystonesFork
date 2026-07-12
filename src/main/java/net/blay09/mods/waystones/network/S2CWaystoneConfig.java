package net.blay09.mods.waystones.network;

import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.blay09.mods.waystones.WaystoneConfig;
import net.minecraft.EntityPlayer;
import net.minecraft.ResourceLocation;

public final class S2CWaystoneConfig implements Packet {
    public static final ResourceLocation CHANNEL = new ResourceLocation("waystones", "config");

    private final boolean teleportButton;
    private final boolean returnOnly;
    private final int cooldown;
    private final int buttonX;
    private final int buttonY;
    private final boolean interDimension;
    private final boolean globalInterDimension;
    private final int warpStoneCooldown;
    private final boolean creativeModeOnly;
    private final boolean setSpawnPoint;
    private final boolean globalNoCooldown;
    private final int xpBaseCost;
    private final int xpBlocksPerLevel;
    private final int xpCrossDimCost;

    public S2CWaystoneConfig(PacketByteBuf buf) {
        this(buf.readBoolean(), buf.readBoolean(), buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readBoolean(), buf.readBoolean(), buf.readInt(), buf.readBoolean(), buf.readBoolean(),
                buf.readBoolean(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public S2CWaystoneConfig() {
        this(WaystoneConfig.teleportButton, WaystoneConfig.teleportButtonReturnOnly,
                WaystoneConfig.teleportButtonCooldownSeconds,
                WaystoneConfig.teleportButtonX, WaystoneConfig.teleportButtonY,
                WaystoneConfig.interDimension, WaystoneConfig.globalInterDimension,
                WaystoneConfig.warpStoneCooldownSeconds,
                WaystoneConfig.creativeModeOnly, WaystoneConfig.setSpawnPoint,
                WaystoneConfig.globalNoCooldown, WaystoneConfig.xpBaseCost,
                WaystoneConfig.xpBlocksPerLevel, WaystoneConfig.xpCrossDimCost);
    }

    private S2CWaystoneConfig(boolean teleportButton, boolean returnOnly,
                              int cooldown, int buttonX, int buttonY,
                              boolean interDimension, boolean globalInterDimension,
                              int warpStoneCooldown, boolean creativeModeOnly, boolean setSpawnPoint,
                              boolean globalNoCooldown, int xpBaseCost, int xpBlocksPerLevel, int xpCrossDimCost) {
        this.teleportButton = teleportButton;
        this.returnOnly = returnOnly;
        this.cooldown = cooldown;
        this.buttonX = buttonX;
        this.buttonY = buttonY;
        this.interDimension = interDimension;
        this.globalInterDimension = globalInterDimension;
        this.warpStoneCooldown = warpStoneCooldown;
        this.creativeModeOnly = creativeModeOnly;
        this.setSpawnPoint = setSpawnPoint;
        this.globalNoCooldown = globalNoCooldown;
        this.xpBaseCost = xpBaseCost;
        this.xpBlocksPerLevel = xpBlocksPerLevel;
        this.xpCrossDimCost = xpCrossDimCost;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(teleportButton);
        buf.writeBoolean(returnOnly);
        buf.writeInt(cooldown);
        buf.writeInt(buttonX);
        buf.writeInt(buttonY);
        buf.writeBoolean(interDimension);
        buf.writeBoolean(globalInterDimension);
        buf.writeInt(warpStoneCooldown);
        buf.writeBoolean(creativeModeOnly);
        buf.writeBoolean(setSpawnPoint);
        buf.writeBoolean(globalNoCooldown);
        buf.writeInt(xpBaseCost);
        buf.writeInt(xpBlocksPerLevel);
        buf.writeInt(xpCrossDimCost);
    }

    @Override
    public void apply(EntityPlayer player) {
        WaystoneConfig.teleportButton = teleportButton;
        WaystoneConfig.teleportButtonReturnOnly = returnOnly;
        WaystoneConfig.teleportButtonCooldownSeconds = cooldown;
        WaystoneConfig.teleportButtonX = buttonX;
        WaystoneConfig.teleportButtonY = buttonY;
        WaystoneConfig.interDimension = interDimension;
        WaystoneConfig.globalInterDimension = globalInterDimension;
        WaystoneConfig.warpStoneCooldownSeconds = warpStoneCooldown;
        WaystoneConfig.creativeModeOnly = creativeModeOnly;
        WaystoneConfig.setSpawnPoint = setSpawnPoint;
        WaystoneConfig.globalNoCooldown = globalNoCooldown;
        WaystoneConfig.xpBaseCost = xpBaseCost;
        WaystoneConfig.xpBlocksPerLevel = xpBlocksPerLevel;
        WaystoneConfig.xpCrossDimCost = xpCrossDimCost;
    }

    @Override
    public ResourceLocation getChannel() {
        return CHANNEL;
    }
}
