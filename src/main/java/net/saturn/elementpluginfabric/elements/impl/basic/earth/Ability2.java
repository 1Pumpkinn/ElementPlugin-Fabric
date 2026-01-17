package net.saturn.elementpluginfabric.elements.impl.basic.earth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.config.MetadataKeys;
import net.saturn.elementpluginfabric.data.TemporaryPlayerData;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.managers.ManaManager;

/**
 * Mob Charm - Punch a mob to make it follow you for 30 seconds
 */
public class Ability2 {
    private final ElementPluginFabric plugin;

    public Ability2(ElementPluginFabric plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayer player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility2Cost(ElementType.EARTH);

        if (!manaManager.hasMana(player, cost)) {
            player.sendSystemMessage(Component.literal("Not enough mana! Need " + cost)
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Set charm mode - next mob punched will be charmed
        TemporaryPlayerData.putLong(
                player.getUUID(),
                MetadataKeys.Earth.CHARM_NEXT_UNTIL,
                System.currentTimeMillis() + 10000 // 10 seconds to punch a mob
        );

        player.sendSystemMessage(Component.literal("Mob Charm ready! Punch a mob to charm it!")
                .withStyle(ChatFormatting.GREEN));

        return true;
    }
}

