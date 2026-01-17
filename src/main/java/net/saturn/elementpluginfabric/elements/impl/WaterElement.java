package net.saturn.elementpluginfabric.elements.impl;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.elements.Element;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.elements.impl.basic.water.Ability1;
import net.saturn.elementpluginfabric.elements.impl.basic.water.Ability2;
import net.saturn.elementpluginfabric.elements.impl.basic.water.WaterBeamData;
import net.saturn.elementpluginfabric.elements.impl.basic.water.WaterBeamListener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WaterElement implements Element {
    private final ElementPluginFabric plugin;
    private final Map<UUID, WaterBeamData> activeBeams = new ConcurrentHashMap<>();
    private final Ability1 ability1;
    private final Ability2 ability2;
    private final WaterBeamListener listener;

    public WaterElement(ElementPluginFabric plugin) {
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
    public void applyUpsides(ServerPlayer player, int upgradeLevel) {
        // Passive 1: Water Breathing + Conduit Power (permanent)
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, Integer.MAX_VALUE, 0, true, false));
        player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, Integer.MAX_VALUE, 0, true, false));

        // Passive 2 (Upgrade II): Dolphins Grace V
        if (upgradeLevel >= 2) {
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, Integer.MAX_VALUE, 4, true, false));
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
    public void clearEffects(ServerPlayer player) {
        player.removeEffect(MobEffects.WATER_BREATHING);
        player.removeEffect(MobEffects.CONDUIT_POWER);
        player.removeEffect(MobEffects.DOLPHINS_GRACE);
        activeBeams.remove(player.getUUID());
    }
}