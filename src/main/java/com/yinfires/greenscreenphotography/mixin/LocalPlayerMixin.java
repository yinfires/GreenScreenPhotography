package com.yinfires.greenscreenphotography.mixin;

import com.yinfires.greenscreenphotography.client.FreeCameraController;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void greenScreenPhotography$freezeLocalPlayerTick(CallbackInfo callbackInfo) {
        if (FreeCameraController.shouldFreezeLocalPlayer()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "aiStep", at = @At("HEAD"), cancellable = true)
    private void greenScreenPhotography$freezeLocalPlayerAiStep(CallbackInfo callbackInfo) {
        if (FreeCameraController.shouldFreezeLocalPlayer()) {
            callbackInfo.cancel();
        }
    }
}
