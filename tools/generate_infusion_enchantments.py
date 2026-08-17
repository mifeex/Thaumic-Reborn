#!/usr/bin/env python3
"""Generate the complete TC4 infusion-enchantment recipe table."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src/main/resources/data/thaumic_reborn/thaumcraft/infusion_recipes"
SALIS = "thaumic_reborn:salis_mundus"

# id, enchantment, instability, aspects, exact TC4 pedestal components
RECIPES = [
 ("repair", "thaumic_reborn:repair", 4, {"praecantatio":8,"fabrico":10,"ordo":10}, ["minecraft:anvil",SALIS]),
 ("haste", "thaumic_reborn:haste", 3, {"praecantatio":4,"iter":8,"volatus":8}, ["thaumic_reborn:nitor",SALIS]),
 ("protection", "minecraft:protection", 1, {"praecantatio":4,"tutamen":8}, ["minecraft:iron_ingot",SALIS]),
 ("fire_protection", "minecraft:fire_protection", 1, {"praecantatio":4,"tutamen":4,"ignis":4}, ["minecraft:iron_ingot","minecraft:magma_cream",SALIS]),
 ("blast_protection", "minecraft:blast_protection", 1, {"praecantatio":4,"tutamen":4,"perditio":4}, ["minecraft:iron_ingot","minecraft:gunpowder",SALIS]),
 ("projectile_protection", "minecraft:projectile_protection", 1, {"praecantatio":4,"tutamen":4,"volatus":4}, ["minecraft:iron_ingot","minecraft:arrow",SALIS]),
 ("feather_falling", "minecraft:feather_falling", 1, {"praecantatio":4,"aer":4,"volatus":4}, ["minecraft:feather",SALIS]),
 ("respiration", "minecraft:respiration", 2, {"praecantatio":4,"aer":8,"aqua":8}, ["minecraft:sugar_cane",SALIS]),
 ("aqua_affinity", "minecraft:aqua_affinity", 2, {"praecantatio":4,"motus":8,"aqua":8}, ["minecraft:sugar_cane","minecraft:slime_ball",SALIS]),
 ("thorns", "minecraft:thorns", 2, {"praecantatio":4,"telum":8,"herba":8}, ["minecraft:dead_bush","minecraft:quartz",SALIS]),
 ("sharpness", "minecraft:sharpness", 2, {"praecantatio":4,"telum":8}, ["minecraft:iron_sword",SALIS]),
 ("smite", "minecraft:smite", 2, {"praecantatio":4,"telum":4,"exanimis":4}, ["minecraft:iron_sword","minecraft:glowstone_dust",SALIS]),
 ("bane_of_arthropods", "minecraft:bane_of_arthropods", 2, {"praecantatio":4,"telum":4,"bestia":4}, ["minecraft:iron_sword","thaumic_reborn:amber",SALIS]),
 ("knockback", "minecraft:knockback", 1, {"praecantatio":4,"telum":3,"motus":3}, ["minecraft:piston",SALIS]),
 ("fire_aspect", "minecraft:fire_aspect", 3, {"praecantatio":4,"telum":4,"ignis":8}, ["minecraft:iron_sword","minecraft:blaze_powder",SALIS]),
 ("looting", "minecraft:looting", 3, {"praecantatio":4,"telum":4,"lucrum":8}, ["minecraft:iron_sword","minecraft:diamond",SALIS]),
 ("efficiency", "minecraft:efficiency", 2, {"praecantatio":4,"instrumentum":4,"ordo":4}, ["minecraft:iron_pickaxe",SALIS]),
 ("silk_touch", "minecraft:silk_touch", 5, {"praecantatio":16,"instrumentum":16,"ordo":16,"messis":16,"perfodio":16}, ["minecraft:iron_pickaxe","minecraft:cobweb",SALIS]),
 ("unbreaking", "minecraft:unbreaking", 2, {"praecantatio":4,"instrumentum":4,"ordo":8}, ["minecraft:iron_pickaxe","minecraft:obsidian",SALIS]),
 ("fortune", "minecraft:fortune", 3, {"praecantatio":4,"instrumentum":4,"lucrum":8}, ["minecraft:iron_pickaxe","minecraft:diamond",SALIS]),
 ("power", "minecraft:power", 2, {"praecantatio":4,"telum":8}, ["minecraft:bow",SALIS]),
 ("punch", "minecraft:punch", 2, {"praecantatio":4,"telum":3,"motus":3}, ["minecraft:piston",SALIS]),
 ("flame", "minecraft:flame", 3, {"praecantatio":4,"telum":4,"ignis":8}, ["minecraft:bow","minecraft:blaze_powder",SALIS]),
 ("infinity", "minecraft:infinity", 5, {"praecantatio":8,"telum":16,"vacuos":16,"permutatio":16}, ["minecraft:bow","minecraft:arrow",SALIS]),
]

OUT.mkdir(parents=True, exist_ok=True)
for name, enchantment, instability, aspects, components in RECIPES:
    data = {
        "research": "infusionenchantment",
        "instability": instability,
        "components": [{"item": item} for item in components],
        "essentia": aspects,
        "result_modifier": {"type": "enchantment", "key": enchantment}
    }
    (OUT / f"infusion_enchant_{name}.json").write_text(
        json.dumps(data, indent=2, ensure_ascii=False) + "\n")
print(f"wrote {len(RECIPES)} infusion enchantment recipes")
