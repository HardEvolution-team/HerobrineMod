package com.electrifiedded.herobrinemod.block;

import com.electrifiedded.archaiclib.legacy.sound.LegacySounds;
import com.electrifiedded.archaiclib.legacy.util.LegacyChat;
import com.electrifiedded.herobrinemod.HerobrineMod;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.util.Random;

/**
 * The Herobrine totem. Shrine (bottom to top): gold block, gold block, totem, netherrack, fire.
 * A complete shrine releases Herobrine (lightning + "You don't know what you did...."); breaking it or the
 * shrine binds him again. Texture state: ACTIVE = glowing sides.
 */
public class BlockHerobrine extends Block {
    public static final PropertyBool ACTIVE = PropertyBool.create("active");
    private static boolean texuse = false;

    public BlockHerobrine() {
        super(Material.ROCK);
        setHardness(3.0F);
        setResistance(20.0F);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setTranslationKey("totem");
        setDefaultState(blockState.getBaseState().withProperty(ACTIVE, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ACTIVE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ACTIVE, (meta & 1) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ACTIVE) ? 1 : 0;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    public int tickRate(World world) {
        return 2;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        world.scheduleUpdate(pos, this, tickRate(world));
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos from) {
        world.scheduleUpdate(pos, this, tickRate(world));
    }

    @Override
    public void onPlayerDestroy(World world, BlockPos pos, IBlockState state) {
        HerobrineMod.canSpawn = HerobrineMod.noTotem == 1 || HerobrineMod.debug;
        texuse = false;
        if (!world.isRemote) LegacyChat.broadcast("You wll never be free....");
    }

    @Override
    public void onExplosionDestroy(World world, BlockPos pos, Explosion explosion) {
        HerobrineMod.canSpawn = HerobrineMod.noTotem == 1 || HerobrineMod.debug;
        texuse = false;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (world.isRemote) return;
        boolean shrine = world.getBlockState(pos.up(2)).getBlock() == Blocks.FIRE
                && world.getBlockState(pos.up()).getBlock() == Blocks.NETHERRACK
                && world.getBlockState(pos.down()).getBlock() == Blocks.GOLD_BLOCK
                && world.getBlockState(pos.down(2)).getBlock() == Blocks.GOLD_BLOCK;
        if (shrine) {
            if (!HerobrineMod.canSpawn || !texuse || !state.getValue(ACTIVE)) {
                texuse = true;
                HerobrineMod.canSpawn = true;
                world.setBlockState(pos, state.withProperty(ACTIVE, true), 3);
                world.scheduleUpdate(pos, this, 10);
                LegacyChat.broadcast("You don't know what you did....");
                EntityLightningBolt bolt = new EntityLightningBolt(world, pos.getX(), pos.getY() + 2, pos.getZ(), false);
                world.addWeatherEffect(bolt);
                LegacySounds.playSoundEffect(world, bolt.posX, bolt.posY, bolt.posZ, "ambient.weather.thunder", 10000.0F, 1.8F);
                LegacySounds.playSoundEffect(world, bolt.posX, bolt.posY, bolt.posZ, "random.explode", 2.0F, 2.0F);
            }
        } else {
            unset(world, pos, state);
        }
    }

    private void unset(World world, BlockPos pos, IBlockState state) {
        if (HerobrineMod.canSpawn || texuse || state.getValue(ACTIVE)) {
            texuse = false;
            HerobrineMod.canSpawn = HerobrineMod.noTotem == 1 || HerobrineMod.prank == 1 || HerobrineMod.debug;
            if (state.getValue(ACTIVE)) world.setBlockState(pos, state.withProperty(ACTIVE, false), 3);
            world.scheduleUpdate(pos, this, 10);
        }
    }
}
