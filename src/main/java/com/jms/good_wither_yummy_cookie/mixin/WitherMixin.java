package com.jms.good_wither_yummy_cookie.mixin;

import com.jms.good_wither_yummy_cookie.WitherEatCookieGoal;
import com.jms.good_wither_yummy_cookie.WitherExtended;
import java.util.Random;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WitherEntity.class)
public class WitherMixin extends MobEntity implements WitherExtended {

    private int cookiesNeededToTame;
    private int cookiesFedToTame;
    private PlayerEntity owner;

    public WitherMixin(EntityType<? extends MobEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void injectWitherConstructor(CallbackInfo info) {
        Random rng = new Random();
        this.cookiesNeededToTame = rng.nextInt(5) + 3;
        this.cookiesFedToTame = 0;
        this.owner = null;
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void injectWitherGoals(CallbackInfo info) {
        this.goalSelector.add(1, new WitherEatCookieGoal((WitherEntity) (Object) this));
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
