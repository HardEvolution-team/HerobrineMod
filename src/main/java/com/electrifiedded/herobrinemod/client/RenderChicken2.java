package com.electrifiedded.herobrinemod.client;

import com.electrifiedded.herobrinemod.entity.EntityChicken2;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderChicken2 extends RenderLiving<EntityChicken2> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("herobrinemod", "textures/mob/angrychicken.png");
    private final float scale;

    public RenderChicken2(RenderManager manager, float shadow, float scale) {
        super(manager, new ModelChicken2(), shadow);
        this.scale = scale;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityChicken2 e) {
        return TEXTURE;
    }

    /** Wing flap angle (passed to the model as ageInTicks). */
    @Override
    protected float handleRotationFloat(EntityChicken2 e, float pt) {
        float f = e.oFlap + (e.wingRotation - e.oFlap) * pt;
        float f1 = e.oFlapSpeed + (e.destPos - e.oFlapSpeed) * pt;
        return (MathHelper.sin(f) + 1.0F) * f1;
    }

    @Override
    protected void preRenderCallback(EntityChicken2 e, float pt) {
        if (scale != 1.0F) GlStateManager.scale(scale, scale, scale);
    }

    @Override
    protected float getDeathMaxRotation(EntityChicken2 e) {
        return 90.0F;
    }
}
