# Fabric Conversion Guide

This document explains the conversion from Paper/Spigot API to Fabric 1.21.10.

## Package Structure

All Fabric code is in the `hs.elementPlugin.fabric` package, keeping the original Paper code intact in `hs.elementPlugin`.

## Key API Conversions

### Core Classes
- `JavaPlugin` → `ModInitializer` (ElementPluginFabric)
- `Bukkit.getServer()` → `MinecraftServer` (passed as parameter)
- `Player` → `ServerPlayerEntity`
- `ItemStack` → `ItemStack` (Minecraft's, different API)
- `Material` → `Item` or `Block`
- `ChatColor` → `Formatting` or `Text` components
- `Location` → `BlockPos` + `ServerWorld`

### Configuration
- `FileConfiguration` / `YamlConfiguration` → `SnakeYAML` (Yaml class)
- `plugin.getDataFolder()` → `config/elementplugin/` directory
- Config files use SnakeYAML directly

### Events
- `@EventHandler` → Fabric Event API
  - `PlayerJoinEvent` → `ServerPlayerEvents.AFTER_LOAD`
  - `PlayerQuitEvent` → `ServerPlayerEvents.DISCONNECT`
  - `PlayerInteractEvent` → `PlayerBlockBreakEvents` or `UseItemCallback`
  - `EntityDamageByEntityEvent` → `LivingEntityDamageEvents`
  - Custom events use `FabricEventFactory`

### Commands
- `CommandExecutor` → `CommandRegistrationCallback`
- `TabCompleter` → `SuggestionProvider`
- Command registration in `onInitialize()` or via callback

### Scheduling
- `BukkitRunnable` / `BukkitScheduler` → `ServerTickEvents`
- `runTaskTimer()` → `ServerTickEvents.END_SERVER_TICK` with tick counter
- `runTaskLater()` → `ServerTickEvents.END_SERVER_TICK` with delay tracking

### Items & Inventories
- `ItemMeta` → `ItemStack.getNbt()` or custom data components
- `PersistentDataContainer` → NBT tags or custom data components
- Item registration via `ItemGroup` or custom registration

### Effects & Attributes
- `PotionEffect` → `StatusEffectInstance`
- `PotionEffectType` → `StatusEffect`
- `Attribute` → `EntityAttributes`
- `player.addPotionEffect()` → `player.addStatusEffect()`

### Particles & Sounds
- `World.spawnParticle()` → `ServerWorld.spawnParticles()`
- `World.playSound()` → `ServerWorld.playSound()`
- Particle types and sound events use Minecraft's registry

### World & Blocks
- `World` → `ServerWorld`
- `Block` → `BlockState`
- `getBlockAt()` → `world.getBlockState(pos)`

## Conversion Status

### ✅ Completed
- Core mod structure (fabric.mod.json, build.gradle)
- Config classes (Constants, AbilityCosts, MetadataKeys)
- Data classes (PlayerData, DataStore)
- ConfigManager
- TrustManager
- ManaManager
- EffectService
- ValidationService
- Element interfaces (Element, ElementContext, ElementType)
- Main mod class (ElementPluginFabric)

### 🚧 In Progress / Needs Work
- ElementManager (needs full conversion)
- ItemManager (needs ElementItem interface)
- All element implementations (Air, Water, Fire, etc.)
- All ability implementations
- Commands (ElementCommand, ManaCommand, etc.)
- Listeners (all event listeners)
- Items (ElementCoreItem, UpgraderItem, etc.)
- GUI (ElementSelectionGUI)
- Recipes (UtilRecipes)
- Utilities (TaskScheduler, MetadataHelper, etc.)

## Remaining Work

### High Priority
1. **ElementManager** - Convert Bukkit Player to ServerPlayerEntity
2. **All Element Implementations** - Convert 8 element classes
3. **Commands** - Convert 6 command classes to Fabric command API
4. **Listeners** - Convert 15+ listener classes to Fabric events

### Medium Priority
5. **Abilities** - Convert 16 ability classes
6. **Items** - Convert item system to Fabric items
7. **Recipes** - Convert to Fabric recipe system
8. **GUI** - Convert to Fabric screen API or use server-side GUI

### Low Priority
9. **Utilities** - Convert utility classes
10. **Visual Effects** - Convert particle and sound utilities

## Building

```bash
./gradlew build
```

The mod JAR will be in `build/libs/`.

## Testing

1. Set up a Fabric 1.21.10 development environment
2. Place the mod JAR in the mods folder
3. Test each feature systematically

## Notes

- The original Paper code remains untouched in `hs.elementPlugin`
- All Fabric code is in `hs.elementPlugin.fabric`
- Some features may need reimplementation due to API differences
- GUI system may need significant changes (Fabric doesn't have built-in GUI like Paper)

