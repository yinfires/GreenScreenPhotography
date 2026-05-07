package com.yinfires.greenscreenphotography;

import com.mojang.logging.LogUtils;
import com.yinfires.greenscreenphotography.registry.ModBlockEntities;
import com.yinfires.greenscreenphotography.registry.ModBlocks;
import com.yinfires.greenscreenphotography.registry.ModCreativeTabs;
import com.yinfires.greenscreenphotography.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(GreenScreenPhotography.MOD_ID)
public class GreenScreenPhotography {
    public static final String MOD_ID = "green_screen_photography";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GreenScreenPhotography(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("GreenScreenPhotography initialized.");
    }
}
