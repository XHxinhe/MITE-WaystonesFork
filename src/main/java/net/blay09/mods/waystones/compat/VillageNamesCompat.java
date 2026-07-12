package net.blay09.mods.waystones.compat;

import net.blay09.mods.waystones.Waystones;
import net.minecraft.NBTBase;
import net.minecraft.NBTTagCompound;
import net.minecraft.NBTTagList;
import net.minecraft.World;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

public final class VillageNamesCompat {
    private VillageNamesCompat() {
    }

    public static String ensureVillageName(World world, int x, int y, int z) {
        try {
            Class<?> dataClass = Class.forName("astrotibs.villagenames.nbt.VNWorldDataStructure");
            Object data = dataClass.getMethod("forWorld", World.class, String.class, String.class)
                    .invoke(null, world, "villagenames3_Village", "NamedStructures");
            NBTTagCompound root = (NBTTagCompound) dataClass.getMethod("getData").invoke(data);
            for (Object value : root.getTags()) {
                if (!(value instanceof NBTBase namedTag)) {
                    continue;
                }
                NBTTagList list = root.getTagList(namedTag.getName());
                if (list.tagCount() == 0 || !(list.tagAt(0) instanceof NBTTagCompound entry)) {
                    continue;
                }
                double dx = entry.getInteger("signX") - x;
                double dz = entry.getInteger("signZ") - z;
                if (dx * dx + dz * dz <= 10000D) {
                    return joinName(entry.getString("namePrefix"), entry.getString("nameRoot"),
                            entry.getString("nameSuffix"));
                }
            }

            Class<?> functionsClass = Class.forName("astrotibs.villagenames.utility.FunctionsVN");
            long salt = ((Number) functionsClass.getMethod("getUniqueLongForXYZ", int.class, int.class, int.class)
                    .invoke(null, x, y, z)).longValue();
            Random random = new Random(world.getSeed() + salt);
            Class<?> generatorClass = Class.forName("astrotibs.villagenames.name.NameGenerator");
            String[] parts = (String[]) generatorClass.getMethod("newRandomName", String.class, Random.class)
                    .invoke(null, "Village", random);
            String prefix = parts.length > 1 ? parts[1] : "";
            String rootName = parts.length > 2 ? parts[2] : "";
            String suffix = parts.length > 3 ? parts[3] : "";

            int[] colors = bannerColors(random);
            NBTTagCompound entry = new NBTTagCompound();
            entry.setInteger("signX", x);
            entry.setInteger("signY", y);
            entry.setInteger("signZ", z);
            entry.setInteger("townColor", colors[0]);
            entry.setInteger("townColor2", colors[1]);
            entry.setString("namePrefix", prefix);
            entry.setString("nameRoot", rootName);
            entry.setString("nameSuffix", suffix);
            entry.setBoolean("fromEntity", true);
            NBTTagList list = new NBTTagList();
            list.appendTag(entry);
            String name = joinName(prefix, rootName, suffix);
            root.setTag(name + ", x" + x + " y" + y + " z" + z, list);
            dataClass.getMethod("markDirty").invoke(data);
            return name;
        } catch (ClassNotFoundException ignored) {
            return null;
        } catch (ReflectiveOperationException | LinkageError exception) {
            Waystones.LOGGER.warn("Village Names compatibility failed", exception);
            return null;
        }
    }

    private static int[] bannerColors(Random random) {
        try {
            Class<?> bannerClass = Class.forName("astrotibs.villagenames.banner.BannerGenerator");
            Object[] banner = (Object[]) bannerClass
                    .getMethod("randomBannerArrays", Random.class, int.class, int.class)
                    .invoke(null, random, -1, -1);
            if (banner.length > 1 && banner[1] instanceof List<?> values && !values.isEmpty()) {
                int first = 15 - ((Number) values.get(0)).intValue();
                int second = values.size() > 1 ? 15 - ((Number) values.get(1)).intValue() : first;
                return new int[]{first, second};
            }
        } catch (ReflectiveOperationException | LinkageError | ClassCastException ignored) {
        }
        return new int[]{0, 0};
    }

    private static String joinName(String prefix, String root, String suffix) {
        return (prefix + " " + root + " " + suffix).trim().replaceAll("\\s+", " ");
    }
}
