# World Content Manual Test — 1.5.0

## Изолированный dedicated server

1. Создать пустую директорию для запуска.
2. Выполнить
   `./gradlew runServer -PrunDirectory=/absolute/path/to/test-run`.
3. Прочитать EULA и самостоятельно установить `eula=true`.
4. Оставить стандартный `level-type=minecraft:normal` и подтвердить, что
   Overworld использует `thaumic_reborn:legacy_overworld`. Отдельным
   прогоном проверить совместимый явный preset:
   `level-type=thaumic_reborn:thaumcraft_modern`.
5. Для каждого прогона менять `level-seed` и использовать новую
   `level-name`.
6. Дождаться `Done`, выполнить `save-all`, затем `stop`.
7. Проверить отсутствие registry/datapack/worldgen errors в `logs/latest.log`.

Минимум три seed:

- положительный decimal seed;
- отрицательный decimal seed;
- текстовый seed.

## Проверка контента

- в совершенно новом мире выполнить:
  `/locate biome thaumic_reborn:magical_forest`,
  `/locate biome thaumic_reborn:tainted_lands` и
  `/locate biome thaumic_reborn:eerie`;
- найти Cinnabar глубоко, Amber около поверхности и все шесть Infused Stone;
- добыть руды без чар, с Fortune и с Silk Touch;
- вырастить оба саженца bonemeal и natural random ticks;
- найти дикие Greatwood/Silverwood, Shimmerleaf, Cinderpearl, Vishroom,
  Mana Pod и Taint Fibres;
- открыть Mound, Eldritch Ring, Hilltop Stones, Aura Totem, Wizard tower и
  Banker house;
- проверить профессии, trade restock, сундуки и Treasure Bags;
- проверить естественный spawn в Magical Forest, Tainted Lands, Eerie и
  Nether;
- убить Guardian, Cultists, Praetor, Warden/Construct и Giant Taintacle;
- просканировать каждый новый блок, предмет, NPC и существо;
- прочитать Crimson Rites и убедиться, что `CRIMSON` появляется и завершён;
- подобрать Primordial Pearl и проверить запись item criterion.

## Раздельные статусы

Автоматическая сборка не подтверждает client rendering, дальние LOD, реальные
spawn rates, корректность нескольких seed, dedicated networking и
двухклиентную синхронизацию. Эти пункты отмечаются пройденными только после
ручного выполнения соответствующего сценария.
