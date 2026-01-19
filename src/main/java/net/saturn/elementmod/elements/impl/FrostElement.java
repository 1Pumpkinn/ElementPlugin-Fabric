package net.saturn.elementmod.elements.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.config.MetadataKeys;
import net.saturn.elementmod.data.TemporaryPlayerData;
import net.saturn.elementmod.elements.Element;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.elements.impl.basic.frost.Ability1;
import net.saturn.elementmod.elements.impl.basic.frost.Ability2;

public class FrostElement implements Element {
    private final ElementMod plugin;
    private final Ability1 ability1;
    private final Ability2 ability2;

    public FrostElement(ElementMod plugin) {
        this.plugin = plugin;
        this.ability1 = new Ability1(plugin);
        this.ability2 = new Ability2(plugin);
    }

    @Override
    public ElementType getType() {
        return ElementType.FROST;
    }

    @Override
    public void applyUpsides(ServerPlayerEntity player, int upgradeLevel) {
        // Passive 1: Speed II when wearing leather boots - handled by event listener
        // Passive 2 (Upgrade II): Speed III on ice - handled by event listener
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
        TemporaryPlayerData.remove(player.getUuid(), MetadataKeys.Frost.FROZEN_PUNCH_READY);
    }
}