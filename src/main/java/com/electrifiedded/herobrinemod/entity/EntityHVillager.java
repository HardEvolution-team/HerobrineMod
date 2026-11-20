package com.electrifiedded.herobrinemod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/** A villager infected by Herobrine: hunts the nearest player, falls apart after ~1 minute. */
public class EntityHVillager extends EntityBaseHMod {
    private int ticker = 0;
    private final int lifespawn;

    public EntityHVillager(World world) {
        super(world);
        lifespawn = 700 + rand.nextInt(500) + 1;
    }

    public EntityHVillager(World world, double x, double y, double z) {
        this(world);
        setPosition(x, y, z);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote && lifespawn < ++ticker) setDead();
    }

    @Override
    protected Entity findTarget() {
        return world.getClosestPlayerToEntity(this, 30.0D);
    }
}
