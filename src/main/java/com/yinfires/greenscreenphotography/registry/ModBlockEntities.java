package com.yinfires.greenscreenphotography.registry;

import com.yinfires.greenscreenphotography.GreenScreenPhotography;
import com.yinfires.greenscreenphotography.block.entity.GreenScreenPhotographyBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GreenScreenPhotography.MOD_ID);

    public static final Supplier<BlockEntityType<GreenScreenPhotographyBlockEntity>> GREEN_SCREEN_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "green_screen_block",
            () -> BlockEntityType.Builder.of(GreenScreenPhotographyBlockEntity::new, ModBlocks.GREEN_SCREEN_BLOCK.get()).build(null)
    );

    private ModBlockEntities() {
    }
}
