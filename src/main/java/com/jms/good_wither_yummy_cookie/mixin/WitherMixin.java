package com.jms.good_wither_yummy_cookie.mixin;

import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

import com.jms.good_wither_yummy_cookie.WitherEatCookieGoal;
import com.jms.good_wither_yummy_cookie.WitherEntityExtension;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.World;

@Mixin(WitherEntity.class)
public abstract class WitherMixin extends HostileEntity implements WitherEntityExtension {

    @Shadow
    @Final
    private ServerBossBar bossBar;

    private int cookiesNeededToTame;
    private int cookiesFedToTame;
    private UUID owner;

    private static final String TAME_INFO_TAG = "TameInfo";
    private static final String COOKIES_NEEDED_TO_TAME_TAG = "CookiesNeededToTame";
    private static final String COOKIES_FED_TO_TAME_TAG = "CookiesFedToTame";
    private static final String OWNER_TAG = "Owner";

    public WitherMixin(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void injectConstructor(CallbackInfo info) {
        Random rng = new Random();
        this.cookiesNeededToTame = rng.nextInt(5) + 3;
        this.cookiesFedToTame = 0;
        this.owner = null;

        if (this.isTamed()) {
            hideBossBar();
        }
    }

    @Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
    private void injectWriteCustomDataToTag(CompoundTag tag, CallbackInfo info) {
        CompoundTag tameInfoTag = new CompoundTag();
        tameInfoTag.putInt(COOKIES_NEEDED_TO_TAME_TAG, this.cookiesNeededToTame);
        tameInfoTag.putInt(COOKIES_FED_TO_TAME_TAG, this.cookiesFedToTame);
        if (this.owner != null) {
            tameInfoTag.putUuid(OWNER_TAG, this.owner);
        }
        tag.put(TAME_INFO_TAG, tameInfoTag);
    }

    @Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
    private void injectReadCustomDataFromTag(CompoundTag tag, CallbackInfo info) {
        CompoundTag tameInfoTag = tag.getCompound(TAME_INFO_TAG);
        this.cookiesNeededToTame = tameInfoTag.getInt(COOKIES_NEEDED_TO_TAME_TAG);
        this.cookiesFedToTame = tameInfoTag.getInt(COOKIES_FED_TO_TAME_TAG);
        if (tameInfoTag.contains(OWNER_TAG)) {
            this.owner = tameInfoTag.getUuid(OWNER_TAG);
        }
    }

    @Inject(method = "initGoals", at = @At("RETURN"))
    private void injectsInitGoals(CallbackInfo info) {
        this.goalSelector.add(1, new WitherEatCookieGoal((WitherEntity) (Object) this));
    }

    @ModifyArg(method = "initGoals", at = @At(value = "INVOKE", target = "net/minecraft/entity/ai/goal/FollowTargetGoal.<init>"), index = 5)
    private Predicate<LivingEntity> modifyFollowTargetGoalPredicate(Predicate<LivingEntity> originalPredicate) {
        Predicate<LivingEntity> targetPlayerPredicate = (livingEntity) -> {
            if (livingEntity instanceof PlayerEntity) {
                return !this.isTamed();
            } else {
                return true;
            }
        };
        return originalPredicate.and(targetPlayerPredicate);
    }

    public void incrementFedCookiesForTaming() {
        this.cookiesFedToTame += 1;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public boolean isTamed() {
        return this.cookiesFedToTame == this.cookiesNeededToTame;
    }

    public void hideBossBar() {
        this.bossBar.setVisible(false);
    }
}
