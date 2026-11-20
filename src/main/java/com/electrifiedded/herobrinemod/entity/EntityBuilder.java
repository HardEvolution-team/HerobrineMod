package com.electrifiedded.herobrinemod.entity;

import com.electrifiedded.archaiclib.legacy.sound.LegacySounds;
import com.electrifiedded.herobrinemod.HerobrineBuilder;
import com.electrifiedded.herobrinemod.HerobrineMod;
import com.electrifiedded.herobrinemod.HerobrineStalker;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * Herobrine the builder: wanders around, then places one of the 22 original constructions (pyramids, towers,
 * tunnels...) or one of the 12 traps, leaves signs, glass/redstone trails, cuts leaves or sets TNT/fire.
 */
public class EntityBuilder extends EntityBaseHMod {
    public int lifespawn, livingTicker = 0, startAction, endTicker = 0;
    public boolean caught = false, cutLeaves = false, placeGlass = false, trap = false, buildingLoaded = false,
            placeASign = false, bloody = false;
    private final boolean spawnedBy = true;
    private final HerobrineBuilder build = new HerobrineBuilder();

    public static final String[] PHRASES = {
            "pmc sucks.", "E?", "u mad bro?", "Am I a ghost?", "Arrow in the knee!", "E", "Get lost!", "Chuck",
            "GameChap", "Berty", "EEEEEEEEEEE!", "I like you", "May I kill you?", "Look behind you!", "1+1",
            "Gold apple", "I rock!", "Watch your back!", "I was here", "Herobrine", "I'll get you!", "Find me", "Hi!",
            "Notch", "Buy Minecraft!", "Get me?", "Where are you?", "Chuck Norris!", "Watch out!", "I'm your shadow",
            "It's me Herobrine!", "Im a griefer", "Where is notch?", "Guess who was here", "Keep out", "Hell",
            "Im not dead!", "I'm alive!", "True", "1.1.0", "ASL?", "---"};

    public EntityBuilder(World world) {
        super(world);
        lifespawn = 700 + rand.nextInt(500) + 1;
        startAction = 100 + rand.nextInt(300) + 1;
        if (rand.nextBoolean()) trap = true;
        if (rand.nextInt(10) == 0) placeGlass = true;
        if (rand.nextInt(30) == 0) cutLeaves = true;
        if (rand.nextInt(5) == 0) placeASign = true;
        if (rand.nextInt(20) == 0) bloody = true;
        setHeldItem(EnumHand.MAIN_HAND, new ItemStack(trap ? Items.DIAMOND_PICKAXE : Items.GOLDEN_PICKAXE));
    }

    public EntityBuilder(World world, double x, double y, double z) {
        this(world);
        setPosition(x, y, z);
    }

    @Override
    protected Entity findTarget() {
        return null; // builds, never attacks
    }

    @Override
    public boolean canDespawn() {
        return false;
    }

    @Override
    public void setDead() {
        if (!isDead) part();
        super.setDead();
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.isRemote) return;
        int i = (int) Math.floor(posX), j = (int) Math.floor(posY), k = (int) Math.floor(posZ);
        EntityPlayer player = world.getClosestPlayerToEntity(this, 30.0D);
        if (player != null && canEntityBeSeen(player) && !caught && !HerobrineMod.debug && !spawnedBy) {
            caught = true;
            LegacySounds.playSoundEffect(world, i + 0.5D, j + 0.5D, k + 0.5D, "mob.ghast.scream", 35.0F, 0.8F);
        }
        if (livingTicker > 200 && placeASign) sign();
        if (trap && livingTicker > 400) {
            if (rand.nextInt(70) == 0 && !buildingLoaded && HerobrineMod.placeTnt == 1) {
                set(i, j, k, Blocks.TNT);
                set(i + 1, j, k, Blocks.TNT);
                set(i + 2, j, k, Blocks.TNT);
                set(i + 1, j + 1, k, Blocks.FIRE);
                setDead();
            }
        } else if (rand.nextInt(50) == 0 && !buildingLoaded && HerobrineMod.placeFire == 1 && livingTicker > 400) {
            for (int d = 0; d < 6; d++) set(i + d, j, k, Blocks.FIRE);
            setDead();
        }
        if (startAction <= livingTicker && build()) setDead();
        if (lifespawn <= livingTicker) setDead();
        if (caught && ++endTicker > 100) setDead();
        int r = rand.nextInt(50) + 1;
        if (r < 7 && cutLeaves) HerobrineStalker.cutLeaves(world, i, j, k);
        if (r < 10 && HerobrineMod.placeGlass == 1 && placeGlass) set(i, j - 1, k, Blocks.GLASS);
        if (r < 5 && bloody && !placeGlass && world.getBlockState(new BlockPos(i, j - 1, k)).getBlock() != Blocks.GLASS) {
            BlockPos p = new BlockPos(i, j, k);
            if (world.isAirBlock(p) && Blocks.REDSTONE_WIRE.canPlaceBlockAt(world, p)) world.setBlockState(p, Blocks.REDSTONE_WIRE.getDefaultState(), 3);
        }
        livingTicker++;
    }

    private void set(int x, int y, int z, net.minecraft.block.Block b) {
        world.setBlockState(new BlockPos(x, y, z), b.getDefaultState(), 3);
    }

    public void sign() {
        placeASign = false;
        BlockPos pos = new BlockPos(this);
        if (!world.isAirBlock(pos) || !Blocks.STANDING_SIGN.canPlaceBlockAt(world, pos)) return;
        world.setBlockState(pos, Blocks.STANDING_SIGN.getDefaultState().withProperty(BlockStandingSign.ROTATION, rand.nextInt(16)), 3);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntitySign) {
            ((TileEntitySign) te).signText[0] = new TextComponentString(PHRASES[rand.nextInt(PHRASES.length)]);
            te.markDirty();
            net.minecraft.block.state.IBlockState s = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, s, s, 3);
        }
    }

    public boolean build() {
        int x = (int) Math.floor(posX), y = (int) Math.floor(posY), z = (int) Math.floor(posZ);
        if (!buildingLoaded) {
            boolean ok;
            if (trap) ok = HerobrineMod.placeTraps == 1 && build.load(rand.nextInt(HerobrineMod.traps) + 1 + ".dat", 0);
            else ok = HerobrineMod.placeBuildings == 1 && build.load(rand.nextInt(HerobrineMod.buildings) + 1 + ".dat", 1);
            if (!ok) {
                startAction = Integer.MAX_VALUE; // nothing to build; just roam until lifespawn
                return false;
            }
            if (build.tryToPlace(world, x, y, z)) return true;
            buildingLoaded = true;
            return false;
        }
        return build.tryToPlace(world, x, y, z);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("LivingTicker", livingTicker);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        livingTicker = tag.getInteger("LivingTicker");
    }
}
