package net.saturn.elementmod.elements.impl.basic.fire;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.managers.ManaManager;

import java.util.Map;
import java.util.UUID;

/**
 * Meteor Shower - Rain down 18 fireballs around your position over 6 seconds
 */
public class Ability2 {
    private final ElementMod plugin;
    private final Map<UUID, MeteorShowerData> activeMeteors;

    public Ability2(ElementMod plugin, Map<UUID, MeteorShowerData> activeMeteors) {
        this.plugin = plugin;
        this.activeMeteors = activeMeteors;
    }

    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility2Cost(ElementType.FIRE);

        if (activeMeteors.containsKey(player.getUuid())) {
            player.sendMessage(Text.literal("Meteor Shower is already active!")
                    .formatted(Formatting.RED));
            return false;
        }

        if (!manaManager.hasMana(player, cost)) {
            player.sendMessage(Text.literal("Not enough mana! Need " + cost)
                    .formatted(Formatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Start meteor shower
        activeMeteors.put(player.getUuid(), new MeteorShowerData(player));

        player.sendMessage(Text.literal("Meteor Shower activated!")
                .formatted(Formatting.GOLD));

        return true;
    }

    public Map<UUID, MeteorShowerData> getActiveMeteors() {
        return activeMeteors;
    }
}

