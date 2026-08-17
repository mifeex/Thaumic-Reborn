#!/usr/bin/env python3
"""Install original TC4 sprites for materialized arcane-recipe items."""

from __future__ import annotations

import json
from pathlib import Path
from zipfile import ZipFile


ROOT = Path(__file__).resolve().parents[1]
JAR = ROOT / "reference/original/Thaumcraft_1.7.10_4.2.3.5.jar"
MODELS = ROOT / "src/main/resources/assets/thaumic_reborn/models/item"
TEXTURES = ROOT / "src/main/resources/assets/thaumic_reborn/textures/item"
ORIGINAL_PREFIX = "assets/thaumcraft/textures/items/"

ICON_MAP = {
    "aer_primal_arrow": "el_arrow_air",
    "aqua_primal_arrow": "el_arrow_water",
    "ignis_primal_arrow": "el_arrow_fire",
    "ordo_primal_arrow": "el_arrow_order",
    "perditio_primal_arrow": "el_arrow_entropy",
    "terra_primal_arrow": "el_arrow_earth",
    "arcane_door": "arcanedoor",
    "blank_golem_core": "golem_core_blank",
    "bone_bow": "bonebow",
    "enchanted_fabric": "cloth",
    "essentia_resonator": "resonator",
    "focus_excavation": "focus_excavation",
    "focus_fire": "focus_fire",
    "focus_frost": "focus_frost",
    "focus_pouch": "focuspouch",
    "focus_primal": "focus_primal",
    "focus_shock": "focus_shock",
    "focus_trade": "focus_trade",
    "gold_key": "keygold",
    "golem_bell": "ironbell",
    "golem_decoration_armor": "golemdecoarmor",
    "golem_decoration_bow_tie": "golemdecobowtie",
    "golem_decoration_dart_launcher": "golemdecodart",
    "golem_decoration_fez": "golemdecofez",
    "golem_decoration_glasses": "golemdecoglasses",
    "golem_decoration_hammer": "golemdecomace",
    "golem_decoration_top_hat": "golemdecotophat",
    "golem_decoration_visor": "golemdecovisor",
    "golem_upgrade_aer": "golem_upgrade_air",
    "golem_upgrade_aqua": "golem_upgrade_water",
    "golem_upgrade_ignis": "golem_upgrade_fire",
    "golem_upgrade_ordo": "golem_upgrade_order",
    "golem_upgrade_perditio": "golem_upgrade_entropy",
    "golem_upgrade_terra": "golem_upgrade_earth",
    "inert_silver_wand_cap": "wand_cap_silver_inert",
    "inert_thaumium_wand_cap": "wand_cap_thaumium_inert",
    "inert_void_wand_cap": "wand_cap_void_inert",
    "iron_key": "keyiron",
    "mirrored_glass": "mirrorglass",
    "primal_charm": "charm",
    "thaumaturge_boots": "clothboots",
    "thaumaturge_leggings": "clothlegs",
    "thaumaturge_robe": "clothchest",
    "thaumium_ingot": "thaumiumingot",
    "void_metal_ingot": "voidingot",
}

ROBE_OVERLAYS = {
    "thaumaturge_boots": "clothbootsover",
    "thaumaturge_leggings": "clothlegsover",
    "thaumaturge_robe": "clothchestover",
}

ROBE_ARMOR_TEXTURES = (
    "robes_1",
    "robes_1_overlay",
    "robes_2",
    "robes_2_overlay",
)


def main() -> None:
    with ZipFile(JAR) as archive:
        names = set(archive.namelist())
        for modern_name, legacy_name in ICON_MAP.items():
            model_path = MODELS / f"{modern_name}.json"
            current = json.loads(model_path.read_text())
            current_texture = current.get("textures", {}).get("layer0")
            if current_texture not in {
                "thaumic_reborn:item/knowledgefragment",
                f"thaumic_reborn:item/{modern_name}",
            }:
                raise RuntimeError(f"Refusing to replace non-placeholder {model_path}")

            legacy_png = f"{ORIGINAL_PREFIX}{legacy_name}.png"
            texture_path = TEXTURES / f"{modern_name}.png"
            texture_path.write_bytes(archive.read(legacy_png))
            legacy_meta = f"{legacy_png}.mcmeta"
            if legacy_meta in names:
                texture_path.with_suffix(".png.mcmeta").write_bytes(
                    archive.read(legacy_meta)
                )

            model_path.write_text(json.dumps({
                "parent": "minecraft:item/generated",
                "textures": {
                    "layer0": f"thaumic_reborn:item/{modern_name}",
                    **({
                        "layer1": f"thaumic_reborn:item/{modern_name}_overlay"
                    } if modern_name in ROBE_OVERLAYS else {}),
                },
            }, indent=2) + "\n")

        for modern_name, legacy_name in ROBE_OVERLAYS.items():
            (TEXTURES / f"{modern_name}_overlay.png").write_bytes(
                archive.read(f"{ORIGINAL_PREFIX}{legacy_name}.png")
            )

        armor_textures = (
            ROOT / "src/main/resources/assets/thaumic_reborn/textures/models"
        )
        for name in ROBE_ARMOR_TEXTURES:
            (armor_textures / f"{name}.png").write_bytes(archive.read(
                f"assets/thaumcraft/textures/models/{name}.png"
            ))

        install_arcane_ear(archive)

    print(f"Installed {len(ICON_MAP)} original TC4 item icons and Arcane Ear model")


def install_arcane_ear(archive: ZipFile) -> None:
    texture_names = {
        "bottom": "arcaneearbottom",
        "top": "arcaneeartopoff",
        "side": "arcaneearsideoff",
        "bell_top": "arcaneearbelltop",
        "bell_side": "arcaneearbellside",
    }
    textures = {}
    for face, legacy_name in texture_names.items():
        modern_name = f"arcane_ear_{face}"
        (TEXTURES / f"{modern_name}.png").write_bytes(archive.read(
            f"assets/thaumcraft/textures/blocks/{legacy_name}.png"
        ))
        textures[face] = f"thaumic_reborn:item/{modern_name}"

    horizontal_base = {
        direction: {"texture": "#side"}
        for direction in ("north", "east", "south", "west")
    }
    horizontal_bell = {
        direction: {"texture": "#bell_side"}
        for direction in ("north", "east", "south", "west")
    }
    model = {
        "parent": "minecraft:block/block",
        "textures": textures,
        "elements": [
            {
                "from": [0, 0, 0],
                "to": [16, 3, 16],
                "faces": {
                    "down": {"texture": "#bottom"},
                    "up": {"texture": "#top"},
                    **horizontal_base,
                },
            },
            {
                "from": [4, 3, 4],
                "to": [12, 16, 12],
                "faces": {
                    "down": {"texture": "#bottom"},
                    "up": {"texture": "#bell_top"},
                    **horizontal_bell,
                },
            },
        ],
    }
    (MODELS / "arcane_ear.json").write_text(
        json.dumps(model, indent=2) + "\n"
    )


if __name__ == "__main__":
    main()
