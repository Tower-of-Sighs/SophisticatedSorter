## Sophisticated Sorter

Allows the built-in sorting functionality from Sophisticated Core to work on the player inventory and more ordinary containers, so the sorting experience across commonly used screens feels more consistent.

This mod does not try to invent a brand-new sorting system. It simply extends the existing Sophisticated Core sorting entry points to more screens.

### Supported Targets

| Loader | Minecraft | Java |
| --- | --- | --- |
| Forge | 1.20.1 | 21 |
| Fabric | 1.20.1 | 21 |
| Fabric | 1.21.1 | 21 |
| NeoForge | 1.21.1 | 21 |
| NeoForge | 26.1 | 25 |

### Main Features

#### Sorting Button Extension

This mod adds the original Sophisticated Core sorting buttons to most ordinary container screens.

What is being extended here is the sorting entry point from Sophisticated Core, brought to more non-Sophisticated containers, rather than adding a completely separate sorting system.

Sorting still uses the original four sorting modes from Sophisticated Core:

- By Name
- By Mod
- By Count
- By Tags

The default mode is By Name.

One button performs the sorting, and the other cycles through the sorting modes. Players already used to the Sophisticated series should not need much readjustment.

#### Player Inventory Sorting

This mod can sort the player's own inventory.

When the current screen does not meet the conditions for sorting the container itself, the mod will sort the player inventory instead. Because of that, even when you are in screens such as the crafting table, furnace, or other functional menus, you can still quickly organize your own inventory.

When the current screen does meet the conditions, the mod will sort the current container itself rather than the player inventory.

This makes it possible to use a sorting style consistent with Sophisticated Core even in ordinary containers such as chests and barrels.

Even if a screen does not show the sorting buttons, sorting can still be triggered with the hotkey.

The default hotkey is `R`.

The hotkey and the buttons use the same target-selection logic: if the current screen is suitable for container sorting, the container is sorted; otherwise the player inventory is sorted.

### Container Filtering Rules

Whether the mod sorts the current container is controlled by two filters together:

Config option: `Filter1`, enabled by default.

When enabled, only screens with more than `46` total slots are treated as valid for sorting the container itself; otherwise the player inventory will be sorted instead.

That `46` can be understood as player inventory `36` + ordinary small container `10`.

Because of that, screens such as the crafting table and other small functional menus are not sorted as containers by default.

Config option: `Filter2`, enabled by default.

When enabled, if the current screen contains slots that are obviously unsuitable for participating in sorting, the mod will not treat that screen as a sortable container screen.

The most typical example is a special slot such as a crafting result slot.

This filter exists to reduce the chance of accidentally sorting special-purpose slots in functional menus.

For ordinary container screens, sorting buttons are only shown when the screen passes the current filters and is not in the blacklist.

If a screen is not suitable for sorting the container itself, the buttons usually will not appear, but the player inventory can still be sorted through the hotkey.

### Relationship with Sophisticated Containers

Containers from the Sophisticated series still use the original sorting entry points and sorting logic from Sophisticated Core.

This mod does not add another set of buttons to those screens, and it does not replace their original sorting implementation.

That is done to make sure mechanics such as stack upgrades, slot rules, and the container behavior from the Sophisticated series continue to work properly.

In other words, this mod extends the ability for more ordinary containers to conveniently call sorting, rather than taking over the original sorting behavior of Sophisticated containers.

### Blacklist

If you do not want this mod's added buttons to appear on a certain screen, you can add that screen to the blacklist.

Press `U` while that screen is open to add or remove it from the blacklist.

The blacklist config option is `specialList`.

In most cases, what gets stored there is the translation key of the screen title rather than the literal text shown on screen.

Common examples:

- `container.chest`
- `container.barrel`
- `container.enderchest`

If you are not sure what should be written for a certain screen, the simplest method is usually not to edit the config by hand, but to open that screen and press `U` once.

### Pinyin Sorting

Config option: `pinyin`

Enabled by default.

When enabled, the mod uses default pinyin-based sorting in a Chinese-language environment. When disabled, that extra handling is not used.

### Mod Positioning

This mod only extends Sophisticated Core sorting into more screens. It is not meant to become a separate large storage-management system on its own.

Because of that, it is more focused on:

- Filling in missing convenience
- Unifying operations
- Reducing unnecessary switching back and forth

Rather than adding too many complicated features unrelated to extending sorting access.

### Future Plans

- Custom container filtering
- Custom button positioning
- Sophisticated Core search and transfer features for containers
