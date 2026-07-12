package net.blay09.mods.waystones.mixin.client;

import net.blay09.mods.waystones.block.BlockWaystone;
import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.client.render.RenderWaystone;
import net.minecraft.Block;
import net.minecraft.RenderBlocks;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderBlocks.class)
public abstract class MixinRenderBlocks {
    @Unique
    private final RenderWaystone waystones$itemRenderer = new RenderWaystone();

    @Inject(method = "renderItemIn3d", at = @At("HEAD"), cancellable = true)
    private static void waystones$renderIn3d(int renderType, CallbackInfoReturnable<Boolean> cir) {
        if (renderType == BlockWaystone.RENDER_TYPE) {
            cir.setReturnValue(!WaystoneConfig.flatInventoryIcon);
        }
    }

    @Inject(method = "renderBlockAsItem",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/Block;getRenderType()I"))
    private void waystones$renderInventoryModel(Block block, int metadata, float brightness, CallbackInfo ci) {
        if (block.getRenderType() != BlockWaystone.RENDER_TYPE || WaystoneConfig.flatInventoryIcon) {
            return;
        }
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, -0.35F, 0.0F);
        GL11.glScalef(0.7F, 0.7F, 0.7F);
        int variant = block instanceof BlockWaystone waystone ? waystone.getVariant() : 0;
        waystones$itemRenderer.renderItem(0.0D, 0.0D, 0.0D, variant);
        GL11.glPopMatrix();
    }
}
