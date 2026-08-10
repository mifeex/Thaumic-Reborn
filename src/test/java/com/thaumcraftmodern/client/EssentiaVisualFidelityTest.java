package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EssentiaVisualFidelityTest {
    @Test
    void phialUsesClassicBottleAndAnimatedTintedEssenceLayers()
            throws Exception {
        String model = Files.readString(Path.of(
                "src/main/resources/assets/thaumcraftmodern/models/item/"
                        + "essentia_phial.json"));
        String filledModel = Files.readString(Path.of(
                "src/main/resources/assets/thaumcraftmodern/models/item/"
                        + "essentia_phial_filled.json"));
        String animation = Files.readString(Path.of(
                "src/main/resources/assets/thaumcraftmodern/textures/item/"
                        + "essence.png.mcmeta"));
        String clientRegistration = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/"
                        + "ClientModEvents.java"));

        assertTrue(model.contains("thaumcraftmodern:item/phial"));
        assertFalse(model.contains("\"layer1\""));
        assertTrue(model.contains("thaumcraftmodern:filled"));
        assertTrue(model.contains("essentia_phial_filled"));
        assertTrue(filledModel.contains("thaumcraftmodern:item/phial"));
        assertTrue(filledModel.contains("thaumcraftmodern:item/essence"));
        assertTrue(clientRegistration.contains("ItemProperties.register("));
        assertTrue(clientRegistration.contains(
                "EssentiaPhialItem.aspect(stack).isPresent()"
        ));
        assertTrue(animation.contains("\"animation\""));
    }

    @Test
    void jarLiquidAndGogglesReadoutUseItsSynchronizedAspect() throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "EssentiaJarBlockEntityRenderer.java"));
        String readout = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/"
                        + "ClientAspectContainerReadout.java"));
        String jarBlock = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/"
                        + "EssentiaJarBlock.java"));
        String itemRenderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "WardedJarItemRenderer.java"));
        String itemModel = Files.readString(Path.of(
                "src/main/resources/assets/thaumcraftmodern/models/item/"
                        + "warded_jar.json"));

        assertTrue(renderer.contains("renderLiquid(jar.aspect(), jar.amount()"));
        assertTrue(renderer.contains("AspectRegistryRuntime.find(aspect)"));
        assertTrue(renderer.contains("int red = (color >> 16) & 255"));
        assertTrue(renderer.contains("TextureAtlas.LOCATION_BLOCKS"));
        assertTrue(renderer.contains("sprite.getU0()"));
        assertTrue(renderer.contains("sprite.getU1()"));
        assertTrue(renderer.contains("sprite.getV0()"));
        assertTrue(renderer.contains("sprite.getV1()"));
        assertTrue(renderer.contains(".color(red, green, blue, 255)"));
        String hudRegistry = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/"
                        + "AspectContainerHudRegistry.java"));
        assertTrue(readout.contains("AspectContainerHudRegistry.resolve"));
        assertTrue(hudRegistry.contains("register(EssentiaJarBlockEntity.class"));
        assertTrue(hudRegistry.contains("jar.aspect(), jar.amount()"));
        assertTrue(hudRegistry.contains("CLASSIC_FACE_OFFSET = 0.6D"));
        assertTrue(jarBlock.contains(
                "box(3.5, 0, 3.5, 12.5, 11.5, 12.5)"));
        assertTrue(jarBlock.contains("return SHAPE;"));
        assertTrue(itemModel.contains("minecraft:builtin/entity"));
        assertTrue(itemRenderer.contains("WardedJarItem.contents(stack)"));
        assertTrue(itemRenderer.contains("EssentiaJarBlockEntityRenderer.renderLiquid("));
        assertTrue(itemRenderer.contains("ModBlocks.WARDED_JAR.get().defaultBlockState()"));
    }
}
