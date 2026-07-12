package net.blay09.mods.waystones;

import net.minecraft.EntityPlayer;

public final class WaystoneXpCost {
    private WaystoneXpCost() {
    }

    public static int get(EntityPlayer player, WaystoneEntry origin, WaystoneEntry target) {
        if (WaystoneConfig.xpBaseCost < 0) {
            return -1;
        }
        int cost = WaystoneConfig.xpBaseCost;
        int sourceDimension = origin == null ? player.dimension : origin.dimension();
        if (sourceDimension != target.dimension()) {
            return cost + WaystoneConfig.xpCrossDimCost;
        }
        if (WaystoneConfig.xpBlocksPerLevel <= 0) {
            return cost;
        }
        double sourceX = origin == null ? player.posX : origin.x();
        double sourceY = origin == null ? player.posY : origin.y();
        double sourceZ = origin == null ? player.posZ : origin.z();
        double dx = sourceX - target.x();
        double dy = sourceY - target.y();
        double dz = sourceZ - target.z();
        return cost + (int) Math.floor(Math.sqrt(dx * dx + dy * dy + dz * dz)
                / WaystoneConfig.xpBlocksPerLevel);
    }

    public static boolean canAfford(EntityPlayer player, int levels) {
        return levels < 0 || player.inCreativeMode() || player.getExperienceLevel() >= levels;
    }

    public static void deduct(EntityPlayer player, int levels) {
        if (levels <= 0 || player.inCreativeMode()) {
            return;
        }
        int targetLevel = Math.max(0, player.getExperienceLevel() - levels);
        int low = 0;
        int high = Math.max(0, player.experience);
        while (low < high) {
            int mid = low + (high - low) / 2;
            if (player.getExperienceLevel(mid) < targetLevel) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        player.addExperience(low - player.experience, false, true);
    }
}
