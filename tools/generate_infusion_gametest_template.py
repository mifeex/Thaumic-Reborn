#!/usr/bin/env python3
"""Generate the empty 29x5x29 structure used to isolate TC4's radius-12 scan."""

from __future__ import annotations

import gzip
import struct
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
TARGET = ROOT / "src/main/resources/data/thaumic_reborn/structures/infusion_empty.nbt"


def named_tag(tag_type: int, name: str, payload: bytes) -> bytes:
    encoded = name.encode("utf-8")
    return bytes([tag_type]) + struct.pack(">H", len(encoded)) + encoded + payload


root = b"".join((
    named_tag(3, "DataVersion", struct.pack(">i", 3465)),
    named_tag(9, "size", bytes([3]) + struct.pack(">i", 3)
              + struct.pack(">iii", 29, 5, 29)),
    named_tag(9, "palette", bytes([10]) + struct.pack(">i", 1)
              + named_tag(8, "Name", struct.pack(">H", 13) + b"minecraft:air") + b"\x00"),
    named_tag(9, "blocks", bytes([10]) + struct.pack(">i", 0)),
    named_tag(9, "entities", bytes([10]) + struct.pack(">i", 0)),
))
TARGET.parent.mkdir(parents=True, exist_ok=True)
with TARGET.open("wb") as raw, gzip.GzipFile(fileobj=raw, mode="wb", mtime=0) as output:
    output.write(b"\x0a\x00\x00" + root + b"\x00")
