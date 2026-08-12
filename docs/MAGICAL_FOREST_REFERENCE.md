# Magical Forest reference profile

This file records the generation contract used by Thaumic Reborn so later
biome work can be checked against the same source instead of approximated from
memory.

## Sources

- Authoritative TC4 binary:
  `reference/original/Thaumcraft_1.7.10_4.2.3.5.jar`
- Readable 1.12.2 port of the same behavior:
  `reference/Thaumcraft-4.2-FOREVA-master/src/main/java/thaumcraft/common/lib/world/biomes/BiomeMagicalForest.java`
- Tree and plant generators:
  `WorldGenBigMagicTree`, `WorldGenGreatwoodTrees`,
  `WorldGenSilverwoodTrees`, `WorldGenManaPods`, and
  `WorldGenCustomFlowers`
- Global node reference:
  `ThaumcraftWorldGenerator.generateWildNodes`

## TC4 biome contract

- Temperature `0.6`, rainfall `0.7`, water color `0x0077EE`.
- Ten tree attempts per chunk.
- Per tree attempt:
  1. Silverwood with probability `1/14` (`7.1429%`, expected `0.7143`
     candidates per chunk).
  2. Otherwise Greatwood with probability `1/10`; its effective probability
     is `13/140` (`9.2857%`, expected `0.9286` candidates per chunk).
  3. Otherwise `WorldGenBigMagicTree`, a tall branching oak. Its effective
     probability is `117/140` (`83.5714%`, expected `8.3571` candidates per
     chunk in unmodified TC4).
- Silverwood world generation runs the Shimmerleaf flower generator around
  the tree (18 local placement attempts).
- Zero to two stone blobs per chunk.
- A four-by-four grid of giant-mushroom checks, each with probability `1/40`
  (expected `0.4` attempts per chunk).
- Ten Mana Pod generator attempts per chunk.
- Eight Vishroom attempts per chunk; the target must be beside wood.
- Pech and Wisp spawn entries both use weight `10`, group size `1-2`, gated by
  their server configuration switches.

## Intentional modern decisions

- The ordinary tree is still made from vanilla oak logs/leaves, but keeps the
  TC4 `WorldGenBigMagicTree` height and branching profile.
- Tree selection and decorator density now follow the original randomizer
  directly: ten independent tree attempts, sequential `1/14` Silverwood then
  `1/10` Greatwood checks, otherwise big magic oak. There is no artificial
  lattice, spacing radius, half-oak rejection, or special-tree priority.
  As in TC4, terrain and crown clearance can make a selected attempt fail.
- The original surface counts are owned by the legacy vegetation pass:
  `2` flower generators, `12` grass generators (`25%` fern), `6` ordinary
  mushroom generators, and `6` reed generators per chunk. Equivalent modern
  biome placed features are omitted so these counts are not doubled.
- The surface overlay accepts only non-cold underlying climates with base
  temperatures from `0.45` through `1.20`. This keeps Magical Forest patches
  beside calm temperate or warm terrain instead of frozen regions or extreme
  hot biomes.
- The requested taiga-style boulders use mossy cobblestone instead of the
  readable port's plain stone material. The original zero-to-two attempt
  count is preserved.
- Normal aura-node spacing remains the modern deterministic 8x8-region
  system. Magical Forest gets one additional candidate in one out of ten
  regions, producing approximately `+10%` candidate density without raising
  the attempt rate of every chunk.
- Biome decoration must not include `minecraft:trees_birch_and_oak`; otherwise
  birches and small vanilla oaks leak back into Magical Forest.

All distribution and visual checks must use newly generated chunks. Existing
chunks are not retroactively redecorated.
