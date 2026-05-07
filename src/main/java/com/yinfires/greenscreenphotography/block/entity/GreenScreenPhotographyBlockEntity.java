package com.yinfires.greenscreenphotography.block.entity;

import com.yinfires.greenscreenphotography.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GreenScreenPhotographyBlockEntity extends BlockEntity {
    public GreenScreenPhotographyBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.GREEN_SCREEN_BLOCK_ENTITY.get(), pos, blockState);
    }
}
