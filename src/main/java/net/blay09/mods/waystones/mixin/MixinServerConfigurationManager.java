package net.blay09.mods.waystones.mixin;

import net.blay09.mods.waystones.WaystoneTeleportContext;
import net.minecraft.Entity;
import net.minecraft.ServerConfigurationManager;
import net.minecraft.Teleporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerConfigurationManager.class)
public abstract class MixinServerConfigurationManager {
    @Redirect(method = "transferEntityToWorld",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/Teleporter;placeInPortal(Lnet/minecraft/Entity;IDDDF)V"))
    private void waystones$skipPortalPlacement(Teleporter teleporter, Entity entity, int oldDimension,
                                               double oldX, double oldY, double oldZ, float oldYaw) {
        if (!WaystoneTeleportContext.bypassesPortal(entity)) {
            teleporter.placeInPortal(entity, oldDimension, oldX, oldY, oldZ, oldYaw);
        }
    }
}
