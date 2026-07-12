package net.blay09.mods.waystones.mixin.client;

import moddedmite.rustedironcore.network.Network;
import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.ClientWaystoneState;
import net.blay09.mods.waystones.WaystoneManager;
import net.blay09.mods.waystones.client.GuiWaystoneButton;
import net.blay09.mods.waystones.network.C2SRequestFreeWarp;
import net.minecraft.GuiButton;
import net.minecraft.GuiInventory;
import net.minecraft.GuiScreen;
import net.minecraft.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiInventory.class)
public abstract class MixinGuiInventory extends GuiScreen {
    private static final int WAYSTONE_BUTTON_ID = 0x5753;

    private GuiWaystoneButton waystones$warpButton;

    @Inject(method = "initGui", at = @At("TAIL"))
    private void waystones$addWarpButton(CallbackInfo ci) {
        if (WaystoneConfig.teleportButton) {
            GuiContainerAccessor container = (GuiContainerAccessor) (Object) this;
            waystones$warpButton = new GuiWaystoneButton(WAYSTONE_BUTTON_ID,
                    container.waystones$getGuiLeft() + WaystoneConfig.teleportButtonX,
                    container.waystones$getGuiTop() + WaystoneConfig.teleportButtonY);
            buttonList.add(waystones$warpButton);
        }
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"), cancellable = true)
    private void waystones$openWarpMenu(GuiButton button, CallbackInfo ci) {
        if (button.id == WAYSTONE_BUTTON_ID) {
            Network.sendToServer(new C2SRequestFreeWarp());
            ci.cancel();
        }
    }

    @Inject(method = "drawScreen", at = @At("TAIL"))
    private void waystones$drawWarpTooltip(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (waystones$warpButton == null || !waystones$warpButton.isHovered()) {
            return;
        }
        java.util.List<String> tooltip = new java.util.ArrayList<>();
        long remaining = WaystoneManager.freeWarpCooldownMs()
                - (System.currentTimeMillis() - ClientWaystoneState.getLastFreeWarp());
        if (WaystoneConfig.teleportButtonReturnOnly) {
            tooltip.add(net.minecraft.EnumChatFormatting.YELLOW
                    + I18n.getString("tooltip.waystones.return"));
            net.blay09.mods.waystones.WaystoneEntry last = ClientWaystoneState.getLast();
            String name = last == null ? I18n.getString("tooltip.waystones.none")
                    : net.minecraft.EnumChatFormatting.DARK_AQUA + last.name();
            tooltip.add(net.minecraft.EnumChatFormatting.GRAY
                    + I18n.getStringParams("tooltip.waystones.bound_to", name));
        } else {
            tooltip.add(net.minecraft.EnumChatFormatting.YELLOW
                    + I18n.getString("tooltip.waystones.open_menu"));
        }
        if (remaining > 0) {
            tooltip.add(net.minecraft.EnumChatFormatting.GRAY + I18n.getStringParams(
                    "tooltip.waystones.cooldown", (remaining + 999L) / 1000L));
        }
        ((GuiContainerAccessor) (Object) this).waystones$drawTooltip(tooltip, mouseX, mouseY);
    }
}
