package net.saturn.elementpluginfabric.elements.impl;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.config.MetadataKeys;
import net.saturn.elementpluginfabric.data.TemporaryPlayerData;
import net.saturn.elementpluginfabric.elements.Element;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.elements.impl.basic.earth.Ability1;
import net.saturn.elementpluginfabric.elements.impl.basic.earth.Ability2;

public class EarthElement implements Element {
    private final ElementPluginFabric plugin;
    private final Ability1 ability1;
    private final Ability2 ability2;

    public EarthElement(ElementPluginFabric plugin) {
        this.plugin = plugin;
        this.ability1 = new Ability1(plugin);
        this.ability2 = new Ability2(plugin);
    }

    @Override
    public ElementType getType() {
        return ElementType.EARTH;
    }

    @Override
    public void applyUpsides(ServerPlayer player, int upgradeLevel) {
        // Passive 1: Hero of the Village (permanent)
        player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, Integer.MAX_VALUE, 0, true, false));

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
    public void clearEffects(ServerPlayer player) {
        player.removeEffect(MobEffects.HERO_OF_THE_VILLAGE);
        // Clear any metadata
        TemporaryPlayerData.remove(player.getUUID(), MetadataKeys.Earth.MINE_UNTIL);
        TemporaryPlayerData.remove(player.getUUID(), MetadataKeys.Earth.CHARM_NEXT_UNTIL);
    }
}