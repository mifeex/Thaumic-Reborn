#!/usr/bin/env python3
"""Materialize TC4 4.2.3.5 Crucible registrations for the shared runtime catalog.

Every source registration receives a modern JSON record. Recipes whose output
or catalyst gameplay is not implemented stay explicitly inactive instead of
silently producing placeholder items.
"""

from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LEGACY = ROOT / "src/main/resources/data/thaumic_reborn/thaumcraft/recipes_legacy"
OUT = ROOT / "src/main/resources/data/thaumic_reborn/thaumcraft/crucible_recipes"
MANIFEST = ROOT / "data/legacy_tc4_4_2_3_5/modern_migration/crucible_recipes.json"
RESEARCH = ROOT / "src/main/resources/data/thaumic_reborn/thaumcraft/research/legacy"

VANILLA = {
    "altgunpowder": ("minecraft:gunpowder", 2, "minecraft:gunpowder"),
    "altslime": ("minecraft:slime_ball", 2, "minecraft:slime_ball"),
    "altclay": ("minecraft:clay_ball", 2, "minecraft:clay_ball"),
    "altglowstone": ("minecraft:glowstone_dust", 2, "#forge:dusts/glowstone"),
    "altink": ("minecraft:ink_sac", 2, "minecraft:ink_sac"),
    "altweb": ("minecraft:cobweb", 1, "minecraft:string"),
    "altmossycobble": ("minecraft:mossy_cobblestone", 1, "minecraft:cobblestone"),
    "altice": ("minecraft:ice", 1, "minecraft:packed_ice"),
    "altcrackedbrick": ("minecraft:cracked_stone_bricks", 1, "minecraft:stone_bricks"),
    "altbonemeal": ("minecraft:bone_meal", 4, "minecraft:bone"),
    "transiron": ("minecraft:iron_nugget", 3, "#forge:nuggets/iron"),
    "transgold": ("minecraft:gold_nugget", 3, "minecraft:gold_nugget"),
    "transsilver": ("thaumic_reborn:silver_nugget", 3, "#forge:nuggets/silver"),
}

SUPPORTED = {
    "alumentum": ("thaumic_reborn:alumentum", 1, "#minecraft:coals"),
    "nitor": ("thaumic_reborn:nitor", 1, "#forge:dusts/glowstone"),
    "thaumium": ("thaumic_reborn:thaumium_ingot", 1, "#forge:ingots/iron"),
    "voidmetal": ("thaumic_reborn:void_metal_ingot", 1, "thaumic_reborn:void_seed"),
    "voidseed": ("thaumic_reborn:void_seed", 1, "minecraft:wheat_seeds"),
    "etherealbloom": ("thaumic_reborn:ethereal_bloom", 1, "thaumic_reborn:shimmerleaf"),
    **VANILLA,
}

UNSUPPORTED = {
    "tallow": ("thaumic_reborn:thaumic_tallow", 1, "minecraft:rotten_flesh"),
    "pureiron": ("thaumic_reborn:native_iron_cluster", 1, "#forge:ores/iron"),
    "puregold": ("thaumic_reborn:native_gold_cluster", 1, "#forge:ores/gold"),
    "purecopper": ("thaumic_reborn:native_copper_cluster", 1, "#forge:ores/copper"),
    "puretin": ("thaumic_reborn:native_tin_cluster", 1, "#forge:ores/tin"),
    "puresilver": ("thaumic_reborn:native_silver_cluster", 1, "#forge:ores/silver"),
    "purelead": ("thaumic_reborn:native_lead_cluster", 1, "#forge:ores/lead"),
    "transcopper": ("minecraft:copper_ingot", 1, "minecraft:copper_ingot"),
    "transtin": ("thaumic_reborn:tin_nugget", 3, "#forge:nuggets/tin"),
    "translead": ("thaumic_reborn:lead_nugget", 3, "#forge:nuggets/lead"),
    "liquiddeath": ("thaumic_reborn:liquid_death_bucket", 1, "minecraft:bucket"),
    "bottletaint": ("thaumic_reborn:bottled_taint", 1, "minecraft:glass_bottle"),
    "golemstraw": ("thaumic_reborn:straw_golem", 1, "minecraft:hay_block"),
    "golemwood": ("thaumic_reborn:wood_golem", 1, "thaumic_reborn:greatwood_log"),
    "golemtallow": ("thaumic_reborn:tallow_golem", 1, "thaumic_reborn:flesh_block"),
    "golemclay": ("thaumic_reborn:clay_golem", 1, "minecraft:clay"),
    "golemflesh": ("thaumic_reborn:flesh_golem", 1, "thaumic_reborn:flesh_block"),
    "golemstone": ("thaumic_reborn:stone_golem", 1, "minecraft:stone_bricks"),
    "golemiron": ("thaumic_reborn:iron_golem_placer", 1, "minecraft:iron_block"),
    "golemthaumium": ("thaumic_reborn:thaumium_golem", 1, "thaumic_reborn:thaumium_block"),
    "coregather": ("thaumic_reborn:gather_golem_core", 1, "thaumic_reborn:blank_golem_core"),
    "corefill": ("thaumic_reborn:fill_golem_core", 1, "thaumic_reborn:blank_golem_core"),
    "coreempty": ("thaumic_reborn:empty_golem_core", 1, "thaumic_reborn:blank_golem_core"),
    "coreharvest": ("thaumic_reborn:harvest_golem_core", 1, "thaumic_reborn:blank_golem_core"),
    "coreguard": ("thaumic_reborn:guard_golem_core", 1, "thaumic_reborn:blank_golem_core"),
    "corebutcher": ("thaumic_reborn:butcher_golem_core", 1, "thaumic_reborn:guard_golem_core"),
    "coreliquid": ("thaumic_reborn:liquid_golem_core", 1, "thaumic_reborn:blank_golem_core"),
    "bathsalts": ("thaumic_reborn:bath_salts", 1, "thaumic_reborn:salis_mundus"),
    "sanesoap": ("thaumic_reborn:sanity_soap", 1, "thaumic_reborn:flesh_block"),
}

PAGE_GROUPS = {
    "alchemicalduplication": {
        "AltGunpowder": "altgunpowder", "AltSlime": "altslime",
        "AltClay": "altclay", "AltGlowstone": "altglowstone", "AltInk": "altink",
    },
    "alchemicalmanufacture": {
        "AltWeb": "altweb", "AltMossyCobble": "altmossycobble", "AltIce": "altice",
    },
    "entropicprocessing": {
        "AltCrackedBrick": "altcrackedbrick", "AltBonemeal": "altbonemeal",
    },
    "etherealbloom": {"EtherealBloom": "etherealbloom"},
}


def ingredient(value: str) -> dict[str, str]:
    return {"tag": value[1:]} if value.startswith("#") else {"item": value}


def research_key(expression: str) -> str:
    match = re.search(r'addCrucibleRecipe\("([^"]+)"', expression)
    if not match:
        raise ValueError(f"missing research key: {expression}")
    return match.group(1).lower()


def write_recipe(entry: dict, recipe_id: str, mapping: tuple[str, int, str], active: bool) -> None:
    output, count, catalyst = mapping
    aspects = {cost["id"]: cost["amount"] for cost in entry["required_aspects"]}
    data = {
        "research": research_key(entry["legacy"]["source_expression"]),
        "catalyst": ingredient(catalyst),
        "output": {"item": output},
        "aspects": aspects,
        "legacy_id": entry["legacy_id"],
    }
    if count != 1:
        data["output"]["count"] = count
    if not active:
        data["inactive"] = True
        data["inactive_reason"] = (
            "output item or its complete gameplay behavior is not implemented"
        )
    (OUT / f"{recipe_id}.json").write_text(
        json.dumps(data, indent=2, ensure_ascii=False) + "\n"
    )


def balanced(entry: dict) -> list[str]:
    aspects = ("aer", "ignis", "aqua", "terra", "ordo", "perditio")
    shard_names = {
        "aer": "air", "ignis": "fire", "aqua": "water",
        "terra": "earth", "ordo": "order", "perditio": "entropy",
    }
    generated = []
    for catalyst_aspect in aspects:
        recipe_id = f"balanced_{shard_names[catalyst_aspect]}"
        data = {
            "research": "crucible",
            "catalyst": {"item": f"thaumic_reborn:{shard_names[catalyst_aspect]}_shard"},
            "output": {"item": "thaumic_reborn:balanced_shard"},
            "aspects": {aspect: 2 for aspect in aspects if aspect != catalyst_aspect},
            "legacy_id": entry["legacy_id"],
        }
        (OUT / f"{recipe_id}.json").write_text(json.dumps(data, indent=2) + "\n")
        generated.append(recipe_id)
    return generated


def update_research_pages() -> None:
    for research_id, replacements in PAGE_GROUPS.items():
        path = RESEARCH / f"{research_id}.json"
        research = json.loads(path.read_text())
        for page in research["pages"]:
            recipe_id = replacements.get(page.get("legacy_content"))
            if page.get("type") == "unavailable" and recipe_id:
                page.clear()
                page.update({
                    "type": "recipe", "title": "",
                    "recipe": f"thaumic_reborn:{recipe_id}",
                })
        research["inactive"] = False
        research.pop("inactive_reason", None)
        path.write_text(json.dumps(research, indent=2, ensure_ascii=False) + "\n")

    crucible_path = RESEARCH / "crucible.json"
    crucible = json.loads(crucible_path.read_text())
    balanced_groups = {
        "thaumic_reborn:balanced_air": [
            "thaumic_reborn:balanced_air",
            "thaumic_reborn:balanced_fire",
            "thaumic_reborn:balanced_water",
        ],
        "thaumic_reborn:balanced_entropy": [
            "thaumic_reborn:balanced_earth",
            "thaumic_reborn:balanced_order",
            "thaumic_reborn:balanced_entropy",
        ],
    }
    for page in crucible["pages"]:
        recipes = balanced_groups.get(page.get("recipe"))
        if recipes:
            page.pop("recipe")
            page["recipes"] = recipes
    crucible_path.write_text(
        json.dumps(crucible, indent=2, ensure_ascii=False) + "\n"
    )


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    for path in OUT.glob("*.json"):
        path.unlink()

    entries = []
    for path in sorted(LEGACY.glob("*.json")):
        entry = json.loads(path.read_text())
        if entry.get("legacy_kind") == "crucible":
            entries.append((path, entry))

    records = []
    for path, entry in entries:
        recipe_id = entry["id"].removesuffix("__a")
        if recipe_id == "balancedshard":
            ids = balanced(entry)
            active = True
        else:
            mapping = SUPPORTED.get(recipe_id) or UNSUPPORTED.get(recipe_id)
            if mapping is None:
                raise KeyError(f"unmapped crucible recipe {recipe_id}")
            active = recipe_id in SUPPORTED
            write_recipe(entry, recipe_id, mapping, active)
            ids = [recipe_id]

        entry["modern_recipe"] = [f"thaumic_reborn:{value}" for value in ids]
        entry["inactive"] = not active
        entry["inactive_reason"] = None if active else (
            "output item or its complete gameplay behavior is not implemented"
        )
        path.write_text(json.dumps(entry, indent=2, ensure_ascii=False) + "\n")
        records.append({
            "legacy_id": entry["legacy_id"],
            "modern_ids": entry["modern_recipe"],
            "status": "runtime" if active else "inactive",
            "source_expression": entry["legacy"]["source_expression"],
        })

    update_research_pages()
    MANIFEST.write_text(json.dumps({
        "source": "Thaumcraft-1.7.10-4.2.3.5.jar",
        "source_registrations": len(entries),
        "runtime_recipes": sum(r["status"] == "runtime" for r in records) + 5,
        "materialized_recipes": sum(len(r["modern_ids"]) for r in records),
        "recipes": records,
    }, indent=2, ensure_ascii=False) + "\n")
    print(
        f"materialized {len(entries)} registrations as "
        f"{sum(len(r['modern_ids']) for r in records)} recipes"
    )


if __name__ == "__main__":
    main()
