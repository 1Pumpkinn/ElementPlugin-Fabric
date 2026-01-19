package net.saturn.elementmod.elements.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.elements.Element;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.elements.impl.basic.air.Ability1;
import net.saturn.elementmod.elements.impl.basic.air.Ability2;

public class AirElement implements Element {
    private final ElementMod plugin;
    private final Ability1 ability1;
    private final Ability2 ability2;

    public AirElement(ElementMod plugin) {
        this.plugin = plugin;
        this.ability1 = new Ability1(plugin);
        this.ability2 = new Ability2(plugin);
    }

    @Override
    public ElementType getType() {
        return ElementType.AIR;
    }

    @Override
    public void applyUpsides(ServerPlayerEntity player, int upgradeLevel) {
        // Passive 1: No fall damage - handled by event listener
        // Passive 2 (Upgrade II): Super jump - also handled by event listener
        // Nothing to apply here as these are reactive effects
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
        // Air element doesn't apply permanent effects that need clearing
    }
}