package net.blay09.mods.waystones.mixin;

import net.blay09.mods.waystones.api.IWaystonePlayer;
import net.minecraft.EntityPlayer;
import net.minecraft.NBTTagCompound;
import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.WaystoneContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer implements IWaystonePlayer {
    @Unique
    private NBTTagCompound waystones$data = new NBTTagCompound();

    @Override
    public NBTTagCompound waystones$getData() {
        return waystones$data;
    }

    @Inject(method = "writeEntityToNBT", at = @At("TAIL"))
    private void waystones$writeData(NBTTagCompound tag, CallbackInfo ci) {
        tag.setTag("WaystonesMITE", waystones$data);
    }

    @Inject(method = "readEntityFromNBT", at = @At("TAIL"))
    private void waystones$readData(NBTTagCompound tag, CallbackInfo ci) {
        waystones$data = tag.getCompoundTag("WaystonesMITE");
    }

    @Inject(method = "getCurrentPlayerStrVsBlock", at = @At("HEAD"), cancellable = true)
    private void waystones$preventWaystoneBreaking(int x, int y, int z, boolean applyHeldItem,
                                                   CallbackInfoReturnable<Float> cir) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        if (waystones$cannotBreak(player, x, y, z)) {
            cir.setReturnValue(0.0F);
        }
    }

    @Inject(method = "getRelativeBlockHardness", at = @At("HEAD"), cancellable = true)
    private void waystones$preventRelativeWaystoneBreaking(int x, int y, int z, boolean applyHeldItem,
                                                           CallbackInfoReturnable<Float> cir) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        if (waystones$cannotBreak(player, x, y, z)) {
            cir.setReturnValue(0.0F);
        }
    }

    @Unique
    private static boolean waystones$cannotBreak(EntityPlayer player, int x, int y, int z) {
        if (!(player.worldObj.getBlock(x, y, z) instanceof net.blay09.mods.waystones.block.BlockWaystone)) {
            return false;
        }
        if (WaystoneConfig.creativeModeOnly && !player.inCreativeMode()) {
            return true;
        }
        if (!WaystoneConfig.invulnerableWaystones || player.inCreativeMode()) {
            return false;
        }
        net.minecraft.TileEntity tile = player.worldObj.getBlockTileEntity(x, y, z);
        if (tile instanceof net.blay09.mods.waystones.block.TileWaystone upper && upper.isUpperPart()) {
            tile = player.worldObj.getBlockTileEntity(x, y - 1, z);
        }
        return tile instanceof net.blay09.mods.waystones.block.TileWaystone waystone
                && !waystone.getWaystoneOwner().isEmpty()
                && !waystone.getWaystoneOwner().equals(player.getEntityName());
    }
}
