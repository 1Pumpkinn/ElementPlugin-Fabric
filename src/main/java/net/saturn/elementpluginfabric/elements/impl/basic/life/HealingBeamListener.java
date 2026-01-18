package net.saturn.elementpluginfabric.elements.impl.basic.life;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Map;
import java.util.UUID;

/**
 * Listener for healing beam tick events
 */
public class HealingBeamListener {
    private final Map<UUID, HealingBeamData> activeBeams;

    public HealingBeamListener(Map<UUID, HealingBeamData> activeBeams) {
        this.activeBeams = activeBeams;
        startBeamTicker();
    }

    private void startBeamTicker() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            activeBeams.values().removeIf(beam -> {
                beam.tick();
                if (beam.isExpired()) {
                    beam.getPlayer().sendSystemMessage(Component.literal("Healing Beam ended!")
                            .withStyle(ChatFormatting.GRAY));
                    return true;
                }
                return false;
            });
        });
    }
}

