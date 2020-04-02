package com.jms.good_wither_yummy_cookie;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class WitherEatCookieGoal extends Goal {

    enum State {
        NotBegun, MovingToCookie, EatingCookie, Finished
    }

    private WitherEntity wither;
    private State state;
    private int ticksSinceStartedEating;
    private Random random;

    public WitherEatCookieGoal(WitherEntity wither) {
        super();

        this.wither = wither;
        this.state = State.NotBegun;
        this.ticksSinceStartedEating = 0;
        this.random = wither.getRandom();

        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.JUMP, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        WitherEntityExtension wither = (WitherEntityExtension) this.wither;

        if (wither.isTamed()) {
            return !getNearbyCookies(7.0).isEmpty();
        } else {
            return !getNearbyCookies(7.0).isEmpty() && (wither.getOwner() != null)
                    && (this.wither.getHealth() / this.wither.getMaximumHealth() <= 0.5);
        }
    }

    @Override
    public boolean shouldContinue() {
        return this.state != State.Finished;
    }

    @Override
    public boolean canStop() {
        return this.state == State.Finished;
    }

    @Override
    public void start() {
        GoodWitherYummyCookie.logDebug(
                "WitherEatCookieGoal.start() called for entity " + this.wither.getCustomName().asFormattedString());

        this.state = State.MovingToCookie;
        this.wither.getNavigation().startMovingTo(getNearbyCookies(7.0).remove(0),
                this.wither.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).getValue());
    }

    @Override
    public void stop() {
        GoodWitherYummyCookie.logDebug(
                "WitherEatCookieGoal.stop() called for entity " + this.wither.getCustomName().asFormattedString());

        this.state = State.NotBegun;
        this.ticksSinceStartedEating = 0;
        this.wither.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.state == State.MovingToCookie && this.wither.getNavigation().isIdle()) {
            List<ItemEntity> nearby_cookies = getNearbyCookies(2.0);
            if (nearby_cookies.isEmpty()) {
                GoodWitherYummyCookie.logDebug(this.wither.getCustomName().asFormattedString()
                        + " found no cookie in method WitherEatCookieGoal.tick()");

                this.state = State.Finished;
            } else {
                this.state = State.EatingCookie;
                nearby_cookies.remove(0).kill();
                this.wither.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5F + 0.5F * (float) this.random.nextInt(2),
                        (this.random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
                spawnEatingParticles();

                WitherEntityExtension wither = (WitherEntityExtension) this.wither;
                if (wither.isTamed()) {
                    GoodWitherYummyCookie.logDebug(this.wither.getCustomName().asFormattedString()

                            + " is tamed and healed with a cookie in method WitherEatCookieGoal.tick()");
                    this.wither.heal(this.wither.getMaximumHealth() / 8.0F);
                } else {
                    wither.incrementFedCookiesForTaming();

                    GoodWitherYummyCookie.logDebug(this.wither.getCustomName().asFormattedString()
                            + " was fed a cookie while not tamed, and is now "
                            + (wither.isTamed() ? "tame" : "not tamed") + " in method WitherEatCookieGoal.tick()");

                    if (wither.isTamed()) {
                        if (this.wither.getTarget() != null && this.wither.getTarget().getUuid() == wither.getOwner()) {
                            this.wither.setTarget(null);
                        }
                        if (this.wither.getAttacker() != null
                                && this.wither.getAttacker().getUuid() == wither.getOwner()) {
                            this.wither.setAttacker(null);
                        }
                        for (int i = 0; i < 3; i++) {
                            Entity trackedEntity = this.wither.world.getEntityById(this.wither.getTrackedEntityId(i));
                            if (trackedEntity != null && trackedEntity.getUuid() == wither.getOwner()) {
                                this.wither.setTrackedEntityId(i, 0);
                            }
                        }
                        wither.hideBossBar();
                        spawnTamedParticles();
                    }
                }
            }
        }

        else if (this.state == State.EatingCookie) {
            this.ticksSinceStartedEating += 1;

            GoodWitherYummyCookie.logDebug(this.wither.getCustomName().asFormattedString() + " is eating a cookie with "
                    + (20 - this.ticksSinceStartedEating) + " ticks left in method WitherEatCookieGoal.tick()");

            if (this.ticksSinceStartedEating == 20) {
                this.state = State.Finished;
            }
        }
    }

    private List<ItemEntity> getNearbyCookies(double radius) {
        return this.wither.world.getEntities(EntityType.ITEM, new Box(this.wither.getBlockPos()).expand(radius),
                entity -> ((ItemEntity) entity).getStack().getItem() == Items.COOKIE);
    }

    @Environment(EnvType.CLIENT)
    private void spawnEatingParticles() {
        for (int i = 0; i < 16; ++i) {
            Vec3d vec3d = new Vec3d(((double) this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D,
                    0.0D);
            vec3d = vec3d.rotateX(-this.wither.pitch * 0.017453292F);
            vec3d = vec3d.rotateY(-this.wither.yaw * 0.017453292F);
            double d = (double) (-this.random.nextFloat()) * 0.6D - 0.3D;
            Vec3d vec3d2 = new Vec3d(((double) this.random.nextFloat() - 0.5D) * 0.3D, d, 0.6D);
            vec3d2 = vec3d2.rotateX(-this.wither.pitch * 0.017453292F);
            vec3d2 = vec3d2.rotateY(-this.wither.yaw * 0.017453292F);
            vec3d2 = vec3d2.add(this.wither.getX(), this.wither.getEyeY(), this.wither.getZ());
            this.wither.world.addParticle(
                    new ItemStackParticleEffect(ParticleTypes.ITEM, Items.COOKIE.getStackForRender()), vec3d2.x,
                    vec3d2.y, vec3d2.z, vec3d.x, vec3d.y + 0.05D, vec3d.z);
        }
    }

    @Environment(EnvType.CLIENT)
    private void spawnTamedParticles() {
        for (int i = 0; i < 7; ++i) {
            double d = this.random.nextGaussian() * 0.02D;
            double e = this.random.nextGaussian() * 0.02D;
            double f = this.random.nextGaussian() * 0.02D;
            this.wither.world.addParticle(ParticleTypes.HEART, this.wither.getParticleX(1.0D),
                    this.wither.getRandomBodyY() + 0.5D, this.wither.getParticleZ(1.0D), d, e, f);
        }
    }
}
