package com.jms.good_wither_yummy_cookie.mixin;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;

@Mixin(TargetPredicate.class)
public interface TargetPredicateMixin {

    @Accessor
    Predicate<LivingEntity> getPredicate();
}
