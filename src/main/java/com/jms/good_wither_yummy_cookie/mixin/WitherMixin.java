package com.jms.good_wither_yummy_cookie.mixin;

import com.jms.good_wither_yummy_cookie.WitherEatCookieGoal;
import com.jms.good_wither_yummy_cookie.WitherExtended;
import java.util.function.Predicate;
import java.util.Random;
import java.util.UUID;

import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WitherEntity.class)
public class WitherMixin extends HostileEntity implements WitherExtended {

    private int cookiesNeededToTame;
    private int cookiesFedToTame;
    private PlayerEntity owner;

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
    }

    @Inject(method = "writeCustomDataToTag", at = @At("TAIL"))
    private void injectWriteCustomDataToTag(CompoundTag tag, CallbackInfo info) {
        tag.putInt(COOKIES_NEEDED_TO_TAME_TAG, this.cookiesNeededToTame);
        tag.putInt(COOKIES_FED_TO_TAME_TAG, this.cookiesFedToTame);
        tag.putString(OWNER_TAG, this.owner.getUuidAsString());
    }

    @Inject(method = "readCustomDataFromTag", at = @At("TAIL"))
    private void injectReadCustomDataFromTag(CompoundTag tag, CallbackInfo info) {
        this.cookiesNeededToTame = tag.getInt(COOKIES_NEEDED_TO_TAME_TAG);
        this.cookiesFedToTame = tag.getInt(COOKIES_FED_TO_TAME_TAG);
        this.owner = this.world.getPlayerByUuid(UUID.fromString(tag.getString(OWNER_TAG)));
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
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

    public void incrementFedCookies() {
        this.cookiesFedToTame += 1;
    }

    public PlayerEntity getOwner() {
        return this.owner;
    }

    public void setOwner(PlayerEntity owner) {
        this.owner = owner;
    }

    public boolean isTamed() {
        return this.cookiesFedToTame == this.cookiesNeededToTame;
    }
}
