package net.blay09.mods.waystones.mixin;

import net.blay09.mods.waystones.worldgen.WaystoneWorldgen;
import net.minecraft.World;
import net.minecraft.WorldGenSpikes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(WorldGenSpikes.class)
public abstract class MixinWorldGenSpikes {
    @Inject(method = "generate", at = @At("RETURN"))
    private void waystones$generateEndWaystone(World world, Random random, int x, int y, int z,
                                               CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValue()) {
            WaystoneWorldgen.onEndSpikeGenerated(world, random, x, y, z);
        }
    }
}
