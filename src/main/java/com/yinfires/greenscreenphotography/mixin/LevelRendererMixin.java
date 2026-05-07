package com.yinfires.greenscreenphotography.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yinfires.greenscreenphotography.client.FreeCameraController;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.TickRateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Redirect(
            method = "renderLevel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/TickRateManager;isEntityFrozen(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private boolean greenScreenPhotography$treatFreeCameraPlayerAsFrozen(TickRateManager tickRateManager, Entity entity) {
        return tickRateManager.isEntityFrozen(entity) || FreeCameraController.isFrozenLocalPlayer(entity);
    }

    @Redirect(
            method = "renderLevel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V")
    )
    private void greenScreenPhotography$renderFreeCameraPlayerStable(
            LevelRenderer levelRenderer,
            Entity entity,
            double camX,
            double camY,
            double camZ,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource
    ) {
        ((LevelRendererMixin)(Object)levelRenderer).greenScreenPhotography$callRenderEntity(
                entity,
                camX,
                camY,
                camZ,
                FreeCameraController.isFrozenLocalPlayer(entity) ? 1.0F : partialTick,
                poseStack,
                bufferSource
        );
    }

    @Invoker("renderEntity")
    abstract void greenScreenPhotography$callRenderEntity(
            Entity entity,
            double camX,
            double camY,
            double camZ,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource
    );
}
