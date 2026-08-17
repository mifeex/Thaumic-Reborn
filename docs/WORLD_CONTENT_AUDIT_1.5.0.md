# World Content Audit — 1.5.0

## Назначение

Версия `1.5.0` закрывает world-content блокеры, которые мешали дальнейшему
переносу исследований, алхимии и essentia. Реализация рассчитана на Minecraft
1.20.1 / Forge 47.4.10 / Java 17.

Исключён только Outer Lands — отдельный генерируемый мир-лабиринт TC4 и его
maze-cell pipeline. Поверхностный Eldritch Ring сохранён; он не создаёт и не
подключает лабиринтное измерение.

## Проверенный источник

- JAR: `reference/original/Thaumcraft_1.7.10_4.2.3.5.jar`
- SHA-256:
  `3dba9786966974701578a658d1bb369bf35bdf5f363079f5ac9c4910a39113be`
- исходная версия: Thaumcraft `4.2.3.5`, Minecraft `1.7.10`
- целевая версия мода: `1.5.0`

Параметры перенесены из `ThaumcraftWorldGenerator`, специальных biome
decorators, генераторов Greatwood/Silverwood/Mana Pod, менеджеров деревенских
домов, таблиц spawn entries, entity drop methods, `ConfigAspects` и
`ConfigResearch`. Исходные выражения аспектов и исследований продолжают
храниться в legacy JSON как provenance.

## Совместимость с современной генерацией

TC4 выполнял собственный проход поверх старого terrain generator. В 1.20.1:

- ванильный noise router, высота `-64..320`, aquifers, deepslate и новые
  карверы не заменяются;
- руды заменяют `stone_ore_replaceables` и `deepslate_ore_replaceables`;
- координаты Cinnabar адаптированы к отрицательной нижней границе мира;
- Amber привязан к фактическому `OCEAN_FLOOR_WG`;
- обычный preset `minecraft:normal` и явный preset
  `thaumic_reborn:thaumcraft_modern` оборачивают vanilla multi-noise biome
  source и меняют только поверхностный биом;
- Lush Caves, Dripstone Caves и Deep Dark ниже Y=0 сохраняются;
- океаны, реки, Nether и End не перекрашиваются.

Стандартные новые миры получают руды, растительность, структуры и спавны через
Forge biome modifiers, а TC4 surface-biome patches — через обёртку обычного
preset `minecraft:normal`. Явный preset
`thaumic_reborn:thaumcraft_modern` сохранён для dedicated server и
совместимости. Уже созданный мир хранит biome source в `level.dat`, поэтому
после обновления требуется новый мир; существующие чанки намеренно не
перекрашиваются.

## Руды и материалы

| Контент | TC4 | Реализация 1.5.0 |
|---|---:|---|
| Cinnabar | 18 одиночных попыток, нижняя 1/5 высоты | 18 попыток, от minY+8 до Y=48; выпадает блок, затем smelting/blasting в Quicksilver |
| Amber | 20 попыток около поверхности | 20 попыток в последних 25 блоках под поверхностью; Silk Touch/Fortune |
| Infused Stone | 8 жил по 6 блоков | 8 жил по 6, все 6 прималов, 1/3 biome-biased выбор |
| Shards | 1–2 до Fortune | 1–2 до Fortune; Silk Touch сохраняет камень |

Зарегистрированы Cinnabar, Amber, шесть Infused Stone, Quicksilver, Amber и
шесть primal shards. Добавлены ore/block/item tags, модели, классические
текстуры, loot tables, smelting/blasting и локализация.

## Деревья и растения

| Контент | Правило |
|---|---|
| Greatwood | базовый знаменатель `25`; biome support forest/magical `1.0`, lush `0.5`, conifer/plains/savanna/swamp `0.2` |
| Silverwood | базовый знаменатель `60`; magical biomes; возможный Pure node в стволе |
| Greatwood shape | точный двухпроходный TC4-алгоритм: height limit `11..21`, attenuation `0.618`, 2×2 trunk, crown width `1.2` then `1.66`, итоговая верхушка примерно `16..32` блоков над основанием |
| Greatwood hollow | шанс `1/16` у дикого дерева и `1/8` у выращенного саженца; cave-spider spawner, 50 попыток паутины и сундук simple-dungeon под деревом |
| Shimmerleaf | группа вокруг дикого Silverwood |
| Cinderpearl | жаркий песчаный биом, попытка `1/30`, 18 цветков |
| Vishroom | Magical Forest, 8 попыток, шанс `1/4`, требуется соседнее бревно |
| Mana Pod | 10 попыток в magical biome, подвешивается под древесиной, age 2–6 |
| Taint Fibres | 18 попыток в Tainted Lands |

Greatwood и Silverwood имеют полный набор log/leaves/planks/sapling. Саженцы
используют те же feature implementations, листья разрушаются стандартной
системой distance, Mana Pod растёт random ticks и выдаёт Mana Beans.
Ethereal Bloom зарегистрирован как природный ресурс, но не генерируется
естественно — это соответствует TC4, где он создавался алхимией.

Восстановлены обычные рецепты TC4:

- 2 Shimmerleaf → Quicksilver;
- 2 Cinderpearl → Blaze Powder;
- Greatwood/Silverwood logs → planks.

## Биомы

- Magical Forest: собственные цвета/климат, Magical tag, Pech/Wisp, magic
  vegetation.
- Tainted Lands: собственные цвета/климат, taint vegetation и taint spawns.
- Eerie: редкие локальные поверхностные пятна и усиленный hostile spawn list.
- Eldritch biome зарегистрирован для данных и команд, но не вставлен в
  Overworld: в TC4 он принадлежал исключённому Outer Lands.

Относительные TC4 веса Magical Forest `5` и Tainted Lands `2` сохранены как
server-config параметры поверхностного overlay. Eerie остаётся малым локальным
пятном, как область вокруг древних точек, а не climate-weighted биомом.

## Структуры и природные точки

| Структура | Частота TC4 | Текущая настройка |
|---|---:|---:|
| Mound / barrow | `1/150` | `1.15/150`, подземная камера, сундук, Eerie node |
| Eldritch Ring | `1/66` | `1/66`, ancient/obsidian ring, dark node, Crimson Knight/Cleric |
| Hilltop Stones | `1/40` | `1.15/40`, ancient pillars, node |
| Obsidian Aura Totem | `1/360` | `1.15/360`, dark node |
| Wizard tower | village weight `15` | jigsaw weight `17`, Thaumaturge и loot chest |
| Banker house | village weight `25` | jigsaw weight `29`, Thaumic Banker |

Первые четыре знаменателя и веса деревенских построек взяты из TC4.
Structure-set даёт Mound, Hilltop Stones и Aura Totem увеличение средней
плотности на `15%`; Eldritch Ring использует исходную плотность `1/66`.
Параметр `structureRarityScale` дополнительно умножает их
знаменатели. Wizard/Banker встроены в vanilla village house pools; из-за
целочисленных jigsaw-весов ближайшие к `+15%` значения составляют `17`
(`+13.3%`) и `29` (`+16%`).

## NPC и существа

Зарегистрированы 28 естественных/структурных living entities TC4:

- Angry Zombie, Furious Zombie, Wisp, Firebat, Pech, Mind Spider;
- Eldritch Guardian/Warden/Construct/Crab;
- Crimson Knight/Cleric/Praetor;
- Inhabited Zombie и Thaumic Slime;
- Tainted Crawler, Taintacle, Taint Tendril, Taint Spore/Swarmer/Swarm;
- Tainted Chicken/Cow/Creeper/Pig/Sheep/Villager;
- Giant Taintacle.

Для всех имеются server-owned attributes, базовые hostile/wander/flying AI,
spawn eggs, сохранение Pech type и Wisp aspect, spawn configuration, scan
definitions, локализация и классические текстуры. Создаваемые игроком големы,
projectiles и технические entity не относятся к world-generated roster.

Естественные spawn entries сохранены для Overworld, Magical Forest, Tainted
Lands, Eerie, Nether и зарегистрированного Eldritch biome. Server config может
отключить Angry Zombies, Firebats, Wisps, Pech, taint и eldritch группы.

## Дропы и получаемые материалы

Добавлены Zombie Brain, Mana Bean, Tainted Goo, Taint Tendril, Gold Coin,
Void Seed, Eldritch Eye, Crimson Rites, Runed Tablet, Primordial Pearl и три
уровня Treasure Bag.

Ключевые исходные ветви разделены:

- Guardian редко даёт Eldritch Eye;
- Knight/Cleric дают Knowledge Fragment, Void Seed или Gold Coin и редко
  Crimson Rites;
- Praetor даёт Rare Treasure Bag;
- Warden/Construct дают Primordial Pearl и Rare Treasure Bag;
- Giant Taintacle даёт Primordial Pearl;
- Wisp создаёт essence с сохранённым primal aspect;
- Pech даёт Mana Bean, монеты и Knowledge Fragment.

Treasure Bags открываются только сервером и выдают 8–12 наград. Common и
Uncommon встречаются в Mound/Wizard loot, Rare — у боссов. Runed Tablet
зарегистрирован и сканируется, но естественно недоступен, потому что его
источником была key room исключённого Outer Lands.

## Аспекты, сканирование и исследования

- исходные TC4 object/entity aspect arrays активируются только для
  существующей modern target;
- добавлены явные scan definitions для новых structural blocks, Mana Pod,
  Mana Bean и существ без прямого legacy record;
- Pech type `0` активирован без ложного объединения NBT-вариантов;
- автоматическая эвристика аспектов остаётся выключенной по умолчанию;
- исследования `ORE`, `PLANTS`, `PECH` снова активны;
- `CRIMSON` активен и серверно завершается чтением Crimson Rites;
- Primordial Pearl записывает исходный item-trigger criterion;
- зависимые исследования с отсутствующей алхимией, Infusion, warp acquisition
  или Outer Lands остаются inactive, даже если их JSON и provenance сохранены.

## Server config

Файл Forge server config содержит:

- `generateOres`, `generateTrees`, `generatePlants`, `generateStructures`,
  `generateBiomes`;
- `cinnabarAttemptsPerChunk=18`, `amberAttemptsPerChunk=20`,
  `infusedStoneAttemptsPerChunk=8`;
- `greatwoodRarity=25`, `silverwoodRarity=60`;
- `structureRarityScale=1`;
- `magicalForestWeight=5`, `taintedLandsWeight=2`;
- отдельные switches для основных spawn-групп.

## Статус проверки

На момент финальной сборки:

- `compileJava` и JUnit: пройдены;
- парсинг всех JSON через `jq`: пройден;
- Forge GameTest server: `16/16` обязательных тестов пройдены, включая
  создание всех 28 типов world-content существ;
- после исправления найденных runtime-проблем успешно созданы и сохранены
  четыре полностью свежих headless Forge-мира: один с vanilla biome source и
  три с preset `thaumic_reborn:thaumcraft_modern` на разных seed;
- во всех четырёх мирах завершилась подготовка spawn chunks и сохранение
  Overworld, Nether и End без registry/datapack/worldgen exception;
- обычный интерактивный `runServer` не запускался: для него требуется явное
  принятие Minecraft EULA пользователем;
- client visual QA, ручной multiplayer, фактические long-run spawn rates и
  баланс длительной генерации не подтверждены автоматическими тестами.

## Итоговый артефакт

- файл: `build/libs/thaumic_reborn-1.5.0.jar`;
- встроенная версия в `META-INF/mods.toml` и manifest: `1.5.0`;
- SHA-256:
  `e7682b09daeb6d523f7ea309fc32340f6d65c5ad04469d0038ca0f96dc3acc92`.

Инструкция для ручной проверки:
[`WORLD_CONTENT_MANUAL_TEST_1.5.0.md`](WORLD_CONTENT_MANUAL_TEST_1.5.0.md).
