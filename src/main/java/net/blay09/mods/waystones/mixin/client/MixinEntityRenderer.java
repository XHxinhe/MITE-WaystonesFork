package net.blay09.mods.waystones.mixin.client;

import net.blay09.mods.waystones.WaystoneContent;
import net.minecraft.EntityPlayer;
import net.minecraft.EntityRenderer;
import net.minecraft.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
    @Inject(method = "getFOVModifier", at = @At("RETURN"), cancellable = true)
    private void waystones$applyReturnScrollFov(float partialTicks, boolean useFovSetting,
                                                CallbackInfoReturnable<Float> cir) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null && player.getItemInUse() != null
                && player.getItemInUse().getItem() == WaystoneContent.RETURN_SCROLL) {
            float multiplier = player.getItemInUseDuration() / 64.0F * 2.0F + 0.5F;
            cir.setReturnValue(cir.getReturnValueF() * multiplier);
        }
    }
}
