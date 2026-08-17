#!/usr/bin/env python3
"""Copy the two deconstruction-table resources byte-for-byte from TC4."""

from pathlib import Path
from zipfile import ZipFile


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "reference/original/Thaumcraft_1.7.10_4.2.3.5.jar"
ASSETS = ROOT / "src/main/resources/assets/thaumic_reborn/textures"
FILES = {
    "assets/thaumcraft/textures/gui/gui_decontable.png":
        ASSETS / "gui/gui_decontable.png",
    "assets/thaumcraft/textures/models/decontable.png":
        ASSETS / "models/decontable.png",
}


def main() -> None:
    with ZipFile(SOURCE) as archive:
        for source, target in FILES.items():
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(archive.read(source))
    print("Copied TC4 deconstruction GUI and model texture byte-for-byte")


if __name__ == "__main__":
    main()
