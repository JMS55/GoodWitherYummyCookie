package com.jms.good_wither_yummy_cookie;

import java.util.UUID;

public interface WitherEntityExtension {

    void incrementFedCookiesForTaming();

    UUID getOwner();

    void setOwner(UUID owner);

    boolean isTamed();
}
