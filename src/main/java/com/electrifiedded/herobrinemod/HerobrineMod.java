package com.electrifiedded.herobrinemod;

import com.electrifiedded.archaiclib.legacy.registry.LegacyGameRegistry;
import com.electrifiedded.archaiclib.legacy.sound.LegacyModSounds;
import com.electrifiedded.archaiclib.legacy.world.LegacySpawns;
import com.electrifiedded.archaiclib.original.OriginalModAssets;
import com.electrifiedded.archaiclib.v1710.api.item.ArchaicItem;
import com.electrifiedded.herobrinemod.block.BlockHerobrine;
import com.electrifiedded.herobrinemod.entity.*;
import com.electrifiedded.herobrinemod.item.ItemHDisc;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, dependencies = "required-after:archaiclib@[1.11.0,)")
public class HerobrineMod {
    public static final Logger LOG = LogManager.getLogger(Reference.MOD_ID);

    /** The original jar this port takes its textures, sounds, texts and building files from (never class-loaded). */
    public static final String ORIGINAL_NAME = "herobrinemod1.7.10.jar - The Herobrine Mod 3.7 for 1.7.10 (by Burnner)";
    public static final String ORIGINAL_URL = "https://www.minecraftforum.net/ (search: The Herobrine Mod by Burnner, 1.7.10)";

    public static boolean debug = false;
    public static int buildings = 22;
    public static int traps = 12;
    /** Herobrine is "released" (totem shrine active, no_totem mode, prank mode or debug). Global, like the original. */
    public static boolean canSpawn = false;
    public static int attac = 1;
    public static int[] spawn = new int[3];
    public static int extremeRare, heroSwine, cowBrine, placeFire, placeTnt, placeLava, placeBuildings, placeTraps,
            placeGlass, placeWater, setOnFire, dropTorch, prank, dropper, kick, miniMode, noTotem, animalCircle;
    public static int teleport = 1;

    public static Block totem;
    public static Item itsHerobrine, seeHerobrine;

    @Mod.Instance(Reference.MOD_ID)
    public static HerobrineMod instance;

    @SidedProxy(clientSide = "com.electrifiedded.herobrinemod.client.ClientProxy", serverSide = "com.electrifiedded.herobrinemod.CommonProxy")
    public static CommonProxy proxy;

    private int nextEntityId = 0;

    public HerobrineMod() {
        MinecraftForge.EVENT_BUS.register(this);
        // the original's sounds.json names files without a domain (1.12 would look in minecraft:); ours is the fixed copy
        OriginalModAssets.hide(Reference.MOD_ID, "sounds.json");
        OriginalModAssets.hide(Reference.MOD_ID, "minecraft/sounds.json");
    }

    public static SoundEvent sound(String legacyName) {
        return LegacyModSounds.get(Reference.MOD_ID, legacyName);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OriginalModAssets.require(Reference.MOD_ID, Reference.MOD_ID, ORIGINAL_NAME, ORIGINAL_URL, Reference.MOD_ID);
        loadConfig(new Configuration(event.getSuggestedConfigurationFile()));
        LegacyModSounds.autoRegister(Reference.MOD_ID);
        registerEntities();
        proxy.preInit();
    }

    private void loadConfig(Configuration c) {
        try {
            c.load();
            animalCircle = c.get("general", "spawn_enemy_circles", 1).getInt();
            spawn[0] = c.get("general", "spawn", 30).getInt();
            spawn[1] = c.get("general", "max_spawn", 7).getInt();
            spawn[2] = c.get("general", "min_spawn", 2, "Not higher than max_spawn").getInt();
            prank = c.get("general", "prank", 0, "Wanna troll your friends? There you go. (no totem block, Herobrine is always free)").getInt();
            heroSwine = c.get("general", "pigbrine", 1).getInt();
            cowBrine = c.get("general", "cowbrine", 1).getInt();
            placeFire = c.get("general", "place_fire", 1).getInt();
            placeLava = c.get("general", "place_lava", 1).getInt();
            placeBuildings = c.get("general", "place_buildings", 1).getInt();
            placeTraps = c.get("general", "place_traps", 1).getInt();
            placeGlass = c.get("general", "place_glass", 1).getInt();
            placeTnt = c.get("general", "place_tnt", 1).getInt();
            placeWater = c.get("general", "place_water", 1).getInt();
            setOnFire = c.get("general", "set_player_on_fire", 1).getInt();
            dropTorch = c.get("general", "drop_torches", 1).getInt();
            dropper = c.get("general", "drop_player_items_on_floor", 1).getInt();
            kick = c.get("general", "Roundhousekick", 1).getInt();
            extremeRare = c.get("general", "extremraremode", 0).getInt();
            miniMode = c.get("general", "mini_mode", 0, "1 = all Herobrine mod mobs are half size").getInt();
            noTotem = c.get("general", "no_totem_mode", 0, "1 = Herobrine spawns without the totem shrine").getInt();
            debug = c.get("general", "debug_mode", 0).getInt() == 1;
        } catch (Exception e) {
            LOG.error("Cannot read config", e);
        } finally {
            if (c.hasChanged()) c.save();
        }
    }

    private void registerEntities() {
        entity(EntityHerobrine.class, "HGhost");
        entity(EntityStalker.class, "HerobrineStalker");
        entity(EntityBuilder.class, "HerobrineBuilder");
        entity(EntityNorris.class, "ChuckNorris");
        entity(EntityFighter.class, "Herobrine");
        entity(EntityFighter2.class, "Notch");
        entity(EntityHVillager.class, "HerobrineSlave");
        entity(EntityChicken2.class, "Evilchicken");
        entity(EntitySucker.class, "HDummy");
        entity(EntityCow2.class, "Cowbrine");
        entity(EntityPig2.class, "Pigbrine");
        entity(EntityHerobrineBoss.class, "HerobrineBoss");
        entity(EntityJetra.class, "Jetra");
        entity(EntityZombie2.class, "Herobrinezombie");
    }

    /** Same egg colours as the original: seeded from the name. */
    private void entity(Class<? extends Entity> cls, String name) {
        Random rand = new Random(name.hashCode());
        int primary = rand.nextInt() * 16777215;
        int secondary = rand.nextInt() * 16777215;
        if (prank == 0) LegacyGameRegistry.registerModEntity(cls, name, nextEntityId++, this, 64, 1, true, primary, secondary);
        else LegacyGameRegistry.registerModEntity(cls, name, nextEntityId++, this, 64, 1, true);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        canSpawn = noTotem == 1 || prank == 1 || debug;
        List<Biome> overworld = LegacySpawns.biomes(LegacySpawns::isOverworld);
        LegacySpawns.add(EntityChicken2.class, 1, 1, 2, EnumCreatureType.CREATURE, overworld);
        LegacySpawns.add(EntityCow2.class, 2, 1, 4, EnumCreatureType.CREATURE, overworld);
        LegacySpawns.add(EntityPig2.class, 1, 1, 1, EnumCreatureType.CREATURE, overworld);
        if (spawn[1] < spawn[2]) {
            LOG.warn("HerobrineMod: WARNING. Wrong max_spawn (lower than min_spawn). Fix it, just do it!");
            LegacySpawns.add(EntitySucker.class, 2, 1, 2, EnumCreatureType.CREATURE, overworld);
        } else {
            LegacySpawns.add(EntitySucker.class, 10, spawn[2], spawn[1], EnumCreatureType.CREATURE, overworld);
        }
    }

    @SubscribeEvent
    public void onBlocks(RegistryEvent.Register<Block> e) {
        if (prank != 0) return;
        totem = new BlockHerobrine().setRegistryName(Reference.MOD_ID, "totem");
        e.getRegistry().register(totem);
    }

    @SubscribeEvent
    public void onItems(RegistryEvent.Register<Item> e) {
        itsHerobrine = disc("seeherobrine", "gothtigger92 - Have You Seen the Herobrine", "record_seeherobrine");
        seeHerobrine = disc("itsherobrine", "Bla Bla Gaming - It's Herobrine", "record_ctune");
        if (totem != null) {
            Item ib = new ItemBlock(totem).setRegistryName(totem.getRegistryName());
            e.getRegistry().register(ib);
            LegacyGameRegistry.registerItemModel(ib, 0, Reference.MOD_ID + ":totem", "inventory");
        }
    }

    private static Item disc(String name, String screenName, String texture) {
        Item item = new ItemHDisc(name, screenName, sound(name)).setTranslationKey(name);
        item.setRegistryName(new ResourceLocation(Reference.MOD_ID, name));
        LegacyGameRegistry.registerItem(item, name);
        ArchaicItem.registerMetaTexture(item, 0, texture);
        return item;
    }

    @SubscribeEvent
    public void onRecipes(RegistryEvent.Register<IRecipe> e) {
        if (totem == null) return;
        IRecipe r = new ShapedOreRecipe(null, new ItemStack(totem), "###", "#X#", "###", 'X', Blocks.SOUL_SAND, '#', Items.BONE);
        r.setRegistryName(new ResourceLocation(Reference.MOD_ID, "totem"));
        e.getRegistry().register(r);
    }
}
