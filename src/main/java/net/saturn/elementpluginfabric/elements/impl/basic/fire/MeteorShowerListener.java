package net.saturn.elementpluginfabric.elements.impl.basic.fire;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;

/**
 * Listener for meteor shower tick events
 */
public class MeteorShowerListener {
    private final Map<UUID, MeteorShowerData> activeMeteors;

    public MeteorShowerListener(Map<UUID, MeteorShowerData> activeMeteors) {
        this.activeMeteors = activeMeteors;
        startMeteorTicker();
    }

    private void startMeteorTicker() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            activeMeteors.values().removeIf(meteor -> {
                meteor.tick();
                if (meteor.isExpired()) {
                    meteor.getPlayer().sendSystemMessage(Component.literal("Meteor Shower ended!")
                            .withStyle(ChatFormatting.GRAY));
                    return true;
                }
                return false;
            });
        });
    }
}

