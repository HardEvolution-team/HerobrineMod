package com.electrifiedded.herobrinemod.entity;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.world.World;

/** Herobrine's fire-proof zombie servant. */
public class EntityZombie2 extends EntityZombie {
    public EntityZombie2(World world) {
        super(world);
        isImmuneToFire = true;
    }

    public EntityZombie2(World world, double x, double y, double z) {
        this(world);
        setPosition(x, y, z);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.15D);
    }

    @Override
    public int getTotalArmorValue() {
        return 2;
    }

    @Override
    protected boolean shouldBurnInDay() {
        return false;
    }
}
