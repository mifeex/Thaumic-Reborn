#!/usr/bin/env python3
"""Build atlas-safe derivatives of the untouched TC4 crystallizer texture."""

from collections import deque
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/resources/assets/thaumic_reborn/textures/models/crystalizer.png"
BLOCK = ROOT / "src/main/resources/assets/thaumic_reborn/textures/block/crystalizer.png"
PARTICLE = ROOT / "src/main/resources/assets/thaumic_reborn/textures/block/crystalizer_particle.png"


def is_chroma_key(colour: tuple[int, int, int]) -> bool:
    # The source contains both 255/0/255 and four 254/0/254 edge texels.
    return colour[0] >= 254 and colour[1] == 0 and colour[2] >= 254


def fill_chroma_key(source: Image.Image) -> Image.Image:
    """Dilate the nearest real texel into TC4's magenta unused UV space."""
    image = source.convert("RGB")
    pixels = image.load()
    width, height = image.size
    queue = deque()
    nearest = [[None for _ in range(width)] for _ in range(height)]

    for y in range(height):
        for x in range(width):
            if not is_chroma_key(pixels[x, y]):
                nearest[y][x] = pixels[x, y]
                queue.append((x, y))

    while queue:
        x, y = queue.popleft()
        colour = nearest[y][x]
        for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
            if 0 <= nx < width and 0 <= ny < height and nearest[ny][nx] is None:
                nearest[ny][nx] = colour
                queue.append((nx, ny))

    output = Image.new("RGB", image.size)
    output.putdata([nearest[y][x] for y in range(height) for x in range(width)])
    return output


def main() -> None:
    source = Image.open(SOURCE).convert("RGB")
    fill_chroma_key(source).save(BLOCK, optimize=True)
    # Exact 16x16 dark casing island from the original TC4 UV sheet.
    source.crop((21, 6, 37, 22)).save(PARTICLE, optimize=True)


if __name__ == "__main__":
    main()
