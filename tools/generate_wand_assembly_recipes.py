#!/usr/bin/env python3
"""Materialize TC4 dynamic casting-tool combinations for recipe viewers."""

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
RECIPES = ROOT / "src/main/resources/data/thaumic_reborn/recipes"
CAPS = ("iron", "copper", "gold", "silver", "thaumium", "void")
WAND_RODS = (
    "wood", "greatwood", "obsidian", "silverwood", "ice", "quartz",
    "reed", "blaze", "bone",
)
STAFF_RODS = (
    "greatwood_staff", "obsidian_staff", "silverwood_staff", "ice_staff",
    "quartz_staff", "reed_staff", "blaze_staff", "bone_staff",
    "primal_staff",
)


def write_recipe(name: str, recipe_type: str, rod: str, cap: str) -> None:
    path = RECIPES / f"{name}.json"
    path.write_text(json.dumps({
        "type": f"thaumic_reborn:{recipe_type}",
        "rod": rod,
        "cap": cap,
    }, indent=2) + "\n", encoding="utf-8")


for rod in WAND_RODS:
    for cap in CAPS:
        if (rod, cap) != ("wood", "iron"):
            write_recipe(f"wand_{rod}_{cap}", "arcane_wand_assembly", rod, cap)
        write_recipe(
            f"sceptre_{rod}_{cap}",
            "arcane_sceptre_assembly",
            rod,
            cap,
        )

for rod in STAFF_RODS:
    for cap in CAPS:
        write_recipe(f"staff_{rod}_{cap}", "arcane_wand_assembly", rod, cap)
