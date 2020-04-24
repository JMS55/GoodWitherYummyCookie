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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
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

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("RETURN"))
    private void injectConstructor(CallbackInfo info) {
        Random random = new Random();
        this.cookiesNeededToTame = random.nextInt(5) + 3;
        this.cookiesFedToTame = 0;
        this.owner = null;
    }

    @Inject(method = "writeCustomDataToTag(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void injectWriteCustomDataToTag(CompoundTag tag, CallbackInfo info) {
        CompoundTag tameInfoTag = new CompoundTag();
        tameInfoTag.putInt(COOKIES_NEEDED_TO_TAME_TAG, this.cookiesNeededToTame);
        tameInfoTag.putInt(COOKIES_FED_TO_TAME_TAG, this.cookiesFedToTame);
        if (this.owner != null) {
            tameInfoTag.putUuid(OWNER_TAG, this.owner);
        }
        tag.put(TAME_INFO_TAG, tameInfoTag);
    }

    @Inject(method = "readCustomDataFromTag(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void injectReadCustomDataFromTag(CompoundTag tag, CallbackInfo info) {
        if (tag.contains(TAME_INFO_TAG)) {
            CompoundTag tameInfoTag = tag.getCompound(TAME_INFO_TAG);
            this.cookiesNeededToTame = tameInfoTag.getInt(COOKIES_NEEDED_TO_TAME_TAG);
            this.cookiesFedToTame = tameInfoTag.getInt(COOKIES_FED_TO_TAME_TAG);
            if (tameInfoTag.containsUuid(OWNER_TAG)) {
                this.owner = tameInfoTag.getUuid(OWNER_TAG);
            }

            if (this.isTamed()) {
                hideBossBar();
            }
        }
    }

    @Inject(method = "initGoals()V", at = @At("RETURN"))
    private void injectInitGoals(CallbackInfo info) {
        this.goalSelector.add(1, new WitherEatCookieGoal((WitherEntity) (Object) this));
    }

    @ModifyArg(method = "initGoals()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/FollowTargetGoal;<init>(Lnet/minecraft/entity/mob/MobEntity;Ljava/lang/Class;IZZLjava/util/function/Predicate;)V"), index = 5)
    private Predicate<LivingEntity> injectCanAttackPredicate(Predicate<LivingEntity> originalPredicate) {
        return originalPredicate.and(livingEntity -> !livingEntity.getUuid().equals(this.owner));
    }

    @ModifyArg(method = "mobTick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTargets(Ljava/lang/Class;Lnet/minecraft/entity/ai/TargetPredicate;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/math/Box;)Ljava/util/List;"), index = 1)
    private TargetPredicate injectHeadTargetPredicate(TargetPredicate originalPredicate) {
        return originalPredicate.setPredicate(((TargetPredicateMixin) originalPredicate).getPredicate()
                .and(livingEntity -> livingEntity.getUuid().equals(this.owner)));
    }

    @Redirect(method = "mobTick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getDifficulty()Lnet/minecraft/world/Difficulty;"))
    private Difficulty injectShootRandomSkulls(World world) {
        return this.isTamed() ? null : world.getDifficulty();
    }

    @Override
    public void setAttacker(LivingEntity attacker) {
        if (attacker == null || !attacker.getUuid().equals(this.owner)) {
            super.setAttacker(attacker);
        }
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
