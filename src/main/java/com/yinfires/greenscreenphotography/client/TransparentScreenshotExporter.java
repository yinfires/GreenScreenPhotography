package com.yinfires.greenscreenphotography.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.yinfires.greenscreenphotography.GreenScreenPhotography;
import com.yinfires.greenscreenphotography.block.GreenScreenPhotographyBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TransparentScreenshotExporter {
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");
    private static final int BLACK_SCREEN_COLOR = 0x000000;
    private static final int WHITE_SCREEN_COLOR = 0xFFFFFF;
    private static final int FULL_ALPHA = 0xFF;
    private static final int SCREEN_COLOR_TOLERANCE = 8;

    private static CaptureState captureState = CaptureState.IDLE;
    private static NativeImage blackSample;

    private TransparentScreenshotExporter() {
    }

    public static Path ensureExportDirectory() {
        Path directory = exportDirectory();
        try {
            Files.createDirectories(directory);
        } catch (IOException exception) {
            GreenScreenPhotography.LOGGER.warn("Failed to create GreenScreenPhotography export directory: {}", directory, exception);
        }
        return directory;
    }

    public static int currentScreenColor() {
        return switch (captureState) {
            case PREPARE_BLACK, BLACK -> BLACK_SCREEN_COLOR;
            case PREPARE_WHITE, WHITE -> WHITE_SCREEN_COLOR;
            case IDLE -> GreenScreenPhotographyBlock.DEFAULT_SCREEN_COLOR;
        };
    }

    public static void requestCapture() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.screen != null || captureState != CaptureState.IDLE) {
            return;
        }
        closeBlackSample();
        captureState = CaptureState.PREPARE_BLACK;
        GreenScreenPhotographyTextureOverride.uploadCurrentColor();
    }

    public static void afterFrame() {
        Minecraft minecraft = Minecraft.getInstance();
        if (captureState == CaptureState.IDLE) {
            return;
        }
        if (minecraft.level == null || minecraft.player == null || minecraft.screen != null) {
            resetCapture();
            return;
        }

        switch (captureState) {
            case PREPARE_BLACK -> captureState = CaptureState.BLACK;
            case BLACK -> {
                blackSample = Screenshot.takeScreenshot(minecraft.getMainRenderTarget());
                captureState = CaptureState.PREPARE_WHITE;
                GreenScreenPhotographyTextureOverride.uploadCurrentColor();
            }
            case PREPARE_WHITE -> captureState = CaptureState.WHITE;
            case WHITE -> {
                try (NativeImage whiteSample = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
                    exportReconstructedImages(minecraft, blackSample, whiteSample);
                } catch (Exception exception) {
                    GreenScreenPhotography.LOGGER.error("Failed to export transparent GreenScreenPhotography screenshot.", exception);
                    minecraft.gui.setOverlayMessage(Component.translatable("message.green_screen_photography.screenshot.failed"), false);
                } finally {
                    resetCapture();
                }
            }
        }
    }

    private static void exportReconstructedImages(Minecraft minecraft, NativeImage blackImage, NativeImage whiteImage) throws IOException {
        if (blackImage == null || blackImage.getWidth() != whiteImage.getWidth() || blackImage.getHeight() != whiteImage.getHeight()) {
            throw new IOException("Screenshot samples do not match.");
        }

        Path directory = ensureExportDirectory();
        String baseName = nextBaseName(directory);
        Path colorFile = directory.resolve(baseName + ".png");
        Path matteFile = directory.resolve(baseName + "_matte.png");

        try (NativeImage transparent = new NativeImage(blackImage.getWidth(), blackImage.getHeight(), true);
             NativeImage matte = new NativeImage(blackImage.getWidth(), blackImage.getHeight(), true)) {
            reconstructImages(blackImage, whiteImage, transparent, matte);
            transparent.writeToFile(colorFile);
            matte.writeToFile(matteFile);
        }
        minecraft.gui.setOverlayMessage(Component.translatable("message.green_screen_photography.screenshot.saved", colorFile.getFileName(), matteFile.getFileName()), false);
    }

    private static void reconstructImages(NativeImage blackImage, NativeImage whiteImage, NativeImage transparent, NativeImage matte) {
        for (int y = 0; y < blackImage.getHeight(); y++) {
            for (int x = 0; x < blackImage.getWidth(); x++) {
                int blackPixel = blackImage.getPixelRGBA(x, y);
                int whitePixel = whiteImage.getPixelRGBA(x, y);
                int blackRed = red(blackPixel);
                int blackGreen = green(blackPixel);
                int blackBlue = blue(blackPixel);
                int whiteRed = red(whitePixel);
                int whiteGreen = green(whitePixel);
                int whiteBlue = blue(whitePixel);

                int reconstructionAlpha = reconstructedAlpha(blackRed, blackGreen, blackBlue, whiteRed, whiteGreen, whiteBlue);
                boolean screenPixel = isScreenPixel(blackRed, blackGreen, blackBlue, whiteRed, whiteGreen, whiteBlue);
                int alpha = screenPixel ? 0 : FULL_ALPHA;
                int red = screenPixel ? 0 : unpremultiply(blackRed, reconstructionAlpha);
                int green = screenPixel ? 0 : unpremultiply(blackGreen, reconstructionAlpha);
                int blue = screenPixel ? 0 : unpremultiply(blackBlue, reconstructionAlpha);

                transparent.setPixelRGBA(x, y, packRgba(red, green, blue, alpha));
                matte.setPixelRGBA(x, y, packRgba(alpha, alpha, alpha, FULL_ALPHA));
            }
        }
    }

    private static int reconstructedAlpha(int blackRed, int blackGreen, int blackBlue, int whiteRed, int whiteGreen, int whiteBlue) {
        int redAlpha = FULL_ALPHA - clamp(whiteRed - blackRed);
        int greenAlpha = FULL_ALPHA - clamp(whiteGreen - blackGreen);
        int blueAlpha = FULL_ALPHA - clamp(whiteBlue - blackBlue);
        return clamp((redAlpha + greenAlpha + blueAlpha) / 3);
    }

    private static boolean isScreenPixel(int blackRed, int blackGreen, int blackBlue, int whiteRed, int whiteGreen, int whiteBlue) {
        return isNearColor(blackRed, blackGreen, blackBlue, BLACK_SCREEN_COLOR)
                && isNearColor(whiteRed, whiteGreen, whiteBlue, WHITE_SCREEN_COLOR);
    }

    private static boolean isNearColor(int red, int green, int blue, int color) {
        int targetRed = color >> 16 & 0xFF;
        int targetGreen = color >> 8 & 0xFF;
        int targetBlue = color & 0xFF;
        return Math.abs(red - targetRed) <= SCREEN_COLOR_TOLERANCE
                && Math.abs(green - targetGreen) <= SCREEN_COLOR_TOLERANCE
                && Math.abs(blue - targetBlue) <= SCREEN_COLOR_TOLERANCE;
    }

    private static int unpremultiply(int premultipliedChannel, int alpha) {
        if (alpha <= 0) {
            return 0;
        }
        return clamp(premultipliedChannel * FULL_ALPHA / alpha);
    }

    private static Path exportDirectory() {
        Minecraft minecraft = Minecraft.getInstance();
        Path gameDirectory = minecraft.gameDirectory.toPath();
        String version = sanitizePathSegment(minecraft.getLaunchedVersion());
        if (gameDirectory.getFileName() != null && gameDirectory.getFileName().toString().equals(version)) {
            return gameDirectory.resolve(GreenScreenPhotography.MOD_ID);
        }
        return gameDirectory.resolve("versions").resolve(version).resolve(GreenScreenPhotography.MOD_ID);
    }

    private static String nextBaseName(Path directory) {
        String timestamp = FILE_TIME.format(LocalDateTime.now());
        String base = "green_screen_photography_" + timestamp;
        int index = 1;
        String candidate = base;
        while (Files.exists(directory.resolve(candidate + ".png")) || Files.exists(directory.resolve(candidate + "_matte.png"))) {
            candidate = base + "_" + index;
            index++;
        }
        return candidate;
    }

    private static String sanitizePathSegment(String value) {
        String sanitized = value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return sanitized.isEmpty() ? "unknown" : sanitized;
    }

    private static int red(int pixel) {
        return pixel & 0xFF;
    }

    private static int green(int pixel) {
        return pixel >> 8 & 0xFF;
    }

    private static int blue(int pixel) {
        return pixel >> 16 & 0xFF;
    }

    private static int packRgba(int red, int green, int blue, int alpha) {
        return red & 0xFF | (green & 0xFF) << 8 | (blue & 0xFF) << 16 | (alpha & 0xFF) << 24;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(FULL_ALPHA, value));
    }

    private static void resetCapture() {
        closeBlackSample();
        captureState = CaptureState.IDLE;
        GreenScreenPhotographyTextureOverride.restoreDefaultColor();
    }

    private static void closeBlackSample() {
        if (blackSample != null) {
            blackSample.close();
            blackSample = null;
        }
    }

    private enum CaptureState {
        IDLE,
        PREPARE_BLACK,
        BLACK,
        PREPARE_WHITE,
        WHITE
    }
}
