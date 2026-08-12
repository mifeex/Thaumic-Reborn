# Master Prompt — Thaumic Reborn (актуальный рабочий контракт)

Скопируй текст ниже в новую задачу Codex, работающую с проектом
`/Users/evgenii/Documents/ThaumcraftModern`.

```text
Ты продолжаешь разработку Thaumic Reborn — самостоятельного faithful-порта
Thaumcraft 4.2.3.5 для Minecraft Forge 1.20.1 / Java 17.

Это не аддон и не «мод, вдохновлённый Thaumcraft». Цель — сохранить игровую
логику, визуальный язык, прогрессию, модели, атмосферу и звук TC4 там, где это
возможно в современной Forge-архитектуре.

## 1. Начало работы и источники истины

Корень проекта:

`/Users/evgenii/Documents/ThaumcraftModern`

Сначала полностью прочитай:

1. `docs/PORT_AUDIT.md`;
2. `docs/CODEX_HANDOFF_2026-07-28.md`;
3. `docs/REFERENCE_BEHAVIOUR.md`;
4. `docs/REFERENCE_ASSET_CATALOG.md`;
5. `docs/CONTENT_EDITING_GUIDE_RU.md`;
6. `docs/RESEARCH_CREATION_GUIDE_RU.md`;
7. `docs/UI_LAYOUT_TUNING_RU.md`.

Оригинальный эталон:

`reference/original/Thaumcraft_1.7.10_4.2.3.5.jar`

SHA-256 оригинального JAR:

`3dba9786966974701578a658d1bb369bf35bdf5f363079f5ac9c4910a39113be`

Версию проекта, путь итогового JAR и состояние рабочей папки всегда определяй
по текущим `gradle.properties`, `build/libs` и исходникам. Не доверяй старым
сообщениям чата или зафиксированным номерам snapshot-версий.

Рабочая папка общая. В ней может идти параллельная работа. Перед правкой:

- проверь актуальный файл, а не предполагай его содержание;
- не откатывай и не перезаписывай чужие изменения;
- не расширяй задачу ради удобного рефакторинга;
- если область пересекается с параллельной работой, остановись и сообщи
  конкретный конфликт.

### Изолированная параллельная работа: Таумометр

Другой чат сейчас занимается только Taumometer, в частности его first-person
моделью, руками, размерами, позиционированием и ресурсами. Эта работа не
должна блокировать разработку механик.

Не изменяй без прямой отдельной задачи:

- `ClientRenderEvents` и first-person hand transforms;
- `ClientThaumometerOverlay` и его ручные UI-константы;
- `models/item/thaumometer.json`;
- `textures/models/scanner.obj`, `scanner.mtl`, `scanner.png`, `scanscreen.png`;
- first-/third-person трансформации или текстуры Таумометра.

Механики сканирования могут использовать общие API знаний и аспектов, но не
должны менять presentation layer Таумометра.

## 2. Неизменные архитектурные правила

1. Forge 1.20.1 и Java 17. Используй современные Forge registrations,
   BlockEntity, Menu/Screen, capabilities, networking и datapack reload.
2. Сервер — единственный источник истины. Клиент может запросить действие и
   отрисовать результат, но не выдаёт награды, не завершает исследования, не
   списывает аспекты/вис/чернила и не создаёт предметы самостоятельно.
3. Каждый сетевой переход проверяется сервером заново: ID, владелец, открытое
   меню, дистанция, предмет в руке, ресурс, prerequisites и состояние мира.
4. Операции расхода атомарны: сначала проверить все условия, затем одним
   переходом изменить ресурсы и выдать результат.
5. Не доверяй NBT предмета как источнику разрешений. Валидируй registry ID,
   player knowledge и server state.
6. Не добавляй Patchouli, Create, AE2, Botania или чужие системы вместо
   Thaumonomicon, аспектов, вис, узлов, essentia, инфузии или варпа. Curios
   допустим позднее только как современная замена Baubles.
7. Не копируй декомпилированный Java-код TC4 механически. Он нужен для
   поведения, последовательности и чисел, а реализация остаётся современной.
8. Данные отделяй от runtime: аспекты, исследования, страницы, сканы, recipe
   definitions, rods/caps и node definitions должны быть data-driven, если это
   не делает серверную валидацию слабее.
9. Не создавай второй registry или дублирующее состояние игрока/мира.
10. Не называй механику «как в TC4» только после компиляции. Всегда указывай
    доказательство: bytecode/resource audit, unit test, GameTest, client visual,
    dedicated server или multiplayer.

## 3. Fidelity ресурсов, текстур и UI

### 3.1 Текстуры и модели

- Оригинальный JAR и подтверждённые ресурсы TC4 — основной визуальный эталон.
- Не генерируй и не дорисовывай «похожую» текстуру, не делай blur/upscale,
  не меняй палитру и не заменяй классический элемент vanilla UI без прямого
  подтверждённого референса.
- До использования ассета установи: происхождение, физический размер PNG,
  atlas coordinates, нужный crop и render pass.
- Не смешивай source coordinates внутри атласа с destination coordinates на
  экране. Не меняй `SOURCE_X/Y/W/H`, когда нужно сдвинуть элемент.
- Для OBJ не подменяй настоящую геометрию плоским atlas crop. Отдельные
  материалы и passes оригинала сохраняй отдельно, если так устроен TC4.
- Если скриншот имеет неясное происхождение, помечай его
  `UNVERIFIED_ADDON_OR_MODPACK` и не переноси его решение в core.
- Уважай происхождение классических ассетов: не утверждай право на публичное
  распространение только потому, что ресурс есть в локальном reference JAR.

### 3.2 UI-протокол

Перед любым изменением GUI/renderer:

1. Назови экран и конкретный элемент.
2. Назови оригинальный TC4 GUI/renderer и source texture.
3. Зафиксируй atlas size, source X/Y/W/H, destination X/Y/W/H, anchor,
   render order, blend/tint, resolution и GUI Scale.
4. Меняй одну независимую область за итерацию.
5. Все ручные размеры/смещения собирай в один подписанный блок констант.
6. Объясняй пользователю, какой параметр двигает вправо/вниз, масштабирует
   frame/glass/content/hands и зависит ли он от GUI Scale.

Пользователь утверждает финальную композицию численными значениями. Агент
пишет код, сохраняет границы слоёв и объясняет параметры.

После UI-правки обязательны новая сборка, полный рестарт клиента, screenshot
с указанными resolution и GUI Scale. До этого статус только
`COMPILED_NOT_VISUALLY_VERIFIED`.

Не допускается:

- подбирать texture crop на глаз;
- добавлять лишние рамки, тени, полосы или декоративные элементы;
- скрывать вкладки/узлы ради исправления геометрии;
- менять first-person и third-person посадку предмета одним экспериментом;
- считать успешный GameTest визуальной проверкой.

### 3.3 Принятый стиль интерфейсов

Thaumonomicon, Research Table, Arcane Workbench и другие ключевые экраны
должны сохранять пергамент, руны, тёмный металл, оригинальные звуки и
атмосферу TC4. Не делай финальный UI обычным vanilla inventory/crafting screen.

В Thaumonomicon:

- фон полностью заполняет внутреннюю область рамки без чёрных краёв;
- категории — классические боковые вкладки, а не отдельные панели;
- иконки исследований меньше рамки и центрированы;
- доступный ручной узел мигает штатной рамкой без дополнительной квадратной
  обводки;
- recipe result и ingredients центрированы, source crop не захватывает
  соседние полосы атласа;
- страницы, изображения и текст не выходят за границы книги.

## 4. Аспекты и очки исследования

### 4.1 Состояние и данные

- Шесть примальных аспектов: Aer, Terra, Ignis, Aqua, Ordo, Perditio.
- Аспекты, порядок, цвет, компоненты и display data читаются из data JSON.
- Player capability хранит known aspects и их количества, сканы, revealed и
  completed research. Состояние сохраняется, копируется при respawn и
  синхронизируется сервером.
- Новый игрок получает по 5 очков каждого примального аспекта.

### 4.2 Семантика очков

- Комбинация аспектов проверяется сервером по components registry.
- На попытку расходуется по одному очку каждого выбранного аспекта только при
  корректной серверной операции.
- Первое открытие составного аспекта даёт 3 очка; повторное — 1.
- Неизвестный аспект нельзя использовать как известный только потому, что он
  есть в registry.
- При нуле аспект остаётся видимым в палитре, но темнеет; он не удаляется из
  known knowledge.
- Любое размещение аспекта в research puzzle и стирание расходует чернила по
  проверенному серверному переходу.

Не добавляй выдуманные «очки исследования», бонусы, стоимости или случайные
награды. Если значение TC4 не подтверждено, сначала исследуй bytecode/data и
явно пометь решение как современную временную адаптацию.

## 5. Исследования и Таумономикон

### 5.1 Data-driven research

Исследования находятся в
`src/main/resources/data/thaumcraftmodern/thaumcraft/research`.

Для них используются `parents`, `hidden_parents`, `revealed_by`,
`reveal_when`, `unlock_when`, `concealed`, `auto_unlock`, `inactive`, `x`, `y`
и структурированные pages. Положение узлов задаётся в JSON, не Java-кодом.

Правила:

- `inactive=true` сохраняет архивную запись видимой при необходимости, но
  никогда не даёт записку и не завершает знание;
- `auto_unlock=true` завершается только server-side при выполнении условий;
- `concealed=true` не появляется до допустимого reveal;
- `parents` и `hidden_parents` должны быть завершены до ручного получения
  исследования;
- линии дерева строятся только из `parents`;
- test research branch во вкладке `research` существует для проверки current
  mechanics. Не используй её как production progression и не удаляй без
  отдельного решения пользователя.

### 5.2 Ручное исследование

Для доступного активного исследования:

1. Узел мигает.
2. В tooltip показано: completed, locked, ready, нехватка материалов или уже
   существующие notes.
3. ПКМ отправляет только ID исследования серверу.
4. Сервер повторно проверяет availability, completion, наличные paper,
   работоспособные Scribing Tools и отсутствие notes с тем же research ID.
5. Сервер списывает одну бумагу и одну единицу чернил, создаёт notes с
   конкретным ID, добавляет их в inventory либо выбрасывает при полном
   inventory, синхронизирует состояние и играет подтверждённый звук.
6. Notes решаются на Research Table; completed notes превращаются в Discovery.
7. Discovery может завершить только разрешённое исследование после полной
   серверной проверки каждого prerequisite.

Первый приоритет стабилизации: вынести завершение из прямого
`DiscoveryItem.use` в единый `ResearchCompletionService` и покрыть негативные
случаи: unknown/inactive ID, missing parent, failed scan/warp condition,
поддельный Discovery и повторное completion.

Сейчас общий гекс-пазл ещё не data-driven для каждого ID. Не маскируй это:
либо реализуй per-research puzzle definitions отдельной завершённой задачей,
либо честно сохраняй ограничение в документации.

## 6. Следующая игровая вертикаль: жезл → узел → вис → Arcane Workbench

После research security audit следующая вертикаль должна быть законченной,
достижимой без команд и ограниченной по scope.

### 6.1 Базовый жезл

Реализуй только:

- wooden rod;
- iron caps;
- basic iron-capped wooden wand;
- data-driven rod/cap properties;
- шесть независимых vis amounts и capacity;
- save/load/copy/sync NBT;
- server-owned tooltip/state mutation;
- классические базовые recipes после проверки оригинала.

Не начинай foci, staffs, sceptres, advanced rods/caps и wand equipment.

### 6.2 Обычный aura node

Первый узел — настоящий world-owned объект, а не entity без сохранения и не
клиентский эффект. Нужны:

- текущий и maximum запас каждого из шести примальных аспектов;
- normal `NodeType` и normal `NodeModifier`;
- persistence, chunk-safe lifecycle и client sync;
- защита от отрицательных значений, overflow, destroyed block entity и
  удалённого чанка;
- сканирование как phenomenon/node target;
- отображение данных через Таумометр без изменения его параллельной визуальной
  реализации;
- renderer, particles, FX и `nodes.png` только после сверки с TC4 resource и
  renderer;
- gameplay-достижимость. Debug command допустима для теста, но не заменяет
  нормальное получение узла.

В этой вертикали запрещены Hungry, Sinister, Tainted, Unstable, Pure, node
equipment, transducer, stabilizer, relays и recharge pedestal.

### 6.3 Transfer vis: node → wand

Транзакция на сервере:

1. проверить игрока, активный wand, node, дистанцию и loaded chunk;
2. определить дефицит вис по каждому primals type;
3. ограничить transfer capacity жезла и запасом узла;
4. проверить, что предмет не сменился и node не уничтожен;
5. одновременно списать из node и добавить в wand;
6. синхронизировать оба объекта;
7. отправить клиенту только FX/result.

Обязательные негативные тесты: duplicate request, remote node, changed held
item, unloaded/destroyed node, empty node, full wand, capacity overflow и
negative amounts.

### 6.4 Arcane Workbench

Не реализуй его vanilla crafting menu. Нужны собственные BlockEntity, Menu,
Screen, wand slot, crafting grid, vis indicators, own arcane recipe type,
shaped/shapeless serializers, research gate и атомарная серверная транзакция.

Порядок: recipe → research → ingredients → wand → all vis costs → result →
одновременное списание → выдача → sync.

Первый arcane recipe: `ArcaneStone1`, девять Arcane Stone, стоимость
`1 Terra + 1 Ignis`, gate `ARCANESTONE`; recipe page Thaumonomicon должна
читать реальный runtime recipe.

## 7. Scan fidelity и известные аудиты

Explicit TC4-derived scan definitions должны иметь приоритет. Не считать
эвристический fallback parity-механикой. Сначала проведи отдельный аудит
`AutomaticScanDefinitionFactory`: fallback должен быть выключаемым server
config с default `false`, а отсутствие explicit definition должно честно
показывать «нельзя изучить».

Не меняй уже изученный scan повторно и не начисляй его награду повторно.
Проверка известных сложных аспектов должна рекурсивно учитывать компоненты.

## 8. Версии, сборка и проверка

Перед изменением прочитай текущий `mod_version`. При изменении модового
поведения/ресурсов повышай его по следующему patch номеру, не откатывай номер
и не угадывай текущую версию.

Минимальные проверки:

- логика: targeted unit tests и `./gradlew test`;
- registry/datapack/network/menu/block entity: `./gradlew runGameTestServer`;
- итог: `./gradlew build`;
- visual UI: новый screenshot после полного client restart;
- multiplayer-sensitive flow: dedicated server и два клиента, когда стенд
  доступен.

После сборки проверь внутреннюю версию `META-INF/mods.toml` и SHA-256 JAR.
Установку или удаление JAR вне workspace выполняй только по прямому запросу
пользователя; старый JAR при замене перемещай в Корзину, не удаляй безвозвратно.

Всегда разделяй результаты: compilation, unit tests, GameTest, resource reload,
client visual, dedicated server, multiplayer. Не подменяй один вид другим.

## 9. Формат работы и отчёта

Перед кодом коротко сообщи: scope, допущения, конкретные файлы и проверки.
Реализуй один завершённый вертикальный кусок, а не набор пустых заготовок.

После работы выдай:

1. игровой цикл, который реально добавлен/исправлен;
2. изменённые файлы;
3. изменения активных исследований и data definitions;
4. использованные TC4 evidence/resources;
5. результаты каждой категории проверки;
6. то, что остаётся `COMPILED_NOT_VISUALLY_VERIFIED`;
7. точные расхождения с TC4;
8. путь, версию и SHA-256 собранного JAR;
9. короткий ручной сценарий проверки без команд;
10. следующий безопасный шаг.

Никогда не заявляй, что весь Thaumcraft перенесён. Завершённой считается только
конкретная проверенная вертикаль.
```

## Статус документа

Этот файл заменяет старый короткий `MASTER_PROMPT.md`. Исторический
`MASTER_PROMPT_1.4.12.md` оставлен как snapshot, но не является текущим
источником требований.
