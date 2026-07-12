package net.blay09.mods.waystones.client.render;

import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.ClientWaystoneState;
import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.WaystoneManager;
import net.blay09.mods.waystones.block.TileWaystone;
import net.minecraft.Minecraft;
import net.minecraft.ResourceLocation;
import net.minecraft.RenderManager;
import net.minecraft.FontRenderer;
import net.minecraft.Gui;
import net.minecraft.Icon;
import net.minecraft.TextureManager;
import net.minecraft.TextureMap;
import net.minecraft.TileEntity;
import net.minecraft.TileEntitySpecialRenderer;
import net.minecraft.Tessellator;
import org.lwjgl.opengl.GL11;

import java.util.Locale;
import java.util.Random;

public final class RenderWaystone extends TileEntitySpecialRenderer {
    private static final ResourceLocation[] TEXTURES = textures("", "sandstone", "mossy", "stonebrick",
            "netherbrick", "endstone", "mossystonebrick");
    private static final ResourceLocation[] ACTIVE_TEXTURES = textures("_active", "sandstone_active", "mossy_active",
            "stonebrick_active", "netherbrick_active", "endstone_active", "stonebrick_active");
    private static final ResourceLocation END_SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
    private static final Random END_PORTAL_RANDOM = new Random(31100L);
    private static final float SCALE = 0.0625F;
    private static final float PILLAR_X_MIN = -10F * SCALE;
    private static final float PILLAR_X_MAX = 10F * SCALE;
    private static final float PILLAR_Z_MIN = -10F * SCALE;
    private static final float PILLAR_Z_MAX = 10F * SCALE;
    private static final float PILLAR_Y_TOP = -48F * SCALE;
    private static final float PILLAR_Y_BOTTOM = -18F * SCALE;
    private static final float UV_U0 = 144F / 256F;
    private static final float UV_U1 = 164F / 256F;
    private static final float UV_U2 = 184F / 256F;
    private static final float UV_U3 = 204F / 256F;
    private static final float UV_U4 = 224F / 256F;
    private static final float UV_V_TOP = 18F / 256F;
    private static final float UV_V_BOTTOM = 48F / 256F;
    private static final float LAVA_UV_SCALE = 3F;
    private static final float END_PORTAL_UV_SCALE = 3F;
    private final ModelWaystone model = new ModelWaystone();

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
        TileWaystone waystone = (TileWaystone) tileEntity;
        if (waystone.isUpperPart()) {
            return;
        }
        boolean active = waystone.hasWorldObj() && ClientWaystoneState.isActive(
                waystone.getWorldObj().provider.dimensionId,
                waystone.xCoord, waystone.yCoord, waystone.zCoord);
        float glowProgress = 1.0F;
        if (active && WaystoneConfig.showCooldownOnWaystone && WaystoneManager.warpStoneCooldownMs() > 0
                && !(waystone.isGlobal() && WaystoneConfig.globalNoCooldown)) {
            glowProgress = Math.min(1.0F, Math.max(0.0F,
                    (System.currentTimeMillis() - ClientWaystoneState.getLastWarpStoneUse())
                            / (float) WaystoneManager.warpStoneCooldownMs()));
        }
        renderModel(x, y, z, waystone.getFacing(), waystone.getVariant(), active, glowProgress);
        if (WaystoneConfig.showNametag && !waystone.getWaystoneName().isEmpty()
                && Minecraft.getMinecraft().thePlayer.getDistanceSqToBlock(
                waystone.xCoord, waystone.yCoord, waystone.zCoord) <= 4096.0D) {
            renderName(waystone.getWaystoneName(), x + 0.5D, y + 2.35D, z + 0.5D);
        }
    }

    public void renderItem(double x, double y, double z, int variant) {
        renderModel(x, y, z, 0, variant, false, 1.0F);
    }

    private void renderModel(double x, double y, double z, int facing, int variant, boolean active,
                             float glowProgress) {
        TextureManager textures = Minecraft.getMinecraft().getTextureManager();
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GL11.glRotatef(facing * 90.0F, 0F, 1F, 0F);
        GL11.glRotatef(-180F, 1F, 0F, 0F);
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        int textureIndex = Math.max(0, Math.min(TEXTURES.length - 1, variant));
        textures.bindTexture(TEXTURES[textureIndex]);
        model.renderAll();
        if (active) {
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_POLYGON_BIT);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glColor4f(1F, 1F, 1F, 1F);
            textures.bindTexture(ACTIVE_TEXTURES[textureIndex]);
            if (!WaystoneConfig.disableTextGlow) {
                GL11.glDisable(GL11.GL_LIGHTING);
                Minecraft.getMinecraft().entityRenderer.disableLightmap(0.0D);
            }
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPolygonOffset(-1F, -1F);
            float[] bounds = overlayBounds(variant);
            float clipY = bounds[0] + glowProgress * (bounds[1] - bounds[0]);
            if (variant == TileWaystone.VARIANT_NETHER) {
                renderNetherLavaOverlay(textures, clipY);
            } else if (variant == TileWaystone.VARIANT_END) {
                renderEndPortalOverlay(textures, clipY);
            } else {
                renderPillarClipped(clipY);
            }
            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            if (!WaystoneConfig.disableTextGlow) {
                Minecraft.getMinecraft().entityRenderer.enableLightmap(0.0D);
                GL11.glEnable(GL11.GL_LIGHTING);
            }
            GL11.glPopAttrib();
        }
        GL11.glPopMatrix();
    }

    private static float[] overlayBounds(int variant) {
        float lower = -18F;
        float upper = -48F;
        for (String line : WaystoneConfig.overlayClipBounds) {
            String variantName = "";
            float parsedLower = lower;
            float parsedUpper = upper;
            for (String segment : line.split(";")) {
                String[] pair = segment.trim().split("=", 2);
                if (pair.length != 2) {
                    continue;
                }
                String key = pair[0].trim().toLowerCase(Locale.ROOT);
                String value = pair[1].trim();
                if (key.equals("variant")) {
                    variantName = value;
                } else if (key.equals("lower")) {
                    parsedLower = parseFloat(value, parsedLower);
                } else if (key.equals("upper")) {
                    parsedUpper = parseFloat(value, parsedUpper);
                }
            }
            if (variantId(variantName) == variant) {
                return new float[]{parsedLower * SCALE, parsedUpper * SCALE};
            }
        }
        return new float[]{lower * SCALE, upper * SCALE};
    }

    private static float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int variantId(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "stone" -> TileWaystone.VARIANT_STONE;
            case "sandstone", "sandy" -> TileWaystone.VARIANT_SANDSTONE;
            case "mossy" -> TileWaystone.VARIANT_MOSSY;
            case "stonebrick" -> TileWaystone.VARIANT_STONEBRICK;
            case "mossy_stonebrick", "mossystonebrick" -> TileWaystone.VARIANT_MOSSY_STONEBRICK;
            case "nether", "netherbrick" -> TileWaystone.VARIANT_NETHER;
            case "end", "endstone" -> TileWaystone.VARIANT_END;
            default -> -1;
        };
    }

    private static void renderNetherLavaOverlay(TextureManager textures, float clipY) {
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0F);
        GL11.glColorMask(false, false, false, false);
        GL11.glDisable(GL11.GL_BLEND);
        renderPillarClipped(clipY);

        GL11.glColorMask(true, true, true, true);
        GL11.glDepthFunc(GL11.GL_EQUAL);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        textures.bindTexture(TextureMap.locationBlocksTexture);
        Icon lava = net.minecraft.Block.lavaMoving.getIcon(0, 0);
        renderPillarClippedDirectUVs(clipY, lava.getMinU(), lava.getMaxU(), lava.getMinV(), lava.getMaxV());
    }

    private static void renderEndPortalOverlay(TextureManager textures, float clipY) {
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0F);
        GL11.glColorMask(false, false, false, false);
        GL11.glDisable(GL11.GL_BLEND);
        renderPillarClipped(clipY);

        GL11.glColorMask(true, true, true, true);
        GL11.glDepthFunc(GL11.GL_EQUAL);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        END_PORTAL_RANDOM.setSeed(31100L);
        float timeOffset = (Minecraft.getSystemTime() % 700000L) / 700000F;
        for (int i = 0; i < 16; i++) {
            float depth = 16F - i;
            float layerScale = i == 0 ? 0.125F : 0.5F;
            float brightness = 1F / (depth + 1F);
            if (i == 0) {
                textures.bindTexture(END_SKY_TEXTURE);
                brightness = 0.1F;
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            } else if (i == 1) {
                textures.bindTexture(END_PORTAL_TEXTURE);
                GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            }
            float red = END_PORTAL_RANDOM.nextFloat() * 0.5F + 0.1F;
            float green = END_PORTAL_RANDOM.nextFloat() * 0.5F + 0.4F;
            float blue = END_PORTAL_RANDOM.nextFloat() * 0.5F + 0.5F;
            if (i == 0) {
                red = green = blue = 1F;
            }
            GL11.glColor4f(red * brightness, green * brightness, blue * brightness, 1F);
            renderPillarClippedTransformedUVs(clipY, layerScale,
                    (i * i * 4321F + i * 9F) * 2F, timeOffset);
        }
    }

    private static void renderPillarClipped(float clipY) {
        float yMin = Math.max(PILLAR_Y_TOP, Math.min(PILLAR_Y_BOTTOM, clipY));
        float yMax = PILLAR_Y_BOTTOM;
        if (yMin >= yMax) {
            return;
        }
        float progress = (yMin - PILLAR_Y_TOP) / (PILLAR_Y_BOTTOM - PILLAR_Y_TOP);
        float vMin = UV_V_TOP + (UV_V_BOTTOM - UV_V_TOP) * progress;
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        face(tess, PILLAR_X_MAX, yMin, PILLAR_Z_MAX, PILLAR_X_MAX, yMin, PILLAR_Z_MIN,
                PILLAR_X_MAX, yMax, PILLAR_Z_MIN, PILLAR_X_MAX, yMax, PILLAR_Z_MAX,
                UV_U3, UV_U2, UV_U2, UV_U3, vMin, UV_V_BOTTOM);
        face(tess, PILLAR_X_MIN, yMin, PILLAR_Z_MIN, PILLAR_X_MIN, yMin, PILLAR_Z_MAX,
                PILLAR_X_MIN, yMax, PILLAR_Z_MAX, PILLAR_X_MIN, yMax, PILLAR_Z_MIN,
                UV_U1, UV_U0, UV_U0, UV_U1, vMin, UV_V_BOTTOM);
        face(tess, PILLAR_X_MAX, yMin, PILLAR_Z_MIN, PILLAR_X_MIN, yMin, PILLAR_Z_MIN,
                PILLAR_X_MIN, yMax, PILLAR_Z_MIN, PILLAR_X_MAX, yMax, PILLAR_Z_MIN,
                UV_U2, UV_U1, UV_U1, UV_U2, vMin, UV_V_BOTTOM);
        face(tess, PILLAR_X_MIN, yMin, PILLAR_Z_MAX, PILLAR_X_MAX, yMin, PILLAR_Z_MAX,
                PILLAR_X_MAX, yMax, PILLAR_Z_MAX, PILLAR_X_MIN, yMax, PILLAR_Z_MAX,
                UV_U4, UV_U3, UV_U3, UV_U4, vMin, UV_V_BOTTOM);
        tess.draw();
    }

    private static void face(Tessellator tess, float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             float u1, float u2, float u3, float u4, float vTop, float vBottom) {
        tess.addVertexWithUV(x1, y1, z1, u1, vTop);
        tess.addVertexWithUV(x2, y2, z2, u2, vTop);
        tess.addVertexWithUV(x3, y3, z3, u3, vBottom);
        tess.addVertexWithUV(x4, y4, z4, u4, vBottom);
    }

    private static void renderPillarClippedDirectUVs(float clipY, float uMin, float uMax,
                                                     float vMin, float vMax) {
        float yMin = Math.max(PILLAR_Y_TOP, Math.min(PILLAR_Y_BOTTOM, clipY));
        if (yMin >= PILLAR_Y_BOTTOM) {
            return;
        }
        float uMid = (uMin + uMax) * 0.5F;
        float vMid = (vMin + vMax) * 0.5F;
        float uHalf = (uMax - uMin) * 0.5F / LAVA_UV_SCALE;
        float vHalf = (vMax - vMin) * 0.5F / LAVA_UV_SCALE;
        uMin = uMid - uHalf;
        uMax = uMid + uHalf;
        vMin = vMid - vHalf;
        vMax = vMid + vHalf;
        float progress = (yMin - PILLAR_Y_TOP) / (PILLAR_Y_BOTTOM - PILLAR_Y_TOP);
        float vClip = vMin + (vMax - vMin) * progress;
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        directFace(tess, PILLAR_X_MAX, yMin, PILLAR_Z_MAX, PILLAR_X_MAX, yMin, PILLAR_Z_MIN,
                PILLAR_X_MAX, PILLAR_Y_BOTTOM, PILLAR_Z_MIN, PILLAR_X_MAX, PILLAR_Y_BOTTOM, PILLAR_Z_MAX,
                uMax, uMin, vClip, vMax);
        directFace(tess, PILLAR_X_MIN, yMin, PILLAR_Z_MIN, PILLAR_X_MIN, yMin, PILLAR_Z_MAX,
                PILLAR_X_MIN, PILLAR_Y_BOTTOM, PILLAR_Z_MAX, PILLAR_X_MIN, PILLAR_Y_BOTTOM, PILLAR_Z_MIN,
                uMax, uMin, vClip, vMax);
        directFace(tess, PILLAR_X_MAX, yMin, PILLAR_Z_MIN, PILLAR_X_MIN, yMin, PILLAR_Z_MIN,
                PILLAR_X_MIN, PILLAR_Y_BOTTOM, PILLAR_Z_MIN, PILLAR_X_MAX, PILLAR_Y_BOTTOM, PILLAR_Z_MIN,
                uMax, uMin, vClip, vMax);
        directFace(tess, PILLAR_X_MIN, yMin, PILLAR_Z_MAX, PILLAR_X_MAX, yMin, PILLAR_Z_MAX,
                PILLAR_X_MAX, PILLAR_Y_BOTTOM, PILLAR_Z_MAX, PILLAR_X_MIN, PILLAR_Y_BOTTOM, PILLAR_Z_MAX,
                uMax, uMin, vClip, vMax);
        tess.draw();
    }

    private static void directFace(Tessellator tess, float x1, float y1, float z1, float x2, float y2, float z2,
                                   float x3, float y3, float z3, float x4, float y4, float z4,
                                   float u1, float u2, float vTop, float vBottom) {
        tess.addVertexWithUV(x1, y1, z1, u1, vTop);
        tess.addVertexWithUV(x2, y2, z2, u2, vTop);
        tess.addVertexWithUV(x3, y3, z3, u2, vBottom);
        tess.addVertexWithUV(x4, y4, z4, u1, vBottom);
    }

    private static void renderPillarClippedTransformedUVs(float clipY, float uvScale,
                                                           float rotation, float timeOffset) {
        float yMin = Math.max(PILLAR_Y_TOP, Math.min(PILLAR_Y_BOTTOM, clipY));
        if (yMin >= PILLAR_Y_BOTTOM) {
            return;
        }
        float vTop = (yMin - PILLAR_Y_TOP) / (PILLAR_Y_BOTTOM - PILLAR_Y_TOP);
        float radians = (float) Math.toRadians(rotation);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        float zoom = 1F / END_PORTAL_UV_SCALE;
        float u0 = 0.5F - 0.5F * zoom;
        float u1 = 0.5F + 0.5F * zoom;
        float v0 = 0.5F + (vTop - 0.5F) * zoom;
        float v1 = 0.5F + 0.5F * zoom;
        float[] a = portalUv(u1, v0, uvScale, cos, sin, timeOffset);
        float[] b = portalUv(u0, v0, uvScale, cos, sin, timeOffset);
        float[] c = portalUv(u0, v1, uvScale, cos, sin, timeOffset);
        float[] d = portalUv(u1, v1, uvScale, cos, sin, timeOffset);
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        transformedFace(tess, PILLAR_X_MAX, yMin, PILLAR_Z_MAX, PILLAR_X_MAX, yMin, PILLAR_Z_MIN,
                PILLAR_X_MAX, PILLAR_Y_BOTTOM, PILLAR_Z_MIN, PILLAR_X_MAX, PILLAR_Y_BOTTOM, PILLAR_Z_MAX,
                a, b, c, d);
        transformedFace(tess, PILLAR_X_MIN, yMin, PILLAR_Z_MIN, PILLAR_X_MIN, yMin, PILLAR_Z_MAX,
                PILLAR_X_MIN, PILLAR_Y_BOTTOM, PILLAR_Z_MAX, PILLAR_X_MIN, PILLAR_Y_BOTTOM, PILLAR_Z_MIN,
                a, b, c, d);
        transformedFace(tess, PILLAR_X_MAX, yMin, PILLAR_Z_MIN, PILLAR_X_MIN, yMin, PILLAR_Z_MIN,
                PILLAR_X_MIN, PILLAR_Y_BOTTOM, PILLAR_Z_MIN, PILLAR_X_MAX, PILLAR_Y_BOTTOM, PILLAR_Z_MIN,
                a, b, c, d);
        transformedFace(tess, PILLAR_X_MIN, yMin, PILLAR_Z_MAX, PILLAR_X_MAX, yMin, PILLAR_Z_MAX,
                PILLAR_X_MAX, PILLAR_Y_BOTTOM, PILLAR_Z_MAX, PILLAR_X_MIN, PILLAR_Y_BOTTOM, PILLAR_Z_MAX,
                a, b, c, d);
        tess.draw();
    }

    private static float[] portalUv(float u, float v, float scale, float cos, float sin, float timeOffset) {
        return new float[]{((u - 0.5F) * cos - (v - 0.5F) * sin + 0.5F) * scale,
                ((u - 0.5F) * sin + (v - 0.5F) * cos + 0.5F) * scale + timeOffset};
    }

    private static void transformedFace(Tessellator tess,
                                        float x1, float y1, float z1, float x2, float y2, float z2,
                                        float x3, float y3, float z3, float x4, float y4, float z4,
                                        float[] a, float[] b, float[] c, float[] d) {
        tess.addVertexWithUV(x1, y1, z1, a[0], a[1]);
        tess.addVertexWithUV(x2, y2, z2, b[0], b[1]);
        tess.addVertexWithUV(x3, y3, z3, c[0], c[1]);
        tess.addVertexWithUV(x4, y4, z4, d[0], d[1]);
    }

    private static void renderName(String name, double x, double y, double z) {
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        float scale = 0.016666668F * 1.6F;
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-scale, -scale, scale);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        int half = font.getStringWidth(name) / 2;
        Gui.drawRect(-half - 2, -2, half + 2, 9, 0x55000000);
        font.drawString(name, -half, 0, 0xFFFFFFFF);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private static ResourceLocation[] textures(String stoneSuffix, String sandstone, String mossy,
                                               String stonebrick, String nether, String end, String mossyStonebrick) {
        return new ResourceLocation[]{
                texture(stoneSuffix.isEmpty() ? "waystone" : "waystone" + stoneSuffix),
                texture(sandstone), texture(mossy), texture(stonebrick),
                texture(nether), texture(end), texture(mossyStonebrick)
        };
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(Waystones.MOD_ID, "textures/entity/" + name + ".png");
    }
}
