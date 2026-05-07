package com.yinfires.greenscreen.mixin;

import com.yinfires.greenscreen.client.FreeCameraController;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void greenscreen$hideHandInFreeCamera(Camera camera, float partialTick, Matrix4f projectionMatrix, CallbackInfo callbackInfo) {
        if (FreeCameraController.isActive()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "pick", at = @At("HEAD"), cancellable = true)
    private void greenscreen$skipPickInFreeCamera(float partialTicks, CallbackInfo callbackInfo) {
        if (FreeCameraController.shouldSuppressHitHighlights()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
    private void greenscreen$skipBlockOutlineInFreeCamera(CallbackInfoReturnable<Boolean> callbackInfo) {
        if (FreeCameraController.shouldSuppressHitHighlights()) {
            callbackInfo.setReturnValue(false);
        }
    }
}
