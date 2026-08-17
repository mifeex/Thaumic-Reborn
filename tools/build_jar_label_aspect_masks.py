#!/usr/bin/env python3
"""Build white-alpha aspect masks used by the classic jar labels."""

from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/resources/assets/thaumic_reborn/textures/aspects"
TARGET = ROOT / "src/main/resources/assets/thaumic_reborn/textures/aspects_label"


def build_mask(source: Path, target: Path) -> None:
    image = Image.open(source).convert("RGBA")
    output = Image.new("RGBA", image.size)
    pixels = []
    for y in range(image.height):
        for x in range(image.width):
            red, green, blue, alpha = image.getpixel((x, y))
            brightness = max(red, green, blue)
            mask_alpha = round(alpha * brightness / 255)
            pixels.append((255, 255, 255, mask_alpha))
    output.putdata(pixels)
    target.parent.mkdir(parents=True, exist_ok=True)
    output.save(target, optimize=True)


def main() -> None:
    for source in sorted(SOURCE.glob("*.png")):
        build_mask(source, TARGET / source.name)


if __name__ == "__main__":
    main()
