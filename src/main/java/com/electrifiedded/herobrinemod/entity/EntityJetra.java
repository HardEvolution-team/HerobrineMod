package com.electrifiedded.herobrinemod.entity;

import com.electrifiedded.archaiclib.legacy.util.LegacyChat;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Jetra: a sad, chatty guy (spawn egg only). */
public class EntityJetra extends EntityAnimal {
    public static final String[] PHRASES = {
            "I feel lonely", "PLEASE!! DON'T HIT ME!", "Why am I here?", "Lol I'm in the herobrine mod.", "It hurts.",
            "I'm useless.", "My mum doesn't love me anymore :(", "I'm smelly -.-", "DafUQ BRO?", "Whuut?",
            "Blablablablabla", "I have no friends.", "My parents don't love me.", "Hi I'm 12.", "Muuuuh!",
            "Wanna be my friend?", "I'm sad :(", "I'm an emo.", "I have no gf.", "Everybody hates me :("};
    private int speakDown = 0;
    private int speakTimer = 50;

    public EntityJetra(World world) {
        super(world);
        setSize(0.6F, 1.95F);
    }

    public EntityJetra(World world, double x, double y, double z) {
        this(world);
        setPosition(x, y, z);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIPanic(this, 1.4D));
        tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(7, new EntityAILookIdle(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.isRemote) return;
        if (speakTimer < ++speakDown) {
            speakDown = 0;
            speakTimer = rand.nextInt(300) + 1;
            if (world.getClosestPlayerToEntity(this, 3000.0D) != null) {
                LegacyChat.broadcast("Jetra says: " + PHRASES[rand.nextInt(PHRASES.length)]);
            }
        }
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
