#!/usr/bin/env python3
"""Validate materialized TC4 JSON without starting Minecraft."""

from __future__ import annotations

import argparse
import json
import re
from pathlib import Path


EXPECTED = {
    "research": 201,
    # 395 materialized TC4 registrations plus raw/deepslate copper aliases
    # introduced by the 1.20.1 registry-ID migration.
    "scans": 397,
    "recipes": 307,
    "aspects": 48,
}
RESOURCE_LOCATION = re.compile(r"^[a-z0-9_.-]+:[a-z0-9/._-]+$")


def load(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def active_files(path: Path):
    for file in path.rglob("*.json"):
        value = load(file)
        if not value.get("inactive", False):
            yield file, value


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--resources", required=True, type=Path)
    args = parser.parse_args()
    resources = args.resources.resolve()
    root = resources / "data/thaumic_reborn/thaumcraft"
    recipe_root = resources / "data/thaumic_reborn/recipes"

    for file in resources.rglob("*.json"):
        load(file)

    aspects = {value["id"] for _, value in active_files(root / "aspects")}
    if len(aspects) != EXPECTED["aspects"]:
        raise ValueError(f"expected 48 active aspects, found {len(aspects)}")

    categories: dict[str, Path] = {}
    for file, value in active_files(root / "categories"):
        previous = categories.setdefault(value["id"], file)
        if previous != file:
            raise ValueError(f"duplicate active category {value['id']}: {previous}, {file}")

    research: dict[str, tuple[Path, dict]] = {}
    for file in (root / "research").rglob("*.json"):
        value = load(file)
        previous = research.setdefault(value["id"], (file, value))
        if previous[0] != file:
            raise ValueError(f"duplicate active research {value['id']}: {previous[0]}, {file}")
        if value["category"] not in categories:
            raise ValueError(f"{file} requests inactive or missing category {value['category']}")
        for page in value["pages"]:
            if page["type"] not in (
                    "text",
                    "recipe",
                    "compound_crafting",
                    "infusion",
                    "unavailable",
            ):
                raise ValueError(f"active research contains unresolved page: {file}")
            if page["type"] == "recipe":
                namespace, path = page["recipe"].split(":", 1)
                if (
                    namespace == "thaumic_reborn"
                    and not (recipe_root / f"{path}.json").exists()
                ):
                    raise ValueError(f"{file} requests missing modern recipe {page['recipe']}")
            if (
                    page["type"] == "compound_crafting"
                    and page.get("recipe") != "thaumic_reborn:node_jar_capture"
            ):
                raise ValueError(
                    f"{file} requests unsupported compound crafting recipe "
                    f"{page.get('recipe')}"
                )
    initially_visible = []
    for research_id, (file, value) in research.items():
        parents = value.get("parents", [])
        hidden_parents = value.get("hidden_parents", [])
        for parent in parents + hidden_parents:
            if parent not in research:
                raise ValueError(f"{file} has missing parent {parent}")
        if "virtual" in value and not isinstance(value["virtual"], bool):
            raise ValueError(f"{file} has non-boolean virtual flag")
        if not value.get("concealed", False) and not value.get("virtual", False):
            initially_visible.append(research_id)

        legacy_flags = value.get("legacy", {}).get("flags", {})
        if legacy_flags.get("hidden") or legacy_flags.get("lost"):
            reveal_when = value.get("reveal_when", {"type": "always"})
            if not value.get("concealed", False) or reveal_when.get("type") == "always":
                raise ValueError(
                    f"classic hidden/lost research has no reveal gate: {file}"
                )

    visiting = set()
    visited = set()

    def visit(research_id: str) -> None:
        if research_id in visiting:
            raise ValueError(f"research dependency cycle contains {research_id}")
        if research_id in visited:
            return
        visiting.add(research_id)
        value = research[research_id][1]
        for parent in value.get("parents", []) + value.get("hidden_parents", []):
            visit(parent)
        visiting.remove(research_id)
        visited.add(research_id)

    for research_id in research:
        visit(research_id)

    scans: dict[tuple[str, str], Path] = {}
    for file, value in active_files(root / "scans"):
        target = value.get("target")
        if not isinstance(target, str) or RESOURCE_LOCATION.fullmatch(target) is None:
            raise ValueError(f"active scan has invalid target: {file}")
        key = value["type"], target
        previous = scans.setdefault(key, file)
        if previous != file:
            raise ValueError(f"duplicate active scan {key}: {previous}, {file}")
        if value["type"] not in (
                "block",
                "block_tag",
                "item",
                "item_tag",
                "entity",
                "phenomenon",
        ):
            raise ValueError(f"active scan has unsupported type: {file}")
        for aspect in value["aspects"]:
            if aspect["id"] not in aspects:
                raise ValueError(f"{file} requests missing aspect {aspect['id']}")
            if not isinstance(aspect["amount"], int) or aspect["amount"] <= 0:
                raise ValueError(f"{file} has invalid aspect amount {aspect}")

    legacy_research = list((root / "research/legacy").glob("*.json"))
    legacy_scans = list((root / "scans/legacy").glob("*.json"))
    legacy_recipes = list((root / "recipes_legacy").glob("*.json"))
    actual = {
        "research": len(legacy_research),
        "scans": len(legacy_scans),
        "recipes": len(legacy_recipes),
        "aspects": len(aspects),
    }
    if actual != EXPECTED:
        raise ValueError(f"materialized count mismatch: expected {EXPECTED}, got {actual}")
    for file in legacy_research + legacy_scans + legacy_recipes:
        if "inactive" not in load(file):
            raise ValueError(f"legacy definition has no inactive flag: {file}")

    print(json.dumps({
        "validated_legacy": actual,
        "active": {
            "categories": len(categories),
            "research": len(research),
            "scans": len(scans),
            "aspects": len(aspects),
        },
        "research_progression": {
            "initially_visible": len(initially_visible),
            "concealed_or_virtual": len(research) - len(initially_visible),
        },
    }, indent=2))


if __name__ == "__main__":
    main()
