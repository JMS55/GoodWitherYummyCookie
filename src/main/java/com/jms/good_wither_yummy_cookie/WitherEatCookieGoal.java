package com.jms.good_wither_yummy_cookie;

import java.util.List;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;

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
    }

    @Override
    public boolean canStart() {
        WitherExtended wither = (WitherExtended) this.wither;

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
        this.wither.getNavigation().startMovingTo(getNearbyCookies(7.0).remove(0), 1.0);
    }

    @Override
    public void stop() {
        this.state = State.NotBegun;
        this.ticksSinceStartedEating = 0;
        this.wither.getNavigation().stop();
        super.stop();
    }

    @Override
    public void tick() {
        if (this.state == State.MovingToCookie && this.wither.getNavigation().isIdle()) {
            List<ItemEntity> nearby_cookies = getNearbyCookies(1.0);
            if (nearby_cookies.isEmpty()) {
                this.state = State.Finished;
            } else {
                this.state = State.EatingCookie;
                nearby_cookies.remove(0).kill();
                // TOOD: Show cookie eating particles maybe
                // TODO: Play cookie eating sound for 1s
                // this.wither.world.playSound(x, y, z, sound, soundCategory, f, g, bl);

                WitherExtended wither = (WitherExtended) this.wither;
                if (wither.isTamed()) {
                    this.wither.heal(this.wither.getMaximumHealth() / 8.0f);
                } else {
                    wither.incrementFedCookies();
                    if (wither.isTamed()) {
                        // TODO: Replace goals with tamed ones
                        // TODO: Show hearts around wither
                    }
                }
            }
        }

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
}
