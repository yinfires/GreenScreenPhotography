package com.yinfires.greenscreenphotography.mixin;

import com.yinfires.greenscreenphotography.client.FreeCameraController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftInteractionMixin {
    @Inject(method = "handleKeybinds", at = @At("HEAD"), cancellable = true)
    private void greenScreenPhotography$blockPlayerInteraction(CallbackInfo callbackInfo) {
        if (FreeCameraController.isActive()) {
            callbackInfo.cancel();
        }
    }
}
