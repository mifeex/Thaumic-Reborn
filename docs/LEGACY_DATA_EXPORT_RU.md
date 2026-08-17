# Экспорт данных Thaumcraft 4.2.3.5

Оригинальный JAR не содержит готовых таблиц JSON: аспекты, объектные теги,
исследования и рецепты регистрируются императивным Java-кодом. Скрипт
`tools/extract_tc4_legacy.py` декомпилирует JAR через ForgeFlower и создаёт два
слоя данных.

## Результаты

`data/legacy_tc4_4_2_3_5/archive` — архивный слой:

- `aspects.json` — полный классический каталог аспектов;
- `object_aspects.json` — регистрации аспектов предметов и блоков;
- `entity_aspects.json` — регистрации аспектов сущностей, включая NBT-условия;
- `research_categories.json`, `research.json`, `research_warp.json` — категории,
  дерево, страницы, флаги, триггеры и варп исследований;
- `recipes.json` — именованные и неименованные рецепты;
- `lang` — исходные английские и русские строки.

`data/legacy_tc4_4_2_3_5/modern_migration` — слой миграции. В нём уже
нормализованы аспекты, исследования и известные vanilla entity ID, но спорные
соответствия сохраняют `migration_status: requires_*`. Поле
`source_expression` оставляется специально: оно позволяет проверить старый
metadata, NBT, Ore Dictionary и условные регистрации без догадок.

В `object_aspects.json` учитываются оба старых пути регистрации:

- `explicit` — готовый список аспектов;
- `recipe_derived_modifier` — поправка к аспектам, которые TC4 сначала вычислял
  из старого рецепта. Такая запись не активируется, пока современный порт не
  воспроизведёт базовое вычисление.

Полный каталог аспектов и их оригинальные иконки также генерируется прямо в
современные каталоги:

- `src/main/resources/data/thaumic_reborn/thaumcraft/aspects`;
- `src/main/resources/assets/thaumic_reborn/textures/aspects`.

Исследования и рецепты из `modern_migration` пока не являются активным
datapack-контентом: большинство ссылается на ещё не перенесённые предметы,
блоки и типы рецептов. Их нельзя активировать массово до появления
соответствующих современных registry ID.

## Материализованный datapack-контент

`tools/materialize_tc4_content.py` создаёт по одному JSON на каждую старую
регистрацию:

- `thaumcraft/research/legacy` — 201 исследование;
- `thaumcraft/scans/legacy` — 326 объектных и 69 entity-регистраций;
- `thaumcraft/recipes_legacy` — 307 рецептов и шаблонов;
- `thaumcraft/categories/legacy` — дополнительные категории.

Каждая запись содержит `inactive`. Для аспектов и сканов это означает полный
пропуск регистрации. Исследование при `inactive: true` всё равно загружается и
занимает своё место в Таумономиконе, но не получает `AUTOUNLOCK`, не может быть
открыто игроком и показывает недоступную страницу вместо отсутствующего
рецепта. Поэтому старое дерево не исчезает из книги из-за ещё не добавленного
предмета.

Записи получают `inactive: false` только если target существует в 1.20.1,
аспекты известны и представление не теряет metadata/NBT-семантику. Для
исследования дополнительно требуются активная категория, существующая иконка,
поддерживаемые страницы, рецепты и родители. Причина отключения записывается в
`inactive_reason`.

Сейчас полностью доступны пять оригинальных исследований: `ASPECTS`,
`RESEARCH`, `KNOWFRAG`, `THAUMONOMICON` и `THAUMOMETER`. Остальные также
загружаются с классическими родителями, скрытыми родителями, координатами и
подсказками. Для более последовательного дерева любой узел с родителем
получает `concealed: true`: сначала видны только 20 настоящих parentless
entry-point исследований TC4, а потом открываются следующие уровни.
Классические `hidden/lost` дополнительно требуют подсказку от сканирования,
аспекта в отсканированной цели, критерия или варпа. `scan_aspect` не
срабатывает от простого создания аспекта на Столе исследований.
Пороги варпа перенесены как 11 для
`BATHSALTS`, 26 для `ELDRITCHMINOR` и 51 для `ELDRITCHMAJOR`; временный варп
не учитывается. Неоднозначные старые предметы не заменяются догадками и
остаются за явным критерием
`thaumic_reborn:legacy_clue/<research_id>`.

Семь старых recipe ID сопоставлены с уже существующими современными рецептами;
каталог `recipes_legacy` сам по себе не регистрирует Forge-рецепты.

Использованная часть MCP stable 12 сохранена в
`modern_migration/mcp_stable_12_1_7_10_fields.json`. Исходный архив
`mcp_stable-12-1.7.10.zip` имеет MD5
`c7530df0026af3b92e34d8f5913ba054`.

## Повторный запуск

```bash
python3 tools/extract_tc4_legacy.py \
  --jar reference/original/Thaumcraft_1.7.10_4.2.3.5.jar \
  --decompiler /path/to/forgeflower.jar \
  --output data/legacy_tc4_4_2_3_5 \
  --modern-aspects src/main/resources/data/thaumic_reborn/thaumcraft/aspects \
  --modern-textures src/main/resources/assets/thaumic_reborn/textures/aspects

python3 tools/materialize_tc4_content.py \
  --archive data/legacy_tc4_4_2_3_5/archive \
  --content-root src/main/resources/data/thaumic_reborn/thaumcraft \
  --lang-root src/main/resources/assets/thaumic_reborn/lang \
  --mcp-fields data/legacy_tc4_4_2_3_5/modern_migration/mcp_stable_12_1_7_10_fields.json \
  --minecraft-assets-jar /path/to/minecraft-1.20.1-client-extra.jar
```

`manifest.json` фиксирует SHA-256 исходного JAR и количество извлечённых
регистраций. Условные циклы старого кода сохраняются как шаблоны регистрации,
а не искусственно разворачиваются без исходного runtime-конфига.
