package com.thaumcraftmodern.registry;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MobEffectIconResourceTest {
    private static final Pattern REGISTERED_EFFECT_ID = Pattern.compile(
            "(?:EFFECTS\\.register|warp)\\s*\\(\\s*\"([^\"]+)\""
    );
    private static final Map<String, String> CLASSIC_ICON_SHA256 = Map.ofEntries(
            Map.entry(
                    "vis_exhaust",
                    "25a370ba7d72eec7c40603ede1036728600995d478e32f2307236c0b2b2b8671"
            ),
            Map.entry(
                    "infectious_vis_exhaust",
                    "88f1b70e94f6ca9ce726371a337bf3174e46dcda4a404539d464ca6a1085024e"
            ),
            Map.entry(
                    "flux_taint",
                    "3ef8c726f9f9e7c2acbe835713513e423edf40a983e4ad97ff0c15b56ebcf682"
            ),
            Map.entry(
                    "unnatural_hunger",
                    "0b96f36d831335719ea64af0be62b314f3ae91d46dff42590a398de45999eb15"
            ),
            Map.entry(
                    "warp_ward",
                    "8da2ae6a1eda0468e4bfae52fd6a3d731bce20adb300f106bdf83f6d4db1af82"
            ),
            Map.entry(
                    "death_gaze",
                    "65f0c01cd26cd7bc940cf2f76773c28b51895e3c31327872fa9782e3f2d9ac85"
            ),
            Map.entry(
                    "blurred_vision",
                    "01e24e6b8f09b3ef23fed31491e41f7d3b3bce2b1c30b9b27b83ee2a2597c194"
            ),
            Map.entry(
                    "sun_scorned",
                    "8e84468c665479e7cf0b2ec91bbef03622aa4b7a89741a98762ff4e358703d7d"
            ),
            Map.entry(
                    "thaumarhia",
                    "fe6cc2f46b8da57f947eb1329dd98c873194fa7f02e8b2899a03358ec51e545b"
            )
    );

    @Test
    void everyRegisteredThaumcraftEffectHasAClassicIcon() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModEffects.java"
        ));

        Set<String> registered = new LinkedHashSet<>();
        Matcher matcher = REGISTERED_EFFECT_ID.matcher(source);
        while (matcher.find()) {
            registered.add(matcher.group(1));
        }

        assertEquals(
                CLASSIC_ICON_SHA256.keySet(),
                registered,
                "Each registered effect must declare its own mob_effect texture"
        );
    }

    @Test
    void effectIconsAreExactNonEmptyTc4AtlasCells() throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        for (Map.Entry<String, String> entry : CLASSIC_ICON_SHA256.entrySet()) {
            String resourcePath = "/assets/thaumic_reborn/textures/mob_effect/"
                    + entry.getKey()
                    + ".png";
            byte[] bytes;
            try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
                assertNotNull(stream, "Missing effect icon " + resourcePath);
                bytes = stream.readAllBytes();
            }

            var image = ImageIO.read(new ByteArrayInputStream(bytes));
            assertNotNull(image, "Unreadable effect icon " + resourcePath);
            assertEquals(18, image.getWidth(), resourcePath);
            assertEquals(18, image.getHeight(), resourcePath);
            assertTrue(hasVisiblePixel(image), "Empty effect icon " + resourcePath);
            if ("vis_exhaust".equals(entry.getKey())) {
                assertTrue(
                        bottomRowIsTransparent(image),
                        "Flux Flu icon has stray pixels on its bottom row"
                );
            }
            assertEquals(
                    entry.getValue(),
                    HexFormat.of().formatHex(sha256.digest(bytes)),
                    "Effect icon no longer matches the original TC4 atlas cell: "
                            + resourcePath
            );
        }
    }

    private static boolean hasVisiblePixel(java.awt.image.BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean bottomRowIsTransparent(
            java.awt.image.BufferedImage image
    ) {
        int y = image.getHeight() - 1;
        for (int x = 0; x < image.getWidth(); x++) {
            if ((image.getRGB(x, y) >>> 24) != 0) {
                return false;
            }
        }
        return true;
    }
}
