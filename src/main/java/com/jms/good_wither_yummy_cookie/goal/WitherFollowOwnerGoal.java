package com.jms.good_wither_yummy_cookie.goal;

import java.util.EnumSet;
import java.util.UUID;

import com.jms.good_wither_yummy_cookie.WitherEntityExtension;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class WitherFollowOwnerGoal extends Goal {
    private final WitherEntity wither;
    private LivingEntity owner;
    private final WorldView world;
    private final double speed;
    private final EntityNavigation navigation;
    private int updateCountdownTicks;
    private final float maxDistance;
    private final float minDistance;
    private float oldWaterPathfindingPenalty;
    private final boolean leavesAllowed;

    public WitherFollowOwnerGoal(WitherEntity wither, double speed, float minDistance, float maxDistance,
            boolean leavesAllowed) {
        this.wither = wither;
        this.world = wither.world;
        this.speed = speed;
        this.navigation = wither.getNavigation();
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.leavesAllowed = leavesAllowed;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    public boolean canStart() {
        WitherEntityExtension wither = (WitherEntityExtension) this.wither;
        if (!wither.isTamed()) {
            return false;
        }
        ServerWorld world = (ServerWorld) this.world;
        UUID owner = wither.getOwner();
        if (owner == null) {
            return false;
        }
        LivingEntity livingEntity = world.getPlayerByUuid(owner);
        if (livingEntity == null) {
            return false;
        } else if (livingEntity.isSpectator()) {
            return false;
            // } else if (this.wither.isSitting()) {
            // return false;
        } else if (this.wither.squaredDistanceTo(livingEntity) < (double) (this.minDistance * this.minDistance)) {
            return false;
        } else {
            this.owner = livingEntity;
            return true;
        }
    }

    public boolean shouldContinue() {
        if (this.navigation.isIdle()) {
            return false;
            // } else if (this.wither.isSitting()) {
            // return false;
        } else {
            return this.wither.squaredDistanceTo(this.owner) > (double) (this.maxDistance * this.maxDistance);
        }
    }

    public void start() {
        this.updateCountdownTicks = 0;
        this.oldWaterPathfindingPenalty = this.wither.getPathfindingPenalty(PathNodeType.WATER);
        this.wither.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
    }

    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.wither.setPathfindingPenalty(PathNodeType.WATER, this.oldWaterPathfindingPenalty);
    }

    public void tick() {
        this.wither.getLookControl().lookAt(this.owner, 10.0F, (float) this.wither.getLookPitchSpeed());
        if (--this.updateCountdownTicks <= 0) {
            this.updateCountdownTicks = 10;
            if (!this.wither.isLeashed() && !this.wither.hasVehicle()) {
                if (this.wither.squaredDistanceTo(this.owner) >= 144.0D) {
                    this.tryTeleport();
                } else {
                    this.navigation.startMovingTo(this.owner, this.speed);
                }

            }
        }
    }

    private void tryTeleport() {
        BlockPos blockPos = new BlockPos(this.owner);

        for (int i = 0; i < 10; ++i) {
            int j = this.getRandomInt(-3, 3);
            int k = this.getRandomInt(-1, 1);
            int l = this.getRandomInt(-3, 3);
            boolean bl = this.tryTeleportTo(blockPos.getX() + j, blockPos.getY() + k, blockPos.getZ() + l);
            if (bl) {
                return;
            }
        }

    }

    private boolean tryTeleportTo(int x, int y, int z) {
        if (Math.abs((double) x - this.owner.getX()) < 2.0D && Math.abs((double) z - this.owner.getZ()) < 2.0D) {
            return false;
        } else if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        } else {
            this.wither.refreshPositionAndAngles((double) ((float) x + 0.5F), (double) y, (double) ((float) z + 0.5F),
                    this.wither.yaw, this.wither.pitch);
            this.navigation.stop();
            return true;
        }
    }

    private boolean canTeleportTo(BlockPos pos) {
        PathNodeType pathNodeType = LandPathNodeMaker.getPathNodeType(this.world, pos.getX(), pos.getY(), pos.getZ());
        if (pathNodeType != PathNodeType.WALKABLE) {
            return false;
        } else {
            BlockState blockState = this.world.getBlockState(pos.down());
            if (!this.leavesAllowed && blockState.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockPos = pos.subtract(new BlockPos(this.wither));
                return this.world.doesNotCollide(this.wither, this.wither.getBoundingBox().offset(blockPos));
            }
        }
    }

    private int getRandomInt(int min, int max) {
        return this.wither.getRandom().nextInt(max - min + 1) + min;
    }
}
