package net.saturn.elementpluginfabric.elements.impl;

import net.minecraft.server.level.ServerPlayer;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.elements.Element;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.elements.impl.basic.air.Ability1;
import net.saturn.elementpluginfabric.elements.impl.basic.air.Ability2;

public class AirElement implements Element {
    private final ElementPluginFabric plugin;
    private final Ability1 ability1;
    private final Ability2 ability2;

    public AirElement(ElementPluginFabric plugin) {
        this.plugin = plugin;
        this.ability1 = new Ability1(plugin);
        this.ability2 = new Ability2(plugin);
    }

    @Override
    public ElementType getType() {
        return ElementType.AIR;
    }

    @Override
    public void applyUpsides(ServerPlayer player, int upgradeLevel) {
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
    public void clearEffects(ServerPlayer player) {
        // Air element doesn't apply permanent effects that need clearing
    }
}