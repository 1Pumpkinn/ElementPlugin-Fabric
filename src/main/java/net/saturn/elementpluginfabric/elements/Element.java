package net.saturn.elementpluginfabric.elements;

import net.minecraft.server.level.ServerPlayer;

public interface Element {
    ElementType getType();

    void applyUpsides(ServerPlayer player, int upgradeLevel);

    boolean ability1(ElementContext context);

    boolean ability2(ElementContext context);

    void clearEffects(ServerPlayer player);
}