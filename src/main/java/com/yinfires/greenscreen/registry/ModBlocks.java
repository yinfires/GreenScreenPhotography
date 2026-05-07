package com.yinfires.greenscreen.registry;

import com.yinfires.greenscreen.GreenScreen;
import com.yinfires.greenscreen.block.GreenScreenBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, GreenScreen.MOD_ID);

    public static final Supplier<Block> GREEN_SCREEN_BLOCK = BLOCKS.register("green_screen_block", () -> new GreenScreenBlock(GreenScreenBlock.createProperties()));

    private ModBlocks() {
    }

    public static ResourceKey<Block> blockKey(String path) {
        return ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(GreenScreen.MOD_ID, path));
    }
}
