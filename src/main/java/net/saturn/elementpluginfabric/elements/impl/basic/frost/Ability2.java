package net.saturn.elementpluginfabric.elements.impl.basic.frost;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.config.Constants;
import net.saturn.elementpluginfabric.config.MetadataKeys;
import net.saturn.elementpluginfabric.data.TemporaryPlayerData;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.managers.ManaManager;

/**
 * Frozen Punch - Next punch (within 10s) freezes enemy for 5 seconds
 */
public class Ability2 {
    private final ElementPluginFabric plugin;

    public Ability2(ElementPluginFabric plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayer player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility2Cost(ElementType.FROST);

        if (!manaManager.hasMana(player, cost)) {
            player.sendSystemMessage(Component.literal("Not enough mana! Need " + cost)
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        TemporaryPlayerData.putLong(
                player.getUUID(),
                MetadataKeys.Frost.FROZEN_PUNCH_READY,
                System.currentTimeMillis() + Constants.Duration.FROST_PUNCH_READY_MS
        );

        player.sendSystemMessage(Component.literal("Frozen Punch ready! Punch an enemy to freeze them!")
                .withStyle(ChatFormatting.AQUA));

        return true;
    }
}

