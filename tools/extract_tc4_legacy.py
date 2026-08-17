#!/usr/bin/env python3
"""Extract Thaumcraft 4.2.3.5 registration data from ForgeFlower sources.

The old mod builds its registries imperatively. This exporter deliberately keeps
unresolved Java expressions alongside normalized fields, so migration never
silently invents a 1.20.1 item or metadata mapping.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import shutil
import subprocess
import tempfile
import zipfile
from pathlib import Path


ASPECT_FIELDS = {
    "AIR": "aer", "EARTH": "terra", "FIRE": "ignis", "WATER": "aqua",
    "ORDER": "ordo", "ENTROPY": "perditio", "VOID": "vacuos",
    "LIGHT": "lux", "WEATHER": "tempestas", "MOTION": "motus",
    "COLD": "gelum", "CRYSTAL": "vitreus", "LIFE": "victus",
    "POISON": "venenum", "ENERGY": "potentia", "EXCHANGE": "permutatio",
    "METAL": "metallum", "DEATH": "mortuus", "FLIGHT": "volatus",
    "DARKNESS": "tenebrae", "SOUL": "spiritus", "HEAL": "sano",
    "TRAVEL": "iter", "ELDRITCH": "alienis", "MAGIC": "praecantatio",
    "AURA": "auram", "TAINT": "vitium", "SLIME": "limus", "PLANT": "herba",
    "TREE": "arbor", "BEAST": "bestia", "FLESH": "corpus",
    "UNDEAD": "exanimis", "MIND": "cognitio", "SENSES": "sensus",
    "MAN": "humanus", "CROP": "messis", "MINE": "perfodio",
    "TOOL": "instrumentum", "HARVEST": "meto", "WEAPON": "telum",
    "ARMOR": "tutamen", "HUNGER": "fames", "GREED": "lucrum",
    "CRAFT": "fabrico", "CLOTH": "pannus", "MECHANISM": "machina",
    "TRAP": "vinculum",
}

VANILLA_ENTITY_IDS = {
    "Zombie": "minecraft:zombie", "Giant": "minecraft:giant",
    "Skeleton": "minecraft:skeleton", "Creeper": "minecraft:creeper",
    "EntityHorse": "minecraft:horse", "Pig": "minecraft:pig",
    "XPOrb": "minecraft:experience_orb", "Sheep": "minecraft:sheep",
    "Cow": "minecraft:cow", "MushroomCow": "minecraft:mooshroom",
    "SnowMan": "minecraft:snow_golem", "Ozelot": "minecraft:ocelot",
    "Chicken": "minecraft:chicken", "Squid": "minecraft:squid",
    "Wolf": "minecraft:wolf", "Bat": "minecraft:bat",
    "Boat": "minecraft:boat", "Spider": "minecraft:spider",
    "Slime": "minecraft:slime", "Ghast": "minecraft:ghast",
    "PigZombie": "minecraft:zombified_piglin", "Enderman": "minecraft:enderman",
    "CaveSpider": "minecraft:cave_spider", "Silverfish": "minecraft:silverfish",
    "Blaze": "minecraft:blaze", "LavaSlime": "minecraft:magma_cube",
    "EnderDragon": "minecraft:ender_dragon", "WitherBoss": "minecraft:wither",
    "Witch": "minecraft:witch", "Villager": "minecraft:villager",
    "VillagerGolem": "minecraft:iron_golem",
    "MinecartRideable": "minecraft:minecart",
    "MinecartChest": "minecraft:chest_minecart",
    "MinecartFurnace": "minecraft:furnace_minecart",
    "MinecartTNT": "minecraft:tnt_minecart",
    "MinecartHopper": "minecraft:hopper_minecart",
    "MinecartSpawner": "minecraft:spawner_minecart",
    "EnderCrystal": "minecraft:end_crystal", "ItemFrame": "minecraft:item_frame",
    "Painting": "minecraft:painting",
}

EXPECTED_COUNTS = {
    "3dba9786966974701578a658d1bb369bf35bdf5f363079f5ac9c4910a39113be": {
        "aspects": 48,
        "object_aspect_registrations": 326,
        "entity_aspect_registrations": 69,
        "research_categories": 6,
        "research": 201,
        "research_warp_registrations": 20,
        "recipes": 307,
    },
}


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def write_json(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def split_top_level(text: str, delimiter: str = ",", track_braces: bool = True) -> list[str]:
    result: list[str] = []
    start = 0
    depths = {"(": 0, "[": 0}
    if track_braces:
        depths["{"] = 0
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
        elif char in closing and closing[char] in depths:
            depths[closing[char]] -= 1
        elif char == delimiter and not any(depths.values()):
            result.append(text[start:index].strip())
            start = index + 1
    result.append(text[start:].strip())
    return result


def statements(text: str) -> list[str]:
    return [
        part.strip() + ";"
        for part in split_top_level(text, ";", track_braces=False)
        if part.strip()
    ]


def call_arguments(text: str, marker: str, start: int = 0) -> tuple[list[str], int] | None:
    position = text.find(marker, start)
    if position < 0:
        return None
    open_paren = position + len(marker) - 1
    depth = 0
    quote = ""
    escaped = False
    for index in range(open_paren, len(text)):
        char = text[index]
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
        elif char == "(":
            depth += 1
        elif char == ")":
            depth -= 1
            if depth == 0:
                return split_top_level(text[open_paren + 1:index]), index + 1
    return None


def unquote(value: str) -> str:
    value = value.strip()
    if len(value) >= 2 and value[0] == value[-1] == '"':
        return bytes(value[1:-1], "utf-8").decode("unicode_escape")
    return value


def aspect_values(expression: str) -> list[dict[str, object]]:
    values = []
    for name, amount in re.findall(r"\.(?:add|merge)\(Aspect\.([A-Z]+),\s*([^)]+)\)", expression):
        parsed: int | str
        try:
            parsed = int(amount.strip())
        except ValueError:
            parsed = amount.strip()
        values.append({"id": ASPECT_FIELDS.get(name, name.lower()), "amount": parsed})
    return values


def aspect_operations(expression: str) -> list[dict[str, object]]:
    values = []
    for operation, name, amount in re.findall(
            r"\.(add|merge|remove)\(Aspect\.([A-Z]+),\s*([^)]+)\)",
            expression,
    ):
        parsed: int | str
        try:
            parsed = int(amount.strip())
        except ValueError:
            parsed = amount.strip()
        values.append({
            "operation": operation,
            "id": ASPECT_FIELDS.get(name, name.lower()),
            "amount": parsed,
        })
    return values


def string_arguments(statement: str, method: str) -> list[str]:
    found = call_arguments(statement, f".{method}(")
    if not found:
        return []
    return [unquote(value) for value in found[0]]


def decompile(jar: Path, decompiler: Path, work: Path) -> Path:
    subprocess.run([
        "java", "-jar", str(decompiler), "-din=1", "-rbr=1", "-dgs=1",
        str(jar), str(work),
    ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.STDOUT)
    output_jar = work / jar.name
    source_root = work / "sources"
    with zipfile.ZipFile(output_jar) as archive:
        wanted = (
            "thaumcraft/api/aspects/Aspect.java",
            "thaumcraft/common/config/ConfigAspects.java",
            "thaumcraft/common/config/ConfigResearch.java",
            "thaumcraft/common/config/ConfigRecipes.java",
        )
        for name in wanted:
            archive.extract(name, source_root)
    return source_root


def parse_aspects(source: str) -> list[dict[str, object]]:
    result = []
    pattern = re.compile(
        r'public static final Aspect ([A-Z]+) = new Aspect\("([^"]+)",\s*(\d+),\s*(.*)\);'
    )
    for order, match in enumerate(pattern.finditer(source)):
        field, aspect_id, color, tail = match.groups()
        components = []
        component_match = re.search(r"new Aspect\[\]\{([^}]*)\}", tail)
        if component_match:
            components = [
                ASPECT_FIELDS.get(name.strip().removeprefix("Aspect."), name.strip().lower())
                for name in component_match.group(1).split(",")
            ]
        blend_match = re.search(r",\s*(\d+)\s*$", tail)
        result.append({
            "id": aspect_id,
            "legacy_field": field,
            "order": order,
            "color": f"{int(color):06X}",
            "components": components,
            "blend": int(blend_match.group(1)) if blend_match else 1,
            "icon": f"thaumcraft:textures/aspects/{aspect_id}.png",
        })
    return result


def parse_aspect_registrations(source: str) -> tuple[list[dict], list[dict]]:
    entities: list[dict] = []
    objects: list[dict] = []
    for statement in statements(source):
        entity_call = call_arguments(statement, "ThaumcraftApi.registerEntityTag(")
        if entity_call:
            args = entity_call[0]
            if len(args) >= 2:
                legacy_id = unquote(args[0])
                entry = {
                    "legacy_entity_id": legacy_id,
                    "modern_target": VANILLA_ENTITY_IDS.get(legacy_id),
                    "aspects": aspect_values(args[1]),
                    "nbt_conditions_raw": args[2:],
                    "source_expression": statement.removesuffix(";").strip(),
                }
                entities.append(entry)
            continue
        registration_mode = "explicit"
        object_call = call_arguments(statement, "ThaumcraftApi.registerObjectTag(")
        if not object_call:
            object_call = call_arguments(
                statement,
                "ThaumcraftApi.registerComplexObjectTag("
            )
            registration_mode = "recipe_derived_modifier"
        if object_call:
            args = object_call[0]
            if len(args) >= 2:
                is_ore = args[0].startswith('"')
                objects.append({
                    "registration_mode": registration_mode,
                    "target_kind": "ore_dictionary" if is_ore else "item_stack",
                    "legacy_target": unquote(args[0]) if is_ore else args[0],
                    "metadata_group_raw": args[1] if len(args) >= 3 else None,
                    "aspects": aspect_values(args[-1]),
                    "aspect_operations": aspect_operations(args[-1]),
                    "source_expression": statement.removesuffix(";").strip(),
                })
    return objects, entities


def parse_page(expression: str) -> dict[str, object]:
    found = call_arguments(expression, "new ResearchPage(")
    args = found[0] if found else []
    raw = args[0] if args else ""
    if len(args) == 1 and raw.startswith('"'):
        page_type = "text"
        content = unquote(raw)
    elif "ResourceLocation" in raw:
        page_type = "image"
        content = raw
    elif "AspectList" in raw:
        page_type = "aspects"
        content = raw
    else:
        page_type = "recipe"
        recipe_match = re.search(r'recipes\.get\("([^"]+)"\)', raw)
        content = recipe_match.group(1) if recipe_match else raw
    return {"type": page_type, "content": content, "source_expression": expression}


def parse_research(source: str) -> list[dict[str, object]]:
    result = []
    for statement in statements(source):
        constructor = call_arguments(statement, "new ResearchItem(")
        if not constructor:
            continue
        args = constructor[0]
        virtual_constructor = len(args) == 2
        if len(args) == 2:
            args += ["new AspectList()", "0", "0", "0", ""]
        if len(args) < 7:
            continue
        flags = {
            name: f".set{name}(" in statement
            for name in (
                "Special", "Secondary", "Round", "Stub", "Virtual", "Concealed",
                "Hidden", "Lost", "AutoUnlock",
            )
        }
        flags["Virtual"] = virtual_constructor or flags["Virtual"]
        pages_call = call_arguments(statement, ".setPages(")
        pages = [parse_page(page) for page in pages_call[0]] if pages_call else []
        result.append({
            "id": unquote(args[0]),
            "category": unquote(args[1]),
            "research_aspects": aspect_values(args[2]),
            "x": int(args[3]) if re.fullmatch(r"-?\d+", args[3]) else args[3],
            "y": int(args[4]) if re.fullmatch(r"-?\d+", args[4]) else args[4],
            "complexity": int(args[5]) if re.fullmatch(r"\d+", args[5]) else args[5],
            "icon_raw": args[6],
            "parents": string_arguments(statement, "setParents"),
            "hidden_parents": string_arguments(statement, "setParentsHidden"),
            "siblings": string_arguments(statement, "setSiblings"),
            "item_triggers_raw": (call_arguments(statement, ".setItemTriggers(") or ([], 0))[0],
            "entity_triggers": string_arguments(statement, "setEntityTriggers"),
            "aspect_triggers": [
                ASPECT_FIELDS.get(value.removeprefix("Aspect."), value.lower())
                for value in string_arguments(statement, "setAspectTriggers")
            ],
            "flags": {re.sub(r"(?<!^)(?=[A-Z])", "_", key).lower(): value for key, value in flags.items()},
            "pages": pages,
            "source_expression": statement.removesuffix(";").strip(),
        })
    return result


def parse_categories(source: str) -> list[dict[str, object]]:
    result = []
    for statement in statements(source):
        found = call_arguments(statement, "ResearchCategories.registerCategory(")
        if found and len(found[0]) >= 3:
            result.append({
                "id": unquote(found[0][0]),
                "icon_raw": found[0][1],
                "background_raw": found[0][2],
                "source_expression": statement.removesuffix(";").strip(),
            })
    return result


def parse_research_warp(source: str) -> list[dict[str, object]]:
    result = []
    for statement in statements(source):
        found = call_arguments(statement, "ThaumcraftApi.addWarpToResearch(")
        if found and len(found[0]) >= 2:
            amount: int | str = found[0][1]
            if re.fullmatch(r"\d+", found[0][1]):
                amount = int(found[0][1])
            result.append({
                "research": unquote(found[0][0]),
                "amount": amount,
                "source_expression": statement.removesuffix(";").strip(),
            })
    return result


def recipe_kind(expression: str) -> str:
    rules = (
        ("addCrucibleRecipe", "crucible"),
        ("addInfusionEnchantmentRecipe", "infusion_enchantment"),
        ("addInfusionCraftingRecipe", "infusion"),
        ("addArcaneCraftingRecipe", "arcane_shaped"),
        ("addShapelessArcaneCraftingRecipe", "arcane_shapeless"),
        ("oreDictRecipe", "crafting_shaped_ore"),
        ("shapelessOreDictRecipe", "crafting_shapeless_ore"),
        ("shapelessNBTOreRecipe", "crafting_shapeless_nbt_ore"),
        ("addShapedRecipe", "crafting_shaped"),
        ("addShapelessRecipe", "crafting_shapeless"),
        ("Arrays.asList", "multiblock"),
    )
    return next((kind for marker, kind in rules if marker in expression), "legacy_java")


def parse_recipes(source: str) -> list[dict[str, object]]:
    result = []
    unkeyed = 0
    unkeyed_markers = (
        "GameRegistry.addRecipe(",
        "GameRegistry.addShapedRecipe(",
        "GameRegistry.addShapelessRecipe(",
        "GameRegistry.addSmelting(",
        "FurnaceRecipes.func_77602_a().func_151394_a(",
        "CraftingManager.func_77594_a().func_92103_a(",
    )
    for statement in statements(source):
        found = call_arguments(statement, "ConfigResearch.recipes.put(")
        if found and len(found[0]) >= 2:
            key, expression = found[0][0], found[0][1]
            result.append({
                "id": unquote(key),
                "kind": recipe_kind(expression),
                "required_aspects": aspect_values(expression),
                "source_expression": expression,
                "source_statement": statement.removesuffix(";").strip(),
            })
            continue
        marker = next((value for value in unkeyed_markers if value in statement), None)
        if marker:
            found = call_arguments(statement, marker)
            if not found:
                continue
            unkeyed += 1
            expression = marker + ", ".join(found[0]) + ")"
            result.append({
                "id": f"unkeyed_{unkeyed:03d}",
                "kind": "smelting" if "Smelting" in marker or "FurnaceRecipes" in marker
                else recipe_kind(expression),
                "required_aspects": aspect_values(expression),
                "source_expression": expression,
                "source_statement": statement.removesuffix(";").strip(),
                "unkeyed": True,
            })
    return result


def parse_lang(raw: bytes) -> dict[str, str]:
    result = {}
    for line in raw.decode("utf-8", errors="replace").splitlines():
        if not line or line.lstrip().startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        result[key] = value
    return result


def modern_research_entry(entry: dict[str, object]) -> dict[str, object]:
    pages = []
    for page in entry["pages"]:
        if page["type"] == "text":
            pages.append({"type": "text", "title": "", "body": page["content"]})
        else:
            pages.append({
                "type": "legacy_unresolved",
                "legacy_type": page["type"],
                "legacy_content": page["content"],
                "source_expression": page["source_expression"],
            })
    return {
        "id": str(entry["id"]).lower(),
        "legacy_id": entry["id"],
        "category": str(entry["category"]).lower(),
        "icon": "thaumic_reborn:thaumonomicon",
        "title": f"tc.research_name.{entry['id']}",
        "subtitle": f"tc.research_text.{entry['id']}",
        "concealed": entry["flags"]["concealed"],
        "auto_unlock": entry["flags"]["auto_unlock"],
        "virtual": entry["flags"]["virtual"],
        "parents": [str(parent).lower() for parent in entry["parents"]],
        "x": entry["x"],
        "y": entry["y"],
        "pages": pages,
        "migration_status": "requires_content_mapping",
        "legacy": {
            "flags": entry["flags"],
            "hidden_parents": entry["hidden_parents"],
            "research_aspects": entry["research_aspects"],
            "icon_raw": entry["icon_raw"],
        },
    }


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", required=True, type=Path)
    parser.add_argument("--decompiler", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--modern-aspects", type=Path)
    parser.add_argument("--modern-textures", type=Path)
    args = parser.parse_args()

    jar = args.jar.resolve()
    output = args.output.resolve()
    if output.exists():
        shutil.rmtree(output)
    output.mkdir(parents=True)

    with tempfile.TemporaryDirectory(prefix="tc4-export-") as temp_name:
        source_root = decompile(jar, args.decompiler.resolve(), Path(temp_name))
        aspect_source = (source_root / "thaumcraft/api/aspects/Aspect.java").read_text(encoding="utf-8")
        tags_source = (source_root / "thaumcraft/common/config/ConfigAspects.java").read_text(encoding="utf-8")
        research_source = (source_root / "thaumcraft/common/config/ConfigResearch.java").read_text(encoding="utf-8")
        recipes_source = (source_root / "thaumcraft/common/config/ConfigRecipes.java").read_text(encoding="utf-8")

        aspects = parse_aspects(aspect_source)
        object_tags, entity_tags = parse_aspect_registrations(tags_source)
        categories = parse_categories(research_source)
        research = parse_research(research_source)
        research_warp = parse_research_warp(research_source)
        recipes = parse_recipes(recipes_source)

    source_hash = sha256(jar)
    counts = {
        "aspects": len(aspects),
        "object_aspect_registrations": len(object_tags),
        "entity_aspect_registrations": len(entity_tags),
        "research_categories": len(categories),
        "research": len(research),
        "research_warp_registrations": len(research_warp),
        "recipes": len(recipes),
    }
    expected = EXPECTED_COUNTS.get(source_hash)
    if expected is not None and counts != expected:
        raise RuntimeError(f"extraction count mismatch: expected {expected}, got {counts}")

    manifest = {
        "format": "thaumic_reborn.tc4-legacy-export",
        "format_version": 1,
        "source": jar.name,
        "source_sha256": source_hash,
        "counts": counts,
        "notes": [
            "source_expression fields are intentional lossless migration evidence",
            "null modern_target means that no automatic 1.20.1 mapping was asserted",
            "conditional Java registrations remain present with their original expression",
        ],
    }
    write_json(output / "manifest.json", manifest)
    write_json(output / "archive/aspects.json", aspects)
    write_json(output / "archive/object_aspects.json", object_tags)
    write_json(output / "archive/entity_aspects.json", entity_tags)
    write_json(output / "archive/research_categories.json", categories)
    write_json(output / "archive/research.json", research)
    write_json(output / "archive/research_warp.json", research_warp)
    write_json(output / "archive/recipes.json", recipes)
    write_json(output / "modern_migration/aspects.json", [
        {
            "id": aspect["id"],
            "order": int(aspect["order"]) * 10,
            "color": aspect["color"],
            "icon": f"thaumic_reborn:textures/aspects/{aspect['id']}.png",
            "components": aspect["components"],
            "migration_status": "active",
        }
        for aspect in aspects
    ])
    write_json(output / "modern_migration/research.json", [modern_research_entry(value) for value in research])
    write_json(output / "modern_migration/entity_scans.json", [
        {
            "type": "entity",
            "target": value["modern_target"],
            "aspects": value["aspects"],
            "legacy_entity_id": value["legacy_entity_id"],
            "migration_status": "mapped" if value["modern_target"] else "requires_target_mapping",
            **({"nbt_conditions_raw": value["nbt_conditions_raw"]} if value["nbt_conditions_raw"] else {}),
        }
        for value in entity_tags
    ])
    write_json(output / "modern_migration/object_scans.json", [
        {
            "legacy_target_kind": value["target_kind"],
            "legacy_target": value["legacy_target"],
            "aspects": value["aspects"],
            "migration_status": "requires_target_mapping",
            **({"metadata_group_raw": value["metadata_group_raw"]} if value["metadata_group_raw"] else {}),
        }
        for value in object_tags
    ])
    write_json(output / "modern_migration/recipes.json", [
        {**value, "migration_status": "requires_recipe_type_and_item_mapping"}
        for value in recipes
    ])

    with zipfile.ZipFile(jar) as archive:
        for locale in ("en_US", "ru_RU"):
            raw = archive.read(f"assets/thaumcraft/lang/{locale}.lang")
            write_json(output / f"archive/lang/{locale}.json", parse_lang(raw))
        if args.modern_textures:
            args.modern_textures.mkdir(parents=True, exist_ok=True)
            for aspect in aspects:
                name = f"assets/thaumcraft/textures/aspects/{aspect['id']}.png"
                (args.modern_textures / f"{aspect['id']}.png").write_bytes(archive.read(name))

    if args.modern_aspects:
        args.modern_aspects.mkdir(parents=True, exist_ok=True)
        for aspect in aspects:
            write_json(args.modern_aspects / f"{aspect['id']}.json", {
                "id": aspect["id"],
                "order": int(aspect["order"]) * 10,
                "color": aspect["color"],
                "icon": f"thaumic_reborn:textures/aspects/{aspect['id']}.png",
                "components": aspect["components"],
            })

    print(json.dumps(manifest, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
