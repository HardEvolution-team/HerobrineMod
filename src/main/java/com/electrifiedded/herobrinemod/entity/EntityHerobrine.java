package com.electrifiedded.herobrinemod.entity;

import com.electrifiedded.archaiclib.legacy.sound.LegacySounds;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

/** "HGhost": Herobrine flying around like a ghast and shooting fireballs (only on Easy, as in the original). */
public class EntityHerobrine extends EntityFlying implements IMob {
    public int courseChangeCooldown = 0;
    public double waypointX, waypointY, waypointZ;
    private EntityLivingBase targetedEntity = null;
    private int aggroCooldown = 0;
    public int attackCounter = 0;

    public EntityHerobrine(World world) {
        super(world);
        isImmuneToFire = true;
        experienceValue = 5;
        setSize(0.6F, 1.95F);
    }

    public EntityHerobrine(World world, double x, double y, double z) {
        this(world);
        setPosition(x, y, z);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source)) return false;
        if ("fireball".equals(source.getDamageType()) && source.getTrueSource() instanceof EntityPlayer) {
            super.attackEntityFrom(source, 1000.0F);
            return true;
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected void updateAITasks() {
        if (!world.isRemote && world.getDifficulty() != EnumDifficulty.EASY) {
            setDead();
            return;
        }
        double dx = waypointX - posX, dy = waypointY - posY, dz = waypointZ - posZ;
        double dist = dx * dx + dy * dy + dz * dz;
        if (dist < 1.0D || dist > 3600.0D) {
            waypointX = posX + (rand.nextFloat() * 2.0F - 1.0F) * 16.0F;
            waypointY = posY + (rand.nextFloat() * 2.0F - 1.0F) * 16.0F;
            waypointZ = posZ + (rand.nextFloat() * 2.0F - 1.0F) * 16.0F;
        }
        if (courseChangeCooldown-- <= 0) {
            courseChangeCooldown += rand.nextInt(5) + 2;
            dist = MathHelper.sqrt(dist);
            if (isCourseTraversable(dist)) {
                motionX += dx / dist * 0.1D;
                motionY += dy / dist * 0.1D;
                motionZ += dz / dist * 0.1D;
            } else {
                waypointX = posX;
                waypointY = posY;
                waypointZ = posZ;
            }
        }
        if (targetedEntity != null && targetedEntity.isDead) targetedEntity = null;
        if (targetedEntity == null || aggroCooldown-- <= 0) {
            targetedEntity = world.getNearestAttackablePlayer(this, 100.0D, 100.0D);
            if (targetedEntity != null) aggroCooldown = 20;
        }
        double range = 64.0D;
        if (targetedEntity != null && targetedEntity.getDistanceSq(this) < range * range) {
            double tx = targetedEntity.posX - posX;
            double ty = targetedEntity.getEntityBoundingBox().minY + targetedEntity.height / 2.0F - (posY + height / 2.0F);
            double tz = targetedEntity.posZ - posZ;
            renderYawOffset = rotationYaw = -((float) Math.atan2(tx, tz)) * 180.0F / (float) Math.PI;
            if (canEntityBeSeen(targetedEntity)) {
                if (attackCounter == 10) world.playEvent(null, 1015, getPosition(), 0);
                attackCounter++;
                if (attackCounter == 20) {
                    world.playEvent(null, 1016, getPosition(), 0);
                    EntityLargeFireball ball = new EntityLargeFireball(world, this, tx, ty, tz);
                    Vec3d look = getLook(1.0F);
                    ball.posX = posX + look.x * 4.0D;
                    ball.posY = posY + height / 2.0F + 0.5D;
                    ball.posZ = posZ + look.z * 4.0D;
                    world.spawnEntity(ball);
                    attackCounter = -40;
                }
            } else if (attackCounter > 0) {
                attackCounter--;
            }
        } else {
            renderYawOffset = rotationYaw = -((float) Math.atan2(motionX, motionZ)) * 180.0F / (float) Math.PI;
            if (attackCounter > 0) attackCounter--;
        }
    }

    private boolean isCourseTraversable(double steps) {
        double sx = (waypointX - posX) / steps, sy = (waypointY - posY) / steps, sz = (waypointZ - posZ) / steps;
        AxisAlignedBB box = getEntityBoundingBox();
        for (int i = 1; i < steps; i++) {
            box = box.offset(sx, sy, sz);
            if (!world.getCollisionBoxes(this, box).isEmpty()) return false;
        }
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return LegacySounds.get("mob.ghast.moan");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return LegacySounds.get("mob.ghast.scream");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return LegacySounds.get("mob.ghast.death");
    }

    @Override
    protected float getSoundVolume() {
        return 10.0F;
    }

    @Override
    public boolean getCanSpawnHere() {
        return rand.nextInt(20) == 0 && super.getCanSpawnHere();
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 1;
    }
}
