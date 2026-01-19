package net.saturn.elementmod.services;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.effect.StatusEffect;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.config.Constants;
import net.saturn.elementmod.data.PlayerData;
import net.saturn.elementmod.elements.Element;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.managers.ElementManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.EnumMap;
import java.util.Map;

/**
 * Centralized service for managing element passive effects.
 * Single source of truth for all effect-related operations.
 */
public class EffectService {
    @SuppressWarnings("unused")
    private final ElementMod plugin;
    private final ElementManager elementManager;
    private boolean monitoring = false;

    // Cache of required effects per element
    private final Map<ElementType, EffectRequirement[]> requiredEffects = new EnumMap<>(ElementType.class);

    public EffectService(ElementMod plugin, ElementManager elementManager) {
        this.plugin = plugin;
        this.elementManager = elementManager;
        initializeRequirements();
        startMonitoring();
    }

    private void initializeRequirements() {
        // Water
        requiredEffects.put(ElementType.WATER, new EffectRequirement[] {
                new EffectRequirement(StatusEffects.WATER_BREATHING, 0, false),
                new EffectRequirement(StatusEffects.CONDUIT_POWER, 0, false)
        });

        // Fire
        requiredEffects.put(ElementType.FIRE, new EffectRequirement[] {
                new EffectRequirement(StatusEffects.FIRE_RESISTANCE, 0, false)
        });

        // Earth
        requiredEffects.put(ElementType.EARTH, new EffectRequirement[] {
                new EffectRequirement(StatusEffects.HERO_OF_THE_VILLAGE, 0, false)
        });

        // Life
        requiredEffects.put(ElementType.LIFE, new EffectRequirement[] {
                new EffectRequirement(StatusEffects.REGENERATION, 0, false)
        });

        // Death
        requiredEffects.put(ElementType.DEATH, new EffectRequirement[] {
                new EffectRequirement(StatusEffects.NIGHT_VISION, 0, false)
        });

        // Metal
        requiredEffects.put(ElementType.METAL, new EffectRequirement[] {
                new EffectRequirement(StatusEffects.HASTE, 0, false)
        });
    }

    /**
     * Clear ALL element effects from a player.
     * Used when switching elements or logging out.
     */
    public void clearAllElementEffects(ServerPlayerEntity player) {
        PlayerData pd = elementManager.data(player.getUuid());
        ElementType currentElement = pd.getCurrentElement();

        // Clear effects from ALL elements
        for (ElementType type : ElementType.values()) {
            if (type == currentElement) continue;

            Element element = elementManager.get(type);
            if (element != null) {
                element.clearEffects(player);
            }
        }

        // Reset health if not Life element
        resetHealthIfNeeded(player, currentElement);
    }

    /**
     * Apply passive effects for player's current element.
     * Single source of truth for effect application.
     */
    public void applyPassiveEffects(ServerPlayerEntity player) {
        PlayerData pd = elementManager.data(player.getUuid());
        ElementType type = pd.getCurrentElement();

        if (type == null) return;

        Element element = elementManager.get(type);
        if (element != null) {
            element.applyUpsides(player, pd.getUpgradeLevel(type));
        }
    }

    /**
     * Validate and restore effects if needed.
     * Called periodically and after certain events.
     */
    public void validateEffects(ServerPlayerEntity player) {
        PlayerData pd = elementManager.data(player.getUuid());
        ElementType currentElement = pd.getCurrentElement();

        if (currentElement == null) return;

        int upgradeLevel = pd.getUpgradeLevel(currentElement);

        // Check required effects
        EffectRequirement[] requirements = requiredEffects.get(currentElement);
        if (requirements != null) {
            for (EffectRequirement req : requirements) {
                if (!req.upgradeRequired || upgradeLevel >= 2) {
                    if (!hasValidEffect(player, req.effect)) {
                        player.addStatusEffect(new StatusEffectInstance(
                                req.effect, Integer.MAX_VALUE, req.level, true, false
                        ));
                    }
                }
            }
        }

        // Special handling for Water upgrade 2
        if (currentElement == ElementType.WATER && upgradeLevel >= 2) {
            if (!hasValidEffect(player, StatusEffects.DOLPHINS_GRACE)) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.DOLPHINS_GRACE, Integer.MAX_VALUE, 4, true, false
                ));
            }
        }

        // Validate health
        resetHealthIfNeeded(player, currentElement);
    }

    private boolean hasValidEffect(ServerPlayerEntity player, RegistryEntry<StatusEffect> effect) {
        StatusEffectInstance instance = player.getStatusEffect(effect);
        return instance != null && instance.getDuration() > 100;
    }

    private void resetHealthIfNeeded(ServerPlayerEntity player, ElementType currentElement) {
        var attr = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attr == null) return;

        double targetHealth = currentElement == ElementType.LIFE ?
                Constants.Health.LIFE_MAX : Constants.Health.NORMAL_MAX;

        if (attr.getBaseValue() != targetHealth) {
            attr.setBaseValue(targetHealth);
            if (!player.isDead() && player.getHealth() > targetHealth) {
                player.setHealth((float) targetHealth);
            }
        }
    }

    /**
     * Start periodic monitoring of effects
     */
    private void startMonitoring() {
        if (monitoring) return;
        monitoring = true;

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!monitoring) return;
            // Run every 2 seconds (40 ticks)
            if (server.getTicks() % 40 == 0) {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    validateEffects(player);
                }
            }
        });
    }

    public void stopMonitoring() {
        monitoring = false;
    }

    /**
     * Helper class for effect requirements
     */
    private static class EffectRequirement {
        final RegistryEntry<StatusEffect> effect;
        final int level;
        final boolean upgradeRequired;

        EffectRequirement(RegistryEntry<StatusEffect> effect, int level, boolean upgradeRequired) {
            this.effect = effect;
            this.level = level;
            this.upgradeRequired = upgradeRequired;
        }
    }
}