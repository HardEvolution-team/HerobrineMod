package com.electrifiedded.herobrinemod.entity;

import com.electrifiedded.archaiclib.legacy.sound.LegacySounds;
import com.electrifiedded.herobrinemod.HerobrineMod;
import com.electrifiedded.herobrinemod.HerobrineStalker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/** Chuck Norris: appears, stares, roundhouse-kicks the player and vanishes; vanishes when hit. */
public class EntityNorris extends EntityStalker {
    public EntityNorris(World world) {
        super(world);
    }

    public EntityNorris(World world, double x, double y, double z, boolean chuck) {
        this(world);
        this.norris = chuck;
        setPosition(x, y, z);
        if (rand.nextInt(30) == 0) setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
    }

    @Override
    protected void onAppear(EntityPlayer target, int i, int j, int k) {
        if (rand.nextInt(20) == 0) LegacySounds.playSoundEffect(world, i + 0.5D, j + 0.5D, k + 0.5D, "mob.ghast.scream", 35.0F, 0.8F);
        LegacySounds.playSoundEffect(world, i + 0.5D, j + 0.5D, k + 0.5D, "ambient.cave.cave", 35.0F, 0.8F);
        lifespawn = 30 + rand.nextInt(20) + 1;
        if (rand.nextInt(11) == 0) {
            HerobrineStalker.stopPlayer(target);
            freezePlayer = true;
            setFreezing(true);
            LegacySounds.playSoundEffect(world, i + 0.5D, j + 0.5D, k + 0.5D, "mob.ghast.scream", 35.0F, 0.8F);
            if (rand.nextInt(25) == 0 && HerobrineMod.dropTorch == 1) {
                HerobrineStalker.dropTorches(world, (int) posX, (int) posY, (int) posZ);
            }
        }
    }

    @Override
    public void attack() {
        HerobrineStalker.blastGlowe(this, world);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (world.isRemote || isEntityInvulnerable(source)) return false;
        if (rand.nextInt(10) == 0) HerobrineStalker.throwPlayer2(world, this);
        setDead();
        return true;
    }
}
