package com.yinfires.greenscreen.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.SmoothDouble;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ClientHooks;

public final class FreeCameraController {
    private static final double NORMAL_SPEED = 12.0;
    private static final double FAST_SPEED = 48.0;
    private static final double MAX_FRAME_SECONDS = 0.1;
    private static final long ENTER_MOVEMENT_GRACE_NANOS = 120_000_000L;

    private static boolean active;
    private static boolean frozen;
    private static boolean previousClientFrozen;
    private static boolean previousServerFrozen;
    private static boolean hadClientTickRate;
    private static boolean hadServerTickRate;
    private static long lastMoveNanos;
    private static long enteredNanos;
    private static Vec3 lockedPlayerPosition;
    private static float lockedPlayerYaw;
    private static float lockedPlayerPitch;
    private static Entity previousCameraEntity;
    private static FreeCameraEntity cameraEntity;

    private FreeCameraController() {
    }

    public static boolean isActive() {
        return active;
    }

    public static boolean shouldFreezeLocalVisuals() {
        return active && frozen;
    }

    public static boolean shouldSuppressHitHighlights() {
        return active;
    }

    public static boolean handleFreeCameraKeyPress() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.level == null || minecraft.player == null) {
            return false;
        }
        if (active) {
            exit(true);
        } else {
            enter();
        }
        return true;
    }

    public static boolean handleFreezeKeyPress() {
        if (!active) {
            return false;
        }
        setFrozen(!frozen, true);
        return true;
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!active) {
            while (GreenScreenClient.FREE_CAMERA.consumeClick()) {
                handleFreeCameraKeyPress();
            }
            return;
        }

        if (minecraft.level == null || minecraft.player == null) {
            exit(false);
            return;
        }

        while (GreenScreenClient.FREE_CAMERA.consumeClick()) {
            exit(true);
            return;
        }
        while (GreenScreenClient.CAMERA_FREEZE.consumeClick()) {
            handleFreezeKeyPress();
        }

        suppressPlayerInteraction(minecraft);
        lockPlayerBody(minecraft);
        if (minecraft.getCameraEntity() != cameraEntity) {
            minecraft.setCameraEntity(cameraEntity);
        }
    }

    public static void onRenderFrame() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!active) {
            return;
        }
        if (minecraft.level == null || minecraft.player == null) {
            exit(false);
            return;
        }

        suppressPlayerInteraction(minecraft);
        lockPlayerBody(minecraft);
        updateMovement(minecraft);
        minecraft.hitResult = null;
        minecraft.crosshairPickEntity = null;
        if (minecraft.getCameraEntity() != cameraEntity) {
            minecraft.setCameraEntity(cameraEntity);
        }
    }

    public static void handleMouseTurn(double movementTime, double accumulatedDX, double accumulatedDY, SmoothDouble smoothTurnX, SmoothDouble smoothTurnY) {
        if (!active || cameraEntity == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        var event = ClientHooks.getTurnPlayerValues(minecraft.options.sensitivity().get(), minecraft.options.smoothCamera);
        double sensitivity = event.getMouseSensitivity() * 0.6F + 0.2F;
        double scaledSensitivity = sensitivity * sensitivity * sensitivity;
        double normalScale = scaledSensitivity * 8.0;
        double yawDelta;
        double pitchDelta;
        if (event.getCinematicCameraEnabled()) {
            yawDelta = smoothTurnX.getNewDeltaValue(accumulatedDX * normalScale, movementTime * normalScale);
            pitchDelta = smoothTurnY.getNewDeltaValue(accumulatedDY * normalScale, movementTime * normalScale);
        } else if (minecraft.options.getCameraType().isFirstPerson() && minecraft.player != null && minecraft.player.isScoping()) {
            smoothTurnX.reset();
            smoothTurnY.reset();
            yawDelta = accumulatedDX * scaledSensitivity;
            pitchDelta = accumulatedDY * scaledSensitivity;
        } else {
            smoothTurnX.reset();
            smoothTurnY.reset();
            yawDelta = accumulatedDX * normalScale;
            pitchDelta = accumulatedDY * normalScale;
        }

        if (minecraft.options.invertYMouse().get()) {
            pitchDelta = -pitchDelta;
        }
        cameraEntity.turnCamera(yawDelta, pitchDelta);
    }

    private static void enter() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        previousCameraEntity = minecraft.getCameraEntity();
        hadClientTickRate = true;
        previousClientFrozen = minecraft.level.tickRateManager().isFrozen();
        hadServerTickRate = minecraft.hasSingleplayerServer() && minecraft.getSingleplayerServer() != null && !minecraft.getSingleplayerServer().isPublished();
        previousServerFrozen = hadServerTickRate && minecraft.getSingleplayerServer().tickRateManager().isFrozen();

        lockedPlayerPosition = minecraft.player.position();
        lockedPlayerYaw = minecraft.player.getYRot();
        lockedPlayerPitch = minecraft.player.getXRot();
        Vec3 eyePosition = minecraft.player.getEyePosition(1.0F);
        cameraEntity = new FreeCameraEntity(minecraft.level, eyePosition, minecraft.player.getYRot(), minecraft.player.getXRot());
        minecraft.setCameraEntity(cameraEntity);
        minecraft.mouseHandler.grabMouse();
        active = true;
        enteredNanos = System.nanoTime();
        lastMoveNanos = System.nanoTime();
        suppressPlayerInteraction(minecraft);
        lockPlayerBody(minecraft);
        setFrozen(true, false);
        minecraft.gui.setOverlayMessage(Component.translatable("message.greenscreen.free_camera.entered"), false);
    }

    private static void exit(boolean showMessage) {
        Minecraft minecraft = Minecraft.getInstance();
        restoreFrozenState(minecraft);
        if (minecraft.player != null) {
            minecraft.setCameraEntity(previousCameraEntity != null ? previousCameraEntity : minecraft.player);
        }
        active = false;
        frozen = false;
        cameraEntity = null;
        previousCameraEntity = null;
        lastMoveNanos = 0L;
        enteredNanos = 0L;
        lockedPlayerPosition = null;
        if (showMessage) {
            minecraft.gui.setOverlayMessage(Component.translatable("message.greenscreen.free_camera.exited"), false);
        }
    }

    private static void restoreFrozenState(Minecraft minecraft) {
        if (hadClientTickRate && minecraft.level != null) {
            minecraft.level.tickRateManager().setFrozen(previousClientFrozen);
        }
        if (hadServerTickRate && minecraft.getSingleplayerServer() != null) {
            minecraft.getSingleplayerServer().tickRateManager().setFrozen(previousServerFrozen);
        }
        hadClientTickRate = false;
        hadServerTickRate = false;
    }

    private static void setFrozen(boolean value, boolean showMessage) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            minecraft.level.tickRateManager().setFrozen(value);
        }
        if (minecraft.hasSingleplayerServer() && minecraft.getSingleplayerServer() != null && !minecraft.getSingleplayerServer().isPublished()) {
            minecraft.getSingleplayerServer().tickRateManager().setFrozen(value);
        }
        frozen = value;
        if (showMessage) {
            minecraft.gui.setOverlayMessage(Component.translatable(value ? "message.greenscreen.world.frozen" : "message.greenscreen.world.running"), false);
        }
    }

    private static void updateMovement(Minecraft minecraft) {
        if (cameraEntity == null) {
            return;
        }

        long now = System.nanoTime();
        double seconds = lastMoveNanos == 0L ? 0.0 : Math.min((now - lastMoveNanos) / 1_000_000_000.0, MAX_FRAME_SECONDS);
        lastMoveNanos = now;
        if (seconds <= 0.0) {
            return;
        }
        if (now - enteredNanos < ENTER_MOVEMENT_GRACE_NANOS) {
            return;
        }

        Options options = minecraft.options;
        double forward = impulse(options.keyUp.isDown(), options.keyDown.isDown());
        double strafe = impulse(options.keyRight.isDown(), options.keyLeft.isDown());
        double vertical = impulse(options.keyJump.isDown(), options.keyShift.isDown());
        if (forward == 0.0 && strafe == 0.0 && vertical == 0.0) {
            return;
        }

        double yawRadians = Math.toRadians(cameraEntity.getYRot());
        Vec3 forwardVector = new Vec3(-Mth.sin((float)yawRadians), 0.0, Mth.cos((float)yawRadians));
        Vec3 rightVector = new Vec3(-Mth.cos((float)yawRadians), 0.0, -Mth.sin((float)yawRadians));
        Vec3 movement = forwardVector.scale(forward).add(rightVector.scale(strafe)).add(0.0, vertical, 0.0);
        if (movement.lengthSqr() > 1.0) {
            movement = movement.normalize();
        }

        double speed = options.keySprint.isDown() ? FAST_SPEED : NORMAL_SPEED;
        cameraEntity.moveCamera(movement.scale(speed * seconds));
    }

    private static void suppressPlayerInteraction(Minecraft minecraft) {
        Options options = minecraft.options;
        if (minecraft.player != null && minecraft.player.isUsingItem() && minecraft.gameMode != null) {
            minecraft.gameMode.releaseUsingItem(minecraft.player);
        }
        options.keyAttack.setDown(false);
        options.keyUse.setDown(false);
        options.keyPickItem.setDown(false);
        while (options.keyAttack.consumeClick()) {
        }
        while (options.keyUse.consumeClick()) {
        }
        while (options.keyPickItem.consumeClick()) {
        }
    }

    private static void lockPlayerBody(Minecraft minecraft) {
        if (minecraft.player == null || lockedPlayerPosition == null) {
            return;
        }

        minecraft.player.setDeltaMovement(Vec3.ZERO);
        minecraft.player.absMoveTo(lockedPlayerPosition.x, lockedPlayerPosition.y, lockedPlayerPosition.z, lockedPlayerYaw, lockedPlayerPitch);
        minecraft.player.setOldPosAndRot();
    }

    private static double impulse(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0;
        }
        return positive ? 1.0 : -1.0;
    }
}
