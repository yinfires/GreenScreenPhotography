package com.yinfires.greenscreen.block.entity;

import com.yinfires.greenscreen.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GreenScreenBlockEntity extends BlockEntity {
    public GreenScreenBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.GREEN_SCREEN_BLOCK_ENTITY.get(), pos, blockState);
    }
}
