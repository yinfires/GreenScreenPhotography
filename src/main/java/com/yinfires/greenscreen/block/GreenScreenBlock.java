package com.yinfires.greenscreen.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class GreenScreenBlock extends Block {
    public static final MapCodec<GreenScreenBlock> CODEC = simpleCodec(GreenScreenBlock::new);
    public static final int DEFAULT_SCREEN_COLOR = 0x00FF00;

    public GreenScreenBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_GREEN)
                .sound(net.minecraft.world.level.block.SoundType.WOOL)
                .strength(0.0F)
                .explosionResistance(0.0F)
                .lightLevel(state -> 0)
                .emissiveRendering((state, level, pos) -> true)
                .noOcclusion();
    }
}
