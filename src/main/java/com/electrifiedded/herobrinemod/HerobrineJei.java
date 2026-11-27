package com.electrifiedded.herobrinemod;

import com.electrifiedded.archaiclib.jei.ArchaicJei;
import com.electrifiedded.archaiclib.jei.ArchaicJei.InfoEntry;
import com.electrifiedded.archaiclib.jei.ArchaicJei.Layer;
import com.electrifiedded.herobrinemod.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** JEI/HEI pages: the totem shrine and Herobrine's creatures. Safe without JEI. */
public final class HerobrineJei {

    private HerobrineJei() {}

    private static final String P = "herobrinemod.jei.";

    private static List<InfoEntry> shrine() {
        List<InfoEntry> l = new ArrayList<>();
        ItemStack totem = HerobrineMod.totem == null ? ItemStack.EMPTY : new ItemStack(HerobrineMod.totem);
        l.add(new InfoEntry(P + "shrine").entity(EntityFighter::new)
                .layer(new Layer(P + "shrine.side", "f", "n", "t", "g", "g")
                        .where('f', new ItemStack(Items.FLINT_AND_STEEL)).where('n', new ItemStack(Blocks.NETHERRACK))
                        .where('t', totem).where('g', new ItemStack(Blocks.GOLD_BLOCK)))
                .output(totem)
                .lines(P + "shrine.desc"));
        return l;
    }

    private static InfoEntry mob(String name, Function<World, Entity> f) {
        return new InfoEntry("entity.herobrinemod." + name + ".name").entity(f).lines(P + name + ".desc");
    }

    private static List<InfoEntry> mobs() {
        List<InfoEntry> l = new ArrayList<>();
        l.add(mob("HDummy", EntitySucker::new));
        l.add(mob("HerobrineStalker", EntityStalker::new));
        l.add(mob("HerobrineBuilder", EntityBuilder::new));
        l.add(mob("Herobrine", EntityFighter::new));
        l.add(mob("HGhost", EntityHerobrine::new));
        l.add(mob("HerobrineBoss", EntityHerobrineBoss::new));
        l.add(mob("Notch", EntityFighter2::new));
        l.add(mob("ChuckNorris", EntityNorris::new));
        l.add(mob("HerobrineSlave", EntityHVillager::new));
        l.add(mob("Herobrinezombie", EntityZombie2::new));
        l.add(mob("Evilchicken", EntityChicken2::new));
        l.add(mob("Cowbrine", EntityCow2::new));
        l.add(mob("Pigbrine", EntityPig2::new));
        l.add(mob("Jetra", EntityJetra::new));
        return l;
    }

    public static void register() {
        ItemStack totem = HerobrineMod.totem == null ? new ItemStack(Blocks.GOLD_BLOCK) : new ItemStack(HerobrineMod.totem);
        ArchaicJei.registerInfoCategory(new ArchaicJei.InfoCategory("herobrinemod.shrine", "The Herobrine Mod", P + "cat.shrine", totem, HerobrineJei::shrine));
        ArchaicJei.registerInfoCategory(new ArchaicJei.InfoCategory("herobrinemod.mobs", "The Herobrine Mod", P + "cat.mobs", totem, HerobrineJei::mobs));
    }
}
