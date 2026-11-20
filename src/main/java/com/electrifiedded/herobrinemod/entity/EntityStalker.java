package com.electrifiedded.herobrinemod.entity;

import com.electrifiedded.archaiclib.legacy.sound.LegacySounds;
import com.electrifiedded.herobrinemod.HerobrineMod;
import com.electrifiedded.herobrinemod.HerobrineStalker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

/**
 * Herobrine the stalker: appears next to the nearest player (sometimes pulls the player to him), stares,
 * freezes them, then plays one random prank (fire ring, lightning, zombies, lava, potion, boss...) and vanishes.
 */
public class EntityStalker extends EntityBaseHMod {
    protected boolean norris = false;
    protected boolean hasAppeared = false;
    protected boolean freezePlayer = false;
    protected boolean ceased = false;
    protected int livingTicker = 0;
    protected int lifespawn = 0;

    public EntityStalker(World world) {
        super(world);
    }

    public EntityStalker(World world, double x, double y, double z, boolean chuck) {
        this(world);
        this.norris = chuck;
        setPosition(x, y, z);
        if (rand.nextInt(30) == 0) {
            setHeldItem(EnumHand.MAIN_HAND, new ItemStack(rand.nextBoolean() ? Items.DIAMOND_SWORD : Items.GOLDEN_SWORD));
        }
    }

    @Override
    protected void initEntityAI() {
        // stands and stares; everything else is scripted in onLivingUpdate
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
    }

    @Override
    protected Entity findTarget() {
        return null;
    }

    @Override
    public void setDead() {
        if (!isDead) part();
        super.setDead();
    }

    @Override
    protected boolean isMovementBlocked() {
        return ceased || super.isMovementBlocked();
    }

    @Override
    public boolean canDespawn() {
        return false;
    }

    @Override
    public void onLivingUpdate() {
        EntityPlayer stalk = world.getClosestPlayerToEntity(this, 300.0D);
        if (stalk != null) faceEntity(stalk, 30.0F, 30.0F);
        ceased = true;
        if (world.isRemote) {
            super.onLivingUpdate();
            if (freezePlayer && stalk != null) HerobrineMod.proxy.freezeLocal(stalk, this);
            return;
        }
        if (hasAppeared) super.onLivingUpdate();
        int i = (int) posX, j = (int) posY, k = (int) posZ;
        if (!hasAppeared) {
            EntityPlayer target = world.getClosestPlayerToEntity(this, 3000.0D);
            if (target == null) return;
            if (HerobrineMod.teleport == 1 && rand.nextInt(70) == 0) {
                target.setPositionAndUpdate(posX + 1.0D, posY, posZ);
                freezePlayer = true;
                setFreezing(true);
            } else {
                setPosition(target.posX + 1.0D, target.posY, target.posZ);
            }
            super.onLivingUpdate();
            hasAppeared = true;
            onAppear(target, i, j, k);
        } else {
            livingTicker++;
        }
        if (freezePlayer && stalk != null) HerobrineStalker.stopPlayer(stalk);
        if (lifespawn < livingTicker) {
            if (stalk != null && rand.nextInt(15) == 0 && HerobrineMod.attac == 1) {
                world.spawnEntity(new EntityFighter(world, stalk.posX + 0.5D, stalk.posY, stalk.posZ + 0.5D, false));
                setDead();
            }
            attack();
            setDead();
        }
    }

    protected void onAppear(EntityPlayer target, int i, int j, int k) {
        int s = rand.nextInt(20) + 1;
        if (s == 5) HerobrineStalker.ringOfFireH(rand.nextInt(5) + 3, world, this);
        if (rand.nextInt(30) == 0) HerobrineStalker.virus(this, world);
        if (s == 10) LegacySounds.playSoundEffect(world, i + 0.5D, j + 0.5D, k + 0.5D, "mob.ghast.scream", 35.0F, 0.8F);
        LegacySounds.playSoundEffect(world, i + 0.5D, j + 0.5D, k + 0.5D, "ambient.cave.cave", 35.0F, 0.8F);
        lifespawn = 30 + rand.nextInt(20) + 1;
        if (rand.nextInt(20) == 0) {
            HerobrineStalker.stopPlayer(target);
            freezePlayer = true;
            setFreezing(true);
            LegacySounds.playSoundEffect(world, i + 0.5D, j + 0.5D, k + 0.5D, "mob.ghast.scream", 35.0F, 0.8F);
        }
        if (rand.nextInt(25) == 0 && HerobrineMod.dropTorch == 1) {
            HerobrineStalker.dropTorches(world, (int) posX, (int) posY, (int) posZ);
        }
    }

    /** Tells the client copy to run the freeze effect on its local player. */
    protected void setFreezing(boolean f) {
        world.setEntityState(this, (byte) (f ? 60 : 61));
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 60) freezePlayer = true;
        else if (id == 61) freezePlayer = false;
        else super.handleStatusUpdate(id);
    }

    public void attack() {
        if (norris) {
            HerobrineStalker.blastGlowe(this, world);
            return;
        }
        int x = (int) posX, y = (int) posY, z = (int) posZ;
        switch (rand.nextInt(30) + 1) {
            case 1: HerobrineStalker.zombie(world, x, y, z, this); break;
            case 2: HerobrineStalker.chicken(world, x, y, z, this); break;
            case 3: HerobrineStalker.blastGlowe(this, world); break;
            case 4: HerobrineStalker.lightning(world, this); break;
            case 5: HerobrineStalker.dropItems(world, this); break;
            case 6:
            case 8: HerobrineStalker.onFire(world, this); break;
            case 7: HerobrineStalker.throwPlayer(world, this); break;
            case 9: HerobrineStalker.placeWater(world, x, y, z); break;
            case 10: HerobrineStalker.placeLava(world, x, y, z); break;
            case 12: HerobrineStalker.breakGlass(world, x, y, z); break;
            case 13: HerobrineStalker.hVillager(world, x, y, z, this); break;
            case 14: HerobrineStalker.ringOfFire(rand.nextInt(12) + 5, world, this); break;
            case 15: HerobrineStalker.ringOfEnemies(rand.nextInt(2) + 5, world, this, 0); break;
            case 16: HerobrineStalker.ringOfEnemies(rand.nextInt(2) + 4, world, this, 1); break;
            case 17: HerobrineStalker.ringOfEnemies(rand.nextInt(2) + 4, world, this, 2); break;
            case 18: HerobrineStalker.virus(this, world); break;
            case 19: HerobrineStalker.potion(world, this); break;
            default: break;
        }
        if (rand.nextInt(250) == 0) HerobrineStalker.boss(world, x, y, z);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!world.isRemote && rand.nextInt(5) == 0) HerobrineStalker.throwPlayer2(world, this);
        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }
}
