#!/usr/bin/env python3
"""Generate vanilla tool scans from TC4 4.2.3.5's exact recipe/bonus rules."""

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src/main/resources/data/thaumic_reborn/thaumcraft/scans/vanilla_tools"

# Explicit TC4 ingredient tags registered by ConfigAspects.
MATERIALS = {
    "wooden": {"arbor": 1},
    "stone": {"terra": 1, "perditio": 1},
    "iron": {"metallum": 4},
    "golden": {"metallum": 3, "lucrum": 2},
    "diamond": {"vitreus": 4, "lucrum": 4},
}

# Minecraft 1.7.10 ToolMaterial harvest levels and ItemSword damage + 1.
TIER_BONUS = {"wooden": 1, "stone": 2, "iron": 3, "golden": 1, "diamond": 4}
SWORD_TELUM = {"wooden": 5, "stone": 6, "iron": 7, "golden": 5, "diamond": 8}

# This seemingly unusual order is the literal TC4 getBonusTags durability branch.
HOE_METO = {"wooden": 1, "stone": 3, "iron": 2, "golden": 1, "diamond": 2}

RECIPES = {
    "pickaxe": (3, 2, "perfodio"),
    "axe": (3, 2, "instrumentum"),
    "shovel": (1, 2, "instrumentum"),
    "hoe": (2, 2, "meto"),
    "sword": (2, 1, "telum"),
}


def scaled(amount: int) -> int:
    return int(amount * 0.75)


for material, ingredient_aspects in MATERIALS.items():
    for tool, (material_count, stick_count, bonus_aspect) in RECIPES.items():
        aspects = {
            aspect: scaled(amount * material_count)
            for aspect, amount in ingredient_aspects.items()
        }
        # TC4 includes every recipe ingredient before applying floor(total * 0.75).
        arbor = scaled(stick_count)
        if arbor:
            aspects["arbor"] = arbor
        if tool == "sword":
            aspects[bonus_aspect] = SWORD_TELUM[material]
        elif tool == "hoe":
            aspects[bonus_aspect] = HOE_METO[material]
        else:
            aspects[bonus_aspect] = TIER_BONUS[material]
        aspects = {key: value for key, value in aspects.items() if value > 0}
        target = f"minecraft:{material}_{tool}"
        payload = {
            "type": "item",
            "target": target,
            "display": f"item.minecraft.{material}_{tool}",
            "aspects": [
                {"id": aspect, "amount": amount}
                for aspect, amount in aspects.items()
            ],
            "legacy": {
                "source": "ThaumcraftCraftingManager.generateTags + getBonusTags",
                "version": "Thaumcraft 4.2.3.5 / Minecraft 1.7.10",
            },
        }
        (OUT / f"{material}_{tool}.json").write_text(
            json.dumps(payload, ensure_ascii=False, separators=(",", ":")) + "\n",
            encoding="utf-8",
        )

# Netherite is post-TC4 content. Keep the project's agreed extrapolation:
# the corrected diamond-tool result plus one of every inherited aspect.
for tool in RECIPES:
    diamond = json.loads((OUT / f"diamond_{tool}.json").read_text(encoding="utf-8"))
    payload = {
        "type": "item",
        "target": f"minecraft:netherite_{tool}",
        "display": f"item.minecraft.netherite_{tool}",
        "aspects": [
            {"id": reward["id"], "amount": reward["amount"] + 1}
            for reward in diamond["aspects"]
        ],
        "modern_rule": "Corrected TC4 diamond tool aspects plus one each.",
    }
    (OUT / f"netherite_{tool}.json").write_text(
        json.dumps(payload, ensure_ascii=False, separators=(",", ":")) + "\n",
        encoding="utf-8",
    )
