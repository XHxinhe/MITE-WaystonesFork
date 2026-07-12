package net.blay09.mods.waystones.client;

import net.blay09.mods.waystones.WaystoneConfig;
import net.minecraft.GuiButton;
import net.minecraft.GuiScreen;
import net.minecraft.I18n;

public final class GuiWaystoneConfig extends GuiScreen {
    private final GuiScreen parent;

    public GuiWaystoneConfig(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        addToggle(0, -66, "gui.waystones.config.nametag", WaystoneConfig.showNametag);
        addToggle(1, -42, "gui.waystones.config.particles", WaystoneConfig.particles);
        addToggle(2, -18, "gui.waystones.config.sounds", WaystoneConfig.sounds);
        addToggle(3, 6, "gui.waystones.config.teleport_sound", !WaystoneConfig.disableTeleportSound);
        addToggle(4, 30, "gui.waystones.config.cooldown_glow", WaystoneConfig.showCooldownOnWaystone);
        addToggle(5, 54, "gui.waystones.config.pause", WaystoneConfig.menusPauseGame);
        addToggle(6, 78, "gui.waystones.config.flat_icon", WaystoneConfig.flatInventoryIcon);
        buttonList.add(new GuiButton(10, width / 2 - 100, height / 2 + 108, 200, 20,
                I18n.getString("gui.done")));
    }

    private void addToggle(int id, int yOffset, String key, boolean value) {
        buttonList.add(new GuiButton(id, width / 2 - 100, height / 2 + yOffset, 200, 20,
                label(key, value)));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0 -> WaystoneConfig.showNametag = !WaystoneConfig.showNametag;
            case 1 -> WaystoneConfig.particles = !WaystoneConfig.particles;
            case 2 -> WaystoneConfig.sounds = !WaystoneConfig.sounds;
            case 3 -> WaystoneConfig.disableTeleportSound = !WaystoneConfig.disableTeleportSound;
            case 4 -> WaystoneConfig.showCooldownOnWaystone = !WaystoneConfig.showCooldownOnWaystone;
            case 5 -> WaystoneConfig.menusPauseGame = !WaystoneConfig.menusPauseGame;
            case 6 -> WaystoneConfig.flatInventoryIcon = !WaystoneConfig.flatInventoryIcon;
            case 10 -> {
                WaystoneConfig.saveCurrent();
                mc.displayGuiScreen(parent);
                return;
            }
            default -> {
                return;
            }
        }
        WaystoneConfig.saveCurrent();
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.getString("gui.waystones.config"),
                width / 2, height / 2 - 112, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return WaystoneConfig.menusPauseGame;
    }

    private static String label(String key, boolean value) {
        return I18n.getString(key) + ": " + I18n.getString(value ? "options.on" : "options.off");
    }
}
