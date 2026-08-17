#!/usr/bin/env python3
"""List 1.20.1 vanilla blocks that postdate 1.7.10 and lack a scan.

The comparison uses the official 1.7.10 client JAR as the historical registry
baseline and the ForgeGradle mapped 1.20.1 JAR used by this project. Active
direct block scans and active block-tag scans are both treated as coverage.
"""

from __future__ import annotations

import argparse
import json
import re
import subprocess
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SCAN_ROOT = ROOT / "src/main/resources/data/thaumic_reborn/thaumcraft/scans"

RENAMES = {
    "brick_block": "bricks", "deadbush": "dead_bush",
    "double_plant": "sunflower", "fence": "oak_fence",
    "fence_gate": "oak_fence_gate", "golden_rail": "powered_rail",
    "grass": "grass_block", "hardened_clay": "terracotta",
    "lit_pumpkin": "jack_o_lantern", "melon_block": "melon",
    "mob_spawner": "spawner", "monster_egg": "infested_stone",
    "nether_brick": "nether_bricks", "noteblock": "note_block",
    "portal": "nether_portal", "reeds": "sugar_cane",
    "red_flower": "poppy", "snow_layer": "snow",
    "standing_sign": "oak_sign", "stonebrick": "stone_bricks",
    "tallgrass": "grass", "trapdoor": "oak_trapdoor",
    "wall_sign": "oak_wall_sign", "waterlily": "lily_pad",
    "web": "cobweb", "wooden_button": "oak_button",
    "wooden_door": "oak_door", "wooden_pressure_plate": "oak_pressure_plate",
    "yellow_flower": "dandelion",
}

COLORS = (
    "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
    "gray", "light_gray", "cyan", "purple", "blue", "brown", "green",
    "red", "black",
)
OLD_WOODS = ("oak", "spruce", "birch", "jungle", "acacia", "dark_oak")


def block_literals(jar: Path, class_name: str) -> set[str]:
    output = subprocess.run(
        ["javap", "-classpath", str(jar), "-c", "-p", class_name],
        check=True, capture_output=True, text=True,
    ).stdout
    return set(re.findall(r"// String ([a-z0-9_]+)$", output, re.MULTILINE))


def legacy_registry_ids(jar: Path) -> set[str]:
    output = subprocess.run(
        ["javap", "-classpath", str(jar), "-c", "-p", "aji"],
        check=True, capture_output=True, text=True,
    ).stdout.splitlines()
    result: set[str] = set()
    awaiting_name = False
    for line in output:
        if "Field c:Lcw" in line:
            awaiting_name = True
            continue
        if awaiting_name:
            match = re.search(r"// String ([a-z0-9_]+)$", line)
            if match:
                result.add(match.group(1))
                awaiting_name = False
            elif "invokevirtual" in line:
                awaiting_name = False
    result.discard("doTileDrops")
    return result


def legacy_equivalents(legacy: set[str], modern: set[str]) -> set[str]:
    result = {RENAMES.get(name, name) for name in legacy}
    result &= modern

    for color in COLORS:
        result.update({
            f"{color}_wool", f"{color}_carpet", f"{color}_stained_glass",
            f"{color}_stained_glass_pane", f"{color}_terracotta",
        })
    for wood in OLD_WOODS:
        result.update({
            f"{wood}_planks", f"{wood}_sapling", f"{wood}_log",
            f"{wood}_wood", f"{wood}_leaves", f"{wood}_slab",
            f"{wood}_stairs",
        })
    result.update({
        "coarse_dirt", "podzol", "red_sand", "chiseled_sandstone",
        "cut_sandstone", "mossy_cobblestone_wall", "chipped_anvil",
        "damaged_anvil", "mushroom_stem", "water_cauldron", "wall_torch",
        "redstone_wall_torch", "attached_melon_stem",
        "attached_pumpkin_stem", "moving_piston", "red_bed",
        "mossy_stone_bricks", "cracked_stone_bricks",
        "chiseled_stone_bricks", "chiseled_quartz_block", "quartz_pillar",
        "smooth_stone_slab", "sandstone_slab", "petrified_oak_slab",
        "cobblestone_slab", "brick_slab", "stone_brick_slab",
        "nether_brick_slab", "quartz_slab", "infested_cobblestone",
        "infested_stone_bricks", "infested_mossy_stone_bricks",
        "infested_cracked_stone_bricks", "infested_chiseled_stone_bricks",
        "blue_orchid", "allium", "azure_bluet", "red_tulip",
        "orange_tulip", "white_tulip", "pink_tulip", "oxeye_daisy",
        "lilac", "tall_grass", "large_fern", "rose_bush", "peony",
    })
    for head in ("skeleton_skull", "wither_skeleton_skull", "zombie_head",
                 "player_head", "creeper_head"):
        result.add(head)
        result.add(head.replace("_head", "_wall_head").replace(
            "_skull", "_wall_skull"))
    return result & modern


def active_scan_coverage() -> tuple[set[str], set[str]]:
    direct: set[str] = set()
    tags: set[str] = set()
    for path in SCAN_ROOT.rglob("*.json"):
        value = json.loads(path.read_text(encoding="utf-8"))
        if value.get("inactive"):
            continue
        if value.get("type") == "block" and value.get("target", "").startswith(
                "minecraft:"):
            direct.add(value["target"])
        elif value.get("type") == "block_tag":
            tags.add(value["target"])
    return direct, tags


class TagResolver:
    def __init__(self, jars: list[Path], resource_roots: list[Path]):
        self.jars = [zipfile.ZipFile(jar) for jar in jars]
        self.resource_roots = resource_roots
        self.memo: dict[str, set[str]] = {}

    def resolve(self, tag: str) -> set[str]:
        if tag in self.memo:
            return self.memo[tag]
        namespace, path = tag.split(":", 1)
        resource = f"data/{namespace}/tags/blocks/{path}.json"
        values: list[str] = []
        for jar in self.jars:
            try:
                values.extend(json.loads(jar.read(resource))["values"])
            except KeyError:
                pass
        for root in self.resource_roots:
            path_on_disk = root / resource
            if path_on_disk.is_file():
                values.extend(json.loads(
                    path_on_disk.read_text(encoding="utf-8")
                )["values"])
        result: set[str] = set()
        self.memo[tag] = result
        for value in values:
            identifier = value if isinstance(value, str) else value.get("id", "")
            if identifier.startswith("#"):
                result.update(self.resolve(identifier[1:]))
            elif identifier.startswith("minecraft:"):
                result.add(identifier)
        return result


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--legacy-client", type=Path, required=True)
    parser.add_argument("--modern-jar", type=Path, required=True)
    parser.add_argument("--modern-data-jar", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()

    modern = block_literals(args.modern_jar, "net.minecraft.world.level.block.Blocks")
    legacy = legacy_registry_ids(args.legacy_client)
    added = modern - legacy_equivalents(legacy, modern)
    direct, tag_scans = active_scan_coverage()
    resolver = TagResolver(
        [args.modern_data_jar, args.modern_jar],
        [ROOT / "src/main/resources"],
    )
    covered = set(direct)
    for tag in tag_scans:
        covered.update(resolver.resolve(tag))
    gaps = sorted(f"minecraft:{name}" for name in added
                  if f"minecraft:{name}" not in covered)

    lines = [
        "# Vanilla 1.20.1 blocks without a TC4 scan example",
        "",
        "Baseline: the official Minecraft 1.7.10 client registry compared with "
        "the mapped Forge 1.20.1 registry used by this project. Metadata variants "
        "that already existed in 1.7.10 are treated as legacy equivalents.",
        "",
        "Legacy client SHA-1: "
        "`e80d9b3bf5085002218d4be59e668bac718abbc6`. Modern baseline: "
        "Minecraft `1.20.1`, Forge `47.4.10`.",
        "",
        "A block is omitted when an active direct block scan or an active block-tag "
        "scan already covers it. Technical blocks are intentionally retained: this "
        "is the exhaustive registry-level gap list, not only Creative-tab items.",
        "",
        f"Total: **{len(gaps)}** blocks.",
        "",
    ]
    lines.extend(f"- `{identifier}`" for identifier in gaps)
    lines.append("")
    args.output.write_text("\n".join(lines), encoding="utf-8")


if __name__ == "__main__":
    main()
