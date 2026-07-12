package net.blay09.mods.waystones.client;

import moddedmite.rustedironcore.network.Network;
import net.blay09.mods.waystones.network.C2SRenameWaystone;
import net.blay09.mods.waystones.ClientWaystoneState;
import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.WaystoneEntry;
import net.minecraft.GuiButton;
import net.minecraft.GuiScreen;
import net.minecraft.GuiTextField;
import net.minecraft.I18n;
import org.lwjgl.input.Keyboard;

public final class GuiWaystoneName extends GuiScreen {
    private final int x;
    private final int y;
    private final int z;
    private final String initialName;
    private final GuiScreen parent;
    private final boolean renaming;
    private boolean global;
    private GuiTextField nameField;
    private GuiButton done;
    private GuiButton cancel;
    private GuiButton globalButton;

    public GuiWaystoneName(int x, int y, int z, String initialName, boolean global) {
        this(x, y, z, initialName, global, null, false);
    }

    public GuiWaystoneName(int x, int y, int z, String initialName, boolean global,
                           GuiScreen parent, boolean renaming) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.initialName = initialName;
        this.global = global;
        this.parent = parent;
        this.renaming = renaming;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        nameField = new GuiTextField(fontRenderer, width / 2 - 100, height / 2 - 15, 200, 20);
        nameField.setMaxStringLength(32);
        nameField.setText(initialName);
        nameField.setFocused(true);
        globalButton = new GuiButton(1, width / 2 - 100, height / 2 + 18, 98, 20,
                globalLabel());
        globalButton.drawButton = mc.thePlayer.inCreativeMode() && !renaming;
        done = new GuiButton(0, width / 2 + 2, height / 2 + 18, 98, 20,
                I18n.getString("gui.done"));
        cancel = new GuiButton(2, width / 2 - 100, height / 2 + 42, 200, 20,
                I18n.getString("gui.cancel"));
        buttonList.add(globalButton);
        buttonList.add(done);
        buttonList.add(cancel);
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == done && done.enabled) {
            Network.sendToServer(new C2SRenameWaystone(x, y, z, nameField.getText(), global, renaming));
            if (!renaming) {
                mc.displayGuiScreen(parent);
            }
        } else if (button == cancel) {
            mc.displayGuiScreen(parent);
        } else if (button == globalButton) {
            global = !global;
            globalButton.displayString = globalLabel();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_RETURN) {
            if (done.enabled) {
                actionPerformed(done);
            }
            return;
        }
        if (!nameField.textboxKeyTyped(typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        nameField.updateCursorCounter();
        String name = nameField.getText().trim();
        done.enabled = !name.isEmpty() && !isDuplicate(name);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawString(fontRenderer, I18n.getString("gui.waystones.name"),
                width / 2 - 100, height / 2 - 32, 0xFFFFFF);
        nameField.drawTextBox();
        if (isDuplicate(nameField.getText().trim())) {
            drawCenteredString(fontRenderer, I18n.getString("gui.waystones.name_taken"),
                    width / 2, height / 2 + 66, 0xFF5555);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return WaystoneConfig.menusPauseGame;
    }

    private boolean isDuplicate(String name) {
        if (name.isEmpty()) {
            return false;
        }
        for (WaystoneEntry entry : ClientWaystoneState.getEntries()) {
            if (entry.name().equalsIgnoreCase(name)
                    && !(entry.dimension() == mc.thePlayer.dimension
                    && entry.x() == x && entry.y() == y && entry.z() == z)) {
                return true;
            }
        }
        return false;
    }

    private String globalLabel() {
        return I18n.getString(global ? "gui.waystones.global.yes" : "gui.waystones.global.no");
    }
}
