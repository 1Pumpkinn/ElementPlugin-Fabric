package net.saturn.elementmod.elements.impl.basic.earth;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.config.MetadataKeys;
import net.saturn.elementmod.data.TemporaryPlayerData;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.managers.ManaManager;

/**
 * Mob Charm - Punch a mob to make it follow you for 30 seconds
 */
public class Ability2 {
    private final ElementMod plugin;

    public Ability2(ElementMod plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility2Cost(ElementType.EARTH);

        if (!manaManager.hasMana(player, cost)) {
            player.sendMessage(Text.literal("Not enough mana! Required: " + cost)
                    .formatted(Formatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Set charm mode - next mob punched will be charmed
        TemporaryPlayerData.putLong(
                player.getUuid(),
                MetadataKeys.Earth.CHARM_NEXT_UNTIL,
                System.currentTimeMillis() + 10000 // 10 seconds to punch a mob
        );

        player.sendMessage(Text.literal("Mob Charm ready! Punch a mob to charm it!")
                .formatted(Formatting.GREEN));

        return true;
    }
}

