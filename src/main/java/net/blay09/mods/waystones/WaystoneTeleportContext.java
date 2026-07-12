package net.blay09.mods.waystones;

import net.minecraft.Entity;

public final class WaystoneTeleportContext {
    private static final ThreadLocal<Entity> PORTAL_BYPASS = new ThreadLocal<>();

    private WaystoneTeleportContext() {
    }

    public static void runWithoutPortal(Entity entity, Runnable action) {
        PORTAL_BYPASS.set(entity);
        try {
            action.run();
        } finally {
            PORTAL_BYPASS.remove();
        }
    }

    public static boolean bypassesPortal(Entity entity) {
        return PORTAL_BYPASS.get() == entity;
    }
}
