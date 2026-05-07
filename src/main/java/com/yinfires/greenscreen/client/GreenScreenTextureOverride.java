package com.yinfires.greenscreen.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.yinfires.greenscreen.GreenScreen;
import com.yinfires.greenscreen.block.GreenScreenBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

final class GreenScreenTextureOverride {
    private static final ResourceLocation SCREEN_TEXTURE = ResourceLocation.fromNamespaceAndPath(GreenScreen.MOD_ID, "block/green_screen_block");

    private GreenScreenTextureOverride() {
    }

    static void uploadCurrentColor() {
        uploadColor(TransparentScreenshotExporter.currentScreenColor());
    }

    static void restoreDefaultColor() {
        uploadColor(GreenScreenBlock.DEFAULT_SCREEN_COLOR);
    }

    private static void uploadColor(int color) {
        Minecraft minecraft = Minecraft.getInstance();
        TextureAtlas atlas = minecraft.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(SCREEN_TEXTURE);
        int width = sprite.contents().width();
        int height = sprite.contents().height();
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        int rgba = red | green << 8 | blue << 16 | 0xFF000000;

        atlas.bind();
        for (int mipLevel = 0; mipLevel < sprite.contents().byMipLevel.length; mipLevel++) {
            int mipWidth = width >> mipLevel;
            int mipHeight = height >> mipLevel;
            if (mipWidth <= 0 || mipHeight <= 0) {
                break;
            }
            uploadMipColor(sprite, mipLevel, mipWidth, mipHeight, rgba);
        }
    }

    private static void uploadMipColor(TextureAtlasSprite sprite, int mipLevel, int width, int height, int rgba) {
        NativeImage image = new NativeImage(width, height, false);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setPixelRGBA(x, y, rgba);
            }
        }
        image.upload(mipLevel, sprite.getX() >> mipLevel, sprite.getY() >> mipLevel, 0, 0, width, height, false, true);
    }
}
