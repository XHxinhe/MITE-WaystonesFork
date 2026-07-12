package net.blay09.mods.waystones.client;

import moddedmite.rustedironcore.network.Network;
import net.blay09.mods.waystones.WaystoneEntry;
import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.network.C2SConfirmReturn;
import net.minecraft.EnumChatFormatting;
import net.minecraft.GuiButton;
import net.minecraft.GuiScreen;
import net.minecraft.I18n;

public final class GuiReturnConfirm extends GuiScreen {
    private final WaystoneEntry target;

    public GuiReturnConfirm(WaystoneEntry target) {
        this.target = target;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 102, height / 2 + 20, 98, 20,
                I18n.getString("gui.yes")));
        buttonList.add(new GuiButton(1, width / 2 + 4, height / 2 + 20, 98, 20,
                I18n.getString("gui.no")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            Network.sendToServer(new C2SConfirmReturn());
        }
        mc.displayGuiScreen(null);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.getString("gui.waystones.confirm_return"),
                width / 2, height / 2 - 34, 0xFFFFFF);
        drawCenteredString(fontRenderer, EnumChatFormatting.GRAY
                        + I18n.getStringParams("gui.waystones.confirm_return.bound", target.name()),
                width / 2, height / 2 - 12, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return WaystoneConfig.menusPauseGame;
    }
}
