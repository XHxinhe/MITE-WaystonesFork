package net.blay09.mods.waystones.mixin.client;

import net.blay09.mods.waystones.ClientWaystoneState;
import net.minecraft.Minecraft;
import net.minecraft.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraftWorld {
    @Shadow
    public WorldClient theWorld;

    @Inject(method = "loadWorld(Lnet/minecraft/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    private void waystones$clearStateForWorld(WorldClient world, String message, CallbackInfo callback) {
        if (world != theWorld) {
            ClientWaystoneState.clear();
        }
    }
}
