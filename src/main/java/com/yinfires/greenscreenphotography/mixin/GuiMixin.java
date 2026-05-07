package com.yinfires.greenscreenphotography.mixin;

import com.yinfires.greenscreenphotography.client.FreeCameraController;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "isExperienceBarVisible", at = @At("HEAD"), cancellable = true)
    private void greenScreenPhotography$hideExperienceInFreeCamera(CallbackInfoReturnable<Boolean> callbackInfo) {
        if (FreeCameraController.isActive()) {
            callbackInfo.setReturnValue(false);
        }
    }
}
