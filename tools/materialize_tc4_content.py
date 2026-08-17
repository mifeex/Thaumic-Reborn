#!/usr/bin/env python3
"""Materialize the TC4 archive as guarded modern data-pack definitions.

Every legacy registration becomes a JSON record. Definitions that cannot be
represented safely on 1.20.1 carry ``inactive: true`` and are ignored by the
runtime reload listeners.
"""

from __future__ import annotations

import argparse
import csv
import json
import re
import shutil
import zipfile
from collections import Counter, defaultdict
from pathlib import Path


ACTIVE_RESEARCH = {
    "ASPECTS",
    "RESEARCH",
    "KNOWFRAG",
    "THAUMONOMICON",
    "THAUMOMETER",
}

RECIPE_MAPPINGS = {
    "Thaumometer": "thaumic_reborn:thaumometer",
    "Scribe1": "thaumic_reborn:scribing_tools",
    "Scribe2": "thaumic_reborn:scribing_tools",
    "Scribe3": "thaumic_reborn:scribing_tools",
    "KnowFrag": "thaumic_reborn:knowledge_fragment",
    "Thaumonomicon": "thaumic_reborn:thaumonomicon_conversion",
    "ResTable": "thaumic_reborn:research_table",
    "WandBasic": "thaumic_reborn:basic_wand",
    "WandCapIron": "thaumic_reborn:iron_wand_cap",
    "Table": "thaumic_reborn:thaumcraft_table",
    "ArcTable": "thaumic_reborn:arcane_workbench_conversion",
    "ArcaneStone1": "thaumic_reborn:arcane_stone",
    "Goggles": "thaumic_reborn:goggles_of_revealing",
    "NodeJar": "thaumic_reborn:node_jar_capture",
}

MOD_ITEM_MAPPINGS = {
    ("ConfigItems.itemThaumonomicon", None): "thaumic_reborn:thaumonomicon",
    ("ConfigItems.itemThaumometer", None): "thaumic_reborn:thaumometer",
    ("ConfigItems.itemInkwell", None): "thaumic_reborn:scribing_tools",
    ("ConfigItems.itemResearchNotes", None): "thaumic_reborn:research_notes",
    ("ConfigItems.itemResource", 9): "thaumic_reborn:knowledge_fragment",
    ("ConfigItems.itemShard", 0): "thaumic_reborn:air_shard",
    ("ConfigItems.itemShard", 1): "thaumic_reborn:fire_shard",
    ("ConfigItems.itemShard", 2): "thaumic_reborn:water_shard",
    ("ConfigItems.itemShard", 3): "thaumic_reborn:earth_shard",
    ("ConfigItems.itemShard", 4): "thaumic_reborn:order_shard",
    ("ConfigItems.itemShard", 5): "thaumic_reborn:entropy_shard",
    ("ConfigBlocks.blockTable", 1): "thaumic_reborn:research_table",
}

RESOURCE_ICON_MAPPINGS = {
    "ASPECTS": "thaumic_reborn:textures/misc/r_aspects.png",
    "ELDRITCHMAJOR": "thaumic_reborn:textures/misc/r_eldritchmajor.png",
    "RESEARCHER1": "thaumic_reborn:textures/misc/r_researcher1.png",
    "RESEARCHER2": "thaumic_reborn:textures/misc/r_researcher2.png",
    "WARP": "thaumic_reborn:textures/misc/r_warp.png",
}

CATEGORY_ICON_RESOURCES = {
    "BASICS": "thaumic_reborn:textures/items/thaumonomiconcheat.png",
    "THAUMATURGY": "thaumic_reborn:textures/misc/r_thaumaturgy.png",
    "ALCHEMY": "thaumic_reborn:textures/misc/r_crucible.png",
    "ARTIFICE": "thaumic_reborn:textures/misc/r_artifice.png",
    "GOLEMANCY": "thaumic_reborn:textures/misc/r_golemancy.png",
    "ELDRITCH": "thaumic_reborn:textures/misc/r_eldritch.png",
}

LEGACY_RENAMES = {
    "grass": "grass_block",
    "web": "cobweb",
    "lit_redstone_ore": "redstone_ore",
    "lit_furnace": "furnace",
    "lit_pumpkin": "jack_o_lantern",
    "reeds": "sugar_cane",
    "clay": "clay_ball",
    "fireball": "fire_charge",
    "netherbrick": "nether_brick",
}

RESERVED_SCAN_TARGETS = {
    ("block", "minecraft:stone"),
    ("block", "minecraft:redstone_block"),
    ("block", "minecraft:torch"),
    ("entity", "minecraft:cow"),
}

LEGACY_ENTITY_TRIGGER_MAPPINGS = {
    "Enderman": "minecraft:enderman",
}

# TC4.2 grants these insights after successful warp events. The modern
# data-driven progression uses the same non-temporary warp thresholds.
WARP_REVEAL_MINIMUM = {
    "BATHSALTS": 11,
    "ELDRITCHMINOR": 26,
    "ELDRITCHMAJOR": 51,
}


def load_json(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


def write_json(path: Path, value) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def slug(value: str) -> str:
    normalized = re.sub(r"[^a-z0-9._-]+", "_", value.lower()).strip("_")
    return normalized or "entry"


def split_top_level(text: str) -> list[str]:
    result = []
    start = 0
    depths = {"(": 0, "[": 0, "{": 0}
    closing = {")": "(", "]": "[", "}": "{"}
    quote = ""
    escaped = False
    for index, char in enumerate(text):
        if quote:
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == quote:
                quote = ""
            continue
        if char in "\"'":
            quote = char
        elif char in depths:
            depths[char] += 1
        elif char in closing:
            depths[closing[char]] -= 1
        elif char == "," and not any(depths.values()):
            result.append(text[start:index].strip())
            start = index + 1
    result.append(text[start:].strip())
    return result


def item_stack(expression: str) -> tuple[str, int | None] | None:
    match = re.fullmatch(r"new ItemStack\((.*)\)", expression.strip())
    if not match:
        return None
    arguments = split_top_level(match.group(1))
    if not arguments:
        return None
    metadata = None
    if len(arguments) >= 3 and re.fullmatch(r"-?\d+", arguments[2]):
        metadata = int(arguments[2])
    return arguments[0], metadata


def modern_assets(jar: Path) -> tuple[set[str], set[str]]:
    blocks = set()
    items = set()
    with zipfile.ZipFile(jar) as archive:
        for name in archive.namelist():
            block = re.fullmatch(r"assets/minecraft/blockstates/([^/]+)\.json", name)
            item = re.fullmatch(r"assets/minecraft/models/item/([^/]+)\.json", name)
            if block:
                blocks.add(block.group(1))
            if item:
                items.add(item.group(1))
    return blocks, items


def field_names(path: Path) -> dict[str, str]:
    if path.suffix.lower() == ".json":
        return load_json(path)
    with path.open(encoding="utf-8", newline="") as stream:
        return {row["searge"]: row["name"] for row in csv.DictReader(stream)}


def combine_aspects(values: list[dict]) -> list[dict]:
    amounts: dict[str, int] = {}
    for value in values:
        if not isinstance(value.get("amount"), int):
            return values
        amounts[value["id"]] = amounts.get(value["id"], 0) + value["amount"]
    return [{"id": key, "amount": amount} for key, amount in amounts.items() if amount > 0]


def map_stack(
        expression: str,
        mappings: dict[str, str],
        blocks: set[str],
        items: set[str],
) -> tuple[str, str] | None:
    parsed = item_stack(expression)
    if not parsed:
        return None
    base, metadata = parsed

    mod_key = (base, metadata)
    modern = MOD_ITEM_MAPPINGS.get(mod_key)
    if modern is None and metadata in (None, 0):
        modern = MOD_ITEM_MAPPINGS.get((base, None))
    if modern:
        target_type = "block" if modern == "thaumic_reborn:research_table" else "item"
        return target_type, modern

    match = re.fullmatch(r"(Blocks|Items)\.(field_\w+)", base)
    if not match or metadata not in (None, 0, 32767):
        return None
    legacy_name = mappings.get(match.group(2))
    if not legacy_name:
        return None
    modern_name = LEGACY_RENAMES.get(legacy_name, legacy_name)
    candidates = blocks if match.group(1) == "Blocks" else items
    if modern_name not in candidates:
        return None
    return ("block" if match.group(1) == "Blocks" else "item"), f"minecraft:{modern_name}"


def map_research_icon(entry: dict) -> str | None:
    parsed = item_stack(entry["icon_raw"])
    if not parsed:
        return None
    base, metadata = parsed
    modern = MOD_ITEM_MAPPINGS.get((base, metadata))
    if modern is None and metadata in (None, 0):
        modern = MOD_ITEM_MAPPINGS.get((base, None))
    return modern


def research_reveal_condition(
        entry: dict,
        mappings: dict[str, str],
        blocks: set[str],
        items: set[str],
) -> dict:
    warp_minimum = WARP_REVEAL_MINIMUM.get(entry["id"])
    if warp_minimum is not None:
        return {
            "type": "warp",
            "measure": "non_temporary",
            "minimum": warp_minimum,
        }

    if not entry["flags"]["hidden"] and not entry["flags"]["lost"]:
        return {"type": "always"}

    conditions = []
    for expression in entry["item_triggers_raw"]:
        mapped = map_stack(expression, mappings, blocks, items)
        if mapped is not None:
            target_type, target = mapped
            conditions.append({"type": "scan", "id": f"{target_type}:{target}"})
    for legacy_entity in entry["entity_triggers"]:
        target = LEGACY_ENTITY_TRIGGER_MAPPINGS.get(legacy_entity)
        if target is not None:
            conditions.append({"type": "scan", "id": f"entity:{target}"})
    for aspect_id in entry["aspect_triggers"]:
        conditions.append({"type": "scan_aspect", "id": aspect_id})

    # TC4 treats item, entity and aspect clues as alternatives. Preserve that
    # OR relationship and remove duplicates without changing source order.
    unique = []
    seen = set()
    for condition in conditions:
        key = json.dumps(condition, sort_keys=True)
        if key not in seen:
            seen.add(key)
            unique.append(condition)

    if not unique:
        # Some classic clues depend on objects or events that do not exist in
        # the modern port yet. Keep them explicitly gated instead of exposing
        # them from game start or inventing a replacement trigger.
        return {
            "type": "criterion",
            "id": f"thaumic_reborn:legacy_clue/{entry['id'].lower()}",
        }
    if len(unique) == 1:
        return unique[0]
    return {"type": "any_of", "conditions": unique}


def materialize_categories(content_root: Path, categories: list[dict]) -> None:
    target = content_root / "categories/legacy"
    if target.exists():
        shutil.rmtree(target)
    for order, category in enumerate(categories):
        category_id = category["id"]
        if category_id == "BASICS":
            basics_path = content_root / "categories/basics.json"
            basics = load_json(basics_path)
            basics.pop("icon", None)
            basics["icon_resource"] = CATEGORY_ICON_RESOURCES[category_id]
            basics["inactive"] = False
            basics["legacy"] = category
            write_json(basics_path, basics)
            continue
        value = {
            "id": category_id.lower(),
            "title": f"tc.research_category.{category_id}",
            "icon_resource": CATEGORY_ICON_RESOURCES[category_id],
            "background": (
                "thaumic_reborn:textures/gui/gui_researchbackeldritch.png"
                if category_id == "ELDRITCH"
                else "thaumic_reborn:textures/gui/gui_researchback.png"
            ),
            "order": 100 + order,
            "inactive": False,
            "inactive_reason": None,
            "legacy": category,
        }
        write_json(target / f"{category_id.lower()}.json", value)


def research_pages(entry: dict) -> list[dict] | None:
    result = []
    for page in entry["pages"]:
        if page["type"] == "text":
            result.append({"type": "text", "title": "", "body": page["content"]})
            continue
        if page["type"] == "recipe" and page["content"] in RECIPE_MAPPINGS:
            result.append({
                "type": "recipe",
                "title": "",
                "recipe": RECIPE_MAPPINGS[page["content"]],
            })
            continue
        return None
    return result


def materialize_research(
        content_root: Path,
        research: list[dict],
        warp: list[dict],
        mappings: dict[str, str],
        blocks: set[str],
        items: set[str],
) -> Counter:
    target = content_root / "research/legacy"
    if target.exists():
        shutil.rmtree(target)
    warp_by_id = defaultdict(list)
    for value in warp:
        warp_by_id[value["research"]].append(value["amount"])
    counts = Counter()
    for entry in research:
        icon = map_research_icon(entry)
        icon_resource = RESOURCE_ICON_MAPPINGS.get(entry["id"])
        pages = research_pages(entry)
        active = (
            entry["id"] in ACTIVE_RESEARCH
            and (icon is not None or icon_resource is not None)
            and pages is not None
        )
        reason = None
        if not active:
            if entry["id"] not in ACTIVE_RESEARCH:
                reason = "referenced gameplay content is not implemented"
            elif icon is None and icon_resource is None:
                reason = "legacy icon item or resource is not mapped"
            else:
                reason = "one or more research pages use an unmapped recipe or page type"
        modern_pages = pages if pages is not None else [
            (
                {"type": "text", "title": "", "body": page["content"]}
                if page["type"] == "text"
                else {
                    "type": "unavailable",
                    "title": "",
                    "body": "",
                    "legacy_type": page["type"],
                    "legacy_content": page["content"],
                    "source_expression": page["source_expression"],
                }
            )
            for page in entry["pages"]
        ]
        parents = [parent.lower() for parent in entry["parents"]]
        hidden_parents = [parent.lower() for parent in entry["hidden_parents"]]
        concealed = bool(
            entry["flags"]["concealed"]
            or entry["flags"]["hidden"]
            or entry["flags"]["lost"]
        )
        value = {
            "id": entry["id"].lower(),
            "legacy_id": entry["id"],
            "category": entry["category"].lower(),
            "title": f"tc.research_name.{entry['id']}",
            "subtitle": f"tc.research_text.{entry['id']}",
            "concealed": concealed,
            "auto_unlock": entry["flags"]["auto_unlock"],
            "virtual": entry["flags"]["virtual"],
            "parents": parents,
            "hidden_parents": hidden_parents,
            "reveal_when": research_reveal_condition(
                entry,
                mappings,
                blocks,
                items,
            ),
            "x": entry["x"] * 24 if isinstance(entry["x"], int) else entry["x"],
            "y": entry["y"] * 24 if isinstance(entry["y"], int) else entry["y"],
            "pages": modern_pages,
            "inactive": not active,
            "inactive_reason": reason,
            "legacy": {
                "x": entry["x"],
                "y": entry["y"],
                "complexity": entry["complexity"],
                "icon_raw": entry["icon_raw"],
                "flags": entry["flags"],
                "hidden_parents": entry["hidden_parents"],
                "siblings": entry["siblings"],
                "item_triggers_raw": entry["item_triggers_raw"],
                "entity_triggers": entry["entity_triggers"],
                "aspect_triggers": entry["aspect_triggers"],
                "research_aspects": entry["research_aspects"],
                "warp": warp_by_id[entry["id"]],
                "source_expression": entry["source_expression"],
            },
        }
        if entry["flags"]["secondary"]:
            value["purchase_cost"] = entry["research_aspects"]
        if icon_resource is not None:
            value["icon_resource"] = icon_resource
        else:
            value["icon"] = icon or "thaumic_reborn:thaumonomicon"
        write_json(target / f"{slug(entry['id'])}.json", value)
        counts["active" if active else "inactive"] += 1
    return counts


def scan_value(
        *,
        active: bool,
        target_type: str,
        target: str,
        aspects: list[dict],
        reason: str | None,
        legacy: dict,
) -> dict:
    return {
        "type": target_type,
        "target": target,
        "display": "",
        "aspects": combine_aspects(aspects),
        "inactive": not active,
        "inactive_reason": reason,
        "legacy": legacy,
    }


def materialize_scans(
        content_root: Path,
        objects: list[dict],
        entities: list[dict],
        mappings: dict[str, str],
        blocks: set[str],
        items: set[str],
) -> Counter:
    target = content_root / "scans/legacy"
    if target.exists():
        shutil.rmtree(target)
    candidates: list[tuple[str, str] | None] = []
    for entry in objects:
        mapped = None
        if entry["registration_mode"] == "explicit" and entry["target_kind"] == "item_stack":
            mapped = map_stack(entry["legacy_target"], mappings, blocks, items)
        candidates.append(mapped)
    collisions = Counter(value for value in candidates if value is not None)
    counts = Counter()

    for index, (entry, mapped) in enumerate(zip(objects, candidates)):
        active = mapped is not None
        reason = None
        if entry["registration_mode"] != "explicit":
            active = False
            reason = "recipe-derived base aspects cannot yet be reproduced safely"
        elif mapped is None:
            active = False
            reason = "legacy item, metadata, or Ore Dictionary target is not mapped"
        elif collisions[mapped] > 1:
            active = False
            reason = "multiple metadata registrations collapse to one modern target"
        elif mapped in RESERVED_SCAN_TARGETS:
            active = False
            reason = "represented by an existing hand-authored modern scan"
        target_type, modern_target = mapped or ("legacy_object", entry["legacy_target"])
        value = scan_value(
            active=active,
            target_type=target_type,
            target=modern_target,
            aspects=entry["aspects"],
            reason=reason,
            legacy=entry,
        )
        write_json(target / f"object_{index:03d}_{slug(str(entry['legacy_target']))[:48]}.json", value)
        counts["active" if active else "inactive"] += 1

    entity_candidates = Counter(
        ("entity", entry["modern_target"])
        for entry in entities
        if entry["modern_target"] and not entry["nbt_conditions_raw"]
    )
    for index, entry in enumerate(entities):
        mapped = entry["modern_target"]
        active = mapped is not None and not entry["nbt_conditions_raw"]
        reason = None
        if mapped is None:
            reason = "legacy entity type is not implemented or mapped"
        elif entry["nbt_conditions_raw"]:
            active = False
            reason = "NBT-conditional entity aspects are not supported"
        elif entity_candidates[("entity", mapped)] > 1:
            active = False
            reason = "multiple legacy registrations collapse to one modern entity"
        elif ("entity", mapped) in RESERVED_SCAN_TARGETS:
            active = False
            reason = "represented by an existing hand-authored modern scan"
        value = scan_value(
            active=active,
            target_type="entity" if mapped else "legacy_entity",
            target=mapped or entry["legacy_entity_id"],
            aspects=entry["aspects"],
            reason=reason,
            legacy=entry,
        )
        write_json(target / f"entity_{index:03d}_{slug(entry['legacy_entity_id'])}.json", value)
        counts["active" if active else "inactive"] += 1
    return counts


def materialize_recipes(content_root: Path, recipes: list[dict]) -> Counter:
    target = content_root / "recipes_legacy"
    if target.exists():
        shutil.rmtree(target)
    counts = Counter()
    seen_ids = Counter()
    for index, entry in enumerate(recipes):
        seen_ids[entry["id"]] += 1
        modern = RECIPE_MAPPINGS.get(entry["id"])
        active = modern is not None
        value = {
            "id": slug(entry["id"]),
            "legacy_id": entry["id"],
            "legacy_kind": entry["kind"],
            "inactive": not active,
            "inactive_reason": None if active else "recipe type or referenced content is not implemented",
            "modern_recipe": modern or "",
            "required_aspects": entry["required_aspects"],
            "legacy": {
                "source_expression": entry["source_expression"],
                "source_statement": entry["source_statement"],
                "unkeyed": entry.get("unkeyed", False),
            },
        }
        suffix = f"_{seen_ids[entry['id']]:02d}" if seen_ids[entry["id"]] > 1 else ""
        write_json(target / f"{index:03d}_{slug(entry['id'])}{suffix}.json", value)
        counts["active" if active else "inactive"] += 1
    return counts


def merge_languages(archive: Path, assets_root: Path) -> None:
    for legacy_locale, modern_locale in (("en_US", "en_us"), ("ru_RU", "ru_ru")):
        legacy = load_json(archive / f"lang/{legacy_locale}.json")
        path = assets_root / f"{modern_locale}.json"
        modern = load_json(path)
        for key, value in legacy.items():
            modern.setdefault(key, value)
        write_json(path, modern)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--archive", required=True, type=Path)
    parser.add_argument("--content-root", required=True, type=Path)
    parser.add_argument("--lang-root", required=True, type=Path)
    parser.add_argument("--mcp-fields", required=True, type=Path)
    parser.add_argument("--minecraft-assets-jar", required=True, type=Path)
    args = parser.parse_args()

    archive = args.archive.resolve()
    content_root = args.content_root.resolve()
    mappings = field_names(args.mcp_fields.resolve())
    blocks, items = modern_assets(args.minecraft_assets_jar.resolve())

    categories = load_json(archive / "research_categories.json")
    research = load_json(archive / "research.json")
    warp = load_json(archive / "research_warp.json")
    objects = load_json(archive / "object_aspects.json")
    entities = load_json(archive / "entity_aspects.json")
    recipes = load_json(archive / "recipes.json")

    materialize_categories(content_root, categories)
    research_counts = materialize_research(
        content_root,
        research,
        warp,
        mappings,
        blocks,
        items,
    )
    scan_counts = materialize_scans(content_root, objects, entities, mappings, blocks, items)
    recipe_counts = materialize_recipes(content_root, recipes)
    merge_languages(archive, args.lang_root.resolve())

    manifest = {
        "format": "thaumic_reborn.materialized-legacy-content",
        "format_version": 1,
        "research": dict(research_counts),
        "scans": dict(scan_counts),
        "recipes": dict(recipe_counts),
        "active_research_ids": sorted(value.lower() for value in ACTIVE_RESEARCH),
        "rule": (
            "inactive scans are skipped; classic concealed flags are preserved; "
            "virtual research is retained for progression but hidden from the UI; "
            "classic hidden/lost research additionally requires its clue"
        ),
    }
    write_json(content_root / "legacy_content_manifest.json", manifest)
    print(json.dumps(manifest, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
