package com.electrifiedded.herobrinemod.client;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

/** Biped (player-shaped) renderer with a fixed texture, held item and scale (mini mode). */
public class RendererHMod<T extends EntityLiving> extends RenderBiped<T> {
    private final ResourceLocation texture;
    private final float scale;

    public RendererHMod(RenderManager manager, ModelBiped model, ResourceLocation texture, float scale) {
        super(manager, model, 0.5F * scale);
        this.texture = texture;
        this.scale = scale;
    }

    @Override
    protected void preRenderCallback(T entity, float partialTicks) {
        if (scale != 1.0F) GlStateManager.scale(scale, scale, scale);
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return texture;
    }
}
