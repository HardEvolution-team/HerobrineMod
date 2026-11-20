package com.electrifiedded.herobrinemod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class CommonProxy {
    public void preInit() {
    }

    /**
     * 1.7 "freeze" effect on the local player (client only): stop, look at Herobrine, portal-nausea overlay.
     * On the server the movement stop is done by the entity itself.
     */
    public void freezeLocal(EntityPlayer player, Entity herobrine) {
    }
}
