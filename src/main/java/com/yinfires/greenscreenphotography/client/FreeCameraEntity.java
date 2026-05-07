package com.yinfires.greenscreenphotography.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

final class FreeCameraEntity extends Entity {
    FreeCameraEntity(Level level, Vec3 eyePosition, float yaw, float pitch) {
        super(EntityType.PLAYER, level);
        this.noPhysics = true;
        this.setCameraEyePosition(eyePosition);
        this.absRotateTo(yaw, pitch);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    public boolean isSpectator() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public float getViewXRot(float partialTick) {
        return this.getXRot();
    }

    @Override
    public float getViewYRot(float partialTick) {
        return this.getYRot();
    }

    Vec3 getCameraEyePosition() {
        return this.position().add(0.0, this.getEyeHeight(), 0.0);
    }

    void setCameraEyePosition(Vec3 eyePosition) {
        Vec3 feetPosition = eyePosition.subtract(0.0, this.getEyeHeight(), 0.0);
        this.absMoveTo(feetPosition.x, feetPosition.y, feetPosition.z);
    }

    void moveCamera(Vec3 movement) {
        if (movement.lengthSqr() <= 1.0E-8) {
            return;
        }
        this.setOldPosAndRot();
        this.setCameraEyePosition(this.getCameraEyePosition().add(movement));
    }

    void turnCamera(double yawDelta, double pitchDelta) {
        float pitch = (float)pitchDelta * 0.15F;
        float yaw = (float)yawDelta * 0.15F;
        this.setXRot(Mth.clamp(this.getXRot() + pitch, -90.0F, 90.0F));
        this.setYRot(this.getYRot() + yaw);
        this.xRotO = Mth.clamp(this.xRotO + pitch, -90.0F, 90.0F);
        this.yRotO += yaw;
    }
}
