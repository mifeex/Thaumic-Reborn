#!/usr/bin/env python3
"""Synchronize restored executable infusion recipes with Thaumonomicon pages."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RESEARCH = ROOT / "src/main/resources/data/thaumic_reborn/thaumcraft/research/legacy"
RECIPES = ROOT / "src/main/resources/data/thaumic_reborn/thaumcraft/infusion_recipes"

mapping = {
    "Mirror": "magic_mirror", "MirrorHand": "hand_mirror",
    "MirrorEssentia": "essentia_mirror",
    "ThaumiumFortressHelm": "fortress_helmet",
    "ThaumiumFortressChest": "fortress_chestplate",
    "ThaumiumFortressLegs": "fortress_leggings",
    "HelmGoggles": "fortress_helmet_goggles",
    "MaskGrinningDevil": "fortress_mask_grinning_devil",
    "MaskAngryGhost": "fortress_mask_angry_ghost",
    "MaskSippingFiend": "fortress_mask_sipping_fiend",
}
enchant_order = [
    "repair", "haste", "protection", "fire_protection", "blast_protection",
    "projectile_protection", "feather_falling", "respiration", "aqua_affinity",
    "thorns", "sharpness", "smite", "bane_of_arthropods", "knockback",
    "fire_aspect", "looting", "efficiency", "silk_touch", "unbreaking",
    "fortune", "power", "punch", "flame", "infinity"
]
mapping.update({("InfEnchRepair" if i == 0 else "InfEnchHaste" if i == 1
                 else f"InfEnch{i-2}"): f"infusion_enchant_{name}"
                for i, name in enumerate(enchant_order)})

representative = {
    **{name: "minecraft:iron_chestplate" for name in enchant_order[2:6]},
    "feather_falling": "minecraft:iron_boots", "respiration": "minecraft:iron_helmet",
    "aqua_affinity": "minecraft:iron_helmet", "thorns": "minecraft:iron_chestplate",
    **{name: "minecraft:iron_sword" for name in enchant_order[10:16]},
    **{name: "minecraft:iron_pickaxe" for name in enchant_order[16:20]},
    **{name: "minecraft:bow" for name in enchant_order[20:]},
    "repair": "thaumic_reborn:thaumium_pickaxe",
    "haste": "thaumic_reborn:boots_of_the_traveller",
}

def instability(value):
    if value <= 0: return "negligible"
    if value == 1: return "minor"
    if value == 2: return "moderate"
    if value == 3: return "high"
    if value <= 5: return "very_high"
    return "dangerous"

def page(recipe_name):
    recipe = json.loads((RECIPES / f"{recipe_name}.json").read_text())
    modifier = recipe.get("result_modifier")
    if "result" in recipe:
        output = recipe["result"]["item"]
        central = recipe["central"]["item"]
    elif modifier and modifier["type"] == "enchantment":
        short = recipe_name.removeprefix("infusion_enchant_")
        output = central = representative[short]
    else:
        output = central = recipe["central"]["item"]
    return {
        "type": "infusion", "title": "recipe.type.infusion",
        "output": output, "central": central,
        "components": [{**component, "count": 1}
                       for component in recipe["components"]],
        "aspect_costs": [{"id": aspect, "amount": amount}
                         for aspect, amount in recipe["essentia"].items()],
        "instability": instability(recipe["instability"]),
        "detail": "tc.research_text.INFUSIONENCHANTMENT"
                  if modifier and modifier["type"] == "enchantment"
                  else "recipe.type.infusion"
    }

files = ["mirror", "mirrorhand", "mirroressentia", "infusionenchantment",
         "armorfortress", "helmgoggles", "maskgrinningdevil",
         "maskangryghost", "masksippingfiend"]
icons = {
    "mirror": "thaumic_reborn:magic_mirror",
    "mirrorhand": "thaumic_reborn:hand_mirror",
    "mirroressentia": "thaumic_reborn:essentia_mirror",
    "armorfortress": "thaumic_reborn:fortress_helmet",
    "helmgoggles": "thaumic_reborn:fortress_helmet",
}
for name in files:
    path = RESEARCH / f"{name}.json"
    data = json.loads(path.read_text())
    data["pages"] = [page(mapping[p["legacy_content"]])
                     if p.get("type") == "unavailable"
                     and p.get("legacy_content") in mapping else p
                     for p in data["pages"]]
    data["inactive"] = False
    data["inactive_reason"] = None
    if name in icons: data["icon"] = icons[name]
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n")
print(f"activated {len(files)} research entries")
