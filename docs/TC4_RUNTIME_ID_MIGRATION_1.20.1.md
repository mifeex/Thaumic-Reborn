# Миграция runtime ID TC4 → Forge 1.20.1

Рабочие ссылки рецептов, исследований и сканов отделены от архивного
provenance. Поля внутри `legacy`, а также `source_expression`,
`legacy_target` и `legacy_entity_id`, намеренно сохраняют оригинальные
значения Thaumcraft 4.2.3.5 / Minecraft 1.7.10.

Команда повторной миграции:

```bash
python3 tools/migrate_tc4_runtime_ids.py
```

Скрипт:

- переводит SRG-поля `Blocks.field_*` и `Items.field_*` на registry ID 1.20.1;
- переводит metadata-варианты на отдельные современные ID;
- переводит `ConfigBlocks` и `ConfigItems` на реально зарегистрированный
  контент `thaumic_reborn`;
- переводит старые entity names (`Thaumcraft.EldritchGuardian`,
  `Thaumcraft.CultistKnight` и другие) на современные entity type ID;
- заменяет Ore Dictionary на `BLOCK_TAG`/`ITEM_TAG`;
- подставляет в исследование результат настоящего runtime-рецепта вместо
  общего значка Таумономикона;
- деактивирует схлопнувшиеся дубликаты, отдавая приоритет hand-authored scan;
- формирует машинный отчёт
  `data/legacy_tc4_4_2_3_5/modern_migration/runtime_ids.json`.

Текущий результат:

- 213 scan-файлов получили современные цели первым проходом;
- 54 исследования получили актуальные item ID иконок;
- 303 explicit scan definitions загружаются в runtime;
- 40 записей остаются архивно видимыми, но inactive, потому что
  соответствующего объекта в текущем registry пока нет.

Регрессия `Duplicate scan definition:
block:thaumic_reborn:ancient_stone`, которая ломала открытие Create World
и существующего мира, закрыта: hand-authored
`scans/world/ancient_stone.json` остаётся единственной активной записью, а
схлопнувшиеся legacy-варианты помечены `inactive`. Unit-тест проверяет
уникальность всех активных `type + target` во всём дереве `scans`, включая
пересечения между hand-authored и legacy-каталогами. Forge GameTest
дополнительно выполняет настоящий resource reload, загружает мир и проверяет,
что `ancient_stone` присутствует в runtime registry ровно один раз.

К последней группе относятся TC4 Golem, Primal Orb, native clusters,
мясные самородки, Salis Mundus, свеча, отсутствующие варианты taint,
cultist armor и отдельные eldritch-блоки. Для них не создаются фиктивные
ID: запись будет автоматически мигрирована после регистрации настоящего
современного объекта и добавления соответствия в скрипт.
