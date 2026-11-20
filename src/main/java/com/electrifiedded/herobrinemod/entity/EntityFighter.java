package com.electrifiedded.herobrinemod.entity;

import com.electrifiedded.archaiclib.legacy.sound.LegacySounds;
import com.electrifiedded.archaiclib.legacy.util.LegacyChat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/** Herobrine who fights: stares for 2 seconds, then attacks (8 dmg). Sometimes his brother Notch shows up. */
public class EntityFighter extends EntityBaseHMod {
    private int ingame = 0;
    private boolean start = false;
    public int wait = 0;

    public EntityFighter(World world) {
        super(world);
    }

    public EntityFighter(World world, double x, double y, double z, boolean sound) {
        this(world);
        setPosition(x, y, z);
        LegacySounds.playSoundEffect(world, posX + 0.5D, posY + 0.5D, posZ + 0.5D, "ambient.cave.cave", 35.0F, 0.8F);
    }

    @Override
    protected int getAttackStrength() {
        return 8;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
    }

    @Override
    protected Entity findTarget() {
        return start ? super.findTarget() : null;
    }

    @Override
    protected boolean isMovementBlocked() {
        return !start || super.isMovementBlocked();
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.isRemote) return;
        if (rand.nextInt(90) == 0) {
            EntityPlayer p = world.getClosestPlayerToEntity(this, 30.0D);
            if (p != null) setPositionAndUpdate(p.posX, p.posY, p.posZ + 3.0D);
        }
        wait++;
        if (!start) {
            EntityPlayer p = world.getClosestPlayerToEntity(this, 30.0D);
            if (p == null) return;
            faceEntity(p, 30.0F, 30.0F);
        }
        if (wait == 45) {
            start = true;
            EntityPlayer p = world.getClosestPlayerToEntity(this, 30.0D);
            if (p == null) return;
            if (rand.nextInt(8) == 0) {
                world.spawnEntity(new EntityFighter2(world, p.posX + 0.5D, p.posY, p.posZ + 0.5D));
                LegacyChat.broadcast("Herobrine: Hi brother!");
                LegacyChat.broadcast("Notch: I don't want you in my game!");
            }
        }
        if (start) ingame++;
        if (ingame == 800) {
            part();
            setDead();
        }
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
        Item i;
        switch (rand.nextInt(6) + 1) {
            case 1: i = Items.DIAMOND_SWORD; break;
            case 3: i = Items.GOLDEN_APPLE; break;
            case 5: i = Items.CAKE; break;
            case 6: i = Items.COOKIE; break;
            default: i = null;
        }
        if (i != null) dropItem(i, 1);
    }

    @Override
    public boolean canDespawn() {
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!start && source.getTrueSource() instanceof EntityPlayer) start = true;
        return super.attackEntityFrom(source, amount);
    }
}
