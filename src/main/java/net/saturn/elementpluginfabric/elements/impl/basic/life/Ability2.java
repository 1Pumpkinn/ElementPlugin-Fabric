package net.saturn.elementpluginfabric.elements.impl.basic.life;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.managers.ManaManager;

import java.util.Map;
import java.util.UUID;

/**
 * Healing Beam - Project a continuous healing beam for 5 seconds (1 heart/sec)
 */
public class Ability2 {
    private final ElementPluginFabric plugin;
    private final Map<UUID, HealingBeamData> activeBeams;

    public Ability2(ElementPluginFabric plugin, Map<UUID, HealingBeamData> activeBeams) {
        this.plugin = plugin;
        this.activeBeams = activeBeams;
    }

    public boolean execute(ElementContext context) {
        ServerPlayer player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility2Cost(ElementType.LIFE);

        if (!manaManager.hasMana(player, cost)) {
            player.sendSystemMessage(Component.literal("Not enough mana! Need " + cost)
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Start healing beam
        activeBeams.put(player.getUUID(), new HealingBeamData(player, context));

        player.sendSystemMessage(Component.literal("Healing Beam activated!")
                .withStyle(ChatFormatting.GREEN));

        return true;
    }

    public Map<UUID, HealingBeamData> getActiveBeams() {
        return activeBeams;
    }
}

