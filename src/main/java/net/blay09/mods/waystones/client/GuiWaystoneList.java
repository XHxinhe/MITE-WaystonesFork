package net.blay09.mods.waystones.client;

import moddedmite.rustedironcore.network.Network;
import net.blay09.mods.waystones.ClientWaystoneState;
import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.WaystoneEntry;
import net.blay09.mods.waystones.WaystoneManager;
import net.blay09.mods.waystones.WaystoneXpCost;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.network.C2SForgetWaystone;
import net.blay09.mods.waystones.network.C2SSetPinned;
import net.blay09.mods.waystones.network.C2STeleport;
import net.minecraft.EnumChatFormatting;
import net.minecraft.GuiButton;
import net.minecraft.GuiScreen;
import net.minecraft.GuiTextField;
import net.minecraft.GuiYesNo;
import net.minecraft.I18n;
import net.minecraft.Minecraft;
import net.minecraft.ResourceLocation;
import net.minecraft.ScaledResolution;
import net.minecraft.Tessellator;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class GuiWaystoneList extends GuiScreen {
    private static final ResourceLocation MENU = new ResourceLocation(Waystones.MOD_ID, "textures/gui/menu.png");
    private static final ResourceLocation XP_ORB = new ResourceLocation(Waystones.MOD_ID, "textures/gui/xporb.png");
    private static final int LIST_WIDTH = 179;
    private static final int LIST_HEIGHT = 105;
    private static final int ROW_HEIGHT = 20;
    private static final int ROW_GAP = 2;
    private static final int ICON_SIZE = 20;

    private final List<WaystoneEntry> allEntries;
    private final boolean warpStone;
    private final boolean freeWarp;
    private final WaystoneEntry origin;
    private final List<String> pinnedNames = new ArrayList<>();
    private final List<EntryRow> rows = new ArrayList<>();
    private GuiTextField searchField;
    private int listX;
    private int listY;
    private int sortX;
    private int configX;
    private int helpX;
    private int titleRenameX;
    private int titleForgetX;
    private int titleIconY;
    private float scrollAmount;
    private float maxScroll;
    private boolean draggingScrollbar;
    private int dragStartY;
    private float dragStartScroll;
    private String lastSearch = "";
    private int lastMouseX;
    private int lastMouseY;
    private WaystoneEntry pendingForget;
    private boolean closeAfterForget;

    public GuiWaystoneList(List<WaystoneEntry> entries, boolean warpStone) {
        this(entries, warpStone, false, null);
    }

    public GuiWaystoneList(List<WaystoneEntry> entries, boolean warpStone, boolean freeWarp) {
        this(entries, warpStone, freeWarp, null);
    }

    public GuiWaystoneList(List<WaystoneEntry> entries, boolean warpStone, boolean freeWarp,
                           WaystoneEntry origin) {
        this.allEntries = new ArrayList<>(entries);
        this.warpStone = warpStone;
        this.freeWarp = freeWarp;
        this.origin = origin;
        pinnedNames.addAll(ClientWaystoneState.getPinnedNames());
    }

    @Override
    public void initGui() {
        buttonList.clear();
        searchField = new GuiTextField(fontRenderer, width / 2 - 100, height / 2 - 55, 200, 20);
        searchField.setMaxStringLength(1000);
        searchField.setText(lastSearch);
        listX = width / 2 - 92;
        listY = height / 2 - 27;
        sortX = listX - 25;
        configX = listX - 25;
        helpX = listX - 25;
        Keyboard.enableRepeatEvents(true);
        rebuildRows();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private void rebuildRows() {
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        List<WaystoneEntry> pinned = new ArrayList<>();
        List<WaystoneEntry> regular = new ArrayList<>();
        for (WaystoneEntry entry : allEntries) {
            if (origin != null && entry.samePosition(origin)) {
                continue;
            }
            if (!query.isEmpty() && !entry.name().toLowerCase(Locale.ROOT).startsWith(query)) {
                continue;
            }
            (pinnedNames.contains(entry.name()) ? pinned : regular).add(entry);
        }
        pinned.sort(Comparator.comparingInt(entry -> pinnedNames.indexOf(entry.name())));
        Comparator<WaystoneEntry> comparator = WaystoneConfig.sortingMode == 1
                ? Comparator.comparingDouble(this::distanceSquared)
                : Comparator.comparing(WaystoneEntry::name, String.CASE_INSENSITIVE_ORDER);
        regular.sort(comparator);
        rows.clear();
        for (WaystoneEntry entry : pinned) {
            rows.add(new EntryRow(entry));
        }
        for (WaystoneEntry entry : regular) {
            rows.add(new EntryRow(entry));
        }
        maxScroll = Math.max(0F, rows.size() * (ROW_HEIGHT + ROW_GAP) - ROW_GAP - LIST_HEIGHT);
        clampScroll();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        drawWorldBackground(0);
        drawTitle(mouseX, mouseY);
        drawCenteredString(fontRenderer, I18n.getString("gui.waystones.select"),
                width / 2, height / 2 - 85, 0xAAAAAA);
        searchField.drawTextBox();
        if (searchField.getText().isEmpty() && !searchField.isFocused()) {
            drawString(fontRenderer, I18n.getString("gui.waystones.search"),
                    width / 2 - 94, height / 2 - 49, 0x777777);
        }
        EntryRow hovered = drawList(mouseX, mouseY);
        drawToolbar(mouseX, mouseY);
        drawTooltips(mouseX, mouseY, hovered);
    }

    private void drawTitle(int mouseX, int mouseY) {
        String title;
        if (origin != null) {
            title = (origin.global() ? EnumChatFormatting.YELLOW.toString() : "")
                    + EnumChatFormatting.UNDERLINE + origin.name();
        } else if (warpStone) {
            title = I18n.getString("item.warp_stone.name");
        } else {
            title = I18n.getString("gui.waystones.select");
        }
        GL11.glPushMatrix();
        GL11.glTranslatef(width / 2F, height / 2F - 110F, 0F);
        GL11.glScalef(1.5F, 1.5F, 1.5F);
        drawCenteredString(fontRenderer, title, 0, 0, 0xFFFFFF);
        GL11.glPopMatrix();

        if (origin == null) {
            return;
        }
        int titleWidth = (int) (fontRenderer.getStringWidth(origin.name()) * 1.5F);
        int titleLeft = width / 2 - titleWidth / 2;
        titleIconY = height / 2 - 113;
        titleRenameX = titleLeft - 20;
        titleForgetX = titleRenameX - 18;
        boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        if (!shift) {
            drawMenuIcon(titleLeft - 18, titleIconY + 2, 19, 75, 20, 17, 15, 12, 1F);
            return;
        }
        boolean renameHover = over(mouseX, mouseY, titleRenameX, titleIconY, 16, 16);
        drawMenuIcon(titleRenameX, titleIconY, renameHover ? 52 : 36, 40, 16, 16, 16, 16, 1F);
        if (!origin.global()) {
            boolean forgetHover = over(mouseX, mouseY, titleForgetX, titleIconY, 16, 16);
            drawMenuIcon(titleForgetX, titleIconY, forgetHover ? 116 : 100, 168,
                    16, 16, 16, 16, 1F);
        }
    }

    private EntryRow drawList(int mouseX, int mouseY) {
        drawRect(listX - 2, listY - 2, listX + LIST_WIDTH + 7, listY + LIST_HEIGHT + 2, 0x7F000000);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        applyScissor();
        EntryRow hovered = null;
        for (int i = 0; i < rows.size(); i++) {
            int rowY = (int) (listY - scrollAmount + i * (ROW_HEIGHT + ROW_GAP));
            if (rowY + ROW_HEIGHT < listY || rowY > listY + LIST_HEIGHT) {
                continue;
            }
            EntryRow row = rows.get(i);
            row.xPosition = listX;
            row.yPosition = rowY;
            boolean inside = over(mouseX, mouseY, listX, listY, LIST_WIDTH, LIST_HEIGHT);
            row.drawButton(mc, inside ? mouseX : -1, inside ? mouseY : -1);
            if (inside && over(mouseX, mouseY, listX, rowY, LIST_WIDTH, ROW_HEIGHT)) {
                hovered = row;
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        drawHorizontalLine(listX - 2, listX + LIST_WIDTH + 6, listY - 2, 0xFFAAAAAA);
        drawHorizontalLine(listX - 2, listX + LIST_WIDTH + 6, listY + LIST_HEIGHT + 1, 0xFFAAAAAA);
        drawVerticalLine(listX - 2, listY - 2, listY + LIST_HEIGHT + 1, 0xFFAAAAAA);
        drawVerticalLine(listX + LIST_WIDTH + 6, listY - 2, listY + LIST_HEIGHT + 1, 0xFFAAAAAA);
        drawScrollbar();
        if (hovered != null) {
            drawBottomInfo(hovered.entry);
        }
        return hovered;
    }

    private void drawScrollbar() {
        int trackX = listX + LIST_WIDTH + 2;
        drawRect(trackX, listY - 1, trackX + 5, listY + LIST_HEIGHT + 1, 0x7F000000);
        int contentHeight = Math.max(1, rows.size() * (ROW_HEIGHT + ROW_GAP) - ROW_GAP);
        int thumbHeight = Math.min(LIST_HEIGHT, Math.max(10, LIST_HEIGHT * LIST_HEIGHT / contentHeight));
        int thumbY = listY + (maxScroll <= 0F ? 0
                : Math.round((LIST_HEIGHT - thumbHeight) * scrollAmount / maxScroll));
        drawRect(trackX, thumbY, trackX + 5, thumbY + thumbHeight, 0xFF808080);
        drawRect(trackX, thumbY, trackX + 4, thumbY + thumbHeight - 1, 0xFFC0C0C0);
    }

    private void drawBottomInfo(WaystoneEntry entry) {
        String text;
        if (entry.dimension() == mc.thePlayer.dimension) {
            int distance = (int) Math.sqrt(distanceSquared(entry));
            text = format("gui.waystones.distance_info", distance, entry.x(), entry.y(), entry.z());
        } else {
            text = dimensionName(entry.dimension());
        }
        drawCenteredString(fontRenderer, text, listX + LIST_WIDTH / 2,
                listY + LIST_HEIGHT + 15, 0xAAAAAA);
    }

    private void drawToolbar(int mouseX, int mouseY) {
        int sortY = listY;
        int configY = listY + 23;
        int helpY = listY + 45;
        boolean sortHover = over(mouseX, mouseY, sortX, sortY, ICON_SIZE, ICON_SIZE);
        int sortU = WaystoneConfig.sortingMode == 0 ? (sortHover ? 80 : 20) : (sortHover ? 100 : 40);
        drawMenuIcon(sortX, sortY, sortU, 92, 20, 20, 20, 20, 1F);
        boolean configHover = over(mouseX, mouseY, configX, configY, ICON_SIZE, ICON_SIZE);
        drawMenuIcon(configX, configY, configHover ? 140 : 60, 0, 20, 20, 20, 20, 1F);
        boolean helpHover = over(mouseX, mouseY, helpX, helpY, ICON_SIZE, ICON_SIZE);
        drawMenuIcon(helpX, helpY, helpHover ? 20 : 0, 132, 20, 20, 20, 20, 1F);
    }

    private void drawTooltips(int mouseX, int mouseY, EntryRow hovered) {
        String tooltip = null;
        if (over(mouseX, mouseY, sortX, listY, ICON_SIZE, ICON_SIZE)) {
            tooltip = format("gui.waystones.sorting",
                    I18n.getString(WaystoneConfig.sortingMode == 0
                            ? "gui.waystones.sorting.alphabetical" : "gui.waystones.sorting.distance"));
        } else if (over(mouseX, mouseY, configX, listY + 23, ICON_SIZE, ICON_SIZE)) {
            tooltip = I18n.getString("gui.waystones.config");
        } else if (over(mouseX, mouseY, helpX, listY + 45, ICON_SIZE, ICON_SIZE)) {
            tooltip = I18n.getString("gui.waystones.help");
        }
        boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        if (shift && origin != null && over(mouseX, mouseY, titleRenameX, titleIconY, 16, 16)) {
            tooltip = I18n.getString("gui.waystones.rename");
        } else if (shift && origin != null && !origin.global()
                && over(mouseX, mouseY, titleForgetX, titleIconY, 16, 16)) {
            tooltip = I18n.getString("gui.waystones.forget");
        } else if (shift && hovered != null) {
            if (over(mouseX, mouseY, hovered.xPosition + 2, hovered.yPosition + 2, 16, 16)
                    && !hovered.entry.global()) {
                tooltip = I18n.getString("gui.waystones.forget");
            } else if (over(mouseX, mouseY, hovered.xPosition + 20, hovered.yPosition + 2, 16, 16)) {
                tooltip = I18n.getString(pinnedNames.contains(hovered.entry.name())
                        ? "gui.waystones.unpin" : "gui.waystones.pin");
            }
        }
        if (tooltip != null) {
            drawTooltip(tooltip, mouseX, mouseY);
        }
    }

    private void drawTooltip(String text, int mouseX, int mouseY) {
        String[] lines = text.split("\\n");
        int tooltipWidth = 0;
        for (String line : lines) {
            tooltipWidth = Math.max(tooltipWidth, fontRenderer.getStringWidth(line));
        }
        int x = Math.min(mouseX + 12, width - tooltipWidth - 8);
        int y = Math.min(mouseY - 12, height - lines.length * 10 - 8);
        drawRect(x - 3, y - 4, x + tooltipWidth + 3, y + lines.length * 10 + 2, 0xF0100010);
        for (int i = 0; i < lines.length; i++) {
            fontRenderer.drawString(lines[i], x, y + i * 10, 0xFFFFFF);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (!searchField.isFocused() && keyCode == mc.gameSettings.keyBindInventory.keyCode) {
            mc.displayGuiScreen(null);
            return;
        }
        if (!searchField.textboxKeyTyped(typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
        String current = searchField.getText();
        if (!current.equals(lastSearch)) {
            lastSearch = current;
            scrollAmount = 0F;
            rebuildRows();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return;
        }
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
        if (over(mouseX, mouseY, sortX, listY, ICON_SIZE, ICON_SIZE)) {
            WaystoneConfig.sortingMode = (WaystoneConfig.sortingMode + 1) % 2;
            WaystoneConfig.saveCurrent();
            rebuildRows();
            return;
        }
        if (over(mouseX, mouseY, configX, listY + 23, ICON_SIZE, ICON_SIZE)) {
            mc.displayGuiScreen(new GuiWaystoneConfig(this));
            return;
        }
        int trackX = listX + LIST_WIDTH + 2;
        if (over(mouseX, mouseY, trackX, listY, 5, LIST_HEIGHT)) {
            draggingScrollbar = true;
            dragStartY = mouseY;
            dragStartScroll = scrollAmount;
            return;
        }
        boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        if (shift && origin != null && over(mouseX, mouseY, titleRenameX, titleIconY, 16, 16)) {
            mc.displayGuiScreen(new GuiWaystoneName(origin.x(), origin.y(), origin.z(),
                    origin.name(), origin.global(), this, true));
            return;
        }
        if (shift && origin != null && !origin.global()
                && over(mouseX, mouseY, titleForgetX, titleIconY, 16, 16)) {
            openForgetDialog(origin, true);
            return;
        }
        EntryRow row = hoveredRow(mouseX, mouseY);
        if (row == null) {
            return;
        }
        if (shift && over(mouseX, mouseY, row.xPosition + 2, row.yPosition + 2, 16, 16)) {
            if (!row.entry.global()) {
                openForgetDialog(row.entry, false);
            }
            return;
        }
        if (shift && over(mouseX, mouseY, row.xPosition + 20, row.yPosition + 2, 16, 16)) {
            boolean pinned = !pinnedNames.contains(row.entry.name());
            pinnedNames.remove(row.entry.name());
            if (pinned) {
                pinnedNames.add(row.entry.name());
            }
            Network.sendToServer(new C2SSetPinned(row.entry, pinned));
            rebuildRows();
            return;
        }
        if (row.isAvailable()) {
            Network.sendToServer(new C2STeleport(row.entry, warpStone, freeWarp));
            mc.displayGuiScreen(null);
        }
    }

    private void openForgetDialog(WaystoneEntry entry, boolean closeAfter) {
        pendingForget = entry;
        closeAfterForget = closeAfter;
        mc.displayGuiScreen(new GuiYesNo(this,
                format("gui.waystones.remove_dialog", entry.name()), "", 0));
    }

    @Override
    public void confirmClicked(boolean confirmed, int id) {
        if (confirmed && pendingForget != null) {
            allEntries.removeIf(entry -> entry.samePosition(pendingForget));
            pinnedNames.remove(pendingForget.name());
            Network.sendToServer(new C2SForgetWaystone(pendingForget));
        }
        WaystoneEntry forgotten = pendingForget;
        pendingForget = null;
        if (confirmed && closeAfterForget && forgotten != null) {
            mc.displayGuiScreen(null);
        } else {
            rebuildRows();
            mc.displayGuiScreen(this);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long elapsed) {
        if (!draggingScrollbar || button != 0 || maxScroll <= 0F) {
            return;
        }
        int contentHeight = Math.max(1, rows.size() * (ROW_HEIGHT + ROW_GAP) - ROW_GAP);
        int thumbHeight = Math.min(LIST_HEIGHT, Math.max(10, LIST_HEIGHT * LIST_HEIGHT / contentHeight));
        int track = Math.max(1, LIST_HEIGHT - thumbHeight);
        scrollAmount = dragStartScroll + (mouseY - dragStartY) * maxScroll / track;
        clampScroll();
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int button) {
        if (button == 0) {
            draggingScrollbar = false;
        }
        super.mouseMovedOrUp(mouseX, mouseY, button);
    }

    @Override
    public void handleMouseInput() {
        int wheel = Mouse.getEventDWheel();
        super.handleMouseInput();
        if (wheel != 0 && over(lastMouseX, lastMouseY, listX, listY, LIST_WIDTH + 7, LIST_HEIGHT)) {
            scrollAmount -= Integer.signum(wheel) * ROW_HEIGHT / 2F;
            clampScroll();
        }
    }

    @Override
    public void updateScreen() {
        searchField.updateCursorCounter();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return WaystoneConfig.menusPauseGame;
    }

    private EntryRow hoveredRow(int mouseX, int mouseY) {
        if (!over(mouseX, mouseY, listX, listY, LIST_WIDTH, LIST_HEIGHT)) {
            return null;
        }
        int index = (int) ((mouseY - listY + scrollAmount) / (ROW_HEIGHT + ROW_GAP));
        if (index < 0 || index >= rows.size()) {
            return null;
        }
        EntryRow row = rows.get(index);
        return over(mouseX, mouseY, listX, row.yPosition, LIST_WIDTH, ROW_HEIGHT) ? row : null;
    }

    private void clampScroll() {
        scrollAmount = Math.max(0F, Math.min(maxScroll, scrollAmount));
    }

    private void applyScissor() {
        ScaledResolution scaled = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        int scale = scaled.getScaleFactor();
        GL11.glScissor(listX * scale,
                (scaled.getScaledHeight() - listY - LIST_HEIGHT) * scale,
                LIST_WIDTH * scale, LIST_HEIGHT * scale);
    }

    private double distanceSquared(WaystoneEntry entry) {
        if (entry.dimension() != mc.thePlayer.dimension) {
            return Double.MAX_VALUE;
        }
        return mc.thePlayer.getDistanceSqToBlock(entry.x(), entry.y(), entry.z());
    }

    private static String dimensionName(int dimension) {
        return switch (dimension) {
            case 0 -> I18n.getString("dimension.waystones.overworld");
            case -1 -> I18n.getString("dimension.waystones.nether");
            case 1 -> I18n.getString("dimension.waystones.end");
            default -> "D" + dimension;
        };
    }

    private static String format(String key, Object... arguments) {
        return String.format(I18n.getString(key), arguments);
    }

    private static boolean over(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static void drawMenuIcon(int x, int y, int u, int v, int sourceWidth, int sourceHeight,
                                     int drawWidth, int drawHeight, float alpha) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(MENU);
        drawTexture(x, y, u, v, sourceWidth, sourceHeight, drawWidth, drawHeight, 256, 256, alpha);
    }

    private static void drawTexture(int x, int y, int u, int v, int sourceWidth, int sourceHeight,
                                    int drawWidth, int drawHeight, int textureWidth, int textureHeight, float alpha) {
        GL11.glColor4f(1F, 1F, 1F, alpha);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        float minU = u / (float) textureWidth;
        float maxU = (u + sourceWidth) / (float) textureWidth;
        float minV = v / (float) textureHeight;
        float maxV = (v + sourceHeight) / (float) textureHeight;
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(x, y + drawHeight, 0, minU, maxV);
        tess.addVertexWithUV(x + drawWidth, y + drawHeight, 0, maxU, maxV);
        tess.addVertexWithUV(x + drawWidth, y, 0, maxU, minV);
        tess.addVertexWithUV(x, y, 0, minU, minV);
        tess.draw();
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    private final class EntryRow extends GuiButton {
        private final WaystoneEntry entry;
        private final int xpCost;

        private EntryRow(WaystoneEntry entry) {
            super(0, listX, listY, LIST_WIDTH, ROW_HEIGHT, entry.name());
            this.entry = entry;
            this.xpCost = WaystoneXpCost.get(mc.thePlayer, origin, entry);
            String name = entry.name();
            int reserved = !freeWarp && xpCost >= 0 ? 45 : 18;
            while (name.length() > 1 && fontRenderer.getStringWidth(name) > LIST_WIDTH - reserved) {
                name = name.substring(0, name.length() - 1);
            }
            if (!name.equals(entry.name())) {
                name += "...";
            }
            displayString = (entry.global() ? EnumChatFormatting.YELLOW.toString() : "") + name;
        }

        @Override
        public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
            enabled = isAvailable();
            super.drawButton(minecraft, mouseX, mouseY);
            if (!freeWarp && xpCost >= 0 && !mc.thePlayer.inCreativeMode()) {
                int color = mc.thePlayer.getExperienceLevel() >= xpCost ? 0x36A336 : 0xFF5555;
                if (!enabled) {
                    color = 0x777777;
                }
                String cost = Integer.toString(xpCost);
                fontRenderer.drawString(cost,
                        xPosition + LIST_WIDTH - 15 - fontRenderer.getStringWidth(cost), yPosition + 6, color);
                Minecraft.getMinecraft().getTextureManager().bindTexture(XP_ORB);
                drawTexture(xPosition + LIST_WIDTH - 13, yPosition + 6,
                        0, 0, 8, 8, 8, 8, 8, 8, enabled ? 1F : 0.5F);
            }
            boolean rowHovered = over(mouseX, mouseY, xPosition, yPosition, LIST_WIDTH, ROW_HEIGHT);
            boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
            if (shift && rowHovered) {
                boolean removeHover = over(mouseX, mouseY, xPosition + 2, yPosition + 2, 16, 16);
                drawMenuIcon(xPosition + 2, yPosition + 2, removeHover ? 116 : 100, 168,
                        16, 16, 16, 16, entry.global() ? 0.5F : 1F);
                boolean pinned = pinnedNames.contains(entry.name());
                boolean pinHover = over(mouseX, mouseY, xPosition + 20, yPosition + 2, 16, 16);
                int pinU = pinned ? (pinHover ? 148 : 132) : (pinHover ? 116 : 100);
                drawMenuIcon(xPosition + 20, yPosition + 2, pinU, 152, 16, 16, 16, 16, 1F);
            } else if (pinnedNames.contains(entry.name())) {
                drawMenuIcon(xPosition + 2, yPosition + 2, 164, 152, 16, 16, 16, 16, 1F);
            }
        }

        private boolean isAvailable() {
            boolean dimensionAllowed = entry.dimension() == mc.thePlayer.dimension
                    || (entry.global() ? WaystoneConfig.globalInterDimension : WaystoneConfig.interDimension);
            boolean cooldownReady = freeWarp || entry.global() && WaystoneConfig.globalNoCooldown
                    || System.currentTimeMillis() - ClientWaystoneState.getLastWarpStoneUse()
                    >= WaystoneManager.warpStoneCooldownMs();
            return dimensionAllowed && cooldownReady
                    && (freeWarp || WaystoneXpCost.canAfford(mc.thePlayer, xpCost));
        }
    }
}
