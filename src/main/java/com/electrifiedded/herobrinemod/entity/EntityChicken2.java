package com.electrifiedded.herobrinemod.entity;

import com.electrifiedded.archaiclib.legacy.sound.LegacySounds;
import com.electrifiedded.herobrinemod.HerobrineMod;
import net.minecraft.block.Block;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/** Evil chicken: a chicken-shaped spider that leaps at players day and night. */
public class EntityChicken2 extends EntitySpider {
    public float wingRotation, destPos, oFlapSpeed, oFlap, wingRotDelta = 1.0F;

    public EntityChicken2(World world) {
        super(world);
        if (HerobrineMod.miniMode == 1) setSize(0.15F, 0.35F);
        else setSize(0.3F, 0.7F);
    }

    public EntityChicken2(World world, double x, double y, double z) {
        this(world);
        setPosition(x, y, z);
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        targetTasks.taskEntries.clear();
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(0.5D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData data) {
        return data; // no spider jockeys / spider potion effects
    }

    @Override
    public void onLivingUpdate() {
        oFlap = wingRotation;
        oFlapSpeed = destPos;
        destPos = (float) (destPos + (onGround ? -1 : 4) * 0.3D);
        if (destPos < 0.0F) destPos = 0.0F;
        if (destPos > 1.0F) destPos = 1.0F;
        if (!onGround && wingRotDelta < 1.0F) wingRotDelta = 1.0F;
        wingRotDelta = (float) (wingRotDelta * 0.9D);
        if (!onGround && motionY < 0.0D) motionY *= 0.6D;
        wingRotation += wingRotDelta * 2.0F;
        super.onLivingUpdate();
        if (!world.isRemote && ticksExisted % 20 == 0 && getAttackTarget() instanceof EntityPlayer
                && getAttackTarget().getHealth() < 5.0F && rand.nextInt(10) == 0) {
            setDead(); // mercy, as in the original
        }
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return null;
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
        dropItem(Items.ROTTEN_FLESH, 1);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return LegacySounds.get("mob.chicken.say");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return LegacySounds.get("mob.chicken.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return LegacySounds.get("mob.chicken.hurt");
    }

    @Override
    protected void playStepSound(BlockPos pos, Block block) {
        playSound(LegacySounds.get("mob.chicken.step"), 0.15F, 1.0F);
    }
}
