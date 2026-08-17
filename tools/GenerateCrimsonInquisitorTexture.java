import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

/**
 * Makes the Inquisitor armor atlas from the original TC4 Knight atlas.
 * Only the helmet's box-UV island changes: a red visor and face-guard vents.
 */
public final class GenerateCrimsonInquisitorTexture {
    private static final Path SOURCE = Path.of(
            "src/main/resources/assets/thaumic_reborn/textures/entity/models/cultist_plate_armor.png"
    );
    private static final Path OUTPUT = Path.of(
            "src/main/resources/assets/thaumic_reborn/textures/entity/models/inquisitor_plate_armor.png"
    );

    private GenerateCrimsonInquisitorTexture() {
    }

    public static void main(String[] args) throws Exception {
        BufferedImage texture = ImageIO.read(SOURCE.toFile());
        if (texture == null || texture.getWidth() != 256 || texture.getHeight() != 128) {
            throw new IllegalStateException("Unexpected original knight armor atlas");
        }

        // ModelKnightArmor helmet: texOffs(41, 8), 9x9x9 cube.
        visor(texture, 50, 17, 9); // front
        visor(texture, 41, 17, 9); // left
        visor(texture, 59, 17, 9); // right
        visor(texture, 68, 17, 9); // rear, keeps the band coherent in motion

        ImageIO.write(texture, "PNG", OUTPUT.toFile());
    }

    private static void visor(BufferedImage texture, int x, int y, int width) {
        int rim = 0xFF34343A;
        int dark = 0xFF111116;
        int red = 0xFFB01920;
        int hot = 0xFFFF3940;
        for (int px = x; px < x + width; px++) {
            texture.setRGB(px, y + 1, rim);
            texture.setRGB(px, y + 2, red);
            texture.setRGB(px, y + 3, dark);
        }
        texture.setRGB(x + width / 2, y + 2, hot);
        for (int px = x + 1; px < x + width - 1; px += 2) {
            texture.setRGB(px, y + 5, 0xFF050508);
            texture.setRGB(px, y + 6, 0xFF050508);
        }
    }
}
