package net.blay09.mods.waystones.mixin;

import net.blay09.mods.waystones.WaystoneEvents;
import net.minecraft.CraftingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = CraftingManager.class, priority = 2000)
public abstract class MixinCraftingManager {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/RecipesMITE;addCraftingRecipes(Lnet/minecraft/CraftingManager;)V"), index = 0)
    private CraftingManager waystones$registerEvents(CraftingManager manager) {
        WaystoneEvents.register();
        return manager;
    }
}
