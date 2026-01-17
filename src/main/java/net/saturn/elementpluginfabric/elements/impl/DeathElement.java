package net.saturn.elementpluginfabric.elements.impl;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.elements.Element;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.elements.impl.basic.death.Ability1;
import net.saturn.elementpluginfabric.elements.impl.basic.death.Ability2;

public class DeathElement implements Element {
    private final ElementPluginFabric plugin;
    private final Ability1 ability1;
    private final Ability2 ability2;

    public DeathElement(ElementPluginFabric plugin) {
        this.plugin = plugin;
        this.ability1 = new Ability1(plugin);
        this.ability2 = new Ability2(plugin);
    }

    @Override
    public ElementType getType() {
        return ElementType.DEATH;
    }

    @Override
    public void applyUpsides(ServerPlayer player, int upgradeLevel) {
        // Passive 1: Night Vision
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false));

        // Raw/undead foods give Golden Apple effects - handled by event listener
        // Passive 2 (Upgrade II): Nearby enemies get Hunger - handled by event listener
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
        player.removeEffect(MobEffects.NIGHT_VISION);
    }
}