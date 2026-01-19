package net.saturn.elementmod.elements.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.elements.Element;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.elements.impl.basic.water.Ability1;
import net.saturn.elementmod.elements.impl.basic.water.Ability2;
import net.saturn.elementmod.elements.impl.basic.water.WaterBeamData;
import net.saturn.elementmod.elements.impl.basic.water.WaterBeamListener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WaterElement implements Element {
    private final ElementMod plugin;
    private final Map<UUID, WaterBeamData> activeBeams = new ConcurrentHashMap<>();
    private final Ability1 ability1;
    private final Ability2 ability2;
    private final WaterBeamListener listener;

    public WaterElement(ElementMod plugin) {
        this.plugin = plugin;
        this.ability1 = new Ability1(plugin);
        this.ability2 = new Ability2(plugin, activeBeams);
        this.listener = new WaterBeamListener(activeBeams);
    }

    @Override
    public ElementType getType() {
        return ElementType.WATER;
    }

    @Override
    public void applyUpsides(ServerPlayerEntity player, int upgradeLevel) {
        // Passive 1: Water Breathing + Conduit Power (permanent)
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, Integer.MAX_VALUE, 0, true, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.CONDUIT_POWER, Integer.MAX_VALUE, 0, true, false));

        // Passive 2 (Upgrade II): Dolphins Grace V
        if (upgradeLevel >= 2) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, Integer.MAX_VALUE, 4, true, false));
        }
    }

    @Override
    public boolean ability1(ElementContext context) {
        return ability1.execute(context);
    }

    @Override
    public boolean ability2(ElementContext context) {
        return ability2.execute(context);
    }

    @Override
    public void clearEffects(ServerPlayerEntity player) {
        player.removeStatusEffect(StatusEffects.WATER_BREATHING);
        player.removeStatusEffect(StatusEffects.CONDUIT_POWER);
        player.removeStatusEffect(StatusEffects.DOLPHINS_GRACE);
        activeBeams.remove(player.getUuid());
    }
}