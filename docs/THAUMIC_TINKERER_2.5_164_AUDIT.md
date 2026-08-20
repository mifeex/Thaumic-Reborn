# Аудит Thaumic Tinkerer 2.5-1.7.10-164

Дата: 2026-08-04  
Эталон: `reference/ThaumicTinkerer_2.5_1.7.10_164.jar`  
SHA-256: `7aeb6d77a24c96a05324ba22c34b5a682d468daf8adf3c8540136b417f4727f1`

Лицензионные границы и рекомендации для публичного релиза: [`THAUMIC_TINKERER_LICENSE_AUDIT.md`](THAUMIC_TINKERER_LICENSE_AUDIT.md).

## 1. Назначение и границы аудита

Это инвентаризация содержимого аддона и сравнение с текущим `ThaumcraftModern`, а не заявление о завершённом переносе. Источником истины служат классы, ресурсы, локализация и регистрационные контракты JAR. Совпадение базовой механики Thaumcraft (аспекты, жезлы, инфузия, исследования) не означает, что соответствующий предмет Thaumic Tinkerer уже реализован.

Статусы:

| Статус | Значение |
|---|---|
| **ЕСТЬ** | Конкретный контент TT и его игровой контракт уже реализованы. |
| **ЧАСТИЧНО** | Есть часть конкретной механики или данных TT, но нет полного цикла. |
| **ЕСТЬ ОСНОВА** | В порте есть подходящая базовая система TC4, но самого контента TT нет. |
| **НЕТ** | Нет ни конкретного контента, ни достаточной специализированной реализации. |
| **НЕ ПЕРЕНОСИТЬ 1:1** | Legacy-интеграция или технический слой требует современного аналога/отдельного решения. |

Главный итог: **ни один из 77 research entries Thaumic Tinkerer сейчас не перенесён как завершённая игровая вертикаль**. Проект уже располагает значительной частью общего фундамента, поэтому перенос не начинается с нуля, но регистрировать TT-исследования до реализации их серверных механик нельзя.

## 2. Фактический масштаб JAR

- Minecraft 1.7.10, Thaumic Tinkerer `2.5-1.7.10-164`;
- обязательная зависимость по `mcmod.info`: Thaumcraft;
- 572 class-файла, включая встроенные API-классы сторонних модов;
- 326 resource entries, 293 PNG, собственных OGG нет;
- 77 именованных исследований: основная ветка (включая зачарования), KAMI и elemental/crop;
- 64 локализованных item/meta-item имени, 30 block/meta-block имён, 14 видимых зачарований;
- 8 GUI-контрактов: Aspectalyzer, Remote Placer, Corporeal Attractor, Osmotic Enchanter, Dynamism Tablet, Bottomless Pouch, Celestial Gateway и список его назначений;
- собственные пакеты для кнопок машин, настройки брони, врат, телепортации и Soul Hearts;
- отдельный KAMI endgame, включаемый конфигом;
- отдельное Bedrock Dimension и генерация кластеров dimensional shards.

Числа локализации не равны количеству registry objects: несколько предметов и блоков используют metadata/subtypes, а часть внутренних сущностей не имеет обычного имени в `.lang`.

## 3. Карта аддона верхнего уровня

| Ветка | Что реализует оригинал | Состояние у нас |
|---|---|---|
| Thaumic Tinkering | машины, логистика, фокусы, талисманы, автоматизация, призыв, мобильность | **ЕСТЬ ОСНОВА**, конкретный TT-контент отсутствует |
| Enchanting | Osmotic Enchanter и 14 специальных зачарований | **НЕТ** |
| KAMI | ichor-прогрессия, броня, инструменты, фокусы, телепортация, dimension shards | **ЕСТЬ ОСНОВА** для инфузии/жезлов/Curios; контент **НЕТ** |
| Infused Crops / Elemental Fire | шесть огней, семена, культуры, зерно, зелья | **НЕТ** |
| Computer integration | ComputerCraft/OpenComputers peripherals для TT и блоков TC | **НЕ ПЕРЕНОСИТЬ 1:1** без выбранной современной интеграции |
| Compat | AE2, Botania, Blood Magic, TConstruct, EnderStorage, IC2 | **НЕ ПЕРЕНОСИТЬ 1:1**; отдельные опциональные модули после ядра |

## 4. Основная ветка Thaumic Tinkering

### 4.1 Материалы и декоративные блоки

| Контент | Оригинальный контракт | Статус |
|---|---|---|
| Smokey Quartz | предмет и семейство блока: обычный, резной, колонна, slab, stairs | **НЕТ**; vanilla-style block family реализуется независимо от TC-систем |
| Ethereal Platform | особая платформа, исследование `PLATFORM` | **НЕТ** |
| Hyperenergetic Nitor | переносимый источник света, оставляющий свет за игроком; поведение зависит от измерения | **ЕСТЬ ОСНОВА**: Nitor существует, но bright/mobile-light варианта TT нет |
| Helmet of Revealing | отдельный шлем с revealing-функцией | **ЕСТЬ ОСНОВА**: goggles/revealing-системы TC; предмет TT отсутствует |

### 4.2 Газы и элементальный свет

| Контент | Оригинальный контракт | Статус |
|---|---|---|
| Gaseous Illuminae | предмет выпускает распространяющийся невидимый светящийся газ | **НЕТ** |
| Gaseous Tenebrae | предмет выпускает распространяющийся газ тьмы | **НЕТ** |
| Fume Dissipator | shift-use удаляет оба вида газа поблизости | **НЕТ** |

Нужны два серверно тикающих gas-block семейства, распространение с лимитом работы на тик, удаление, свет/затемнение, сохранение и синхронизация. Это не следует смешивать с flux gas базового Thaumcraft.

### 4.3 Фокусы основной ветки

| Фокус | Поведение оригинала | Статус |
|---|---|---|
| Uprising | толкает владельца по направлению взгляда и сбрасывает накопленный fall damage | **ЕСТЬ ОСНОВА**: wand/focus/vis; конкретного фокуса нет |
| Dislocation | забирает блок вместе с BlockEntity/NBT и позднее ставит; TE стоят ×5 vis, spawner ×20 | **ЕСТЬ ОСНОВА**, но безопасного TT-переноса блоков нет |
| Telekinesis | перемещает лежащие предметы к точке взгляда; при sneaking — к игроку | **ЕСТЬ ОСНОВА**, фокуса нет |
| Efreet's Flame | переплавляющий фокус; имеет собственные smelt-data правила | **ЕСТЬ ОСНОВА**, фокуса нет |
| Mending | лечение фокусом | **ЕСТЬ ОСНОВА**, фокуса нет |
| Ender Rift | доступ к Ender Chest через фокус | **ЕСТЬ ОСНОВА**, фокуса/меню нет |
| Distortion | отражение/отклонение снарядов | **ЕСТЬ ОСНОВА**, фокуса нет |

Для каждого нужны точные vis costs, длительность use, upgrades, дальность, допустимые цели, PvP/server checks, FX и сохранение NBT из байткода оригинала. Одних research pages недостаточно.

### 4.4 Талисманы, знания и служебные предметы

| Контент | Оригинальный контракт | Статус |
|---|---|---|
| Talisman of Remedium | Curio/amulet; включается shift-use, снимает вредные эффекты, warp-effects особенно сильно расходуют durability | **ЕСТЬ ОСНОВА**: Curios/effects/warp; предмета нет |
| Talisman of Withhold | Curio/amulet; поглощает XP orbs и выдаёт Bottles o' Enchanting при use со стеклянной бутылкой | **ЕСТЬ ОСНОВА**: Curios; хранилища/предмета нет |
| Spellbinding Cloth | crafting-рецепт снимает зачарования с предмета | **НЕТ** |
| Tome of Knowledge Sharing | привязка к знаниям игрока и передача исследований другому игроку; survival recipe конфигурируем | **ЕСТЬ ОСНОВА**: player research persistence/sync; предмета и безопасного импорта нет |
| Soul Mould | снимает шаблон сущности для фильтра Corporeal Attractor/призыва | **НЕТ** |
| Cursed Spirit's Blade | режим сбора soul aspects, переключаемое состояние | **НЕТ** |
| Soul Aspect | обычный, condensed и infused уровни; материал цепочки призыва | **НЕТ** |
| Infused Scribing Tools | в локализации явно помечены `(NYI)` | **НЕ СЧИТАТЬ КОНТЕНТОМ ДЛЯ ПЕРЕНОСА**, пока байткод не подтвердит активный контракт |

### 4.5 Машины и BlockEntity

| Машина | Полный игровой контракт | Статус |
|---|---|---|
| Aspectalyzer | инвентарь + GUI; компьютер считывает аспекты предмета | **ЕСТЬ ОСНОВА**: item-aspect registry; машина/периферия отсутствует |
| Transvector Interface | привязка Binder-ом в радиусе 4 блоков; проксирует стороны, items, fluids, essentia и legacy energy; camo | **ЕСТЬ ОСНОВА** для essentia/capabilities; универсального proxy нет |
| Transvector Dislocator | удалённая разновидность transvector-механики | **НЕТ** |
| Golem Connector | Binder → golem до 30 блоков, computer control и camo | **ЕСТЬ ОСНОВА**: golems; connector/protocol отсутствуют |
| Dynamism Tablet | fake player использует предмет left/right click; режим always/redstone, ориентация, скорость инструмента | **НЕТ**; требует строгих permission/claim checks |
| Remote Placer | удалённое размещение с GUI ориентации/режимов | **НЕТ** |
| Kinetic Attractor | push/pull предметов; радиус = половина redstone strength | **НЕТ** |
| Corporeal Attractor | push/pull живых существ; фильтр Soul Mould, child/adult и GUI | **НЕТ** |
| Osmotic Enchanter | выбранные enchantments/levels, wand vis, длительный процесс, 6 колонн из 2–12 obsidian totems + Nitor, запрет iron/copper caps | **ЕСТЬ ОСНОВА**: wand vis/menus; машина и алгоритм отсутствуют |
| Essentia Funnel | ставится над hopper; переливает jar в jar при совместимом аспекте | **ЕСТЬ ОСНОВА**: jars и essentia transport; блока нет |
| Thaumic Restorer | машина ремонта с отдельной моделью/рендером | **ЕСТЬ ОСНОВА**: essentia/menus; машины нет |
| Tablet of Necromancy | призыв сущностей через soul-material chain | **НЕТ** |
| Levitational Locomotive | перемещение конструкции/блоков | **НЕТ** |
| Levitational Locomotive Relay | связанный relay для locomotive | **НЕТ** |

Также JAR содержит внутренние `BlockForcefield`, `TileForcefield`, `BlockCamo`, `TileCamo`, `BlockSummon`, mobilizer/relay и служебные модели/рендеры. Их нельзя потерять при переносе только видимых research names: это части законченных вертикалей, а не обязательно отдельные исследования.

## 5. Зачарования

Оригинал регистрирует 14 видимых TT-зачарований и ещё несколько внутренних полей (`filtration`, `imbued`, `resolute`), не представленных как обычные локализованные исследования в данном JAR. Их активность следует подтверждать отдельно; автоматически включать их нельзя.

| Зачарование | Цель/эффект по оригиналу | Статус |
|---|---|---|
| Ascent Boost | поножи, усиление прыжка | **НЕТ** |
| Slow Fall | ботинки, замедление падения; Shift временно отключает | **НЕТ** |
| Flaming Touch | инструмент специализируется на дереве; вне специализации неэффективен, double durability | **НЕТ** |
| Disintegrate | инструмент специализируется на мягких/обычных блоках; вне специализации неэффективен, double durability | **НЕТ** |
| Quick Draw | быстрее натягивает лук | **НЕТ** |
| Vampirism | возвращает владельцу часть нанесённого живой цели урона | **НЕТ** |
| Dispersed Strikes | специальный боевой модификатор | **НЕТ** |
| Focused Strikes | специальный боевой модификатор | **НЕТ** |
| Final Strike | специальный боевой модификатор | **НЕТ** |
| Valiance | специальный боевой модификатор | **НЕТ** |
| Tunnel | специальное добывающее зачарование | **НЕТ** |
| Shatter | специальное добывающее/боевое зачарование | **НЕТ** |
| Shockwave | специальный ударный эффект | **НЕТ** |
| Pounce | специальная мобильность/атака | **НЕТ** |

Есть собственный `EnchantmentManager`, compatibility rules и Osmotic Enchanter UI с иконками как vanilla, так и Thaumcraft/TT enchantments. Перенос только event handlers без менеджера совместимости изменит контракт выбора чар.

## 6. KAMI endgame

KAMI — отдельная, конфигурируемая high-end ветка. Её нельзя выдавать игроку частично: материалы, dimension shards, infusion chain, wand components, tools/armor и awakened upgrades образуют связанную прогрессию.

### 6.1 Материалы и компоненты

| Контент | Роль | Статус |
|---|---|---|
| Nether Shard / Ender Shard | dimension-specific drops | **НЕТ** |
| Ichor | базовый KAMI-материал | **НЕТ** |
| Ichorcloth | броня и wand core | **НЕТ** |
| Ichorium / nugget | инструменты, caps и endgame recipes | **НЕТ** |
| Ichorium Caps | wand cap component | **ЕСТЬ ОСНОВА**: dynamic wand assembly; компонента нет |
| Ichorcloth-strapped Silverwood Core | wand rod/core component | **ЕСТЬ ОСНОВА**; компонента нет |
| Protoclay | материал копирования/создания | **НЕТ** |
| Celestial Pearl | gateway/recall/awakening chain | **НЕТ** |

### 6.2 Ichorcloth armor

Базовый комплект: Cowl, Robe, Leggings, Boots. Awakened-комплект:

- Cowl of the Abyssal Depths;
- Robes of the Stratosphere;
- Leggings of the Burning Mantle;
- Boots of the Horizontal Shield.

JAR содержит отдельные серверные и клиентские armor handlers, toggle packet/keybind/HUD, Soul Hearts и модель крыльев. Это подтверждает, что awakened armor — не просто повышенные armor attributes. В `ThaumcraftModern` есть Curios, броня и эффекты как инфраструктура, но ни одна часть KAMI armor не реализована: **ЕСТЬ ОСНОВА / контент НЕТ**.

### 6.3 Ichorium tools

Базовые: pickaxe, shovel, axe, sword. Awakened версии имеют по три режима:

- pickaxe: Block / Square / Line;
- shovel: Block / Square / Column;
- axe: Block / Square / Tree;
- sword: Single / Area / Soul.

Есть отдельный HUD режима, key handling, area-break logic и три визуальных состояния gem для каждого awakened tool. Статус всех восьми инструментов: **НЕТ**, при наличии общей основы tool/item networking.

### 6.4 KAMI предметы и фокусы

| Контент | Контракт | Статус |
|---|---|---|
| Bottomless Pouch | собственный переносной inventory, GUI/container, запрет вложения pouch в pouch | **НЕТ** |
| Black Hole Ring | Curio с режимом absorbing и NBT-состоянием | **ЕСТЬ ОСНОВА**: Curios; предмета нет |
| Worldshaper's Looking Glass | сохраняет/предсказывает размещение блоков; отдельный item renderer и prediction renderer | **НЕТ** |
| Feline Amulet | Curio со специальным защитным/кошачьим эффектом | **ЕСТЬ ОСНОВА**; предмета нет |
| Shadowbeam | лучевой wand focus с собственными Beam/Particle | **ЕСТЬ ОСНОВА**; фокуса нет |
| Experience Drain | wand focus, работающий с XP | **ЕСТЬ ОСНОВА**; фокуса нет |
| Celestial Recall | teleport focus с отдельным depth icon/state | **ЕСТЬ ОСНОВА**; фокуса нет |

### 6.5 Celestial Gateway и Bedrock Dimension

`Celestial Gateway` имеет GUI, именованные destinations, entrance point, разрешение входящих телепортов, проверку другого измерения/дистанции и server packet телепортации. Отдельно присутствуют `Bedrock Portal`, `WorldProviderBedrock`, `ChunkProviderBedrock`, `TeleporterBedrock`, `KamiDimensionHandler`, `OreClusterGenerator` и частоты руд.

Статус: **ЕСТЬ ОСНОВА** — собственные измерения, saved data, menus и server networking в проекте существуют; конкретных gateway, bedrock dimension, portal generation и dimensional shards нет. Это отдельная P4-вертикаль с fresh-world, dedicated-server и multiplayer проверкой.

## 7. Elemental Fire, infused crops и зелья

Оригинал содержит шесть видов elemental fire:

- Ignis, Aqua, Aer, Terra, Ordo, Perditio;
- отдельные `BlockFire*`, animated layer textures и предметные формы;
- infused farmland, шесть вариантов seeds/crops, четыре стадии роста;
- Imbued Grain и aspect-based crop loot;
- четыре видимых infused potion: Aer, Ignis, Terra, Aqua;
- bonemeal integration, управляемую конфигом;
- primal-aspect enum для grain/potions и отдельный potion effect handler.

Research UI объединяет это в шесть исследований огня и `INFUSED_POTIONS` (локализовано как Infused Crops). В текущем порте всего этого нет: **НЕТ**. Базовые аспекты и crop/block APIs — только фундамент.

## 8. Исследовательская структура

В JAR подтверждены 77 именованных узлов.

### Основная категория — 45 узлов

`PERIPHERALS`, `ASPECT_ANALYZER`, `DARK_QUARTZ`, `SHARE_TOME`, `INTERFACE`, `GOLEM_CONNECTOR`, `GASEOUS_LIGHT`, `GASEOUS_SHADOW`, `GAS_REMOVER`, `SPELL_CLOTH`, `ANIMATION_TABLET`, `FOCUS_FLIGHT`, `FOCUS_DISLOCATION`, `CLEANSING_TALISMAN`, `BRIGHT_NITOR`, `FOCUS_TELEKINESIS`, `MAGNETS`, `ENCHANTER`, `XP_TALISMAN`, `FUNNEL`, 14 узлов `TTENCH_*`, `FOCUS_SMELT`, `FOCUS_HEAL`, `FOCUS_ENDER_CHEST`, `BLOOD_SWORD`, `SUMMON`, `DISLOCATOR`, `REVEALING_HELM`, `REPAIRER`, `FOCUS_DEFLECT`, `LEVITATOR`, `PLATFORM`.

### KAMI — 25 узлов

`DIMENSION_SHARDS`, `ICHOR`, `ICHOR_CLOTH`, `ICHORIUM`, `ROD_ICHORCLOTH`, `CAP_ICHOR`, `ICHORCLOTH_ARMOR`, четыре awakened armor узла, `CAT_AMULET`, `ICHOR_TOOLS`, четыре awakened tool узла, `ICHOR_POUCH`, `BLOCK_TALISMAN`, `PLACEMENT_MIRROR`, `FOCUS_SHADOWBEAM`, `FOCUS_XP_DRAIN`, `PROTOCLAY`, `WARP_GATE`, `FOCUS_RECALL`.

Группировка `ICHORCLOTH_ARMOR` и `ICHOR_TOOLS` включает базовые комплекты без отдельных локализованных research names для каждой базовой части. Поэтому источником миграционной истины должен стать runtime registration graph, а не только `.lang`.

### Elemental/crop — 7 узлов

`FIRE_IGNIS`, `FIRE_AQUA`, `FIRE_AER`, `FIRE_TERRA`, `FIRE_ORDO`, `FIRE_PERDITIO`, `INFUSED_POTIONS`.

### Служебная неоднозначность

`LibResearch` также объявляет ключи `MAGNET`, `MOB_MAGNET`, `ICHORCLOTH_HELM/CHEST/LEGS/BOOTS`, `ICHOR_PICK/SHOVEL/AXE/SWORD`, `LEVITATOR_RELAY`, `REMOTE_PLACER`. Они могут быть внутренними/recipe child keys и не имеют отдельного `ttresearch.name.*` в английской локализации. Их следует сохранить в извлечённом графе, но не считать отдельными пользовательскими узлами без подтверждения регистрации.

## 9. GUI, сеть и сохранение

| Вертикаль | GUI/container | Пакеты/состояние |
|---|---|---|
| Aspectalyzer | `GuiAspectAnalyzer`, `ContainerAspectAnalyzer` | inventory/TileEntity sync, computer query |
| Remote Placer | `GuiRemotePlacer`, `ContainerRemotePlacer` | `PacketPlacerButton` |
| Corporeal Attractor | `GuiMobMagnet`, `ContainerMobMagnet` | `PacketMobMagnetButton`, filters/modes |
| Osmotic Enchanter | `GuiEnchanting`, `ContainerEnchanter` | add enchant/start work packets, wand/item lock |
| Dynamism Tablet | `GuiAnimationTablet`, `ContainerAnimationTablet` | `PacketTabletButton`, fake-player state |
| Bottomless Pouch | `GuiIchorPouch`, `ContainerIchorPouch` | item NBT inventory |
| Celestial Gateway | gate GUI + destination GUI + container | gate button/teleport packets, destinations/permission |
| Armor/Soul Hearts | HUD/key handlers | armor toggle and Soul Hearts packets |

Для современного порта все изменения inventory, enchantment selection, block movement, entity filters и teleport destinations должны быть server-authoritative. Клиентские prediction/render handlers не являются разрешением на действие.

## 10. Интеграции и внешние зависимости

В JAR присутствуют:

- ComputerCraft peripherals: Arcane Bore, Brain in a Jar, Arcane Ear, Deconstructor, aspect containers, essentia transport;
- OpenComputers drivers для тех же семейств;
- Botania, Blood Magic, Tinkers' Construct, EnderStorage, IndustrialCraft compatibility hooks;
- AE2 API-классы, упакованные внутрь JAR;
- legacy energy/fluid/item proxying у Transvector Interface.

Решение для порта:

1. Не встраивать копии чужих API.
2. Сначала реализовать автономный TT gameplay.
3. Затем вынести integrations в optional compat packages с `ModList`/capability checks.
4. ComputerCraft/CC:Tweaked и OpenComputers-подобную поддержку считать отдельным scope: выбрать поддерживаемый современный мод и зафиксировать API.
5. Для Transvector Interface определить точный whitelist capabilities; «проксировать всё» опасно для ownership, chunk loading и recursion.

## 11. Что уже есть в ThaumcraftModern

### Реально пригодный фундамент

- аспекты и aspect tags/scan registry;
- знания игрока, research state, research puzzle и синхронизация;
- собственный Thaumonomicon screen и data-driven research pages;
- wand state, динамическая сборка rod/cap, vis consumption и focus framework;
- arcane crafting, crucible и infusion recipes;
- essentia jars, transport definitions, tubes и advanced buffer/flow;
- Curios для прежних bauble-слотов;
- golem foundations;
- custom menus/screens/network packets;
- worldgen, dimensions/structures и saved-data patterns;
- базовые эффекты, сущности и server-authoritative conventions.

### Чего это не доказывает

- ни одного registry ID TT;
- ни одного из 77 TT research definitions/pages как активной полной вертикали;
- рецептов TT с точными instability/essentia/vis/ingredients;
- моделей, текстур, GUI и FX TT в namespace проекта;
- точного поведения машин, enchantments, KAMI armor/tools, elemental fire или crops;
- совместимости с ComputerCraft/OpenComputers/Botania/Blood Magic/TCon/AE2/IC2;
- dedicated-server, multiplayer и визуальной приёмки TT.

Итого по статусам: **конкретный TT-контент — 0 завершённых вертикалей; общий фундамент — значительный, но не засчитывается как порт аддона**.

## 12. Предлагаемая структура переноса

### P0 — извлечение эталона и namespace

- получить полный decompiled reference tree или систематический `javap -c -p` dump;
- извлечь фактический registration graph, parents/coordinates/aspects/warp/recipes всех 77 research entries;
- составить registry manifest: class → legacy name/meta → modern ID → recipe → research → assets;
- решить namespace (`thaumictinkerer` либо согласованный namespace внутри основного мода) и лицензионный статус assets;
- добавить coverage tests, запрещающие активное исследование без executable output/mechanic.

### P1 — автономная базовая вертикаль

Рекомендуемый первый выпуск: Smokey Quartz family → Gaseous Illuminae/Tenebrae → Fume Dissipator → Spellbinding Cloth. Здесь минимум зависимостей от сложной TC-инфраструктуры, но можно проверить assets, research, recipes, gas simulation и серверную синхронизацию.

### P2 — эссенция и utility machines

- Essentia Funnel;
- Aspectalyzer без computer integration, затем optional peripheral;
- Kinetic/Corporeal Attractors + Soul Mould;
- Thaumic Restorer;
- Dynamism Tablet и Remote Placer после permission model.

### P3 — фокусы и талисманы

- семь базовых фокусов;
- два талисмана и knowledge-sharing tome;
- exact vis costs/upgrades/FX/NBT;
- projectile and block-entity safety GameTests.

### P4 — enchanting и transvector

- 14 зачарований и compatibility manager;
- Osmotic Enchanter multiblock/process/GUI;
- Transvector Interface/Dislocator/Binder;
- capability recursion, unload and cross-chunk tests.

### P5 — soul, summon, movement

- Cursed Spirit's Blade и три уровня Soul Aspect;
- Tablet of Necromancy;
- Levitational Locomotive + Relay;
- entity/block ownership, NBT and chunk boundary tests.

### P6 — elemental agriculture

- шесть elemental fires;
- farmland, crops, loot and potions;
- bonemeal config, world interaction and effect tests.

### P7 — KAMI целиком

- dimensional shards и Bedrock Dimension;
- Ichor → Ichorcloth/Ichorium → wand components;
- полный base/awakened armor and tools;
- pouch/ring/mirror/amulet/foci;
- Celestial Gateway and Recall;
- fresh-world acquisition, save/reload, dedicated server, multiplayer и visual acceptance.

## 13. Acceptance-критерии полного порта

Порт Thaumic Tinkerer нельзя считать завершённым, пока одновременно не выполнено:

1. Registry manifest покрывает все активные legacy blocks/items/subtypes/enchantments/recipes.
2. Все 77 подтверждённых research entries имеют правильные parents, coordinates, aspects, warp, pages и executable rewards.
3. Ни одна страница не обещает неработающую механику; скрытые/отключённые legacy элементы остаются неактивными.
4. Все machines проходят place/use/save/reload/break/drop и redstone tests.
5. Все переносимые inventories и действия защищены серверной валидацией.
6. Focus costs, upgrades, target rules и NBT совпадают с оригиналом.
7. KAMI проходит полный survival acquisition cycle без creative substitutions.
8. Dimension/gateway проходят fresh-world и cross-dimension tests.
9. Выполнены focused JVM tests, Forge GameTests, dedicated-server boot и минимум один multiplayer сценарий.
10. Отдельно проведена клиентская визуальная приёмка GUI, моделей, held/item renders, armor, particles и HUD. Успешная сборка означает только `COMPILED_NOT_VISUALLY_VERIFIED`.

## 14. Следующий обязательный артефакт

Перед реализацией нужен машинно-проверяемый `THAUMIC_TINKERER_MANIFEST.json` с одной записью на каждый registry subtype и исследование. Текущий аудит даёт полную функциональную карту, но точные числа рецептов, vis, essentia, instability, research coordinates/parents и конфигурационные defaults должны быть извлечены из методов регистрации и конструкторов JAR, а не угаданы по локализации.
