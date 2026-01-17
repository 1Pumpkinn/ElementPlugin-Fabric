package net.saturn.elementpluginfabric.elements;

import net.minecraft.server.network.ServerPlayerEntity;

public interface Element {
    ElementType getType();

    void applyUpsides(ServerPlayerEntity player, int upgradeLevel);

    boolean ability1(ElementContext context);

    boolean ability2(ElementContext context);

    void clearEffects(ServerPlayerEntity player);
}

