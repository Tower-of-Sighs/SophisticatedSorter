# Changelog

## [1.1.0-hotfix]

- Forge 1.20.1: fixed a production startup crash. The forge mixin config now declares its
  `refmap`, so mixin targets (e.g. the `quickcraftSlots` accessor) resolve correctly on the
  SRG-named production runtime instead of failing with "No candidates were found".
- Per-screen client options now use a combined **screen class + title key** identifier:
  - Button visibility (disable toggle) and button offset records are stored per screen, so
    containers that share one screen class (chest, barrel, shulker box, trapped chest - all
    `ChestScreen`) no longer share button positions or the hide toggle with each other.
  - The button offset *render/drag* path now uses the same identifier (previously it used the
    screen class only).
  - Matching is backward compatible and loose: a stored entry matches when it equals the full
    `class@title` id, the bare title key (the old format) or the bare screen class, so existing
    config entries keep working.

## [1.1.0] - Container Settings

This release brings the new **Container Settings** system to all supported loaders and versions.
It was originally developed for NeoForge 1.21.1 and is now available on Forge 1.20.1, Fabric 1.20.1,
Fabric 1.21.1, NeoForge 1.21.1 and NeoForge 26.1, with the loader-neutral logic shared from the
common codebase.

### New: container settings
- Per-container settings screen for every usable container (chests, barrels, ...) and the player
  inventory, opened from a new settings button in the top-right button group (the group shifts left
  to fit it). On vanilla container screens the entry is also reachable directly.
- Settings are persisted **server-side** per container (world SavedData, keyed by dimension and
  position), so every container keeps its own memory / no-sort / item-display preferences
  independently of the client and of other containers.
- **Memory slots**: pick a remembered item for any slot; empty memorized slots show the remembered
  item as a translucent ghost in the regular container view, and the server refuses placements into
  the slot that do not match the memorized item.
- **Ignore sorting (no-sort)**: mark slots that sorting must never touch; those slots keep whatever
  they hold.
- **Slot highlights**: the regular container view draws the same color stripes and memory ghosts the
  settings screen uses, so you can see at a glance which slots are special.
- Sorting now respects these settings: no-sort slots stay in place, and memorized slots are emptied
  and refilled with their remembered items during sorting (the classic pre-26.1 rule; see the new
  client config `memorySlotSorting` on 26.1).

### Ports and quality
- The whole settings feature is ported to Forge 1.20.1, Fabric 1.20.1/1.21.1 and NeoForge 26.1,
  adapted to each loader's networking, events, item handlers and Sophisticated Core API.
- Forge 1.20.1: fixed the per-slot highlight stripes not rendering on vanilla container screens
  (wrong `fillGradient` argument order); entering or leaving the settings screen no longer resets
  the mouse cursor to the screen center (menu swaps are closed server-side only, mirroring
  NeoForge's `SophisticatedMenuProvider` behavior).
- NeoForge 26.1: the settings screen now shows the container's actual items (item snapshots travel
  with the menu-open data instead of relying on per-slot sync that 26.1 no longer performs for
  view-only slots); sorting no longer silently does nothing after marking ignore-sort slots; new
  client config `memorySlotSorting` (default `true`) restores the classic memorized-slot refill
  behavior, set it to `false` to use the 26.1 sorter's plain handling.
- Shared key/store/resolver logic moved into the common source set used by all 1.20.1/1.21.1
  targets; language entries synchronized across all targets.

### Notes
- Container contents shown in the settings screen are a snapshot taken when the screen opens.
- `memorySlotSorting` lives in the client config (`sophisticatedsorter-client.toml`) and only
  affects the NeoForge 26.1 target, where Sophisticated Core removed memory-slot handling from its
  own sorter.