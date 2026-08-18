package com.thaumcraftmodern.golemancy;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thaumcraftmodern.entity.GolemCoreType;
import com.thaumcraftmodern.entity.GolemUpgradeType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import org.junit.jupiter.api.Test;

final class GolemCoreAndUpgradeFidelityTest {
    private static final Path RESOURCES = Path.of("src/main/resources");
    private static final Path ORIGINAL = Path.of(
            "reference/Thaumcraft-4.2-FOREVA-master/thaumcraft_src/assets/thaumcraft/textures/items");
    private static final Path ORIGINAL_GUI = Path.of(
            "reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/assets/thaumcraft/textures/gui/guigolem.png");

    @Test void allTwelveLegacyCoreIdsAreStable() {
        assertEquals(12, GolemCoreType.values().length);
        for (int id = 0; id < 12; id++) assertEquals(id, GolemCoreType.byLegacyId(id).legacyId());
        assertEquals(GolemCoreType.ALCHEMY, GolemCoreType.byLegacyId(6));
        assertEquals("essentia", GolemCoreType.ALCHEMY.textureId());
        assertEquals(6, GolemCoreType.FILL.configurationSlots(0));
        assertEquals(12, GolemCoreType.FILL.configurationSlots(1));
        assertEquals(18, GolemCoreType.USE.configurationSlots(2));
        assertEquals(1, GolemCoreType.LIQUID.configurationSlots(0));
        assertEquals(3, GolemCoreType.LIQUID.configurationSlots(2));
    }

    @Test void allSixLegacyUpgradeIdsAreStable() {
        assertArrayEquals(new String[]{"aer","terra","ignis","aqua","ordo","perditio"},
                java.util.Arrays.stream(GolemUpgradeType.values()).map(GolemUpgradeType::id).toArray(String[]::new));
    }

    @Test void coreTexturesAreByteExactTc4Assets() throws Exception {
        for (GolemCoreType core : GolemCoreType.values()) {
            String texture = "golem_core_" + core.textureId() + ".png";
            assertEquals(sha1(ORIGINAL.resolve(texture)),
                    sha1(RESOURCES.resolve("assets/thaumic_reborn/textures/item/" + texture)), core.id());
            assertEquals(Files.readString(ORIGINAL.resolve(texture + ".mcmeta")).replaceAll("\\s", ""),
                    Files.readString(RESOURCES.resolve(
                            "assets/thaumic_reborn/textures/item/" + texture + ".mcmeta")).replaceAll("\\s", ""),
                    core.id() + " animation metadata");
        }
    }

    @Test void everyCoreResearchIsActiveAndShowsExecutableRecipe() throws IOException {
        for (GolemCoreType core : GolemCoreType.values()) {
            String researchId = "core" + core.id();
            JsonObject research = json("data/thaumic_reborn/thaumcraft/research/legacy/" + researchId + ".json");
            assertFalse(research.get("inactive").getAsBoolean(), researchId);
            assertEquals("thaumic_reborn:" + core.id() + "_golem_core", research.get("icon").getAsString());
            assertTrue(research.getAsJsonArray("pages").asList().stream()
                    .map(element -> element.getAsJsonObject())
                    .anyMatch(page -> {
                        String type = page.get("type").getAsString();
                        return "recipe".equals(type) || "infusion".equals(type);
                    }), researchId);
        }
    }

    @Test void classicInteractionStateAndModernGoalContractsArePresent() throws Exception {
        String entity = Files.readString(Path.of("src/main/java/com/thaumcraftmodern/entity/ClassicGolemEntity.java"));
        String goals = Files.readString(Path.of("src/main/java/com/thaumcraftmodern/entity/GolemCoreGoals.java"));
        String renderer = Files.readString(Path.of("src/main/java/com/thaumcraftmodern/client/render/GolemCoreRenderLayer.java"));
        String heldRenderer = Files.readString(Path.of("src/main/java/com/thaumcraftmodern/client/render/GolemHeldItemRenderLayer.java"));
        String bobber = Files.readString(Path.of("src/main/java/com/thaumcraftmodern/entity/GolemFishingBobberEntity.java"));
        String bobberRenderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/GolemFishingBobberRenderer.java"));
        assertAll(
                () -> assertTrue(entity.contains("instanceof GolemCoreItem")),
                () -> assertTrue(entity.contains("putByteArray(\"Upgrades\"")),
                () -> assertTrue(entity.contains("putLong(\"GolemHomePos\"")),
                () -> assertTrue(entity.contains("restrictTo(BlockPos.of(tag.getLong(\"GolemHomePos\"))")),
                () -> assertTrue(entity.contains("tag.put(\"GolemInventory\"")),
                () -> assertTrue(entity.contains("tag.putString(\"EssentiaCarried\"")),
                () -> assertTrue(entity.contains("ContainerHelper.saveAllItems")),
                () -> assertTrue(goals.contains("extends MeleeAttackGoal")),
                () -> assertTrue(goals.contains("extends NearestAttackableTargetGoal")),
                () -> assertTrue(goals.contains("case HARVEST")),
                () -> assertTrue(renderer.contains("translateToBody")),
                () -> assertTrue(renderer.contains("ModItems.golemCore(core)")),
                () -> assertTrue(renderer.contains("golem_upgrade_empty.png")),
                () -> assertTrue(renderer.contains("renderEmptySlot(")),
                () -> assertFalse(renderer.contains("if (core == null) return"),
                        "TC4 renders installed upgrades independently of an empty core"),
                () -> assertTrue(entity.contains("EntityDataSerializers.ITEM_STACK")),
                () -> assertTrue(entity.contains("carriedForDisplay()")),
                () -> assertTrue(heldRenderer.contains("renderBetweenHands")),
                () -> assertTrue(heldRenderer.contains(
                        "private static final float HELD_ITEM_SCALE = 1.3F"
                )),
                () -> assertEquals(1, heldRenderer.split(
                        "poses.scale\\(HELD_ITEM_SCALE, HELD_ITEM_SCALE, "
                                + "HELD_ITEM_SCALE\\)", -1
                ).length - 1),
                () -> assertTrue(heldRenderer.contains("poses.translate(0F, 2.5F, -1.25F)")),
                () -> assertFalse(heldRenderer.contains("poses.translate(-.5F, 2.5F, -1.25F)")),
                () -> assertTrue(heldRenderer.contains("Items.FISHING_ROD")),
                () -> assertTrue(goals.contains("animatedContainer.startOpen(containerActor)")),
                () -> assertTrue(goals.contains("animatedContainer.stopOpen(containerActor)")),
                () -> assertTrue(goals.contains("GOLEM_FISHING_BOBBER")),
                () -> assertTrue(bobber.contains("SPLASH_CATCH")),
                () -> assertTrue(bobberRenderer.contains("RenderType.lineStrip()")),
                () -> assertTrue(bobberRenderer.contains("Mth.sin(bodyYaw) * .18D")),
                () -> assertTrue(goals.contains("dryFishingCatchLanding()")),
                () -> assertTrue(goals.contains("landing.x, landing.y, landing.z")),
                () -> assertTrue(entity.contains("FluidUtil.getFilledBucket")));

        assertEquals(
                sha1(Path.of(
                        "reference/Thaumcraft-4.2-FOREVA-master/src/main/"
                                + "resources/assets/thaumcraft/textures/items/"
                                + "golem_upgrade_empty.png"
                )),
                sha1(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/item/"
                                + "golem_upgrade_empty.png"
                ))
        );
    }

    @Test void originalBootAnimationAiAndMaterialGuiContractsArePresent() throws Exception {
        String entity = Files.readString(Path.of("src/main/java/com/thaumcraftmodern/entity/ClassicGolemEntity.java"));
        String model = Files.readString(Path.of("src/main/java/com/thaumcraftmodern/client/render/StrawGolemModel.java"));
        String goals = Files.readString(Path.of("src/main/java/com/thaumcraftmodern/entity/GolemCoreGoals.java"));
        String menu = Files.readString(Path.of("src/main/java/com/thaumcraftmodern/world/menu/GolemMenu.java"));
        String screen = Files.readString(Path.of("src/main/java/com/thaumcraftmodern/client/screen/GolemScreen.java"));
        assertAll(
                () -> assertTrue(entity.contains("broadcastEntityEvent(this, (byte) 7)")),
                () -> assertTrue(entity.contains("bootup *= bootup / 33.1F")),
                () -> assertTrue(entity.contains("ModSounds.CAMERA_TICKS")),
                () -> assertTrue(entity.contains("GolemFilters")),
                () -> assertTrue(model.contains("entity.bootup() / 57.295776F")),
                () -> assertTrue(goals.contains("DefaultRandomPos.getPosAway")),
                () -> assertFalse(goals.contains("RandomStrollGoal")),
                () -> assertTrue(menu.contains("cursor.copyWithCount(1)")),
                () -> assertTrue(menu.contains("VISIBLE_FILTERS = 6")),
                () -> assertTrue(menu.contains("ensureConfigurationInventories")),
                () -> assertTrue(menu.contains("FILTER_BUTTON_BASE")),
                () -> assertTrue(menu.contains("isActive() { return false; }")),
                () -> assertFalse(menu.contains("void clicked(")),
                () -> assertFalse(menu.contains("setCarried(")),
                () -> assertTrue(screen.contains("golem.material().ordinal() * 24")),
                () -> assertTrue(screen.contains("TEXT_SCALE = .5F")),
                () -> assertTrue(screen.contains("graphics.renderItem(icon")),
                () -> assertTrue(screen.contains("renderEntityInInventoryFollowsMouse")),
                () -> assertTrue(screen.contains("hoveredFilterView")),
                () -> assertTrue(screen.contains("graphics.renderTooltip(font, icon")),
                () -> assertEquals(sha1(ORIGINAL_GUI), sha1(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/gui/guigolem.png"))));
    }

    @Test void golemBellKeepsOriginalMarkerContractAndWorldUi() throws Exception {
        String bell = Files.readString(Path.of("src/main/java/com/thaumcraftmodern/item/GolemBellItem.java"));
        String entity = Files.readString(Path.of("src/main/java/com/thaumcraftmodern/entity/ClassicGolemEntity.java"));
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/GolemBellMarkerRenderer.java"));
        String renderTypes = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/GolemBellRenderTypes.java"));
        String creative = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModCreativeTabs.java"));
        JsonObject research = json("data/thaumic_reborn/thaumcraft/research/legacy/golembell.json");
        JsonObject recipe = json("data/thaumic_reborn/recipes/golem_bell.json");
        Path originalAssets = Path.of(
                "reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/assets/thaumcraft/textures");
        assertAll(
                () -> assertTrue(bell.contains("SoundEvents.EXPERIENCE_ORB_PICKUP")),
                () -> assertTrue(bell.contains("ModSounds.ZAP")),
                () -> assertTrue(bell.contains("onItemUseFirst(ItemStack stack, UseOnContext context)")),
                () -> assertTrue(bell.contains("onEntityInteract(PlayerInteractEvent.EntityInteract event)")),
                () -> assertTrue(bell.contains("onEntityInteractSpecific(")),
                () -> assertTrue(bell.contains("selectGolem(event.getItemStack(), player")),
                () -> assertTrue(bell.contains("putLong(HOME")),
                () -> assertTrue(bell.contains("writeMarkers(stack, golem.markers())")),
                () -> assertTrue(bell.contains("new GolemBellSyncPacket(hand, bell)")),
                () -> assertTrue(bell.contains("player.inventoryMenu.broadcastChanges()")),
                () -> assertTrue(entity.contains("changeMarker(BlockPos pos, Direction side, boolean sneaking)")),
                () -> assertTrue(entity.contains("removeMarkersAt(BlockPos pos)")),
                () -> assertTrue(bell.contains("BlockEvent.BreakEvent")),
                () -> assertTrue(bell.contains("writeMarkers(bell, selectedGolem.markers())")),
                () -> assertTrue(renderer.contains("drawFace")),
                () -> assertTrue(renderer.contains("drawLink")),
                () -> assertTrue(renderTypes.contains("GL_TEXTURE_WRAP_S")),
                () -> assertTrue(renderTypes.contains("GL_REPEAT")),
                () -> assertTrue(renderTypes.contains("REPEATING_SCRIPT")),
                () -> assertTrue(renderTypes.contains("POSITION_COLOR_TEX")),
                () -> assertTrue(renderTypes.contains("POSITION_COLOR_TEX_SHADER")),
                () -> assertTrue(renderTypes.contains(
                        ".setTransparencyState(LIGHTNING_TRANSPARENCY)")),
                () -> assertFalse(renderTypes.contains(
                        ".setTransparencyState(ADDITIVE_TRANSPARENCY)")),
                () -> assertTrue(renderTypes.contains(
                        "new TextureStateShard(texture, true, false)")),
                () -> assertTrue(renderer.contains(".uv2(200)")),
                () -> assertFalse(renderer.contains("LightTexture.FULL_BRIGHT")),
                () -> assertTrue(renderer.contains("MultiBufferSource.immediate(new BufferBuilder(4096))")),
                () -> assertTrue(renderer.contains("linkBuffers.endBatch(LINK_TYPE)")),
                () -> assertTrue(renderer.contains(
                        "drawFace(poses.last(), buffers.getBuffer(MARK_TYPE)")),
                () -> assertTrue(renderer.contains("buffers.endBatch(MARK_TYPE)")),
                () -> assertFalse(renderer.contains(
                        "VertexConsumer marks = buffers.getBuffer(MARK_TYPE)")),
                () -> assertTrue(renderer.contains("renderBellStatus(RenderGuiEvent.Post event)")),
                () -> assertTrue(creative.contains("get(\"golem_bell\").get()")),
                () -> assertFalse(research.get("inactive").getAsBoolean()),
                () -> assertEquals("thaumic_reborn:golem_bell",
                        recipe.getAsJsonObject("result").get("item").getAsString()),
                () -> assertEquals(sha1(originalAssets.resolve("misc/mark.png")), sha1(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/misc/mark.png"))),
                () -> assertEquals(sha1(originalAssets.resolve("misc/home.png")), sha1(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/misc/home.png"))),
                () -> assertEquals(sha1(originalAssets.resolve("blocks/empty.png")), sha1(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/block/empty.png"))));
    }

    private static JsonObject json(String path) throws IOException {
        return JsonParser.parseString(Files.readString(RESOURCES.resolve(path))).getAsJsonObject();
    }

    private static String sha1(Path path) throws Exception {
        return java.util.HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-1").digest(Files.readAllBytes(path)));
    }
}
