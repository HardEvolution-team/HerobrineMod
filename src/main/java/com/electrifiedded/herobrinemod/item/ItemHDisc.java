package com.electrifiedded.herobrinemod.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemRecord;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Music disc whose tooltip / "now playing" text is a fixed string (as in the original). */
public class ItemHDisc extends ItemRecord {
    public final String rname;
    private final String screenName;

    public ItemHDisc(String name, String screenName, SoundEvent sound) {
        super(name, sound);
        this.rname = name;
        this.screenName = screenName;
        setCreativeTab(CreativeTabs.MISC);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getRecordNameLocal() {
        return screenName;
    }
}
