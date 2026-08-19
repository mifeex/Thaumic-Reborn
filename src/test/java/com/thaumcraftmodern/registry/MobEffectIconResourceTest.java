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
                    "44eda129671afb76047e865863af2f63343eb237d80c1a82ecf4a14ad8e3a020"
            ),
            Map.entry(
                    "infectious_vis_exhaust",
                    "90ec1a08796377f3961f149047643bb8797493b650b4e663c36e2fc234c2e7aa"
            ),
            Map.entry(
                    "flux_taint",
                    "a1cbacb7b147575c8943e31a07554fd23cc9945a9d7f58fd2b6b45b8bdef69c0"
            ),
            Map.entry(
                    "unnatural_hunger",
                    "c674b21ee5b5bfb40cc0babfa8c392aa29cfecc68f3376a7b3c3ebb17f1f198f"
            ),
            Map.entry(
                    "warp_ward",
                    "a025c74251a8c6a7c29e758d1a62734a7003a157f6115fbf9c9f29aa3397fe08"
            ),
            Map.entry(
                    "death_gaze",
                    "f927894e8b7237c49d8bffb6f433666c9cce3edd200095bcdb579a5aad572d98"
            ),
            Map.entry(
                    "blurred_vision",
                    "9169d4a10ecd149969d931cdca9b4935f6d966f4f3ea874ab1c4c1506d2e5ea0"
            ),
            Map.entry(
                    "sun_scorned",
                    "08d99c64f3acfe62d9a51de1704aaf342af8b11732cbf0d6922d90d46dc73b34"
            ),
            Map.entry(
                    "thaumarhia",
                    "10faccec4f7decfb78aa2b57ea3a66f6584b6f00aeb60db23d83ff1819ec1711"
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
