package com.yinfires.greenscreenphotography.mixin;

import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WalkAnimationState.class)
public interface WalkAnimationStateAccessor {
    @Accessor("speedOld")
    void greenScreenPhotography$setSpeedOld(float speedOld);

    @Accessor("speed")
    void greenScreenPhotography$setSpeed(float speed);

    @Accessor("position")
    void greenScreenPhotography$setPosition(float position);
}
