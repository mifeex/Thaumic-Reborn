#!/usr/bin/env python3
"""Copy TC4's exact 16x16 mask icons into their registered item paths."""

from pathlib import Path
import shutil


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/thaumic_reborn/textures"
OUTPUTS = {
    0: "fortress_helmet_mask_grinning_devil.png",
    1: "fortress_helmet_mask_angry_ghost.png",
    2: "fortress_helmet_mask_sipping_fiend.png",
}


def main() -> None:
    item = ASSETS / "item"
    item.mkdir(parents=True, exist_ok=True)
    for mask, filename in OUTPUTS.items():
        shutil.copyfile(ASSETS / "misc" / f"r_mask{mask}.png", item / filename)


if __name__ == "__main__":
    main()
