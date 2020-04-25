package com.jms.good_wither_yummy_cookie.goal;

import java.util.EnumSet;
import java.util.UUID;

import com.jms.good_wither_yummy_cookie.WitherEntityExtension;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.server.world.ServerWorld;

public class WitherAttackWithOwnerGoal extends TrackTargetGoal {
    private final WitherEntity wither;
    private LivingEntity attacking;
    private int lastAttackTime;

    public WitherAttackWithOwnerGoal(WitherEntity wither) {
        super(wither, false);
        this.wither = wither;
        this.setControls(EnumSet.of(Goal.Control.TARGET));
    }

    public boolean canStart() {
        WitherEntityExtension wither = (WitherEntityExtension) this.wither;
        if (wither.isTamed()) { // && !this.wither.isSitting()) {
            ServerWorld world = (ServerWorld) this.wither.world;
            UUID owner = wither.getOwner();
            if (owner == null) {
                return false;
            }
            LivingEntity livingEntity = world.getPlayerByUuid(owner);
            if (livingEntity == null) {
                return false;
            } else {
                this.attacking = livingEntity.getAttacking();
                int i = livingEntity.getLastAttackTime();
                return i != this.lastAttackTime && this.canTrack(this.attacking, TargetPredicate.DEFAULT);
                // && this.wither.canAttackWithOwner(this.attacking, livingEntity);
            }
        } else {
            return false;
        }
    }

    public void start() {
        this.mob.setTarget(this.attacking);
        WitherEntityExtension wither = (WitherEntityExtension) this.wither;
        ServerWorld world = (ServerWorld) this.wither.world;
        LivingEntity livingEntity = world.getPlayerByUuid(wither.getOwner());
        if (livingEntity != null) {
            this.lastAttackTime = livingEntity.getLastAttackTime();
        }

        super.start();
    }
}
