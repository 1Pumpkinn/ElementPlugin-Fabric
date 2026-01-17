package net.saturn.elementpluginfabric.elements.impl.basic.death;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.managers.ManaManager;

/**
 * Wither Skull - Fire an explosive wither skull
 */
public class Ability2 {
    private final ElementPluginFabric plugin;

    public Ability2(ElementPluginFabric plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayer player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility2Cost(ElementType.DEATH);

        if (!manaManager.hasMana(player, cost)) {
            player.sendSystemMessage(Component.literal("Not enough mana! Need " + cost)
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        Vec3 lookVec = player.getLookAngle();
        Vec3 spawnPos = player.getEyePosition().add(lookVec.scale(1.5));

        WitherSkull skull = new WitherSkull(player.level(), player, lookVec);
        skull.setPos(spawnPos);
        skull.setDangerous(true); // Makes it explosive

        player.level().addFreshEntity(skull);

        player.sendSystemMessage(Component.literal("Wither Skull launched!")
                .withStyle(ChatFormatting.DARK_PURPLE));

        return true;
    }
}

