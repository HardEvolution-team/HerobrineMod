package com.electrifiedded.herobrinemod.entity;

import com.electrifiedded.archaiclib.legacy.world.LegacyParticles;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/**
 * 1.7 base of the hostile "animals" (Herobrine, Notch, Pigbrine, infected villager...): an EntityAnimal that
 * hunts the nearest visible player (old non-AI findPlayerToAttack + attackEntity), rebuilt on 1.12 AI tasks.
 */
public class EntityBaseHMod extends EntityAnimal {
    public EntityBaseHMod(World world) {
        super(world);
        setSize(0.6F, 1.95F);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
        tasks.addTask(5, new EntityAIWanderAvoidWater(this, 0.8D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(7, new EntityAILookIdle(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(0.5D);
        // 1.7 non-AI speeds (0.9) were not attribute based; 0.3 is the 1.12 equivalent pace
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(getAttackStrength());
    }

    /** 1.7 getAttackStrength(Entity). */
    protected int getAttackStrength() {
        return 2;
    }

    /** 1.7 findPlayerToAttack(): nearest visible player within 16 blocks. Null = no target. */
    protected Entity findTarget() {
        EntityPlayer p = world.getClosestPlayerToEntity(this, 16.0D);
        return p != null && canEntityBeSeen(p) ? p : null;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!world.isRemote && ticksExisted % 10 == 0) {
            EntityLivingBase cur = getAttackTarget();
            if (cur != null && rand.nextInt(10) == 0) {
                setAttackTarget(null); // 1.7: 1% per tick to lose interest
            } else if (cur == null || !cur.isEntityAlive() || cur instanceof EntityPlayer && ((EntityPlayer) cur).capabilities.disableDamage) {
                Entity t = findTarget();
                if (t instanceof EntityPlayer && ((EntityPlayer) t).capabilities.disableDamage) t = null;
                setAttackTarget(t instanceof EntityLivingBase ? (EntityLivingBase) t : null);
            }
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this),
                (float) getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
        if (hit) applyEnchantments(this, target);
        return hit;
    }

    /** 1.7 smoke puff used when Herobrine vanishes. Works from the server (sent to clients). */
    public void part() {
        for (int i = 0; i < 20; i++) {
            double d = rand.nextGaussian() * 0.02D, d1 = rand.nextGaussian() * 0.02D, d2 = rand.nextGaussian() * 0.02D, d3 = 10.0D;
            LegacyParticles.spawnBroadcast(world, "largesmoke",
                    posX + rand.nextFloat() * width * 2.0F - width - d * d3,
                    posY + rand.nextFloat() * height - d1 * d3,
                    posZ + rand.nextFloat() * width * 2.0F - width - d2 * d3, d, d1, d2);
        }
    }

    @Override
    public void setInWeb() {
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    public boolean isPotionApplicable(PotionEffect effect) {
        return effect.getPotion() != MobEffects.POISON && super.isPotionApplicable(effect);
    }

    @Override
    public boolean getCanSpawnHere() {
        return true;
    }

    @Override
    public boolean isBreedingItem(net.minecraft.item.ItemStack stack) {
        return false;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }
}
