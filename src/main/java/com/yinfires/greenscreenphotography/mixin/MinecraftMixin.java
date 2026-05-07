package com.yinfires.greenscreenphotography.mixin;

import com.yinfires.greenscreenphotography.client.FreeCameraController;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.BooleanSupplier;

@Mixin(net.minecraft.client.Minecraft.class)
public abstract class MinecraftMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;tick()V"))
    private void greenScreenPhotography$tickGameRenderer(GameRenderer gameRenderer) {
        if (!FreeCameraController.shouldFreezeLocalVisuals()) {
            gameRenderer.tick();
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;tick()V"))
    private void greenScreenPhotography$tickLevelRenderer(LevelRenderer levelRenderer) {
        if (!FreeCameraController.shouldFreezeLocalVisuals()) {
            levelRenderer.tick();
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;tickEntities()V"))
    private void greenScreenPhotography$tickEntities(ClientLevel level) {
        if (!FreeCameraController.shouldFreezeLocalVisuals()) {
            level.tickEntities();
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;tick(Ljava/util/function/BooleanSupplier;)V"))
    private void greenScreenPhotography$tickLevel(ClientLevel level, BooleanSupplier hasTimeLeft) {
        if (!FreeCameraController.shouldFreezeLocalVisuals()) {
            level.tick(hasTimeLeft);
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;animateTick(III)V"))
    private void greenScreenPhotography$animateTick(ClientLevel level, int posX, int posY, int posZ) {
        if (!FreeCameraController.shouldFreezeLocalVisuals()) {
            level.animateTick(posX, posY, posZ);
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;tick()V"))
    private void greenScreenPhotography$tickParticles(ParticleEngine particleEngine) {
        if (!FreeCameraController.shouldFreezeLocalVisuals()) {
            particleEngine.tick();
        }
    }
}
