package net.saturn.elementpluginfabric.elements.impl.basic.fire;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.managers.ManaManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Meteor Shower - Rain down 18 fireballs around your position over 6 seconds
 */
public class Ability2 {
    private final ElementPluginFabric plugin;
    private final Map<UUID, MeteorShowerData> activeMeteors;

    public Ability2(ElementPluginFabric plugin, Map<UUID, MeteorShowerData> activeMeteors) {
        this.plugin = plugin;
        this.activeMeteors = activeMeteors;
    }

    public boolean execute(ElementContext context) {
        ServerPlayer player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility2Cost(ElementType.FIRE);

        if (!manaManager.hasMana(player, cost)) {
            player.sendSystemMessage(Component.literal("Not enough mana! Need " + cost)
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Start meteor shower
        activeMeteors.put(player.getUUID(), new MeteorShowerData(player));

        player.sendSystemMessage(Component.literal("Meteor Shower activated!")
                .withStyle(ChatFormatting.GOLD));

        return true;
    }

    public Map<UUID, MeteorShowerData> getActiveMeteors() {
        return activeMeteors;
    }
}

