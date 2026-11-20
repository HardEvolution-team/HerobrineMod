package com.electrifiedded.herobrinemod.entity;

import com.electrifiedded.archaiclib.legacy.sound.LegacySounds;
import com.electrifiedded.herobrinemod.HerobrineMod;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

/** Pigbrine: a hostile pig that leaves primed TNT when it dies. */
public class EntityPig2 extends EntityBaseHMod {
    public EntityPig2(World world) {
        super(world);
        if (HerobrineMod.miniMode == 1) setSize(0.45F, 0.45F);
        else setSize(0.9F, 0.9F);
    }

    public EntityPig2(World world, double x, double y, double z) {
        this(world);
        setPosition(x, y, z);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!world.isRemote && ticksExisted == 1 && HerobrineMod.heroSwine != 1) setDead();
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (!world.isRemote && HerobrineMod.placeTnt == 1) {
            world.spawnEntity(new EntityTNTPrimed(world, posX, posY, posZ, null));
        }
    }

    @Override
    public void onStruckByLightning(EntityLightningBolt bolt) {
        if (world.isRemote || isDead) return;
        EntityPigZombie z = new EntityPigZombie(world);
        z.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
        world.spawnEntity(z);
        setDead();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return LegacySounds.get("mob.pig");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return LegacySounds.get("mob.pig");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return LegacySounds.get("mob.pigdeath");
    }
}
