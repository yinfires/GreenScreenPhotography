package com.yinfires.greenscreenphotography.mixin;

import com.yinfires.greenscreenphotography.client.FreeCameraController;
import com.yinfires.greenscreenphotography.client.GreenScreenPhotographyClient;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void greenScreenPhotography$handleCameraKeys(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo callbackInfo) {
        Minecraft minecraft = Minecraft.getInstance();
        if (windowPointer != minecraft.getWindow().getWindow() || action == GLFW.GLFW_RELEASE) {
            return;
        }

        if (FreeCameraController.isActive() && GreenScreenPhotographyClient.CAMERA_FREEZE.matches(key, scanCode)) {
            if (action == GLFW.GLFW_PRESS) {
                FreeCameraController.handleFreezeKeyPress();
            }
            callbackInfo.cancel();
            return;
        }

        if (minecraft.screen == null && GreenScreenPhotographyClient.FREE_CAMERA.matches(key, scanCode)) {
            if (action == GLFW.GLFW_PRESS) {
                FreeCameraController.handleFreeCameraKeyPress();
            }
            callbackInfo.cancel();
        }
    }
}
