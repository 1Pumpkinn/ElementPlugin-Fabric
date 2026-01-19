package net.saturn.elementmod.elements.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.config.MetadataKeys;
import net.saturn.elementmod.data.TemporaryPlayerData;
import net.saturn.elementmod.elements.Element;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.elements.impl.basic.earth.Ability1;
import net.saturn.elementmod.elements.impl.basic.earth.Ability2;

public class EarthElement implements Element {
    private final ElementMod plugin;
    private final Ability1 ability1;
    private final Ability2 ability2;

    public EarthElement(ElementMod plugin) {
        this.plugin = plugin;
        this.ability1 = new Ability1(plugin);
        this.ability2 = new Ability2(plugin);
    }

    @Override
    public ElementType getType() {
        return ElementType.EARTH;
    }

    @Override
    public void applyUpsides(ServerPlayerEntity player, int upgradeLevel) {
        // Passive 1: Hero of the Village (permanent)
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, Integer.MAX_VALUE, 0, true, false));

        // Passive 2 (Upgrade II): Double ore drops - handled by event listener
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
        player.removeStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE);
        // Clear any metadata
        TemporaryPlayerData.remove(player.getUuid(), MetadataKeys.Earth.MINE_UNTIL);
        TemporaryPlayerData.remove(player.getUuid(), MetadataKeys.Earth.CHARM_NEXT_UNTIL);
    }
}