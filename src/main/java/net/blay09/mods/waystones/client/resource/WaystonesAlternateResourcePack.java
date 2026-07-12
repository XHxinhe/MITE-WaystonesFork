package net.blay09.mods.waystones.client.resource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.MetadataSection;
import net.minecraft.MetadataSerializer;
import net.minecraft.ResourceLocation;
import net.minecraft.ResourcePack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

public final class WaystonesAlternateResourcePack implements ResourcePack {
    public static final String PACK_NAME = "Waystones Modernity Textures";
    private static final String PREFIX = "/waystones_modernity/";
    private static final String PACK_META = "{\"pack\":{\"description\":"
            + "\"Modernity textures for Waystones-X, by DarkBum\",\"pack_format\":1}}";

    @Override
    public InputStream getInputStream(ResourceLocation location) {
        return getClass().getResourceAsStream(PREFIX + path(location));
    }

    @Override
    public boolean resourceExists(ResourceLocation location) {
        return getClass().getResource(PREFIX + path(location)) != null;
    }

    @Override
    public Set getResourceDomains() {
        return Collections.singleton("waystones");
    }

    @Override
    public MetadataSection getPackMetadata(MetadataSerializer serializer, String section) {
        JsonObject json = JsonParser.parseString(PACK_META).getAsJsonObject();
        return serializer.parseMetadataSection(section, json);
    }

    @Override
    public BufferedImage getPackImage() {
        try (InputStream input = getClass().getResourceAsStream(PREFIX + "pack.png")) {
            if (input != null) {
                return ImageIO.read(input);
            }
        } catch (Exception ignored) {
        }
        try (InputStream input = getClass().getResourceAsStream("/assets/waystones/modernity_pack_logo.png")) {
            return input == null ? null : ImageIO.read(input);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public String getPackName() {
        return PACK_NAME;
    }

    private static String path(ResourceLocation location) {
        return "assets/" + location.getResourceDomain() + "/" + location.getResourcePath();
    }
}
