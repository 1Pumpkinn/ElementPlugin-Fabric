package net.saturn.elementmod.elements.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.elements.Element;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.elements.impl.basic.fire.Ability1;
import net.saturn.elementmod.elements.impl.basic.fire.Ability2;
import net.saturn.elementmod.elements.impl.basic.fire.MeteorShowerData;
import net.saturn.elementmod.elements.impl.basic.fire.MeteorShowerListener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FireElement implements Element {
    private final ElementMod plugin;
    private final Map<UUID, MeteorShowerData> activeMeteors = new ConcurrentHashMap<>();
    private final Ability1 ability1;
    private final Ability2 ability2;
    private final MeteorShowerListener listener;

    public FireElement(ElementMod plugin) {
        this.plugin = plugin;
        this.ability1 = new Ability1(plugin);
        this.ability2 = new Ability2(plugin, activeMeteors);
        this.listener = new MeteorShowerListener(activeMeteors);
    }

    @Override
    public ElementType getType() {
        return ElementType.FIRE;
    }

    @Override
    public void applyUpsides(ServerPlayerEntity player, int upgradeLevel) {
        // Passive 1: Fire Resistance (permanent)
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false));

        // Passive 2 (Upgrade II): Fire Aspect on attacks - handled by event listener
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
        player.removeStatusEffect(StatusEffects.FIRE_RESISTANCE);
        activeMeteors.remove(player.getUuid());
    }
}