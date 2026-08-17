#!/usr/bin/env python3

import json
from pathlib import Path


ROOT = Path(
    "src/main/resources/data/thaumic_reborn/thaumcraft/research/legacy"
)


def main() -> None:
    updated = 0
    for file in sorted(ROOT.glob("*.json")):
        research = json.loads(file.read_text())
        secondary = research.get("legacy", {}).get("flags", {}).get(
            "secondary",
            False,
        )
        costs = research.get("legacy", {}).get("research_aspects")
        if not secondary:
            if "purchase_cost" in research:
                del research["purchase_cost"]
                write_json(file, research)
                updated += 1
            continue
        if not costs:
            raise ValueError(
                f"Secondary research {research.get('id')} has no aspect cost"
            )

        ordered = {}
        for key, value in research.items():
            if key == "purchase_cost":
                continue
            if key == "inactive":
                ordered["purchase_cost"] = costs
            ordered[key] = value
        if "purchase_cost" not in ordered:
            ordered["purchase_cost"] = costs
        if research.get("purchase_cost") != costs:
            write_json(file, ordered)
            updated += 1

    print(f"Synchronized {updated} TC4 secondary research purchase costs")


def write_json(file: Path, value: dict) -> None:
    file.write_text(
        json.dumps(value, indent=2, ensure_ascii=False) + "\n"
    )


if __name__ == "__main__":
    main()
