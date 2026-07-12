package net.blay09.mods.waystones.client;

import moddedmite.rustedironcore.api.event.Handlers;
import moddedmite.rustedironcore.api.event.listener.ITickListener;
import net.minecraft.GuiScreen;
import net.minecraft.Minecraft;

import java.util.concurrent.atomic.AtomicReference;

public final class ClientScreenQueue implements ITickListener {
    private static final ClientScreenQueue INSTANCE = new ClientScreenQueue();
    private static final AtomicReference<GuiScreen> PENDING_SCREEN = new AtomicReference<>();
    private static boolean registered;

    private ClientScreenQueue() {
    }

    public static void init() {
        if (!registered) {
            Handlers.Tick.register(INSTANCE);
            registered = true;
        }
    }

    public static void open(GuiScreen screen) {
        PENDING_SCREEN.set(screen);
    }

    @Override
    public void onClientTick(Minecraft client) {
        GuiScreen screen = PENDING_SCREEN.getAndSet(null);
        if (screen == null) {
            return;
        }
        client.displayGuiScreen(screen);
    }
}
