package net.saturn.elementmod.elements.impl.basic.fire;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
                    meteor.getPlayer().sendMessage(Text.literal("Meteor Shower ended!")
                            .formatted(Formatting.GRAY));
                    return true;
                }
                return false;
            });
        });
    }
}

