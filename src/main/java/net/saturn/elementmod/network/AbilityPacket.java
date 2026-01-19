package net.saturn.elementmod.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.saturn.elementmod.ElementMod;

public record AbilityPacket(int abilityNumber) implements CustomPayload {
    public static final CustomPayload.Id<AbilityPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("elementplugin", "ability"));
    
    public static final PacketCodec<RegistryByteBuf, AbilityPacket> CODEC = PacketCodecs.VAR_INT.xmap(
            AbilityPacket::new,
            AbilityPacket::abilityNumber
    ).cast();
    
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
    
    public static void register() {
        // Register server-side packet type and handler
        PayloadTypeRegistry.playS2C().register(AbilityPacket.PACKET_ID, AbilityPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(AbilityPacket.PACKET_ID, AbilityPacket.CODEC);
        
        // Register server-side receiver
        ServerPlayNetworking.registerGlobalReceiver(AbilityPacket.PACKET_ID, (packet, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                ElementMod plugin = ElementMod.getInstance();
                if (plugin != null && plugin.getElementManager() != null) {
                    if (packet.abilityNumber() == 1) {
                        plugin.getElementManager().useAbility1(player);
                    } else if (packet.abilityNumber() == 2) {
                        plugin.getElementManager().useAbility2(player);
                    }
                }
            });
        });
    }
}

