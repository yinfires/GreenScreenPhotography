# Green Screen Workspace Notes

## Framework

- This workspace targets the same baseline as Moon Spire: Minecraft 1.21.1, NeoForge 21.1.228, ModDevGradle 2.0.141, Java 21, and Parchment 2024.11.17.
- Keep `gradle.properties`, `settings.gradle`, `build.gradle`, `src/main/templates/META-INF/neoforge.mods.toml`, and the `@Mod` id in sync when changing project identity.
- The current mod id is `greenscreen`; the base package is `com.yinfires.greenscreen`.

## Gameplay And Language

- Every player-facing string must use a translation key.
- Do not use `Component.literal(...)` or direct English/Chinese text for names, descriptions, HUD labels, messages, buttons, phases, item text, or screen text.
- Store display text in language files and render it with `Component.translatable(...)`.
- If a code change adds, removes, or renames a translation key, update all affected language files in the same change and remove dead keys.
- Internal ids, registry names, NBT keys, payload ids, resource paths, and code comments are not player-facing language and should remain stable implementation strings.

## UI Rules

- Before UI changes, inspect the existing screen/widget/resource patterns and keep new code consistent with them.
- Green Screen UI should default to a black/gray palette with square, minimal shapes unless a specific feature needs another treatment.
- Moon Spire-specific battle, card, and gameplay UI rules should not be copied here unless Green Screen intentionally gains those systems.
- Every custom screen should avoid vanilla blur. Prefer a shared no-blur base screen that preserves the normal translucent dim background without calling the vanilla menu blur path.
- In custom `render(...)` methods, draw order should be: background first, custom UI content second, widgets last.
- Do not call `super.render(...)` at the end of a custom screen when custom-rendered content controls the layer order.
- Do not add blur effects, blurred backgrounds, backdrop filters, blur shaders, or screen-wide blur overlays.
- Prefer crisp HUD/screen panels that keep the world view readable behind the interface.
- Button text should default to horizontal and vertical centering. Only use icon-led or left-aligned text when the control is deliberately designed that way.
- UI text should not fake bold weight by drawing the same text repeatedly with small offsets. Use font/style support for bold text; draw outlines only when the design explicitly calls for an outline.
- Any UI with a scrollbar must show and allow scrollbar scrolling only when content exceeds the visible area.
- Scrollable UI should use smooth pixel scrolling, support direct thumb dragging, and must not skip or permanently hide rows/items.
- If content fits completely, wheel overscroll may move briefly but must naturally settle back; apply the same rebound behavior at top and bottom edges.
- For smooth animations, advance animation state from render-frame real time using `System.nanoTime()` deltas clamped to a sensible range, rather than relying on 20 TPS `tick()` progression.
- Prefer frame-rate-independent easing such as `1 - pow(1 - perTickAmount, deltaTicks)` plus a small snap threshold.
- When adapting extendable PNG buttons or panels, inspect source dimensions and non-transparent bounds first. Preserve fixed end caps and stretch only the repeatable flat middle band.
- Do not scale a whole extendable texture to fit if that warps angled ends or borders; derive cap sizes from the source slice ratio and destination height.
- Modal UI must render above all base widgets and swallow base input. Render base content first, draw a normal translucent blocking layer, then draw the modal panel last.
- When a modal closes by confirm, cancel, right click, ESC, or outside click, clear modal state and rebuild/refresh underlying hover, drag, focus, and scrollbar state so stale highlights do not remain.

## Working Rhythm

- Avoid long silent code-reading stretches. After the first quick scan, report what files look relevant and what will change.
- Prefer a small complete patch once enough context is known, then compile or run the narrowest useful verification.
- Do not wait until every detail is understood before making low-risk layout, documentation, or narrowly scoped changes.
