package net.blay09.mods.waystones.worldgen;

import net.blay09.mods.waystones.block.TileWaystone;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

record WaystoneWorldgenRule(String structure, double chance, int variant, String fixedName,
                             boolean forceGlobal, boolean autoActivateGlobal,
                             Set<Integer> dimensions, Set<Integer> biomes) {
    static WaystoneWorldgenRule parse(String encoded) {
        Map<String, String> values = new HashMap<>();
        for (String part : encoded.split(";")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2) {
                values.put(pair[0].trim(), pair[1].trim());
            }
        }
        String structure = values.getOrDefault("structure", "").toLowerCase();
        if (structure.isEmpty()) {
            return null;
        }
        double chance;
        try {
            chance = Math.max(0.0D, Math.min(1.0D, Double.parseDouble(values.getOrDefault("chance", "1"))));
        } catch (NumberFormatException ignored) {
            chance = 1.0D;
        }
        String fixedName = values.getOrDefault("name", "");
        boolean forceGlobal = Boolean.parseBoolean(values.getOrDefault("forceGlobal", "false"))
                && !fixedName.isEmpty();
        return new WaystoneWorldgenRule(structure, chance, variant(values.getOrDefault("type", "auto")),
                fixedName, forceGlobal,
                Boolean.parseBoolean(values.getOrDefault("autoActivateGlobal", "false")),
                integerSet(values.getOrDefault("dimensionWhitelist", "*")),
                integerSet(values.getOrDefault("biomeWhitelist", "*")));
    }

    boolean accepts(int dimension, int biome) {
        return (dimensions.isEmpty() || dimensions.contains(dimension))
                && (biomes.isEmpty() || biomes.contains(biome));
    }

    private static int variant(String name) {
        return switch (name.toLowerCase()) {
            case "sandy", "sandstone" -> TileWaystone.VARIANT_SANDSTONE;
            case "mossy" -> TileWaystone.VARIANT_MOSSY;
            case "stonebrick" -> TileWaystone.VARIANT_STONEBRICK;
            case "mossy_stonebrick", "mossystonebrick" -> TileWaystone.VARIANT_MOSSY_STONEBRICK;
            case "nether", "netherbrick" -> TileWaystone.VARIANT_NETHER;
            case "end", "endstone" -> TileWaystone.VARIANT_END;
            case "stone" -> TileWaystone.VARIANT_STONE;
            default -> -1;
        };
    }

    private static Set<Integer> integerSet(String value) {
        Set<Integer> result = new HashSet<>();
        if (value.equals("*")) {
            return result;
        }
        for (String part : value.split(",")) {
            try {
                result.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }
}
