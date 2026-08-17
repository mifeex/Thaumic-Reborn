package com.thaumcraftmodern.golemancy;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thaumcraftmodern.entity.StrawGolemEntity;
import com.thaumcraftmodern.entity.GolemMaterial;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

final class ClassicGolemancyFirstVerticalFidelityTest {
    private static final Path RESOURCES = Path.of("src/main/resources");

    @Test void strawStatsMatchTc4235EnumGolemType() {
        assertEquals(10, StrawGolemEntity.HEALTH);
        assertEquals(0, StrawGolemEntity.ARMOR);
        assertEquals(.38D, StrawGolemEntity.SPEED);
        assertEquals(1, StrawGolemEntity.CARRY_LIMIT);
        assertEquals(1, StrawGolemEntity.UPGRADE_SLOTS);
        assertEquals(75, StrawGolemEntity.REGEN_DELAY);
    }

    @Test void allMaterialStatsMatchTc4235EnumGolemType() {
        assertMaterial(GolemMaterial.STRAW,10,1,0,0,.38,false,1,75,0);
        assertMaterial(GolemMaterial.WOOD,20,4,1,6,.35,false,1,75,1);
        assertMaterial(GolemMaterial.TALLOW,20,8,2,9,.33,false,2,75,2);
        assertMaterial(GolemMaterial.CLAY,25,8,2,9,.33,true,1,100,2);
        assertMaterial(GolemMaterial.FLESH,15,4,1,6,.35,false,2,40,1);
        assertMaterial(GolemMaterial.STONE,30,16,3,12,.32,true,1,100,3);
        assertMaterial(GolemMaterial.IRON,35,32,4,15,.31,true,1,125,4);
        assertMaterial(GolemMaterial.THAUMIUM,40,32,4,15,.32,true,2,100,4);
    }

    @Test void originalTexturesArePresentAtOriginalAtlasSizes() throws IOException {
        assertImage("assets/thaumic_reborn/textures/entity/models/chesthungry.png",64,64);
        assertImage("assets/thaumic_reborn/textures/block/chesthungry.png",64,64);
        assertImage("assets/thaumic_reborn/textures/entity/models/golem_straw.png",128,128);
        assertImage("assets/thaumic_reborn/textures/item/golem_straw.png",16,16);
        assertEquals("192d9c378e49634af120097ee5be84c861491470",
                sha1(RESOURCES.resolve("assets/thaumic_reborn/textures/entity/models/chesthungry.png")));
        assertEquals("192d9c378e49634af120097ee5be84c861491470",
                sha1(RESOURCES.resolve("assets/thaumic_reborn/textures/block/chesthungry.png")));
    }

    @Test void hungryChestUsesOriginalModelBoxUvAndExactThreePieceItemModel() throws IOException {
        String model = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/HungryChestModel.java"));
        assertTrue(model.contains("box(poses,out,light,overlay,0,-5,-14,14,5,14,0,0)"));
        assertTrue(model.contains("box(poses,out,light,overlay,-1,-2,-15,2,4,1,0,0)"));

        JsonObject itemModel=json("assets/thaumic_reborn/models/item/hungry_chest.json");
        assertEquals(3,itemModel.getAsJsonArray("elements").size());
        assertEquals("thaumic_reborn:block/chesthungry",
                itemModel.getAsJsonObject("textures").get("shell").getAsString());
        assertEquals(14,itemModel.getAsJsonArray("elements").get(1).getAsJsonObject()
                .getAsJsonArray("to").get(1).getAsInt());
    }

    @Test void hungryChestBiteUsesOriginalSpeedAndOneSynchronizedEvent() throws IOException {
        String tile=Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/HungryChestBlockEntity.java"));
        String block=Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/HungryChestBlock.java"));
        assertTrue(tile.contains("EAT_EVENT = 2"));
        assertTrue(tile.contains("EAT_LID_KICK = 2"));
        assertTrue(tile.contains("NORMAL_LID_SPEED = 0.1F"));
        assertTrue(tile.contains("ITEM_LIFT_TICKS = 2"));
        assertTrue(tile.contains("lid = Math.max(lid, value / 10F)"));
        assertTrue(tile.contains("level.playLocalSound"));
        assertTrue(block.contains("chest.startEating(item)"));
        assertFalse(block.contains("level.playSound(null, item.getX()"));
    }

    @Test void allGolemAndFetterTexturesAreExactOriginalFiles() throws IOException {
        String[] kinds={"straw","wood","tallow","clay","flesh","stone","iron","thaumium"};
        String[] itemHashes={"ee854a90434026d9e1acea220eb10eae57a51985","337f2b83f6d4b647d1b43ca7a166025beda5f9c0",
                "a2edf58dedd0528d4f45de896c385c6f53052ab2","17850db7ef31d41f7e74759b10574449611aa05c",
                "b5c4d53b9435c2e93a2bf0083b35d89528e6eed1","b52ab6f008ec52668c89dcaffe08844bd5bd20d6",
                "a9ec873ae0267a49355eefd41af473f28dc84e74","055a58e7cd73253b00798ba1ada3d8b434d20ad2"};
        String[] entityHashes={"9f1a5188c3502641d85b95beb54175351b0248e0","7d5d885a06a9b84ca044080dc3617c51c8eed91e",
                "3f05a95ae3fcd4f4f4d70d4ea79d70b1cbdfa6e5","2015c40c23daf04cdad56237811275d55b47bc90",
                "f17010cd359aa999d62b40c17a45a9bca3d8607b","63e8670af33ae610e47adda0c98dcda920e36407",
                "b58150fff142680e4e237f27ca74a84f4c29b0f1","ae027547b66fc5f285a1250d17ba13ac36b39bfc"};
        for(int i=0;i<kinds.length;i++){
            assertEquals(itemHashes[i],sha1(RESOURCES.resolve("assets/thaumic_reborn/textures/item/golem_"+kinds[i]+".png")));
            assertEquals(entityHashes[i],sha1(RESOURCES.resolve("assets/thaumic_reborn/textures/entity/models/golem_"+kinds[i]+".png")));
        }
        assertEquals("ad4b3f55d1c114861148f384c119cf26bd737916",sha1(RESOURCES.resolve("assets/thaumic_reborn/textures/block/golem_stone_side.png")));
        assertEquals("afeefc0cbf232e7b59929a2489d6f4871c48f032",sha1(RESOURCES.resolve("assets/thaumic_reborn/textures/block/golem_stone_top.png")));
        assertEquals("3794132b89df69898e296de3a17574e861725445",sha1(RESOURCES.resolve("assets/thaumic_reborn/textures/block/golem_stone_top_active.png")));
    }

    @Test void allMaterialResearchAndCrucibleRecipesAreActive() throws IOException {
        for(String kind:new String[]{"wood","tallow","clay","flesh","stone","iron","thaumium"}){
            JsonObject research=json("data/thaumic_reborn/thaumcraft/research/legacy/golem"+kind+".json");
            assertFalse(research.get("inactive").getAsBoolean(),kind);
            assertEquals("thaumic_reborn:"+kind+"_golem",research.get("icon").getAsString());
            JsonObject recipe=json("data/thaumic_reborn/thaumcraft/crucible_recipes/golem"+kind+".json");
            assertFalse(recipe.get("inactive").getAsBoolean(),kind);
            assertEquals("thaumic_reborn:"+kind+"_golem",recipe.getAsJsonObject("output").get("item").getAsString());
        }
    }

    @Test void golemFetterKeepsOriginalPoweredDeactivationContract() throws IOException {
        JsonObject states=json("assets/thaumic_reborn/blockstates/golem_fetter.json")
                .getAsJsonObject("variants");
        assertEquals("thaumic_reborn:block/golem_fetter",
                states.getAsJsonObject("active=false").get("model").getAsString());
        assertEquals("thaumic_reborn:block/golem_fetter_active",
                states.getAsJsonObject("active=true").get("model").getAsString());
        String block=Files.readString(Path.of("src/main/java/com/thaumcraftmodern/world/block/GolemFetterBlock.java"));
        String entity=Files.readString(Path.of("src/main/java/com/thaumcraftmodern/entity/ClassicGolemEntity.java"));
        assertTrue(block.contains("level.hasNeighborSignal(pos)"));
        assertTrue(entity.contains("blockPosition().below()).is(ModBlocks.GOLEM_FETTER.get())"));
        JsonObject recipe=json("data/thaumic_reborn/recipes/golem_fetter.json");
        assertEquals("golemfetter",recipe.get("research").getAsString());
        assertEquals(5,recipe.getAsJsonObject("vis").get("terra").getAsInt());
        assertEquals(5,recipe.getAsJsonObject("vis").get("ordo").getAsInt());
    }

    @Test void hungryChestAndStrawGolemRecipesAreExecutableResearchPages() throws IOException {
        JsonObject chest = json("data/thaumic_reborn/thaumcraft/research/legacy/hungrychest.json");
        assertFalse(chest.get("inactive").getAsBoolean());
        assertEquals("thaumic_reborn:hungry_chest", chest.get("icon").getAsString());

        JsonObject golem = json("data/thaumic_reborn/thaumcraft/research/legacy/golemstraw.json");
        assertFalse(golem.get("inactive").getAsBoolean());
        assertEquals("thaumic_reborn:straw_golem",golem.get("icon").getAsString());
        JsonObject recipePage = golem.getAsJsonArray("pages").get(2).getAsJsonObject();
        assertEquals("recipe",recipePage.get("type").getAsString());
        assertEquals("thaumic_reborn:golemstraw",recipePage.get("recipe").getAsString());

        JsonObject crucible = json("data/thaumic_reborn/thaumcraft/crucible_recipes/golemstraw.json");
        assertFalse(crucible.has("inactive"));
        assertEquals("thaumic_reborn:straw_golem",
                crucible.getAsJsonObject("output").get("item").getAsString());
    }

    @Test void modernGolemPlacementAlsoWorksWhileSneaking() throws IOException {
        String item = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/item/ClassicGolemItem.java"));
        assertFalse(item.contains("context.getPlayer().isShiftKeyDown()"));
    }

    private static JsonObject json(String relative) throws IOException {
        return JsonParser.parseString(Files.readString(RESOURCES.resolve(relative))).getAsJsonObject();
    }
    private static void assertMaterial(GolemMaterial material,int health,int carry,int strength,int armor,
            double speed,boolean fireResistant,int upgrades,int regen,int visCost){
        assertAll(material.name(),
                ()->assertEquals(health,material.health()),()->assertEquals(carry,material.carry()),
                ()->assertEquals(strength,material.strength()),()->assertEquals(armor,material.armor()),
                ()->assertEquals(speed,material.speed()),()->assertEquals(fireResistant,material.fireResistant()),
                ()->assertEquals(upgrades,material.upgradeSlots()),()->assertEquals(regen,material.regenerationDelay()),
                ()->assertEquals(visCost,material.visCost()));
    }
    private static void assertImage(String relative,int width,int height) throws IOException {
        BufferedImage image=ImageIO.read(RESOURCES.resolve(relative).toFile());
        assertNotNull(image); assertEquals(width,image.getWidth()); assertEquals(height,image.getHeight());
    }
    private static String sha1(Path path) throws IOException {
        try {
            byte[] digest=MessageDigest.getInstance("SHA-1").digest(Files.readAllBytes(path));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }
}
