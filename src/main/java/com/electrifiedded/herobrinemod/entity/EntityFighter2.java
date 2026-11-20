package com.electrifiedded.herobrinemod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

/** Notch: hunts every fighting Herobrine within 100 blocks (10 dmg), leaves in smoke when none remain. */
public class EntityFighter2 extends EntityBaseHMod {
    private int tick = 400;
    public int wait = 0;

    public EntityFighter2(World world) {
        super(world);
    }

    public EntityFighter2(World world, double x, double y, double z) {
        this(world);
        setPosition(x, y, z);
    }

    @Override
    protected int getAttackStrength() {
        return 10;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.33D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(100.0D);
    }

    private List<EntityFighter> fighters(double range) {
        return world.getEntitiesWithinAABB(EntityFighter.class, new AxisAlignedBB(posX - range, posY - range, posZ - range,
                posX + range, posY + range, posZ + range));
    }

    @Override
    protected Entity findTarget() {
        List<EntityFighter> list = fighters(100.0D);
        if (!list.isEmpty()) return list.get(rand.nextInt(list.size()));
        return null;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.isRemote) return;
        if (tick > 400) {
            tick = 0;
            List<EntityFighter> list = fighters(20.0D);
            if (!list.isEmpty()) setAttackTarget(list.get(rand.nextInt(list.size())));
        } else {
            tick++;
        }
        if (getAttackTarget() == null || !getAttackTarget().isEntityAlive()) {
            if (++wait > 400) {
                part();
                setDead();
            }
        }
    }

    @Override
    public boolean canDespawn() {
        return false;
    }
}
