package net.blay09.mods.waystones.client.render;

import net.minecraft.ModelBase;
import net.minecraft.ModelRenderer;

public final class ModelWaystone extends ModelBase {
    private final ModelRenderer top;
    private final ModelRenderer topMidTop;
    private final ModelRenderer topMidBottom;
    private final ModelRenderer topBottom;
    private final ModelRenderer pillar;
    private final ModelRenderer baseTop;
    private final ModelRenderer baseMid;
    private final ModelRenderer baseBottom;

    public ModelWaystone() {
        textureWidth = 256;
        textureHeight = 256;
        top = part(0, 0, -8, -64, -8, 16, 4, 16);
        topMidTop = part(64, 0, -10, -60, -10, 20, 4, 20);
        topMidBottom = part(0, 76, -14, -56, -14, 28, 4, 28);
        topBottom = part(0, 24, -12, -52, -12, 24, 4, 24);
        pillar = part(144, -2, -10, -48, -10, 20, 30, 20);
        baseTop = part(96, 48, -12, -18, -12, 24, 4, 24);
        baseMid = part(112, 76, -14, -14, -14, 28, 8, 28);
        baseBottom = part(0, 112, -16, -6, -16, 32, 6, 32);
    }

    private ModelRenderer part(int u, int v, float x, float y, float z, int w, int h, int d) {
        ModelRenderer part = new ModelRenderer(this, u, v);
        part.addBox(x, y, z, w, h, d);
        return part;
    }

    public void renderAll() {
        float scale = 0.0625F;
        top.render(scale);
        topMidTop.render(scale);
        topMidBottom.render(scale);
        topBottom.render(scale);
        pillar.render(scale);
        baseTop.render(scale);
        baseMid.render(scale);
        baseBottom.render(scale);
    }

    public void renderPillar() {
        pillar.render(0.0625F);
    }
}
