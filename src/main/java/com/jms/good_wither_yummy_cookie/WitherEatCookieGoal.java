package com.jms.good_wither_yummy_cookie;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
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

    public WitherEatCookieGoal(WitherEntity wither) {
        super();

        this.wither = wither;
        this.state = State.NotBegun;
        this.ticksSinceStartedEating = 0;

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
        this.state = State.MovingToCookie;
        this.wither.getNavigation().startMovingTo(getNearbyCookies(7.0).remove(0),
                this.wither.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).getValue());
    }

    @Override
    public void stop() {
        this.state = State.NotBegun;
        this.ticksSinceStartedEating = 0;
        this.wither.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.state == State.MovingToCookie && this.wither.getNavigation().isIdle()) {
            List<ItemEntity> nearby_cookies = getNearbyCookies(1.0);
            if (nearby_cookies.isEmpty()) {
                this.state = State.Finished;
            } else {
                ItemEntity cookie = nearby_cookies.remove(0);

                this.state = State.EatingCookie;
                spawnItemParticles(cookie.getStack(), 16);
                this.wither.playSound(SoundEvents.ENTITY_GENERIC_EAT, 3.0F, 1.0F);

                cookie.kill();

                WitherEntityExtension wither = (WitherEntityExtension) this.wither;
                if (wither.isTamed()) {
                    this.wither.heal(this.wither.getMaximumHealth() / 8.0F);
                } else {
                    wither.incrementFedCookiesForTaming();
                    if (wither.isTamed()) {
                        // TODO: Remove boss bar

                        if (this.wither.world.isClient) {
                            double d = this.wither.getRandom().nextGaussian() * 0.02D;
                            double e = this.wither.getRandom().nextGaussian() * 0.02D;
                            double f = this.wither.getRandom().nextGaussian() * 0.02D;
                            this.wither.world.addParticle(ParticleTypes.HEART, true, this.wither.getParticleX(1.0D),
                                    this.wither.getRandomBodyY() + 0.5D, this.wither.getParticleZ(1.0D), d, e, f);
                        }
                    }
                }
            }
        }

        // TODO: Match eating time to sound/particle length
        else if (this.state == State.EatingCookie) {
            if (this.ticksSinceStartedEating == 20) {
                this.state = State.Finished;
            } else {
                this.ticksSinceStartedEating += 1;
            }
        }
    }

    private List<ItemEntity> getNearbyCookies(double radius) {
        return this.wither.world.getEntities(EntityType.ITEM, new Box(this.wither.getBlockPos()).expand(radius),
                entity -> ((ItemEntity) entity).getStack().getItem() == Items.COOKIE);
    }

    private void spawnItemParticles(ItemStack stack, int count) {
        for (int i = 0; i < count; ++i) {
            Vec3d vec3d = new Vec3d(((double) this.wither.getRandom().nextFloat() - 0.5D) * 0.1D,
                    Math.random() * 0.1D + 0.1D, 0.0D);
            vec3d = vec3d.rotateX(-this.wither.pitch * 0.017453292F);
            vec3d = vec3d.rotateY(-this.wither.yaw * 0.017453292F);
            double d = (double) (-this.wither.getRandom().nextFloat()) * 0.6D - 0.3D;
            Vec3d vec3d2 = new Vec3d(((double) this.wither.getRandom().nextFloat() - 0.5D) * 0.3D, d, 0.6D);
            vec3d2 = vec3d2.rotateX(-this.wither.pitch * 0.017453292F);
            vec3d2 = vec3d2.rotateY(-this.wither.yaw * 0.017453292F);
            vec3d2 = vec3d2.add(this.wither.getX(), this.wither.getEyeY(), this.wither.getZ());
            this.wither.world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), vec3d2.x, vec3d2.y,
                    vec3d2.z, vec3d.x, vec3d.y + 0.05D, vec3d.z);
        }
    }
}
