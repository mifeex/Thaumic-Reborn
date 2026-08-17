#!/usr/bin/env python3
"""Materialize TC4 generateTags results for currently ported recipe outputs."""
from __future__ import annotations

import argparse
import json
import math
import re
import zipfile
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"
SCANS = RES / "data/thaumic_reborn/thaumcraft/scans"


class Tags:
    def __init__(self, jars: list[Path]):
        self.jars = [zipfile.ZipFile(path) for path in jars if path.is_file()]
        self.memo: dict[tuple[str, str], list[str]] = {}

    def resolve(self, tag: str, registry: str = "items") -> list[str]:
        key = (tag, registry)
        if key in self.memo:
            return self.memo[key]
        self.memo[key] = []
        namespace, name = tag.split(":", 1)
        resource = f"data/{namespace}/tags/{registry}/{name}.json"
        values: list[object] = []
        for jar in self.jars:
            try:
                values.extend(json.loads(jar.read(resource))["values"])
            except KeyError:
                pass
        disk = RES / resource
        if disk.is_file():
            values.extend(json.loads(disk.read_text())["values"])
        result: list[str] = []
        for value in values:
            identifier = value if isinstance(value, str) else value.get("id", "")
            if identifier.startswith("#"):
                result.extend(self.resolve(identifier[1:], registry))
            elif identifier:
                result.append(identifier)
        self.memo[key] = list(dict.fromkeys(result))
        return self.memo[key]


def scan_aspects(tags: Tags) -> tuple[dict[str, Counter], set[str]]:
    aspects: dict[str, Counter] = {}
    covered: set[str] = set()
    for path in SCANS.rglob("*.json"):
        value = json.loads(path.read_text())
        if value.get("inactive"):
            continue
        reward = Counter({row["id"]: row["amount"] for row in value.get("aspects", [])})
        target, kind = value.get("target", ""), value.get("type")
        if kind in ("item", "block"):
            aspects[target] = reward
            covered.add(target)
        elif kind in ("item_tag", "block_tag"):
            registry = "items" if kind == "item_tag" else "blocks"
            for identifier in tags.resolve(target, registry):
                aspects.setdefault(identifier, reward)
                covered.add(identifier)
    return aspects, covered


def ingredient(entry: object) -> tuple[str, str] | None:
    if not isinstance(entry, dict):
        return None
    if "item" in entry:
        return "item", entry["item"]
    if "tag" in entry:
        return "tag", entry["tag"]
    return None


def output(value: dict) -> tuple[str, int] | None:
    result = value.get("result") or value.get("output")
    if isinstance(result, str):
        return result, 1
    if isinstance(result, dict) and "item" in result:
        return result["item"], max(1, result.get("count", 1))
    return None


def recipes(tags: Tags) -> list[dict]:
    result: list[dict] = []
    roots = [
        RES / "data/thaumic_reborn/recipes",
        RES / "data/thaumic_reborn/thaumcraft/crucible_recipes",
        RES / "data/thaumic_reborn/thaumcraft/infusion_recipes",
    ]
    sources: list[tuple[str, dict]] = []
    for root in roots:
        for path in root.glob("*.json"):
            sources.append((str(path.relative_to(ROOT)), json.loads(path.read_text())))
    for jar in tags.jars:
        for name in jar.namelist():
            if name.startswith("data/minecraft/recipes/") and name.endswith(".json"):
                sources.append((name, json.loads(jar.read(name))))
    for source_name, value in sources:
            target = output(value)
            if not target:
                continue
            ingredients: list[tuple[str, str]] = []
            if "catalyst" in value:
                found = ingredient(value["catalyst"])
                if found:
                    ingredients.append(found)
            elif "components" in value or "input" in value:
                found = ingredient(value.get("input"))
                if found:
                    ingredients.append(found)
                for entry in value.get("components", []):
                    found = ingredient(entry)
                    if found:
                        ingredients.append(found)
            elif "pattern" in value and "key" in value:
                counts = Counter("".join(value["pattern"]).replace(" ", ""))
                for symbol, count in counts.items():
                    found = ingredient(value["key"].get(symbol))
                    if found:
                        ingredients.extend([found] * count)
            else:
                for entry in value.get("ingredients", []):
                    found = ingredient(entry)
                    if found:
                        ingredients.append(found)
            result.append({
                "path": source_name,
                "output": target[0],
                "count": target[1],
                "ingredients": ingredients,
                "vis": value.get("vis") or value.get("aspects") or {},
                "magic": "thaumic_reborn" in source_name or
                         str(value.get("type", "")).startswith("thaumic_reborn:"),
                "priority": 0 if "crucible_recipes" in source_name else
                            1 if "arcane" in str(value.get("type", "")) else
                            2 if "infusion_recipes" in source_name else 3,
            })
    return sorted(result, key=lambda row: row["priority"])


def derive(recipe: dict, known: dict[str, Counter], tags: Tags) -> Counter | None:
    total: Counter = Counter()
    for kind, identifier in recipe["ingredients"]:
        candidates = [identifier] if kind == "item" else tags.resolve(identifier)
        selected = next((known[item] for item in candidates if known.get(item)), None)
        if selected is None:
            return None
        total.update(selected)
    count = recipe["count"]
    result = Counter({key: int(amount * 0.75 / count)
                      for key, amount in total.items()})
    if recipe["magic"]:
        for key, amount in recipe["vis"].items():
            result[key] += int(math.sqrt(amount) / count)
    return Counter({key: min(64, amount) for key, amount in result.items() if amount > 0})


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--data-jar", action="append", type=Path, default=[])
    args = parser.parse_args()
    tags = Tags(args.data_jar)
    known, covered = scan_aspects(tags)
    pending = recipes(tags)
    provenance: dict[str, str] = {}
    changed = True
    while changed:
        changed = False
        for recipe in pending:
            target = recipe["output"]
            if target in known:
                continue
            reward = derive(recipe, known, tags)
            if reward:
                known[target] = reward
                provenance[target] = recipe["path"]
                changed = True
    activated_modifiers = 0
    for path in sorted((SCANS / "legacy").glob("*.json")):
        value = json.loads(path.read_text())
        if (not value.get("inactive") or
                value.get("inactive_reason") !=
                "recipe-derived base aspects cannot yet be reproduced safely" or
                value.get("type") not in ("item", "block")):
            continue
        target = value.get("target", "")
        combined = Counter(known.get(target, {}))
        combined.update({row["id"]: row["amount"]
                         for row in value.get("aspects", [])})
        value["aspects"] = [{"id": key, "amount": min(64, amount)}
                            for key, amount in combined.items() if amount > 0]
        value["inactive"] = False
        value.pop("inactive_reason", None)
        value["recipe_derivation"] = (
            "TC4 generateTags base plus the original registered complex-object modifier."
        )
        path.write_text(json.dumps(value, indent=2) + "\n")
        known[target] = combined
        covered.add(target)
        activated_modifiers += 1
    generated = 0
    destination = SCANS / "recipe_derived"
    destination.mkdir(exist_ok=True)
    for stale in destination.glob("*.json"):
        stale_value = json.loads(stale.read_text())
        if not stale_value.get("target", "").startswith("thaumic_reborn:"):
            stale.unlink()
    for target, source in sorted(provenance.items()):
        if not target.startswith("thaumic_reborn:") or target in covered:
            continue
        name = re.sub(r"[^a-z0-9_]+", "_", target.split(":", 1)[1]) + ".json"
        value = {
            "type": "block" if (RES / "assets/thaumic_reborn/blockstates" /
                                  f"{target.split(':', 1)[1]}.json").is_file() else "item",
            "target": target,
            "display": "",
            "aspects": [{"id": key, "amount": amount}
                        for key, amount in known[target].items()],
            "inactive": False,
            "recipe_derivation": "TC4 generateTags: 0.75 of ingredient aspects plus floor(sqrt(vis)), divided by output count.",
            "derivation_source": source,
        }
        (destination / name).write_text(json.dumps(value, indent=2) + "\n")
        generated += 1
    print(f"generated {generated}; derivable identities {len(provenance)}; "
          f"activated legacy modifiers {activated_modifiers}")


if __name__ == "__main__":
    main()
