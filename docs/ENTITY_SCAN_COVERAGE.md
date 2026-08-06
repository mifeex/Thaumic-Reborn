# Entity scan coverage: Thaumcraft 4 → ThaumcraftModern

Аудит выполнен по legacy-файлам `data/thaumcraftmodern/thaumcraft/scans/legacy/entity_*.json` и текущей регистрации `ModEntities`/`LegacyMobKind`.

## Итоговые числа

| Слой | Количество |
|---|---:|
| Legacy entity scan-файлов | 69 |
| Уникальных legacy target ID | 67 |
| Неактивных legacy-записей | 3 |
| Текущих `LegacyMobKind` | 30 |
| Текущих custom entity targets со сканом | 40 (включая golems и projectile-like legacy targets) |
| Добавленных в этой актуализации mob-сканов | 2 |
| Технических entity без mob-скана | 7 |

Три legacy-записи не следует включать как отдельные современные ключи:

- `entity_003_skeleton` — wither-skeleton NBT-вариант; старый extractor свёл его к `minecraft:skeleton`, поэтому нужен отдельный NBT/variant-механизм, а не второй общий ключ skeleton.
- `entity_005_creeper` — charged-creeper NBT-вариант; по той же причине не должен перезаписывать обычный creeper.
- `entity_010_cow` — старый дубликат, отключён, поскольку современный cow уже имеет hand-authored scan.

## Legacy vanilla entity ID → современный ID

Основные переименования уже перенесены корректно:

| Legacy 1.7.10 | Современный ID |
|---|---|
| `EntityHorse` | `minecraft:horse` |
| `MushroomCow` | `minecraft:mooshroom` |
| `SnowMan` | `minecraft:snow_golem` |
| `Ozelot` | `minecraft:ocelot` |
| `PigZombie` | `minecraft:zombified_piglin` |
| `LavaSlime` | `minecraft:magma_cube` |
| `EnderDragon` | `minecraft:ender_dragon` |
| `WitherBoss` | `minecraft:wither` |
| `VillagerGolem` | `minecraft:iron_golem` |
| `EntityMinecart*` | соответствующие `minecraft:*_minecart` |
| `EntityEnderCrystal` | `minecraft:end_crystal` |
| `EntityItemFrame` | `minecraft:item_frame` |
| `EntityPainting` | `minecraft:painting` |

Остальные vanilla IDs (`zombie`, `giant`, `skeleton`, `creeper`, `pig`, `sheep`, `chicken`, `squid`, `wolf`, `bat`, `boat`, `spider`, `slime`, `ghast`, `enderman`, `cave_spider`, `silverfish`, `blaze`, `witch`, `villager`, `experience_orb`) совпадают по смыслу с современными registry ID.

## Legacy Thaumcraft entity → текущий мод

Есть активные сканы для: `primal_orb`, `firebat`, трёх вариантов pech (`pech/forager`, `pech/mage`, `pech/stalker`), `thaumic_slime`, `angry_zombie`, `furious_zombie`, всех taint-сущностей, `mind_spider`, `eldritch_guardian`, `eldritch_orb`, `crimson_knight`, `crimson_cleric`, `wisp`, а также шести материалов classic golem и straw golem.

В текущем `LegacyMobKind` присутствуют дополнительные типы, которых не было отдельной legacy-записью или для которых ещё нет entity-скана:

- `crimson_inquisitor` — современный отдельный registry ID; в TC4 ближайший источник аспектов — cultist knight/cleric, но это не тот же entity key.
- `converted_villager` — современный отдельный registry ID; нужен отдельный явный ключ (аспекты можно согласовать с villager/tainted villager после проверки поведения).
- `pech` — базовый enum-тип, но фактические сущности создаются вариантами `pech/forager`, `pech/mage`, `pech/stalker`; отдельный базовый ключ не нужен, если renderer/entity factory не создаёт `pech` без варианта.

## Что добавить

После актуализации legacy-вариантов добавлены два mob-скана:

- `thaumcraftmodern:crimson_inquisitor` — отдельный ключ; за основу взят cultist knight (`Alienis 1, Humanus 2, Perditio 1`) с `Telum 2` за боевую специализацию.
- `thaumcraftmodern:converted_villager` — отдельный ключ, наследующий vanilla villager (`Humanus 3, Aer 2`), пока не найден отдельный source-backed TC4 tag.

Технические entity-типы без mob-скана:

| Количество | Современный ID | Причина |
|---:|---|---|
| 1 | `thaumcraftmodern:alumentum` | отдельная runtime entity, legacy entity-скана нет |
| 1 | `thaumcraftmodern:bottled_taint` | projectile entity, legacy entity-скана нет |
| 1 | `thaumcraftmodern:pech_blast` | projectile entity, legacy entity-скана нет |
| 1 | `thaumcraftmodern:faceless_witness` | современная entity, legacy аналога нет |
| 1 | `thaumcraftmodern:frost_shard` | projectile entity, legacy entity-скана нет |
| 1 | `thaumcraftmodern:focus_ember` | projectile entity, legacy entity-скана нет |
| 1 | `thaumcraftmodern:golem_fishing_bobber` | runtime bobber entity, legacy entity-скана нет |

Итого в этой актуализации добавлено **2 mob-скана**. Отдельно остаются **7 технических entity**; их не следует считать «мобами» и включать в обязательный список изучаемых существ, пока не будет принято решение сканировать projectile/bobber-объекты.

## Как переносить аспекты

Для legacy-совпадений сохраняем исходные аспекты и registry-переименование. Для новых типов сначала регистрируем явные аспекты в одном месте; recipe-derived генератор не должен сам придумывать entity-аспекты. Для projectile/bobber сущностей разумно использовать отдельный ключ только если игрок действительно может навести Thaumometer на entity; иначе их можно оставить без scan как технические сущности.
