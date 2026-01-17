package net.saturn.elementpluginfabric.elements.impl.basic.water;

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
 * Water Beam - Fire a continuous damaging water beam (deals 5 hearts total over 10 seconds)
 */
public class Ability2 {
    private final ElementPluginFabric plugin;
    private final Map<UUID, WaterBeamData> activeBeams;

    public Ability2(ElementPluginFabric plugin, Map<UUID, WaterBeamData> activeBeams) {
        this.plugin = plugin;
        this.activeBeams = activeBeams;
    }

    public boolean execute(ElementContext context) {
        ServerPlayer player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility2Cost(ElementType.WATER);

        if (!manaManager.hasMana(player, cost)) {
            player.sendSystemMessage(Component.literal("Not enough mana! Need " + cost)
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Start water beam
        activeBeams.put(player.getUUID(), new WaterBeamData(player, plugin));

        player.sendSystemMessage(Component.literal("Water Beam activated!")
                .withStyle(ChatFormatting.BLUE));

        return true;
    }

    public Map<UUID, WaterBeamData> getActiveBeams() {
        return activeBeams;
    }
}

