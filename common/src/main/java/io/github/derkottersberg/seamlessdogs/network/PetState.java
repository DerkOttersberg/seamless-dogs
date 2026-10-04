package io.github.derkottersberg.seamlessdogs.network;

import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Zero remaining ticks cancels; UUIDs prevent entity-ID reuse after reconnect. */
public record PetState(UUID player, UUID dog, int remainingTicks) implements CustomPacketPayload {
    public static final Type<PetState> TYPE = new Type<>(SeamlessDogs.id("pet_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PetState> CODEC = StreamCodec.of(
        (buffer, packet) -> { buffer.writeUUID(packet.player); buffer.writeUUID(packet.dog); buffer.writeByte(packet.remainingTicks); },
        buffer -> new PetState(buffer.readUUID(), buffer.readUUID(), buffer.readUnsignedByte()));
    public PetState { remainingTicks = Math.clamp(remainingTicks, 0, SeamlessDogs.DURATION); }
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
