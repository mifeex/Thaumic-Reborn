# Статус вертикалей в дереве 1.5.68

| Область | Реализовано | Текущее доказательство |
|---|---|---|
| Forge 1.20.1 / Java 17 | Регистрации и run-конфигурации клиента, сервера и Forge GameTest | Forge GameTest 7/7 на 2026-07-28; обычный client/dedicated запуск не проверен |
| Аспекты и знания | 48 аспектов; player capability NBT v3; scans/research/reveal/criteria/warp | Content validator и unit/GameTest; save/reload и multiplayer требуют runtime-проверки |
| Сканирование | Block/item/entity, `BLOCK_TAG`/`ITEM_TAG` и `PHENOMENON` aura node; 303 active explicit definitions; legacy SRG/Ore Dictionary/entity targets переведены на registry ID и tags Minecraft/Forge 1.20.1 | Полное дерево scan-ресурсов проверяется на уникальность активных `type + target`; Forge GameTest выполнил resource reload, создал/загрузил мир и подтвердил единственный `block:thaumic_reborn:ancient_stone`, устранив crash Create World/load world; 40 объектов без современного эквивалента остаются inactive |
| Research Table / Thaumonomicon | Собственные Menu/Screen и research completion; legacy flags выбирают оригинальные PRIMARY/ROUND/SECONDARY/HIDDEN/SPECIAL atlas frames, доступные узлы используют пользовательский 800-ms grayscale pulse; Research Notes, Discovery и свиток стола окрашиваются цветом первого research aspect | Resolver/unit и source-fidelity tests; клиентские цвета и рамки остаются `COMPILED_NOT_VISUALLY_VERIFIED` |
| Жезлы и vis | 6 independent centivis; единый atomic player-aware расход с cap/form, aspect-specific gear, активными Flux Flu/Phage и extension event для warp/других источников | Unit и GameTest; отдельный Silverwood rod скрыт из creative |
| Aura node | TC4 worldgen (`1/36` чанка), все type/modifier, произвольные primal/compound aspect pools, UUID, v1→v2 persistence/sync, scan, зарядка, регенерация, unstable discharge, fading decay, node bullying в радиусе ±4, Hungry pull/orbit/throw/block-eating и classic break burst/essence drops | 212/212 unit и 18/18 Forge GameTests общего набора; client resource reload без missing texture узла, но Hungry physics, node zap/burst и type rows ещё требуют визуальной Play Mode приёмки |
| Eldritch Guardian / Obelisk | Guardian и Warden используют classic multipart model; обелиск в радиусе 6 каждые 20 тиков даёт 40 тиков Resistance/Regeneration attempt, лечит 1 HP и тянет чёрный type-5 wisp; world/altar spawn только ночью на `MOTION_BLOCKING_NO_LEAVES` surface, biome weight сохранён `1` | Unit `212/212`, Forge GameTest `18/18`, client particle-atlas reload; визуальная траектория wisp и статистика ночного спавна вручную не приняты |
| Goggles of Revealing | Vanilla head armor, 5% vis discount, постоянное раскрытие node, exact arcane recipe | Unit/source; armor layer и visibility требуют client screenshot |
| NODEJAR | Точная 3×4×3 структура, 6×70 base vis, research gate, любой удерживаемый casting tool с достаточным modifier-adjusted vis, atomic capture/place, codec/ledger/creative stack | Unit и source; visual/save/reload/multiplayer acceptance не проведены |
| Thaumcraft Table / Arcane Workbench | Wand conversion с zero-vis, wand slot, vanilla + arcane recipes и research/vis transaction; динамический sceptre recipe повторяет `" TF"/" RT"/"T  "`; crafting grid и wand slot сохраняются slot-indexed NBT | Сфокусированные GameTests скипетра и NBT round-trip добавлены, но текущий dedicated GameTest server блокируется client-only `HumanoidModel` до выполнения тестов; полный stop/start и клиентский экран не подтверждены |
| Arcane Stone | 9 blocks за 8 stone + air shard, 1 Terra + 1 Ignis, `ARCANESTONE` | Runtime recipe, unit и GameTest |
| Классические ресурсы | 80 provenance-recorded PNG/OGG/resources; один deterministic OBJ adapter | SHA-256 catalog; новые модели/UI имеют статус `COMPILED_NOT_VISUALLY_VERIFIED` |

Legacy Discovery, в котором есть только `Research` без
`DiscoveryVersion=1`, совпадающего `ValidatedResearch` и серверного
`thaumic_reborn:discovery_ready/<id>` claim, безопасно отклоняется и не
расходуется. Автоматическая миграция такого предмета намеренно не реализована:
нельзя восстановить факт серверного решения Notes из одного доверенного
клиентом NBT.

На 2026-07-28 подтверждены content validator, полный unit suite `120/120`,
`./gradlew build` и Forge GameTest `7/7` предыдущего gameplay-прогона.
В 1.4.24 возвращён зафиксированный baseline first-person Таумометра:
`336×292`, offsets `0/0`, GUI model translation `0/0/0`. Client Play Mode,
save/reload мира, обычный dedicated server и два клиента не проверены. Ручной
сценарий:
[`FIRST_DISCOVERY_MANUAL_TEST.md`](FIRST_DISCOVERY_MANUAL_TEST.md).

## Зафиксированное решение по Silverwood

Точная классическая механика конфликтует с текущим запретом на advanced
rods/caps:

- деревянный wand rod имеет capacity `25` vis каждого примала;
- iron wand cap умножает расход на `1.1`;
- `NODEJAR` стоит `70` каждого примала, то есть `77` с iron cap;
- классическая research-цепочка:
  `NODES → NODETAPPER1 → NODEPRESERVE → NODEJAR`;
- `NODEPRESERVE` требует конфигурацию не с wooden rod и не с iron cap;
- Goggles of Revealing создаются через Arcane Workbench.

По прямому решению владельца добавлен готовый Silverwood/iron wand capacity
`100`. Его recipe, Infusion и advanced parent progression пока не добавлены.
Такой заряженный wand оплачивает точные `77` каждого примала для `NODEJAR`;
стоимость и cap modifier не ослаблены.
