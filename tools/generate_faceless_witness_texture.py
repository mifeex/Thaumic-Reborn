#!/usr/bin/env python3
"""Create the fixed 128x128 box-UV texture for the Faceless Witness model."""

from __future__ import annotations

import struct
import zlib
from pathlib import Path


SIZE = 128
OUT = Path("src/main/resources/assets/thaumic_reborn/textures/entity/models/faceless_witness.png")
BLACK = [(10, 9, 15, 255), (17, 16, 24, 255), (25, 23, 34, 255), (35, 31, 45, 255)]
IRON = [(24, 23, 31, 255), (34, 32, 42, 255), (46, 42, 54, 255), (57, 51, 65, 255)]
VIOLET = [(53, 37, 75, 255), (72, 48, 101, 255), (96, 63, 133, 255), (125, 82, 169, 255), (166, 111, 218, 255)]


pixels = [[BLACK[0] for _ in range(SIZE)] for _ in range(SIZE)]


def fill(x: int, y: int, width: int, height: int, palette, seed: int) -> None:
    for py in range(y, min(SIZE, y + height)):
        for px in range(x, min(SIZE, x + width)):
            edge = px in (x, x + width - 1) or py in (y, y + height - 1)
            noise = (px * 17 + py * 31 + seed * 13) % 19
            shade = 0 if edge else (2 if noise == 0 else 1 if noise < 6 else 0)
            pixels[py][px] = palette[min(shade, len(palette) - 1)]


def stripe(x: int, y: int, width: int, height: int, color) -> None:
    for py in range(y, min(SIZE, y + height)):
        px = x + width // 2 + ((py // 7) & 1)
        if 0 <= px < SIZE:
            pixels[py][px] = color


# Hood shell, crown and dedicated face void.
fill(0, 0, 40, 22, IRON, 1)
fill(0, 22, 24, 9, BLACK, 2)
fill(32, 30, 18, 10, [BLACK[0]], 0)

# Torso, chest plate and shoulder armour.
fill(40, 0, 27, 27, BLACK, 3)
fill(72, 0, 23, 15, IRON, 4)
fill(72, 16, 20, 14, IRON, 5)
fill(92, 16, 20, 14, IRON, 6)

# Long arms and legs; every island remains independently readable.
fill(0, 42, 12, 41, BLACK, 7)
fill(16, 42, 12, 41, BLACK, 8)
fill(32, 42, 16, 19, BLACK, 9)
fill(48, 42, 16, 19, BLACK, 10)
fill(32, 64, 16, 14, IRON, 15)
fill(48, 64, 16, 14, IRON, 16)

# Four distinct mantle islands with sparse, non-emissive violet wear.
for args in ((64, 34, 14, 26, 11), (80, 34, 8, 28, 12),
             (90, 34, 8, 28, 13), (100, 34, 18, 29, 14)):
    fill(*args[:4], BLACK, args[4])
    stripe(*args[:4], VIOLET[0])
fill(0, 84, 14, 22, BLACK, 17)
fill(16, 84, 14, 25, BLACK, 18)
stripe(0, 84, 14, 22, VIOLET[1])
stripe(16, 84, 14, 25, VIOLET[1])

# Three segmented back limbs. Each segment uses its own box-UV strip.
for u, seed in ((76, 20), (92, 30), (108, 40)):
    fill(u, 76, 12, 11, VIOLET, seed)
    fill(u, 88, 12, 11, VIOLET, seed + 1)
    fill(u, 100, 12, 11, VIOLET, seed + 2)

# Two eye tips and the snapped third tip. The central slits remain black.
fill(96, 108, 15, 12, VIOLET[1:], 50)
fill(112, 108, 16, 12, VIOLET, 51)
# Dedicated inset eye plates used by the model, kept bright and readable.
fill(96, 120, 14, 8, VIOLET[2:], 52)
for py in range(110, 116):
    for px in (101, 102, 103, 117, 118, 119):
        if px < SIZE:
            pixels[py][px] = BLACK[0]


def chunk(kind: bytes, data: bytes) -> bytes:
    return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)


raw = bytearray()
for row in pixels:
    raw.append(0)
    for pixel in row:
        raw.extend(pixel)

png = b"\x89PNG\r\n\x1a\n"
png += chunk(b"IHDR", struct.pack(">IIBBBBB", SIZE, SIZE, 8, 6, 0, 0, 0))
png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
png += chunk(b"IEND", b"")
OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_bytes(png)
