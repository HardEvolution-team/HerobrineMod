package com.electrifiedded.herobrinemod.entity;

import com.electrifiedded.herobrinemod.HerobrineMod;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.ItemStack;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

/**
 * "HDummy": the natural spawn carrier. When Herobrine is released (totem) it turns into a Stalker or a Builder
 * (sometimes plus a ghost, Chuck Norris, Pigbrine, Cowbrine or an evil chicken) and disappears.
 */
public class EntitySucker extends EntityAnimal {
    public EntitySucker(World world) {
        super(world);
    }

    public EntitySucker(World world, double x, double y, double z) {
        this(world);
        setPosition(x, y, z);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
    }

    @Override
    public void onLivingUpdate() {
        if (world.isRemote) return;
        if (!HerobrineMod.debug && (!HerobrineMod.canSpawn || world.getDifficulty() == EnumDifficulty.PEACEFUL)) {
            setDead();
            return;
        }
        if (HerobrineMod.extremeRare == 1 && rand.nextInt(80) != 0) {
            setDead();
            return;
        }
        double x = posX, y = posY, z = posZ;
        if (rand.nextInt(130) == 0) world.spawnEntity(new EntityHerobrine(world, x, y, z + 3.0D));
        if (rand.nextInt(130) == 0) world.spawnEntity(new EntityNorris(world, x, y, z + 3.0D, true));
        if (rand.nextBoolean()) world.spawnEntity(new EntityStalker(world, x + 0.5D, y, z + 0.5D, false));
        else world.spawnEntity(new EntityBuilder(world, x + 0.5D, y, z + 0.5D));
        if (rand.nextInt(100) == 0) world.spawnEntity(new EntityPig2(world, x + 0.5D, y, z + 0.5D));
        if (rand.nextInt(100) == 0) world.spawnEntity(new EntityCow2(world, x + 0.5D, y, z + 0.5D));
        if (rand.nextInt(100) == 0) world.spawnEntity(new EntityChicken2(world, x + 0.5D, y, z + 0.5D));
        setDead();
    }

    @Override
    public boolean getCanSpawnHere() {
        return true;
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
