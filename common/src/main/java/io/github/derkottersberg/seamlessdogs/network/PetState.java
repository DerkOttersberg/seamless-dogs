package io.github.derkottersberg.seamlessdogs.network;

import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import io.github.derkottersberg.seamlessdogs.network.PacketCodec;
import io.github.derkottersberg.seamlessdogs.internal.DogsPayload;

/** Zero remaining ticks cancels; UUIDs prevent entity-ID reuse after reconnect. */
public record PetState(UUID player, UUID dog, int remainingTicks) implements DogsPayload {
    public static final Type<PetState> TYPE = new Type<>(SeamlessDogs.id("pet_state"));
    public static final PacketCodec<PetState> CODEC = PacketCodec.of(
        (buffer, packet) -> { buffer.writeUUID(packet.player); buffer.writeUUID(packet.dog); buffer.writeByte(packet.remainingTicks); },
        buffer -> new PetState(buffer.readUUID(), buffer.readUUID(), buffer.readUnsignedByte()));
    public PetState { remainingTicks = Math.max(0, Math.min(remainingTicks, SeamlessDogs.DURATION)); }
    public Type<? extends DogsPayload> type() { return TYPE; }
}
