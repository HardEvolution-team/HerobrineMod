package com.electrifiedded.herobrinemod.client;

import com.electrifiedded.archaiclib.legacy.client.LegacyRenderLiving;
import com.electrifiedded.herobrinemod.CommonProxy;
import com.electrifiedded.herobrinemod.HerobrineMod;
import com.electrifiedded.herobrinemod.entity.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelCow;
import net.minecraft.client.model.ModelPig;
import net.minecraft.client.model.ModelVillager;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import static com.electrifiedded.archaiclib.legacy.client.LegacyRenderLiving.tex;

public class ClientProxy extends CommonProxy {
    private static final String M = "herobrinemod:textures/mob/";

    @Override
    public void preInit() {
        float s = HerobrineMod.miniMode == 1 ? 0.5F : 1.0F;
        biped(EntitySucker.class, "sucker", s);
        biped(EntityFighter.class, "sucker", s);
        biped(EntityFighter2.class, "notch", s);
        biped(EntityHerobrine.class, "sucker", s);
        biped(EntityStalker.class, "sucker", s);
        biped(EntityNorris.class, "chuck", s);
        biped(EntityBuilder.class, "sucker", s);
        biped(EntityHerobrineBoss.class, "sucker", s);
        biped(EntityJetra.class, "jetra", 1.0F);
        RenderingRegistry.registerEntityRenderingHandler(EntityZombie2.class,
                m -> new RendererHMod<>(m, new ModelZombie(), tex(M + "zombie.png"), s));
        RenderingRegistry.registerEntityRenderingHandler(EntityHVillager.class,
                m -> new LegacyRenderLiving<EntityHVillager>(m, new ModelVillager(0.0F), 0.5F, e -> tex(M + "hvillager.png")) {
                    @Override
                    protected void preRenderCallback(EntityHVillager e, float pt) {
                        net.minecraft.client.renderer.GlStateManager.scale(0.9375F, 0.9375F, 0.9375F);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityCow2.class, m -> new LegacyRenderLiving<EntityCow2>(m, new ModelCow(), 0.5F * s, e -> tex(M + "cowbrine.png")) {
            @Override
            protected void preRenderCallback(EntityCow2 e, float pt) {
                net.minecraft.client.renderer.GlStateManager.scale(s, s, s);
            }
        });
        RenderingRegistry.registerEntityRenderingHandler(EntityPig2.class, m -> new LegacyRenderLiving<EntityPig2>(m, new ModelPig(), 0.5F * s, e -> tex(M + "pigbrine.png")) {
            @Override
            protected void preRenderCallback(EntityPig2 e, float pt) {
                net.minecraft.client.renderer.GlStateManager.scale(s, s, s);
            }
        });
        RenderingRegistry.registerEntityRenderingHandler(EntityChicken2.class, m -> new RenderChicken2(m, 0.3F * s, s));
    }

    private static <T extends EntityLiving> void biped(Class<T> cls, String texture, float scale) {
        RenderingRegistry.registerEntityRenderingHandler(cls, m -> new RendererHMod<T>(m, new ModelBiped(), tex(M + texture + ".png"), scale));
    }

    @Override
    public void freezeLocal(EntityPlayer player, Entity herobrine) {
        EntityPlayerSP me = Minecraft.getMinecraft().player;
        if (me == null || player != me) return;
        me.motionX = 0.0D;
        me.motionY = 0.0D;
        me.motionZ = 0.0D;
        double dx = herobrine.posX - me.posX, dz = herobrine.posZ - me.posZ;
        double dy = herobrine.posY + herobrine.getEyeHeight() - (me.posY + me.getEyeHeight());
        double dh = MathHelper.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(dy, dh) * 180.0D / Math.PI));
        me.rotationPitch = rot(me.rotationPitch, pitch, 50.0F);
        me.rotationYaw = rot(me.rotationYaw, yaw, 50.0F);
        me.timeInPortal = 0.5F;
        me.prevTimeInPortal = 0.5F;
    }

    private static float rot(float cur, float target, float max) {
        float d = MathHelper.wrapDegrees(target - cur);
        if (d > max) d = max;
        if (d < -max) d = -max;
        return cur + d;
    }
}
