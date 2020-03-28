package com.jms.good_wither_yummy_cookie;

import net.minecraft.entity.player.PlayerEntity;

public interface WitherExtended {

    void incrementFedCookies();

    PlayerEntity getOwner();

    void setOwner(PlayerEntity owner);

    boolean isTamed();
}
