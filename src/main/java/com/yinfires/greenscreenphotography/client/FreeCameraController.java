package com.yinfires.greenscreenphotography.client;

import com.yinfires.greenscreenphotography.mixin.WalkAnimationStateAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.SmoothDouble;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
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
    private static boolean previousPlayerNoPhysics;
    private static boolean hadClientTickRate;
    private static boolean hadServerTickRate;
    private static long lastMoveNanos;
    private static long enteredNanos;
    private static boolean ignoreNextMouseTurn;
    private static LocalPlayer frozenPlayer;
    private static PlayerFreezeSnapshot playerFreezeSnapshot;
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

    public static boolean shouldFreezeLocalPlayer() {
        return active && frozen;
    }

    public static boolean shouldSuppressHitHighlights() {
        return active;
    }

    public static boolean isFrozen() {
        return frozen;
    }

    public static Entity getActiveCameraEntity() {
        return active ? cameraEntity : null;
    }

    public static boolean consumeInitialMouseTurnIgnore() {
        if (!ignoreNextMouseTurn) {
            return false;
        }
        ignoreNextMouseTurn = false;
        return true;
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
            while (GreenScreenPhotographyClient.FREE_CAMERA.consumeClick()) {
                handleFreeCameraKeyPress();
            }
            return;
        }

        if (minecraft.level == null || minecraft.player == null) {
            exit(false);
            return;
        }
        if (!isFrozenPlayerValid(minecraft)) {
            exit(false);
            return;
        }

        while (GreenScreenPhotographyClient.FREE_CAMERA.consumeClick()) {
            exit(true);
            return;
        }
        while (GreenScreenPhotographyClient.CAMERA_FREEZE.consumeClick()) {
            handleFreezeKeyPress();
        }

        suppressPlayerInteraction(minecraft);
        if (frozen) {
            suppressPlayerMovementState(minecraft);
            syncFrozenPlayer(minecraft);
        }
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
        if (!isFrozenPlayerValid(minecraft)) {
            exit(false);
            return;
        }

        suppressPlayerInteraction(minecraft);
        if (frozen) {
            suppressPlayerMovementState(minecraft);
            syncFrozenPlayer(minecraft);
        }
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

        playerFreezeSnapshot = PlayerFreezeSnapshot.capture(minecraft.player);
        frozenPlayer = minecraft.player;
        previousPlayerNoPhysics = minecraft.player.noPhysics;
        Vec3 eyePosition = minecraft.player.getEyePosition(1.0F);
        cameraEntity = new FreeCameraEntity(minecraft.level, eyePosition, minecraft.player.getYRot(), minecraft.player.getXRot());
        minecraft.setCameraEntity(cameraEntity);
        minecraft.mouseHandler.grabMouse();
        active = true;
        enteredNanos = System.nanoTime();
        ignoreNextMouseTurn = true;
        lastMoveNanos = System.nanoTime();
        suppressPlayerInteraction(minecraft);
        suppressPlayerMovementState(minecraft);
        syncFrozenPlayer(minecraft);
        setFrozen(true, false);
        minecraft.gui.setOverlayMessage(Component.translatable("message.green_screen_photography.free_camera.entered"), false);
    }

    private static void exit(boolean showMessage) {
        Minecraft minecraft = Minecraft.getInstance();
        restoreFrozenState(minecraft);
        minecraft.hitResult = null;
        minecraft.crosshairPickEntity = null;
        if (minecraft.player != null) {
            minecraft.setCameraEntity(previousCameraEntity != null ? previousCameraEntity : minecraft.player);
        }
        active = false;
        frozen = false;
        cameraEntity = null;
        previousCameraEntity = null;
        lastMoveNanos = 0L;
        enteredNanos = 0L;
        ignoreNextMouseTurn = false;
        frozenPlayer = null;
        playerFreezeSnapshot = null;
        if (minecraft.player != null) {
            minecraft.player.noPhysics = previousPlayerNoPhysics;
        }
        if (showMessage) {
            minecraft.gui.setOverlayMessage(Component.translatable("message.green_screen_photography.free_camera.exited"), false);
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
        boolean wasFrozen = frozen;
        if (minecraft.level != null) {
            minecraft.level.tickRateManager().setFrozen(value);
        }
        if (minecraft.hasSingleplayerServer() && minecraft.getSingleplayerServer() != null && !minecraft.getSingleplayerServer().isPublished()) {
            minecraft.getSingleplayerServer().tickRateManager().setFrozen(value);
        }
        frozen = value;
        if (wasFrozen && !value && minecraft.player != null) {
            minecraft.player.noPhysics = previousPlayerNoPhysics;
            minecraft.player.setOldPosAndRot();
            minecraft.player.xCloakO = minecraft.player.xCloak;
            minecraft.player.yCloakO = minecraft.player.yCloak;
            minecraft.player.zCloakO = minecraft.player.zCloak;
        } else if (!wasFrozen && value && minecraft.player != null) {
            playerFreezeSnapshot = PlayerFreezeSnapshot.capture(minecraft.player);
            syncFrozenPlayer(minecraft);
        }
        if (showMessage) {
            minecraft.gui.setOverlayMessage(Component.translatable(value ? "message.green_screen_photography.world.frozen" : "message.green_screen_photography.world.running"), false);
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

    private static void suppressPlayerMovementState(Minecraft minecraft) {
        if (minecraft.player == null) {
            return;
        }
        Input input = minecraft.player.input;
        input.leftImpulse = 0.0F;
        input.forwardImpulse = 0.0F;
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
        minecraft.player.setDeltaMovement(Vec3.ZERO);
        minecraft.player.walkDist = minecraft.player.walkDistO;
        minecraft.player.bob = minecraft.player.oBob;
    }

    private static void syncFrozenPlayer(Minecraft minecraft) {
        if (minecraft.player == null || playerFreezeSnapshot == null) {
            return;
        }

        playerFreezeSnapshot.apply(minecraft.player);
    }

    public static boolean isFrozenLocalPlayer(Entity entity) {
        return active && frozen && entity == frozenPlayer;
    }

    private static boolean isFrozenPlayerValid(Minecraft minecraft) {
        return frozenPlayer != null
                && minecraft.player == frozenPlayer
                && minecraft.player.isAlive()
                && !minecraft.player.isRemoved()
                && minecraft.player.getHealth() > 0.0F;
    }

    private static double impulse(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0;
        }
        return positive ? 1.0 : -1.0;
    }

    private record PlayerFreezeSnapshot(
            Vec3 position,
            float yRot,
            float xRot,
            float yHeadRot,
            float yBodyRot,
            boolean onGround,
            float walkDist,
            float moveDist,
            float flyDist,
            float bob,
            float xBob,
            float yBob,
            float attackAnim,
            int swingTime,
            boolean swinging,
            InteractionHand swingingArm,
            float walkAnimationSpeed,
            float walkAnimationPosition
    ) {
        private static PlayerFreezeSnapshot capture(LocalPlayer player) {
            return new PlayerFreezeSnapshot(
                    player.position(),
                    player.getYRot(),
                    player.getXRot(),
                    player.getYHeadRot(),
                    player.yBodyRot,
                    player.onGround(),
                    player.walkDist,
                    player.moveDist,
                    player.flyDist,
                    player.bob,
                    player.xBob,
                    player.yBob,
                    player.attackAnim,
                    player.swingTime,
                    player.swinging,
                    player.swingingArm,
                    player.walkAnimation.speed(),
                    player.walkAnimation.position()
            );
        }

        private void apply(LocalPlayer player) {
            player.noPhysics = true;
            player.setDeltaMovement(Vec3.ZERO);
            player.setOnGround(this.onGround);
            player.xxa = 0.0F;
            player.yya = 0.0F;
            player.zza = 0.0F;

            player.absMoveTo(this.position.x, this.position.y, this.position.z, this.yRot, this.xRot);
            player.setOldPosAndRot();
            player.xo = this.position.x;
            player.yo = this.position.y;
            player.zo = this.position.z;
            player.xOld = this.position.x;
            player.yOld = this.position.y;
            player.zOld = this.position.z;

            player.setYHeadRot(this.yHeadRot);
            player.setYBodyRot(this.yBodyRot);
            player.yHeadRotO = this.yHeadRot;
            player.yBodyRotO = this.yBodyRot;
            player.yRotO = this.yRot;
            player.xRotO = this.xRot;

            player.walkDist = this.walkDist;
            player.walkDistO = this.walkDist;
            player.moveDist = this.moveDist;
            player.flyDist = this.flyDist;
            player.oBob = this.bob;
            player.bob = this.bob;
            player.xBobO = this.xBob;
            player.xBob = this.xBob;
            player.yBobO = this.yBob;
            player.yBob = this.yBob;

            player.oAttackAnim = this.attackAnim;
            player.attackAnim = this.attackAnim;
            player.swingTime = this.swingTime;
            player.swinging = this.swinging;
            player.swingingArm = this.swingingArm;

            WalkAnimationStateAccessor walkAnimation = (WalkAnimationStateAccessor)player.walkAnimation;
            walkAnimation.greenScreenPhotography$setSpeedOld(this.walkAnimationSpeed);
            walkAnimation.greenScreenPhotography$setSpeed(this.walkAnimationSpeed);
            walkAnimation.greenScreenPhotography$setPosition(this.walkAnimationPosition);

            player.xCloak = this.position.x;
            player.yCloak = this.position.y;
            player.zCloak = this.position.z;
            player.xCloakO = this.position.x;
            player.yCloakO = this.position.y;
            player.zCloakO = this.position.z;
        }
    }
}
