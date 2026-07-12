package net.blay09.mods.waystones.mixin.client;

import net.minecraft.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(GuiContainer.class)
public interface GuiContainerAccessor {
    @Accessor("guiLeft")
    int waystones$getGuiLeft();

    @Accessor("guiTop")
    int waystones$getGuiTop();

    @Invoker("func_102021_a")
    void waystones$drawTooltip(List tooltip, int mouseX, int mouseY);
}
