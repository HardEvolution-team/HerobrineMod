package com.electrifiedded.herobrinemod.entity;

import com.electrifiedded.archaiclib.legacy.sound.LegacySounds;
import com.electrifiedded.herobrinemod.HerobrineMod;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

/** Cowbrine: a cow that never stops staring at you, and leaves lava where it dies. */
public class EntityCow2 extends EntityAnimal {
    public EntityCow2(World world) {
        super(world);
        if (HerobrineMod.miniMode == 1) setSize(0.45F, 0.65F);
        else setSize(0.9F, 1.3F);
    }

    public EntityCow2(World world, double x, double y, double z) {
        this(world);
        setPosition(x, y, z);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(6, new EntityAIWanderAvoidWater(this, 1.0D));
        tasks.addTask(8, new EntityAILookIdle(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!world.isRemote && ticksExisted == 1 && (HerobrineMod.cowBrine != 1 || world.getDifficulty() == EnumDifficulty.EASY)) {
            setDead();
            return;
        }
        EntityPlayer player = world.getClosestPlayerToEntity(this, 15.0D);
        if (player != null) faceEntity(player, 30.0F, 30.0F);
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (!world.isRemote && HerobrineMod.placeLava == 1) {
            BlockPos p = new BlockPos(this);
            if (world.isAirBlock(p) || world.getBlockState(p).getMaterial().isReplaceable()) world.setBlockState(p, Blocks.FLOWING_LAVA.getDefaultState(), 3);
            if (world.isAirBlock(p.up())) world.setBlockState(p.up(), Blocks.FLOWING_LAVA.getDefaultState(), 3);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return LegacySounds.get("mob.cow");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return LegacySounds.get("mob.cowhurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return LegacySounds.get("mob.cowhurt");
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
    }

    @Override
    protected net.minecraft.util.ResourceLocation getLootTable() {
        return null;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }
}
