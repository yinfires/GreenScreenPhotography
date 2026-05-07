package com.yinfires.greenscreen.mixin;

import com.yinfires.greenscreen.client.FreeCameraController;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.SmoothDouble;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Shadow
    private SmoothDouble smoothTurnX;

    @Shadow
    private SmoothDouble smoothTurnY;

    @Shadow
    private double accumulatedDX;

    @Shadow
    private double accumulatedDY;

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void greenscreen$turnFreeCamera(double movementTime, CallbackInfo callbackInfo) {
        if (FreeCameraController.isActive()) {
            FreeCameraController.handleMouseTurn(movementTime, this.accumulatedDX, this.accumulatedDY, this.smoothTurnX, this.smoothTurnY);
            callbackInfo.cancel();
        }
    }
}
