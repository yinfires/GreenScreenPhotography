package com.yinfires.greenscreenphotography.registry;

import com.yinfires.greenscreenphotography.GreenScreenPhotography;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, GreenScreenPhotography.MOD_ID);

    public static final Supplier<Item> GREEN_SCREEN_BLOCK_ITEM = ITEMS.register("green_screen_block", () -> new BlockItem(
            ModBlocks.GREEN_SCREEN_BLOCK.get(),
            new Item.Properties()
    ));

    private ModItems() {
    }
}
