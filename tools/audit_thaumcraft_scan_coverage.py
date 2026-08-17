#!/usr/bin/env python3
"""Audit explicit scan coverage for the exact registered TC content inventory."""

from __future__ import annotations

import argparse
import json
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SCAN_ROOT = ROOT / "src/main/resources/data/thaumic_reborn/thaumcraft/scans"
RESOURCE_ROOT = ROOT / "src/main/resources"


def source_inventory() -> dict[str, list[str]]:
    """Resolve every registration without loading Minecraft registries.

    Direct fields use their model-shaped field name when helpers construct the
    final ID (wandCap, wandRod, and staffRod). Dynamic map registrations are
    taken from their declared name list and the generated model families.
    """
    import re

    assets = RESOURCE_ROOT / "assets/thaumic_reborn"
    item_models = {path.stem for path in (assets / "models/item").glob("*.json")}
    block_states = {path.stem for path in (assets / "blockstates").glob("*.json")}
    block_source = (ROOT / "src/main/java/com/thaumcraftmodern/registry/ModBlocks.java") \
        .read_text(encoding="utf-8")
    item_source = (ROOT / "src/main/java/com/thaumcraftmodern/registry/ModItems.java") \
        .read_text(encoding="utf-8")

    blocks: set[str] = set()
    block_pattern = re.compile(
        r"public static final RegistryObject<(?:\? extends )?\w*Block>\s+(\w+)\s*=(.*?);",
        re.S,
    )
    for field, expression in block_pattern.findall(block_source):
        literal = re.search(r'"([a-z0-9_]+)"', expression).group(1)
        blocks.add(field.lower() if field.lower() in block_states else literal)

    items: set[str] = set()
    item_pattern = re.compile(
        r"public static final RegistryObject<(?:\? extends )?\w*Item>\s+(\w+)\s*=(.*?);",
        re.S,
    )
    for field, expression in item_pattern.findall(item_source):
        literal = re.search(r'"([a-z0-9_]+)"', expression).group(1)
        items.add(field.lower() if field.lower() in item_models else literal)
    component_body = re.search(
        r"private static Map<String, RegistryObject<Item>> "
        r"registerArcaneRecipeComponents\(\) \{(.*?)"
        r"Map<String, RegistryObject<Item>> registered",
        item_source, re.S,
    ).group(1)
    items.update(re.findall(r'"([a-z0-9_]+)"', component_body))
    items.update(name for name in item_models if name.startswith("apprentice_ring_"))
    items.update(name for name in item_models if name.endswith("_spawn_egg"))
    items.update(name for name in item_models if name.endswith("_golem_core"))
    return {
        "blocks": sorted(f"thaumic_reborn:{name}" for name in blocks),
        "items": sorted(f"thaumic_reborn:{name}" for name in items),
    }


class TagResolver:
    def __init__(self, jars: list[Path]):
        self.jars = [zipfile.ZipFile(jar) for jar in jars if jar.is_file()]
        self.memo: dict[tuple[str, str], set[str]] = {}

    def resolve(self, tag: str, registry: str) -> set[str]:
        key = (tag, registry)
        if key in self.memo:
            return self.memo[key]
        result: set[str] = set()
        self.memo[key] = result
        namespace, name = tag.split(":", 1)
        resource = f"data/{namespace}/tags/{registry}/{name}.json"
        values: list[object] = []
        for jar in self.jars:
            try:
                values.extend(json.loads(jar.read(resource))["values"])
            except KeyError:
                pass
        disk = RESOURCE_ROOT / resource
        if disk.is_file():
            values.extend(json.loads(disk.read_text(encoding="utf-8"))["values"])
        for value in values:
            identifier = value if isinstance(value, str) else value.get("id", "")
            if identifier.startswith("#"):
                result.update(self.resolve(identifier[1:], registry))
            else:
                result.add(identifier)
        return result


def active_coverage(resolver: TagResolver) -> tuple[set[str], set[str]]:
    blocks: set[str] = set()
    items: set[str] = set()
    for path in SCAN_ROOT.rglob("*.json"):
        value = json.loads(path.read_text(encoding="utf-8"))
        if value.get("inactive"):
            continue
        target = value.get("target", "")
        target_type = value.get("type")
        if target_type == "block":
            blocks.add(target)
        elif target_type == "item":
            items.add(target)
        elif target_type == "block_tag":
            blocks.update(resolver.resolve(target, "blocks"))
        elif target_type == "item_tag":
            items.update(resolver.resolve(target, "items"))
    return blocks, items


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--inventory", type=Path)
    parser.add_argument("--modern-data-jar", type=Path, action="append", default=[])
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()

    inventory = (json.loads(args.inventory.read_text(encoding="utf-8"))
                 if args.inventory else source_inventory())
    registered_blocks = set(inventory["blocks"])
    registered_items = set(inventory["items"])
    block_coverage, item_coverage = active_coverage(TagResolver(args.modern_data_jar))
    missing_blocks = sorted(registered_blocks - block_coverage)
    # A BlockItem first resolves through its block scan, so block coverage is
    # valid item coverage as well.
    missing_items = sorted(registered_items - item_coverage - block_coverage)

    lines = [
        "# ThaumcraftModern registered content without an explicit scan",
        "",
        "The inventory is extracted from the `ModBlocks.BLOCKS` and "
        "`ModItems.ITEMS` source declarations, including their dynamic "
        "registration families; model overrides and unused assets are not counted.",
        "Block items are considered covered when their registered block has an "
        "explicit direct or tag scan, matching `ScanRegistry.findForItem`.",
        "Automatic compatibility inference is deliberately not counted as a "
        "faithful TC4 scan.",
        "",
        f"Registered blocks: **{len(registered_blocks)}**; explicit scan gaps: "
        f"**{len(missing_blocks)}**.",
        f"Registered items: **{len(registered_items)}**; explicit scan gaps after "
        f"BlockItem fallback: **{len(missing_items)}**.",
        "",
        "## Blocks",
        "",
        *(f"- `{identifier}`" for identifier in missing_blocks),
        "",
        "## Items",
        "",
        *(f"- `{identifier}`" for identifier in missing_items),
        "",
    ]
    args.output.write_text("\n".join(lines), encoding="utf-8")


if __name__ == "__main__":
    main()
