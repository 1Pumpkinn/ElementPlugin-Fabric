package net.saturn.elementpluginfabric.services;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.config.Constants;
import net.saturn.elementpluginfabric.data.PlayerData;
import net.saturn.elementpluginfabric.elements.Element;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.managers.ElementManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumMap;
import java.util.Map;

/**
 * Centralized service for managing element passive effects.
 * Single source of truth for all effect-related operations.
 */
public class EffectService {
    @SuppressWarnings("unused")
    private final ElementPluginFabric plugin;
    private final ElementManager elementManager;
    private boolean monitoring = false;

    // Cache of required effects per element
    private final Map<ElementType, EffectRequirement[]> requiredEffects = new EnumMap<>(ElementType.class);

    public EffectService(ElementPluginFabric plugin, ElementManager elementManager) {
        this.plugin = plugin;
        this.elementManager = elementManager;
        initializeRequirements();
        startMonitoring();
    }

    private void initializeRequirements() {
        // Water - use Holder directly
        requiredEffects.put(ElementType.WATER, new EffectRequirement[] {
                new EffectRequirement(MobEffects.WATER_BREATHING, 0, false),
                new EffectRequirement(MobEffects.CONDUIT_POWER, 0, false)
        });

        // Fire
        requiredEffects.put(ElementType.FIRE, new EffectRequirement[] {
                new EffectRequirement(MobEffects.FIRE_RESISTANCE, 0, false)
        });

        // Earth
        requiredEffects.put(ElementType.EARTH, new EffectRequirement[] {
                new EffectRequirement(MobEffects.HERO_OF_THE_VILLAGE, 0, false)
        });

        // Life
        requiredEffects.put(ElementType.LIFE, new EffectRequirement[] {
                new EffectRequirement(MobEffects.REGENERATION, 0, false)
        });

        // Death
        requiredEffects.put(ElementType.DEATH, new EffectRequirement[] {
                new EffectRequirement(MobEffects.NIGHT_VISION, 0, false)
        });

        // Metal
        requiredEffects.put(ElementType.METAL, new EffectRequirement[] {
                new EffectRequirement(MobEffects.HASTE, 0, false)
        });
    }

    /**
     * Clear ALL element effects from a player.
     * Used when switching elements or logging out.
     */
    public void clearAllElementEffects(ServerPlayer player) {
        PlayerData pd = elementManager.data(player.getUUID());
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
    public void applyPassiveEffects(ServerPlayer player) {
        PlayerData pd = elementManager.data(player.getUUID());
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
    public void validateEffects(ServerPlayer player) {
        PlayerData pd = elementManager.data(player.getUUID());
        ElementType currentElement = pd.getCurrentElement();

        if (currentElement == null) return;

        int upgradeLevel = pd.getUpgradeLevel(currentElement);

        // Check required effects
        EffectRequirement[] requirements = requiredEffects.get(currentElement);
        if (requirements != null) {
            for (EffectRequirement req : requirements) {
                if (!req.upgradeRequired || upgradeLevel >= 2) {
                    if (!hasValidEffect(player, req.effect)) {
                        // Create MobEffectInstance with Holder<MobEffect>
                        player.addEffect(new MobEffectInstance(
                                req.effect, Integer.MAX_VALUE, req.level, true, false
                        ));
                    }
                }
            }
        }

        // Special handling for Water upgrade 2
        if (currentElement == ElementType.WATER && upgradeLevel >= 2) {
            if (!hasValidEffect(player, MobEffects.DOLPHINS_GRACE)) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.DOLPHINS_GRACE, Integer.MAX_VALUE, 4, true, false
                ));
            }
        }

        // Validate health
        resetHealthIfNeeded(player, currentElement);
    }

    private boolean hasValidEffect(ServerPlayer player, Holder<MobEffect> effect) {
        MobEffectInstance instance = player.getEffect(effect);
        return instance != null && instance.getDuration() > 100;
    }

    private void resetHealthIfNeeded(ServerPlayer player, ElementType currentElement) {
        var attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr == null) return;

        double targetHealth = currentElement == ElementType.LIFE ?
                Constants.Health.LIFE_MAX : Constants.Health.NORMAL_MAX;

        if (attr.getBaseValue() != targetHealth) {
            attr.setBaseValue(targetHealth);
            if (!player.isDeadOrDying() && player.getHealth() > targetHealth) {
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
            if (server.getTickCount() % 40 == 0) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
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
        final Holder<MobEffect> effect;
        final int level;
        final boolean upgradeRequired;

        EffectRequirement(Holder<MobEffect> effect, int level, boolean upgradeRequired) {
            this.effect = effect;
            this.level = level;
            this.upgradeRequired = upgradeRequired;
        }
    }
}