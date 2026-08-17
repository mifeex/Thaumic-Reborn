# Как редактировать аспекты, сканы, рецепты и Таумономикон

Контент первой вертикали загружается из JSON. После изменения файлов запустите
`/reload` в мире. Если JSON неверный, точная ошибка и имя файла появятся в
`logs/latest.log`.

## Временно отключённые определения

В JSON аспектов, сканов, категорий и исследований можно добавить:

```json
{
  "inactive": true,
  "inactive_reason": "referenced item is not implemented"
}
```

Для аспекта или скана такая запись полностью пропускается reload listener и не
заменяет aspect-награду сканирования. Исследование остаётся видимым узлом
Таумономикона, но не участвует в автоматическом открытии и не даёт доступ к
отсутствующему рецепту. `inactive_reason` предназначен для редактора и
загрузчиком не интерпретируется.

Проверка `inactive` выполняется раньше обязательных полей. Это позволяет
хранить ещё не адаптированные legacy target и страницы без ложных ошибок
валидации. Для включения записи недостаточно убрать флаг: все её современные
registry ID, родители, категории, аспекты и рецепты должны уже существовать.

## Быстрая карта файлов

| Что меняется | Где находится |
|---|---|
| Аспекты и формулы их объединения | `src/main/resources/data/thaumic_reborn/thaumcraft/aspects/` |
| Иконки аспектов | `src/main/resources/assets/thaumic_reborn/textures/aspects/` |
| Состав аспектов у блоков, предметов и сущностей | `src/main/resources/data/thaumic_reborn/thaumcraft/scans/` |
| Обычные Minecraft-рецепты | `src/main/resources/data/thaumic_reborn/recipes/` |
| Вкладки Таумономикона | `src/main/resources/data/thaumic_reborn/thaumcraft/categories/` |
| Исследования и страницы Таумономикона | `src/main/resources/data/thaumic_reborn/thaumcraft/research/` |
| In-world сборка составных структур | `src/main/resources/data/thaumic_reborn/thaumcraft/constructions/` |
| Русские названия и тексты | `src/main/resources/assets/thaumic_reborn/lang/ru_ru.json` |
| Английские названия и тексты | `src/main/resources/assets/thaumic_reborn/lang/en_us.json` |

## Триггеры составных структур

Каждая поддерживаемая in-world сборка имеет отдельный JSON. Нельзя оставлять
способ активации неявным:

```json
{
  "id": "research_table",
  "handler": "research_table_pair",
  "trigger": {
    "type": "item",
    "item": "thaumic_reborn:scribing_tools",
    "consume": 1
  },
  "research": "",
  "vis": {}
}
```

Для жезла используется `"trigger": {"type": "wand", "consume": 0}`.
`research` содержит lowercase ID исследования или пустую строку. `vis`
содержит base-cost в целых единицах до модификатора наконечника и скидок
экипировки.

Поле `handler` выбирает проверенную Java-реализацию геометрии. Геометрию
нельзя произвольно переименовать или заменить только JSON-массивом: в
оригинальном TC4 display blueprint находился в рецепте, а ориентация,
пустые клетки, специальные части и замена block metadata выполнялись
отдельными методами `WandManager.fit*/replace*`.

## Создание аспекта

Скопируйте один JSON из папки `thaumcraft/aspects` и поменяйте поля:

```json
{
  "id": "motus",
  "order": 90,
  "color": "CDCCF4",
  "icon": "thaumic_reborn:textures/aspects/motus.png",
  "components": [
    "aer",
    "ordo"
  ]
}
```

- `id` — уникальный идентификатор строчными латинскими буквами.
- `order` — положение в списке аспектов на Столе исследований. Меньшее число
  показывается раньше.
- `color` — RGB без `#`.
- `icon` — путь к PNG. Иконку положите в указанную папку.
- `components` — пустой массив для первичного аспекта или ровно два уже
  существующих аспекта для составного.

Текущая панель рассчитана максимум на 25 аспектов (пять строк по пять
столбцов). Для большего количества потребуется прокрутка интерфейса.

Отдельного JSON-рецепта для объединения аспектов нет. Формула автоматически
берётся из `components`: пример выше создаёт `Motus` из `Aer + Ordo` в любом
порядке. Первое успешное создание даёт 3 очка нового аспекта, повторное — 1.

Размещение аспекта на поле исследования расходует одну его единицу. После
расходования последней единицы счётчик сразу становится равен `0`, а иконка
темнеет. Сканирование или создание, вновь поднявшее запас выше нуля,
автоматически возвращает иконке исходный цвет.

Добавьте название в оба lang-файла:

```json
"aspect.thaumic_reborn.motus": "Движение"
```

## Состав аспектов у изучаемых целей

В режиме fidelity Таунометр изучает только цели с активным явным JSON в
`thaumcraft/scans`. В текущих resources находятся 140 таких определений; ещё
259 перенесённых legacy scans помечены `inactive`, пока их target и аспекты не
адаптированы. Явный JSON всегда имеет приоритет.

Для compatibility с объектами без классического определения существует
heuristic по типу, Minecraft-тегам и ID цели. Он является opt-in SERVER
настройкой и по умолчанию выключен:

```toml
[scanning]
automaticScanFallback = false
```

Файл `thaumic_reborn-server.toml` находится в `serverconfig` конкретного мира.
После изменения перезапустите мир или dedicated server. При `true`, например,
вода может получить Aqua, лава — Ignis и Terra, животные — Victus, а водные
животные — Victus и Aqua. Это совместимый вывод, а не доказанные TC4 data.

Выключение fallback запрещает только создание новых inferred scans. Если
`scanKey` уже был сохранён в player knowledge, его состав продолжает
интерпретироваться и отображаться: смена config не повреждает старое
сохранение.

Чтобы точно назначить или изменить аспекты конкретной цели, создайте JSON в
`thaumcraft/scans`. Пример для предмета:

```json
{
  "type": "item",
  "target": "minecraft:feather",
  "display": "item.minecraft.feather",
  "aspects": [
    {
      "id": "aer",
      "amount": 2
    }
  ]
}
```

`type` принимает `block`, `item`, `entity` или `phenomenon`. Последний тип
используется для стабильных не-registry целей, например
`thaumic_reborn:aura_node`. Выпавший предмет использует определение `item`.
Если это `BlockItem` и отдельного `item`-определения нет,
он автоматически использует `block`-определение того же блока. Поэтому
поставленный и выпавший камень имеют один `scanKey` и не дают награду дважды.

Примеры переопределений воды, лавы и животного:

```json
{
  "type": "block",
  "target": "minecraft:water",
  "display": "block.minecraft.water",
  "aspects": [
    { "id": "aqua", "amount": 4 }
  ]
}
```

```json
{
  "type": "block",
  "target": "minecraft:lava",
  "display": "block.minecraft.lava",
  "aspects": [
    { "id": "ignis", "amount": 4 },
    { "id": "terra", "amount": 1 }
  ]
}
```

```json
{
  "type": "entity",
  "target": "minecraft:pig",
  "display": "entity.minecraft.pig",
  "aspects": [
    { "id": "victus", "amount": 3 }
  ]
}
```

Для других модов используется их полный ID, например
`"target": "examplemod:custom_animal"`. После добавления или изменения JSON
выполните `/reload`.

Чтобы явно запретить изучение цели, оставьте список пустым:

```json
{
  "type": "item",
  "target": "examplemod:decorative_item",
  "aspects": []
}
```

Таумометр всё равно полностью выполнит анимацию сканирования, после чего
справа снизу без фона появится текст «Этот предмет нельзя изучить».

Если цель содержит составной аспект, игрок должен знать его компоненты.
Например, для `humanus = bestia + cognitio` отсутствие `cognitio` не даст
изучить предмет и подсказка потребует сначала изучить именно Cognitio. Проверка
проходит по всей цепочке составных аспектов, но в подсказке сначала называется
ближайший недостающий компонент.

Уже изученная цель повторно не сканируется: удержание и полоса прогресса для
неё не запускаются. Её название и записанный при первом изучении состав
аспектов сразу отображаются в линзе Таумометра.

## Обычные рецепты крафта

Они находятся в `data/thaumic_reborn/recipes` и используют обычный формат
Minecraft 1.20.1: `minecraft:crafting_shaped` или
`minecraft:crafting_shapeless`.

Чтобы показать рецепт на странице Таумономикона, укажите его полный ID:

```json
{
  "type": "recipe",
  "title": "research.thaumic_reborn.example.recipe.title",
  "recipe": "thaumic_reborn:knowledge_fragment"
}
```

Если recipe реализует общий `AspectCostProvider`, Таумономикон автоматически
показывает под схемой все ненулевые затраты в едином виде: цветная иконка
аспекта и точное число в правом нижнем углу. Текущие
`thaumic_reborn:arcane_shaped` и `thaumic_reborn:arcane_shapeless`
предоставляют свои значения `vis` автоматически. Тот же контракт предназначен
для рецептов матрицы/инфузии и других механик, расходующих аспекты.

Для действия палочкой или другой страницы без runtime-рецепта стоимость можно
описать прямо на странице. Список не ограничен шестью примальными аспектами и
при нехватке ширины переносится на следующие строки:

```json
{
  "type": "text",
  "title": "research.thaumic_reborn.example.action.title",
  "body": "research.thaumic_reborn.example.action.body",
  "aspect_costs": [
    { "id": "aer", "amount": 25 },
    { "id": "praecantatio", "amount": 16 },
    { "id": "instrumentum", "amount": 8 }
  ]
}
```

Для recipe-страницы явно заданный `aspect_costs` имеет приоритет над
автоматической стоимостью recipe. Это нужно только для особых действий;
обычный Arcane Workbench рецепт не должен дублировать свой `vis` в research
JSON. Не добавляйте стоимость к обычному vanilla-крафту, если он ничего
магического не расходует.

После `/reload` проверьте рецепт командой:

```text
/recipe give @s thaumic_reborn:knowledge_fragment
```

## Создание вкладки Таумономикона

Создайте JSON в `thaumcraft/categories`:

```json
{
  "id": "alchemy",
  "title": "screen.thaumic_reborn.thaumonomicon.category.alchemy",
  "icon": "minecraft:brewing_stand",
  "background": "thaumic_reborn:textures/gui/gui_researchback.png",
  "order": 20
}
```

- `id` используется исследованиями в поле `category`.
- `title` — ключ локализации.
- `icon` — ID предмета на боковой вкладке.
- `background` — фон дерева. Текущий классический фон можно не менять.
- `order` — порядок вкладок. Первые девять идут слева, следующие девять
  справа.

После этого добавьте перевод `title` в `ru_ru.json` и `en_us.json`.

Production-категория `research`, которая использовалась только служебной веткой
`test_research_*`, удалена. Для продолжения основной прогрессии используйте
существующую классическую категорию `basics` или создайте содержательную
категорию по схеме выше. `first_discovery` теперь находится в `basics`.

## Создание исследования

Создайте JSON в `thaumcraft/research`:

```json
{
  "id": "alchemy_start",
  "category": "alchemy",
  "icon": "minecraft:glass_bottle",
  "title": "research.thaumic_reborn.alchemy_start.title",
  "subtitle": "research.thaumic_reborn.alchemy_start.subtitle",
  "concealed": true,
  "parents": [
    "basics"
  ],
  "hidden_parents": [
    "first_discovery"
  ],
  "reveal_when": {
    "type": "any_of",
    "conditions": [
      {
        "type": "scan",
        "id": "item:minecraft:glass_bottle"
      },
      {
        "type": "criterion",
        "id": "crafted:minecraft:brewing_stand"
      }
    ]
  },
  "unlock_when": {
    "type": "aspect_known",
    "id": "aqua"
  },
  "x": 0,
  "y": 0,
  "pages": [
    {
      "type": "text",
      "title": "research.thaumic_reborn.alchemy_start.page.title",
      "body": "research.thaumic_reborn.alchemy_start.page.body"
    },
    {
      "type": "recipe",
      "title": "research.thaumic_reborn.alchemy_start.recipe.title",
      "recipe": "minecraft:brewing_stand"
    }
  ]
}
```

- `category` — вкладка из `thaumcraft/categories`.
- `icon` — предмет внутри узла. Для классической 32×32 research-картинки вместо
  него укажите `icon_resource`; одновременно задавать оба поля нельзя.
- `x`, `y` — положение центра узла относительно центра дерева.
- `parents` — все перечисленные исследования должны быть завершены.
- `hidden_parents` — такие же обязательные исследования, но линии к ним в
  дереве не рисуются. Это аналог `setParentsHidden` из TC4.
- `revealed_by` — необязательное дополнительное исследование-разблокировщик.
- `concealed: false` позволяет показать узел после `reveal_when`, даже если
  родители ещё не завершены; `true` дополнительно требует завершить всех
  `parents` и `hidden_parents`, прежде чем узел вообще появится.
- `reveal_when` — условие первого появления исследования в книге.
- `unlock_when` — дополнительное условие доступности уже появившегося узла.
- `virtual: true` сохраняет служебный классический research marker в
  progression, но не рисует отдельный узел/страницу.
- `pages` поддерживает `text`, `recipe` и точечный
  `compound_crafting`. Последний сейчас предназначен для runtime descriptor
  `thaumic_reborn:node_jar_capture`, а не для произвольной картинки.
- Любая страница может содержать `aspect_costs`; это общий UI-контракт для
  действий палочкой и механик без Minecraft `Recipe`. Recipe-страницы обычно
  получают стоимость автоматически через `AspectCostProvider`.

Появление и завершение хранятся отдельно, как в классическом TC4. Если
сканирование однажды проявило скрытое исследование, оно больше не исчезнет
даже после изменения временного состояния игрока. Завершённое, доступное и
закрытое состояния отображаются разными рамками и подписями. Линии дерева
строятся только по `parents`, а не по порядку файлов.

### Серверное завершение ручного исследования

Ручное исследование нельзя завершать прямой записью ID из item NBT. Единственная
точка перехода в completed state — `ResearchCompletionService`. После
серверного решения Notes игровая механика должна:

```java
ResearchCompletionService.markDiscoveryReady(knowledge, researchId);
ItemStack discovery = DiscoveryItem.create(researchId);
```

`DiscoveryItem.create` пишет согласованный payload:
`Research`, `DiscoveryVersion=1`, `ValidatedResearch`. При использовании
Discovery сервис заново проверяет payload, player-owned claim, существование и
active state определения, `auto_unlock`, reveal, `parents`, `hidden_parents`,
`revealed_by`, `reveal_when` и `unlock_when`.

Предмет старого формата только с `Research`, подделанный payload и Discovery
без server claim безопасно отклоняются и не расходуются. Автоматической миграции
legacy Discovery нет: по одному item NBT нельзя достоверно восстановить факт
решения Notes. Общие данные player knowledge хранятся в NBT v3 и мигрируют из
v1/v2, но эта миграция не преобразует старые предметы Discovery.

### Разметка текстовых страниц

Текст из ключа `body` поддерживает классическую разметку Тауномикона.
Названия тегов регистронезависимы:

| Тег | Результат |
| --- | --- |
| `<BR>`, `<br>`, `<br/>` | Перенос на следующую строку |
| `<LINE>`, `<hr>` | Горизонтальный разделитель |
| `<IMG>...</IMG>` | Картинка отдельным центрированным блоком |

Формат изображения:

```text
<IMG>namespace:textures/path/image.png:x:y:width:height:scale</IMG>
```

Например:

```text
Первый абзац.<BR><BR>
<IMG>thaumcraft:textures/misc/research4.png:0:0:128:128:.6</IMG>
<BR>Текст под изображением.<LINE>Следующий раздел.
```

- `x`, `y`, `width`, `height` задают область исходной текстуры.
- `scale` задаёт масштаб этой области на странице.
- Слишком широкое изображение автоматически уменьшается до ширины страницы.
- Высота текста рассчитывается вместе с изображениями и разделителями; шрифт
  уменьшается только тогда, когда вся страница иначе не помещается.
- Некорректный или неизвестный тег остаётся видимым текстом — ошибка в
  локализации не удалит часть страницы молча.

В перенесённых текстах TC4 используются только `BR`, `LINE` и `IMG`. Все
оригинальные текстуры, на которые ссылаются текущие русские и английские
страницы, уже включены в сборку в пространстве `assets/thaumcraft`.
Жирный шрифт, подчёркивание и цвета по-прежнему задаются штатными кодами
Minecraft, например `§l`, `§n`, `§5` и `§r`.

### Условия появления и доступности

Любое условие имеет `type`. Условия можно вкладывать друг в друга:

```json
{
  "type": "all_of",
  "conditions": [
    {
      "type": "research_completed",
      "id": "first_discovery"
    },
    {
      "type": "any_of",
      "conditions": [
        {
          "type": "scan",
          "id": "entity:minecraft:enderman"
        },
        {
          "type": "scan",
          "id": "item:minecraft:ender_pearl"
        }
      ]
    },
    {
      "type": "warp",
      "measure": "non_temporary",
      "minimum": 5
    }
  ]
}
```

Поддерживаемые типы:

| `type` | Поля | Значение |
| --- | --- | --- |
| `always` | — | Всегда выполнено |
| `all_of` | `conditions` | Выполнены все вложенные условия |
| `any_of` | `conditions` | Выполнено хотя бы одно |
| `not` | `condition` | Вложенное условие не выполнено |
| `research_completed` | `id` | Исследование завершено |
| `research_revealed` | `id` | Исследование уже появилось в книге |
| `scan` | `id` | Цель изучена Таумометром |
| `scan_aspect` | `id` | Изучена хотя бы одна цель, содержащая аспект |
| `aspect_known` | `id` | Игрок открыл аспект |
| `aspect_amount` | `id`, `minimum` | В запасе есть указанное количество аспекта |
| `warp` | `measure`, `minimum` | Достигнут порог варпа |
| `criterion` | `id` | Выполнено игровое событие-критерий |

Формат ID сканирования совпадает с сохранёнными целями Таумометра:

```text
block:minecraft:stone
item:minecraft:ender_pearl
entity:minecraft:enderman
```

Для `warp.measure` доступны:

- `permanent` — только постоянный варп;
- `normal` — обычный, снимаемый варп;
- `temporary` — временный варп;
- `non_temporary` — постоянный + обычный. Это классическое поведение условий
  TC4 и значение по умолчанию;
- `total` — сумма всех трёх типов.

Некоторые `criterion` записываются автоматически:

```text
advancement:minecraft:story/enter_the_end
crafted:minecraft:brewing_stand
smelted:minecraft:iron_ingot
```

Для собственной механики критерий записывается на сервере так:

```java
ResearchProgressService.recordCriterion(
        serverPlayer,
        "thaumic_reborn:entered_outer_lands",
        "outer_lands"
);
```

Варп добавляется через тот же серверный сервис; после изменения условия
исследований пересчитываются и знания сразу синхронизируются:

```java
ResearchProgressService.addWarp(
        serverPlayer,
        WarpType.PERMANENT,
        1,
        "forbidden_research"
);
```

Для обычной последовательности достаточно:

```json
{
  "concealed": true,
  "parents": [
    "previous_research"
  ]
}
```

Такой узел появится сразу после завершения `previous_research`. Активное
исследование `basics` уже настроено как практический пример: оно появляется
после `first_discovery`.

### Debug-показ всего дерева

Клиентский конфиг находится в:

```text
config/thaumic_reborn-client.toml
```

По умолчанию отладочный показ отключён:

```toml
[research]
debugShowAllResearch = false
```

Для проверки координат и связей временно установите значение `true` и
переоткройте игру. В Таумономиконе станут видимы все зарегистрированные узлы
во всех вкладках. Этот параметр **только показывает дерево**: он не завершает
исследования, не выдаёт записки, не обходит серверные условия и не изменяет
сохранённые знания игрока. Перед обычной игрой верните `false`.

### Как выдать тестовые записки для нового исследования

Текущий гекс-пазл общий для всех записок. Для проверки нового ID:

```text
/give @s thaumic_reborn:research_notes{Research:"alchemy_start",Placements:{}}
```

После решения записки превратятся в Открытие с ID `alchemy_start`, а его
использование запишет исследование в знания игрока. Именно серверное завершение
пазла создаёт claim, необходимый `ResearchCompletionService`.

Не заменяйте этот сценарий прямой выдачей Discovery:

```text
/give @s thaumic_reborn:discovery{Research:"alchemy_start"}
```

Такой legacy-предмет не имеет versioned payload и должен быть отклонён. Даже
внешне согласованный payload не завершит исследование у игрока без серверного
claim о решённых Notes.

Важно: JSON полностью создаёт вкладку, узел, условия, страницы и отображаемый
рецепт. Автоматический способ получения записок в survival для каждого нового
исследования пока задаётся отдельно игровой механикой или рецептом; одна запись
в Таумономиконе сама по себе предмет записок не выдаёт.

## API составных жезлов

Компоненты не зашиты в Java. Rod добавляется в
`data/thaumic_reborn/thaumcraft/wands/<id>.json`:

```json
{
  "kind": "rod",
  "id": "example",
  "capacity_vis": 75,
  "translation_key": "wand.rod.example",
  "recharge_aspects": ["aer"],
  "recharge_interval_ticks": 200,
  "recharge_centivis": 1,
  "staff": false,
  "runes": false
}
```

Cap добавляется в ту же папку
`data/thaumic_reborn/thaumcraft/wands/<id>.json`:

```json
{
  "kind": "cap",
  "id": "example",
  "cost_modifier": 1.0,
  "translation_key": "wand.cap.example",
  "special_aspects": ["aer"],
  "special_cost_modifier": 0.95
}
```

После resource reload готовый stack создаётся через единый публичный вход:

```java
ItemStack wand = WandApi.createWand("greatwood", "gold", true);
ItemStack sceptre = WandApi.createSceptre("silverwood", "thaumium", false);
ItemStack staff = WandApi.createStaff("primal_staff", "void", true);
ItemStack debug = WandApi.createCodexWand();
```

Последний предмет имеет `1000` vis каждого primal, сразу заполнен и намеренно
не имеет recipe. `WandApi` отклоняет обычный rod для staff и staff rod для
wand/sceptre. `WandApi.isCraftingTool(stack)` возвращает `true` для wand и
sceptre, но `false` для staff. `WandApi.acceptsFocus(stack)` возвращает
`false` только для sceptre. Изменение vis выполняйте через серверный
`WandVisService`, чтобы сохранить атомарность и синхронизацию.

## Страница наполнения для проверки UI

Research page с `"type": "infusion"` принимает `output`, `central`,
`components`, `aspect_costs`, `instability` и необязательный `detail`.
Это только отображение в книге, не исполняемый рецепт. Готовые примеры
находятся в `research/infusion_layout_test.json`; вкладка открывается
автоматически и содержит варианты на 3–12 компонентов.

## Минимальная проверка после изменений

1. Выполнить `/reload`.
2. Проверить `logs/latest.log`: количество загруженных aspects, scans,
   categories и research.
3. Переоткрыть Таумономикон — определения синхронизируются при открытии.
4. Проверить обе локализации.
5. Перед сборкой выполнить `./gradlew test build`.

## Диагностический лог Стола исследований и Таумономикона

Весь путь действия записывается в `logs/latest.log` с маркером
`TCM-RESEARCH`: открытие и закрытие экрана, начало перетаскивания, выбранный
слот, отправленный button ID, серверная расшифровка, количества аспектов до и
после, загрузка и сохранение записок, синхронизация клиента, вкладки и открытые
страницы Таумономикона. Запись `SERVER_RESEARCH_PROGRESS` отдельно показывает,
какие исследования появились или автоматически завершились после скана,
крафта, изменения варпа либо завершения родительского исследования.

После воспроизведения ошибки приложите полный `latest.log`. При запуске из
проекта это файл:

```text
run/logs/latest.log
```

Для быстрой выборки только исследовательского пайплайна:

```bash
rg "TCM-RESEARCH" run/logs/latest.log
```

Важно присылать лог сразу после воспроизведения и до следующего запуска игры:
при новом запуске `latest.log` заменяется.
