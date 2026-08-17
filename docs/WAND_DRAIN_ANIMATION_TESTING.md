# Wand drain animation comparison

The active node-draining animation is selected in:

`src/main/resources/assets/thaumic_reborn/config/wand_casting_render.json`

Use the top-level field:

```json
"drain_animation_mode": "modern"
```

Available values:

- `modern` — the video-tuned animation: vertical hold, forward grip-pivot
  tilt, wider slow clockwise orbit, and the startup motion played backwards
  on release.
- `classic` — the original TC4 4.2.3.5 renderer motion: three-tick startup
  to `-60°`, first-person X/Z context rotations, and the two unscaled legacy
  sine waves. It intentionally keeps the original immediate release.

For a development client, change the value, make sure the resource is copied
to the run directory (`./gradlew processResources` or a continuous resource
task), then press `F3+T`. The reload log reports the selected drain animation.

The same file also contains the per-form `casting_pivot` values used by the
modern forward tilt. They are independent for wand, sceptre, and staff.
