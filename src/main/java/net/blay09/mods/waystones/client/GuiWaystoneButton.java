package net.blay09.mods.waystones.client;

import net.blay09.mods.waystones.ClientWaystoneState;
import net.blay09.mods.waystones.WaystoneContent;
import net.blay09.mods.waystones.WaystoneManager;
import net.minecraft.GuiButton;
import net.minecraft.Minecraft;
import net.minecraft.TextureMap;
import org.lwjgl.opengl.GL11;

public final class GuiWaystoneButton extends GuiButton {
    private boolean hovered;

    public GuiWaystoneButton(int id, int x, int y) {
        super(id, x, y, 16, 16, "");
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        if (!drawButton) {
            return;
        }
        hovered = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;
        boolean ready = ClientWaystoneState.getLast() != null
                && System.currentTimeMillis() - ClientWaystoneState.getLastFreeWarp()
                >= WaystoneManager.freeWarpCooldownMs();
        minecraft.getTextureManager().bindTexture(TextureMap.locationItemsTexture);
        if (!ready) {
            GL11.glColor4f(0.5F, 0.5F, 0.5F, 0.5F);
        } else if (hovered) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            GL11.glColor4f(0.8F, 0.8F, 0.8F, 0.8F);
        }
        drawTexturedModelRectFromIcon(xPosition, yPosition,
                WaystoneContent.RETURN_SCROLL.getIconFromSubtype(0), width, height);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public boolean isHovered() {
        return hovered;
    }
}
