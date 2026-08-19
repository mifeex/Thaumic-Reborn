import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Extracts the original TC4 status-effect icons from textures/misc/potions.png.
 *
 * <p>Minecraft 1.7 draws potion status icons from the atlas band beginning at
 * Y=198. {@code Potion#setIconIndex(x, y)} selects an 18x18 cell inside that
 * band, so the PNG row is {@code 198 + y * 18}.</p>
 */
public final class ExtractTc4EffectIcons {
    private static final int ICON_SIZE = 18;
    private static final Map<String, Cell> ICONS = legacyIcons();

    private ExtractTc4EffectIcons() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException(
                    "Usage: java tools/ExtractTc4EffectIcons.java <potions.png> <output-directory>"
            );
        }

        Path atlasPath = Path.of(args[0]);
        Path outputDirectory = Path.of(args[1]);
        BufferedImage atlas = ImageIO.read(atlasPath.toFile());
        if (atlas == null) {
            throw new IOException("Unable to decode " + atlasPath);
        }
        if (atlas.getWidth() != 256 || atlas.getHeight() != 256) {
            throw new IOException(
                    "Expected the original 256x256 TC4 potion atlas, got "
                            + atlas.getWidth() + "x" + atlas.getHeight()
            );
        }

        Files.createDirectories(outputDirectory);
        for (Map.Entry<String, Cell> entry : ICONS.entrySet()) {
            Cell cell = entry.getValue();
            int sourceX = cell.x() * ICON_SIZE;
            int sourceY = 198 + cell.y() * ICON_SIZE;
            BufferedImage icon = atlas.getSubimage(sourceX, sourceY, ICON_SIZE, ICON_SIZE);
            Path output = outputDirectory.resolve(entry.getKey() + ".png");
            if (!ImageIO.write(icon, "png", output.toFile())) {
                throw new IOException("No PNG writer available for " + output);
            }
            System.out.println(output);
        }
    }

    private static Map<String, Cell> legacyIcons() {
        Map<String, Cell> icons = new LinkedHashMap<>();
        icons.put("vis_exhaust", new Cell(5, 1));
        icons.put("infectious_vis_exhaust", new Cell(6, 1));
        icons.put("flux_taint", new Cell(3, 1));
        icons.put("unnatural_hunger", new Cell(7, 1));
        icons.put("warp_ward", new Cell(3, 2));
        icons.put("death_gaze", new Cell(4, 2));
        icons.put("blurred_vision", new Cell(5, 2));
        icons.put("sun_scorned", new Cell(6, 2));
        icons.put("thaumarhia", new Cell(7, 2));
        return icons;
    }

    private record Cell(int x, int y) {
    }
}
