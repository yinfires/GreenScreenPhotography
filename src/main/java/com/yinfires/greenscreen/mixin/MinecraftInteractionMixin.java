package com.yinfires.greenscreen.mixin;

import com.yinfires.greenscreen.client.FreeCameraController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftInteractionMixin {
    @Inject(method = "handleKeybinds", at = @At("HEAD"), cancellable = true)
    private void greenscreen$blockPlayerInteraction(CallbackInfo callbackInfo) {
        if (FreeCameraController.isActive()) {
            callbackInfo.cancel();
        }
    }
}
