package net.saturn.elementmod.elements.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.elements.Element;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.elements.impl.basic.metal.Ability1;
import net.saturn.elementmod.elements.impl.basic.metal.Ability2;

public class MetalElement implements Element {
    private final ElementMod plugin;
    private final Ability1 ability1;
    private final Ability2 ability2;

    public MetalElement(ElementMod plugin) {
        this.plugin = plugin;
        this.ability1 = new Ability1(plugin);
        this.ability2 = new Ability2(plugin);
    }

    @Override
    public ElementType getType() {
        return ElementType.METAL;
    }

    @Override
    public void applyUpsides(ServerPlayerEntity player, int upgradeLevel) {
        // Passive 1: Haste I
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, Integer.MAX_VALUE, 0, true, false));

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
    public void clearEffects(ServerPlayerEntity player) {
        player.removeStatusEffect(StatusEffects.HASTE);
    }
}
