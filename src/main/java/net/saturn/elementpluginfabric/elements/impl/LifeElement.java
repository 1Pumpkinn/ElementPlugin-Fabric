package net.saturn.elementpluginfabric.elements.impl;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.config.Constants;
import net.saturn.elementpluginfabric.elements.Element;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.elements.impl.basic.life.Ability1;
import net.saturn.elementpluginfabric.elements.impl.basic.life.Ability2;
import net.saturn.elementpluginfabric.elements.impl.basic.life.HealingBeamData;
import net.saturn.elementpluginfabric.elements.impl.basic.life.HealingBeamListener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LifeElement implements Element {
    private final ElementPluginFabric plugin;
    private final Map<UUID, HealingBeamData> activeBeams = new ConcurrentHashMap<>();
    private final Ability1 ability1;
    private final Ability2 ability2;
    private final HealingBeamListener listener;

    public LifeElement(ElementPluginFabric plugin) {
        this.plugin = plugin;
        this.ability1 = new Ability1(plugin);
        this.ability2 = new Ability2(plugin, activeBeams);
        this.listener = new HealingBeamListener(activeBeams);
    }

    @Override
    public ElementType getType() {
        return ElementType.LIFE;
    }

    @Override
    public void applyUpsides(ServerPlayer player, int upgradeLevel) {
        // Passive 1: 15 hearts (30 HP) + Regeneration I
        var attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(Constants.Health.LIFE_MAX);
        }
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Integer.MAX_VALUE, 0, true, false));

        // 50% faster natural regeneration - handled by event listener

        // Passive 2 (Upgrade II): Crops grow instantly - handled by event listener
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
        // Reset max health
        var attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(Constants.Health.NORMAL_MAX);
            if (player.getHealth() > Constants.Health.NORMAL_MAX) {
                player.setHealth((float) Constants.Health.NORMAL_MAX);
            }
        }
        player.removeEffect(MobEffects.REGENERATION);
        activeBeams.remove(player.getUUID());
    }
}