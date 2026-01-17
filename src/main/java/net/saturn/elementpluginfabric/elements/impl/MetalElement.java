package net.saturn.elementpluginfabric.elements.impl;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.elements.Element;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.elements.impl.basic.metal.Ability1;
import net.saturn.elementpluginfabric.elements.impl.basic.metal.Ability2;

public class MetalElement implements Element {
    private final ElementPluginFabric plugin;
    private final Ability1 ability1;
    private final Ability2 ability2;

    public MetalElement(ElementPluginFabric plugin) {
        this.plugin = plugin;
        this.ability1 = new Ability1(plugin);
        this.ability2 = new Ability2(plugin);
    }

    @Override
    public ElementType getType() {
        return ElementType.METAL;
    }

    @Override
    public void applyUpsides(ServerPlayer player, int upgradeLevel) {
        // Passive 1: Haste I
        player.addEffect(new MobEffectInstance(MobEffects.HASTE, Integer.MAX_VALUE, 0, true, false));

        // Passive 2 (Upgrade II): Arrow immunity - handled by event listener
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
        player.removeEffect(MobEffects.HASTE);
    }
}
