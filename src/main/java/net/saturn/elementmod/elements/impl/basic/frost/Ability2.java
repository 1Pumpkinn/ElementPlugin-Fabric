package net.saturn.elementmod.elements.impl.basic.frost;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.config.Constants;
import net.saturn.elementmod.config.MetadataKeys;
import net.saturn.elementmod.data.TemporaryPlayerData;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.managers.ManaManager;

/**
 * Frozen Punch - Next punch (within 10s) freezes enemy for 5 seconds
 */
public class Ability2 {
    private final ElementMod plugin;

    public Ability2(ElementMod plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility2Cost(ElementType.FROST);

        if (!manaManager.hasMana(player, cost)) {
            player.sendMessage(Text.literal("Not enough mana! Need " + cost)
                    .formatted(Formatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        TemporaryPlayerData.putLong(
                player.getUuid(),
                MetadataKeys.Frost.FROZEN_PUNCH_READY,
                System.currentTimeMillis() + Constants.Duration.FROST_PUNCH_READY_MS
        );

        player.sendMessage(Text.literal("Frozen Punch ready! Your next punch will freeze an enemy.")
                .formatted(Formatting.AQUA));

        return true;
    }
}

