package com.electrifiedded.herobrinemod;

import com.electrifiedded.archaiclib.legacy.util.LegacyChat;
import com.electrifiedded.herobrinemod.entity.*;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/** Herobrine's pranks. All server side. */
public final class HerobrineStalker {
    private static final Random RAND = new com.electrifiedded.archaiclib.util.FastRandom();

    public static final String[] CHRISTMAS = {
            "Damn! Build me a christmastree!", "Im satan santa!", "Hi!", "I got a present for you!", "HoHoHo!",
            "Merry Christmas!", "Where is my christmastree??!", "I am a bad santa!", "Hi Bertie! I got a present for you!",
            "Hi GameChap!", "come closer", "Don't be scared", "Already got an arrow in your knee?"};
    public static final String[] PHRASES = {
            "yag si ten.tfarcenimtenalp", "skcus arteJ", "!yag era sresu tfarcenimtenalp", "Don't use planetminecraft.com",
            "planetminecraft.com sucks", "3=+1+1", "140%!", "tihs si efil naisa ton mi kcuF=SLANIF", "E", "EEEEEEEEEEEEEE!",
            "Hi GameChap!", "Hi Berty!", "?!!!erehw si hctoN", "nevaeh ot yawriats", "!lleh ot emoclew", "!kcik esuohdnouR",
            "!enirboreH ma I", "Hi!", "?tey deracs", "ten.tfarcenimtenalp no erog tsoP", "!deirram si hctoN", "!kcab ruoy hctaw",
            "ypeerC", "!olleh yaS", "?hctoN ees uoy diD", "!yalp annaw tsuj I", "!emag a yalp s'teL", "?em ekil uoy oD",
            "?mA I erehW", "?I mA ohW", "!evarg ym dniF", "!dneirf ruoy mI", "!uoy ees ot eciN", "?uoy era ohW",
            "?edih uoy od yhW", "!uoy teg lliw I", "!sdnomaid tnaw I", "?em ees uoy naC", "!nileppeZ deL ot netsiL"};

    private HerobrineStalker() {}

    private static String phrase() {
        return PHRASES[RAND.nextInt(PHRASES.length)];
    }

    private static EntityPlayer nearest(World world, Entity e, double range) {
        return world.getClosestPlayerToEntity(e, range);
    }

    /** Server half of the 1.7 freeze: stop the player. */
    public static void stopPlayer(EntityPlayer p) {
        p.motionX = 0.0D;
        p.motionY = 0.0D;
        p.motionZ = 0.0D;
        p.velocityChanged = true;
    }

    public static void virus(EntityLiving herobrine, World world) {
        if (world.isRemote) return;
        double x = herobrine.posX, y = herobrine.posY, z = herobrine.posZ;
        List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(x - 150, y - 150, z - 150, x + 150, y + 150, z + 150));
        for (Entity e : list) {
            Entity sp = null;
            if (e instanceof EntityPig) sp = new EntityPig2(world, e.posX, e.posY, e.posZ);
            else if (e instanceof EntityChicken) sp = new EntityChicken2(world, e.posX, e.posY, e.posZ);
            else if (e instanceof EntityCow) sp = new EntityCow2(world, e.posX, e.posY, e.posZ);
            else if (e instanceof EntityVillager) sp = new EntityHVillager(world, e.posX, e.posY, e.posZ);
            if (sp != null) {
                e.setDead();
                world.spawnEntity(sp);
            }
        }
    }

    /** Roundhouse kick. */
    public static void blastGlowe(EntityLiving herobrine, World world) {
        if (HerobrineMod.kick != 1 || world.isRemote) return;
        EntityPlayer p = nearest(world, herobrine, 3000.0D);
        if (p == null) return;
        p.motionZ += 0.5D;
        p.motionX += 0.8D;
        p.motionY += 0.5D;
        p.velocityChanged = true;
        LegacyChat.broadcast("kcik-esuohdnuoR");
    }

    public static void lightning(World world, EntityLiving herobrine) {
        EntityPlayer p = nearest(world, herobrine, 300.0D);
        if (p == null || world.isRemote) return;
        LegacyChat.broadcast(phrase());
        world.addWeatherEffect(new EntityLightningBolt(world, p.posX, p.posY + 2.0D, p.posZ, false));
    }

    /** Breaks (drops) every torch within 100 blocks horizontally, +-10 vertically. */
    public static void dropTorches(World world, int i, int j, int k) {
        if (world.isRemote) return;
        k -= 2;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int l = -100; l < 100; l++)
            for (int j1 = -100; j1 < 100; j1++) {
                p.setPos(i + l + 1, j, k + j1 + 1);
                if (!world.isBlockLoaded(p)) continue;
                for (int i1 = -10; i1 < 10; i1++) {
                    p.setPos(i + l + 1, j + i1, k + j1 + 1);
                    if (world.getBlockState(p).getBlock() == Blocks.TORCH) world.destroyBlock(p.toImmutable(), true);
                }
            }
    }

    public static void cutLeaves(World world, int i, int j, int k) {
        k -= 2;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int l = -20; l < 20; l++)
            for (int i1 = -10; i1 < 10; i1++)
                for (int j1 = -20; j1 < 20; j1++) {
                    p.setPos(i + l + 1, j + i1, k + j1 + 1);
                    if (world.isBlockLoaded(p) && world.getBlockState(p).getBlock() instanceof BlockLeaves) world.setBlockToAir(p);
                }
    }

    public static void breakGlass(World world, int i, int j, int k) {
        k -= 2;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int l = -20; l < 20; l++)
            for (int i1 = -10; i1 < 10; i1++)
                for (int j1 = -20; j1 < 20; j1++) {
                    p.setPos(i + l + 1, j + i1, k + j1 + 1);
                    if (world.isBlockLoaded(p) && world.getBlockState(p).getBlock() == Blocks.GLASS && RAND.nextInt(4) == 0) world.setBlockToAir(p);
                }
    }

    public static void onFire(World world, EntityLiving herobrine) {
        if (HerobrineMod.setOnFire != 1 || RAND.nextInt(7) != 0) return;
        EntityPlayer p = nearest(world, herobrine, 3000.0D);
        if (p == null) return;
        p.setFire(10);
        LegacyChat.broadcast(phrase());
        LegacyChat.broadcast("Hi " + p.getDisplayNameString());
        p.inventory.addItemStackToInventory(new ItemStack(Items.BUCKET, 1));
    }

    public static void throwPlayer2(World world, EntityLiving herobrine) {
        EntityPlayer p = nearest(world, herobrine, 3000.0D);
        if (p == null) return;
        p.motionY += 0.9988889F;
        p.velocityChanged = true;
        LegacyChat.broadcast(phrase());
    }

    public static void throwPlayer(World world, EntityLiving herobrine) {
        EntityPlayer p = nearest(world, herobrine, 3000.0D);
        if (p == null) return;
        p.motionY += 0.9988889F;
        p.velocityChanged = true;
    }

    public static void boss(World world, int i, int j, int k) {
        EntityHerobrineBoss boss = new EntityHerobrineBoss(world);
        boss.setPosition(i + 0.5D, j + 1.0D, k + 0.5D);
        world.spawnEntity(boss);
    }

    public static void zombie(World world, int i, int j, int k, EntityLiving herobrine) {
        world.spawnEntity(new EntityZombie2(world, i + 0.5D, j + 1.0D, k + 0.5D));
        world.spawnEntity(new EntityZombie2(world, i - 0.5D, j - 1.0D, k - 0.5D));
        if (nearest(world, herobrine, 3000.0D) != null) LegacyChat.broadcast(phrase());
    }

    public static void hVillager(World world, int i, int j, int k, EntityLiving herobrine) {
        world.spawnEntity(new EntityHVillager(world, i + 0.5D, j + 1.0D, k + 0.5D));
        if (nearest(world, herobrine, 3000.0D) != null) LegacyChat.broadcast(phrase());
    }

    public static void chicken(World world, int i, int j, int k, EntityLiving herobrine) {
        LegacyChat.broadcast(phrase());
        for (int x = 0; x < 10; x++) world.spawnEntity(new EntityChicken2(world, i + 0.5D * x, j + 1.0D, k + 0.5D * x));
        if (RAND.nextInt(7) == 0) world.spawnEntity(new EntityPig2(world, i + 0.5D, j + 1.0D, k + 0.5D));
    }

    public static void potion(World world, EntityLiving herobrine) {
        EntityPlayer p = nearest(world, herobrine, 3000.0D);
        if (p == null) return;
        Potion[] all = {MobEffects.POISON, MobEffects.MINING_FATIGUE, MobEffects.NAUSEA, MobEffects.BLINDNESS,
                MobEffects.SLOWNESS, MobEffects.NIGHT_VISION, MobEffects.INSTANT_DAMAGE};
        p.addPotionEffect(new PotionEffect(all[RAND.nextInt(all.length)], 140, 0));
    }

    public static void dropItems(World world, EntityLiving herobrine) {
        if (HerobrineMod.dropper == 0) return;
        EntityPlayer p = nearest(world, herobrine, 3000.0D);
        if (p == null) return;
        LegacyChat.broadcast(phrase());
        p.inventory.dropAllItems();
    }

    private static void fireCircle(World world, double cx, double cy, double cz, int radius) {
        for (int e = 0; e <= 360; e++) {
            BlockPos p = new BlockPos((int) (cx + radius * Math.cos(e)), (int) cy, (int) (cz + radius * Math.sin(e)));
            if (world.isAirBlock(p) && !world.isAirBlock(p.down())) {
                IBlockState fire = Blocks.FIRE.getDefaultState();
                world.setBlockState(p, fire, 3);
            }
        }
    }

    public static void ringOfFire(int radius, World world, EntityLiving herobrine) {
        if (HerobrineMod.placeFire == 0) return;
        EntityPlayer p = nearest(world, herobrine, 3000.0D);
        if (p == null) return;
        fireCircle(world, p.posX, p.posY, p.posZ, radius);
        LegacyChat.broadcast(phrase());
        LegacyChat.broadcast("Hi " + p.getDisplayNameString());
    }

    public static void ringOfFireH(int radius, World world, EntityLiving herobrine) {
        if (HerobrineMod.placeFire == 0) return;
        fireCircle(world, herobrine.posX, herobrine.posY, herobrine.posZ, radius);
    }

    public static void ringOfEnemies(int radius, World world, EntityLiving herobrine, int enemy) {
        if (HerobrineMod.animalCircle == 0) return;
        EntityPlayer p = nearest(world, herobrine, 3000.0D);
        if (p == null) return;
        int cooldown = 0;
        for (int e = 0; e <= 360; e++) {
            if (30 < ++cooldown) {
                double x = p.posX + radius * Math.cos(e), z = p.posZ + radius * Math.sin(e), y = p.posY + 1.0D;
                Entity sp = enemy == 0 ? new EntityChicken2(world, x, y, z) : enemy == 1 ? new EntityPig2(world, x, y, z) : new EntityHVillager(world, x, y, z);
                world.spawnEntity(sp);
                cooldown = 0;
            }
        }
    }

    public static void placeWater(World world, int i, int j, int k) {
        if (HerobrineMod.placeWater != 1 || RAND.nextInt(10) != 0) return;
        for (int y = 0; y < 2; y++)
            for (int x = 0; x < 3; x++) world.setBlockState(new BlockPos(i + x, j + y, k), Blocks.WATER.getDefaultState(), 3);
    }

    public static void placeLava(World world, int i, int j, int k) {
        if (HerobrineMod.placeLava != 1 || RAND.nextInt(10) != 0) return;
        for (int x = 0; x < 3; x++) world.setBlockState(new BlockPos(i + x, j, k), Blocks.LAVA.getDefaultState(), 3);
    }
}
