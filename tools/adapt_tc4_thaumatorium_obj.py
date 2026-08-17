#!/usr/bin/env python3
"""Bake the TC4 Thaumatorium renderer transform into block-space OBJ.

The original OBJ is centered on X/Y and uses Z as its vertical axis. TC4's
TileThaumatoriumRenderer rotates it -90 degrees around X, then translates it
to the block centre. Baking that transform gives the two-block-tall model the
coordinate system expected by a modern Forge block model while preserving the
original asset byte-for-byte.
"""

from __future__ import annotations

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SOURCE = (
    ROOT
    / "src/main/resources/assets/thaumic_reborn/textures/models"
    / "thaumatorium.obj"
)
OUTPUT = SOURCE.with_name("thaumatorium_block.obj")


def main() -> None:
    output: list[str] = [
        "# Forge block-space adapter generated from the original TC4 thaumatorium.obj",
        "# Original source remains byte-for-byte unchanged in thaumatorium.obj.",
    ]
    for line in SOURCE.read_text(encoding="utf-8").splitlines():
        if line.startswith("v "):
            _, x_raw, y_raw, z_raw = line.split()
            x = float(x_raw)
            y = float(y_raw)
            z = float(z_raw)
            output.append(f"v {x + 0.5:.6f} {z:.6f} {0.5 - y:.6f}")
        elif line.startswith("vn "):
            _, x_raw, y_raw, z_raw = line.split()
            x = float(x_raw)
            y = float(y_raw)
            z = float(z_raw)
            output.append(f"vn {x:.6f} {z:.6f} {-y:.6f}")
        else:
            output.append(line)
    OUTPUT.write_text("\n".join(output) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
