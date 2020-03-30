package com.jms.good_wither_yummy_cookie;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class GoodWitherYummyCookie implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "goodwitheryummycookie";
    public static final String MOD_NAME = "Good Wither Yummy Cookie";

    public static final WitherWhistle WITHER_WHISTLE = new WitherWhistle(new Item.Settings().group(ItemGroup.MISC));

    @Override
    public void onInitialize() {
        logInfo("Initializing");

        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "wither_whistle"), WITHER_WHISTLE);

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            final ItemStack held_items = player.getStackInHand(hand);
            if (held_items.getItem() == Items.NAME_TAG && held_items.hasCustomName()) {
                if (entity.getType() == EntityType.WITHER) {
                    WitherEntityExtension wither = (WitherEntityExtension) entity;
                    if (!wither.isTamed()) {
                        // TODO: Check to make sure player does not already have a tamed wither

                        logDebug(player.getEntityName() + " has marked " + held_items.getName().asFormattedString()
                                + " for taming");

                        wither.setOwner(player.getUuid());
                        held_items.useOnEntity(player, (LivingEntity) entity, hand);

                        return ActionResult.SUCCESS;
                    }
                }
            }
            held_items.useOnEntity(player, (LivingEntity) entity, hand);
            return ActionResult.PASS;
        });
    }

    public static void logInfo(String message) {
        log(Level.INFO, message);
    }

    public static void logDebug(String message) {
        log(Level.DEBUG, message);
    }

    private static void log(Level level, String message) {
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }
}
