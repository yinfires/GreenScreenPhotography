package com.yinfires.greenscreenphotography.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.yinfires.greenscreenphotography.GreenScreenPhotography;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = GreenScreenPhotography.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public final class GreenScreenPhotographyClient {
    public static final KeyMapping FREE_CAMERA = new KeyMapping(
            "key.green_screen_photography.free_camera",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            "key.categories.green_screen_photography"
    );
    public static final KeyMapping CAMERA_FREEZE = new KeyMapping(
            "key.green_screen_photography.camera_freeze",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.green_screen_photography"
    );
    public static final KeyMapping TRANSPARENT_SCREENSHOT = new KeyMapping(
            "key.green_screen_photography.transparent_screenshot",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.green_screen_photography"
    );

    private GreenScreenPhotographyClient() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(FREE_CAMERA);
        event.register(CAMERA_FREEZE);
        event.register(TRANSPARENT_SCREENSHOT);
        TransparentScreenshotExporter.ensureExportDirectory();
    }

    @EventBusSubscriber(modid = GreenScreenPhotography.MOD_ID, value = Dist.CLIENT)
    public static final class ClientEvents {
        private ClientEvents() {
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            FreeCameraController.tick();
            while (TRANSPARENT_SCREENSHOT.consumeClick()) {
                TransparentScreenshotExporter.requestCapture();
            }
        }

        @SubscribeEvent
        public static void onRenderFrame(RenderFrameEvent.Pre event) {
            FreeCameraController.onRenderFrame();
        }

        @SubscribeEvent
        public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
            if (FreeCameraController.shouldFreezeLocalPlayer()) {
                Input input = event.getInput();
                input.leftImpulse = 0.0F;
                input.forwardImpulse = 0.0F;
                input.up = false;
                input.down = false;
                input.left = false;
                input.right = false;
                input.jumping = false;
                input.shiftKeyDown = false;
            }
        }

        @SubscribeEvent
        public static void onMouseButton(InputEvent.MouseButton.Pre event) {
            if (FreeCameraController.isActive() && Minecraft.getInstance().screen == null) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void afterFrame(RenderFrameEvent.Post event) {
            TransparentScreenshotExporter.afterFrame();
        }
    }
}
