# Thaumic Reborn

An unofficial community port of Azanor's Thaumcraft 4.2.3.5 for Minecraft Forge 1.20.1 / Java 17.

For compatibility with existing worlds, the technical mod ID and resource
namespace remain `thaumcraftmodern`. Distribution artifacts use the
`thaumic-reborn` name.

Сейчас реализованы первая вертикаль, следующая gameplay-вертикаль и
world-content foundation из
[`docs/MASTER_PROMPT_1.4.19.md`](docs/MASTER_PROMPT_1.4.19.md):
**сканирование → исследование → жезл → аурный узел → NODEJAR → Arcane
Workbench → Arcane Stone**, а также руды, магические деревья, растения,
поверхностные биомы, структуры, NPC и естественно появляющиеся существа TC4.

Это не готовый полный порт TC4. Полные системы алхимии, Infusion, taint,
создаваемых игроком големов и Outer Lands пока не входят в реализованный
игровой контур.

## Требования

- JDK 17;
- Minecraft Forge 1.20.1-47.4.10;
- запуск только через приложенный Gradle wrapper.

Проверка окружения и сборка:

```bash
java -version
./gradlew test
./gradlew runGameTestServer
./gradlew build
```

Готовый dev-JAR появляется в `build/libs/`.

## Запуск

Клиент:

```bash
./gradlew runClient
```

Dedicated server:

```bash
./gradlew runServer
```

Для обычного dedicated server необходимо самостоятельно прочитать и принять
Minecraft EULA в созданном `run/eula.txt`.

Для изолированной проверки свежего мира можно задать отдельную директорию:

```bash
./gradlew runServer -PrunDirectory=/absolute/path/to/test-run
```

## Команды для тестирования

Открыть все исследования:

```text
/thaumcraft research all
```

Открыть все исследования выбранной категории:

```text
/thaumcraft research category <категория>
```

Открыть все аспекты:

```text
/thaumcraft aspects all
```

World preset `thaumcraftmodern:thaumcraft_modern` сохраняет современный
overworld noise generator и пещерные биомы, добавляя только поверхностные
пятна Magical Forest, Tainted Lands и Eerie. Параметры частоты находятся в
server config. Полный аудит переноса:
[`docs/WORLD_CONTENT_AUDIT_1.5.0.md`](docs/WORLD_CONTENT_AUDIT_1.5.0.md).

## Игровой цикл первой вертикали

1. Создайте элементальный кристалл из осколка аметиста и красителя.
2. Создайте basic wand из двух iron caps и wooden stick, затем примените
   пустой wand к книжной полке. Vis для создания Thaumonomicon не требуется и
   не расходуется.
3. Создайте Thaumometer, Research Table, Scribing Tools и Research Notes.
4. Удерживая Thaumometer:
   - просканируйте блок красного камня, чтобы открыть `Potentia`;
   - держите факел во второй руке и используйте Thaumometer, чтобы открыть `Lux`;
   - просканируйте корову, чтобы проверить скан сущности и открыть `Victus`.
5. Положите Scribing Tools в левый слот Research Table, Research Notes — в
   правый.
6. Соедините якоря пяти ячеек слева направо:
   `Aer → Lux → Ignis → Potentia → Ordo`.
7. Используйте полученное Discovery. В Thaumonomicon откроется скрытая запись и
   рецепт Knowledge Fragment.
8. Создайте Knowledge Fragment из бумаги, осколка аметиста и любого
   элементального кристалла.

Подробный client/dedicated/multiplayer сценарий:
[`docs/FIRST_DISCOVERY_MANUAL_TEST.md`](docs/FIRST_DISCOVERY_MANUAL_TEST.md).

## Архитектура

- аспекты, сканы и страницы исследований загружаются из versioned datapack JSON;
- знания хранятся в player capability NBT v3, мигрируют из v1/v2 и копируются
  при respawn;
- сканирование, расход чернил, состояние пазла, завершение исследования и
  выдача рецепта определяет сервер;
- единственная точка завершения ручного исследования —
  `ResearchCompletionService`: Discovery принимается только с согласованным
  versioned payload и ранее записанным сервером claim о завершённых Notes;
- автоматический вывод аспектов для целей без явного scan JSON выключен по
  умолчанию и включается только server config; явный JSON всегда имеет
  приоритет, а уже сохранённые scan keys продолжают читаться;
- клиент получает только синхронизацию, интерфейс и FX;
- Thaumonomicon и Research Table используют собственные экраны на классических
  пергаментных ресурсах, без Patchouli.

Служебная категория `research` и пять записей `test_research_*` удалены.
Классический legacy research с ID `research` сохранён, а `first_discovery`
находится в категории `basics`. Старые предметы Discovery, созданные до
versioned payload и server claim, безопасно отклоняются; автоматической
миграции таких предметов пока нет.

## Игровой цикл первой тауматургии

1. Создать iron caps и базовый wooden wand.
2. Превратить bookshelf в Thaumonomicon без vis.
3. Найти/просканировать NORMAL aura node и зарядить wand удержанием.
4. Превратить Thaumcraft Table в Arcane Workbench тем же wand; его NBT
   переносится в слот стола без расхода vis.
5. Завершить `GOGGLES`, создать и надеть Goggles of Revealing.
6. Пройти `NODES → NODETAPPER1 → NODEPRESERVE → NODEJAR`.
7. Зарядить готовый Silverwood/iron wand capacity `100`; recipe этого wand
   пока намеренно отсутствует.
8. Построить вокруг node точную структуру 3×4×3 и заключить его за
   `70` каждого примала (`77` с iron cap).
9. Создать 9 Arcane Stone за 8 stone, air shard, `1 Terra + 1 Ignis`.

Arcane Workbench также выполняет обычные vanilla crafting recipes без wand.

Техническое сопоставление с TC4:
[`docs/REFERENCE_BEHAVIOUR.md`](docs/REFERENCE_BEHAVIOUR.md).
Аудит неоднозначных кадров из локальной папки `reference`:
[`docs/SCREENSHOT_AUDIT.md`](docs/SCREENSHOT_AUDIT.md).

## Классические ресурсы

В репозитории находится локально отобранная рабочая копия ресурсов из
пользовательского эталонного JAR. Не публикуйте и не распространяйте сборку с
этими ресурсами без разрешения правообладателей. См.
[`NOTICE-CLASSIC-ASSETS.md`](NOTICE-CLASSIC-ASSETS.md) и
[`docs/REFERENCE_ASSET_CATALOG.md`](docs/REFERENCE_ASSET_CATALOG.md).

## Редактирование контента

Пошаговая инструкция по добавлению аспектов, сканируемых целей, рецептов,
вкладок и исследований:
[`docs/CONTENT_EDITING_GUIDE_RU.md`](docs/CONTENT_EDITING_GUIDE_RU.md).

Отдельный краткий гайд по созданию исследований и тестированию цепочек:
[`docs/RESEARCH_CREATION_GUIDE_RU.md`](docs/RESEARCH_CREATION_GUIDE_RU.md).

Thaumcraft 4.2.3.5 is by Azanor.
